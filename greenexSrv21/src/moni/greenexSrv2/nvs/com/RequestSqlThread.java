package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class RequestSqlThread extends Thread {
	private globalData gData;
	private volatile Connection conn = null;
	private String connectionString;
	private String user;
	private String password;
	private String SQL;
	private remoteSystem s;
	List<Map<String, String>> resordsMap = new ArrayList<Map<String, String>>();

	public RequestSqlThread(globalData gData, String connectionString, String user, String password, String SQL,
			remoteSystem s) {
		this.gData = gData;
		this.connectionString = connectionString;
		this.user = user;
		this.password = password;
		this.SQL = SQL;
		this.s = s;
	}

	@Override
	public void run() {

		Connection conn = null;

		String conn_str = "";
		String user = s.params.get("user");
		String hash = s.params.get("hash");

		conn_str = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false";

		try {

			conn = DriverManager.getConnection(conn_str, user, password);
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
					}

				}

				resordsMap.add(record);

			}

			s.records = resordsMap;

			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			s.params.put("message", errors.toString());

		}
	}
}
