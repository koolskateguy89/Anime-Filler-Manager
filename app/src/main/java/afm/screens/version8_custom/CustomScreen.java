package afm.screens.version8_custom;

import static afm.database.AnimeListKt.MyListKt;
import static afm.database.AnimeListKt.ToWatchKt;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.CustomTextField;

import afm.Main;
import afm.anime.Anime;
import afm.anime.AnimeBuilder;
import afm.anime.AnimeType;
import afm.anime.Genre;
import afm.anime.GenreType;
import afm.anime.Status;
import afm.common.utils.Utils;

// A lot of things here are identical to SearchScreen
// TODO: update to reflect updates in Anime & Database
public class CustomScreen extends GridPane {

	@FXML
	private CustomTextField nameField;

	@FXML
	private CustomTextField studioField;

	// TODO: synopsis field

	@FXML
	private CheckComboBox<Genre> genreCombo;
	@FXML
	private CheckComboBox<Genre> demoCombo;
	@FXML
	private CheckComboBox<Genre> themeCombo;

	@FXML
	private ComboBox<AnimeType> typeCombo;
	@FXML
	private TextField startYearField;
	@FXML
	private ComboBox<Status> statusCombo;

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
		genreCombo.getItems().addAll(Genre.valuesOfType(GenreType.NORMAL));
		Utils.useTitleAsPromptText(genreCombo);
		demoCombo.getItems().addAll(Genre.valuesOfType(GenreType.DEMOGRAPHIC));
		Utils.useTitleAsPromptText(demoCombo);
		themeCombo.getItems().addAll(Genre.valuesOfType(GenreType.THEME));
		Utils.useTitleAsPromptText(themeCombo);

		typeCombo.getItems().addAll(AnimeType.values());
		typeCombo.setValue(AnimeType.UNKNOWN);
		startYearField.textProperty().addListener(Utils.positiveIntOrEmptyListener());
		startYearField.setText(Integer.toString(Utils.getCurrentYear()));
		statusCombo.getItems().addAll(Status.values());
		statusCombo.setValue(Status.UNKNOWN);

		totalEpField.textProperty().addListener(Utils.positiveIntOrEmptyListener());
		//totalEpField.setText(Integer.toString(Anime.NOT_FINISHED));
		currEpField.textProperty().addListener(Utils.positiveIntOrEmptyListener());
		//currEpField.setText("1");
	}

	@FXML
	void clearGenres() {
		genreCombo.getCheckModel().clearChecks();
		demoCombo.getCheckModel().clearChecks();
		themeCombo.getCheckModel().clearChecks();
	}

	@FXML
	void resetFields() {
		nameField.clear();
		studioField.clear();

		clearGenres();

		typeCombo.getSelectionModel().clearSelection();
		startYearField.clear();
		statusCombo.getSelectionModel().clearSelection();

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

		// TODO: check startYearField valid
		// TODO: check totalEpField valid
		// TODO: check currEpField valid

		return emptyName || emptyGenre;
	}

	// Return an Anime object, built from fields in this screen
	private Anime getAnimeFromFields() {
		final AnimeBuilder builder = new AnimeBuilder(nameField.getText().strip());
		builder.setCustom(true);

		builder.setGenres(genreCombo.getCheckModel().getCheckedItems());

		final String studio = studioField.getText();
		if (studio != null && !studio.isBlank())
			builder.setStudios(Set.of(studio.strip()));

		// TODO: check valid int
		builder.setStartYear(Integer.parseInt(startYearField.getText()));

		builder.setEpisodes(Utils.toIntOrNull(totalEpField.getText()));

		builder.setCurrEp(Utils.toIntOrNull(currEpField.getText()));

		try {
			//builder.setCurrEp(Integer.parseInt(currEpField.getText()));
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
