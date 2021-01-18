package afm.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import com.sun.javafx.PlatformUtil;

public class Browser {

	private Browser() { }

	// see https://stackoverflow.com/a/18509384
	public static boolean open(String url) {
		boolean opened = false;
		
		if (PlatformUtil.isMac()) {
			opened = openMac(url);
		} else {
			opened = open0(url);
		}

		if (!opened) {
			if (PlatformUtil.isWindows()) {
				opened = openWindows(url);
			} else if (PlatformUtil.isLinux()) {
				opened = openLinux(url);
			}
		}

		if (!opened) {
			Alert a = new Alert(AlertType.ERROR, "Browser could not be opened.");
			a.showAndWait();
		}

		return opened;
	}

	// Usually works in Windows & Linux
	private static boolean open0(String url) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(url));
				return true;
			} catch (IOException | URISyntaxException e) {
				return false;
			}
		}
		return false;
	}

	private static boolean openWindows(String url) {
		try {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			return true;
		} catch (IOException io) {
			try {
				Runtime.getRuntime().exec("start " + url);
				return true;
			} catch (IOException io1) {
				return false;
			}
		}
	}

	// If it doesn't work, try opening the other way
	// this might work for Unix, I'm not sure
	private static boolean openMac(String url) {
		try {
			Runtime.getRuntime().exec("open " + url);
			return true;
		} catch (IOException io) {
			return open0(url);
		}
	}

	// see https://stackoverflow.com/a/37926900
	// also maybe https://stackoverflow.com/a/28807079
	private static boolean openLinux(String url) {
		try (InputStream is = Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream()){
			if (is.read() != -1) {
				Runtime.getRuntime().exec(new String[] { "xdg-open", url });
				return true;
			}
		} catch (IOException ignored) {
		}
		return false;
	}

}
