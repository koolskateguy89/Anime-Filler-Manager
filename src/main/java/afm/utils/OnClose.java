package afm.utils;

import afm.database.Database;
import afm.user.Settings;
import lombok.Getter;

// Singleton
public class OnClose extends Thread {

	@Getter(lazy = true)
	private static final OnClose instance = new OnClose();

	static {
		System.out.println(instance);
	}

   /*
	public static OnClose getInstance() {
		if (instance == null) {
			instance = new OnClose();
		}

		return instance;
	}
	*/

	private OnClose() {
		setDaemon(false);
		setName("On close thread");
	}

	// Save Settings preferences & Save MyList & ToWatch into database
	@Override
	public void run() {
		Settings.save();
		Database.saveAll();
	}

}