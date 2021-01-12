package afm.utils;

import afm.database.Database;
import afm.user.Settings;

// Singleton
public class OnClose extends Thread {

	private static OnClose instance;

	public static OnClose getInstance() {
		if (instance == null) {
			instance = new OnClose();
		}

		return instance;
	}

	private OnClose() {
		setDaemon(false);
		setName("On close thread");
	}

	// Save Settings preferences & Save MyList & ToWAtch into database
	@Override
	public void run() {
		Settings.save();
		Database.saveAll();
	}

}