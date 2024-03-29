package afm.screens.infowindows;

import static afm.database.AnimeListKt.MyListKt;
import static afm.database.AnimeListKt.ToWatchKt;

import java.util.HashSet;

import javax.annotation.Nullable;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import afm.Main;
import afm.anime.Anime;
import afm.common.NotificationFactory;
import afm.common.utils.Utils;
import afm.user.Settings;
import afm.user.Theme;

// not much point of it being abstract I think...
public abstract class InfoWindow extends Stage {

	private static final HashSet<Stage> openWindows = new HashSet<>();

	private static void addWindow(Stage window) {
		openWindows.add(window);
	}

	private static void removeWindow(Stage window) {
		openWindows.remove(window);
	}

	public static void closeAllOpenWindows() {
		if (openWindows.isEmpty())
			return;

		openWindows.forEach(Stage::close);
		openWindows.clear();
	}

	// apply new theme to all open infoWindows
	public static void applyTheme(@Nullable Theme theme) {
		if (theme == null)
			return;

		for (Stage stage : openWindows) {
			if (stage instanceof InfoWindow iw)
				theme.applyTo(iw.pane);
		}
	}

	protected final Anime anime;

	@FXML
	private Pane pane;

	@FXML
	protected ImageView imageView;

	@FXML
	protected TextArea infoTextArea;

	@FXML
	protected Button urlBtn;

	@FXML
	protected Button browserBtn;

	@FXML
	private TextField totalEpField;

	@FXML
	protected Button openEpsBtn;

	@FXML
	protected Button fillerBtn;

	protected InfoWindow(Anime anime) {
		this.anime = anime;
		addWindow(this);

		getIcons().add(new Image("icons/Info.png"));
		setOnCloseRequest(windowEvent -> closeWindow());
		setAlwaysOnTop(Settings.get(Settings.Key.ALWAYS_ON_TOP));

		//initOwner(Main.getStage()); // this always on top of primaryStage
	}

	protected void afterInitialize() {
		// apply current theme to this
		Settings.themeProperty.get().applyTo(pane);
		requestFocus();

		// put this in the middle of primaryStage
		this.setOnShown(e -> {
			Stage main = Main.getStage();

			final double mainWidth = main.getWidth();
			final double width = this.getWidth();
			double x = main.getX() + mainWidth/2 - width/2;

			final double mainHeight = main.getHeight();
			final double height = this.getHeight();
			double y = main.getY() + mainHeight/2 - height/2;

			setX(x);
			setY(y);
		});

		// if anime != null, totalEpField, fillerBtn, etc. will not be null
		if (anime == null)
			return;

		// If the anime does not have a URL, hide URL & browser button
		if (anime.getUrl() == null) {
			openEpsBtn.setVisible(false);
			urlBtn.setVisible(false);
			browserBtn.setVisible(false);
		}

		int eps = anime.getEpisodes();
		// not finished anime / indeterminable number of episodes
		if (eps == Anime.NOT_FINISHED || eps == 0)
			totalEpField.setText("Not finished");
		else
			totalEpField.setText(Integer.toString(eps));

		if (anime.getFillers().isEmpty())
			fillerBtn.setVisible(false);

		infoTextArea.setText(anime.getSynopsis());

		if (anime.getImageURL() != null)
			imageView.setImage(new Image(anime.getImageURL()));
	}

	// https://stackoverflow.com/a/46395543
	private static String wrapText(String message) {
		StringBuilder sb = new StringBuilder(message);
		for (int i = 0; i < message.length(); i += 200) {
			sb.insert(i, '\n');
		}
		return sb.toString();
	}

	@FXML
	void openEpisode() {
		String name = anime.getName();
		name = Utils.trimDashes(Utils.replaceNonAlphanumericWithDash(name));

		String url;
		if (anime.getCurrEp() == 0)
			url = "https://anime-update.com/anime/" + name;
		else
			url = "https://anime-update.com/watch-online/" + name + "-episode-" + anime.getCurrEp();

		//NotificationFactory.showInfoNotification("Opening browser...");

		Main.getInstance().getHostServices().showDocument(url);
	}

	@FXML
	void openFillers() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(this);
		Utils.wrapAlertText(alert);

		alert.setHeaderText("Your next episode to watch is: " + anime.getNextEpisode());
		alert.setTitle("Filler episodes for: " + anime.getName());

		String fillerString = anime.getFillers().toString();
		fillerString = fillerString.substring(1, fillerString.length()-1);

		alert.setContentText(wrapText(fillerString));

		// use ButtonData.YES to put refresh button on left
		ButtonType refresh = new ButtonType("Refresh", ButtonData.YES);
		alert.getButtonTypes().add(refresh);

		ButtonType result = alert.showAndWait().orElse(null);

		while (result == refresh) {
			anime.findFillers();

			// so database will be updated
			if (MyListKt.contains(anime))
				MyListKt.add(anime);
			else if (ToWatchKt.contains(anime))
				ToWatchKt.add(anime);

			alert.setHeaderText("Your next episode to watch is: " + anime.getNextEpisode());

			fillerString = anime.getFillers().toString();
			fillerString = fillerString.substring(1, fillerString.length()-1);

			alert.setContentText(wrapText(fillerString));

			result = alert.showAndWait().orElse(null);
		}
	}

	@FXML
	void remove() {
		MyListKt.remove(anime);
		ToWatchKt.remove(anime);
		closeWindow();
	}

	@FXML
	void copyURL() {
		Utils.copyToClipboard(anime.getUrl());
		NotificationFactory.showInfoNotification("Copied URL to clipboard!");
	}

	@FXML
	void copyName() {
		Utils.copyToClipboard(anime.getName());
		NotificationFactory.showInfoNotification("Copied name to clipboard!");
	}

	@FXML
	void openBrowser() {
		String url = anime.getUrl();
		if (url == null)
			return;

		Main.getInstance().getHostServices().showDocument(url);
	}

	@FXML
	void closeWindow() {
		if (imageStage != null)
			imageStage.close();

		removeWindow(this);
		close();
	}

	private Stage imageStage;
	@FXML
	void openImage(MouseEvent event) {
		if (imageStage != null) {
			addWindow(imageStage);
			imageStage.show();
			// if window is minimized, un-minimize it
			imageStage.setIconified(false);
			// move window to front
			imageStage.toFront();
			// request focus
			imageStage.requestFocus();
			return;
		}

		final ImageView v = (ImageView)event.getSource();
		final Image img = v.getImage();
		if (img == null)
			return;


		// 'clone' the original ImageView
		ImageView view = new ImageView(img);

		// Create a new Stage (Window) to show the image
		Pane root = new Pane(view);
		Scene scene = new Scene(root);

		imageStage = new Stage();
		imageStage.initOwner(this);
		imageStage.setTitle(anime.getName());
		imageStage.getIcons().add(img);

		// Roughly maintain the aspect ratio of the original image
		imageStage.setMinHeight(img.getHeight() / 2);
		imageStage.setMinWidth(img.getWidth() / 2);

		// The ImageView will always take up the entire window
		view.fitWidthProperty().bind(scene.widthProperty());
		view.fitHeightProperty().bind(scene.heightProperty());

		addWindow(imageStage);
		imageStage.setOnCloseRequest(e -> removeWindow(imageStage));

		imageStage.setScene(scene);
		imageStage.centerOnScreen();
		imageStage.show();
	}
}
