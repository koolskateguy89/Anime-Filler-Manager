package afm.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.google.common.base.Strings;

import lombok.Getter;

import afm.utils.Utils;

public class Settings {

	public enum Key {
		SHOW_FACTS(true),
		NAME_ORDER(false),
		PLAY_SOUND(false),
		ALWAYS_ON_TOP(false),
		SKIP_LOADING(false),
		;

		final boolean defaultValue;
		Key(boolean def) {
			this.defaultValue = def;
		}
		public boolean getDefault() {
			return defaultValue;
		}
	}

	// Don't allow this class to be instantiated
	private Settings() { }

	private static final String PREF_NAME;
	static {
		String name = Settings.class.getCanonicalName();
		PREF_NAME = Utils.inJar() ? "jar:"+name : name;
	}

	private static final HashMap<String, Boolean> defaults = new HashMap<>() {{
		for (Key key : Key.values()) {
			put(key.toString(), key.getDefault());
		}
	}};

	// Maybe use EnumMap<Key,Boolean>
	private static final HashMap<String, Boolean> map = new HashMap<>(defaults);


	private static final String DATABASE_KEY = "DATABASE_URLS";
	private static final String SELECTED_KEY = "SELECTED_DATABASE";

	public static final StringProperty selectedDatabaseProperty = new SimpleStringProperty();

	public static String getSelectedDatabase() {
		return selectedDatabaseProperty.getValue();
	}

	@Getter
	private static final Set<String> databaseUrls = new LinkedHashSet<>();

	static {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);
		loadValues(prefs);
		loadDatabaseUrls(prefs);
	}

	private static void loadValues(Preferences prefs) {
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

	/*
	 * Most file systems don't allow '/' in file name so it should be safe to use
	 * '////' as the delimiter
	 */
	private static void loadDatabaseUrls(Preferences prefs) {
		String selected = prefs.get(SELECTED_KEY, "Internal");
		selectedDatabaseProperty.setValue(selected);

		String data = prefs.get(DATABASE_KEY, null);

		if (Strings.isNullOrEmpty(data)) {
			return;
		}

		// https://stackoverflow.com/a/6374137
		String[] urls = data.split(Pattern.quote("////"));
		databaseUrls.addAll(Arrays.asList(urls));
	}

	// OnClose
	public static void save() {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);

		map.forEach(prefs::putBoolean);

		String urls = String.join("////", databaseUrls);
		prefs.put(DATABASE_KEY, urls);

		String selectedDatabase = getSelectedDatabase();
		if (selectedDatabase != null)
			prefs.put(SELECTED_KEY, selectedDatabase);

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
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

	public static void reset() {
		map.putAll(defaults);
	}

}
