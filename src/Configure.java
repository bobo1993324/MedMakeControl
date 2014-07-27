import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class Configure {
	public static boolean runNoisy = false;
	public static String COMM40;
	public static String COMMT;
	public static ArrayList<String> COMMList = new ArrayList<String>();
	public static String currentFilePath;
	private static String dbPath = "40ports.sql";
	private static String tableName = "config";
	static int graphLength=90;

	public static String getCurrentFileParent() {
		File f = new File(currentFilePath);
		return f.getParent();
	}

	public static String getCurrentFileName() {
		File f = new File(currentFilePath);
		return f.getName();
	}

	public static void main(String[] args) {
		try {
			readAll();
			setCurrentFilePath("aaaaaaasdsdf/sdfsd");
			setCOMM40("com1");
			readAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readAll() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			System.out.println("Opened database successfully");
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "
					+ Configure.tableName);
			while (rs.next()) {
				String name = rs.getString("name");
				String value = rs.getString("value");
				if (name.equals("cfp")) {
					Configure.currentFilePath = value;
					System.out.println("read currentFilePath = "+value);
				} else if (name.equals("comm40")) {
					boolean find = false;
					for (int i = 0; i < COMMList.size(); i++) {
						if (value.equals(COMMList.get(i))) {
							find = true;
							break;
						}
					}
					if (find){
						Configure.COMM40 = value;
						System.out.println("read COMM40 = "+value);
					}else{
						Configure.COMM40=Configure.COMMList.get(0);
					}
				}else if (!value.equals("") && name.equals("graphLength")) {
					graphLength=new Integer(value);
				}
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (java.sql.SQLException e) {
			System.err.println(e.getErrorCode() + ":" + e.getMessage());
			// missing db
			if (e.getErrorCode() == 0) {
				createTable();
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		
	}

	private static void createTable() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CREATE TABLE " + tableName + " " + "("
					+ " name           TEXT    NOT NULL, "
					+ " value            TEXT   " + " )";
			stmt.executeUpdate(sql);

			System.out.println("Table created successfully");
			sql = "INSERT INTO " + Configure.tableName + " (name, value) "
					+ "VALUES ('cfp', '')";

			stmt.executeUpdate(sql);

			sql = "INSERT INTO " + Configure.tableName + " (name, value) "
					+ "VALUES ('comm40', '')";

			stmt.executeUpdate(sql);
			
			sql = "INSERT INTO " + Configure.tableName + " (name, value) "
					+ "VALUES ('graphLength', '')";
			stmt.executeUpdate(sql);
			
			stmt.close();
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void setCOMM40(String comm40) {
		COMM40 = comm40;
		dbUpdate("comm40", comm40);
	}

	private static void dbUpdate(String name, String value) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "UPDATE " + tableName + " set value = '"+value+"' WHERE name = '"+name +"'";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setCurrentFilePath(String path) {
		currentFilePath = path;
		dbUpdate("cfp", path);
	}

	public static int getGraphLength() {
		return graphLength;
	}

	public static void setGraphLength(int graphLength) {
		Configure.graphLength=graphLength;
		dbUpdate("graphLength", new Integer(graphLength).toString());
	}
}
