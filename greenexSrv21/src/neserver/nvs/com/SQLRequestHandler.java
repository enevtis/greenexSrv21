package neserver.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;
import obj.greenexSrv2.nvs.com.TblField;

public class SQLRequestHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_links";
	private String tableName = "monitor_links";
	private String SQL = "";
	public String caption = "";

	public String graphGuid = "";
	public String physGuid = "";

	public SQLRequestHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));
		ConnectionData conData = readConnectionParameters(pr);

		this.caption = gData.tr("SQL for ");
		this.caption += pr.description + " " + pr.sid + " " + pr.sysnr;

		out += getBeginPage();
		out += strTopPanel(caption);

		
		
		String fileName = "";
		
		if (params!=null) {
			if (params.containsKey("action")) {
				fileName = gData.mainPath + "/files/sap_hana_sql/" + params.get("action") + ".sql";
			} 
		}

		String SQL = readTextFile(fileName);

		String connString = "jdbc:sap://" + conData.ip + ":" + conData.port + "/?autocommit=false";

		SqlReturn res = getSqlFromSapHana2(connString, conData.user, conData.password, SQL);

		out += getHtmlPlainTable(res);

		out += getEndPage();

		return out;
	}

	public String getHtmlPlainTable(SqlReturn res) {
		return getHtmlPlainTable(res, "table1");
	}
	
	public String getHtmlPlainTable(SqlReturn res, String cssClass) {
		String out = "";

		out += "<table class='table1'>";

		out += "<thead>";
		out += "<tr>";
		for (TblField tf : res.fields) {
			out += "<th>";
			out += (tf.fieldLabel.isEmpty()) ? tf.fieldName : tf.fieldLabel;
			out += "</th>";
		}

		out += "</tr>";
		out += "</thead>";

		out += "</tbody>";

		for (Map<String, String> rec : res.records) {

			out += "<tr>";

			for (TblField tf : res.fields) {
				out += "<td>";
				out += rec.get(tf.fieldName);
				out += "</td>";
			}

			out += "</tr>";

		}
		out += "</tbody>";
		out += "</table>";
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

//            System.out.println("resordsMap " + resordsMap.size());
			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
			}
		}

		return out;
	}
}
