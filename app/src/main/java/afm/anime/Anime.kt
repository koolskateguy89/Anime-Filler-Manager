package afm.anime

import afm.common.utils.ImmutableEnumSet
import afm.common.utils.immutable
import afm.common.utils.makeButtonProperty
import afm.common.utils.setAll
import afm.common.utils.setStyleClass
import afm.database.MyListKt
import afm.database.ToWatchKt
import afm.screens.Menu
import afm.screens.infowindows.MyListInfoWindow
import afm.screens.infowindows.ResultInfoWindow
import afm.screens.infowindows.ToWatchInfoWindow
import com.github.koolskateguy89.filler.Filler
import javafx.beans.property.Property
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.image.Image
import java.util.EnumSet
import java.util.TreeSet

private val DEFAULT_EPISODE_LENGTH = EpisodeLength(0)

class AnimeBuilder(var name: String) {

    // default values
    var id: Int? = null //for MAL website
        private set
    var info = ""
        private set
    val studios = mutableSetOf<String>()
    val genres: EnumSet<Genre> = EnumSet.noneOf(Genre::class.java)
    var imageURL: String? = null
        private set

    val fillers = TreeSet<Filler>()

    var type = AnimeType.UNKNOWN
        private set
    var startYear = 0
        private set
    var status = Status.UNKNOWN
        private set

    var episodes = Anime.NOT_FINISHED
        private set
    var currEp = 1
        private set
    var episodeLength: EpisodeLength = DEFAULT_EPISODE_LENGTH
        private set

    var custom = false
        private set

    fun reset() {
        name = ""
        id = null
        info = ""
        studios.clear()
        genres.clear()
        imageURL = null
        fillers.clear()
        type = AnimeType.UNKNOWN
        startYear = 0
        status = Status.UNKNOWN
        episodes = Anime.NOT_FINISHED
        episodeLength = DEFAULT_EPISODE_LENGTH
        currEp = 1
        custom = false
    }

    fun setName(name: String): AnimeBuilder {
        this.name = name
        return this
    }

    fun setId(id: Int?): AnimeBuilder {
        this.id = id
        return this
    }

    fun setSynopsis(synopsis: String): AnimeBuilder {
        info = synopsis
        return this
    }

    fun setStudios(studios: Collection<String>): AnimeBuilder {
        this.studios.setAll(studios)
        return this
    }

    fun setGenres(genres: Collection<Genre>): AnimeBuilder {
        require(!genres.isEmpty()) { "Genres cannot be empty" }
        this.genres.setAll(genres)
        return this
    }

    fun addGenre(genre: Genre): AnimeBuilder {
        genres.add(genre)
        return this
    }

    fun addGenres(genres: Collection<Genre>): AnimeBuilder {
        this.genres.addAll(genres)
        return this
    }

    fun setImageURL(url: String?): AnimeBuilder {
        imageURL = url
        return this
    }

    fun addFiller(filler: Filler): AnimeBuilder {
        fillers.add(filler)
        return this
    }

    fun addFillerAsString(s: String): AnimeBuilder {
        return addFiller(Filler.valueOf(s))
    }

    fun addFillers(fillers: Collection<Filler>): AnimeBuilder {
        this.fillers.addAll(fillers)
        return this
    }

    fun setAnimeType(type: AnimeType): AnimeBuilder {
        this.type = type
        return this
    }

    fun setStartYear(startYear: Int): AnimeBuilder {
        this.startYear = startYear
        return this
    }

    fun setStatus(status: Status): AnimeBuilder {
        this.status = status
        return this
    }

    fun setEpisodes(episodes: Int?): AnimeBuilder {
        this.episodes = episodes ?: Anime.NOT_FINISHED
        this.episodes = this.episodes.coerceAtLeast(Anime.NOT_FINISHED)
        return this
    }

    fun setEpisodeLength(episodeLength: EpisodeLength): AnimeBuilder {
        this.episodeLength = episodeLength
        return this
    }

    fun setCurrEp(currEp: Int?): AnimeBuilder {
        this.currEp = currEp ?: 1
        return this
    }

    fun setCustom(custom: Boolean): AnimeBuilder {
        this.custom = custom
        return this
    }

    fun build(): Anime = Anime(this)

}

class Anime(builder: AnimeBuilder) {

    val name: String = builder.name
    val id: Int? = builder.id //for MAL website
    val synopsis: String = builder.info
    val studios: Set<String> = builder.studios.toSet()

    val genres: ImmutableEnumSet<Genre> = builder.genres.immutable()
    @Suppress("UNUSED")
    val genreString: String = genres.joinToString(", ") { it.toString() }

    val imageURL: String? = builder.imageURL
    val image: Image? by lazy { imageURL?.let { Image(it) } }

    val fillers = TreeSet<Filler>().apply {
        if (!builder.custom) {
            // If the anime is not finished or doesn't already have any filler,
            // check for any fillers
            if (builder.fillers.isEmpty() || builder.episodes == NOT_FINISHED) {
                findFillers()
            } else {
                addAll(builder.fillers)
            }
        }
    }

    val type: AnimeType = builder.type
    val startYear: Int = builder.startYear
    val status: Status = builder.status

    val episodes: Int = builder.episodes
    var currEp: Int = builder.currEp
        set(episode) {
            if (episode in episodeRange) {
                field = episode
            } else if (episode < 0) {
                field = 0
            }
        }

    private val episodeRange: IntRange = if (episodes == NOT_FINISHED)
        0..Integer.MAX_VALUE
    else
        0..episodes

    val episodeLength: EpisodeLength = builder.episodeLength

    @get:JvmName("isCustom")
    val custom: Boolean = builder.custom

    override fun toString(): String {
        return name + (if (studios.isEmpty()) "" else " studios = $studios") +
                if (episodes == NOT_FINISHED || episodes == 0) "" else " episode(s) = $episodes"
    }

    // Only search for filler if anime is not custom and has more than 48 episodes or is not finished
    fun findFillers() {
        if (custom || episodes < 48 && episodes != NOT_FINISHED)
            return

        fillers.setAll(Filler.getFillers(name))
    }

    // takes filler into account
    fun getNextEpisode(): String {
        var nextEp = currEp + 1

        for (filler in fillers) {
            if (nextEp in filler) {
                // 'skip' to the end of the filler range
                // this is fine as consecutive filler SHOULD be in the same range
                nextEp = filler.end + 1
                break
            }
            if (currEp < filler.start) {
                // if this clause is triggered, we have gone past our episode,
                // this should happen if the next episode is not filler
                break
            }
        }

        return if (nextEp > episodes && episodes != NOT_FINISHED) "-" else nextEp.toString()
    }

    fun getUrl(): String? {
        return if (custom || id == null) null else "https://myanimelist.net/anime/$id"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return other is Anime
                && name == other.name
                && studios == other.studios
                && genres == other.genres
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + studios.hashCode()
        result = 31 * result + genres.hashCode()
        return result
    }

    companion object {
        //episode count
        const val NOT_FINISHED = -1

        @JvmField
        var SORT_BY_NAME: Comparator<Anime> = Comparator { a1, a2 ->
            a1.name.compareTo(a2.name, ignoreCase = true)
        }

        @JvmField
        var SORT_BY_NAME_DESC: Comparator<Anime> = SORT_BY_NAME.reversed()

        @JvmStatic
        fun builder(name: String = ""): AnimeBuilder = AnimeBuilder(name)

        private val HIGHLIGHT = Menu.SELECTED
        private const val SEE_INFO = "See info"
    }

    //<editor-fold desc="Buttons">
    /* Everything following this is to help ResultsScreen with anime
	 * returned from search results:
	 *
	 * - if an anime is already in ML, both button disable & highlight
	 *   ML btn;
	 * - if an anime is already in TW, both button disable & highlight
	 *   TW btn;
	 * - else, set up both buttons to do on action:
	 *      * add anime to respective location (ML/TW)
	 *      * disable both buttons
	 *
	 * - infoBtn - highlight & become mouse transparent (see below),
	 *             InfoWindow will make it mouse non-transparent and unhighlight it
	 *
	 *
	 *  Neither buttons actually become disabled, just made "mouse transparent"
	 *  which means mouse events called on them are ignored
	 *
	 *  mouseTransparent property is:
	 *  "If true, this node (together with all its children) is completely
	 *   transparent to mouse events. When choosing target for mouse event,
	 *   nodes with mouseTransparent set to true and their subtrees won'
	 *   be taken into account."
	 *
	 *
	 *  Also helps MyListScreen and ToWatchScreen to have appropriately functioning buttons
	 */

    /* for ResultsScreen */
    private val infoBtn = Button(SEE_INFO).apply {
        onAction = EventHandler {
            setStyleClass(HIGHLIGHT)
            ResultInfoWindow.open(this@Anime, this, myListBtn, toWatchBtn)
        }
    }
    private val myListBtn = Button("Add")
    private val toWatchBtn = Button("Add")

    init {
        when (this) {
            in MyListKt -> { // anime is already in MyList
                myListBtn.setStyleClass(HIGHLIGHT)
                myListBtn.isMouseTransparent = true
                toWatchBtn.isMouseTransparent = true
            }
            in ToWatchKt -> { // anime is already in ToWatch
                toWatchBtn.setStyleClass(HIGHLIGHT)
                toWatchBtn.isMouseTransparent = true
                myListBtn.isMouseTransparent = true
            }
            else -> { // anime is in neither MyList nor ToWatch
                myListBtn.onAction = EventHandler {
                    MyListKt.add(this)
                    myListBtn.setStyleClass(HIGHLIGHT)

                    myListBtn.isMouseTransparent = true
                    toWatchBtn.isMouseTransparent = true
                }

                toWatchBtn.onAction = EventHandler {
                    ToWatchKt.add(this)
                    toWatchBtn.setStyleClass(HIGHLIGHT)

                    toWatchBtn.isMouseTransparent = true
                    myListBtn.isMouseTransparent = true
                }
            }
        }
    }

    // basically return infoBtn
    @Suppress("UNUSED")
    fun infoBtnProperty(): Property<Button> {
        return makeButtonProperty("InfoBtnProperty", infoBtn)
    }

    // basically return myListBtn
    @Suppress("UNUSED")
    fun myListBtnProperty(): Property<Button> {
        return makeButtonProperty("MyListBtnProperty", myListBtn)
    }

    // basically return toWatchBtn
    @Suppress("UNUSED")
    fun toWatchBtnProperty(): Property<Button> {
        return makeButtonProperty("ToWatchBtnProperty", toWatchBtn)
    }


    /* for MyListScreen */

    private val myListInfoBtn = Button(SEE_INFO).apply {
        onAction = EventHandler {
            setStyleClass(HIGHLIGHT)
            MyListInfoWindow.open(this@Anime, this)
        }
    }
    private val myListRemoveBtn = Button("Remove").apply {
        onAction = EventHandler {
            MyListKt.remove(this@Anime)
        }
    }
    private val moveToToWatchBtn = Button("Move").apply {
        onAction = EventHandler {
            MyListKt.remove(this@Anime)
            ToWatchKt.add(this@Anime)
        }
    }

    @Suppress("UNUSED")
    fun myListInfoProperty(): Property<Button> {
        return makeButtonProperty("MyListInfoBtnProperty", myListInfoBtn)
    }

    @Suppress("UNUSED")
    fun myListRemoveProperty(): Property<Button> {
        return makeButtonProperty("MyListRemoveBtnProperty", myListRemoveBtn)
    }

    @Suppress("UNUSED")
    fun moveToToWatchProperty(): Property<Button> {
        return makeButtonProperty("MoveToToWatchBtnProperty", moveToToWatchBtn)
    }


    /* for ToWatchScreen */

    private val toWatchInfoBtn = Button(SEE_INFO).apply {
        onAction = EventHandler { event: ActionEvent? ->
            setStyleClass(HIGHLIGHT)
            ToWatchInfoWindow.open(this@Anime, this)
        }
    }
    private val toWatchRemoveBtn = Button("Remove").apply {
        onAction = EventHandler {
            ToWatchKt.remove(this@Anime)
        }
    }
    private val moveToMyListBtn = Button("Move").apply {
        onAction = EventHandler {
            ToWatchKt.remove(this@Anime)
            MyListKt.add(this@Anime)
        }
    }

    @Suppress("UNUSED")
    fun toWatchInfoProperty(): Property<Button> {
        return makeButtonProperty("ToWatchInfoBtnProperty", toWatchInfoBtn)
    }

    @Suppress("UNUSED")
    fun toWatchRemoveProperty(): Property<Button> {
        return makeButtonProperty("ToWatchRemoveBtnProperty", toWatchRemoveBtn)
    }

    @Suppress("UNUSED")
    fun moveToMyListProperty(): Property<Button> {
        return makeButtonProperty("MoveToMyListBtnProperty", moveToMyListBtn)
    }
    //</editor-fold>

}
