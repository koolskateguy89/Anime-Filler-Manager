@file:JvmName("Utils")

package afm.utils

import afm.Main
import afm.anime.Anime
import com.google.common.base.Strings
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
import java.util.concurrent.ThreadLocalRandom
import java.util.prefs.Preferences
import kotlin.math.abs
import kotlin.math.min


class Utils private constructor() {
    companion object {
        @JvmStatic
        val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

        @JvmStatic
        @get:JvmName("inJar")
        val inJar = "jar" == Utils::class.java.getResource("")!!.protocol

        @JvmStatic
        fun isFirstRun(): Boolean = run {
            val prefs = Preferences.userRoot().node(Utils::class.java.canonicalName)
            val firstRun = prefs.getBoolean("FIRST_RUN", true)
            prefs.putBoolean("FIRST_RUN", false)
            firstRun
        }


        // this 500ns vs regex 2000ns
        @JvmStatic
        fun splitByCapitals(input: String): Array<String> {
            val res = ArrayList<String>()

            var start = 0

            for (i in 1 until input.length) {
                val here = input[i]
                if (Character.isUpperCase(here)) {
                    res.add(input.substring(start, i))
                    start = i
                }
            }

            // add remaining
            res.add(input.substring(start))

            res.removeIf(String?::isNullOrBlank)

            return res.toTypedArray()
        }


        private val classLoader: ClassLoader = Utils::class.java.classLoader

        @JvmStatic
        fun getFxmlUrl(fname: String): URL? = classLoader.getResource("view/$fname.fxml")


        // inclusive of min & max
        @JvmStatic
        fun randomNumberClosed(minVal: Int, maxVal: Int): Int {
            if (minVal == maxVal)
                return minVal
            else if (minVal > maxVal)
                return randomNumberClosed(maxVal, minVal)

            return ThreadLocalRandom.current().nextInt(minVal, maxVal + 1)
        }

        // exclusive of max
        @JvmStatic
        fun randomNumber(minVal: Int, maxVal: Int): Int {
            if (abs(maxVal - minVal) == 1)
                return min(minVal, maxVal)
            else if (minVal >= maxVal)
                return randomNumber(maxVal, minVal)

            return ThreadLocalRandom.current().nextInt(minVal, maxVal)
        }


        @JvmStatic
        fun String.isNumeric(): Boolean = when (toDoubleOrNull()) {
            null -> false
            else -> true
        }

        @JvmStatic
        fun String.isInteger(): Boolean = when (toIntOrNull()) {
            null -> false
            else -> DoubleMath.isMathematicalInteger(toDouble())
        }

        @JvmStatic
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
        @JvmStatic
        fun onlyAllowIntegersListener(): ChangeListener<String?> =
            ChangeListener { obs: ObservableValue<out String?>, oldVal: String?, newVal: String? ->
                /* if (newVal.isNullOrEmpty())
                    (obs as StringProperty).value = "0";
                else */
                if (!Strings.isNullOrEmpty(newVal) && !newVal.isStrictInteger())
                    (obs as StringProperty).value = oldVal
            }


        @JvmStatic
        fun sleep(millis: Long) = Thread.sleep(millis)


        private fun TableColumn<*, *>.changeStyle(style: String) {
            val newStyle = if (this.style?.isNotEmpty() == true)
                "${this.style}; $style"
            else
                style

            this.style = newStyle
        }

        @JvmStatic
        fun topCenterColumnAlignment(col: TableColumn<*, *>) {
            col.changeStyle("-fx-alignment: TOP-CENTER")
        }

        @JvmStatic
        fun centerColumnAlignment(col: TableColumn<*, *>) {
            col.changeStyle("-fx-alignment: CENTER")
        }


        // Need to use a custom cell factory in order to be able to make it wrap text
        @JvmStatic
        fun <T> TableColumn<T, String>.wrapColText() {
            class WrappingTableCell : TableCell<T, String?>() {
                val text = Text()

                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    setText(null)
                    if (empty) {
                        setGraphic(null)
                    } else {
                        text.wrappingWidthProperty().bind(this@wrapColText.widthProperty())
                        text.textProperty().bind(this@WrappingTableCell.itemProperty())
                        graphic = text
                        setPrefHeight(USE_COMPUTED_SIZE)
                    }
                }
            }
            cellFactory = Callback { WrappingTableCell() }
        }

        @JvmStatic
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
        @JvmStatic
        fun wrapAlertText(alert: Alert) {
            alert.dialogPane.minHeight = Region.USE_PREF_SIZE
        }

        @JvmStatic
        fun showAndWaitConfAlert(header: String?, content: String?): ButtonType? {
            val alert = Alert(AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO)
            alert.initOwner(Main.getStage())

            if (header != null)
                alert.headerText = header

            wrapAlertText(alert)
            return alert.showAndWait().orElse(ButtonType.NO)
        }

        @JvmStatic
        fun copyToClipboard(s: String?) {
            val content = ClipboardContent()
            content.putString(s)
            Clipboard.getSystemClipboard().setContent(content)
        }

        private fun generateColumn(name: String): TableColumn<Anime, Button> {
            val col = TableColumn<Anime, Button>(name)
            col.isEditable = false
            col.isSortable = false
            centerColumnAlignment(col)
            return col
        }

        @JvmStatic
        fun getActionsCol(): TableColumn<Anime, Button> {
            val col = TableColumn<Anime, Button>("Actions")
            col.isEditable = false
            col.isSortable = false
            return col
        }

        /* For ResultsScreen */

        @JvmStatic
        fun getResultInfoCol(): TableColumn<Anime, Button> {
            val infoCol = generateColumn("See Info")
            infoCol.prefWidth = 101.60003662109375
            return infoCol
        }

        @JvmStatic
        fun getResultCol(name: String?): TableColumn<Anime, Button> {
            val col = generateColumn(name!!)
            col.prefWidth = 76.5
            return col
        }

        /* For MyListScreen & ToWatchScreen */

        @JvmStatic
        fun getInfoCol(): TableColumn<Anime, Button> {
            val infoCol = generateColumn("See Info")
            infoCol.prefWidth = 75.20001220703125
            return infoCol
        }

        @JvmStatic
        fun getMoveCol(move: String): TableColumn<Anime, Button> {
            val moveCol = generateColumn("Move to $move")
            moveCol.prefWidth = 109.5999755859375
            return moveCol
        }

        @JvmStatic
        fun getRemoveCol(): TableColumn<Anime, Button> {
            val moveCol = generateColumn("Remove")
            moveCol.prefWidth = 71.2000732421875
            return moveCol
        }

        @JvmStatic
        fun setStyleClass(node: Styleable, styleclass: List<String>) {
            node.styleClass.setAll(styleclass)
        }

    }
}
