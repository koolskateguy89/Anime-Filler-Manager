@file:JvmName("Utils")
@file:JvmMultifileClass

package afm.common.utils

import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.css.Styleable
import javafx.scene.control.Alert
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.util.Callback
import org.controlsfx.control.CheckComboBox

fun TableColumn<*, *>.topCenterColumnAlignment() {
    style += "; -fx-alignment: TOP-CENTER"
}

fun <T, R> TableColumn<T, R>.wrapColText() {
    // Need to use a custom cell factory in order to be able to make it wrap text
    cellFactory = Callback {
        object : TableCell<T, R?>() {
            val graphicText = Text()

            override fun updateItem(item: R?, empty: Boolean) {
                super.updateItem(item, empty)
                text = null

                if (empty) {
                    graphic = null
                } else {
                    graphicText.wrappingWidthProperty().bind(it.widthProperty())
                    graphicText.textProperty().bind(Bindings.convert(itemProperty()))
                    prefHeight = USE_COMPUTED_SIZE
                    graphic = graphicText
                }
            }
        }
    }
}

// make Alert wrap text: https://stackoverflow.com/a/36938061
fun Alert.wrapAlertText() {
    dialogPane.minHeight = Region.USE_PREF_SIZE
}

fun Styleable.setStyleClass(styleClass: List<String>) {
    this.styleClass.setAll(styleClass)
}

fun CheckComboBox<*>.useTitleAsPromptText() {
    val promptText = title
    checkModel.checkedItems.addListener(ListChangeListener { change ->
        title = if (change.list.isEmpty())
            promptText
        else
            null
    })
}
