@file:JvmName("Utils")
@file:JvmMultifileClass

package afm.common.utils

import afm.Main
import afm.anime.Anime
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.TableColumn
import javafx.scene.control.TextFormatter
import javafx.util.converter.IntegerStringConverter
import java.net.URL
import java.util.regex.Pattern

fun getFxmlUrl(fname: String): URL? = classLoader.getResource("view/$fname.fxml")


// for java code
fun toIntOrNull(s: String): Int? = s.toIntOrNull()


// Stop user from typing any characters that aren't numeric
val NATURAL_NUMBER_REGEX: Pattern = Pattern.compile("([1-9][0-9]*)?")

private fun intTextFormatterForPattern(pattern: Pattern, defaultValue: Int): TextFormatter<Int> =
    TextFormatter(IntegerStringConverter(), defaultValue) { change: TextFormatter.Change ->
        val newText = change.controlNewText
        if (pattern.matcher(newText).matches())
            change
        else
            null
    }

@JvmOverloads
fun positiveIntOrEmptyFormatter(defaultValue: Int = 0): TextFormatter<Int> = intTextFormatterForPattern(NATURAL_NUMBER_REGEX, defaultValue)


fun sleep(millis: Long) = Thread.sleep(millis)


fun showAndWaitConfAlert(header: String?, content: String?): ButtonType {
    return Alert(AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO).run {
        initOwner(Main.getStage())
        headerText = header
        showAndWait().orElse(ButtonType.NO)
    }
}


private fun generateColumn(name: String, prefWidth: Double): TableColumn<Anime, Button> {
    return TableColumn<Anime, Button>(name).apply {
        isEditable = false
        isSortable = false
        style = "-fx-alignment: CENTER"
        this.prefWidth = prefWidth
    }
}

fun <T> getActionsCol(): TableColumn<Anime, T> {
    return TableColumn<Anime, T>("Actions").apply {
        isEditable = false
        isSortable = false
    }
}

/* For ResultsScreen */

fun getResultInfoCol(): TableColumn<Anime, Button> =
    generateColumn("See Info", 101.60003662109375)

fun getResultCol(name: String): TableColumn<Anime, Button> =
    generateColumn(name, 76.5)


/* For MyListScreen & ToWatchScreen */

fun getInfoCol(): TableColumn<Anime, Button> =
    generateColumn("See Info", 75.20001220703125)

fun getMoveCol(move: String): TableColumn<Anime, Button> =
    generateColumn("Move to $move", 109.5999755859375)

fun getRemoveCol(): TableColumn<Anime, Button> =
    generateColumn("Remove", 71.2000732421875)
