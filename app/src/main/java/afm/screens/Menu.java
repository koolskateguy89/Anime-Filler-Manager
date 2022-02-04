package afm.screens;

import static afm.common.utils.Utils.setStyleClass;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import afm.Main;
import afm.common.utils.Utils;

public final class Menu extends VBox {

	public static final List<String> SELECTED = List.of("button", "selected"),
									   NORMAL = List.of("button", "menu-button"),
									  RESULTS = List.of("button", "results");

	private static final List<String> SELECTED_MENU = List.of("button", "selected-menu-button");
	private static final List<String> MISC = List.of("button", "misc-menu-button");

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
	private Button settingsBtn;

	public void resultsScreen() {
		resetAllStyles();
		setStyleClass(searchBtn, RESULTS);
	}

	public void searchScreen() {
		resetAllStyles();
		setStyleClass(searchBtn, SELECTED_MENU);
	}

	private void resetAllStyles() {
		setStyleClass(searchBtn, NORMAL);
		setStyleClass(myListBtn, NORMAL);
		setStyleClass(toWatchBtn, NORMAL);
		setStyleClass(customBtn, NORMAL);

		settingsBtn.getStyleClass().setAll(MISC);
	}

	@FXML
	void searchButtonPressed() {
		main.openSearchOrResultsScreen();
	}

	@FXML
	void myListButtonPressed() {
		resetAllStyles();
		setStyleClass(myListBtn, SELECTED_MENU);
		main.openMyListScreen();
	}

	@FXML
	void toWatchButtonPressed() {
		resetAllStyles();
		setStyleClass(toWatchBtn, SELECTED_MENU);
		main.openToWatchScreen();
	}

	@FXML
	void customButtonPressed() {
		resetAllStyles();
		setStyleClass(customBtn, SELECTED_MENU);
		main.openCustomScreen();
	}

	@FXML
	void openHomeScreen() {
		resetAllStyles();
		main.showWelcomeScreen();
	}

	@FXML
	void openSettingsScreen() {
		resetAllStyles();
		setStyleClass(settingsBtn, SELECTED);
		main.openSettingsScreen();
	}

	public void openSearchScreenFromWelcome() {
		searchButtonPressed();
	}

	public void openMyListScreenFromWelcome() {
		myListButtonPressed();
	}

	public void openToWatchScreenFromWelcome() {
		toWatchButtonPressed();
	}
}
