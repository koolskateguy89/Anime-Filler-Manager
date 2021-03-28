package afm.screens;

import static afm.user.Settings.Key.*;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

import afm.Main;
import afm.user.Settings;
import afm.utils.Utils;

public class SettingsScreen extends Pane {

	public SettingsScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("SettingsScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	private void initialize() {
		if (Settings.get(NAMEORDER)) {
			nameCheckBox.setSelected(true);
		} else {
			insertionCheckBox.setSelected(true);
		}

		factsCheckBox.setSelected(Settings.get(SHOWFACTS));

		soundCheckBox.setSelected(Settings.get(PLAYSOUND));

		alwaysOnTopBox.setSelected(Settings.get(ALWAYSONTOP));
	}

	@FXML
	private CheckBox nameCheckBox;

	@FXML
	private CheckBox insertionCheckBox;

	@FXML
	private CheckBox factsCheckBox;

	@FXML
	private CheckBox soundCheckBox;

	@FXML
	private CheckBox alwaysOnTopBox;

    @FXML
    void insertion(ActionEvent event) {
    	Settings.invert(NAMEORDER);
    	nameCheckBox.setSelected(!insertionCheckBox.isSelected());
    }

    @FXML
    void name(ActionEvent event) {
	    Settings.invert(NAMEORDER);
    	insertionCheckBox.setSelected(!nameCheckBox.isSelected());
    }

    @FXML
    void showFacts(ActionEvent event) {
	    Settings.invert(SHOWFACTS);
    }

    @FXML
    void playSound(ActionEvent event) {
	    Settings.invert(PLAYSOUND);
    }

    @FXML
    void alwaysOnTop(ActionEvent event) {
	    Settings.invert(ALWAYSONTOP);
    	Main.getStage().setAlwaysOnTop(Settings.get(ALWAYSONTOP));
    }

}
