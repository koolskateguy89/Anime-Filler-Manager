package afm.database

import afm.anime.Anime
import afm.anime.AnimeType
import afm.anime.Status
import afm.common.utils.inJar
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.kxtra.slf4j.getLogger
import java.nio.file.Path
import kotlin.io.path.exists

object Database {

    @Suppress("UNUSED")
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

    private val internalUrl = "jdbc:sqlite:${if (inJar) ":resource:" else "core/src/main/resources/"}databases/animeDB.db"

    @JvmStatic
    var url: String = internalUrl
        set(newUrl) {
            if (newUrl == field)
                return

            if (newUrl == "Internal") {
                if (field != internalUrl) {
                    db = Database.connect(internalUrl, DRIVER)
                    field = internalUrl
                }
            } else if (newUrl.isValidPath()) {
                db = if (!Path.of(newUrl).exists())
                    createNew(newUrl)
                else
                    Database.connect("jdbc:sqlite:$newUrl", DRIVER)
                field = newUrl
            }

        }

    private const val DRIVER = "org.sqlite.JDBC"

    var db: Database = Database.connect(url, DRIVER)
        private set

    @JvmStatic
    fun loadAll() {
        // if (inJar && isFirstRun && url == internalUrl)
        //     transaction(db) { clearTables() }
        // else
        loadAllImpl()
    }

    private fun loadAllImpl() {
        transaction(db) {
            loadTable(MyListKt, MyListTable)
            loadTable(ToWatchKt, ToWatchTable)
        }
    }

    // has to be called in a transaction
    @Suppress("UNUSED")
    private fun Transaction.clearTables() {
        MyListTable.deleteAll()
        ToWatchTable.deleteAll()
    }

    // has to be called in a transaction
    @Suppress("UNUSED")
    private fun Transaction.loadTable(animeList: AnimeList, table: AnimeTable) {
        table.allAnime().forEach { animeList.addSilent(it) }
    }

    @JvmStatic
    fun saveAll() {
        transaction(db) {
            saveTable(MyListKt, MyListTable)
            saveTable(ToWatchKt, ToWatchTable)
        }
    }

    // has to be called in a transaction
    private fun Transaction.saveTable(animeList: AnimeList, table: AnimeTable) {
        val removed = animeList.removedNames
        table.deleteWhere { table.id inList removed }

        animeList.added.forEach { anime ->
            anime.toAnimeEntity()
        }
    }

    // if table already exists: clear it, else create new table
    @JvmStatic
    fun createNew(url: String): Database = Database.connect("jdbc:sqlite:$url", DRIVER).also {
        transaction(it) {
            SchemaUtils.createMissingTablesAndColumns(
                MyListTable,
                ToWatchTable,
                withLogs = false,
            )
            clearTables()
        }
    }
}

private fun String.isValidPath(): Boolean = try {
    Path.of(this)
    true
} catch (_: Exception) {
    false
}

fun main() {
//    afm.database.Database.url = "./test.db"
//    transaction(afm.database.Database.db) {
//        Anime.build("ok mate") {
//            setId(1)
//            setGenres(EnumSet.allOf(Genre::class.java))
//        }.toAnimeEntity()
//    }
    //ExposedDatabase.db
//    transaction(afm.database.Database.db) {
//        SchemaUtils.createMissingTablesAndColumns(
//            MyListTable,
//            ToWatchTable,
//            withLogs = false,
//        )
//        ToWatchTable.allAnime()
//    }
}

// TODO: need to use upsert to insert stuff because will fail unique constraint

/*
Not in 1NF:
studios, (many-many)
genres, (many-many)
fillers (many-many but 'not much point')
 */
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
    override val entityClass = MyListEntity.Companion
}

object ToWatchTable : AnimeTable("towatch") {
    override val entityClass = ToWatchEntity.Companion
}

private fun AnimeTable.allAnime(): Collection<Anime> = entityClass.all().map { it.toAnime() }

private fun AnimeEntity.toAnime(): Anime = Anime.build(name) {
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
}

// has to be called in a transaction
// workaround to have ext function with 2 receivers
@Suppress("UNUSED")
private val Transaction.toAnimeEntity: Anime.() -> AnimeEntity
    get() = {
        val ec: EntityClass<String, AnimeEntity> = if (MyListKt.contains(this))
            MyListEntity.Companion
        else
            ToWatchEntity.Companion

        val anime = this

        // also stores it in db
        ec.new(anime.name) {
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
