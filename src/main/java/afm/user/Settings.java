package afm.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import afm.utils.Utils;

public final class Settings {

	// Don't allow this class to be instantiated
	private Settings() { }
	
	private static final String DB_URL = Utils.inJar() ? "jdbc:sqlite::resource:databases/config.db"
													   : "jdbc:sqlite:src/main/resources/databases/config.db";

	private static final String QUERY  = "INSERT INTO Config(showFacts, nameOrder, playSound) "
									   + "VALUES (?,?,?)";
	
	static {
		if (Utils.inJar() && Utils.firstRun()) {
			defaultValues();
		} else {
			loadValues();
		}
	}
	
	private static void defaultValues() {
		showFacts = true;
		nameOrder = false; // insertion order
		playSound = false;
	}
	
	private static void loadValues() {
		try (Connection con = DriverManager.getConnection(DB_URL);
				Statement s = con.createStatement()) {

			// There should only be 1 record in the database
			ResultSet rs = s.executeQuery("SELECT * from Config");
			rs.next();
			
			showFacts = rs.getBoolean(1);
			nameOrder = rs.getBoolean(2);
			playSound = rs.getBoolean(3);
			
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void save() {
		try (Connection con = DriverManager.getConnection(DB_URL);
			 Statement s = con.createStatement();
			 PreparedStatement ps = con.prepareStatement(QUERY)) {
			
			// Clear database
			s.executeUpdate("DELETE FROM Config");
			
			// Put updated values into database
			ps.setBoolean(1, showFacts);
			ps.setBoolean(2, nameOrder);
			ps.setBoolean(3, playSound);
			
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	
	private static boolean showFacts;
	
	public static boolean showFacts() {
		return showFacts;
	}
	
	public static void invertShowFacts() {
		showFacts = !showFacts;
	}
	
	
	private static boolean nameOrder;
	
	public static boolean nameOrder() {
		return nameOrder;
	}
	public static boolean insertionOrder() {
		return !nameOrder;
	}
	
	public static void invertNameOrder() {
		nameOrder = !nameOrder;
	}
	
	
	private static boolean playSound;
	
	public static boolean playSound() {
		return playSound;
	}
	
	public static void invertPlaySound() {
		playSound = !playSound;
	}
	
}
