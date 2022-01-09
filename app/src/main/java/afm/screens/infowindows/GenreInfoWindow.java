package afm.screens.infowindows;

import java.io.IOException;
import java.util.EnumMap;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputEvent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import afm.Main;
import afm.anime.Genre;
import afm.common.utils.Utils;
import afm.screens.Menu;

public class GenreInfoWindow extends InfoWindow {

	// maybe this should be in Genre class
	private static ImmutableMap<Genre, String> genreDefMap;
	/* // problem is that it won't be backed by an EnumMap :/
	private static final ImmutableMap<Genre, String> genreDefMap =
			ImmutableMap.<Genre, String>builderWithExpectedSize(Genre.values().length)
					.put(Genre.Action, """
								Plays out mainly through a clash of physical forces. Frequently these
								stories have fast cuts, tough characters making quick decisions and
								usually a beautiful girl nearby. Anything quick and most likely a thin
								storyline.
								""")
					.put(Genre.Adventure, "Travelling and undertaking an adventure in a certain place")
					.put(Genre.AvantGarde, "Anime that have mind-twisting plots.")
					.put(Genre.AwardWinning, "Won a major award.")
					.put(Genre.BoysLove, "Gay.")
					.put(Genre.Cars, "Involves cars as a main feature e.g. racing")
					.put(Genre.Comedy, "Aims to make you laugh")
					//.put(Genre.Comedy, "Anime: *plays*" + "\n" + "You: *laughs*")
					.put(Genre.Demons, "Involves demons as a main feature")
					.put(Genre.Drama, """
								Anime that often show life or characters through conflict and
								emotions. In general, the different parts of the story tend to
								form a whole that is greater than the sum of the parts. In other
								words, the story has a message that is bigger than just the story
								line itself.
								""")
					.put(Genre.Ecchi, "Almost cartoon porn.")
					.put(Genre.Erotica, "\uD83D\uDE33")
					.put(Genre.Fantasy, "Based in a fanatical world.")
					.put(Genre.Game, """
								Central theme is based on a non-violent, non-sports game, like
								go, chess, trading card games or computer/video games.
								""")
					.put(Genre.GirlsLove, "Lesbian.")
					.put(Genre.Gourmet, "Food.")
					.put(Genre.Harem, "Not many guys, but many, many girls.")
					.put(Genre.Hentai, "Cartoon porn.")
					.put(Genre.Historical, "Based on past historical events.")
					.put(Genre.Horror, "ooooo scarryyyy.")
					.put(Genre.Josei, "Aimed towards young adult women.")
					.put(Genre.Kids, """
								Aimed at kids

								yikes
								""")
					.put(Genre.Magic, "Involves magic as a main feature.")
					.put(Genre.MartialArts, "MARTIAL ART.")
					.put(Genre.Mecha, "Involves robotic entities as a main feature.")
					.put(Genre.Military, "An anime series/movie that has a heavy militaristic feel behind it.")
					.put(Genre.Music, "Headphones and concerts etc.")
					.put(Genre.Mystery, "?")
					.put(Genre.Parody, "Naruto -> Narudo" + "\n" + "Spiderman -> Spooderman.")
					.put(Genre.Police, "Based on characters who are involved in a police force.")
					.put(Genre.Psychological, "Brain.")
					.put(Genre.Romance, "Love.")
					.put(Genre.Samurai, "Sword.")
					.put(Genre.School, "Based in a school setting.")
					.put(Genre.SciFi, "Science?")
					.put(Genre.Seinen, "Aimed towards young adult men.")
					.put(Genre.Shoujo, "Aimed at a \"young female audience\".")
					.put(Genre.Shounen, "Aimed at a \"young male audience\".")
					.put(Genre.SliceOfLife, "Aims to mimic real life through anime.")
					.put(Genre.Space, "NASA.")
					.put(Genre.Sports, "Literally sports, or rather, physical exertion...")
					.put(Genre.Supernatural, "strange.")
					.put(Genre.SuperPower, "Usually based in a word where having superpowers is the norm.")
					.put(Genre.Suspense, "...")
					.put(Genre.Vampire, "Draculaaaaa.")
					.put(Genre.WorkLife, "About working.")
					.build();
	 */

	public static void init() {
		EnumMap<Genre, String> tempMap = new EnumMap<>(Genre.class);
		// genre definitions: https://myanimelist.net/anime/genre/info

		tempMap.put(Genre.Action, """
								Plays out mainly through a clash of physical forces. Frequently these
								stories have fast cuts, tough characters making quick decisions and
								usually a beautiful girl nearby. Anything quick and most likely a thin
								storyline.
								""");

		tempMap.put(Genre.Adventure, "Travelling and undertaking an adventure in a certain place");

		tempMap.put(Genre.AvantGarde, "Anime that have mind-twisting plots.");

		tempMap.put(Genre.AwardWinning, "Won a major award.");

		tempMap.put(Genre.BoysLove, "Gay.");

		tempMap.put(Genre.Cars, "Involves cars as a main feature e.g. racing");

		tempMap.put(Genre.Comedy, "Aims to make you laugh");
		//tempMap.put(Genre.Comedy, "Anime: *plays*" + "\n" + "You: *laughs*");

		tempMap.put(Genre.Demons, "Involves demons as a main feature");

		tempMap.put(Genre.Drama, """
								Anime that often show life or characters through conflict and
								emotions. In general, the different parts of the story tend to
								form a whole that is greater than the sum of the parts. In other
								words, the story has a message that is bigger than just the story
								line itself.
								""");

		tempMap.put(Genre.Ecchi, "Almost cartoon porn.");

		tempMap.put(Genre.Erotica, "\uD83D\uDE33");

		tempMap.put(Genre.Fantasy, "Based in a fanatical world.");

		tempMap.put(Genre.Game, """
								Central theme is based on a non-violent, non-sports game, like
								go, chess, trading card games or computer/video games.
								""");

		tempMap.put(Genre.GirlsLove, "Lesbian.");

		tempMap.put(Genre.Gourmet, "Food.");

		tempMap.put(Genre.Harem, "Not many guys, but many, many girls.");

		tempMap.put(Genre.Hentai, "Cartoon porn.");

		tempMap.put(Genre.Historical, "Based on past historical events.");

		tempMap.put(Genre.Horror, "ooooo scarryyyy.");

		tempMap.put(Genre.Josei, "Aimed towards young adult women.");

		tempMap.put(Genre.Kids, """
								Aimed at kids



								yikes
								""");

		tempMap.put(Genre.Magic, "Involves magic as a main feature.");

		tempMap.put(Genre.MartialArts, "MARTIAL ART.");

		tempMap.put(Genre.Mecha, "Involves robotic entities as a main feature.");

		tempMap.put(Genre.Military, "An anime series/movie that has a heavy militaristic feel behind it.");

		tempMap.put(Genre.Music, "Headphones and concerts etc.");

		tempMap.put(Genre.Mystery, "?");

		tempMap.put(Genre.Parody, "Naruto -> Narudo" + "\n" + "Spiderman -> Spooderman.");

		tempMap.put(Genre.Police, "Based on characters who are involved in a police force.");

		tempMap.put(Genre.Psychological, "Brain.");

		tempMap.put(Genre.Romance, "Love.");

		tempMap.put(Genre.Samurai, "Sword.");

		tempMap.put(Genre.School, "Based in a school setting.");

		tempMap.put(Genre.SciFi, "Science?");

		tempMap.put(Genre.Seinen, "Aimed towards young adult men.");

		tempMap.put(Genre.Shoujo, "Aimed at a \"young female audience\".");

		tempMap.put(Genre.Shounen, "Aimed at a \"young male audience\".");

		tempMap.put(Genre.SliceOfLife, "Aims to mimic real life through anime.");

		tempMap.put(Genre.Space, "NASA.");

		tempMap.put(Genre.Sports, "Literally sports, or rather, physical exertion...");

		tempMap.put(Genre.Supernatural, "strange.");

		tempMap.put(Genre.SuperPower, "Usually based in a word where having superpowers is the norm.");

		tempMap.put(Genre.Suspense, "...");

		tempMap.put(Genre.Vampire, "Draculaaaaa.");

		tempMap.put(Genre.WorkLife, "About working.");

		genreDefMap = Maps.immutableEnumMap(tempMap);
	}

	public static void open(Button infoBtn) {
		Utils.setStyleClass(infoBtn, Menu.SELECTED);

		try {
			new GenreInfoWindow(infoBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static final EventType<InputEvent> ALL_EVENTS = InputEvent.ANY;
	private static final EventHandler<InputEvent> IGNORE_EVENT = InputEvent::consume;

	private final Button helpBtn;

	@FXML
	private ComboBox<Genre> genreCombo;

	@FXML
	private TextArea textArea;

	private GenreInfoWindow(Button helpBtn) throws IOException {
		super(null);
		this.helpBtn = helpBtn;

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(afm.common.utils.Utils.getFxmlUrl("infowindows/GenreInfoWindow"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();

		// Stop main screen receiving input
		Main.getStage().addEventFilter(ALL_EVENTS, IGNORE_EVENT);
	}

	@FXML
	void initialize() {
		setTitle("Genre Explanations");

		// add all Genres into the ComboBox
		genreCombo.getItems().addAll(Genre.values());

		// set it so when user selects a genre, it sets textArea as the definiton
		// of that Genre
		genreCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				textArea.setText(genreDefMap.get(newVal));
			} else {
				textArea.setText(null);
			}
		});

		super.afterInitialize();
	}

	@Override @FXML
	void closeWindow() {
		helpBtn.getStyleClass().setAll("button");
		helpBtn.setMouseTransparent(false);

		super.closeWindow();

		// allow main screen to receive input again
		Main.getStage().removeEventFilter(ALL_EVENTS, IGNORE_EVENT);
	}
}
