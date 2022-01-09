package afm.screens.infowindows;

import static afm.database.DelegatesKt.MyListKt;
import static afm.database.DelegatesKt.ToWatchKt;

import java.io.IOException;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import afm.anime.Anime;
import afm.common.utils.Utils;

// Refreshing of MyList table is done in MyListKt.add(Anime)
public class MyListInfoWindow extends InfoWindow {

	public static void open(Anime a, Button infoBtn) {
		try {
			new MyListInfoWindow(a, infoBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Button infoBtn;

	// temporarily change the button event handler to set focus on this isntead
	// of trying to open another info window
	private final EventHandler<ActionEvent> eventHandler;

	@FXML
	private TextField currEpField;

	private MyListInfoWindow(Anime a, Button infoBtn) throws IOException {
		super(a);

		this.infoBtn = infoBtn;
		eventHandler = infoBtn.getOnAction();
		infoBtn.setOnAction(e -> this.requestFocus());

		FXMLLoader loader = new FXMLLoader(afm.common.utils.Utils.getFxmlUrl("infowindows/MyListInfoWindow"));
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
				anime.setCurrEp(0);
				MyListKt.add(anime);
			} else if (Utils.isStrictInteger(newVal)) {
				int newEps = Integer.parseInt(newVal);
				anime.setCurrEp(newEps);
				MyListKt.add(anime);

				// re-set value in case there's a problem such as the episode being outside
				// of the anime's episode range
				if (anime.getCurrEp() != newEps)
					sp.setValue(Integer.toString(anime.getCurrEp()));
			} else {
				// the input is not acceptable, i.e. is not an integer
				sp.setValue(oldVal);
			}
		});

		super.afterInitialize();
	}

	@FXML
	void move() {
		MyListKt.remove(anime);
		ToWatchKt.add(anime);
		closeWindow();
	}

	@Override @FXML
	void closeWindow() {
		infoBtn.getStyleClass().setAll("button");
		infoBtn.setOnAction(eventHandler);

		super.closeWindow();
	}
}
