package afm.utils;

import java.net.URL;

import javafx.scene.media.AudioClip;

public class SoundFactory {
	
	private SoundFactory() { }
	
	private static final AudioClip PING;
	
	static {
		URL pingURL = SoundFactory.class.getClassLoader().getResource("sounds/ping.mp3");
		PING = new AudioClip(pingURL.toString());
		PING.setVolume(.5);
	}
	
	public static void ping() {
		PING.play();
	}
}
