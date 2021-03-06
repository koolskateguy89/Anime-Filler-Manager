package afm.screens.version3_search;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
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
import javafx.scene.text.Text;

import afm.Main;
import afm.anime.Genre;
import afm.anime.Search;
import afm.anime.Season;
import afm.screens.infowindows.GenreInfoWindow;
import afm.utils.Utils;

public final class SearchScreen extends GridPane {

	private static final String REMOVE = "Remove ";

	@FXML
	private TextField nameField;

	@FXML
	private TextField studioField;

	@FXML
	private ComboBox<Genre> genreCombo;
	@FXML
	private Button genreHelpBtn;
	@FXML
	private Text genreText;
	@FXML
	private ContextMenu genreContextMenu;

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

	private final ObservableSet<Genre> genreSet = FXCollections.observableSet(EnumSet.noneOf(Genre.class));
	private final ObservableSet<Season> seasonSet = FXCollections.observableSet(new TreeSet<>());


	public SearchScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("SearchScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	private void initialize() {
		genreCombo.getItems().addAll(Genre.values());

		final var seasonItems = sznCombo.getItems();
		seasonItems.add("Spring");
		seasonItems.add("Summer");
		seasonItems.add("Fall");
		seasonItems.add("Winter");

		final var yearItems = yearCombo.getItems();
		for (int i = Season.END_YEAR; i >= Season.START_YEAR; i--)
			yearItems.add(i);


		/* Made genreSet an ObservableSet<Genre> to be able to add a
		 * listener to it -> was able to take out a lot of code :)
		 */
		final SetChangeListener<Genre> genreListener = change -> {
			updateGenreText();
			updateGenreContextMenu();
		};
		genreSet.addListener(genreListener);

		final SetChangeListener<Season> sznListener = change -> {
			updateSeasonText();
			updateSeasonContextMenu();

		};
		seasonSet.addListener(sznListener);

		minEpsField.textProperty().addListener(Utils.onlyAllowIntegersListener());
	}

	@FXML
	void addGenre(ActionEvent event) {
		final Genre selectedGenre = genreCombo.getValue();
		// if a genre has been selected
		if (selectedGenre != null) {
			genreSet.add(selectedGenre);
		}
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

	// Update genreText when genreSet has changed
	private void updateGenreText() {
		StringBuilder sb = new StringBuilder("Genres: ");

		Iterator<Genre> it = genreSet.iterator();
		if (it.hasNext())
			sb.append(it.next());
		while (it.hasNext())
			sb.append(", ").append(it.next());

		genreText.setText(sb.toString());
	}

	// Update genreContextMenu when genreSet has changed
	private void updateGenreContextMenu() {
		if (genreSet.isEmpty()) {
			genreContextMenu.getItems().clear();
			return;
		}

		/* map from Genre to its 'corresponding' MenuItem,
		 * collect to a List,
		 * add an ActionListener to each MenuItem - to remove the corresponding Genre,
		 * set the contextMenu as the List
		 */
		var menuItems = genreSet.stream()
								.map(genre -> new MenuItem(REMOVE+genre.toString()))
								.collect(Collectors.toList());

		menuItems.forEach(mi -> mi.setOnAction(event -> {
									final String s = mi.getText().replace(REMOVE, "");
									final Genre g = Genre.parseGenreFromToString(s);
									genreSet.remove(g);
								})
						 );

		genreContextMenu.getItems().setAll(menuItems);
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
		genreCombo.setValue(null);
		genreSet.clear();
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
		nameField.setText("");

		studioField.setText("");

		minEpsField.setText("");

		genreCombo.getSelectionModel().clearSelection();
		sznCombo.getSelectionModel().clearSelection();
		yearCombo.getSelectionModel().clearSelection();

		minEpsField.setText(null);

		clearGenres(null);
		clearSeasons(null);

		// Problem: comboBox promptText isn't showing after reset (if smthn was selected)
	}

	@FXML
	void startSearchProcess(ActionEvent event) {
		Search search = new Search();

		if (!confirmGenre(search))
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

	private boolean confirmGenre(Search search) {
		if (genreSet.isEmpty()) {
			// if user 'selected' a Genre but didn't add it, ask if they want to add it
			Genre potentialGenre = genreCombo.getValue();
			if (potentialGenre != null) {
				String content =
						"You haven't added any Genres, " + "but you have selected {" +
						potentialGenre.toString().replace("(", "").replace(")", "") +
						'}' + '.' +
						'\n' +
						"Do you want to search for this genre?";
				ButtonType result = Utils.showAndWaitConfAlert(content);
				if (result != ButtonType.YES)
					return false;

				genreSet.add(potentialGenre);
			} else {
				Alert needGenre = new Alert(AlertType.ERROR, "At least 1 genre is needed to search!");
				needGenre.initOwner(Main.getStage());
				needGenre.showAndWait();
				return false;
			}
		}

		search.setGenres(genreSet);
		return true;
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
				nameField.setText(null);
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
				studioField.setText(null);
				return false;
			}
		}

		if (studio != null)
			search.setStudio(studio.strip());

		return true;
	}
}
