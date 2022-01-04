@file:JvmName("Utils")

package afm.utils

import afm.Main
import afm.anime.Anime
import com.google.common.math.DoubleMath
import javafx.beans.InvalidationListener
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.css.Styleable
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.util.Callback
import java.net.URL
import java.util.Calendar
import java.util.prefs.Preferences


val UtilsJavaClass: Class<*> = object {}.javaClass.enclosingClass
val classLoader: ClassLoader = object {}.javaClass.classLoader

val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

@get:JvmName("inJar")
val inJar: Boolean = run {
    val url: URL? = classLoader.getResource("")
    // for some reason now url is null when in jar
    url == null || url.protocol == "jar"
}

@get:JvmName("isFirstRun")
val isFirstRun: Boolean = run {
    val prefs = Preferences.userRoot().node(UtilsJavaClass.canonicalName)
    val firstRun = prefs.getBoolean("FIRST_RUN", true)
    prefs.putBoolean("FIRST_RUN", false)
    firstRun
}


// this 500ns vs regex 2000ns
fun String.splitByCapitals(): Array<String> {
    val res = ArrayList<String>()

    var start = 0

    for (i in 1 until length) {
        val here = this[i]
        if (here.isUpperCase()) {
            res.add(substring(start, i))
            start = i
        }
    }

    // add remaining
    res.add(substring(start))

    res.removeIf(String?::isNullOrBlank)

    return res.toTypedArray()
}


fun getFxmlUrl(fname: String): URL? = classLoader.getResource("view/$fname.fxml")

fun String.isNumeric(): Boolean = when (toDoubleOrNull()) {
    null -> false
    else -> true
}

fun String.isInteger(): Boolean = when (toIntOrNull()) {
    null -> false
    else -> DoubleMath.isMathematicalInteger(toDouble())
}

fun String?.isStrictInteger(): Boolean {
    if (this.isNullOrEmpty())
        return false

    for (c in this) {
        if (!c.isDigit())
            return false
    }

    return true
}

// Stop user from typing any characters that aren't numeric
fun onlyAllowIntegersListener(): ChangeListener<String?> =
    ChangeListener { obs, oldVal, newVal ->
        /* if (newVal.isNullOrEmpty())
            (obs as StringProperty).value = "0";
        else */
        if (!newVal.isNullOrEmpty() && !newVal.isStrictInteger())
            (obs as StringProperty).value = oldVal
    }


fun sleep(millis: Long) = Thread.sleep(millis)


private fun TableColumn<*, *>.changeStyle(style: String) {
    val newStyle = if (this.style?.isNotEmpty() == true)
        "${this.style}; $style"
    else
        style

    this.style = newStyle
}

fun topCenterColumnAlignment(col: TableColumn<*, *>) {
    col.changeStyle("-fx-alignment: TOP-CENTER")
}

fun centerColumnAlignment(col: TableColumn<*, *>) {
    col.changeStyle("-fx-alignment: CENTER")
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
                    prefHeight = Region.USE_COMPUTED_SIZE
                    graphic = graphicText
                }
            }
        }
    }
}


fun makeButtonProperty(name: String, btn: Button): Property<Button> = object : Property<Button> {
    override fun getName(): String {
        return name
    }

    override fun getValue(): Button {
        return btn
    }

    override fun getBean(): Any? {
        return null
    }

    override fun addListener(listener: ChangeListener<in Button>) {}
    override fun removeListener(listener: ChangeListener<in Button>) {}

    override fun addListener(listener: InvalidationListener) {}
    override fun removeListener(listener: InvalidationListener) {}

    override fun setValue(value: Button) {}

    override fun bind(observable: ObservableValue<out Button?>) {}
    override fun unbind() {}
    override fun isBound(): Boolean {
        return false
    }

    override fun bindBidirectional(other: Property<Button?>) {}
    override fun unbindBidirectional(other: Property<Button?>) {}
}


// make Alert wrap text: https://stackoverflow.com/a/36938061
fun Alert.wrapAlertText() {
    dialogPane.minHeight = Region.USE_PREF_SIZE
}

fun showAndWaitConfAlert(header: String?, content: String?): ButtonType {
    return Alert(AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO).run {
        initOwner(Main.getStage())

        if (header != null)
            headerText = header

        showAndWait().orElse(ButtonType.NO)
    }
}

fun String.copyToClipboard() {
    val content = ClipboardContent()
    content.putString(this)
    Clipboard.getSystemClipboard().setContent(content)
}


private fun generateColumn(name: String): TableColumn<Anime, Button> {
    val col = TableColumn<Anime, Button>(name)
    col.isEditable = false
    col.isSortable = false
    centerColumnAlignment(col)
    return col
}

fun getActionsCol(): TableColumn<Anime, Button> {
    val col = TableColumn<Anime, Button>("Actions")
    col.isEditable = false
    col.isSortable = false
    return col
}

/* For ResultsScreen */

fun getResultInfoCol(): TableColumn<Anime, Button> {
    val infoCol = generateColumn("See Info")
    infoCol.prefWidth = 101.60003662109375
    return infoCol
}

fun getResultCol(name: String): TableColumn<Anime, Button> {
    val col = generateColumn(name)
    col.prefWidth = 76.5
    return col
}

/* For MyListScreen & ToWatchScreen */

fun getInfoCol(): TableColumn<Anime, Button> {
    val infoCol = generateColumn("See Info")
    infoCol.prefWidth = 75.20001220703125
    return infoCol
}

fun getMoveCol(move: String): TableColumn<Anime, Button> {
    val moveCol = generateColumn("Move to $move")
    moveCol.prefWidth = 109.5999755859375
    return moveCol
}

fun getRemoveCol(): TableColumn<Anime, Button> {
    val moveCol = generateColumn("Remove")
    moveCol.prefWidth = 71.2000732421875
    return moveCol
}

fun setStyleClass(node: Styleable, styleClass: List<String>) {
    node.styleClass.setAll(styleClass)
}
