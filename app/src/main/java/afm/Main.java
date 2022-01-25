package afm;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import afm.anime.Anime;
import afm.anime.Search;
import afm.common.OnClose;
import afm.common.utils.Utils;
import afm.screens.Menu;
import afm.screens.infowindows.InfoWindow;
import afm.screens.settings.SettingsScreen;
import afm.screens.version1_start.StartScreen;
import afm.screens.version2_welcome.WelcomeScreen;
import afm.screens.version3_search.SearchScreen;
import afm.screens.version4_searching.SearchingScreen;
import afm.screens.version5_results.ResultsScreen;
import afm.screens.version6_myList.MyListScreen;
import afm.screens.version7_toWatch.ToWatchScreen;
import afm.screens.version8_custom.CustomScreen;
import afm.user.Settings;
import afm.user.Theme;

// https://github.com/koolskateguy89/Anime-Filler-Manager
public class Main extends Application {

	// unresolved reference getInstance in Kotlin when using @Getter :/
	private static Main instance;

	public static @Nonnull Main getInstance() {
		return instance;
	}

	public static @Nonnull Stage getStage() {
		return instance.stage;
	}

	private Stage stage;
	private Scene scene;

	public SplitPane mainScreen;
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
				welcomeScreen = new WelcomeScreen();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		normalHeight();
		stage.setWidth(welcomeScreen.getPrefWidth());
		scene.setRoot(welcomeScreen);
	}

	public void openSettingsScreen() {
		setScreen(settingsScreen);
	}

	// Set up mainScreen variable & screenList & scene root.
	// Changed it to return Main so welcomeScreen can do method chaining
	public @Nonnull Main initMainScreen() {
		if (mainScreen == null || screenList == null)
			try {
				mainScreen = new SplitPane();
				FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("MainScreen"));
				loader.setRoot(mainScreen);
				loader.load();

				screenList = mainScreen.getItems();
				screenList.add(menu);
			} catch (IOException e) {
				e.printStackTrace();
			}

		scene.setRoot(mainScreen);
		stage.setResizable(true);

		return this;
	}

	private void normalHeight() {
		stage.setHeight(searchScreen.getPrefHeight()); // most screens have the same pref height
	}

	// Change 'current' screen (right of splitpane)
	private void setScreen(@Nonnull Pane newPane) {
		//InfoWindow.closeAllOpenWindows();

		if (screenList.size() == 1) {
			screenList.add(newPane);
		} else {
			// insert new pane & disable old pane
			screenList.set(1, newPane).setDisable(true);
		}
		newPane.setDisable(false);

		scene.setRoot(mainScreen);

		// Resize window to properly fit new screen
		final double width = menu.getPrefWidth() + newPane.getPrefWidth() + 25;
		stage.setWidth(width);

		double dividerPosition = menu.getPrefWidth() / width;
		mainScreen.setDividerPosition(0, dividerPosition);

		if (!stage.isFullScreen()) {
			// only increase height if new screen is a screen with a table
			if (newPane instanceof ResultsScreen || newPane instanceof MyListScreen || newPane instanceof ToWatchScreen) {
				stage.setHeight(Math.max(menu.getPrefHeight(), newPane.getPrefHeight()) + 40);
			} else {
				normalHeight();
			}
		}
	}

	// when search screen button is pressed:
	// if results, show results screen -> when pressed again, show search screen
	// else show search screen
	public void openSearchOrResultsScreen() {
		if (!resultsScreen.hasResults() || screenList.get(1) == resultsScreen) {
			menu.searchScreen();
			setScreen(searchScreen);
			resultsScreen.setResults(Collections.emptyList());
		} else {
			// only want to show results screen if not on search school
			if (screenList.get(1) != searchScreen) {
				menu.resultsScreen();
				setScreen(resultsScreen);
			}
		}
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

	public void moveToSearchingScreen(@Nonnull Search s) {
		searchingScreen.setSearch(s);
		searchingScreen.startSearch();

		scene.setRoot(searchingScreen);
		stage.setWidth(searchingScreen.getPrefWidth());
	}

	public void moveToResultsScreen(@Nonnull List<Anime> results) {
		resultsScreen.setResults(results);

		menu.resultsScreen();
		setScreen(resultsScreen);
	}

	public void applyTheme(@Nullable Theme theme) {
		if (theme == null)
			return;

		theme.applyTo(welcomeScreen);
		theme.applyTo(menu);
		theme.applyTo(settingsScreen);

		theme.applyTo(searchScreen);
		theme.applyTo(searchingScreen);
		theme.applyTo(resultsScreen);

		theme.applyTo(myListScreen);
		theme.applyTo(toWatchScreen);

		theme.applyTo(customScreen);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		Main.instance = this;
		stage = primaryStage;

		stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
			if (Boolean.TRUE.equals(isFocused)) {
				stage.opacityProperty().bind(Settings.opacityProperty.multiply(0.01));
			} else {
				stage.opacityProperty().bind(Settings.inactiveOpacityProperty.multiply(0.01));
			}
		});

		stage.setAlwaysOnTop(Settings.get(Settings.Key.ALWAYS_ON_TOP));

		stage.setTitle("Anime Filler Manager");

		stage.getIcons().add(new Image("icons/MainIcon.ico"));

		stage.setResizable(false);

		stage.setOnCloseRequest(e -> InfoWindow.closeAllOpenWindows());

		// OnClose thread will be run upon JVM trying to exit
		Runtime.getRuntime().addShutdownHook(OnClose.INSTANCE);

		// start loading is done internally in start screen (by button action)
		startScreen = new StartScreen();
		scene = new Scene(startScreen);

		// don't lazily load to make applying theme easier
		searchingScreen = new SearchingScreen();
		resultsScreen = new ResultsScreen();

		stage.setScene(scene);
		stage.centerOnScreen();
		stage.toFront();
		stage.show();
	}

}
