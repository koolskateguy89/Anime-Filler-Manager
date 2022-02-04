package afm.screens.animefactories

import afm.anime.Anime
import afm.common.utils.setStyleClass
import afm.database.AnimeList
import afm.database.MyListKt
import afm.database.ToWatchKt
import afm.screens.Menu
import afm.screens.infowindows.ResultInfoWindow
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.util.Callback
import java.util.function.BiConsumer

/* This is to help ResultsScreen with anime
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

private typealias Factory = Callback<CellDataFeatures<Anime, Button>, ObservableValue<Button>>

private typealias ButtonProp = ReadOnlyObjectWrapper<Button>

object ResultsInfoBtnFactory : Factory {
    override fun call(param: CellDataFeatures<Anime, Button>): ObservableValue<Button> {
        val anime = param.value

        val actionsCol = param.tableView.columns.last()
        val myListCol = actionsCol.columns[1]
        val toWatchCol = actionsCol.columns[2]

        val myListBtn: Button = myListCol.getCellData(anime) as Button
        val toWatchBtn: Button = toWatchCol.getCellData(anime) as Button

        val infoBtn = Button("See info").apply {
            onAction = EventHandler {
                setStyleClass(HIGHLIGHT)
                //val myListBtn = Button()
                //val toWatchBtn = Button()
                ResultInfoWindow.open(anime, this, myListBtn, toWatchBtn)
            }
        }

        return ReadOnlyObjectWrapper(infoBtn)
    }
}

object ResultsAddBtns {

    private val btnMap = mutableMapOf<Anime, Pair<ButtonProp, ButtonProp>>()

    @JvmField
    val myListFactory = Factory { param ->
        val anime = param.value

        if (anime !in btnMap)
            btnMap[anime] = makeBtns(anime)
        else
            updateBtns(anime)

        btnMap.getValue(anime).component1()
    }

    @JvmField
    val toWatchFactory = Factory { param ->
        val anime = param.value

        if (anime !in btnMap)
            btnMap[anime] = makeBtns(anime)
        else
            updateBtns(anime)

        btnMap.getValue(anime).component2()
    }

    private fun makeBtns(anime: Anime): Pair<ButtonProp, ButtonProp> {
        val myListBtn = Button("Add")
        val toWatchBtn = Button("Add")

        updateBtns(anime, myListBtn, toWatchBtn)

        return ReadOnlyObjectWrapper(myListBtn) to ReadOnlyObjectWrapper(toWatchBtn)
    }

    private fun updateBtns(anime: Anime, myListBtn: Button, toWatchBtn: Button) {
        // default values
        arrayOf(myListBtn, toWatchBtn).forEach {
            it.setStyleClass("button")
            it.isMouseTransparent = false
            it.onAction = null
        }

        when (anime) {
            in MyListKt -> { // anime is already in MyList
                myListBtn.setStyleClass(HIGHLIGHT)
                myListBtn.isMouseTransparent = true
                toWatchBtn.isMouseTransparent = true
            }
            in ToWatchKt -> { // anime is already in ToWatch
                toWatchBtn.setStyleClass(HIGHLIGHT)
                toWatchBtn.isMouseTransparent = true
                myListBtn.isMouseTransparent = true
            }
            else -> { // anime is in neither MyList nor ToWatch
                myListBtn.onAction = EventHandler {
                    MyListKt.add(anime)
                    myListBtn.setStyleClass(HIGHLIGHT)

                    myListBtn.isMouseTransparent = true
                    toWatchBtn.isMouseTransparent = true
                }

                toWatchBtn.onAction = EventHandler {
                    ToWatchKt.add(anime)
                    toWatchBtn.setStyleClass(HIGHLIGHT)

                    toWatchBtn.isMouseTransparent = true
                    myListBtn.isMouseTransparent = true
                }
            }
        }
    }

    private fun updateBtns(anime: Anime) {
        val pair = btnMap.getValue(anime)
        val myListBtn = pair.component1().value
        val toWatchBtn = pair.component2().value
        updateBtns(anime, myListBtn, toWatchBtn)
    }

}

// (Anime, Button) -> Unit
class InfoBtnFactory(private val infoWindowCallback: BiConsumer<Anime, Button>) : Factory {
    override fun call(param: CellDataFeatures<Anime, Button>): ObservableValue<Button> {
        val anime = param.value
        val infoBtn = Button("See info").apply {
            onAction = EventHandler {
                setStyleClass(HIGHLIGHT)
                infoWindowCallback.accept(anime, this)
            }
        }

        return ReadOnlyObjectWrapper(infoBtn)
    }
}

class MoveBtnFactory(private val from: AnimeList, private val to: AnimeList) : Factory {
    override fun call(param: CellDataFeatures<Anime, Button>): ObservableValue<Button> {
        val anime = param.value
        val moveBtn = Button("Move").apply {
            onAction = EventHandler {
                setStyleClass(HIGHLIGHT)
                from.remove(anime)
                to.add(anime)
            }
        }

        return ReadOnlyObjectWrapper(moveBtn)
    }
}

class RemoveBtnFactory(private val animeList: AnimeList) : Factory {
    override fun call(param: CellDataFeatures<Anime, Button>): ObservableValue<Button> {
        val anime = param.value
        val removeBtn = Button("Remove").apply {
            onAction = EventHandler {
                animeList.remove(anime)
            }
        }

        return ReadOnlyObjectWrapper(removeBtn)
    }
}
