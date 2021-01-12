package afm;

/*
 * Wrapper around Main to allow Maven-shade-plugin to build
 * (Can't use Main as it extends Application)
 *
 * Also means I don't have to use VM arguments to run it :)
 */
class Launcher {

	public static void main(String[] args) {
		Main.launch0(args);
	}

}
