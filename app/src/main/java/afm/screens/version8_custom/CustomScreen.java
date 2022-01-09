package afm.screens.version8_custom;

import static afm.database.DelegatesKt.MyListKt;
import static afm.database.DelegatesKt.ToWatchKt;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javafx.collections.ListChangeListener;
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
import afm.common.utils.Utils;

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

		// use the combobox title as prompt text
		genreCombo.getCheckModel().getCheckedItems().addListener((ListChangeListener<Genre>) change -> {
			if (change.getList().isEmpty()) {
				genreCombo.setTitle("Select genre(s)");
			} else {
				genreCombo.setTitle(null);
			}
		});

		sznCombo.getItems().addAll("Spring", "Summer", "Fall", "Winter");

		final var years = yearCombo.getItems();
		for (int i = Season.END_YEAR; i >= Season.START_YEAR; i--)
			years.add(i);

		totalEpField.textProperty().addListener(Utils.intOnlyListener());
		currEpField.textProperty().addListener(Utils.intOnlyListener());
	}

	@FXML
	void clearGenres() {
		genreCombo.getCheckModel().clearChecks();
	}

	@FXML
	void resetFields() {
		nameField.clear();

		studioField.clear();

		totalEpField.clear();
		currEpField.clear();

		clearGenres();
		sznCombo.getSelectionModel().clearSelection();
		yearCombo.getSelectionModel().clearSelection();

		totalEpField.clear();
		currEpField.clear();
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
			builder.setStudios(Set.of(studio.strip()));

		final String szn = sznCombo.getValue();
		final Integer year = yearCombo.getValue();
		//if (szn != null && year != null)
			//builder.setSeason(Season.getSeason(szn, year));

		try {
			builder.setEpisodes(Integer.parseInt(totalEpField.getText()));
		} catch (NumberFormatException ignored) {
			// TODO: show error: didn't enter valid integer
			// accept default value
		}
		try {
			builder.setCurrEp(Integer.parseInt(currEpField.getText()));
		} catch (NumberFormatException ignored) {
			// TODO: show error: didn't enter valid integer
			// accept default value
		}

		return builder.build();
	}

	@FXML
	void tryAddToMyList() {
		if (cantAdd()) return;

		// anime position is set by MyList
		Anime anime = getAnimeFromFields();

		if (MyListKt.contains(anime)) {
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

		MyListKt.add(anime);
		resetFields();
	}

	@FXML
	void tryAddToToWatch() {
		if (cantAdd()) return;

		// anime position is set by ToWatch
		Anime anime = getAnimeFromFields();

		if (ToWatchKt.contains(anime)) {
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

		ToWatchKt.add(anime);
		resetFields();
	}
}
