package afm.screens.animefactories

import afm.anime.Anime
import afm.screens.Menu
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.util.Callback

/*
	 * I could make it lazily load the buttons instead
	 * Or just have one infowindow with 3 buttons and change them as needed
	 */
/* Everything following this is to help ResultsScreen with anime
	 * returned from search results:
	 *
	 * - if an anime is already in ML, both button disable & highlight
	 *   ML btn;
	 * - if an anime is already in TW, both button disable & highlight
	 *   TW btn;
	 * - else, set up both buttons to do on action:
	 *      * add anime to respective location (ML/TW)
	 *      * disable both buttons
	 *
	 * - infoBtn - highlight & become mouse transparent (see below),
	 *             InfoWindow will make it mouse non-transparent and unhighlight it
	 *
	 *
	 *  Neither buttons actually become disabled, just made "mouse transparent"
	 *  which means mouse events called on them are ignored
	 *
	 *  mouseTransparent property is:
	 *  "If true, this node (together with all its children) is completely
	 *   transparent to mouse events. When choosing target for mouse event,
	 *   nodes with mouseTransparent set to true and their subtrees won'
	 *   be taken into account."
	 *
	 *
	 *  Also helps MyListScreen and ToWatchScreen to have appropriately functioning buttons
	 */

private val HIGHLIGHT: List<String> = Menu.SELECTED
private const val SEE_INFO = "See info"

//private typealias Three<T> = Triple<T, T, T>
private data class Three<T>(var a: T?, var b: T?, var c: T?)

// is this the best way to do it?
private val animeBtnMap = mutableMapOf<Anime, Three<Button>>()

class ResultsInfoButtonFactory(val anime: Anime) : Callback<TableColumn.CellDataFeatures<Anime, Button>, ObservableValue<Button>>
{

    private lateinit var btn: Button
    val obs = SimpleObjectProperty<Button>(null)

    override fun call(param: TableColumn.CellDataFeatures<Anime, Button>): ObservableValue<Button> {
        if (!::btn.isInitialized) {
            btn = Button(SEE_INFO)
            // how do get access to all the buttons of an anime :/
            /*
            btn.setOnAction(EventHandler {
                infoBtn.setStyleClass(Anime.HIGHLIGHT)
                ResultInfoWindow.open(anime, infoBtn, myListBtn, toWatchBtn)
            })
             */
            obs.set(btn)
        }

        return obs
    }
}
