package afm.database;

import static afm.utils.Utils.firstRun;
import static afm.utils.Utils.inJar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.Map;

import org.sqlite.SQLiteDataSource;

import com.google.common.base.Strings;

import afm.anime.Anime;
import afm.anime.Anime.AnimeBuilder;
import afm.anime.Genre;
import afm.anime.Season;
import afm.utils.Handler;

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
public final class Database {

	// don't allow this to be instantiated
	private Database() { }

	private static final String DB_URL = inJar() ? "jdbc:sqlite::resource:databases/animeDB.db"
											     : "jdbc:sqlite:src/main/resources/databases/animeDB.db";

	private static final String MYLIST_INSERT_QUERY  = "INSERT INTO MyList(name, genres, id, "
													 + "studio, seasonString, info, custom, currEp, "
													 + "totalEps, imageURL, fillers) "
													 + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

	private static final String TOWATCH_INSERT_QUERY = "INSERT INTO ToWatch(name, genres, id, "
			 										 + "studio, seasonString, info, custom, currEp, "
			 										 + "totalEps, imageURL, fillers) "
			 										 + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";


	// help performance with getting anime
	private static final Map<String, Integer> COLUMN_MAP;

	static {
		COLUMN_MAP = Map.ofEntries(Map.entry("name", 1), Map.entry("genres", 2), Map.entry("id", 3),
						Map.entry("studio", 4), Map.entry("seasonString", 5), Map.entry("info", 6),
						Map.entry("custom", 7), Map.entry("currEp", 8), Map.entry("totalEps", 9),
						Map.entry("imageURL", 10), Map.entry("fillers", 11));
	}

	// load myList & toWatch contents into runtime linkedHSs
	public static void init(Handler handler) {
		if (inJar() && firstRun())
			clearTables();
		else
			loadAll();

		MyList.init(handler);
		ToWatch.init(handler);
	}

	private static final SQLiteDataSource ds = new SQLiteDataSource();

	static {
		ds.setUrl(DB_URL);
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
			if (!inJar()) e.printStackTrace();
		}
	}

	private static void loadAll() {
		try (Connection con = ds.getConnection(); Statement s = con.createStatement()) {

			s.setQueryTimeout(30);

			loadMyList(s);

			loadToWatch(s);

		} catch (SQLException e) {
			/*if (!inJar())*/ e.printStackTrace();
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

	// Load contents of MyList table into runtime MyList
	private static void loadMyList(Statement statement) throws SQLException {
		try (ResultSet rs = statement.executeQuery("SELECT * FROM MyList")) {
			while (rs.next()) {
				MyList.addSilent(loadAnimeFromResultSet(rs));
			}
		}
	}

	// Load contents of ToWatch table into runtime ToWatch
	private static void loadToWatch(Statement statement) throws SQLException {
		try (ResultSet rs = statement.executeQuery("SELECT * FROM ToWatch")) {
			while (rs.next()) {
				ToWatch.addSilent(loadAnimeFromResultSet(rs));
			}
		}
	}

	public static void saveAll() {
		try (Connection con = ds.getConnection()){
			con.setAutoCommit(false);

			saveMyList(con);

			saveToWatch(con);

			con.commit();

		} catch (SQLException e) {
			if (!inJar()) e.printStackTrace();
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

}
