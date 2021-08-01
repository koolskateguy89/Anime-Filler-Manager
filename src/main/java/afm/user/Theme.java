package afm.user;

import javafx.scene.layout.Pane;

import lombok.Getter;

// atm no Theme is applied to welcomeScreen
public enum Theme {
	DEFAULT("application.css"),
	LIGHT("lighttheme.css"),
	;

	@Getter
	final String stylesheet;

	Theme(String fileName) {
		this.stylesheet = "view/stylesheets/" + fileName;
	}

	@Override
	public String toString() {
		String name = this.name();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}

	public void apply(Pane pane) {
		pane.getStylesheets().set(0, stylesheet);
	}
}
