package neserver.nvs.com;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.conn.jco.ext.DestinationDataProvider;

import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.SAPR3;
import obj.greenexSrv2.nvs.com.SqlReturn;
import obj.greenexSrv2.nvs.com.TblField;

public class TestHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_conn_data";
	private String tableName = screenName;
	private String SQL = "";
	public String caption = "Test page";

	public TestHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {

		String out = "";
		String curStyle = "";

		int userRight = Utils.determineUserRights(gData, "connect_data", gData.commonParams.get("currentUser"));
		if (userRight < 1) {

			out += "Sorry, you don't have necessary authorisation! Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}

		out += getBeginPage();
		out += strTopPanel(caption);

		Map<String, String> sparams = new HashMap<String, String>();

		out += getDataForAllSystems(sparams);

		out += getEndPage();

		return out;
	}

	public String getDataForAllSystems(Map<String, String> params) {
		String out = "";
		String SQL = "";
//		SQL += "SELECT object_guid, MAX(check_date) AS max_check_date, MIN(check_date) AS min_check_date ";
//		SQL += " FROM monitor_idocs GROUP BY object_guid ";
		
		SQL += "SELECT c.short AS 'project', b.short AS 'sap_system', b.sid, b.def_ip, s1.*, ";
		SQL += "TIMESTAMPDIFF(HOUR,s1.check_date_old,s1.check_date_new) AS 'past_hours' ";
		SQL += "FROM ( ";
		SQL += "SELECT object_guid, MAX(check_date) AS check_date_new, MIN(check_date) AS check_date_old FROM monitor_idocs ";
		SQL += "GROUP BY object_guid ) s1 ";
		SQL += "LEFT JOIN app_systems b ON s1.object_guid = b.guid ";
		SQL += "LEFT JOIN projects c ON b.project_guid = c.guid ";
		
		
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		
		for (Map<String, String> rec : records) {
			
			String objectGuid = rec.get("object_guid");
			String maxCheckDate = rec.get("check_date_new");
			String minCheckDate = rec.get("check_date_old");
			
			out += "<hr>";
			out += " Проект:" + rec.get("project") + "<br>";
			out += " SAP система:" + rec.get("sap_system") + "<br>";
			int minutes = Integer.valueOf(rec.get("past_hours")) * 60;	
			out += " интервал наблюдения " + Utils.timeConvert(minutes) + "<br>";
			out += "с " + rec.get("check_date_old") + " по " + rec.get("check_date_new") + "<br>";

			String link = "<a href='https://" + gData.getOwnIp() + ":" + gData.commonParams.get("webServicePort") ;
			link += "/idocs?guid=" + objectGuid + "'>подробнее</a><br>данные для входа demo/demo";
			out += link;								
			
			
			String SQL2 = "";
			SQL2 += "SELECT monthname(STR_TO_DATE(s1.month,'%m')) AS 'месяц', s1.year, ";
			SQL2 += "FORMAT(s1.total_new,0) AS 'количество_idoc', FORMAT(s2.total_old,0) AS 'старое значение', ";
			SQL2 += "FORMAT(s1.total_new - s2.total_old,0) AS 'рост', ";
			SQL2 += "TIMESTAMPDIFF(HOUR,s2.check_date_old,s1.check_date_new) AS 'за часов' ";
			SQL2 += "FROM ( ";
			SQL2 += "SELECT a.object_guid, b.sid, b.def_ip, b.short,a.year, a.month, ";
			SQL2 += "sum(a.total) AS total_new, a.check_date AS check_date_new FROM monitor_idocs a ";
			SQL2 += "LEFT JOIN app_systems b ON a.object_guid = b.guid ";
			SQL2 += "WHERE ABS(TIMESTAMPDIFF(MINUTE,check_date,'" + maxCheckDate + "')) < 5 ";
			SQL2 += "GROUP BY short, sid, def_ip, YEAR, MONTH ";
			SQL2 += "ORDER BY short,YEAR,MONTH ) s1 ";
			SQL2 += "LEFT JOIN  ";
			SQL2 += "( ";
			SQL2 += "SELECT a.object_guid, b.sid, b.def_ip, b.short,a.year, a.month,  ";
			SQL2 += "sum(a.total) AS total_old, a.check_date AS check_date_old FROM monitor_idocs a ";
			SQL2 += "LEFT JOIN app_systems b ON a.object_guid = b.guid ";
			SQL2 += "WHERE ABS(TIMESTAMPDIFF(MINUTE,check_date,'" + minCheckDate + "')) < 5 ";
			SQL2 += "GROUP BY short, sid, def_ip, YEAR, MONTH ";
			SQL2 += ") s2 ON s1.object_guid = s2.object_guid AND s1.year = s2.year AND s1.month = s2.month ";
			SQL2 += "WHERE s1.object_guid = '" + objectGuid + "' ";			
			
			out += Utils.getHtmlTablePageFromSqlreturn(gData, SQL2);
			
			

		}
		
		
		return out;
	}
	
	
	
	
	
	public String doTest(Map<String, String> params) {
		String out = "";
		out += "<h3> Айдоки</h3>";

		String SQL = "";

SQL +="SELECT s1.object_guid ,s1.sid, s1.def_ip, s1.short, monthname(STR_TO_DATE(s1.month,'%m')) AS 'период', s1.year,  ";
SQL +="FORMAT(s1.total_new,0) AS total_new, FORMAT(s2.total_old,0) AS total_old, ";
SQL +="FORMAT(s1.total_new - s2.total_old,0) AS 'рост', ";
SQL +="TIMESTAMPDIFF(HOUR,s2.check_date_old,s1.check_date_new) AS 'за дней' ";
SQL +="FROM ( ";
SQL +="SELECT a.object_guid, b.sid, b.def_ip, b.short,a.year, a.month, ";
SQL +="sum(a.total) AS total_new, a.check_date AS check_date_new FROM monitor_idocs a ";
SQL +="LEFT JOIN app_systems b ON a.object_guid = b.guid ";
SQL +="WHERE check_date = (SELECT MAX(check_date) FROM monitor_idocs) ";
SQL +="GROUP BY short, sid, def_ip, YEAR, MONTH ";
SQL +="ORDER BY short,YEAR,MONTH ) s1 ";
SQL +="LEFT JOIN  ";
SQL +="( ";
SQL +="SELECT a.object_guid, b.sid, b.def_ip, b.short,a.year, a.month, "; 
SQL +="sum(a.total) AS total_old, a.check_date AS check_date_old FROM monitor_idocs a ";
SQL +="LEFT JOIN app_systems b ON a.object_guid = b.guid ";
SQL +="WHERE check_date = (SELECT MIN(check_date) FROM monitor_idocs) ";
SQL +="GROUP BY short, sid, def_ip, YEAR, MONTH ";
SQL +=") s2 ON s1.object_guid = s2.object_guid AND s1.year = s2.year AND s1.month = s2.month ";

		out += Utils.getHtmlTablePageFromSqlreturn(gData, SQL);




		return out;
	}

}
