package afm.screens.infowindows;

import java.io.IOException;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import afm.anime.Anime;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.utils.Utils;

// Refreshing of MyList table is done in MyList.add(Anime)
public class MyListInfoWindow extends InfoWindow {

	public static void open(Anime a, Button infoBtn) {
		try {
			new MyListInfoWindow(a, infoBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Button infoBtn;

	@FXML
	private TextField currEpField;

	private MyListInfoWindow(Anime a, Button infoBtn) throws IOException {
		super(a);
		this.infoBtn = infoBtn;

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("MyListInfoWindow"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	void initialize() {
		setTitle("Info: "+anime.getName());

		currEpField.setText(Integer.toString(anime.getCurrEp()));

		// when user changes what is contained in currEpField, change the
		// current episode of the anime
		// and only allow them to enter numbers in currEpField
		currEpField.textProperty().addListener((obs, oldVal, newVal) -> {
			StringProperty sp = (StringProperty)obs;

			if (newVal.isEmpty()) {
				sp.setValue("0");
			} else if (Utils.isStrictInteger(newVal)) {
				int newEps = Integer.parseInt(newVal);
    			anime.setCurrEp(newEps);
    			MyList.update(anime);
    			sp.setValue(Integer.toString(anime.getCurrEp()));
			} else {
				sp.setValue(oldVal);
			}
		});

		super.afterInitialize();
	}

	@FXML
	void move(ActionEvent event) {
		MyList.remove(anime);
		ToWatch.add(anime);
		closeWindow(null);
    }

	@Override @FXML
	void closeWindow(ActionEvent event) {
		infoBtn.setStyle("");
		infoBtn.setMouseTransparent(false);

		super.closeWindow(event);
	}
}
