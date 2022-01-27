package afm.database

import afm.Main
import afm.anime.Anime
import afm.anime.AnimeType
import afm.anime.Genre
import afm.anime.Season
import afm.anime.Status
import afm.common.utils.NonNullMap
import afm.common.utils.emptyEnumSet
import afm.common.utils.inJar
import afm.common.utils.isFirstRun
import afm.common.utils.wrapAlertText
import afm.screens.version1_start.StartScreen.LoadTask
import afm.user.Settings
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.kxtra.slf4j.getLogger
import org.sqlite.SQLiteDataSource
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement


private val COLUMN_MAP = NonNullMap(
    mapOf(
        "name" to 1,
        "genres" to 2,
        "id" to 3,
        "studio" to 4,
        "seasonString" to 5,
        "info" to 6,
        "custom" to 7,
        "currEp" to 8,
        "totalEps" to 9,
        "imageURL" to 10,
        "fillers" to 11
    )
)

/*
 * I was initially using Serialization (EnumSet is Serializable) to store
 * genreSet in the database (as a Blob) but it was simply wayyyy too slow,
 * so instead I store genreString and when reading in I split & parse into
 * Genre. MUCH, MUCH faster.
 *
 * 	- When serializing, it would take in excess of 30 seconds to load 10 anime in
 * 	  (potentially much longer, I didn't bother to wait for them to all load in).
 *
 *
 * Using a HashMap (columnMap) to map column name to column number for performance
 *
 *
 * Optimised saving to ML/TW by keeping record of which anime were added
 * (/changed) and which anime were removed, only making necessary changes to db.
 *
 * Further optimised it by using 1 PreparedStatement for adding to ML/TW - adding
 * Batches, decreasing number of network round trips to 2 (1 to format ps and 1
 * to do update
 *
 * Also adding anime in batches + using PreparedStatements to further increase performance.
 */
object Database {

    private val logger = getLogger()

    // From SQLiteStudio
    @JvmStatic
    val fileExtensions = arrayOf(
        "*.db",
        "*.db2",
        "*.db3",
        "*.sdb",
        "*.s2db",
        "*.s3db",
        "*.sqlite",
        "*.sqlite2",
        "*.sqlite3",
        "*.sl2",
        "*.sl3",
    )

    private val DB_URL: String = run {
        fun mayBeValidDatabase(url: String): Boolean {
            if (url.isEmpty())
                return false

            val exists: Boolean = Files.exists(Path.of(url))

            if (!exists) {
                logger.warn("Database file does not exist. Falling back on internal")
                Settings.selectedDatabaseProperty.value = "Internal"

                Platform.runLater {
                    val content = """
						Database file does not exist/is not a valid file.
						Falling back on internal database.
						""".trimIndent()

                    Alert(AlertType.ERROR, content).run {
                        initOwner(Main.getStage())
                        wrapAlertText()
                        showAndWait()
                    }
                }
            }

            return exists
        }

        val url: String = Settings.getSelectedDatabase()

        // fallback on internal database if provided url is definitely not valid
        if (url == "Internal" || !mayBeValidDatabase(url)) {
            if (inJar)
                "jdbc:sqlite::resource:databases/animeDB.db"
            else
                /*
                 * Using `Database.javaClass.getResource("/databases/animeDB.db").toString()`
                 * will give the database in target/classes/..., but if not running in jar
                 * (i.e. through IDE or something), really we want to modify the database in
                 * src/main/resources for persistence as target/ is likely to be deleted.
                 */
                "jdbc:sqlite:src/main/resources/databases/animeDB.db"
        } else {
            "jdbc:sqlite:$url"
        }
    }

    private val ds = SQLiteDataSource().apply {
        url = DB_URL
    }

    private const val MYLIST_INSERT_QUERY = """
    INSERT INTO MyList(name, genres, id,
    studio, seasonString, info, custom, currEp,
    totalEps, imageURL, fillers)
    VALUES (?,?,?,?,?,?,?,?,?,?,?)
    """

    private const val TOWATCH_INSERT_QUERY = """
    INSERT INTO ToWatch(name, genres, id,
    studio, seasonString, info, custom, currEp,
    totalEps, imageURL, fillers)
    VALUES (?,?,?,?,?,?,?,?,?,?,?)
    """

    // load myList & toWatch contents into runtime linkedHS's
    @JvmStatic
    fun init(task: LoadTask, start: Double, end: Double) {
        if (inJar && isFirstRun && Settings.getSelectedDatabase() == "Internal")
            clearTables()
        else
            loadAll(task, start, end)

        MyListKt.init()
        ToWatchKt.init()
    }

    // https://stackoverflow.com/a/1604121
    private fun Connection.tableExists(tableName: String): Boolean {
        val sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'"
        createStatement().use { st ->
            st.executeQuery(sql).use { rs ->
                return rs.next()
            }
        }
    }

    private const val CREATE_MYLIST = """
	CREATE TABLE MyList (
		name         STRING  PRIMARY KEY ON CONFLICT REPLACE
		                     UNIQUE ON CONFLICT REPLACE
		                     NOT NULL,
		genres       STRING  NOT NULL,
		id           INT     UNIQUE ON CONFLICT REPLACE,
		studio       STRING,
		seasonString STRING,
		info         STRING,
		custom       BOOLEAN NOT NULL,
		currEp       INT     NOT NULL
		                     DEFAULT (0),
		totalEps     INT     NOT NULL
		                     DEFAULT ( -1),
		imageURL     STRING,
		fillers      STRING
	);
    """

    private const val CREATE_TOWATCH = """
	CREATE TABLE ToWatch (
		name         STRING  PRIMARY KEY ON CONFLICT REPLACE
		                     UNIQUE ON CONFLICT REPLACE
		                     NOT NULL,
		genres       STRING  NOT NULL,
		id           INT     UNIQUE ON CONFLICT REPLACE,
		studio       STRING,
		seasonString STRING,
		info         STRING,
		custom       BOOLEAN NOT NULL,
		currEp       INT     NOT NULL
		                     DEFAULT (0),
		totalEps     INT     NOT NULL
		                     DEFAULT ( -1),
		imageURL     STRING,
		fillers      STRING
	);
	"""

    // if table already exists: clear it, else create new table
    // current impl. means table won't exist but I'll keep it like this for now
    @JvmStatic
    fun createNew(url: String) {
        val ds = SQLiteDataSource()
        ds.url = "jdbc:sqlite:$url"

        ds.connection.use { con ->
            con.createStatement().use { st ->
                if (con.tableExists("MyList"))
                    st.executeUpdate("DELETE FROM MyList")
                else
                    st.execute(CREATE_MYLIST)

                if (con.tableExists("ToWatch"))
                    st.executeUpdate("DELETE FROM ToWatch")
                else
                    st.execute(CREATE_TOWATCH)
            }
        }
    }


    // This is very important for exporting as jar - jar will be effectively 'fresh'
    private fun clearTables() {
        ds.connection.use { con ->
            con.createStatement().use { s ->
                s.addBatch("DELETE FROM MyList")
                s.addBatch("DELETE FROM ToWatch")
                s.executeBatch()
            }
        }
    }

    private fun loadAll(task: LoadTask, start: Double, end: Double) {
        ds.connection.use { con ->
            con.createStatement().use { stmnt ->
                stmnt.queryTimeout = 30

                val halfDiff = (end - start) / 2

                runCatching {
                    loadMyList(stmnt, task, start, end - halfDiff)
                }.recover { e ->
                    logger.error("Loading MyList failed", e)
                }
                kotlin.runCatching {  }.
                // still load ToWatch if there is an error loading MyList
                runCatching {
                    loadToWatch(stmnt, task, start + halfDiff, end)
                }.recover { e ->
                    logger.error("Loading ToWatch failed", e)
                }

                //e?.also { it.printStackTrace() }?.nextException?.printStackTrace()
            }
        }
    }

    // Load contents of MyList table into runtime MyList
    private fun loadMyList(statement: Statement, task: LoadTask, start: Double, end: Double) {
        val diff: Double = end - start
        val step: Double
        val size: Int = getTableSize(statement, "MyList")

        if (size == 0) {
            task.incrementProgress(diff)
            return
        }

        step = diff / size

        statement.executeQuery("SELECT * FROM MyList").use {
            while (it.next()) {
                MyListKt.addSilent(loadAnimeFromResultSet(it))
                task.incrementProgress(step)
            }
        }
    }

    // Load contents of ToWatch table into runtime ToWatch
    private fun loadToWatch(statement: Statement, task: LoadTask, start: Double, end: Double) {
        val diff: Double = end - start
        val step: Double
        val size: Int = getTableSize(statement, "ToWatch")

        if (size == 0) {
            task.incrementProgress(diff)
            return
        }

        step = diff / size

        statement.executeQuery("SELECT * FROM ToWatch").use {
            while (it.next()) {
                ToWatchKt.addSilent(loadAnimeFromResultSet(it))
                task.incrementProgress(step)
            }
        }
    }

    @JvmStatic
    fun saveAll() {
        ds.connection.use {
            it.autoCommit = false

            runCatching {
                saveMyList(it)
            }.recover { e ->
                logger.error("Failed saving MyList", e)
            }
            // try to save ToWatch even if saving MyList fails
            runCatching {
                saveToWatch(it)
            }.recover { e ->
                logger.error("Failed saving ToWatch", e)
            }

            it.commit()
        }
    }

    /*
     * Delete all anime that were removed from MyList, from animeDB database.
     * Save all anime that were added to MyList(runtime) in animeDB database.
     */
    private fun saveMyList(con: Connection) {
        val removed = MyListKt.getRemovedSQL()

        if (removed.isNotEmpty()) {
            con.createStatement().use {
                val removeSQL = "DELETE FROM MyList WHERE name IN ($removed)"
                it.executeUpdate(removeSQL)
            }
        }

        // add anime in batches
        val added = MyListKt.getAdded()
        // FIXME: NPE here somewhere
        if (added.isNotEmpty()) {
            con.prepareStatement(MYLIST_INSERT_QUERY).use {
                var batchSize = 0
                for (anime in added) {
                    anime.prepareStatement(it)
                    it.addBatch()
                    it.clearParameters()

                    // execute 25 batches at a time
                    if (++batchSize >= 25) {
                        it.executeBatch()
                        batchSize = 0
                    }
                }

                // execute rest of batches
                if (batchSize > 0)
                    it.executeBatch()
            }
        }
    }

    /*
     * Delete all anime that were removed from ToWatch, from animeDB database.
     * Save all anime that were added to ToWatch(runtime) in animeDB database.
     */
    private fun saveToWatch(con: Connection) {
        val removed = ToWatchKt.getRemovedSQL()

        if (removed.isNotEmpty()) {
            con.createStatement().use {
                val removeSQL = "DELETE FROM ToWatch WHERE name IN ($removed)"
                it.executeUpdate(removeSQL)
            }
        }

        // add anime in batches
        val added = ToWatchKt.getAdded()
        if (added.isNotEmpty()) {
            con.prepareStatement(TOWATCH_INSERT_QUERY).use {
                var batchSize = 0
                for (anime in added) {
                    anime.prepareStatement(it)
                    it.addBatch()
                    it.clearParameters()

                    // execute 100 batches at a time
                    if (++batchSize >= 100) {
                        it.executeBatch()
                        batchSize = 0
                    }
                }

                // execute rest of batches
                if (batchSize > 0)
                    it.executeBatch()
            }
        }
    }

    private fun loadAnimeFromResultSet(rs: ResultSet): Anime {
        val builder = Anime.builder()

        with(rs) {
            builder.setName(getString(COLUMN_MAP["name"]))
                   .setId(getInt(COLUMN_MAP["id"]))
                   .setSynopsis(getString(COLUMN_MAP["info"]))
                   .setStudios(setOf(getString(COLUMN_MAP["studio"])))
                   .setImageURL(getString(COLUMN_MAP["imageURL"]))

            // TODO: AnimeType, Status
            Season.getSeasonFromToString(
                getString(COLUMN_MAP["seasonString"])
            )?.let { season -> builder.setStartYear(season.year) }
            //builder.setSeason(season)

            builder.setCurrEp(getInt(COLUMN_MAP["currEp"]))
                   .setEpisodes(getInt(COLUMN_MAP["totalEps"]))
            // TODO: episodeLength
                   .setCustom(getBoolean(COLUMN_MAP["custom"]))

            getString(COLUMN_MAP["genres"]).let { genreString ->
                builder.setGenres(
                    genreString.split(", ")
                        .mapTo(emptyEnumSet<Genre>(), Genre::parseGenreFromToString)
                )
            }

            getString(COLUMN_MAP["fillers"])?.let { fillerString ->
                if (fillerString.isNotEmpty())
                    fillerString.split(", ").forEach(builder::addFillerAsString)
            }
        }

        return builder.build()
    }

    private fun getTableSize(s: Statement, tableName: String): Int {
        s.executeQuery("SELECT COUNT(*) from $tableName").use { rs ->
            rs.next()
            return rs.getInt(1)
        }
    }
}

// TODO: move MyListKt and ToWatchKt to here?
// or embed them inside Database and instead of loading the db into a data struct

const val DRIVER = "org.sqlite.JDBC"

object DatabaseNew {

    private val logger = getLogger()

    // From SQLiteStudio
    @JvmStatic
    val fileExtensions = arrayOf(
        "*.db",
        "*.db2",
        "*.db3",
        "*.sdb",
        "*.s2db",
        "*.s3db",
        "*.sqlite",
        "*.sqlite2",
        "*.sqlite3",
        "*.sl2",
        "*.sl3",
    )

    private val DB_URL: String = run {
        fun mayBeValidDatabase(url: String): Boolean {
            if (url.isEmpty())
                return false

            val exists: Boolean = Files.exists(Path.of(url))

            if (!exists) {
                logger.warn("Database file does not exist. Falling back on internal")
                Settings.selectedDatabaseProperty.value = "Internal"

                Platform.runLater {
                    val content = """
						Database file does not exist/is not a valid file.
						Falling back on internal database.
						""".trimIndent()

                    Alert(AlertType.ERROR, content).run {
                        initOwner(Main.getStage())
                        wrapAlertText()
                        showAndWait()
                    }
                }
            }

            return exists
        }

        val url: String = Settings.getSelectedDatabase()

        // fallback on internal database if provided url is definitely not valid
        if (url == "Internal" || !mayBeValidDatabase(url)) {
            if (inJar)
                "jdbc:sqlite::resource:databases/animeDB.db"
            else
            /*
             * Using `Database.javaClass.getResource("/databases/animeDB.db").toString()`
             * will give the database in target/classes/..., but if not running in jar
             * (i.e. through IDE or something), really we want to modify the database in
             * src/main/resources for persistence as target/ is likely to be deleted.
             */
                "jdbc:sqlite:src/main/resources/databases/animeDB.db"
        } else {
            "jdbc:sqlite:$url"
        }
    }

    val db: Database = Database.connect(DB_URL, DRIVER)
        .also { TransactionManager.defaultDatabase = it }

    @JvmStatic
    fun init(task: LoadTask, start: Double, end: Double) {
        if (inJar && isFirstRun && Settings.getSelectedDatabase() == "Internal")
            transaction { clearTables() }
        else
            loadAll(task, start, end)

        MyListKt.init()
        ToWatchKt.init()
    }

    private fun Transaction.clearTables() {
        MyListTable.deleteAll()
        ToWatchTable.deleteAll()
    }

    // if table already exists: clear it, else create new table
    // current impl. means table won't exist, but I'll keep it like this for now
    @JvmStatic
    fun createNew(url: String) {
        transaction(Database.connect("jdbc:sqlite:$url", DRIVER)) {
            SchemaUtils.createMissingTablesAndColumns(
                MyListTable,
                ToWatchTable,
                withLogs = false,
            )
            clearTables()
        }
    }

    private fun loadAll(task: LoadTask, start: Double, end: Double) {
        val diff: Double = end - start
        val step = diff / 2

        transaction {
            MyListTable.selectAllAnime().forEach { MyListKt.addSilent(it) }
            task.incrementProgress(step)
            ToWatchTable.selectAllAnime().forEach { ToWatchKt.addSilent(it) }
            task.incrementProgress(step)
        }
    }

    @JvmStatic
    fun saveAll() {
        transaction {
            val mlRemoved = MyListKt.getRemovedNames()
            //MyListTable.deleteWhere { MyListTable.id inList mlRemoved }
            MyListTable.deleteAnimeWithNames(mlRemoved)

            val twRemoved = ToWatchKt.getRemovedNames()
            //ToWatchTable.deleteWhere { ToWatchTable.id inList twRemoved }
            ToWatchTable.deleteAnimeWithNames(twRemoved)

            // TODO: added anime
        }
    }

}

fun main() {
    //ExposedDatabase.db
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            MyListTable,
            ToWatchTable,
            withLogs = false,
        )
        ToWatchTable.selectAllAnime()
    }
}

// TODO I need to use upsert to insert stuff

sealed class AnimeTable(name: String) : IdTable<String>(name) {
    // id === name
    final override val id: Column<EntityID<String>> = varchar("name", 30).entityId()
    final override val primaryKey = PrimaryKey(this.id)

    val malId = integer("mal_id").nullable().uniqueIndex()
    val synopsis = text("synopsis").default("")
    val studios = text("studios").default("[]") // delimited
    val genres = text("genres").default("[]") // ordinals delimited
    val imageUrl = text("imageURL").nullable()

    val fillers = text("fillers").default("[]")

    val type = enumeration("type", AnimeType::class).default(AnimeType.UNKNOWN)
    val startYear = integer("start_year").default(0)
    val status = enumeration("status", Status::class).default(Status.UNKNOWN)

    val totalEps = integer("total_episodes").default(Anime.NOT_FINISHED)
    val currEp = integer("current_episode").default(0)
    val episodeLength = integer("episode_length").default(0)

    val custom = bool("custom").default(false)

    abstract val entityClass: EntityClass<String, out AnimeEntity>
}

object MyListTable : AnimeTable("mylist") {
    override val entityClass = MyListEntity
}

object ToWatchTable : AnimeTable("towatch") {
    override val entityClass = ToWatchEntity
}

private fun AnimeTable.selectAllAnime(): Set<Anime> = entityClass.all()
    //.onEach { println(it) }
    .map { it.toAnime() }
    .toSet()

private fun AnimeTable.deleteAnimeWithNames(names: Iterable<String>) {
    deleteWhere { this@deleteAnimeWithNames.id inList names }
}
