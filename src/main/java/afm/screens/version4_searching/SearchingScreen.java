package afm.screens.version4_searching;

import static afm.utils.Utils.sleep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import afm.anime.Anime;
import afm.anime.Search;
import afm.user.Settings;
import afm.utils.Handler;
import afm.utils.SoundFactory;
import afm.utils.Utils;

public class SearchingScreen extends Pane {

	private final Handler h;

	private Search search;

	private final List<Anime> results = Collections.synchronizedList(new ArrayList<Anime>());

	private ObservableList<Node> circles;

	@FXML private HBox loadingBox;
	@FXML private Button resultsBtn;


	public SearchingScreen(Handler h) throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("SearchingScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();

		this.h = h;
	}

	// change search
	// (This is called on the JavaFX Application Thread)
	public void setSearch(Search s) {
		results.clear();
		search = s;
	}

	@FXML
	private void initialize() {
		circles = loadingBox.getChildren();
		circles.forEach(circle -> circle.setOpacity(0));
	}


	private volatile boolean stopLoading = false;
	private volatile boolean stopBlinking = false;

	private final Service<Void> searching = new SearchService();
	private final Service<Void> loading = new LoadService();
	private final Service<Void> blinking = new BlinkService();


	// (This is called on the JavaFX Application Thread)
	public void startSearch() {
		resultsBtn.setDisable(true);
		resultsBtn.setText("Not yet...");

		stopLoading = false;

		loading.restart();
		searching.restart();
	}

	@FXML
	void openResults(ActionEvent event) {
		stopBlinking = true;
		h.getMain().moveToResultsScreen(results);
	}

	// Helper Service classes
	private class SearchService extends Service<Void> {
		SearchService() {
			super();

			setOnSucceeded(event -> {
				stopLoading = true;
				stopBlinking = false;

				resultsBtn.setDisable(false);
				resultsBtn.setText("Open Results");

				blinking.restart();

				if (Settings.playSound())
					SoundFactory.ping();
			});

			// if something goes wrong: print exception & terminate
			exceptionProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal != null) {
					Exception ex = (Exception) newVal;
					ex.printStackTrace();
					Platform.exit();
					System.exit(0);
				}
			});
		}

		@Override
		protected Task<Void> createTask() {
			return new Task<>() {
				@Override
				protected Void call() {
					results.addAll(search.search());
					return null;
				}
			};
		}
	}

	private class BlinkService extends Service<Void> {
		BlinkService() {
			super();
		}

		@Override
		protected Task<Void> createTask() {
			return new Task<>() {

				private boolean dim = true;	// start by dimming circles
				private double opacity = 0.8;
				private double step = 0.4;

				@Override
				protected Void call() throws Exception {
					// Start by making all circles visible
					circles.forEach(c -> c.setOpacity(1));

					while (!stopBlinking) {
						Platform.runLater(() -> circles.forEach(c -> c.setOpacity(opacity)));

						// If dimming, decrement opacity
						if (dim)
							opacity -= step;
						// If lighting up, increment opacity
						else
							opacity += step;

						// Stop dimming/stop lighting up
						if (opacity > 1 || opacity < 0)
							dim = !dim;

						sleep(70);
					}

					return null;
				}
			};
		}
	}

	private class LoadService extends Service<Void> {
		LoadService() {
			super();
		}

		/*
		 * 11 circles
		 *
		 * peak starts at middle index (5),
		 * 'peakflat' is to be 3 circles wide (+- 1 from peak index),
		 * 2 after peakflat (left&right) are to be fading out,
		 * rest invisible
		 *
		 * All ternary operators below are to solve over/underflow
		 */
		@Override
		protected Task<Void> createTask() {
			return new Task<>() {

				final int size = circles.size();
				int peak = size / 2;	// start at middle

				@Override
				protected Void call() throws Exception {
					          /* Position, opacity of each circle*/
					HashMap<Integer, Double> circleMap = new HashMap<>();
					
					IntStream.range(0, size)
							 .forEach(i -> circleMap.put(i, 0d));

					final int right = (peak >= size - 1) ? 0 : peak + 1;
					final int left = (peak <= 0) ? size - 1 : peak - 1;

					circleMap.put(peak, 1d);
					circleMap.put(left, 0.9);
					circleMap.put(right, 0.9);

					// left fading circles from peak
					int leftPos = (left <= 0) ? size - 1 : left - 1;
					double opacity = 0.65;
					for (int i = 0; i < 2; i++) {
						circleMap.put(leftPos, opacity);

						opacity -= 0.3;
						leftPos = (leftPos <= 0) ? size - 1 : leftPos - 1;
					}

					//right fading circles from peak
					int rightPos = (right >= size - 1) ? 0 : right + 1;
					opacity = 0.65;
					for (int i = 0; i < 2; i++) {
						circleMap.put(rightPos, opacity);

						opacity -= 0.3;
						rightPos = (rightPos >= size - 1) ? 0 : rightPos + 1;
					}

					Platform.runLater(() -> {
						circleMap.entrySet().forEach(entry ->
							circles.get(entry.getKey()).setOpacity(entry.getValue())
						);
					});
					
					circleMap.clear();

					sleep(130);
					// Move the circle at the end to the start, imitating a cycle
					while (!stopLoading) {
						Platform.runLater(() -> circles.add(0, circles.remove(size-1)));
						sleep(130);
					}

					return null;
				}

			};
		}
	}

}
