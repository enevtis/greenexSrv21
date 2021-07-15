package neserver.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;
import obj.greenexSrv2.nvs.com.TblField;

public class SQLRequestHandler2 extends HandlerTemplate {

	public String caption = "";

	public SQLRequestHandler2(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {
		String out = "";
		String SQL = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));
		ConnectionData conData = readConnectionParameters(pr);
		String connString = "";
		SqlReturn res = null;

		this.caption = "SQL (" + params.get("action") + ") for ";
		this.caption += pr.description + " " + pr.sid + " " + pr.sysnr;

		out += getBeginPage();
		out += strTopPanel(caption);

		String typeDatabase = pr.description.toLowerCase().contains("hana") ? "sap_hana" : (pr.description.toLowerCase().contains("oracle") ? "oracle" : "unknown");		
		
		SQL= readFromTable_sql_remote_check(this.getClass().getSimpleName(),params.get("action"),typeDatabase);

		switch (typeDatabase) {
		
		case "sap_hana":
			
			connString = "jdbc:sap://" + conData.ip + ":" + conData.port + "/?autocommit=false";
			res = gData.sqlReq.getSqlFromSapHana2(connString, conData.user, conData.password, SQL);
			out += getHtmlPlainTable(res);
			
			break;
		case "oracle":

			connString = "jdbc:oracle:thin:@" + conData.ip + ":" + conData.port + ":"
					+ conData.sid;
			res = gData.sqlReq.getSqlFromOracle(connString, conData.user, conData.password, SQL);
			out += getHtmlPlainTable(res);			
			
			break;
			
			
		default:
			break;
		}
		
		out += getEndPage();
		return out;
	}

	@Override
	protected String readFromTable_sql_remote_check(String className, String job_name, String filter) {
		String out = "";

		String SQL = "select * from sql_remote_check where class='" + className + "' ";
		SQL += "and job_name='" + job_name + "' and filter='" + filter + "' order by id";
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {

			out += rec.get("sql_text") + " ";

		}
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

	

}
