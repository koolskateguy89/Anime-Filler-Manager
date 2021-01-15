package afm.screens.infowindows;

import java.io.IOException;

import afm.anime.Anime;
import afm.database.MyList;
import afm.database.ToWatch;
import afm.utils.Utils;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Refreshing of MyList table is done in MyList.add(Anime)
public class MyListInfoWindow extends InfoWindow {

	public static void open(Anime a, Button infoBtn) {
		try {
			new MyListInfoWindow(a, infoBtn).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Button infoBtn;

	@FXML
	private ImageView imageView;

	@FXML
	private TextArea infoTextArea;

	@FXML
	private TextField currEpField;

	@FXML
	private TextField totalEpField;

	@FXML
	private Button fillerBtn;

	private MyListInfoWindow(Anime a, Button infoBtn) throws IOException {
		super(a);
		this.infoBtn = infoBtn;

		// load FXML file into this object
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("MyListInfoWindow"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	@FXML
	void initialize() {
		setTitle("Info: "+anime.getName());

		// don't check if null as null strings are made empty strings
		infoTextArea.setText(anime.getInfo());

		imageView.setImage(anime.getImage());

		// anime is not finished (== -1) or episode number is undeterminable (== 0)
		int eps = anime.getEpisodes();
		if (eps < 1)
			totalEpField.setText("Not finished");
		else
			totalEpField.setText(Integer.toString(anime.getEpisodes()));

		if (anime.getFillers().isEmpty())
			fillerBtn.setVisible(false);

		if (anime.getURL() == null)
			browserBtn.setVisible(false);


		currEpField.setText(Integer.toString(anime.getCurrEp()));

		// when user changes what is contained in currEpField, change the
		// current episode of the anime
		// and only allow them to enter numbers in currEpField
		currEpField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (Utils.isInteger(newVal)) {
				int newEps = Integer.parseInt(newVal);
    			anime.setCurrEp(newEps);
    			MyList.update(anime);
    			((StringProperty)obs).setValue(Integer.toString(anime.getCurrEp()));
    			return;
			}

			if (!newVal.equals(""))
				((StringProperty)obs).setValue(oldVal);
		});

		super.afterInitialize();
	}

	@FXML
	void move(ActionEvent event) {
		MyList.remove(anime);
		ToWatch.add(anime);
		closeWindow(null);
    }

	@Override @FXML
	void closeWindow(ActionEvent event) {
		infoBtn.setStyle("");
		infoBtn.setMouseTransparent(false);

		super.closeWindow(event);
	}
}
