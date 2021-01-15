package afm.screens.infowindows;

import java.util.HashSet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import afm.anime.Anime;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.utils.Browser;
import afm.utils.Handler;
import afm.utils.NotificationFactory;
import afm.utils.Utils;


public abstract class InfoWindow extends Stage {

	public static void init(Handler h) {
		GenreInfoWindow.init(h);
	}

	private static HashSet<Stage> openWindows = new HashSet<>();

	private static void addWindow(Stage w) {
		openWindows.add(w);
	}

	private static void removeWindow(Stage w) {
		openWindows.remove(w);
	}

	public static void closeAllOpenWindows() {
		if (openWindows.isEmpty())
			return;

		openWindows.forEach(Stage::close);
		openWindows.clear();
	}

	protected final Anime anime;

	@FXML protected Button urlBtn;

	@FXML protected Button browserBtn;

	protected InfoWindow(Anime anime) {
		this.anime = anime;
		addWindow(this);
	}

	protected void afterInitialize() {
		// If the anime does not have a URL, hide URL button
		if (urlBtn != null && anime != null && anime.getURL() == null)
			urlBtn.setVisible(false);

		getIcons().add(new Image("icons/InfoIcon.png"));

		requestFocus();
		setOnCloseRequest(event -> closeWindow(null));
		centerOnScreen();
	}

	@FXML void openFillers(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);

		alert.setTitle("Filler episodes for: " + anime.getName());
		alert.setHeaderText(null);	// hide header

		String fillerString = anime.getFillers().toString();
		alert.setContentText(fillerString.substring(1, fillerString.length()-1));

		alert.showAndWait();
	}

	@FXML void remove(ActionEvent event) {
		MyList.remove(anime);
		ToWatch.remove(anime);
		closeWindow(null);
    }

	@FXML void copyURL(ActionEvent event) {
		Utils.copyToClipboard(anime.getURL());
		NotificationFactory.showInfoNotification("Copied URL to clipboard!");
	}

	@FXML void copyName(ActionEvent event) {
		Utils.copyToClipboard(anime.getName());
		NotificationFactory.showInfoNotification("Copied name to clipboard!");
	}

	@FXML void openBrowser(ActionEvent event) {
		String url = anime.getURL();
		if (url == null) return;

		NotificationFactory.showInfoNotification("Opening browser...");
		Browser.open(url);
	}

	@FXML void closeWindow(ActionEvent event) {
		removeWindow(this);
		close();
	}

	@FXML void openImage(MouseEvent event) {
		final ImageView v = (ImageView)event.getSource();
		final Image img = v.getImage();
		if (img == null)
			return;


		// clone the original ImageView
		ImageView view = new ImageView(img);

		// Create a new Stage (Window) to show the image
		Pane root = new Pane(view);
		Scene scene = new Scene(root);

		Stage stage = new Stage();
		stage.setTitle(anime.getName());
		stage.getIcons().add(img);

		// Roughly maintain the aspect ratio of the original image
		stage.setMinHeight(img.getHeight() / 2);
		stage.setMinWidth(img.getWidth() / 2);

		// The ImageView will always take up the entire window
		view.fitWidthProperty().bind(scene.widthProperty());
		view.fitHeightProperty().bind(scene.heightProperty());

		addWindow(stage);
		stage.setOnCloseRequest(e -> removeWindow(stage));

		stage.setScene(scene);
		stage.centerOnScreen();
		stage.show();
	}
}
