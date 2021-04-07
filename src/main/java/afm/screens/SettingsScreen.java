package afm.screens;

import static afm.user.Settings.Key.ALWAYS_ON_TOP;
import static afm.user.Settings.Key.NAME_ORDER;
import static afm.user.Settings.Key.PLAY_SOUND;
import static afm.user.Settings.Key.SHOW_FACTS;
import static afm.user.Settings.Key.SKIP_LOADING;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import afm.Main;
import afm.database.Database;
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

		skipLoadingBox.setSelected(Settings.get(SKIP_LOADING));
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
	private CheckBox skipLoadingBox;

	@FXML
	private ChoiceBox<String> databaseBox;

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

    @FXML
    void skipLoading(ActionEvent event) {
    	Settings.invert(SKIP_LOADING);
    }

    @FXML
    void createDatabase(ActionEvent event) {
	    FileChooser fc = new FileChooser();

	    // From SQLiteStudio
	    final String[] extensions = {
			"*.db",
			"*.db2",
			"*.db3",
			"*.sdb",
			"*.s2db",
			"*.s3db",
			"*.sqlite",
			"*.sqlite2",
			"*.sqlite3",
			"*.sl2",
			"*.sl3",
	    };
	    String s = String.join(" ", extensions);

	    ExtensionFilter filter = new ExtensionFilter(
			"All SQLite databases (%s)".formatted(s),
			extensions
	    );

	    fc.setInitialDirectory(new File("."));
	    fc.getExtensionFilters().add(filter);
	    fc.setTitle("Select anime database");

	    File database = fc.showSaveDialog(Main.getStage());

	    if (database == null)
	    	return;

	    String url = database.getAbsolutePath();
	    try {
	    	Database.initDatabase(url);
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }
	    System.out.println(url);
    }

    @FXML
	void resetToDefault(ActionEvent event) {
    	nameCheckBox.setSelected(false);
    	insertionCheckBox.setSelected(false);
    	Settings.reset();
    	initialize();
    }

}
