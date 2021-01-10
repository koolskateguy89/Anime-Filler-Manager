package afm.utils;

import afm.Main;
import javafx.stage.Stage;

public class Handler {
	
	Main main;
	
	public Handler(Main main) {
		this.main = main;
	}
	
	public Main getMain() {
		return main;
	}

	public Stage getStage() {
		return main.getStage();
	}
}
