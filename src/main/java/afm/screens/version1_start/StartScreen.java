package afm.screens.version1_start;

import java.io.IOException;

import javafx.application.Platform;
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
import afm.screens.SettingsScreen;
import afm.screens.infowindows.InfoWindow;
import afm.screens.version2_welcome.WelcomeScreen;
import afm.screens.version3_search.SearchScreen;
import afm.screens.version6_myList.MyListScreen;
import afm.screens.version7_toWatch.ToWatchScreen;
import afm.screens.version8_custom.CustomScreen;
import afm.user.Settings;
import afm.utils.Facts;
import afm.utils.Handler;
import afm.utils.Utils;

public class StartScreen extends Pane {

	private final Handler h;
	private Pair<Integer, String> fact;
	private Task<Void> loadTask;
	
	public StartScreen(Handler h) throws IOException {
		this.h = h;
		
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("StartScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}
	
	@FXML void initialize() {
		//Get a random fact and its id and display it in factText
		if (Settings.showFacts()) {
			Facts.init(); // only need to init if showing facts
			fact = Facts.getRandomFact();
			factText.setText("Fact " + fact.getKey() + ": " + fact.getValue());
		} else {
			factText.setVisible(false);
		}
		
		//Initialise the loadTask which loads all screens
		loadTask = new Task<Void>() {
			protected Void call() {
				try {
					Platform.runLater(() -> {
						// Hide startBtn
						startBtn.setDisable(true);
						startBtn.setVisible(false);
						// Enable progressBar
						progressBar.setDisable(false);
						// Show loadLbl
						loadLbl.setOpacity(1);
					});
					
					updateProgress(5,100);
					
					Season.init();
					updateProgress(10,100);
					
					InfoWindow.init(h);
					
					//Load different Screens
					final Main main = h.getMain();
					
					main.welcomeScreen = new WelcomeScreen(h);
					updateProgress(25,100);
					main.menu = new Menu(h);
					updateProgress(35,100);
					main.settingsScreen = new SettingsScreen();
					updateProgress(40,100);
					main.searchScreen = new SearchScreen(h);
					updateProgress(45,100);
					main.myListScreen = new MyListScreen();
					updateProgress(55,100);
					main.toWatchScreen = new ToWatchScreen();
					updateProgress(65,100);
					main.customScreen = new CustomScreen();
					updateProgress(80,100);
					
					Thread updateThread = new Thread(() ->  {
						long time = 1000; // i want it to take x seconds to fill rest of bar
						long wait = time / (98 - 80);
						for (int n = 80; n < 98; n++) {
							Utils.sleep(wait);
							updateProgress(n, 100);
						}
					});
					
					updateThread.setDaemon(false);
					updateThread.start();
					
					
					// load MyList & ToWatch from database into run time memory
					Database.init(h);
					
					
					updateThread.join();
					
					//Completely fill progressBar
					updateProgress(100, 100);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// apparently it's better to re-interrupt soooo
					// "Restore interrupted state"
					Thread.currentThread().interrupt();
				}
				
				return null;
			}
		};

		//Once loading has been finished do:
		loadTask.setOnSucceeded(event -> {
			//change loadLbl text to notify user
			loadLbl.setOpacity(1);
			loadLbl.setText("Done!");
			//enable startBtn
			startBtn.setVisible(true);
			startBtn.setDisable(false);
		});
	}
	
	@FXML private ProgressBar progressBar;

	@FXML private Button startBtn;
    
	@FXML private Text factText;
    
	@FXML private Text loadLbl;

    // When startBtn is first pressed, load all elements first,
	// otherwise, move to welcome screen
    @FXML void moveToWelcomeScreen(ActionEvent event) {
    	if (!loadTask.isRunning() && !loadTask.isDone()) {
    		// Bind progressBar progress to the loadTask
    		progressBar.progressProperty().bind(loadTask.progressProperty());
    		
    		// Show a new fact
    		if (Settings.showFacts()) {
    			fact = Facts.getRandomFact();
    			factText.setText("Fact " + fact.getKey() + ": " + fact.getValue());
    		}
    		
    		loadAll();
    	} else
    		h.getMain().moveToWelcomeScreen();
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
				
				Utils.sleep(60);
			}
		});
		
		/* Change the thread from a user thread to a daemon thread (low priority)
		 *  so the JVM can terminate if the thread is still running.
		 *
		 * The problem that this solved:
		 * 	- if user closed window while loading, the thread would still be running,
		 *    this was solved by the workaround (mentioned above) of checking the screen
		 *    was still showing
		*/
		fadeLoadLbl.setDaemon(true);
		fadeLoadLbl.start();
		
		//Start loading screens
		final Thread loadThread = new Thread(loadTask);
		loadThread.setDaemon(true);
		loadThread.start();
	}
}
