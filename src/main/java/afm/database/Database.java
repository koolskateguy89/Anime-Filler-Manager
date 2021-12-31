package afm.database;

import static afm.utils.Utils.firstRun;
import static afm.utils.Utils.inJar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.sqlite.SQLiteDataSource;

import com.google.common.base.Strings;

import afm.Main;
import afm.anime.Anime;
import afm.anime.Anime.AnimeBuilder;
import afm.anime.Genre;
import afm.anime.Season;
import afm.screens.version1_start.StartScreen;
import afm.user.Settings;
import afm.utils.Utils;

/*
 * I was initially using Serialization (EnumSet is Serializable) to store
 * genreSet in the database (as a Blob) but it was simply wayyyy too slow,
 * so instead I store genreString and when reading in I split & parse into
 * Genre. MUCH, MUCH faster.
 *
 * 	- When serializing, it would take in excess of 30 seconds to load 10 anime in
 * 	  (potentially much longer, I didn't bother to wait for them to all load in).
 *
 *
 * Using a HashMap (columnMap) to map column name to column number for performance
 *
 *
 * Optimised saving to ML/TW by keeping record of which anime were added
 * (/changed) and which anime were removed, only making necessary changes to db.
 *
 * Further optimised it by using 1 PreparedStatement for adding to ML/TW - adding
 * Batches, decreasing number of network round trips to 2 (1 to format ps and 1
 * to do update
 */
public class Database {

	// don't allow this to be instantiated
	private Database() { }

	// From SQLiteStudio
	public static final String[] FILE_EXTENSIONS = {
		"*.db",
		"*.db2",
		"*.db3",
		"*.sdb",
		"*.s2db",
		"*.s3db",
		"*.sqlite",
		"*.sqlite2",
		"*.sqlite3",
		"*.sl2",
		"*.sl3",
	};

	private static final String DB_URL;

	static {
		String url = Settings.getSelectedDatabase();

		// fallback on internal database
		if ("Internal".equals(url) || !mayBeValidDatabase(url)) {
			DB_URL = inJar() ? "jdbc:sqlite::resource:databases/animeDB.db"
							 : "jdbc:sqlite:src/main/resources/databases/animeDB.db";
		} else {
			DB_URL = "jdbc:sqlite:" + url;
		}
	}

	private static boolean mayBeValidDatabase(String url) {
		boolean validString = !Strings.isNullOrEmpty(url);

		if (!validString)
			return false;

		try {
			boolean exists = Files.exists(Path.of(url));

			if (!exists)	// go to catch clause
				throw new IllegalArgumentException();

			return true;

		} catch (NullPointerException | IllegalArgumentException e) {
			Settings.selectedDatabaseProperty.setValue("Internal");

			Platform.runLater(() -> {
				String content = """
						Database file does not exist/is not a valid file.
						Falling back on internal database.
						""";

				Alert alert = new Alert(AlertType.ERROR, content);
				alert.initOwner(Main.getStage());
				Utils.wrapAlertText(alert);

				alert.showAndWait();
			});
		}

		return false;
	}

	private static final String MYLIST_INSERT_QUERY  = "INSERT INTO MyList(name, genres, id, "
													 + "studio, seasonString, info, custom, currEp, "
													 + "totalEps, imageURL, fillers) "
													 + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

	private static final String TOWATCH_INSERT_QUERY = "INSERT INTO ToWatch(name, genres, id, "
			 										 + "studio, seasonString, info, custom, currEp, "
			 										 + "totalEps, imageURL, fillers) "
			 										 + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";


	// help performance with getting anime
	private static final Map<String, Integer> COLUMN_MAP = Map.ofEntries(
			Map.entry("name", 1),
			Map.entry("genres", 2),
			Map.entry("id", 3),
			Map.entry("studio", 4),
			Map.entry("seasonString", 5),
			Map.entry("info", 6),
			Map.entry("custom", 7),
			Map.entry("currEp", 8),
			Map.entry("totalEps", 9),
			Map.entry("imageURL", 10),
			Map.entry("fillers", 11)
	);

	// load myList & toWatch contents into runtime linkedHS's
	public static void init(StartScreen.LoadTask task, double start, double end) {
		if (inJar() && firstRun() && Settings.getSelectedDatabase().equals("Internal"))
			clearTables();
		else
			loadAll(task, start, end);

		MyList.init();
		ToWatch.init();
	}

	private static final SQLiteDataSource ds = new SQLiteDataSource() {{
		setUrl(DB_URL);
	}};

	// https://stackoverflow.com/a/1604121
	private static boolean tableExists(Connection con, String tableName) throws SQLException {
		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'".formatted(tableName);

		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
			return rs.next();
		}
	}

	private static final String CREATE_MYLIST = """
			CREATE TABLE MyList (
				name         STRING  PRIMARY KEY ON CONFLICT REPLACE
				                     UNIQUE ON CONFLICT REPLACE
				                     NOT NULL,
				genres       STRING  NOT NULL,
				id           INT     UNIQUE ON CONFLICT REPLACE,
				studio       STRING,
				seasonString STRING,
				info         STRING,
				custom       BOOLEAN NOT NULL,
				currEp       INT     NOT NULL
				                     DEFAULT (0),
				totalEps     INT     NOT NULL
				                     DEFAULT ( -1),
				imageURL     STRING,
				fillers      STRING
			);
			""";

	private static final String CREATE_TOWATCH = """
			CREATE TABLE ToWatch (
				name         STRING  PRIMARY KEY ON CONFLICT REPLACE
				                     UNIQUE ON CONFLICT REPLACE
				                     NOT NULL,
				genres       STRING  NOT NULL,
				id           INT     UNIQUE ON CONFLICT REPLACE,
				studio       STRING,
				seasonString STRING,
				info         STRING,
				custom       BOOLEAN NOT NULL,
				currEp       INT     NOT NULL
				                     DEFAULT (0),
				totalEps     INT     NOT NULL
				                     DEFAULT ( -1),
				imageURL     STRING,
				fillers      STRING
			);
			""";

	// if table already exists: clear it, else create new table
	// current impl. means table won't exist but I'll keep it like this for now
	public static void createNew(String url) throws SQLException {
		SQLiteDataSource ds = new SQLiteDataSource();
		ds.setUrl("jdbc:sqlite:" + url);

		try (Connection con = ds.getConnection(); Statement st = con.createStatement()) {
			if (tableExists(con, "MyList")) {
				st.executeUpdate("DELETE FROM MyList");
			} else {
				st.execute(CREATE_MYLIST);
			}

			if (tableExists(con, "ToWatch")) {
				st.executeUpdate("DELETE FROM ToWatch");
			} else {
				st.execute(CREATE_TOWATCH);
			}
		}
	}

	/*
	 * [If in JAR & first time running, clear database tables]
	 * This is very important for exporting as jar - jar will be effectively 'fresh'
	 * (will be fresh the first time user can interact)
	 *
	 * (Preferences are cleared whenever a jar is built using batch file clearprefs)
	 */
	private static void clearTables() {
		try(Connection con = ds.getConnection(); Statement s = con.createStatement()) {

			s.addBatch("DELETE FROM MyList");
			s.addBatch("DELETE FROM ToWatch");

			s.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void loadAll(StartScreen.LoadTask task, double start, double end) {
		try (Connection con = ds.getConnection(); Statement s = con.createStatement()) {

			s.setQueryTimeout(30);

			double halfDiff = (end - start) / 2;

			// load MyList 'atomically' in case of error loading MyList
			try {
				loadMyList(s, task, start, end - halfDiff);
			} catch (SQLException e) {
				try {
					loadToWatch(s, task, start + halfDiff, end);
				} catch (SQLException e2) {
					e.setNextException(e2);
				}
				throw e;
			}

			loadToWatch(s, task, start + halfDiff, end);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Load contents of MyList table into runtime MyList
	private static void loadMyList(Statement statement, StartScreen.LoadTask task,
								   double start, double end) throws SQLException {
		double diff = end - start;
		double step;
		int size = getTableSize(statement, "MyList");

		if (size == 0) {
			task.incrementProgress(diff);
			return;
		}

		step = diff / size;

		try (ResultSet rs = statement.executeQuery("SELECT * FROM MyList")) {
			while (rs.next()) {
				MyList.addSilent(loadAnimeFromResultSet(rs));
				task.incrementProgress(step);
			}
		}
	}

	// Load contents of ToWatch table into runtime ToWatch
	private static void loadToWatch(Statement statement, StartScreen.LoadTask task,
									double start, double end) throws SQLException {
		double diff = end - start;
		double step;
		int size = getTableSize(statement, "ToWatch");

		if (size == 0) {
			task.incrementProgress(diff);
			return;
		}

		step = diff / size;

		try (ResultSet rs = statement.executeQuery("SELECT * FROM ToWatch")) {
			while (rs.next()) {
				ToWatch.addSilent(loadAnimeFromResultSet(rs));
				task.incrementProgress(step);
			}
		}
	}

	public static void saveAll() {
		try (Connection con = ds.getConnection()){
			con.setAutoCommit(false);

			try {
				saveMyList(con);
			} catch (SQLException e) {
				saveToWatch(con);
				throw e;
			}

			saveToWatch(con);

			con.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Delete all anime that were removed from MyList, from animeDB database.
	 * Save all anime that were added to MyList(runtime) in animeDB database.
	 */
	private static void saveMyList(Connection con) throws SQLException {
		String removed = MyList.getRemovedSQL();

		if (!removed.isEmpty()) {
			try (Statement st = con.createStatement()) {
				String removeSQL = "DELETE FROM MyList WHERE name IN (" + removed + ')';
				st.executeUpdate(removeSQL);
			}
		}

		// add anime in batches
		var added = MyList.getAdded();
		if (!added.isEmpty()) {
			try (PreparedStatement ps = con.prepareStatement(MYLIST_INSERT_QUERY)) {
				int batchSize = 0;
				for (Anime anime : added) {
					anime.prepareStatement(ps);
					ps.addBatch();
					ps.clearParameters();

					// execute 25 batches at a time
					if (++batchSize >= 25) {
						ps.executeBatch();
						batchSize = 0;
					}
				}

				// execute rest of batches
				if (batchSize > 0)
					ps.executeBatch();
			}
		}
	}

	/*
	 * Delete all anime that were removed from ToWatch, from animeDB database.
	 * Save all anime that were added to ToWatch(runtime) in animeDB database.
	 */
	private static void saveToWatch(Connection con) throws SQLException {
		String removed = ToWatch.getRemovedSQL();

		if (!removed.isEmpty()) {
			try (Statement st = con.createStatement()) {
				String removeSQL = "DELETE FROM ToWatch WHERE name IN (" + removed + ')';
				st.executeUpdate(removeSQL);
			}
		}

		// add anime in batches
		var added = ToWatch.getAdded();
		if (!added.isEmpty()) {
			try (PreparedStatement ps = con.prepareStatement(TOWATCH_INSERT_QUERY)) {
				int batchSize = 0;
				for (Anime anime : added) {
					anime.prepareStatement(ps);
					ps.addBatch();
					ps.clearParameters();

					// execute 100 batches at a time
					if (++batchSize >= 100) {
						ps.executeBatch();
						batchSize = 0;
					}
				}

				// execute rest of batches
				if (batchSize > 0)
					ps.executeBatch();
			}
		}
	}

	private static Anime loadAnimeFromResultSet(ResultSet rs) throws SQLException {
		AnimeBuilder builder = Anime.builder();

		builder.setName(rs.getString(COLUMN_MAP.get("name")))
				.setStudio(rs.getString(COLUMN_MAP.get("studio")))
				.setInfo(rs.getString(COLUMN_MAP.get("info")));

		// this returns null if input is null so its fine
		Season season = Season.getSeasonFromToString(rs.getString(COLUMN_MAP.get("seasonString")));
		builder.setSeason(season);

		builder.setCurrEp(rs.getInt(COLUMN_MAP.get("currEp")))
				.setEpisodes(rs.getInt(COLUMN_MAP.get("totalEps")));

		builder.setImageURL(rs.getString(COLUMN_MAP.get("imageURL")));

		builder.setId(rs.getInt(COLUMN_MAP.get("id")))
				.setCustom(rs.getBoolean(COLUMN_MAP.get("custom")));

		String genreString = rs.getString(COLUMN_MAP.get("genres"));
		EnumSet<Genre> genreSet = EnumSet.noneOf(Genre.class);
		for (String g :  genreString.split(", ")) {
			genreSet.add(Genre.parseGenreFromToString(g));
		}
		builder.setGenres(genreSet);

		String fillerString = rs.getString(COLUMN_MAP.get("fillers"));
		if (!Strings.isNullOrEmpty(fillerString)) {
			for (String f : fillerString.split(", "))
				builder.addFillerAsString(f);
		}

		return builder.build();
	}

	private static int getTableSize(Statement s, String tableName) throws SQLException {
		try (ResultSet rs = s.executeQuery("SELECT COUNT(*) from " + tableName)) {
			rs.next();
			return rs.getInt(1);
		}
	}

}
