package afm.screens.infowindows;

import java.io.IOException;

import afm.anime.Anime;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

//Refreshing of ToWatch table is done in ToWatch.add(Anime)
public class ToWatchInfoWindow extends InfoWindow {

	public static void open(Anime a, Button infoBtn) {
		try {
			new ToWatchInfoWindow(a, infoBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Button infoBtn;

	private ToWatchInfoWindow(Anime a, Button infoBtn) throws IOException {
		super(a);
		this.infoBtn = infoBtn;

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("ToWatchInfoWindow"));
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
	void move(ActionEvent event) {
		ToWatch.remove(anime);
		MyList.add(anime);
		closeWindow(null);
	}

	@Override @FXML
	void closeWindow(ActionEvent event) {
		infoBtn.setStyle("");
		infoBtn.setMouseTransparent(false);

		super.closeWindow(event);
	}
}
