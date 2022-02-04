package afm.screens.version7_toWatch;

import static afm.database.AnimeListKt.MyListKt;
import static afm.database.AnimeListKt.ToWatchKt;

import java.io.IOException;

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
import afm.screens.animefactories.InfoBtnFactory;
import afm.screens.animefactories.MoveBtnFactory;
import afm.screens.animefactories.RemoveBtnFactory;
import afm.screens.infowindows.ToWatchInfoWindow;

public class ToWatchScreen extends Pane {

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

	public ToWatchScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("ToWatchScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}

	public void refreshTable() {
		tableItems.setAll(ToWatchKt.values());
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


		TableColumn<Anime, Button> infoCol = Utils.getInfoCol();
		infoCol.setCellValueFactory(new InfoBtnFactory(ToWatchInfoWindow::open));

		TableColumn<Anime, Button> moveToMyListCol = Utils.getMoveCol("MyList");
		moveToMyListCol.setCellValueFactory(new MoveBtnFactory(ToWatchKt, MyListKt));

		TableColumn<Anime, Button> removeCol = Utils.getRemoveCol();
		removeCol.setCellValueFactory(new RemoveBtnFactory(ToWatchKt));

		TableColumn<Anime, ?> actions = Utils.getActionsCol();
		actions.getColumns().addAll(infoCol, moveToMyListCol, removeCol);
		table.getColumns().add(actions);
	}
}
