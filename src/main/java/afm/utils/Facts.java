package afm.utils;

import java.io.IOException;
import java.util.HashMap;
import javafx.util.Pair;

public final class Facts {

	private Facts() {
		throw new IllegalAccessError("class Facts cannot be instantiated");
	}

	// 15 facts at a time - 15 facts in a file
	// atm only 2 fact files
	// http://randomfactgenerator.net/
	private static HashMap<Integer, String> factMap = new HashMap<>();

	/* Read text file fileX.txt into factMap
	 * 	where X = random number generated between 1 and 5 (2 atm)
	 *
	 * - Called in StartScreen initialiser.
	 */
	public static void init() {
		try {
			int fileNum = Utils.randomNumberClosed(1, 2);

			// path is going to be length 16, which is default anyway
			StringBuilder path = new StringBuilder(16);
			path.append("facts").append('/').append("facts");
			path.append(fileNum).append(".txt");

			String file = Utils.getFileAsString(path.toString());
			String[] lines = file.split(System.lineSeparator());

			for(int i = 0; i < lines.length; i++) {
				factMap.put(i+1, lines[i]);
			}
		} catch (IOException | NullPointerException e) {
			factMap.put(factMap.size()+1, "A Levels are less stressful than the IB Diploma :)");
			e.printStackTrace();
		}
	}

	public static Pair<Integer, String> getRandomFact() {
		int id = Utils.randomNumber(1, factMap.size());
		String fact = factMap.get(id);

		return fact != null ? new Pair<>(id, fact)
							: new Pair<>(0,"A Levels are less stressful than the IB Diploma :)");
	}

}
