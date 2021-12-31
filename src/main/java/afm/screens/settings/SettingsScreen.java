package afm.screens.settings;

import static afm.user.Settings.Key.ALWAYS_ON_TOP;
import static afm.user.Settings.Key.NAME_ORDER;
import static afm.user.Settings.Key.PLAY_SOUND;
import static afm.user.Settings.Key.SHOW_FACTS;
import static afm.user.Settings.Key.SKIP_LOADING;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.controlsfx.control.PropertySheet;

import afm.Main;
import afm.database.Database;
import afm.screens.infowindows.InfoWindow;
import afm.screens.settings.items.BooleanItem;
import afm.screens.settings.items.EnumItem;
import afm.screens.settings.items.Item;
import afm.screens.version1_start.StartScreen;
import afm.user.Settings;
import afm.user.Theme;
import afm.utils.Utils;

/*
 * The bad things about using a PropertySheet:
 *  - Using lots of listeners
 *  - Having to manually create items
 *  - GUI elements sort of seperated from the FXML
 */
public class SettingsScreen extends Pane {

	@FXML
	private CheckBox nameCheckBox;

	@FXML
	private CheckBox insertionCheckBox;

	@FXML
	private ChoiceBox<String> databaseBox;

	@FXML
	private Slider opacitySlider;
	@FXML
	private Slider inactiveOpacitySlider;


	public SettingsScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("SettingsScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	private PropertySheet sheet;

	// loading
	private final Item showFacts = new BooleanItem(Category.LOADING, "*Show facts", "Show facts on loading screen");
	private final BooleanProperty showFactsProp = showFacts.getProperty();
	private final Item skipLoading = new BooleanItem(Category.LOADING, "*Automate loading screen");
	private final BooleanProperty skipLoadingProp = skipLoading.getProperty();

	// search
	private final Item playSound = new BooleanItem(Category.SEARCH, "Play sound after search?");
	private final BooleanProperty playSoundProp = playSound.getProperty();

	// visual
	private final Item theme = new EnumItem<>(Theme.class, Category.VISUAL, "Theme");
	private final Property<Theme> themeProperty = theme.getProperty();
	private final Item alwaysOnTop = new BooleanItem(Category.VISUAL, "Always on top");
	private final BooleanProperty alwaysOnTopProp = alwaysOnTop.getProperty();

	private void initCheckBoxes() {
		if (Settings.get(NAME_ORDER)) {
			nameCheckBox.setSelected(true);
		} else {
			insertionCheckBox.setSelected(true);
		}

		showFactsProp.set(Settings.get(SHOW_FACTS));

		playSoundProp.set(Settings.get(PLAY_SOUND));

		alwaysOnTopProp.set(Settings.get(ALWAYS_ON_TOP));

		skipLoadingProp.set(Settings.get(SKIP_LOADING));
	}

	private void initSheet() {
		showFactsProp.addListener((obs, oldVal, newVal) -> {
			if (newVal != oldVal) {
				Settings.invert(SHOW_FACTS);
			}
		});

		skipLoadingProp.addListener((obs, oldVal, newVal) -> {
			if (newVal != oldVal) {
				Settings.invert(SKIP_LOADING);
			}
		});

		playSoundProp.addListener((obs, oldVal, newVal) -> {
			if (newVal != oldVal) {
				Settings.invert(PLAY_SOUND);
			}
		});

		themeProperty.bindBidirectional(Settings.themeProperty);
		themeProperty.addListener((obs, oldTheme, newTheme) -> {
			if (newTheme != null) {
				Main.getInstance().applyTheme(newTheme);
				InfoWindow.applyTheme(newTheme);
			}
		});

		alwaysOnTopProp.addListener((obs, oldVal, newVal) -> {
			if (newVal != oldVal) {
				Settings.invert(ALWAYS_ON_TOP);
				Main.getStage().setAlwaysOnTop(Settings.get(ALWAYS_ON_TOP));
			}
		});

		var items = sheet.getItems();
		items.add(showFacts);
		items.add(skipLoading);

		items.add(playSound);

		items.add(theme);
		items.add(alwaysOnTop);
	}

	@FXML
	private void initialize() {
		initCheckBoxes();

		initSheet();

		databaseBox.getItems().add("Internal");
		databaseBox.getItems().addAll(Settings.getDatabaseUrls());

		databaseBox.setOnAction(event -> formatDatabaseBox());

		databaseBox.valueProperty().bindBidirectional(Settings.selectedDatabaseProperty);

		opacitySlider.valueProperty().bindBidirectional(Settings.opacityProperty);
		inactiveOpacitySlider.valueProperty().bindBidirectional(Settings.inactiveOpacityProperty);


		Theme currentTheme = Settings.themeProperty.get();

		// this applies the current theme once all screens have been loaded
		if (currentTheme != Theme.DEFAULT) {
			Platform.runLater(() -> {
				StartScreen.LoadTask task = Main.getInstance().startScreen.loadTask;
				EventHandler<WorkerStateEvent> onSucceeded = task.getOnSucceeded();

				// chain the old onSucceeded with a new one
				task.setOnSucceeded(event -> {
					if (onSucceeded != null)
						onSucceeded.handle(event);

					Main.getInstance().applyTheme(currentTheme);
				});
			});
		}
	}

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

	private static FileChooser getDatabaseFileChooser() {
		FileChooser fc = new FileChooser();

		String s = String.join(" ", Database.FILE_EXTENSIONS);

		ExtensionFilter filter = new ExtensionFilter(
				"All SQLite databases (%s)".formatted(s),
				Database.FILE_EXTENSIONS
		);

		fc.setTitle("Select anime database");
		fc.setInitialDirectory(new File("."));
		fc.getExtensionFilters().add(filter);

		return fc;
	}

	@FXML
	void addDatabase(ActionEvent event) {
		FileChooser fc = getDatabaseFileChooser();

		List<File> databases = fc.showOpenMultipleDialog(Main.getStage());

		if (databases == null || databases.isEmpty())   // it shouldn't be empty just check anyway
			return;

		var urls = Settings.getDatabaseUrls();
		var items = databaseBox.getItems();

		for (File database : databases) {
			String url = database.getAbsolutePath();
			if (urls.add(url)) {
				items.add(url);
			}
		}

		// If only 1 database was added, select it
		if (databases.size() == 1)
			databaseBox.setValue(databases.get(0).getAbsolutePath());
	}

	/*
	 * Creating the database is fast enough that it doesn't need to be done in a
	 * separate thread
	 */
	@FXML
	void createDatabase(ActionEvent event) {
		FileChooser fc = getDatabaseFileChooser();

		File file = fc.showSaveDialog(Main.getStage());

		if (file == null)
			return;

		file.delete();

		String url = file.getAbsolutePath();

		try {
			Database.createNew(url);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (Settings.getDatabaseUrls().add(url))
			databaseBox.getItems().add(url);

		Settings.getDatabaseUrls().add(url);
		databaseBox.setValue(url);
	}

	// TODO: 'fix' this (make text right aligned)
	private void formatDatabaseBox() {
		var children = databaseBox.getChildrenUnmodifiable();
		if (children.isEmpty())
			return;

		Label label = (Label) children.get(0);
		label.setTextAlignment(TextAlignment.RIGHT);
		//label.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		//label.setAlignment(Pos.CENTER_RIGHT);
	}

	@FXML
	void clearDatabases(ActionEvent event) {
		var items = databaseBox.getItems();
		items.clear();
		items.add("Internal");
		databaseBox.setValue("Internal");

		Settings.getDatabaseUrls().clear();
	}

	@FXML
	void resetToDefault(ActionEvent event) {
		Settings.reset();

		nameCheckBox.setSelected(false);
		insertionCheckBox.setSelected(false);
		initCheckBoxes();
	}
}
