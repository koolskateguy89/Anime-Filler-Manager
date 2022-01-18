package afm.common;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Browser {

	private Browser() { }

	static final String OS = System.getProperty("os.name", "unknown").toLowerCase();

	static final boolean isWindows = OS.contains("win");

	static final boolean isUnix = !isWindows && (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));

	static final boolean isMac = !isUnix && (OS.contains("mac") || OS.contains("osx"));

	// see https://stackoverflow.com/a/18509384
	public static boolean open(String url) {
		boolean opened;

		if (isMac) {
			opened = openMac(url);
		} else {
			opened = open0(url);
		}

		if (!opened) {
			if (isWindows) {
				opened = openWindows(url);
			} else if (isUnix) {
				opened = openUnix(url);
			}
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
			Runtime.getRuntime().exec(new String[] {"rundll32", "url.dll", "FileProtocolHandler", url});
			return true;
		} catch (IOException io) {
			try {
				Runtime.getRuntime().exec(new String[] {"start", url});
				return true;
			} catch (IOException io1) {
				return false;
			}
		}
	}

	// If it doesn't work, try opening the other way
	private static boolean openMac(String url) {
		try {
			Runtime.getRuntime().exec(new String[] {"open", url});
			return true;
		} catch (IOException io) {
			return open0(url);
		}
	}

	// only works if xdg-open command exists, which SHOULD be true
	private static boolean openUnix(String url) {
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
