package afm.screens.version2_welcome;

import java.io.IOException;

import afm.Main;
import afm.utils.Handler;
import afm.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

// now also being used as a Home Screen
public class WelcomeScreen extends VBox {
	
	private final Handler h;
	
	public WelcomeScreen(Handler h) throws IOException {
		this.h = h;
		
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("WelcomeScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}
	
	// Initialise main screen & return main to be able to change screens easily
	// Quite clever imo
	private Main initMain() {
		return h.getMain().initMainScreen();
	}

	@FXML void searchButtonPressed(ActionEvent event) {
    	initMain().menu.openSearchScreenFromWelcome();
    }
	
	@FXML void myListButtonPressed(ActionEvent event) {
		initMain().menu.openMyListScreenFromWelcome();
	}
	
	@FXML void toWatchButtonPressed(ActionEvent event) {
		initMain().menu.openToWatchScreenFromWelcome();
	}

}
