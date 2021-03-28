package afm.user;

import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import afm.utils.Utils;

public class Settings {

	// Don't allow this class to be instantiated
	private Settings() { }

	private static final String name = Settings.class.getCanonicalName();
	private static final String PREF_NAME = Utils.inJar() ? "jar:"+name : name;

	static {
		loadValues();
	}

	static class Default {
		static final boolean showFacts = true;
		static final boolean nameOrder = false;
		static final boolean playSound = false;
		static final boolean alwaysOnTop = false;
	}

	private static void loadValues() {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);

		showFacts = prefs.getBoolean("showFacts", Default.showFacts);
		nameOrder = prefs.getBoolean("nameOrder", Default.nameOrder);
		playSound = prefs.getBoolean("playSound", Default.playSound);
		alwaysOnTop = prefs.getBoolean("alwaysOnTop", Default.alwaysOnTop);
	}

	// OnClose
	public static void save() {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);

		prefs.putBoolean("showFacts", showFacts);
		prefs.putBoolean("nameOrder", nameOrder);
		prefs.putBoolean("playSound", playSound);
		prefs.putBoolean("alwaysOnTop", alwaysOnTop);

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private static HashMap<String, Boolean> map = new HashMap<>();

	// probably not gonna be used tbh
	public static void put(String key, boolean value) {
		map.put(key, value);
	}

	public static boolean get(String key) {
		Boolean value = map.get(key);
		return value != null ? value : false;   // default to false
	}

	public static void invert(String key) {
		Boolean value = map.get(key);
	}

	private static boolean showFacts;

	private static boolean playSound;

	private static boolean nameOrder;

	private static boolean alwaysOnTop;

	public static boolean showFacts() {
		return showFacts;
	}

	public static void invertShowFacts() {
		showFacts = !showFacts;
	}


	public static boolean nameOrder() {
		return nameOrder;
	}

	public static void invertNameOrder() {
		nameOrder = !nameOrder;
	}


	public static boolean playSound() {
		return playSound;
	}

	public static void invertPlaySound() {
		playSound = !playSound;
	}


	public static boolean alwaysOnTop() {
		return alwaysOnTop;
	}

	public static void invertAlwaysOnTop() {
		alwaysOnTop = !alwaysOnTop;
	}

}
