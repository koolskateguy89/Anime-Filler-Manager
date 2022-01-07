package afm.user

import javafx.scene.layout.Pane

// atm no Theme is applied to welcomeScreen
@Suppress("unused")
enum class Theme(fileName: String) {
    DEFAULT("application.css"),
    LIGHT("lighttheme.css");

    private val stylesheet = "view/stylesheets/$fileName"

    override fun toString(): String = name[0] + name.substring(1).lowercase()

    fun applyTo(pane: Pane) {
        pane.stylesheets[0] = stylesheet
    }
}
