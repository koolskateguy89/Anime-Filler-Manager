package afm.screens.version3_search;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.TextFields;

import afm.Main;
import afm.anime.Genre;
import afm.anime.Search;
import afm.anime.Season;
import afm.screens.infowindows.GenreInfoWindow;
import afm.utils.Utils;

// TODO: don't when search screen button is pressed, show results; when pressed again, show search screen
public final class SearchScreen extends GridPane {

	private static final String REMOVE = "Remove ";

	// use a Pane as a placeholder for nameField to make using SceneBuilder easier
	// (its style is white background)
	@FXML
	private StackPane namePane;
	private TextField nameField;

	@FXML
	private StackPane studioPane;
	private TextField studioField;

	@FXML
	private CheckComboBox<Genre> genreCombo;
	@FXML
	private Button genreHelpBtn;

	@FXML
	private ComboBox<String> sznCombo;
	@FXML
	private ComboBox<Integer> yearCombo;
	@FXML
	private ContextMenu seasonContextMenu;
	@FXML
	private Text seasonText;

	@FXML
	private TextField minEpsField;

	@FXML
	private Button searchBtn;

	private final ObservableSet<Season> seasonSet = FXCollections.observableSet(new TreeSet<>());


	public SearchScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("SearchScreen"));
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

		minEpsField.textProperty().addListener(Utils.onlyAllowIntegersListener());


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

		final var seasonItems = sznCombo.getItems();
		seasonItems.add("Spring");
		seasonItems.add("Summer");
		seasonItems.add("Fall");
		seasonItems.add("Winter");

		final var yearItems = yearCombo.getItems();
		for (int i = Season.END_YEAR; i >= Season.START_YEAR; i--)
			yearItems.add(i);

		final SetChangeListener<Season> sznListener = change -> {
			updateSeasonText();
			updateSeasonContextMenu();
		};
		seasonSet.addListener(sznListener);
	}

	@FXML
	void addSeason(ActionEvent event) {
		final String s = sznCombo.getValue();
		final Integer year = yearCombo.getValue();

		if (s == null || year == null)
			return;

		seasonSet.add(Season.getSeason(s, year));
	}

	// Add all Seasons in the currently selected year
	@FXML
	void addAllYear(ActionEvent event) {
		final Integer year = yearCombo.getValue();

		if (year == null)
			return;

		seasonSet.addAll(List.of(Season.getAllSeasonsFromYear(year)));
	}

	private void updateSeasonText() {
		StringBuilder sb = new StringBuilder("Seasons: ");

		Iterator<Season> it = seasonSet.iterator();
		if (it.hasNext())
			sb.append(it.next());
		while (it.hasNext())
			sb.append(", ").append(it.next());

		seasonText.setText(sb.toString());
	}

	private void updateSeasonContextMenu() {
		if (seasonSet.isEmpty()) {
			seasonContextMenu.getItems().clear();
			return;
		}

		/* map from Season to its 'corresponding' MenuItem,
		 * collect to a List,
		 * add an ActionListener to each MenuItem - to remove the corresponding Season,
		 * set the contextMenu as the List
		 */
		var menuItems = seasonSet.stream()
								 .map(season -> new MenuItem(REMOVE+season.toString()))
								 .collect(Collectors.toList());

		menuItems.forEach(mi -> mi.setOnAction(event -> {
									final String str = mi.getText().replace(REMOVE, "");
									final Season s = Season.getSeasonFromToString(str);
									seasonSet.remove(s);
								})
						 );

		seasonContextMenu.getItems().setAll(menuItems);
	}

	@FXML
	void openGenreInfoWindow(ActionEvent event) {
		GenreInfoWindow.open(genreHelpBtn);
	}

	@FXML
	void clearGenres(ActionEvent event) {
		genreCombo.getCheckModel().clearChecks();
	}

	@FXML
	void clearSeasons(ActionEvent event) {
		sznCombo.setValue(null);
		yearCombo.setValue(null);
		seasonSet.clear();
	}

	// When user pressed reset button, reset contents of all fields
	@FXML
	void clearFields(ActionEvent event) {
		nameField.clear();

		studioField.clear();

		minEpsField.clear();

		genreCombo.getCheckModel().clearChecks();
		sznCombo.getSelectionModel().clearSelection();
		yearCombo.getSelectionModel().clearSelection();

		minEpsField.clear();

		clearGenres(null);
		clearSeasons(null);

		// FIXME: comboBox promptText isn't showing after reset (if smthn was selected)
	}

	@FXML
	void startSearchProcess(ActionEvent event) {
		Search search = new Search();

		if (!confirmGenres(search))
			return;

		if (!confirmName(search))
			return;

		if (!confirmStudio(search))
			return;

		if (!seasonSet.isEmpty())
			search.setSeasons(seasonSet);

		final String minEps = minEpsField.getText();
		if (minEps != null && !minEps.isBlank() && Utils.isInteger(minEps)) {
			search.setMinEpisodes(Integer.parseInt(minEps));
		}

		Main.getInstance().moveToSearchingScreen(search);
		clearFields(null);
	}

	private boolean confirmGenres(Search search) {
		var genres = genreCombo.getCheckModel().getCheckedItems();

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

	private boolean confirmName(Search search) {
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

	private boolean confirmStudio(Search search) {
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
