package afm.screens.version8_custom;

import java.io.IOException;
import java.util.Optional;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.textfield.TextFields;

import afm.Main;
import afm.anime.Anime;
import afm.anime.Anime.AnimeBuilder;
import afm.anime.Genre;
import afm.anime.Season;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.utils.Utils;
import impl.org.controlsfx.skin.SearchableComboBoxSkin;

// A lot of things here are identical to SearchScreen
public class CustomScreen extends GridPane {

	@FXML
	private StackPane namePane;
	private TextField nameField;

	@FXML
	private StackPane studioPane;
	private TextField studioField;

	@FXML
	private CheckComboBox<Genre> genreCombo;

	@FXML
	private ComboBox<String> sznCombo;
	@FXML
	private SearchableComboBox<Integer> yearCombo;

	@FXML
	private TextField totalEpField;
	@FXML
	private TextField currEpField;

	public CustomScreen() throws IOException {
		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("CustomScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	private void initialize() {
		nameField = TextFields.createClearableTextField();
		nameField.setPromptText("Name");
		namePane.setStyle(null);
		namePane.getChildren().add(nameField);

		studioField = TextFields.createClearableTextField();
		studioField.setPromptText("Studio");
		studioPane.setStyle(null);
		studioPane.getChildren().add(studioField);

		genreCombo.setTitle("Select genre(s)");
		genreCombo.getItems().addAll(Genre.values());

		// use the title as prompt text
		genreCombo.getCheckModel().getCheckedItems().addListener((ListChangeListener<Genre>) change -> {
			if (change.getList().isEmpty()) {
				genreCombo.setTitle("Select genre(s)");
			} else {
				genreCombo.setTitle(null);
			}
		});

		final var szns = sznCombo.getItems();
		szns.add("Spring");
		szns.add("Summer");
		szns.add("Fall");
		szns.add("Winter");

		final var years = yearCombo.getItems();
		for (int i = Season.END_YEAR; i >= Season.START_YEAR; i--)
			years.add(i);

		totalEpField.textProperty().addListener(Utils.onlyAllowIntegersListener());
		currEpField.textProperty().addListener(Utils.onlyAllowIntegersListener());
	}

	private boolean opened = false;
	public void open() {
		if (opened)
			return;

		setYearPromptText();

		opened = true;
	}

	/*
	 * This waits (by polling) until the yearCombo has a skin (has been rendered I think) and uses the Skin to set
	 *   the prompt text of the 'inner' ComboBox. Usually waits about 40ms.
	 * This is needed to give the yearCombo prompt text because using SearchableComboBox.setPromptText(String)
	 *   doesn't actually visually do anything.
	 */
	private void setYearPromptText() {
		Thread t = new Thread(() -> {
			// wait until skin isn't null
			while (yearCombo.getSkin() == null);

			SearchableComboBoxSkin<Integer> skin = (SearchableComboBoxSkin<Integer>) yearCombo.getSkin();

			ComboBox comboBox = (ComboBox) skin.getChildren().get(0);
			comboBox.setPromptText("Year");
		});
		t.start();
	}

	@FXML
	void clearGenres(ActionEvent event) {
		genreCombo.getCheckModel().clearChecks();
	}

	// When user pressed reset button, reset contents of all fields
	@FXML
	void resetFields(ActionEvent event) {
		nameField.clear();

		studioField.clear();

		totalEpField.clear();
		currEpField.clear();

		clearGenres(null);
		sznCombo.getSelectionModel().clearSelection();
		yearCombo.getSelectionModel().clearSelection();

		totalEpField.clear();
		currEpField.clear();

		// Problem: comboBox promptText isn't showing after reset (if smthn was selected)
	}

	// can only add an anime if the anime has a name & genre
	private boolean cantAdd() {
		boolean emptyName = nameField.getText().isBlank();
		boolean emptyGenre = genreCombo.getCheckModel().getCheckedItems().isEmpty();

		if (emptyName) {
			Alert needName = new Alert(AlertType.ERROR, "The anime needs a name!");
			needName.initOwner(Main.getStage());
			needName.showAndWait();
		}
		if (emptyGenre) {
			Alert needGenre = new Alert(AlertType.ERROR, "At least 1 genre is needed!");
			needGenre.initOwner(Main.getStage());
			needGenre.showAndWait();
		}

		return emptyName || emptyGenre;
	}

	// Return an Anime object, built from fields in this screen
	private Anime getAnimeFromFields() {
		final AnimeBuilder builder = Anime.builder(nameField.getText().strip());

		builder.setCustom(true);

		builder.setGenres(genreCombo.getCheckModel().getCheckedItems());

		final String studio = studioField.getText();
		if (studio != null && !studio.isBlank())
			builder.setStudio(studio.strip());

		final String szn = sznCombo.getValue();
		final Integer year = yearCombo.getValue();
		if (szn != null && year != null)
			builder.setSeason(Season.getSeason(szn, year));

		try {
			builder.setEpisodes(Integer.parseInt(totalEpField.getText()));
		} catch (NumberFormatException ignored) {
			// accept default value
		}
		try {
			builder.setCurrEp(Integer.parseInt(currEpField.getText()));
		} catch (NumberFormatException ignored) {
			// accept default value
		}

		return builder.build();
	}

	@FXML
	void tryAddToMyList(ActionEvent event) {
		if (cantAdd()) return;

		// anime position is set by MyList
		Anime anime = getAnimeFromFields();

		if (MyList.contains(anime)) {
			/*
			 * open Alert to ask if they want to overwrite it
			 * if yes: carry on
			 * otherwise: return
			 */
			Alert alert = new Alert(AlertType.CONFIRMATION);

			// hide header
			alert.setHeaderText(null);

			alert.setContentText("There is already a very similar anime in MyList, "
					+ "do you want to overwrite it?");

			alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			// if user clicks no or closes the window, return
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.YES)
				return;
		}

		MyList.add(anime);
		resetFields(null);
	}

	@FXML
	void tryAddToToWatch(ActionEvent event) {
		if (cantAdd()) return;

		// anime position is set by ToWatch
		Anime anime = getAnimeFromFields();

		if (ToWatch.contains(anime)) {
			/*
			 * open Alert to ask if they want to overwrite it
			 * if yes: carry on
			 * otherwise: return
			 */
			Alert alert = new Alert(AlertType.CONFIRMATION);

			// hide header
			alert.setHeaderText(null);

			alert.setContentText("There is already a very similar anime in ToWatch, "
								+ "do you want to overwrite it?");

			alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			// if user clicks no or closes the window, return
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.YES)
				return;
		}

		ToWatch.add(anime);
		resetFields(null);
	}
}
