package afm.screens;

import java.io.IOException;

import afm.utils.Utils;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;

public class MainScreen extends SplitPane {
	
	public MainScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("MainScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}
}
