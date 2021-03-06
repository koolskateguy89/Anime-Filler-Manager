package afm.screens.infowindows;

import static afm.utils.Utils.setStyleClass;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import afm.anime.Anime;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.screens.Menu;
import afm.utils.Utils;

public class ResultInfoWindow extends InfoWindow {

	public static void open(Anime a, Button infoBtn, Button myListBtn, Button toWatchBtn) {
		try {
			new ResultInfoWindow(a, infoBtn, myListBtn, toWatchBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Button infoBtn;
	private final Button MLbtn;
	private final Button TWbtn;

	private final EventHandler<ActionEvent> eventHandler;

	@FXML
	private Button myListBtn;

	@FXML
	private Button toWatchBtn;

	private ResultInfoWindow(Anime a, Button infoBtn, Button myListBtn, Button toWatchBtn) throws IOException {
		super(a);
		this.infoBtn = infoBtn;
		MLbtn = myListBtn;
		TWbtn = toWatchBtn;

		eventHandler = infoBtn.getOnAction();
		infoBtn.setOnAction(e -> this.requestFocus());

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("ResultInfoWindow"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	void initialize() {
		// Don't allow the anime to be moved if already present
		if (MyList.contains(anime) || ToWatch.contains(anime)) {
			myListBtn.setVisible(false);
			toWatchBtn.setVisible(false);
		}

		setTitle("Info: "+anime.getName());

		super.afterInitialize();
	}

	// these also move it to MyList/ToWatch if it was in the other
	@FXML
	void addToMyList(ActionEvent event) {
		ToWatch.remove(anime);
		MyList.add(anime);
		setStyleClass(MLbtn, Menu.SELECTED);
		makeBothBtnsTransparent();
		closeWindow(null);
	}

	@FXML
	void addToToWatch(ActionEvent event) {
		MyList.remove(anime);
		ToWatch.add(anime);
		setStyleClass(TWbtn, Menu.SELECTED);
		makeBothBtnsTransparent();
		closeWindow(null);
	}

	private void makeBothBtnsTransparent() {
		MLbtn.setMouseTransparent(true);
		TWbtn.setMouseTransparent(true);
	}

	@Override @FXML
	void closeWindow(ActionEvent event) {
		infoBtn.getStyleClass().setAll("button");
		infoBtn.setOnAction(eventHandler);

		super.closeWindow(event);
	}
}
