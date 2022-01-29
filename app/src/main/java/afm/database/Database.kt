package afm.database

import afm.Main
import afm.anime.Anime
import afm.anime.AnimeType
import afm.anime.Status
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
import java.nio.file.Files
import java.nio.file.Path

// TODO: move MyListKt and ToWatchKt to here?
// or embed them inside Database and instead of loading the db into a data struct

private const val DRIVER = "org.sqlite.JDBC"

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

    // has to be called in a transaction
    private fun Transaction.clearTables() {
        MyListTable.deleteAll()
        ToWatchTable.deleteAll()
    }

    private fun loadAll(task: LoadTask, start: Double, end: Double) {
        val diff: Double = end - start
        val step = diff / 2

        transaction {
            loadTable(MyListKt, MyListTable)
            task.incrementProgress(step)
            loadTable(ToWatchKt, ToWatchTable)
            task.incrementProgress(step)
        }
    }

    // has to be called in a transaction
    private fun Transaction.loadTable(animeList: AnimeList, table: AnimeTable) {
        table.allAnime().forEach { animeList.addSilent(it) }
    }

    @JvmStatic
    fun saveAll() {
        transaction {
            saveTable(MyListKt, MyListTable)
            saveTable(ToWatchKt, ToWatchTable)
        }
    }

    // has to be called in a transaction
    private fun Transaction.saveTable(animeList: AnimeList, table: AnimeTable) {
        val removed = animeList.getRemovedNames()
        table.deleteWhere { table.id inList removed }

        animeList.getAdded().forEach { anime ->
            anime.toAnimeEntity()
        }
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
}

fun main() {
    //ExposedDatabase.db
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            MyListTable,
            ToWatchTable,
            withLogs = false,
        )
        ToWatchTable.allAnime()
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

    abstract val entityClass: EntityClass<String, AnimeEntity>
}

object MyListTable : AnimeTable("mylist") {
    override val entityClass = MyListEntity
}

object ToWatchTable : AnimeTable("towatch") {
    override val entityClass = ToWatchEntity
}

private fun AnimeTable.allAnime(): Collection<Anime> = entityClass.all().map { it.toAnime() }

private fun AnimeEntity.toAnime(): Anime = with (Anime.builder(name)) {
    val entity = this@toAnime

    setId(entity.malId)
    setSynopsis(entity.synopsis)
    setStudios(entity.studios)

    setGenres(entity.genres)

    setImageURL(entity.imageUrl)

    addFillers(entity.fillers)
    setAnimeType(entity.type)
    setStartYear(entity.startYear)
    setStatus(entity.status)

    setEpisodes(entity.episodes)
    setCurrEp(entity.currEp)
    setEpisodeLength(entity.episodeLength)

    setCustom(entity.custom)

    build()
}

// has to be called in a transaction
// workaround to have ext function with 2 receivers
private val Transaction.toAnimeEntity: Anime.() -> AnimeEntity
    get() = fun Anime.(): AnimeEntity {
        val ec: EntityClass<String, AnimeEntity> = if (MyListKt.contains(this))
            MyListEntity.Companion
        else
            ToWatchEntity.Companion

        val anime = this

        // also stores it in db
        return ec.new(anime.name) {
            malId = anime.id
            synopsis = anime.synopsis
            studios = anime.studios
            genres = anime.genres.backingSet

            imageUrl = anime.imageURL

            fillers = anime.fillers
            type = anime.type
            startYear = anime.startYear
            status = anime.status

            episodes = anime.episodes
            currEp = anime.currEp
            episodeLength = anime.episodeLength

            custom = anime.custom
        }
    }
