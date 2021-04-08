package afm.screens.version2_welcome;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import afm.Main;
import afm.utils.Utils;

// now also being used as a Home Screen
public class WelcomeScreen extends VBox {

	public WelcomeScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("WelcomeScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	// Initialise main screen & return main to be able to change screens easily
	// Quite clever imo
	private Main initMain() {
		return Main.getInstance().initMainScreen();
	}

	@FXML
	void searchButtonPressed(ActionEvent event) {
		initMain().menu.openSearchScreenFromWelcome();
	}

	@FXML
	void myListButtonPressed(ActionEvent event) {
		initMain().menu.openMyListScreenFromWelcome();
	}

	@FXML
	void toWatchButtonPressed(ActionEvent event) {
		initMain().menu.openToWatchScreenFromWelcome();
	}

}
