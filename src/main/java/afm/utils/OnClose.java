package afm.utils;

import lombok.Getter;

import afm.database.Database;
import afm.user.Settings;

// Singleton
public class OnClose extends Thread {

	@Getter(lazy = true)
	private static final OnClose instance = new OnClose();

	private OnClose() {
		setName("On close thread");
		setDaemon(false);
	}

	// Save Settings preferences & Save MyList & ToWatch into database
	@Override
	public void run() {
		Settings.save();
		Database.saveAll();
	}

}
