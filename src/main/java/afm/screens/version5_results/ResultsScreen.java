package afm.screens.version5_results;

import java.io.IOException;
import java.util.List;

import afm.anime.Anime;
import afm.anime.Season;
import afm.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

public class ResultsScreen extends Pane {

	@FXML private TableView<Anime> table;

	@FXML private TableColumn<Anime, String> nameCol;

	@FXML private TableColumn<Anime, String> studioCol;

	@FXML private TableColumn<Anime, Season> seasonCol;

	@FXML private TableColumn<Anime, String> genreCol;
	
	private List<Anime> tableItems;

	public ResultsScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("ResultsScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	public final void setResults(List<Anime> results) {
		if (tableItems == null)
			tableItems = table.getItems();
		
		tableItems.clear();
		tableItems.addAll(results);
	}

	@SuppressWarnings("unchecked")
	@FXML private void initialize() {
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		Utils.wrapColText(nameCol);
		
		studioCol.setCellValueFactory(new PropertyValueFactory<>("studio"));
		Utils.wrapColText(studioCol);
		
		seasonCol.setCellValueFactory(new PropertyValueFactory<>("season"));
		
		genreCol.setCellValueFactory(new PropertyValueFactory<>("genreString"));
		Utils.wrapColText(genreCol);
		
		
		TableColumn<Anime, ?> actions = Utils.getActionsCol();
		table.getColumns().add(actions);
		
		TableColumn<Anime, Button> infoCol = Utils.getResultInfoCol();
		TableColumn<Anime, Button> myListCol = Utils.getResultCol("MyList");
		TableColumn<Anime, Button> toWatchCol = Utils.getResultCol("ToWatch");
		actions.getColumns().addAll(infoCol, myListCol, toWatchCol);
		
		// calls infoBtnProperty
		infoCol.setCellValueFactory(new PropertyValueFactory<>("infoBtn"));
		
		// calls myListBtnProperty
		myListCol.setCellValueFactory(new PropertyValueFactory<>("myListBtn"));
		
		// calls toWatchBtnProperty
		toWatchCol.setCellValueFactory(new PropertyValueFactory<>("toWatchBtn"));
	}
}
