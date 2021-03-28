package afm.user;

import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import afm.utils.Utils;

public class Settings {

	public enum Key {
		SHOW_FACTS(true),
		NAME_ORDER(false),
		PLAY_SOUND(false),
		ALWAYS_ON_TOP(false),
		;

		final boolean def;
		Key(boolean def) {
			this.def = def;
		}
		public boolean getDefault() {
			return def;
		}
	}

	// Don't allow this class to be instantiated
	private Settings() { }

	private static final String name = Settings.class.getCanonicalName();
	private static final String PREF_NAME = Utils.inJar() ? "jar:"+name : name;

	private static final HashMap<String, Boolean> defaults = new HashMap<>() {{
		for (Key key : Key.values()) {
			put(key.toString(), key.getDefault());
		}
	}};

	// now that I think about it, I could start this Map out as the default map
	// similar to how it is now, but there's no need for the `defaults` map
	// also maybe use EnumMap<Key,Boolean>
	private static final HashMap<String, Boolean> map = new HashMap<>() {{
		putAll(defaults);
	}};

	static {
		loadValues();
	}

	private static void loadValues() {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);

		try {
			String[] keys = prefs.keys();
			for (String key : keys) {
				Boolean def = defaults.get(key);
				if (def == null)
					continue;

				boolean value = prefs.getBoolean(key, def);
				map.put(key, value);
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

	}

	// OnClose
	public static void save() {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);

		map.forEach(prefs::putBoolean);

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	// probably not gonna be used tbh
	public static void put(String key, boolean value) {
		map.put(key, value);
	}

	public static boolean get(Key key) {
		return get(key.toString());
	}

	public static boolean get(String key) {
		Boolean value = map.get(key);
		return value != null ? value : false;   // default to false if key doesn't exist
	}

	public static void invert(Key key) {
		invert(key.toString());
	}

	public static void invert(String key) {
		Boolean value = map.get(key);
		if (value != null)
			map.put(key, !value);
	}

}
