package com.gooddata.access.util;

/**
 * Mapping SQL Data type to Java data type
 * 
 * SQL DataType		Java Data Type
 * CHARACTER	 	String
 * VARCHAR	 	String
 * LONGVARCHAR	 	String
 * NUMERIC	 	java.math.BigDecimal
 * DECIMAL	 	java.math.BigDecimal
 * BIT	boolean	Boolean
 * TINYINT	byte	Integer
 * SMALLINT	short	Integer
 * INTEGER	int	Integer
 * BIGINT	long	Long
 * REAL	float	Float
 * FLOAT	double	Double
 * DOUBLE PRECISION	double	Double
 * BINARY	 	byte[]
 * VARBINARY	 	byte[]
 * LONGVARBINARY	 	byte[]
 * DATE	 	java.sql.Date
 * TIME	 	java.sql.Time
 * TIMESTAMP	 	java.sql.Timestamp
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import java.math.BigDecimal;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * A class to encapsulate the input and output settings Keep it simple, no
 * getter/setter here. One input maps to one output
 */
class UserProfile {
	final static String INPUTFILE = "i";
	final static String OUTPUTFILE = "o";
	final static String COMPRESSION = "c";
	String inputFilePath = "";
	String outputFilePath = "";
	// Try to avoid using Java Zip lib since it has bug for
	// Default.BEST_COMPRESSION
	// Apply compression to the output result to save space
	boolean compression = false;
	ArrayList<String> sqlList = new ArrayList<String>();

	UserProfile() {

	}

	UserProfile(String inputFilename, String outputFilename)
			throws FileNotFoundException {
		if (!new java.io.File(inputFilename).exists()) {
			throw new java.io.FileNotFoundException(inputFilename
					+ " does not exist");
		}
		inputFilePath = inputFilename;
		outputFilePath = outputFilename;

		// Delete exiting output files if exist
		if (new java.io.File(outputFilePath).exists()) {
			System.err.println(outputFilePath + " already exist, overriding");
			new java.io.File(outputFilePath).delete();
		}
	}

	UserProfile(String inputFilename) throws FileNotFoundException {
		if (!new java.io.File(inputFilename).exists()) {
			throw new java.io.FileNotFoundException(inputFilename
					+ " does not exist");
		}
		inputFilePath = inputFilename;
		String newFile = FilenameUtils.removeExtension(inputFilename);

		outputFilePath = newFile + ".csv";

		// Delete exiting output files if exist
		if (new java.io.File(outputFilePath).exists()) {
			System.err.println(outputFilePath + " already exist, overriding");
			new java.io.File(outputFilePath).delete();
		}
	}
}

/**
 * To sore all DB account/password and info
 */
class DBConnectionProfile {
	final static String DBNAME = "dbname";
	final static String USERNAME = "u";
	final static String PASSWORD = "p";
	String username = "";
	String passwd = "";
	String dbname = "";
}

/**
 * To perform special data type conversion when reading from the database. This
 * is a case-by-case scenario and the default here is set to assist Universal
 * McCann project. Update this to your own need if necessary.
 */
class DataTypeMapping {

	/**
	 * Generate the mapping base on column name, you need to understand what are
	 * the naming convention defined for each column to determine its data type
	 * 
	 * @param columnName
	 */

	protected int[] findColIndex(String[] columnName, String columnKeyword) {
		int[] bitmap = new int[columnName.length];
		for (int i = 0; i < columnName.length; i++) {
			bitmap[i] = 0;
			if (columnName[i].toLowerCase()
					.indexOf(columnKeyword.toLowerCase()) >= 0) {
				bitmap[i] = 1;
			}
		}
		return bitmap;
	}

	protected int[] findColIndex(String[] columnName, String[] columnKeyword) {
		int[] bitmap = new int[columnName.length];
		for (int i = 0; i < columnName.length; i++) {
			bitmap[i] = 0;
			for (int j = 0; j < columnKeyword.length; j++) {
				if (columnName[i].toLowerCase().indexOf(
						columnKeyword[j].toLowerCase()) >= 0) {
					bitmap[i] = 1;
				}
			}
		}
		return bitmap;
	}

	protected String get(ResultSet rs, int[] bitmap, int columnIndex)
			throws SQLException {
		if (bitmap[columnIndex - 1] == 0) {
			return rs.getString(columnIndex);
		} else if (bitmap[columnIndex - 1] == 1) {
			Double dec = rs.getDouble(columnIndex);
			if (dec != null) {
				String strDec = "";
				try {
					java.math.BigDecimal bb = new BigDecimal(dec);
					strDec = bb.toPlainString();
				} catch (Exception ex) {
					System.out.println("Converting " + dec
							+ " encounter problems");
				}
				return strDec;
			} else {
				return "";
			}
		} else {
			throw new SQLException("Cannot identify data type for column "
					+ columnIndex);
		}
	}
}

public class QueryDBMain {

	static String qryString = null;
	final static java.util.Queue<UserProfile> qryQueue = new java.util.concurrent.LinkedBlockingQueue<UserProfile>();

	/**
	 * @param args
	 * @throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Current working/running dir is "
				+ System.getProperty("user.dir"));

		Options options = createOptions();
		UserProfile profile = new UserProfile();
		DBConnectionProfile dbconn = new DBConnectionProfile();

		validateInput(args, options, profile, dbconn);

		File input = new File(profile.inputFilePath);
		updateQueue(qryQueue, input);

		// Initialize DB connection, single thread support to access DB only now
		Connection conn = null;
		try {
			conn = getConnection(dbconn);
		} catch (Exception e2) {
			e2.printStackTrace();
			System.exit(-1);
		}

		while (!qryQueue.isEmpty()) {
			UserProfile up = qryQueue.poll();
			if (up != null) {
				System.out.println("UserProfile processed " + up.inputFilePath);
				run(conn, up);
			}
		}
	}

	// TBD: Export this to Thread Pool
	public static void run(Connection conn, UserProfile profile)
			throws IOException {

		// Prepare output file
		CSVWriter writer = new CSVWriter(
				new FileWriter(profile.outputFilePath), ',',
				CSVWriter.DEFAULT_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_QUOTE_CHARACTER, "\n");

		// TBD: Open SQL file and read in all SQL's
		ArrayList<String> sqlList = profile.sqlList;
		BufferedReader br = new BufferedReader(new FileReader(new File(
				profile.inputFilePath)));
		String sqlStat = null;
		while ((sqlStat = br.readLine()) != null) {
			if (sqlStat.length() > 0) {
				sqlList.add(sqlStat);
				System.out.println("Reading in " + sqlStat);
			}
		}
		br.close();

		for (int sqlIdx = 0; sqlIdx < sqlList.size(); sqlIdx++) {
			String sqlToExecute = sqlList.get(sqlIdx);
			System.out.println("Executing " + sqlToExecute);
			Statement st = null;
			try {
				st = conn.createStatement();
			} catch (SQLException e2) {
				e2.printStackTrace();
				try {
					conn.close();
				} catch (SQLException e) {
				}
				System.exit(-1);
			}

			java.sql.ResultSet rs = null;
			DataTypeMapping dtm = new DataTypeMapping();
			try {
				rs = st.executeQuery(sqlToExecute);

				ResultSetMetaData rsMetaData = rs.getMetaData();
				int numberOfCol = rsMetaData.getColumnCount();
				System.out.println("resultSet MetaData column Count="
						+ numberOfCol);
				String[] columnAry = new String[numberOfCol];
				for (int i = 1; i <= numberOfCol; i++) {
					System.out.print(rsMetaData.getColumnName(i) + ",");
					columnAry[i - 1] = rsMetaData.getColumnLabel(i);
				}
				System.out.println("");
				// Write header first
				writer.writeNext(columnAry);
				String[] numFieldKeyword = {"grp","num","dol","spend","rwm25","sumof"};
				int[] colType = dtm.findColIndex(columnAry, numFieldKeyword); // Detect
																	// all GRP
																	// columns,
																	// apply
																	// decimal

				rs = st.executeQuery(sqlToExecute);

				// Do Not use column header to fetch the results, some column
				// header
				// has the same name. Use column index instead.
				int totalRead = 0;
				String result = null;
				while (rs.next()) {
					int i = 1;
					for (i = 1; i < numberOfCol; i++) {
						result = dtm.get(rs, colType, i);
						// System.out.print("\"" + result + "\",");
						columnAry[i - 1] = (result == null ? "" : result);
					}
					result = dtm.get(rs, colType, i);
					;
					columnAry[i - 1] = (result == null ? "" : result);
					// System.out.println("\"" + result + "\"");
					++totalRead;
					writer.writeNext(columnAry); // apply quotes to all
				}
				System.out.println("Total read recored " + totalRead);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					st.close();
				} catch (SQLException e1) {
				}
				writer.close();
			}
		}
	}

	private static Connection getConnection(DBConnectionProfile dbconn)
			throws Exception {
		String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
		String url = "jdbc:odbc:" + dbconn.dbname;
		String username = dbconn.username;
		String password = dbconn.passwd;
		Class.forName(driver);
		return DriverManager.getConnection(url, username, password);
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(DBConnectionProfile.DBNAME, true, "Database Name");
		options.addOption(DBConnectionProfile.USERNAME, true, "Username");
		options.addOption(DBConnectionProfile.PASSWORD, true, "Password");
		options.addOption(UserProfile.INPUTFILE, true, "Input file path");
		options.addOption(UserProfile.OUTPUTFILE, true, "Output file path");
		options.addOption(UserProfile.COMPRESSION, false,
				"Apply compression to output file");
		return options;
	}

	private static void printHelpAndExit(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(QueryDBMain.class.getName(), options);
		System.exit(-1);
	}

	private static void validateInput(String[] args, Options options,
			UserProfile profile, DBConnectionProfile dbconn) {

		// Just parsing the command line
		CommandLineParser parser = new PosixParser();

		try {
			CommandLine line = parser.parse(options, args);

			// Results profile
			if (line.hasOption(UserProfile.INPUTFILE)) {
				profile.inputFilePath = line
						.getOptionValue(UserProfile.INPUTFILE);
				System.out.println("Input file is set to "
						+ profile.inputFilePath);
				if (!new java.io.File(profile.inputFilePath).exists()) {
					System.err.println(profile.inputFilePath
							+ " does not exist, can't continue, exiting");
					System.exit(-1);
				}
			} else {
				System.err.println("Required input file option -"
						+ UserProfile.INPUTFILE
						+ " is not specified, can't continue");
				printHelpAndExit(options);
			}
			if (line.hasOption(UserProfile.OUTPUTFILE)) {
				profile.outputFilePath = line
						.getOptionValue(UserProfile.OUTPUTFILE);
			} else {
				if (!new File(profile.inputFilePath).isDirectory()) {
					String newFile = FilenameUtils
							.removeExtension(profile.inputFilePath);
					profile.outputFilePath = newFile + ".csv";
					System.err.println("Optional output file option -"
							+ UserProfile.OUTPUTFILE
							+ " is not specified, default "
							+ profile.outputFilePath + " as default");
				} else {
					System.err
							.println("Input is directory, all output will be located in its SQL input directory");
				}
			}
			// Delete exiting output files if exist
			if (new java.io.File(profile.outputFilePath).exists()
					&& !new File(profile.inputFilePath).isDirectory()) {
				System.err.println(profile.outputFilePath
						+ " already exist, overriding");
				new java.io.File(profile.outputFilePath).delete();
			}
			if (line.hasOption(UserProfile.COMPRESSION)) {
				profile.compression = true;
			} else {
				profile.compression = false;
			}

			// DB Connection Profile
			if (!line.hasOption(DBConnectionProfile.USERNAME)) {
				System.err.println("DB username option -"
						+ DBConnectionProfile.USERNAME
						+ " is not specified, applying \"sa\" as username");
				dbconn.username = "sa";
			} else {
				dbconn.username = line
						.getOptionValue(DBConnectionProfile.USERNAME);
			}
			if (!line.hasOption(DBConnectionProfile.PASSWORD)) {
				System.err.println("DB password option -"
						+ DBConnectionProfile.PASSWORD
						+ " is not specified, applying empty password");
				dbconn.passwd = "";
			} else {
				dbconn.passwd = line
						.getOptionValue(DBConnectionProfile.PASSWORD);
			}
			if (!line.hasOption(DBConnectionProfile.DBNAME)) {
				System.err
						.println("DB databaes name option -"
								+ DBConnectionProfile.DBNAME
								+ " is not specified, can't apply any database to access. You MUST specify the database name");
				printHelpAndExit(options);
			} else {
				dbconn.dbname = line.getOptionValue(DBConnectionProfile.DBNAME);
			}

		} catch (org.apache.commons.cli.ParseException exp) {

			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			printHelpAndExit(options);
		}
	}

	private static void updateQueue(java.util.Queue<UserProfile> queue,
			File input) throws FileNotFoundException {
		System.out.println("Traversing directory to glean all SQL files");
		if (input.isDirectory()) {
			// Traverse the directory for all SQL, and generate their CSVs
			File[] allFiles = input.listFiles();
			for (int fidx = 0; fidx < allFiles.length; fidx++) {
				if (allFiles[fidx].exists() && allFiles[fidx].isFile()
						&& allFiles[fidx].getAbsolutePath().endsWith(".sql")) {
					UserProfile newProfile = new UserProfile(
							allFiles[fidx].getAbsolutePath());
					qryQueue.add(newProfile);
					System.out.println("Found sql " + newProfile.inputFilePath
							+ " to process");
				} else {
					System.out.println("Traversing into " + allFiles[fidx].getName()
							+ " to look for more SQL files");
					updateQueue(queue, allFiles[fidx]);
				}
			}
		} else {
			if(input.exists() && input.isFile() && input.getAbsolutePath().endsWith(".sql")) {
				qryQueue.add(new UserProfile(input.getAbsolutePath()));
			} else {
				System.out.println("Skipping non-SQL files " + input.getAbsolutePath());
			}
		}
	}

}
