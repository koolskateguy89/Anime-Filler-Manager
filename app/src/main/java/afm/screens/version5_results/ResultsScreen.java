package afm.screens.version5_results;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import afm.anime.Anime;
import afm.anime.Season;
import afm.common.utils.Utils;

public class ResultsScreen extends Pane {

	@FXML
	private TableView<Anime> table;

	@FXML
	private TableColumn<Anime, String> nameCol;

	@FXML
	private TableColumn<Anime, String> studioCol;

	@FXML
	private TableColumn<Anime, Season> seasonCol;

	@FXML
	private TableColumn<Anime, String> genreCol;

	private List<Anime> tableItems;

	public ResultsScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(afm.common.utils.Utils.getFxmlUrl("ResultsScreen"));
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

	@FXML
	private void initialize() {
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		afm.common.utils.Utils.wrapColText(nameCol);

		studioCol.setCellValueFactory(new PropertyValueFactory<>("studio"));
		afm.common.utils.Utils.wrapColText(studioCol);

		seasonCol.setCellValueFactory(new PropertyValueFactory<>("season"));

		genreCol.setCellValueFactory(new PropertyValueFactory<>("genreString"));
		Utils.wrapColText(genreCol);


		TableColumn<Anime, Button> actions = afm.common.utils.Utils.getActionsCol();
		table.getColumns().add(actions);

		TableColumn<Anime, Button> infoCol = afm.common.utils.Utils.getResultInfoCol();
		TableColumn<Anime, Button> myListCol = afm.common.utils.Utils.getResultCol("MyList");
		TableColumn<Anime, Button> toWatchCol = afm.common.utils.Utils.getResultCol("ToWatch");
		actions.getColumns().addAll(infoCol, myListCol, toWatchCol);

		// calls infoBtnProperty
		infoCol.setCellValueFactory(new PropertyValueFactory<>("infoBtn"));

		// calls myListBtnProperty
		myListCol.setCellValueFactory(new PropertyValueFactory<>("myListBtn"));

		// calls toWatchBtnProperty
		toWatchCol.setCellValueFactory(new PropertyValueFactory<>("toWatchBtn"));
	}
}
