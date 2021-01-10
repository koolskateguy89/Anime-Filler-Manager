package afm.screens.infowindows;

import java.io.IOException;

import afm.anime.Anime;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

//Refreshing of ToWatch table is done in ToWatch.add(Anime)
public class ToWatchInfoWindow extends InfoWindow {

	public static void open(Anime a, Button infoBtn) {
		try {
			new ToWatchInfoWindow(a, infoBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Button infoBtn;

	@FXML private ImageView imageView;

	@FXML private TextArea infoTextArea;
	
	@FXML private TextField totalEpField;
	
	@FXML private Button fillerBtn;

	private ToWatchInfoWindow(Anime a, Button infoBtn) throws IOException {
		super(a);
		this.infoBtn = infoBtn;

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("ToWatchInfoWindow"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML void initialize() {
		setTitle("Info: " + anime.getName());

		getIcons().add(new Image("icons/InfoIcon.png"));

		// don't need to check if null as null strings are made empty strings
		infoTextArea.setText(anime.getInfo());

		// don't need to check if null as default is null
		imageView.setImage(anime.getImage());
		
		int eps = anime.getEpisodes();
		if (eps < 1)
			totalEpField.setText("Not finished");
		else
			totalEpField.setText(Integer.toString(anime.getEpisodes()));
		
		if (anime.getFillers().isEmpty())
			fillerBtn.setVisible(false);
		
		if (anime.getURL() == null)
			browserBtn.setVisible(false);
		
		super.afterInitialize();

		requestFocus();
		setOnCloseRequest(event -> closeWindow(null));
		centerOnScreen();
	}

	@FXML void move(ActionEvent event) {
		ToWatch.remove(anime);
		MyList.add(anime);
		closeWindow(null);
	}

	@FXML @Override void closeWindow(ActionEvent event) {
		infoBtn.setStyle("");
		infoBtn.setMouseTransparent(false);

		super.closeWindow(event);
	}
}
