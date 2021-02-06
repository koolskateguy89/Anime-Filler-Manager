package afm;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import afm.anime.Anime;
import afm.anime.Search;
import afm.screens.MainScreen;
import afm.screens.Menu;
import afm.screens.SettingsScreen;
import afm.screens.infowindows.InfoWindow;
import afm.screens.version1_start.StartScreen;
import afm.screens.version2_welcome.WelcomeScreen;
import afm.screens.version3_search.SearchScreen;
import afm.screens.version4_searching.SearchingScreen;
import afm.screens.version5_results.ResultsScreen;
import afm.screens.version6_myList.MyListScreen;
import afm.screens.version7_toWatch.ToWatchScreen;
import afm.screens.version8_custom.CustomScreen;
import afm.utils.Handler;
import afm.utils.OnClose;

// https://github.com/koolskateguy89/Anime-Filler-Manager
public class Main extends Application {

	final Handler h = new Handler(this);

	public Stage getStage() {
		return stage;
	}

	private Stage stage;
	private Scene scene;

	public MainScreen mainScreen;
	private ObservableList<Node> screenList;

	public StartScreen startScreen;
	public WelcomeScreen welcomeScreen;
	public Menu menu;
	public SettingsScreen settingsScreen;


	public SearchScreen searchScreen;
	private SearchingScreen searchingScreen;
	private ResultsScreen resultsScreen;

	public MyListScreen myListScreen;

	public ToWatchScreen toWatchScreen;

	public CustomScreen customScreen;

	// welcomeScreen == home screen
	public void moveToWelcomeScreen() {
		InfoWindow.closeAllOpenWindows();

		if (welcomeScreen == null) {
			try {
				welcomeScreen = new WelcomeScreen(h);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		normalSize();
		scene.setRoot(welcomeScreen);
	}

	public void openSettingsScreen() {
		setScreen(settingsScreen);
	}

	// Set up mainScreen variable & screenList & scene root.
	// Changed it to return Main so welcomeScreen can do method chaining
	public Main initMainScreen() {
		if (mainScreen == null || screenList == null)
			try {
				mainScreen = new MainScreen();
				screenList = mainScreen.getItems();
				screenList.add(menu);
			} catch (IOException e) {
				e.printStackTrace();
			}

		scene.setRoot(mainScreen);
		stage.setResizable(true);

		return this;
	}

	private void normalSize() {
		stage.setHeight(searchScreen.getPrefHeight()); // most screens have the same pref height
	}

	// Change 'current' screen (right of splitpane)
	private void setScreen(Pane newPane) {
		//InfoWindow.closeAllOpenWindows();

		if (screenList.size() == 1)
			screenList.add(newPane);
		else {
			screenList.get(1).setDisable(true);
			screenList.set(1, newPane);
		}
		newPane.setDisable(false);

		scene.setRoot(mainScreen);

		// Resize window to properly fit new screen
		stage.setWidth(menu.getPrefWidth() + newPane.getPrefWidth() + 25);
		mainScreen.setDividerPosition(0, 0.13);

		// only increase height if new screen is a screen with a table
		if (newPane instanceof ResultsScreen || newPane instanceof MyListScreen || newPane instanceof ToWatchScreen) {
			stage.setHeight(Math.max(menu.getPrefHeight(), newPane.getPrefHeight()) + 40);
		} else {
			normalSize();
		}
	}

	public void openSearchScreen() {
		setScreen(searchScreen);
	}

	public void openMyListScreen() {
		myListScreen.refreshTable();
		setScreen(myListScreen);
	}

	public void openToWatchScreen() {
		toWatchScreen.refreshTable();
		setScreen(toWatchScreen);
	}

	public void openCustomScreen() {
		// no need to reset fields when opening CustomScreen
		setScreen(customScreen);
	}

	public void moveToSearchingScreen(Search s) {
		if (searchingScreen == null)
			try {
				searchingScreen = new SearchingScreen(h);
			} catch (IOException e) {
				e.printStackTrace();
				Platform.exit();
			}

		searchingScreen.setSearch(s);
		searchingScreen.startSearch();

		scene.setRoot(searchingScreen);
		stage.setWidth(searchingScreen.getPrefWidth());
	}

	public void moveToResultsScreen(List<Anime> results) {
		if (resultsScreen == null)
			try {
				resultsScreen = new ResultsScreen();
			} catch (IOException e) {
				e.printStackTrace();
				Platform.exit();
			}

		resultsScreen.setResults(results);

		menu.resultsScreen();
		setScreen(resultsScreen);
	}

	@Override
	public void start(final Stage primaryStage) {
		try {
			stage = primaryStage;

			stage.setTitle("Anime Filler Manager");

			stage.getIcons().add(new Image("icons/MainIcon.ico"));

			stage.setResizable(false);

			stage.setOnCloseRequest(e -> InfoWindow.closeAllOpenWindows());

			// OnClose thread will be run upon JVM trying to exit
			Runtime.getRuntime().addShutdownHook(OnClose.getInstance());

			// start loading is done internally in start screen (by button action)
			startScreen = new StartScreen(h);
			scene = new Scene(startScreen);

			stage.setScene(scene);
			stage.centerOnScreen();
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			Platform.exit();
		}
	}

	static void launch0(String[] args) {
		launch(args);
	}

}
