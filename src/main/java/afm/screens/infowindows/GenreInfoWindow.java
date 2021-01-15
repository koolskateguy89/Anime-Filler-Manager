package afm.screens.infowindows;

import java.io.IOException;
import java.util.EnumMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import afm.anime.Genre;
import afm.screens.Menu;
import afm.utils.Handler;
import afm.utils.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;


public class GenreInfoWindow extends InfoWindow {

	static Handler h;

	// maybe this should be in Genre c;ass
	private static ImmutableMap<Genre, String> genreDefMap;

	public static void init(Handler h) {
		GenreInfoWindow.h = h;

		EnumMap<Genre, String> tempMap = new EnumMap<>(Genre.class);

		tempMap.put(Genre.Action, "A genre in which the protagonist or "
				+ "protagonists are thrust into a series of events that "
				+ "typically include violence, extended fighting, physical "
				+ "feats, rescues and frantic chases.");

		tempMap.put(Genre.Adventure, "Travelling and undertaking an adventure in a certain place.");

		tempMap.put(Genre.Cars, "Involves cars as a main feature e.g. racing.");

		tempMap.put(Genre.Comedy, "Aims to make you laugh");
		//tempMap.put(Genre.Comedy, "Anime: *plays*" + "\n" + "You: *laughs*");

		tempMap.put(Genre.Dementia, "Memory loss and dat.");

		tempMap.put(Genre.Demons, "Involves demons as a main feature.");

		tempMap.put(Genre.Drama, "Beef?? idk");

		tempMap.put(Genre.Ecchi, "Almost cartoon porn.");

		tempMap.put(Genre.Fantasy, "Not real I guess");

		tempMap.put(Genre.Game, "Has gaming as a core aspect.");

		tempMap.put(Genre.Harem, "Not many guys, but many, many girls.");

		tempMap.put(Genre.Hentai, "Cartoon porn.");

		tempMap.put(Genre.Historical, "References the past innit.");

		tempMap.put(Genre.Horror, "Horror.");

		tempMap.put(Genre.Josei, "Aimed towards young adult women");

		tempMap.put(Genre.Kids, "Aimed at kids" + "\n\n\n" + "yikes");

		tempMap.put(Genre.Magic, "Involves magic as a main feature.");

		tempMap.put(Genre.MartialArts, "MARTIAL ARTS.");

		tempMap.put(Genre.Mecha, "Robots and dat");

		tempMap.put(Genre.Military, "Army and dat.");

		tempMap.put(Genre.Music, "Headphones and concerts and dat.");

		tempMap.put(Genre.Mystery, "?");

		tempMap.put(Genre.Parody, "Naruto -> Narudo" + "\n" + "Spiderman -> Spooderman");

		tempMap.put(Genre.Police, "Bruh");

		tempMap.put(Genre.Psychological, "Brain");

		tempMap.put(Genre.Romance, "Love");

		tempMap.put(Genre.Samurai, "Sword");

		tempMap.put(Genre.School, "Learn?");

		tempMap.put(Genre.SciFi, "Science?");

		tempMap.put(Genre.Seinen, "Aimed towards young adult men");

		tempMap.put(Genre.Shoujo, "Aimed at a \"young female audience\"");
		tempMap.put(Genre.ShoujoAi, "Relationship between two females");

		tempMap.put(Genre.Shounen, "Aimed at a \"young male audience\"");
		tempMap.put(Genre.ShounenAi, "Relationship between two males");

		tempMap.put(Genre.SliceOfLife, "Mimic real life I guess");

		tempMap.put(Genre.Space, "NASA");

		tempMap.put(Genre.Sports, "literally sports");

		tempMap.put(Genre.Supernatural, "strage");

		tempMap.put(Genre.SuperPower, "how do you not know what this is...");

		tempMap.put(Genre.Thriller, "scary?");

		tempMap.put(Genre.Vampire, "Dracula and dat");

		tempMap.put(Genre.Yaoi, "Gay hentai.");

		tempMap.put(Genre.Yuri, "Female gay hentai.");

		genreDefMap = Maps.immutableEnumMap(tempMap);
	}

	public static void open(Button infoBtn) {
		infoBtn.setStyle(Menu.SELECTED);
		/* using Platform.runLater() because it affects the main application */
		Platform.runLater(() -> {
			try {
				new GenreInfoWindow(infoBtn).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}


	private static final EventType<InputEvent> EVENT_TYPE = InputEvent.ANY;
	private static final EventHandler<InputEvent> EVENT_HANDLER = InputEvent::consume;

	private final Button helpBtn;

	@FXML
	private ComboBox<Genre> genreCombo;

	@FXML
	private TextArea textArea;

	private GenreInfoWindow(Button helpBtn) throws IOException {
		super(null);
		this.helpBtn = helpBtn;

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("GenreInfoWindow"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();

		// Stop main screen receiving input
		h.getStage().addEventFilter(EVENT_TYPE, EVENT_HANDLER);
	}

	@FXML
	void initialize() {
		setTitle("Genre Expanations");

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
	void closeWindow(ActionEvent event) {
		helpBtn.setStyle("");
		helpBtn.setMouseTransparent(false);

		super.closeWindow(event);

		// allow main screen to receive input again
		h.getStage().removeEventFilter(EVENT_TYPE, EVENT_HANDLER);
	}
}
