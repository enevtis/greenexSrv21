package greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import obj.greenexSrv2.nvs.com.SqlReturn;
import obj.greenexSrv2.nvs.com.TblField;

public class SqlRequest {

	globalData gData;

	String connectionString = "";
	String user = "";
	String password = "";

	public SqlRequest(globalData gData) {
		this.gData = gData;
		this.connectionString = gData.commonParams.get("connectionString");
		this.user = gData.commonParams.get("user");
		this.password = gData.commonParams.get("password");

	}

	public List<Map<String, String>> getSelect(String SQL) {

		List<Map<String, String>> resordsMap = new ArrayList<Map<String, String>>();

		if (SQL == null || SQL.isEmpty()) {
			return resordsMap;
		}
		Connection conn = null;

		try {

			conn = DriverManager.getConnection(connectionString, user, password);
			
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(SQL);

			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			String colName = "";
			String colType = "";
			String colLabel = "";

			while (rs.next()) {

				Map<String, String> record = new HashMap<String, String>();

				for (int i = 1; i <= cols; i++) {

					colName = rsmd.getColumnName(i);
					colType = rsmd.getColumnTypeName(i);
					colLabel = rsmd.getColumnLabel(i);

					if (colType.toUpperCase().contains("CHAR")) {

						record.put(colLabel, rs.getString(colLabel));

					} else if (colType.toUpperCase().contains("INT")) {

						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));

//		                	this.gData.logger.info("colLabel = " + colLabel + " = " + rs.getInt(colLabel) );                	

					} else if (colType.toUpperCase().contains("FLOAT")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));

					} else if (colType.toUpperCase().contains("DATETIME")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("TIMESTAMP")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("DOUBLE")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));

					} else if (colType.toUpperCase().contains("DECIMAL")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));
						
					} else {
						record.put(colLabel, colType);

						gData.logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType="
								+ colType + " colLabel=" + colLabel);

					}

				}

				resordsMap.add(record);

			}

//	            System.out.println("resordsMap " + resordsMap.size());
			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(SQL + " \n " + errors.toString());

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(SQL + " \n " + errors.toString());
			}
		}

		return resordsMap;
	}

	public List<Map<String, String>> getSelect(String dbConnectionString, String dbUser, String dbpassword,
			String SQL) {

		List<Map<String, String>> resordsMap = new ArrayList<Map<String, String>>();

		if (SQL == null || SQL.isEmpty()) {
			return resordsMap;
		}
		Connection conn = null;

		try {

			conn = DriverManager.getConnection(dbConnectionString, dbUser, dbpassword);

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(SQL);

			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			String colName = "";
			String colType = "";
			String colLabel = "";

			while (rs.next()) {

				Map<String, String> record = new HashMap<String, String>();

				for (int i = 1; i <= cols; i++) {

					colName = rsmd.getColumnName(i);
					colType = rsmd.getColumnTypeName(i);
					colLabel = rsmd.getColumnLabel(i);

					if (colType.toUpperCase().contains("CHAR")) {

						record.put(colLabel, rs.getString(colLabel));

					} else if (colType.toUpperCase().contains("INT")) {

						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));

//		                	this.gData.logger.info("colLabel = " + colLabel + " = " + rs.getInt(colLabel) );                	

					} else if (colType.toUpperCase().contains("FLOAT")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));

					} else if (colType.toUpperCase().contains("DATETIME")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("TIMESTAMP")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("DOUBLE")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));
					} else {

						record.put(colLabel, colType);

						gData.logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType="
								+ colType + " colLabel=" + colLabel);

					}

				}

				resordsMap.add(record);

			}

//	            System.out.println("resordsMap " + resordsMap.size());
			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(SQL + " \n " + errors.toString());

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(SQL + " \n " + errors.toString());
			}
		}

		return resordsMap;
	}

	public void saveResult(List<String> sqlList) {

		String currSQL = "";

		if (sqlList == null)
			return;

		if (sqlList.size() == 0)
			return;

		Connection conn = null;

		try {

			conn = DriverManager.getConnection(gData.commonParams.get("connectionString"),
					gData.commonParams.get("user"), gData.commonParams.get("password"));

			for (String s : sqlList) {
				Statement stmt = conn.createStatement();
				currSQL = s;
				stmt.executeUpdate(s);
			}

			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(currSQL + "\n" + errors.toString());

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(currSQL + "\n" + errors.toString());
			}
		}

	}

	public void saveResult(String sqlLine) {

		String currSQL = "";

		if (sqlLine.isEmpty())
			return;

		Connection conn = null;

		try {

			conn = DriverManager.getConnection(gData.commonParams.get("connectionString"),
					gData.commonParams.get("user"), gData.commonParams.get("password"));

			Statement stmt = conn.createStatement();

			stmt.executeUpdate(sqlLine);

			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(sqlLine + "\n" + errors.toString());

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(sqlLine + "\n" + errors.toString());
			}
		}

	}

	public String readOneValue(String SQL) {
		String out = "";
		String[] parts = SQL.split("\\s+");
		String fieldName = parts[1];
		List<Map<String, String>> records_list = getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {
				for (Map<String, String> rec : records_list) {
					out = rec.get(fieldName);
				}
			}
		}
		return out;
		
// 		Example		
//		String SQL = "select value_limit from monitor_links where monitor_number=" + params.get("job_number")
//		+ " and object_guid='" + s.params.get("guid") + "'";
		
	}

	public SqlReturn getSqlFromSapHana(String connString, String user, String pass, String SQL) {

		SqlReturn out = new SqlReturn();
		// List<Map<String , String>> resordsMap = new ArrayList<Map<String,String>>();
		Connection conn = null;

		try {
			conn = DriverManager.getConnection(connString, user, pass);

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(SQL);

			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			String colName = "";
			String colType = "";
			String colLabel = "";

			while (rs.next()) {

				Map<String, String> record = new HashMap<String, String>();

				for (int i = 1; i <= cols; i++) {

					colName = rsmd.getColumnName(i);
					colType = rsmd.getColumnTypeName(i);
					colLabel = rsmd.getColumnLabel(i);

					if (colType.toUpperCase().contains("CHAR")) {

						record.put(colLabel, rs.getString(colLabel));

					} else if (colType.toUpperCase().contains("INTEGER")) {
//		                	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));
					} else if (colType.toUpperCase().contains("BIGINT")) {

//		                	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
						record.put(colLabel, String.valueOf(rs.getLong(colLabel)));

					} else if (colType.toUpperCase().contains("SMALLINT")) {

//	               	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));

					} else if (colType.toUpperCase().contains("DECIMAL")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));

					} else if (colType.toUpperCase().contains("DATETIME")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("TIMESTAMP")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("NCLOB")) {

						record.put(colLabel, rs.getString(colLabel));

					} else {

						gData.logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType="
								+ colType + " colLabel=" + colLabel);

					}

				}

				out.records.add(record);

			}

			conn.close();

		} catch (Exception e) {

			out.isOk = false;

			String buffer = e.getMessage();
			buffer = buffer.replaceAll("[^A-Za-z0-9_. :/\\-<>?=&\\p{IsCyrillic}]", "");
			out.message = buffer;
//			out.message = out.message.substring(0,10);

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());

		}

		return out;
	}
	public SqlReturn getSqlFromOracle(String connString, String user, String pass, String SQL) {

		SqlReturn out = new SqlReturn();
		boolean isFieldListEmpty = true;
		Connection conn = null;

		try {
			conn = DriverManager.getConnection(connString, user, pass);

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(SQL);

			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			String colName = "";
			String colType = "";
			String colLabel = "";

			
			while (rs.next()) {

				Map<String, String> record = new HashMap<String, String>();

				for (int i = 1; i <= cols; i++) {

					colName = rsmd.getColumnName(i);
					colType = rsmd.getColumnTypeName(i).toUpperCase();
					colLabel = rsmd.getColumnLabel(i);

					if (isFieldListEmpty) {
						TblField tf = new TblField();
						tf.fieldName = colName;
						tf.fieldType = colType;
						tf.fieldLabel = colLabel;
						out.fields.add(tf);
					}


					
					if (colType.toUpperCase().contains("CHAR")) {

						record.put(colLabel, rs.getString(colLabel));

					} else if (colType.toUpperCase().contains("INTEGER") 
							|| colType.contains("NUMBER")) {
						
						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));
					} else if (colType.toUpperCase().contains("BIGINT")) {

						record.put(colLabel, String.valueOf(rs.getLong(colLabel)));

					} else if (colType.toUpperCase().contains("SMALLINT")) {

						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));

					} else if (colType.toUpperCase().contains("DECIMAL")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));

					} else if (colType.toUpperCase().contains("DATETIME")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("TIMESTAMP")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("NCLOB")) {

						record.put(colLabel, rs.getString(colLabel));

					} else {

						gData.logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType="
								+ colType + " colLabel=" + colLabel);

					}

				}

				out.records.add(record);
				isFieldListEmpty = false;

			}

			conn.close();

		} catch (Exception e) {

			out.isOk = false;

			String buffer = e.getMessage();
			buffer = buffer.replaceAll("[^A-Za-z0-9_. :/\\-<>?=&\\p{IsCyrillic}]", "");
			out.message = buffer;
//			out.message = out.message.substring(0,10);

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());

		}

		return out;
	}

	public SqlReturn getSqlFromSapHana2(String connString, String user, String pass, String SQL) {

		SqlReturn out = new SqlReturn();
		boolean isFieldListEmpty = true;

		Connection conn = null;

		try {
			conn = DriverManager.getConnection(connString, user, pass);

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(SQL);

			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			String colName = "";
			String colType = "";
			String colLabel = "";

			while (rs.next()) {

				Map<String, String> record = new HashMap<String, String>();

				for (int i = 1; i <= cols; i++) {

					colName = rsmd.getColumnName(i);
					colType = rsmd.getColumnTypeName(i);
					colLabel = rsmd.getColumnLabel(i);

					if (isFieldListEmpty) {
						TblField tf = new TblField();
						tf.fieldName = colName;
						tf.fieldType = colType;
						tf.fieldLabel = colLabel;
						out.fields.add(tf);
					}

					if (colType.toUpperCase().contains("CHAR")) {

						record.put(colLabel, rs.getString(colLabel));

					} else if (colType.toUpperCase().contains("INTEGER")) {
//	                	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));
					} else if (colType.toUpperCase().contains("BIGINT")) {

//	                	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
						record.put(colLabel, String.valueOf(rs.getLong(colLabel)));

					} else if (colType.toUpperCase().contains("SMALLINT")) {

//               	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
						record.put(colLabel, String.valueOf(rs.getInt(colLabel)));

					} else if (colType.toUpperCase().contains("DECIMAL")) {

						record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));

					} else if (colType.toUpperCase().contains("DATETIME")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("TIMESTAMP")) {

						record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));

					} else if (colType.toUpperCase().contains("NCLOB")) {

						record.put(colLabel, rs.getString(colLabel));

					} else {

						gData.logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType="
								+ colType + " colLabel=" + colLabel);

					}

				}

				out.records.add(record);
				isFieldListEmpty = false;

			}
			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());

		} 

		return out;
	}
}


