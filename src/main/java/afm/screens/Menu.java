package afm.screens;

import static afm.utils.Utils.setStyleClass;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import afm.Main;
import afm.utils.Utils;

public final class Menu extends VBox {

	public static final List<String> SELECTED = List.of("button", "Selected"),
									   NORMAL = List.of("button", "MenuButton"),
									  RESULTS = List.of("button", "Results");

	private static final List<String> SELECTED_MENU = List.of("button", "SelectedMenuButton");

	private final Main main = Main.getInstance();

	public Menu() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("Menu"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

    @FXML
    private Button searchBtn;

    @FXML
    private Button myListBtn;

    @FXML
    private Button toWatchBtn;

    @FXML
    private Button customBtn;

    @FXML
    private Button homeBtn;

    @FXML
    private Button settingsBtn;

    public void resultsScreen() {
    	resetAllStyles();
    	setStyleClass(searchBtn, RESULTS);
    }

    private void resetAllStyles() {
    	setStyleClass(searchBtn, NORMAL);
	    setStyleClass(myListBtn, NORMAL);
	    setStyleClass(toWatchBtn, NORMAL);
	    setStyleClass(customBtn, NORMAL);
    }

    @FXML
    void searchButtonPressed(ActionEvent event) {
    	resetAllStyles();
	    setStyleClass(searchBtn, SELECTED_MENU);
	    main.openSearchScreen();
    }

    @FXML
    void myListButtonPressed(ActionEvent event) {
    	resetAllStyles();
	    setStyleClass(myListBtn, SELECTED_MENU);
	    main.openMyListScreen();
    }

    @FXML
    void toWatchButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	setStyleClass(toWatchBtn, SELECTED_MENU);
	    main.openToWatchScreen();
    }

    @FXML
    void customButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	setStyleClass(customBtn, SELECTED_MENU);
    	main.openCustomScreen();
    }

    @FXML
    void openHomeScreen(ActionEvent event) {
    	resetAllStyles();
	    main.moveToWelcomeScreen();
    }

    @FXML
    void openSettingsScreen(ActionEvent event) {
    	resetAllStyles();
	    main.openSettingsScreen();
    }

    public void openSearchScreenFromWelcome() {
    	searchButtonPressed(null);
    }

    public void openMyListScreenFromWelcome() {
    	myListButtonPressed(null);
    }

    public void openToWatchScreenFromWelcome() {
    	toWatchButtonPressed(null);
    }
}
