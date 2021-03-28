package afm.screens;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import afm.Main;
import afm.utils.Utils;

public final class Menu extends Pane {

	public static final String SELECTED = "-fx-background-color: #77DBE5; -fx-text-fill: #300E4E",
								 NORMAL = "-fx-background-color: #282828; -fx-text-fill: #E8E8E8",
								RESULTS = "-fx-background-color: #0044FF; -fx-text-fill: #FAFAFA";


	final Main main = Main.getInstance();

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

    /*
     * Load the icons here rather than set it through Scenebuilder as it is
     * easier to get them to be the desired size like this
     */
    @FXML
    void initialize() {
    	Image homeIcon = new Image("icons/HomeIcon.png", 16, 16, true, true);
    	ImageView iv = new ImageView(homeIcon);
    	iv.setSmooth(true);
    	homeBtn.setGraphic(iv);

    	Image settingsIcon = new Image("icons/SettingsIcon.gif", 16, 16, true, true);
    	iv = new ImageView(settingsIcon);
    	iv.setSmooth(true);
    	settingsBtn.setGraphic(iv);
    }

    public void resultsScreen() {
    	resetAllStyles();
    	searchBtn.setStyle(RESULTS);
    }

    private void resetAllStyles() {
    	searchBtn.setStyle(NORMAL);
    	myListBtn.setStyle(NORMAL);
    	toWatchBtn.setStyle(NORMAL);
    	customBtn.setStyle(NORMAL);
    }

    @FXML
    void searchButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	searchBtn.setStyle(SELECTED);
	    main.openSearchScreen();
    }

    @FXML
    void myListButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	myListBtn.setStyle(SELECTED);
	    main.openMyListScreen();
    }

    @FXML
    void toWatchButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	toWatchBtn.setStyle(SELECTED);
	    main.openToWatchScreen();
    }

    @FXML
    void customButtonPressed(ActionEvent event) {
    	resetAllStyles();
    	customBtn.setStyle(SELECTED);
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
