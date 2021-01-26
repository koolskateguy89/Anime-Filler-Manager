package afm.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import com.google.common.base.Strings;
import com.google.common.math.DoubleMath;

import afm.anime.Anime;
import afm.anime.Genre;

public class Utils {

	private Utils() {}

	private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

	public static int getCurrentYear() {
		return CURRENT_YEAR;
	}

	// this 500ns vs regex 2000ns
	public static String[] splitByCapitals(String input) {
		ArrayList<String> res = new ArrayList<>();

		int start = 0;
		for (int i = 1; i < input.length(); i++) {
			char here = input.charAt(i);
			if (Character.isUpperCase(here)) {
				res.add(input.substring(start, i));
				start = i;
			}
		}

		// add remaining
		res.add(input.substring(start));

		res.removeIf(s -> s == null || s.isBlank());

		return res.toArray(new String[0]);
	}

	private static String currDir;

	// copied from StackOverflow
	public static String getRunningDir() {
		if (currDir == null) {
			try {
				currDir = Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			} catch (URISyntaxException e) {
				e.printStackTrace();
				Platform.exit();
				System.exit(0);
			}
		}

		return currDir;
	}

	private static Boolean inJar;

	public static boolean inJar() {
		if (inJar == null)
			inJar = getRunningDir().endsWith(".jar");

		// Let Java decide how to unbox
		return inJar;
	}

	private static final boolean FIRST_RUN;
	private static final String KEY = "FIRST_RUN";

	static {
		Preferences prefs = Preferences.userRoot().node(Utils.class.getSimpleName());
		// Returns the value associated to the key (param 0), if the value does not exist,
		// it is the first run, so return true (param 1).
		FIRST_RUN = prefs.getBoolean(KEY, true);
		// Sets the value associated to the key such that next time, the call above will return
		// false
		prefs.putBoolean(KEY, false);
	}

	public static boolean firstRun() {
		return FIRST_RUN;
	}

	public static URL getFxmlUrl(String fname) {
		return Utils.class.getClassLoader().getResource("view/" + fname + ".fxml");
	}

	// see https://stackoverflow.com/a/35446009
	public static String getFileAsString(String path) throws IOException {
		try (InputStream in = Utils.class.getClassLoader().getResourceAsStream(path);
				ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			if (in == null)
				return "";

			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			return result.toString(StandardCharsets.UTF_8);
		}
	}

	// inclusive of min & max
	public static int randomNumberClosed(int min, int max) {
		if (min == max)
			return min;
		else if (min > max) {
			return randomNumberClosed(max, min);
		}
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	// exclusive of max
	public static int randomNumber(int min, int max) {
		if (Math.abs(max - min) == 1)
			return Math.min(min, max);
		else if (min >= max) {
			return randomNumber(max, min);
		}
		return ThreadLocalRandom.current().nextInt(min, max);
	}

	public static boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException nfe) {
			try {
				return DoubleMath.isMathematicalInteger(Double.parseDouble(s));
			} catch (NumberFormatException nfe1) {
				return false;
			}
		}
	}

	public static boolean isStrictInteger(final String s) {
		if (Strings.isNullOrEmpty(s))
			return false;

		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// Helper to search for a Genre (given the result of its toString)
	// from Genre.values()
	public static int binarySearch(Genre[] arr, String key) {
		return binarySearch(arr, key, 0, arr.length);
	}

	// Exclusive of end
	/* a special implementation of binary search that ignores case */
	private static int binarySearch(Genre[] arr, String key, int start, int end) {
		if (start > end)
			return -1;

		int mid = start + (end - start) / 2;

		int comp = key.compareToIgnoreCase(arr[mid].toString());

		if (comp == 0)
			return mid;
		else if (comp < 0) { // key is less, search left
			return binarySearch(arr, key, start, mid);
		} else // comp > 0, key is greater, search right
			return binarySearch(arr, key, mid + 1, end);
	}

	@SuppressWarnings("rawtypes")
	private static <T extends Styleable> void changeStyle(T target, String style) {
		String newStyle = target.getStyle();
		if (newStyle != null && !newStyle.isEmpty())
			newStyle = newStyle + "; " + style;
		else
			newStyle = style;

		if (target instanceof Node) {
			((Node)target).setStyle(newStyle);
		} else if (target instanceof  TableColumn) {
			((TableColumn)target).setStyle(newStyle);
		}
	}

	public static void topCenterColumnAlignment(TableColumn<?, ?> col) {
		changeStyle(col, "-fx-alignment: TOP-CENTER");
	}

	public static void centerColumnAlignment(TableColumn<?, ?> col) {
		changeStyle(col, "-fx-alignment: CENTER");
	}

	// Need to use a custom cell factory in order to be able to make it wrap text
	public static <T> void wrapColText(TableColumn<T, String> col) {
		col.setCellFactory(tc -> new TableCell<>() {

			final Text text = new Text();

			@Override
			public void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (empty) {
					setGraphic(null);
					setText(null);
				} else {
					text.wrappingWidthProperty().bind(col.widthProperty());
					text.textProperty().bind(this.itemProperty()); /* (this refers to the cell) */

					setGraphic(text);
					setText(null);
					setPrefHeight(Region.USE_COMPUTED_SIZE);
				}
			}
		});
	}

	//No longer using serialization to save genre (see comments at top of Database)

	/*
	 public static byte[] serialize(EnumSet<Genre> genreSet) throws IOException {
		 // generate a mask of the set so nothing happens to original set
		 EnumSet<Genre> mask = genreSet.clone();

		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 ObjectOutputStream out = new ObjectOutputStream(bos);

		 out.writeObject(mask);
		 out.close();

		 // this is what is to be saved into database blob: byte[] bytes =
		 byte[] bytes = bos.toByteArray();
		 bos.close();

		 return bytes;
	 }

	 @SuppressWarnings("unchecked")
	 public static EnumSet<Genre> deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		 EnumSet<Genre> genres;

		 ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		 ObjectInputStream in = new ObjectInputStream(bis);

		 genres = (EnumSet<Genre>) in.readObject();
		 in.close();
		 bis.close();
		 return genres;
	 }
	*/


	public static Property<Button> makeButtonProperty(String name, Button btn) {
		return new Property<>() {

			@Override public String getName() {
				return name;
			}
			@Override public Button getValue() {
				return btn;
			}

			@Override public Object getBean() { return null; }

			@Override public void addListener(ChangeListener<? super Button> listener) { }
			@Override public void removeListener(ChangeListener<? super Button> listener) { }

			@Override public void addListener(InvalidationListener listener) { }

			@Override public void removeListener(InvalidationListener listener) { }

			@Override public void setValue(Button value) { }

			@Override public void bind(ObservableValue<? extends Button> observable) { }
			@Override public void unbind() { }
			@Override public boolean isBound() { return false; }

			@Override public void bindBidirectional(Property<Button> other) { }
			@Override public void unbindBidirectional(Property<Button> other) { }
		};
	}

	public static ButtonType showAndWaitConfAlert(String content) {
		return showAndWaitConfAlert(null, content);
	}

	// make Alert wrap text: https://stackoverflow.com/a/36938061
	public static ButtonType showAndWaitConfAlert(String header, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);

		if (header != null)
			alert.setHeaderText(header);

		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

		return alert.showAndWait().orElse(ButtonType.NO);
	}

	// Stop user from typing any characters that aren't numeric
	public static ChangeListener<String> onlyAllowIntegersListener() {
		return (obs, oldVal, newVal) -> {
			/* if (newVal == null || newVal.isEmpty())
				((StringProperty) obs).setValue("0");
			else */
			if (!isStrictInteger(newVal))
				((StringProperty) obs).setValue(oldVal);
		};
	}

	public static void copyToClipboard(String s) {
		ClipboardContent content = new ClipboardContent();
		content.putString(s);
		Clipboard.getSystemClipboard().setContent(content);
	}

	private static TableColumn<Anime, Button> generateColumn(String name) {
		TableColumn<Anime, Button> col = new TableColumn<>(name);
		col.setEditable(false);
		col.setSortable(false);
		centerColumnAlignment(col);
		return col;
	}

	public static TableColumn<Anime, Button> getActionsCol() {
		TableColumn<Anime, Button> col = new TableColumn<>("Actions");
		col.setEditable(false);
		col.setSortable(false);
		return col;
	}

	/* For ResultsScreen */

	public static TableColumn<Anime, Button> getResultInfoCol() {
		TableColumn<Anime, Button> infoCol = generateColumn("See Info");
		infoCol.setPrefWidth(101.60003662109375);
		return infoCol;
	}

	public static TableColumn<Anime, Button> getResultCol(String name) {
		TableColumn<Anime, Button> col = generateColumn(name);
		col.setPrefWidth(76.5);
		return col;
	}

	/* For MyListScreen & ToWatchScreen */

	public static TableColumn<Anime, Button> getInfoCol() {
		TableColumn<Anime, Button> infoCol = generateColumn("See Info");
		infoCol.setPrefWidth(75.20001220703125);
		return infoCol;
	}

	public static TableColumn<Anime, Button> getMoveCol(String move) {
		TableColumn<Anime, Button> moveCol = generateColumn("Move to " + move);
		moveCol.setPrefWidth(109.5999755859375);
		return moveCol;
	}

	public static TableColumn<Anime, Button> getRemoveCol() {
		TableColumn<Anime, Button> moveCol = generateColumn("Remove");
		moveCol.setPrefWidth(71.2000732421875);
		return moveCol;
	}
}
