package afm.screens.version7_toWatch;

import java.io.IOException;

import afm.anime.Anime;
import afm.anime.Season;
import afm.database.ToWatch;
import afm.utils.Utils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

public class ToWatchScreen extends Pane {

	@FXML private TableView<Anime> table;

	@FXML private TableColumn<Anime, String> nameCol;

	@FXML private TableColumn<Anime, String> studioCol;

	@FXML private TableColumn<Anime, Season> seasonCol;

	@FXML private TableColumn<Anime, String> genreCol;
	
	private ObservableList<Anime> tableItems;

	public ToWatchScreen() throws IOException {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("ToWatchScreen"));
		loader.setController(this);
		loader.setRoot(this);
		loader.load();
	}
	
	public void refreshTable() {
		tableItems.setAll(ToWatch.values());
	}

	@SuppressWarnings("unchecked")
	@FXML private void initialize() {
		tableItems = table.getItems();
		
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		Utils.wrapColText(nameCol);
		
		studioCol.setCellValueFactory(new PropertyValueFactory<>("studio"));
		Utils.wrapColText(studioCol);
		
		seasonCol.setCellValueFactory(new PropertyValueFactory<>("season"));
		Utils.topCenterColumnAlignment(seasonCol);
		
		genreCol.setCellValueFactory(new PropertyValueFactory<>("genreString"));
		Utils.wrapColText(genreCol);
		
		
		TableColumn<Anime, ?> actions = Utils.getActionsCol();
		table.getColumns().add(actions);
		
		TableColumn<Anime, Button> infoCol = Utils.getInfoCol();
		TableColumn<Anime, Button> moveToMyListCol = Utils.getMoveCol("MyList");
		TableColumn<Anime, Button> removeCol = Utils.getRemoveCol();
		actions.getColumns().addAll(infoCol, moveToMyListCol, removeCol);
		
		infoCol.setCellValueFactory(new PropertyValueFactory<>("toWatchInfo"));
		
		moveToMyListCol.setCellValueFactory(new PropertyValueFactory<>("moveToMyList"));
		
		removeCol.setCellValueFactory(new PropertyValueFactory<>("toWatchRemove"));
	}
}
