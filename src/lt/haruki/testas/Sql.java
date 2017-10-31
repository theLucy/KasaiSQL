package lt.haruki.testas;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Sql {
	
	/*== CONNECTION SETTINGS ==================================================*/
	public final String DB_DRIVER_CLASS = "org.firebirdsql.jdbc.FBDriver";
	public final String DB_DRIVER_NAME = "jdbc:firebirdsql";
	public final String DB_HOSTNAME;
	public final String DB_ENCODING;
	public final String DB_NAME;
	public final String DB_USER;
	public final String DB_PASSWORD;
	public final String TABLE_NAME = "days";
	
	public final String DB_URL;
	/*=========================================================================*/
	
	private static Driver d = null;
	private static Connection c = null;
	private static DatabaseMetaData dbMetaData;
	private static Statement s = null;
	private static ResultSetMetaData rsm = null;
	private static ResultSet rs = null;
	
	public static Object[][] content;
	public static Object[] header;
	private static int tWidth = 0;
	private static int tHeight = 0;
	private static int j = 0;
	private static String query = "";
	
	public Sql(final String DB_HOSTNAME, final String DB_ENCODING, final String DB_NAME, final String DB_USER, final String DB_PASSWORD) {
		RegisterSqlDriver();
		this.DB_HOSTNAME = DB_HOSTNAME;
		this.DB_ENCODING = DB_ENCODING;
		this.DB_NAME = DB_NAME;
		this.DB_USER = DB_USER;
		this.DB_PASSWORD = DB_PASSWORD;
		DB_URL = DB_DRIVER_NAME + ":" + DB_HOSTNAME + ":" + DB_NAME + "?lc_ctype=" + DB_ENCODING;
	}
	
	public void DoQuery(String query) {
		try {
			s = c.createStatement ();
			rs = s.executeQuery(query);
			rsm = rs.getMetaData();
			tWidth = rsm.getColumnCount();
			while(rs.next()) {
				tHeight++;
			}
			content = new Object[tHeight][tWidth];
			header = new Object[tWidth];
			rs = dbMetaData.getColumns(null, null, TABLE_NAME, "%");
			for (int i = 0; i < tWidth; i++) {
				header[i] = rsm.getColumnName(i+1);
			}
			rs = s.executeQuery(query);
			System.out.println("-------------------------------------------------------------");
			System.out.format("| %12s | %12s | %12s | %12s |\n", header);
			System.out.println("-------------------------------------------------------------");
			while(rs.next()) {
				for(int i = 0; i < tWidth; i++) {
					content[j][i] = rs.getString(rsm.getColumnName(i+1));
					System.out.format("| %12s ",rs .getString(rsm.getColumnName(i+1)));
				}
				j++;
				System.out.print("|\n");
			}
			System.out.println("-------------------------------------------------------------");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println ("Unable to execute query!");
			ShowSQLException(e);
		}
	}

	public void RegisterSqlDriver() {
		try {
			Class.forName (DB_DRIVER_CLASS);
		} catch (java.lang.ClassNotFoundException e) {
			System.out.println ("Firebird JCA-JDBC driver was not found!");
			System.out.println (e.getMessage());
			return;
		}
	}
	
	public void PrintDriverVersion() {
		try {
			d = DriverManager.getDriver (DB_URL);
			System.out.println ("Firebird JCA-JDBC driver version " +
							d.getMajorVersion () +
							"." +
							d.getMinorVersion () +
							" registered with driver manager.");
		} catch (SQLException e) {
			System.out.println ("Unable to find Firebird JCA-JDBC driver among the registered drivers.");
			ShowSQLException (e);
			return;
		}
	}
	
	public void InitializeSqlConnection() {
		try {
			c = DriverManager.getConnection (DB_URL, DB_USER, DB_PASSWORD);
			System.out.println ("Connection established.");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println ("Unable to establish a connection through the driver manager.");
			ShowSQLException(e);
			return;
		}
	}
	
	public void DisableAutoCommit() {
		try {
			c.setAutoCommit (false);
			System.out.println ("Auto-commit is disabled.");
		} catch (SQLException e) {
			System.out.println ("Unable to disable autocommit.");
			ShowSQLException(e);
			return;
		}
	}
	
	public void ShowSQLException(java.sql.SQLException e) {
		SQLException next = e;
		while (next != null) {
			System.out.println (next.getMessage ());
			System.out.println ("Error Code: " + next.getErrorCode ());
			System.out.println ("SQL State: " + next.getSQLState ());
			next = next.getNextException();
		}
	}
	public void ShowTables() {
		try {
			dbMetaData = c.getMetaData ();

			if (dbMetaData.supportsTransactions ())
				System.out.println ("Transactions are supported.");
			else
				System.out.println ("Transactions are not supported.");

			ResultSet tables = dbMetaData.getTables (null, null, "%", new String[] {"TABLE"});
			System.out.print("Existing tables: ");
			//window.setLabel("Existing tables: ");
			while (tables.next()) {
				System.out.print(tables.getString("TABLE_NAME") + " ");
				//window.extendLabel(tables.getString("TABLE_NAME") + " ");
			}
			System.out.println();
			tables.close ();
		} catch (SQLException e) {
			System.out.println ("Unable to extract database meta data.");
			ShowSQLException(e);
		}
	}
	
	public void endConnection() {
		System.out.println ("Closing database resources and rolling back any changes we made to the database.");

		try {if (rs!=null) rs.close ();} catch(SQLException e) {ShowSQLException(e);}
		
		try {if (s!=null) s.close ();} catch(SQLException e) {ShowSQLException(e);}

		try {if (c!=null) c.rollback ();} catch(SQLException e) {ShowSQLException(e);}
		
		try {if (c!=null) c.close ();} catch(SQLException e) {ShowSQLException(e);}
	}
}