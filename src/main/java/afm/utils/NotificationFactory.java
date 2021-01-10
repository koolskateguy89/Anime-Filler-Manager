package afm.utils;

import static java.util.Objects.requireNonNullElse;

import org.controlsfx.control.Notifications;

import javafx.application.Platform;
import javafx.util.Duration;

public class NotificationFactory {
	
	private NotificationFactory() {
		throw new IllegalAccessError("Static class NotificationFactory cannot be instantiated");
	}
	
	private static final Duration DEFAULT_DUR = Duration.seconds(10);
	private static final Duration DEFAULT_INFO_DUR = Duration.seconds(3);
	
	public static Notifications getNotification(String title, String text, Duration duration) {
		return Notifications.create()
		 			 		.darkStyle()
		 			 		.title(title)
		 			 		.text(text)
		 			 		.hideAfter(requireNonNullElse(duration, DEFAULT_DUR));
	}
	
	public static void showNotification(String title, String text, Duration duration) {
		Notifications noti = getNotification(title, text, duration);
		Platform.runLater(noti::show);
		Platform.runLater(getNotification(title, text, duration)::show);
	}

	public static void showInfoNotification(String text) {
		showInfoNotification(null, text, DEFAULT_INFO_DUR);
	}
	
	public static void showInfoNotification(String title, String text, Duration duration) {
		Notifications noti = getNotification(title, text, requireNonNullElse(duration, DEFAULT_INFO_DUR));
		Platform.runLater(noti::showInformation);
	}
}
