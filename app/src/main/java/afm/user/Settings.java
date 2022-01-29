package afm.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import lombok.Getter;

import afm.common.utils.Utils;

/*
 * TODO: Map<String, String> for String settings (database, selected db etc.)
 *  basically make getting a String setting shorter (if I add more in future it won't clutter this)
 */
public class Settings {

	// Don't allow this class to be instantiated
	private Settings() { }

	private enum PrefKey {
		DATABASE("DATABASE_URLS"),
		SELECTED_DB("SELECTED_DATABASE"),
		OPACITY,
		INACTIVE_OPACITY,
		THEME,
		;

		final String key;
		PrefKey() {
			this.key = this.name();
		}
		PrefKey(String key) {
			this.key = key;
		}
	}

	public enum Key {
		SHOW_FACTS(true),
		NAME_ORDER(false),
		PLAY_SOUND(false),
		ALWAYS_ON_TOP(false),
		SKIP_LOADING(false),
		;

		final boolean defaultValue;

		Key(boolean def) {
			defaultValue = def;
		}

		public boolean getDefault() {
			return defaultValue;
		}
	}

	private static final String PREF_NAME = (Utils.inJar() ? "jar:" : "") + Settings.class.getCanonicalName();

	private static final Map<String, Boolean> defaults = Collections.unmodifiableMap(new HashMap<>() {{
		for (Key key : Key.values()) {
			put(key.toString(), key.getDefault());
		}
	}});

	// Maybe use EnumMap<Key,Boolean>
	private static final HashMap<String, Boolean> map = new HashMap<>(defaults);

	public static final StringProperty selectedDatabaseProperty = new SimpleStringProperty();

	public static @Nonnull String getSelectedDatabase() {
		return selectedDatabaseProperty.getValue();
	}

	@Getter
	private static final Set<String> databaseUrls = new LinkedHashSet<>();

	public static final DoubleProperty opacityProperty = new SimpleDoubleProperty();
	public static final DoubleProperty inactiveOpacityProperty = new SimpleDoubleProperty();

	public static final ObjectProperty<Theme> themeProperty = new SimpleObjectProperty<>();

	static {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);
		loadBooleans(prefs);
		loadDatabaseUrls(prefs);
		loadOpacities(prefs);
		loadRest(prefs);
	}

	private static void loadBooleans(Preferences prefs) {
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
	 * Most file systems don't allow '/' in file name, so it should be safe to use
	 * '////' as the delimiter
	 */
	private static void loadDatabaseUrls(Preferences prefs) {
		String selected = prefs.get(PrefKey.SELECTED_DB.key, "Internal");
		selectedDatabaseProperty.setValue(selected);

		String data = prefs.get(PrefKey.DATABASE.key, "");

		if (data.isEmpty())
			return;

		// pattern quote: https://stackoverflow.com/a/6374137
		String[] urls = data.split(Pattern.quote("////"));
		databaseUrls.addAll(Arrays.asList(urls));
	}

	private static void loadOpacities(Preferences prefs) {
		double opacity = prefs.getDouble(PrefKey.OPACITY.key, 100);
		opacityProperty.set(opacity);

		double inactiveOpacity = prefs.getDouble(PrefKey.INACTIVE_OPACITY.key, 100);
		inactiveOpacityProperty.set(inactiveOpacity);
	}

	private static void loadRest(Preferences prefs) {
		String name = prefs.get(PrefKey.THEME.key, "DEFAULT");
		themeProperty.set(Theme.valueOf(name));
		// anything else to load
	}

	// OnClose
	public static void save() {
		Preferences prefs = Preferences.userRoot().node(PREF_NAME);

		map.forEach(prefs::putBoolean);

		String urls = String.join("////", databaseUrls);
		prefs.put(PrefKey.DATABASE.key, urls);

		String selectedDatabase = getSelectedDatabase();
		if (selectedDatabase != null)
			prefs.put(PrefKey.SELECTED_DB.key, selectedDatabase);

		prefs.putDouble(PrefKey.OPACITY.key, opacityProperty.get());
		prefs.putDouble(PrefKey.INACTIVE_OPACITY.key, inactiveOpacityProperty.get());

		prefs.put(PrefKey.THEME.key, themeProperty.get().name());

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public static boolean get(@Nonnull Key key) {
		return get(key.toString());
	}

	public static boolean get(@Nonnull String key) {
		Boolean value = map.get(key);
		return value != null ? value : false;   // default to false if key doesn't exist
	}

	public static void invert(@Nonnull Key key) {
		invert(key.toString());
	}

	public static void invert(@Nonnull String key) {
		Boolean value = map.get(key);
		if (value != null)
			map.put(key, !value);
	}

	public static void reset() {
		map.putAll(defaults);

		opacityProperty.set(100);
		inactiveOpacityProperty.set(100);

		themeProperty.set(Theme.DEFAULT);
	}

}
