@file:JvmName("Utils")

package afm.utils

import afm.Main
import afm.anime.Anime
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

fun String.isNumeric(): Boolean = toDoubleOrNull() != null

fun toIntOrNull(s: String): Int? = s.toIntOrNull()

fun String?.isStrictInteger(): Boolean = !isNullOrEmpty() && all { it.isDigit() }

// Stop user from typing any characters that aren't numeric
fun onlyAllowIntegersListener(): ChangeListener<String?> =
    ChangeListener { obs, oldVal, newVal ->
        /* if (newVal.isNullOrEmpty())
            (obs as StringProperty).value = "0";
        else */
        if (!newVal.isStrictInteger())
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

fun TableColumn<*, *>.topCenterColumnAlignment() = changeStyle("-fx-alignment: TOP-CENTER")

private fun TableColumn<*, *>.centerColumnAlignment() = changeStyle("-fx-alignment: CENTER")

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
    return TableColumn<Anime, Button>(name).apply {
        isEditable = false
        isSortable = false
        centerColumnAlignment()
    }
}

fun getActionsCol(): TableColumn<Anime, Button> {
    return TableColumn<Anime, Button>("Actions").apply {
        isEditable = false
        isSortable = false
    }
}

/* For ResultsScreen */

fun getResultInfoCol(): TableColumn<Anime, Button> {
    return generateColumn("See Info").apply {
        prefWidth = 101.60003662109375
    }
}

fun getResultCol(name: String): TableColumn<Anime, Button> {
    return generateColumn(name).apply {
        prefWidth = 76.5
    }
}

/* For MyListScreen & ToWatchScreen */

fun getInfoCol(): TableColumn<Anime, Button> {
    return generateColumn("See Info").apply {
        prefWidth = 75.20001220703125
    }
}

fun getMoveCol(move: String): TableColumn<Anime, Button> {
    return generateColumn("Move to $move").apply {
        prefWidth = 109.5999755859375
    }
}

fun getRemoveCol(): TableColumn<Anime, Button> {
    return generateColumn("Remove").apply {
        prefWidth = 71.2000732421875
    }
}

fun Styleable.setStyleClass(styleClass: List<String>) {
    this.styleClass.setAll(styleClass)
}
