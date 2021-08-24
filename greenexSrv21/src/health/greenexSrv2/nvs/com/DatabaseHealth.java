package health.greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class DatabaseHealth extends HealthTemplate{
	public DatabaseHealth(globalData gData) {
		super(gData);

	}

	public String getBackup_status(String reportName) {

		String out = "";
		List<String> guids = getListGuidsForJobName("backup", reportName);
		if (guids.size() == 0)
			return "";
		out += "<p class='caption_item'>Статус бэкапа:</p>";
		out += "<ul class='ol_1'>";
		for (String guid: guids){		
			out += "<li>" + checkHealthBackup(guid);		
		}
		out += "</ul>";

		return out;
	}

	
	private String checkHealthBackup(String guid) {
		// 201 проверка бэкапов SAP_HANA (прошло часов)
		// 208 проверка бэкапов Oracle прошло часов
		String out = "";

		String SQL = "";
		
		SQL += "SELECT a.*,b.short,b.db_type, b.sid, c.value_limit, ";
		SQL += "TIMESTAMPDIFF(MINUTE,a.check_date, NOW()) AS 'past_minutes', "; 
		SQL += "CASE WHEN result_number < c.value_limit THEN 'работает ок' ELSE 'есть ошибки' END AS 'work_status',  ";
		SQL += "CASE WHEN result_number < c.value_limit THEN 'black' ELSE 'red' END AS 'status_color'  ";
		SQL += "FROM monitor_results a  ";
		SQL += "LEFT JOIN db_systems b ON a.object_guid = b.guid ";
		SQL += "LEFT JOIN monitor_links c ON a.object_guid = c.object_guid AND c.monitor_number IN (201,208) ";
		SQL += "WHERE a.monitor_number IN (201,208) AND a.object_guid='" + guid + "' ";
		SQL += "AND check_date = (SELECT MAX(check_date) FROM monitor_results WHERE monitor_number IN (201,208)  ";
		SQL += "and object_guid='" + guid + "') ";
		

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {

			String color = rec.get("status_color");
			String fontWeight = color.equals("red") ? "bold" : "normal";
			out += "<p style='" + Utils.getStyleMessage(color) + "'>";
			out += "бэкап " + rec.get("db_type")  + " " + rec.get("short") + " " + rec.get("work_status");
			out += ": Последний бэкап был " ; 

			int pastMinutesAfterBackup = (int)(Float.valueOf(rec.get("result_number")) * 60);

			out += "" + Utils.timeConvert(pastMinutesAfterBackup) + " назад ";
			
			if (color.equals("red")){
			 out += " что больше лимита " + rec.get("value_limit") + " час.";			
			} else {
			 out += " что меньше лимита " + rec.get("value_limit") + " час.";				
			}

			out += "(" + Utils.timeConvert(Integer.valueOf(rec.get("past_minutes"))) + " назад)";
			out += "</p>";

		}

		return out;
	}

}
