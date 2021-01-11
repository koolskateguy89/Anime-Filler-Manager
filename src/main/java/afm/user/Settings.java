package afm.user;

import java.util.prefs.Preferences;

public final class Settings {

	// Don't allow this class to be instantiated
	private Settings() { }
	
	static {
		loadValues();
	}
	
	private static final boolean DEFAULT_SHOWFACTS = true;
	private static final boolean DEFAULT_NAMEORDER = false;
	private static final boolean DEFAULT_PLAYSOUND = false;
	
	private static void loadValues() {
		Preferences prefs = Preferences.userRoot().node(Settings.class.getSimpleName());
		
		showFacts = prefs.getBoolean("showFacts", DEFAULT_SHOWFACTS);
		nameOrder = prefs.getBoolean("nameOrder", DEFAULT_NAMEORDER);
		playSound = prefs.getBoolean("playSound", DEFAULT_PLAYSOUND);
	}
	
	// OnClose
	public static void save() {
		Preferences prefs = Preferences.userRoot().node(Settings.class.getSimpleName());
		
		prefs.putBoolean("showFacts", showFacts);
		prefs.putBoolean("nameOrder", nameOrder);
		prefs.putBoolean("playSound", playSound);
	}
	
	
	private static boolean showFacts;
	
	private static boolean playSound;
	
	private static boolean nameOrder;
	
	public static boolean showFacts() {
		return showFacts;
	}
	
	public static void invertShowFacts() {
		showFacts = !showFacts;
	}
	
	
	public static boolean nameOrder() {
		return nameOrder;
	}
	public static boolean insertionOrder() {
		return !nameOrder;
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
	
}
