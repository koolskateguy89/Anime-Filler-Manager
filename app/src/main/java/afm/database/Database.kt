package afm.database

import afm.Main
import afm.anime.Anime
import afm.anime.Genre
import afm.anime.Season
import afm.common.utils.NonNullMap
import afm.common.utils.inJar
import afm.common.utils.isFirstRun
import afm.common.utils.wrapAlertText
import afm.screens.version1_start.StartScreen.LoadTask
import afm.user.Settings
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import org.sqlite.SQLiteDataSource
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.EnumSet

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
        val url: String? = Settings.getSelectedDatabase()

        // fallback on internal database
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

    private fun mayBeValidDatabase(url: String?): Boolean {
        if (url.isNullOrEmpty())
            return false

        try {
            // go to catch clause if not exists
            require(Files.exists(Path.of(url)))

            return true
        } catch (e: Exception) {
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

        return false
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

    private val ds = SQLiteDataSource().apply {
        url = DB_URL
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
            con.createStatement().use {
                it.queryTimeout = 30

                val halfDiff = (end - start) / 2

                // still load ToWatch if there is an error loading MyList
                // TODO: convert loadAll.try to runCatching
                try {
                    loadMyList(it, task, start, end - halfDiff)
                } catch (e: SQLException) {
                    try {
                        loadToWatch(it, task, start + halfDiff, end)
                    } catch (e2: SQLException) {
                        e.nextException = e2
                    }
                    throw e
                }

                loadToWatch(it, task, start + halfDiff, end)
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
            // TODO: convert saveAll.try to runCatching
            try {
                saveMyList(it)
            } catch (e: SQLException) {
                // try to save ToWatch even if saving MyList fails
                try {
                    saveToWatch(it)
                    it.commit()
                } catch (e1: SQLException) {
                    e.nextException = e1
                }
                throw e
            }
            saveToWatch(it)
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

        rs.run {
            builder.setName(getString(COLUMN_MAP["name"]))
                .setStudios(setOf(getString(COLUMN_MAP["studio"])))
                .setInfo(getString(COLUMN_MAP["info"]))

            val season: Season? = Season.getSeasonFromToString(
                getString(COLUMN_MAP["seasonString"])
            )
            //builder.setSeason(season)

            builder.setCurrEp(getInt(COLUMN_MAP["currEp"]))
                .setEpisodes(getInt(COLUMN_MAP["totalEps"]))

            builder.setImageURL(getString(COLUMN_MAP["imageURL"]))

            builder.setId(getInt(COLUMN_MAP["id"]))
                .setCustom(getBoolean(COLUMN_MAP["custom"]))

            val genreString = getString(COLUMN_MAP["genres"])
            val genreSet = genreString.split(", ")
                .map(Genre::parseGenreFromToString)
                .toCollection(EnumSet.noneOf(Genre::class.java))

            builder.setGenres(genreSet)

            val fillerString = rs.getString(COLUMN_MAP["fillers"])
            if (!fillerString.isNullOrEmpty())
                for (f in fillerString.split(", "))
                    builder.addFillerAsString(f)
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
// or embed them inside Database and instead of loading the db into a data struct, query it
// every time? (mongoDB)
// then rename interface Funcs to Database?
// then rename object Database to ???
// or if embed then e.g. Database.MyList.add(...) etc.
