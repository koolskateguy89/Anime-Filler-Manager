package afm.anime;

import static afm.utils.Utils.makeButtonProperty;
import static afm.utils.Utils.setStyleClass;
import static java.util.Objects.requireNonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import afm.database.MyList;
import afm.database.ToWatch;
import afm.screens.Menu;
import afm.screens.infowindows.MyListInfoWindow;
import afm.screens.infowindows.ResultInfoWindow;
import afm.screens.infowindows.ToWatchInfoWindow;

/*
 * Saving into database:
 *
 * 1  +name - unique String
 * 2  +genres - non-null String (storing genreString)
 * 3  +id - unique int
 * 4  +studio - String
 * 5  +seasonString - String (using (Season).toString) (will need to be parsed back into Season)
 * 6  +info - String
 * 7  +custom - non-null boolean
 * 8  +currEp - non-null int default 0
 * 9  +totalEps - non-null int default -1 (NOT_FINISHED)
 * 10 +imageURL - String
 * 11 +fillers - String (fillers as a String)
 *
 */
@EqualsAndHashCode(doNotUseGetters = true,
				   onlyExplicitlyIncluded = true,
				   cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
public final class Anime {

	//episode count
	public static final int NOT_FINISHED = -1;

	public static final Comparator<Anime> SORT_BY_NAME,
									 	  SORT_BY_NAME_DESC;

	static {
		SORT_BY_NAME = (Anime a1, Anime a2) -> a1.name.compareToIgnoreCase(a2.name);
		SORT_BY_NAME_DESC = SORT_BY_NAME.reversed();
	}

	public static AnimeBuilder builder() {
		return new AnimeBuilder("");
	}

	public static AnimeBuilder builder(String name) {
		return new AnimeBuilder(name);
	}


	/* [mutable] Builder class for Anime so Anime can be effectively immutable
	 *
	 * also it's a static class so it can be instantiated in a stand-alone
	 * manner (no inner reference to an instance of Anime)
	 */
	public static final class AnimeBuilder {

		private String imageURL;
		private String name;
		private String studio;
		private Season season;
		private EnumSet<Genre> genres = EnumSet.noneOf(Genre.class);
		private TreeSet<Filler> fillers = new TreeSet<>();
		private String info;

		private int episodes = Anime.NOT_FINISHED;
		private int currEp = 0;
		private Integer id; //for MAL website
		private boolean custom = false;

		/** AnimeBuilder should be instantiated using {@link Anime#builder} */
		private AnimeBuilder(String name) {
			this.name = requireNonNull(name);
		}

		public void clear() {
			name = null;
			studio = null;
			season = null;
			info = null;

			imageURL = null;

			genres.clear();
			fillers.clear();

			id = null;
		}

		public AnimeBuilder setName(String name) {
			this.name = requireNonNull(name);
			return this;
		}

		public AnimeBuilder setStudio(String studio) {
			this.studio = studio;
			return this;
		}

		public final AnimeBuilder setSeason(Season season) {
			this.season = season;
			return this;
		}

		public final AnimeBuilder setImageURL(String url) {
			imageURL = url;
			return this;
		}

		public AnimeBuilder setGenres(EnumSet<Genre> genres) {
			if (genres.isEmpty())
				throw new IllegalArgumentException("Genres cannot be empty");

			this.genres = genres.clone();
			return this;
		}

		public AnimeBuilder setGenres(Collection<Genre> genres) {
			if (genres.isEmpty())
				throw new IllegalArgumentException("Genres cannot be empty");

			this.genres.clear();
			this.genres.addAll(genres);
			return this;
		}

		public AnimeBuilder addGenre(Genre genre) {
			this.genres.add(genre);
			return this;
		}

		public AnimeBuilder addFiller(Filler filler) {
			fillers.add(filler);
			return this;
		}

		public AnimeBuilder addFillerAsString(String s) {
			return addFiller(Filler.parseFiller(s));
		}

		public AnimeBuilder setInfo(String info) {
			this.info = info;
			return this;
		}

		public AnimeBuilder setEpisodes(int episodes) {
			this.episodes = episodes;
			return this;
		}

		public AnimeBuilder setCurrEp(int currEp) {
			this.currEp = currEp;
			return this;
		}

		public AnimeBuilder setId(int id) {
			this.id = id;
			return this;
		}

		public AnimeBuilder setCustom(boolean custom) {
			this.custom = custom;
			return this;
		}

		public Anime build() {
			return new Anime(this);
		}
	}

	@EqualsAndHashCode.Include @Getter private final String name;
	@EqualsAndHashCode.Include @Getter private final String studio;
	@EqualsAndHashCode.Include @Getter private final Season season;

	@EqualsAndHashCode.Include private final ImmutableSet<Genre> genres;
	@Getter private final String genreString;

	@Getter private TreeSet<Filler> fillers = new TreeSet<>();

	@Getter private final String info;

	private final String imageURL;
	private Image image;

	@Getter private final int episodes;
	private int currEp;
	@Getter private final Integer id; //for MAL website
	@Getter private final boolean custom;

	private final Range<Integer> episodeRange;

	@SuppressWarnings("unchecked")
	private Anime(AnimeBuilder builder) {
		name = requireNonNull(builder.name);

		genres = Sets.immutableEnumSet(builder.genres);
		genreString = genres.stream().map(Genre::toString).collect(Collectors.joining(", "));

		studio = builder.studio;
		season = builder.season;
		info = builder.info;

		imageURL = builder.imageURL;

		currEp = builder.currEp;
		episodes = builder.episodes;
		episodeRange = (episodes == NOT_FINISHED) ? Range.atLeast(0) : Range.closed(0, episodes);

		custom = builder.custom;
		id = builder.id;

		// It only has fillers if the anime is not custom
		if (!custom) {
			// If the anime is not finished or doesn't already have any filler,
			// check for any fillers
			if (builder.fillers.isEmpty() || episodes == NOT_FINISHED) {
				findFillers();
			} else {
				fillers = (TreeSet<Filler>) builder.fillers.clone();
			}
		}

		initBtns();
	}

	@Override
	public String toString() {
		return name + ( (studio == null || studio.equals("-"))? "" : ("  studio = "+studio) ) +
					  ( (episodes == Anime.NOT_FINISHED || episodes == 0)? "" : "  episode(s) = "+episodes );
	}

	// Set up a PreparedStatement to be ready to write this anime into database
	public void prepareStatement(PreparedStatement ps) throws SQLException {
		ps.setString(1, this.name.replace(";", ""));

		//ps.setBytes(2, Utils.serialize(this.genres));
		ps.setString(2, this.genreString);

		if (this.id != null)
			ps.setInt(3, this.id);
		ps.setString(4, this.studio.replace(";", ""));

		if (this.season != null)
			ps.setString(5, this.season.toString());
		ps.setString(6, this.info);
		ps.setBoolean(7, this.custom);

		ps.setInt(8, this.currEp);
		ps.setInt(9, this.episodes);
		ps.setString(10, this.imageURL);

		String fillerString = this.fillers.stream().map(Filler::toString).collect(Collectors.joining(", "));
		ps.setString(11, fillerString);
	}

	// Only search for filler if anime is not custom and has more than 48 episodes or is not finished
	public void findFillers() {
		if ((custom) || (episodes < 48 && episodes != NOT_FINISHED))
			return;

		fillers.clear();
		fillers.addAll(Filler.getFillers(name));
	}

	// defensively copy
	public EnumSet<Genre> getGenres() {
		return EnumSet.copyOf(genres);
	}

	public Image getImage() {
		if (imageURL == null)
			return null;

		if (image == null) {
			image = new Image(imageURL);
		}

		return image;
	}

	// taking filler into account
	public String getNextEpisode() {
		int nextEp = currEp + 1;

		for (Filler filler : fillers) {

			if (filler.contains(nextEp)) {
				// 'skip' to the end of the filler range
				// this is fine as consecutive filler SHOULD be in the same range
				nextEp = filler.end() + 1;
				break;
			}

			if (currEp < filler.start()) {
				// if this if clause is triggered, we have gone past our episode,
				// this should happen if the next episode is not filler
				break;
			}
		}
		return (nextEp > episodes && episodes != Anime.NOT_FINISHED) ? "-" : Integer.toString(nextEp);
	}

	public String getURL() {
		return (custom || id == null) ? null : "https://myanimelist.net/anime/" + id;
	}

	public int getCurrEp() {
		return currEp;
	}

	public void setCurrEp(int currEp) {
		if (episodeRange.contains(currEp)) {
			this.currEp = currEp;
		} else if (currEp < 0) {
			this.currEp = 0;
		}
	}


	/*
	 * I could make it lazily load the buttons instead
	 * Or just have one infowindow with 3 buttons and change them as needed
	 */

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

	private static final List<String> HIGHLIGHT = Menu.SELECTED;
	private static final String SEE_INFO = "See info";

	private void initBtns() {
		initResultBtns();
		initMyListBtns();
		initToWatchBtns();
	}
	/* for ResultsScreen */

	private Button infoBtn;
	private Button myListBtn;
	private Button toWatchBtn;

	private void initResultBtns() {
		myListBtn = new Button("Add");
		toWatchBtn = new Button("Add");
		infoBtn = new Button(SEE_INFO);

		infoBtn.setOnAction(event -> {
			setStyleClass(infoBtn, HIGHLIGHT);
			ResultInfoWindow.open(this, infoBtn, myListBtn, toWatchBtn);
		});

		// anime is already in MyList
		if (MyList.contains(this)) {
			setStyleClass(myListBtn, HIGHLIGHT);
			myListBtn.setMouseTransparent(true);
			toWatchBtn.setMouseTransparent(true);
		// anime is already in ToWatch
		} else if (ToWatch.contains(this)) {
			setStyleClass(toWatchBtn, HIGHLIGHT);
			toWatchBtn.setMouseTransparent(true);
			myListBtn.setMouseTransparent(true);
		// anime is in neither MyList nor ToWatch
		} else {
			myListBtn.setOnAction(event -> {
				MyList.add(this);
				setStyleClass(myListBtn, HIGHLIGHT);

				myListBtn.setMouseTransparent(true);
				toWatchBtn.setMouseTransparent(true);
			});

			toWatchBtn.setOnAction(event -> {
				ToWatch.add(this);
				setStyleClass(toWatchBtn, HIGHLIGHT);

				toWatchBtn.setMouseTransparent(true);
				myListBtn.setMouseTransparent(true);
			});
		}
	}

	// basically return infoBtn
	public Property<Button> infoBtnProperty() {
		return makeButtonProperty("InfoBtnProperty", infoBtn);
	}

	// basically return myListBtn
	public Property<Button> myListBtnProperty() {
		return makeButtonProperty("MyListBtnProperty", myListBtn);
	}

	// basically return toWatchBtn
	public Property<Button> toWatchBtnProperty() {
		return makeButtonProperty("ToWatchBtnProperty", toWatchBtn);
	}


	/* for MyListScreen */

	private Button myListInfoBtn;
	private Button myListRemoveBtn;
	private Button moveToToWatchBtn;

	private void initMyListBtns() {
		myListInfoBtn = new Button(SEE_INFO);
		myListRemoveBtn = new Button("Remove");
		moveToToWatchBtn = new Button("Move");

		myListInfoBtn.setOnAction(event -> {
			setStyleClass(myListInfoBtn, HIGHLIGHT);
			MyListInfoWindow.open(this, myListInfoBtn);
		});

		myListRemoveBtn.setOnAction(event ->
			MyList.remove(this)
		);

		moveToToWatchBtn.setOnAction(event -> {
			MyList.remove(this);
			ToWatch.add(this);
		});
	}

	public Property<Button> myListInfoProperty() {
		return makeButtonProperty("MyListInfoBtnProperty", myListInfoBtn);
	}

	public Property<Button> myListRemoveProperty() {
		return makeButtonProperty("MyListRemoveBtnProperty", myListRemoveBtn);
	}

	public Property<Button> moveToToWatchProperty() {
		return makeButtonProperty("MoveToToWatchBtnProperty", moveToToWatchBtn);
	}


	/* for ToWatchScreen */

	private Button toWatchInfoBtn;
	private Button toWatchRemoveBtn;
	private Button moveToMyListBtn;

	private void initToWatchBtns() {
		toWatchInfoBtn = new Button(SEE_INFO);
		toWatchRemoveBtn = new Button("Remove");
		moveToMyListBtn = new Button("Move");

		toWatchInfoBtn.setOnAction(event -> {
			setStyleClass(toWatchInfoBtn, HIGHLIGHT);
			ToWatchInfoWindow.open(this, toWatchInfoBtn);
		});

		toWatchRemoveBtn.setOnAction(event ->
			ToWatch.remove(this)
		);

		moveToMyListBtn.setOnAction(event -> {
			ToWatch.remove(this);
			MyList.add(this);
		});
	}

	public Property<Button> toWatchInfoProperty() {
		return makeButtonProperty("ToWatchInfoBtnProperty", toWatchInfoBtn);
	}

	public Property<Button> toWatchRemoveProperty() {
		return makeButtonProperty("ToWatchRemoveBtnProperty", toWatchRemoveBtn);
	}

	public Property<Button> moveToMyListProperty() {
		return makeButtonProperty("MoveToMyListBtnProperty", moveToMyListBtn);
	}
}
