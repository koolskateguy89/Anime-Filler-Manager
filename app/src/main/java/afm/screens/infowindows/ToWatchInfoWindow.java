package afm.screens.infowindows;

import static afm.database.AnimeListKt.MyListKt;
import static afm.database.AnimeListKt.ToWatchKt;

import java.io.IOException;

import javax.annotation.Nonnull;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import afm.anime.Anime;
import afm.common.utils.Utils;

//Refreshing of ToWatch table is done in ToWatchKt.add(Anime)
public class ToWatchInfoWindow extends InfoWindow {

	public static void open(@Nonnull Anime anime, @Nonnull Button infoBtn) {
		try {
			new ToWatchInfoWindow(anime, infoBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Button infoBtn;

	// temporarily change the button event handler to set focus on this isntead
	// of trying to open another info window
	private final EventHandler<ActionEvent> eventHandler;

	private ToWatchInfoWindow(Anime a, Button infoBtn) throws IOException {
		super(a);

		this.infoBtn = infoBtn;
		eventHandler = infoBtn.getOnAction();
		infoBtn.setOnAction(e -> this.requestFocus());

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("infowindows/ToWatchInfoWindow"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	void initialize() {
		setTitle("Info: " + anime.getName());

		super.afterInitialize();
	}

	@FXML
	void move() {
		ToWatchKt.remove(anime);
		MyListKt.add(anime);
		closeWindow();
	}

	@Override @FXML
	void closeWindow() {
		infoBtn.getStyleClass().setAll("button");
		infoBtn.setOnAction(eventHandler);

		super.closeWindow();
	}
}
