package afm.screens.version5_results;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import afm.anime.Anime;
import afm.common.utils.Utils;

// TODO: change tables to show Name, Studios, [Airing], InfoBtn, addBtnx2
public class ResultsScreen extends Pane {

	@FXML
	private TableView<Anime> table;

	@FXML
	private TableColumn<Anime, String> nameCol;

	@FXML
	private TableColumn<Anime, String> studiosCol;

	@FXML
	private TableColumn<Anime, Integer> yearCol;

	@FXML
	private TableColumn<Anime, String> genreCol;

	private ObservableList<Anime> tableItems;

	public ResultsScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("ResultsScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	public boolean hasResults() {
		return !tableItems.isEmpty();
	}

	public final void setResults(@Nonnull List<Anime> results) {
		tableItems.setAll(results);
	}

	@FXML
	private void initialize() {
		tableItems = table.getItems();

		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		Utils.wrapColText(nameCol);

		studiosCol.setCellValueFactory(new PropertyValueFactory<>("studios"));
		Utils.wrapColText(studiosCol);

		yearCol.setCellValueFactory(new PropertyValueFactory<>("startYear"));
		Utils.topCenterColumnAlignment(yearCol);

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
