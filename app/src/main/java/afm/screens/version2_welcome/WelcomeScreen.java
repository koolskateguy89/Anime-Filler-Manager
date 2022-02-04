package afm.screens.version2_welcome;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import afm.Main;
import afm.common.Facts;
import afm.common.utils.Utils;
import afm.user.Settings;

import kotlin.Pair;

// now also being used as a Home Screen
public class WelcomeScreen extends VBox {

	public WelcomeScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("WelcomeScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	private Text factText;

	@FXML
	private void initialize() {
		//Get a random fact and its id and display it in factText
		if (Settings.get(Settings.Key.SHOW_FACTS)) {
			Facts.init(); // only need to init if showing facts
			Pair<Integer, String> fact = Facts.getRandomFact();
			factText.setText("Fact " + fact.getFirst() + ": " + fact.getSecond());
		} else {
			factText.setVisible(false);
		}
	}

	// Initialise main screen & return main to be able to change screens easily
	// Quite clever imo
	private Main initMain() {
		return Main.getInstance().initMainScreen();
	}

	@FXML
	void searchButtonPressed() {
		initMain().menu.openSearchScreenFromWelcome();
	}

	@FXML
	void myListButtonPressed() {
		initMain().menu.openMyListScreenFromWelcome();
	}

	@FXML
	void toWatchButtonPressed() {
		initMain().menu.openToWatchScreenFromWelcome();
	}

}
