@file:JvmName("Utils")
@file:JvmMultifileClass

package afm.utils

import javafx.css.Styleable
import javafx.scene.control.Alert
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.util.Callback


fun TableColumn<*, *>.topCenterColumnAlignment() {
    style += "; -fx-alignment: TOP-CENTER"
}

fun <T> TableColumn<T, String>.wrapColText() {
    // Need to use a custom cell factory in order to be able to make it wrap text
    cellFactory = Callback {
        object : TableCell<T, String?>() {
            val graphicText = Text()

            override fun updateItem(item: String?, empty: Boolean) {
                super.updateItem(item, empty)
                text = null

                if (empty) {
                    graphic = null
                } else {
                    graphicText.wrappingWidthProperty().bind(it.widthProperty())
                    graphicText.textProperty().bind(itemProperty())
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
