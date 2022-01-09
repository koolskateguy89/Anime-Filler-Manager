package afm.screens.version7_toWatch;

import static afm.database.DelegatesKt.ToWatchKt;

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
import afm.anime.Season;
import afm.common.utils.Utils;

public class ToWatchScreen extends Pane {

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

	private ObservableList<Anime> tableItems;

	public ToWatchScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(afm.common.utils.Utils.getFxmlUrl("ToWatchScreen"));
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
		afm.common.utils.Utils.wrapColText(nameCol);

		studioCol.setCellValueFactory(new PropertyValueFactory<>("studio"));
		afm.common.utils.Utils.wrapColText(studioCol);

		seasonCol.setCellValueFactory(new PropertyValueFactory<>("season"));
		afm.common.utils.Utils.topCenterColumnAlignment(seasonCol);

		genreCol.setCellValueFactory(new PropertyValueFactory<>("genreString"));
		Utils.wrapColText(genreCol);


		TableColumn<Anime, Button> actions = afm.common.utils.Utils.getActionsCol();
		table.getColumns().add(actions);

		TableColumn<Anime, Button> infoCol = afm.common.utils.Utils.getInfoCol();
		TableColumn<Anime, Button> moveToMyListCol = afm.common.utils.Utils.getMoveCol("MyList");
		TableColumn<Anime, Button> removeCol = afm.common.utils.Utils.getRemoveCol();
		actions.getColumns().addAll(infoCol, moveToMyListCol, removeCol);

		infoCol.setCellValueFactory(new PropertyValueFactory<>("toWatchInfo"));

		moveToMyListCol.setCellValueFactory(new PropertyValueFactory<>("moveToMyList"));

		removeCol.setCellValueFactory(new PropertyValueFactory<>("toWatchRemove"));
	}
}
