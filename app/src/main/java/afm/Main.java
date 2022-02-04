package afm;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.Application;
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
import afm.common.utils.Utils;
import afm.database.Database;
import afm.screens.Menu;
import afm.screens.infowindows.InfoWindow;
import afm.screens.settings.SettingsScreen;
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
public final class Main extends Application {

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
	public void showWelcomeScreen() {
		InfoWindow.closeAllOpenWindows();

		welcomeScreen.show();
		scene.setRoot(welcomeScreen);
		stage.setHeight(welcomeScreen.getPrefHeight());
		stage.setWidth(welcomeScreen.getPrefWidth());
	}

	public void openSettingsScreen() {
		setScreen(settingsScreen);
	}

	// Set up mainScreen variable & screenList & scene root.
	// Changed it to return Main so welcomeScreen can use method chaining
	public @Nonnull Main setupMainScreen() {
		if (screenList.isEmpty())
			screenList.add(menu);
		scene.setRoot(mainScreen);
		return this;
	}

	private void normalHeight() {
		// all non-table screens have the same pref height
		stage.setHeight(mainScreen.getPrefHeight());
	}

	// Change 'current' screen (right of splitpane)
	private void setScreen(@Nonnull Pane newPane) {
		// InfoWindow.closeAllOpenWindows();

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
			// only increase height if new screen is a table-screen
			if (newPane instanceof ResultsScreen || newPane instanceof MyListScreen
					|| newPane instanceof ToWatchScreen) {
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
	public void init() throws Exception {
		Main.instance = this;

		// OnClose thread will be run upon JVM trying to exit
		Runtime.getRuntime().addShutdownHook(OnClose.INSTANCE);

		mainScreen = new SplitPane();
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("MainScreen"));
		loader.setRoot(mainScreen);
		loader.load();
		screenList = mainScreen.getItems();

		welcomeScreen = new WelcomeScreen();
		menu = new Menu();
		settingsScreen = new SettingsScreen();

		searchScreen = new SearchScreen();
		searchingScreen = new SearchingScreen();
		resultsScreen = new ResultsScreen();

		myListScreen = new MyListScreen();
		toWatchScreen = new ToWatchScreen();

		customScreen = new CustomScreen();

		// load MyList & ToWatch from database into run time data structures
		Database.loadAll();

		applyTheme(Settings.themeProperty.get());
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		stage = primaryStage;

		stage.setTitle("Anime Filler Manager");
		stage.getIcons().add(new Image("icons/MainIcon.ico"));

		stage.setOnCloseRequest(e -> InfoWindow.closeAllOpenWindows());

		stage.setAlwaysOnTop(Settings.get(Settings.Key.ALWAYS_ON_TOP));

		stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
			if (Boolean.TRUE.equals(isFocused)) {
				stage.opacityProperty().bind(Settings.opacityProperty.multiply(0.01));
			} else {
				stage.opacityProperty().bind(Settings.inactiveOpacityProperty.multiply(0.01));
			}
		});

		scene = new Scene(welcomeScreen);
		stage.setScene(scene);
		// for some reason stage.sizeToScene() doesn't work properly
		stage.setHeight(welcomeScreen.getPrefHeight());
		stage.setWidth(welcomeScreen.getPrefWidth());

		stage.centerOnScreen();
		stage.toFront();
		stage.show();
	}

}
