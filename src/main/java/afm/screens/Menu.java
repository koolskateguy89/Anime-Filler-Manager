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

    /*
     * Need to change this to change styleclass instead of changing style to
     * make making changes easier (only have to change stylesheet)
     */

	/*public static final String SELECTED = "-fx-background-color: #77DBE5; -fx-text-fill: #300E4E",
								 NORMAL = "-fx-background-color: #291965; -fx-text-fill: #E8E8E8",
								RESULTS = "-fx-background-color: #0044FF; -fx-text-fill: #FAFAFA";*/
	
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
    	//searchBtn.setStyle(RESULTS);
    }

    private void resetAllStyles() {
    	setStyleClass(searchBtn, NORMAL);
	    setStyleClass(myListBtn, NORMAL);
	    setStyleClass(toWatchBtn, NORMAL);
	    setStyleClass(customBtn, NORMAL);

    	/*searchBtn.setStyle(NORMAL);
    	myListBtn.setStyle(NORMAL);
    	toWatchBtn.setStyle(NORMAL);
    	customBtn.setStyle(NORMAL);*/
    }

    @FXML
    void searchButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	//searchBtn.setStyle(SELECTED);
	    setStyleClass(searchBtn, SELECTED_MENU);
	    main.openSearchScreen();
    }

    @FXML
    void myListButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	//myListBtn.setStyle(SELECTED);
	    setStyleClass(myListBtn, SELECTED_MENU);
	    main.openMyListScreen();
    }

    @FXML
    void toWatchButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	setStyleClass(toWatchBtn, SELECTED_MENU);
    	//toWatchBtn.setStyle(SELECTED);
	    main.openToWatchScreen();
    }

    @FXML
    void customButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	setStyleClass(customBtn, SELECTED_MENU);
    	//customBtn.setStyle(SELECTED);
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
