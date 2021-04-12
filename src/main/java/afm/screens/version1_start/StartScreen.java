package afm.screens.version1_start;

import static afm.utils.Utils.sleep;

import java.io.IOException;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Pair;

import afm.Main;
import afm.anime.Season;
import afm.database.Database;
import afm.screens.Menu;
import afm.screens.infowindows.InfoWindow;
import afm.screens.settings.SettingsScreen;
import afm.screens.version2_welcome.WelcomeScreen;
import afm.screens.version3_search.SearchScreen;
import afm.screens.version6_myList.MyListScreen;
import afm.screens.version7_toWatch.ToWatchScreen;
import afm.screens.version8_custom.CustomScreen;
import afm.user.Settings;
import afm.utils.Facts;
import afm.utils.Utils;

public class StartScreen extends Pane {

	private Pair<Integer, String> fact;
	public LoadTask loadTask;

	public StartScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("StartScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	void initialize() {
		//Get a random fact and its id and display it in factText
		if (Settings.get(Settings.Key.SHOW_FACTS)) {
			Facts.init(); // only need to init if showing facts
			fact = Facts.getRandomFact();
			factText.setText("Fact " + fact.getKey() + ": " + fact.getValue());
		} else {
			factText.setVisible(false);
		}

		//Initialise the loadTask which loads all screens
		loadTask = new LoadTask();

		// automate the loading process
		if (Settings.get(Settings.Key.SKIP_LOADING)) {
			startBtn.fire();
			var onSucceeded = loadTask.getOnSucceeded();
			loadTask.setOnSucceeded(e -> {
				// perform the 'original' onSucceeded task
				onSucceeded.handle(null);
				// go to next screen
				startBtn.fire();
			});
		}
	}

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Button startBtn;

	@FXML
	private Text factText;

	@FXML
	private Text loadLbl;

	// When startBtn is first pressed, load all elements first,
	// otherwise, move to welcome screen
	@FXML
	void moveToWelcomeScreen(ActionEvent event) {
		if (!loadTask.isRunning() && !loadTask.isDone()) {
			// Bind progressBar progress to the loadTask
			progressBar.progressProperty().bind(loadTask.progressProperty());

			// Show a new fact
			if (Settings.get(Settings.Key.SHOW_FACTS)) {
				fact = Facts.getRandomFact();
				factText.setText("Fact " + fact.getKey() + ": " + fact.getValue());
			}

			loadAll();
		} else
			Main.getInstance().moveToWelcomeScreen();
	}

	private void loadAll() {
		//fade the loadLbl in and out
		final Thread fadeLoadLbl = new Thread(() -> {
			//start by fading out (decrementing opacity)
			boolean fadeOut = true;
			//fade while loading isn't finished & stage is still showing
			while (!loadTask.isDone()) {
				if (fadeOut) {
					//to fade out, decrease opacity
					loadLbl.setOpacity(loadLbl.getOpacity() - 0.1);
				} else {
					//to fade in, increase opacity
					loadLbl.setOpacity(loadLbl.getOpacity() + 0.1);
				}

				//start to fade out once loadLbl is fully visible
				if (loadLbl.getOpacity() >= 1)
					fadeOut = true;
				//start to fade in once loadLbl is not visible
				else if (loadLbl.getOpacity() <= 0)
					fadeOut = false;

				sleep(60);
			}
		});

		/* Change the thread from a user thread to a daemon thread (low priority)
		 *  so the JVM can terminate if the thread is still running.
		 *
		 * The problem that this solved:
		 * 	- if user closed window while loading, the thread would still be running,
		 *	this was solved by the workaround (mentioned above) of checking the screen
		 *	was still showing
		*/
		fadeLoadLbl.setDaemon(true);
		fadeLoadLbl.start();

		//Start loading screens
		final Thread loadThread = new Thread(loadTask);
		loadThread.setDaemon(true);
		loadThread.start();
	}

	public class LoadTask extends Task<Void> {

		private final int max = 100;
		private double progress;

		public LoadTask() {
			setOnSucceeded(event -> {
				//change loadLbl text to notify user
				loadLbl.setOpacity(1);
				loadLbl.setText("Done!");
				//enable startBtn
				startBtn.setVisible(true);
				startBtn.setDisable(false);
			});
		}

		protected Void call() {
			try {
				// Hide startBtn
				startBtn.setDisable(true);
				startBtn.setVisible(false);
				// Enable progressBar
				progressBar.setDisable(false);
				// Show loadLbl
				loadLbl.setOpacity(1);

				updateProgress(5, max);

				Season.init();
				updateProgress(10, max);

				InfoWindow.init();
				updateProgress(15,  max);

				// Load different Screens
				final Main main = Main.getInstance();

				main.welcomeScreen = new WelcomeScreen();
				updateProgress(25, max);
				main.menu = new Menu();
				updateProgress(35, max);
				main.settingsScreen = new SettingsScreen();
				main.settingsScreen.setDisable(true);
				updateProgress(40, max);
				main.searchScreen = new SearchScreen();
				main.searchScreen.setDisable(true);
				updateProgress(45, max);
				main.myListScreen = new MyListScreen();
				main.myListScreen.setDisable(true);
				updateProgress(55, max);
				main.toWatchScreen = new ToWatchScreen();
				main.toWatchScreen.setDisable(true);
				updateProgress(65, max);
				main.customScreen = new CustomScreen();
				main.customScreen.setDisable(true);
				updateProgress(80, max);

				// load MyList & ToWatch from database into run time data structures
				progress = 80;
				Database.init(this, 80,  max);

				// Completely fill progressBar
				updateProgress(1.0,  1.0);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		public void incrementProgress(double inc) {
			// for some reason it freezes when using (getProgress() + inc)
			progress += inc;
			updateProgress(progress, max);
		}

	}
}
