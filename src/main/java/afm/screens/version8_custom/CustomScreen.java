package afm.screens.version8_custom;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import afm.anime.Anime;
import afm.anime.Anime.AnimeBuilder;
import afm.anime.Genre;
import afm.anime.Season;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.utils.Utils;

// A lot of things here are identical to SearchScreen
public class CustomScreen extends GridPane {

	@FXML
    private TextField nameField;

    @FXML
    private TextField studioField;

    @FXML
    private ComboBox<Genre> genreCombo;
    @FXML
    private Text genreText;
    @FXML
    private ContextMenu genreContextMenu;

    @FXML
    private ComboBox<String> sznCombo;
    @FXML
    private ComboBox<Integer> yearCombo;

    @FXML
    private TextField totalEpField;
    @FXML
    private TextField currEpField;

	private final ObservableSet<Genre> genreSet = FXCollections.observableSet(EnumSet.noneOf(Genre.class));

	public CustomScreen() throws IOException {
		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("CustomScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

    @FXML
    private void initialize() {
    	genreCombo.getItems().addAll(Genre.values());

    	final var szns = sznCombo.getItems();
    	szns.add("Spring");
    	szns.add("Summer");
    	szns.add("Fall");
    	szns.add("Winter");

    	final var years = yearCombo.getItems();
    	for (int i = Season.END_YEAR; i >= Season.START_YEAR; i--)
    		years.add(i);

    	final SetChangeListener<Genre> genreListener = change -> {
    		updateGenreText();
    		updateGenreContextMenu();
    	};
    	genreSet.addListener(genreListener);

    	totalEpField.textProperty().addListener(Utils.onlyAllowIntegersListener());
    	currEpField.textProperty().addListener(Utils.onlyAllowIntegersListener());
    }

    // When user pressed 'Add' button (to add a Genre to genreSet)
    @FXML
    void addGenre(ActionEvent event) {
    	final Genre selectedGenre = genreCombo.getValue();
    	if (selectedGenre != null) {	// if a genre has been selected
    		genreSet.add(selectedGenre);
    	}
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
                                .map(genre -> new MenuItem("Remove: "+genre.toString()))
                                .collect(Collectors.toList());

        menuItems.forEach(mi -> mi.setOnAction(event -> {
                                    final String s = mi.getText().replace("Remove: ", "");
                                    final Genre g = Genre.parseGenreFromToString(s);
                                    genreSet.remove(g);
                                })
                         );

        genreContextMenu.getItems().setAll(menuItems);
    }

    @FXML
    void clearGenres(ActionEvent event) {
    	genreSet.clear();
    }

    // When user pressed reset button, reset contents of all fields
    @FXML
    void resetFields(ActionEvent event) {
    	nameField.setText("");

        studioField.setText("");

        totalEpField.setText("");
        currEpField.setText("");

        genreCombo.getSelectionModel().clearSelection();
        sznCombo.getSelectionModel().clearSelection();
        yearCombo.getSelectionModel().clearSelection();

        genreSet.clear();

    	// Problem: comboBox promptText isn't showing after reset (if smthn was selected)
    }

    // can only add an anime if the anime has a name & genre
    private boolean cantAdd() {
    	boolean emptyName = nameField.getText().isBlank();
    	boolean emptyGenre = genreSet.isEmpty();

    	if (emptyName) {
    		Alert needName = new Alert(AlertType.ERROR, "The anime needs a name!");
    		needName.showAndWait();
    	}
    	if (emptyGenre) {
    		Alert needGenre = new Alert(AlertType.ERROR, "At least 1 genre is needed!");
    		needGenre.showAndWait();
    	}

    	return emptyName || emptyGenre;
    }

	// Return an Anime object, built from fields in this screen
    private Anime getAnimeFromFields() {
    	final AnimeBuilder builder = Anime.builder(nameField.getText().strip());

    	builder.setCustom(true)
    		   .setGenres(genreSet);

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
