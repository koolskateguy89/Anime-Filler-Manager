package afm.screens;

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
		if (Settings.nameOrder()) {
			nameCheckBox.setSelected(true);
		} else {
			insertionCheckBox.setSelected(true);
		}

		factsCheckBox.setSelected(Settings.showFacts());

		soundCheckBox.setSelected(Settings.playSound());

		alwaysOnTopBox.setSelected(Settings.alwaysOnTop());
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
    	Settings.invertNameOrder();
    	nameCheckBox.setSelected(!insertionCheckBox.isSelected());
    }

    @FXML
    void name(ActionEvent event) {
    	Settings.invertNameOrder();
    	insertionCheckBox.setSelected(!nameCheckBox.isSelected());
    }

    @FXML
    void showFacts(ActionEvent event) {
    	Settings.invertShowFacts();
    }

    @FXML
    void playSound(ActionEvent event) {
    	Settings.invertPlaySound();
    }

    @FXML
    void alwaysOnTop(ActionEvent event) {
    	Settings.invertAlwaysOnTop();
    	Main.getStage().setAlwaysOnTop(Settings.alwaysOnTop());
    }

}
