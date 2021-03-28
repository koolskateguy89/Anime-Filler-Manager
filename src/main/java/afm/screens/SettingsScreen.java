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
		if (Settings.get(NAME_ORDER)) {
			nameCheckBox.setSelected(true);
		} else {
			insertionCheckBox.setSelected(true);
		}

		factsCheckBox.setSelected(Settings.get(SHOW_FACTS));

		soundCheckBox.setSelected(Settings.get(PLAY_SOUND));

		alwaysOnTopBox.setSelected(Settings.get(ALWAYS_ON_TOP));
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
    	Settings.invert(NAME_ORDER);
    	nameCheckBox.setSelected(!insertionCheckBox.isSelected());
    }

    @FXML
    void name(ActionEvent event) {
	    Settings.invert(NAME_ORDER);
    	insertionCheckBox.setSelected(!nameCheckBox.isSelected());
    }

    @FXML
    void showFacts(ActionEvent event) {
	    Settings.invert(SHOW_FACTS);
    }

    @FXML
    void playSound(ActionEvent event) {
	    Settings.invert(PLAY_SOUND);
    }

    @FXML
    void alwaysOnTop(ActionEvent event) {
	    Settings.invert(ALWAYS_ON_TOP);
    	Main.getStage().setAlwaysOnTop(Settings.get(ALWAYS_ON_TOP));
    }

}
