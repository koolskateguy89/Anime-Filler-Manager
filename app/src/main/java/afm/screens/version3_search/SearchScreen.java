package afm.screens.version3_search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.CustomTextField;

import afm.Main;
import afm.anime.AnimeType;
import afm.anime.Genre;
import afm.anime.GenreType;
import afm.anime.Search;
import afm.anime.Status;
import afm.common.utils.Utils;
import afm.screens.infowindows.GenreInfoWindow;

public final class SearchScreen extends GridPane {

	@FXML
	private CustomTextField nameField;

	@FXML
	private CustomTextField studioField;

	@FXML
	private CheckComboBox<Genre> genreCombo;
	@FXML
	private CheckComboBox<Genre> demoCombo;
	@FXML
	private CheckComboBox<Genre> themeCombo;
	@FXML
	private Button genreHelpBtn;

	@FXML
	private ComboBox<AnimeType> typeCombo;
	@FXML
	private TextField startYearField;
	@FXML
	private ComboBox<Status> statusCombo;
	@FXML
	private TextField minEpsField;

	@FXML
	private Button searchBtn;

	public SearchScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("SearchScreen"));
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
		startYearField.textProperty().addListener(Utils.intOrEmptyListener());
		statusCombo.getItems().addAll(Status.values());
		minEpsField.textProperty().addListener(Utils.intOrEmptyListener());
	}

	@FXML
	void openGenreInfoWindow() {
		GenreInfoWindow.open(genreHelpBtn);
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
		minEpsField.clear();
	}

	@FXML
	void startSearchProcess() {
		Search search = new Search();

		if (!validateGenres(search))
			return;

		if (!validateName(search))
			return;

		if (!validateStudio(search))
			return;

		search.setAnimeType(typeCombo.getValue());
		search.setStartYear(Utils.toIntOrNull(startYearField.getText()));
		search.setStatus(statusCombo.getValue());

		final String minEps = minEpsField.getText();
		if (minEps != null && !minEps.isBlank()) {
			Integer i = Utils.toIntOrNull(minEps);
			if (i != null)
				search.setMinEpisodes(i);
		}

		Main.getInstance().moveToSearchingScreen(search);
		resetFields();
	}

	private boolean validateGenres(Search search) {
		List<Genre> genres = new ArrayList<>();
		genres.addAll(genreCombo.getCheckModel().getCheckedItems());
		genres.addAll(demoCombo.getCheckModel().getCheckedItems());
		genres.addAll(themeCombo.getCheckModel().getCheckedItems());

		if (genres.isEmpty()) {
			Alert needGenre = new Alert(AlertType.ERROR, "At least 1 genre is needed to search!");
			needGenre.initOwner(Main.getStage());
			needGenre.showAndWait();
			return false;
		} else {
			search.setGenres(genres);
			return true;
		}
	}

	private boolean validateName(Search search) {
		final String name = nameField.getText();

		// if user has only types whitespace in name field, confirm they still want to search
		if (name != null && !name.isEmpty() && name.isBlank()) {
			String header = "Name entered is only whitespace!";
			String content = """
							The anime name will not be taken into account when
							searching, do you still want to search?
							""";

			if (Utils.showAndWaitConfAlert(header, content) == ButtonType.YES) {
				return true;
			} else {
				nameField.clear();
				return false;
			}
		}

		if (name != null)
			search.setName(name.strip());

		return true;
	}

	private boolean validateStudio(Search search) {
		final String studio = studioField.getText();
		// if user has only types whitespace in studio field, confirm they still want to search
		if (studio != null && !studio.isEmpty() && studio.isBlank()) {
			String header = "Studio entered is only whitespace!";
			String content = """
							The anime studio will not be taken into account when
							searching, do you still want to search?
							""";

			if (Utils.showAndWaitConfAlert(header, content) == ButtonType.YES) {
				return true;
			} else {
				studioField.clear();
				return false;
			}
		}

		if (studio != null)
			search.setStudio(studio.strip());

		return true;
	}
}
