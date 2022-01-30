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
		out += "<p class='caption_item'>" + gData.tr("f874771b-1255-46c0-ac91-a4d22397d14e") +":</p>";
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
		SQL += "CASE WHEN result_number < c.value_limit THEN '" + gData.tr("81f15e97-f500-407a-b94d-3341659581c9") + "' ELSE '" + gData.tr("9ee99228-89a5-4828-95b2-d453310e3987") + "' END AS 'work_status',  ";
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
			out += "" + gData.tr("184ec0a1-ecd5-4aad-9fe2-1f5161c7664f") + " " + rec.get("db_type")  + " " + rec.get("short") + " " + rec.get("work_status");
			out += ": "+ gData.tr("0086e171-bd29-4217-be59-88b6ae6268db") +" " ; 

			int pastMinutesAfterBackup = (int)(Float.valueOf(rec.get("result_number")) * 60);

			out += "" + Utils.timeConvert(pastMinutesAfterBackup,gData.lang) + " " + gData.tr("d3ef97de-890b-4722-beb2-2811c225ae82");
			
			if (color.equals("red")){
			 out += " " + gData.tr("b6542d16-9f82-47f2-a644-12feff1bb6c5") + " " + rec.get("value_limit") + " " + gData.tr("be46ab5f-8f47-46b2-ba5c-40ec682ee489") +".";			
			} else {
			 out += " " + gData.tr("ad531450-79e4-4019-8a1c-9ca101e6964b") + " " + rec.get("value_limit") + " " + gData.tr("be46ab5f-8f47-46b2-ba5c-40ec682ee489") +".";				
			}

			out += "(" + Utils.timeConvert(Integer.valueOf(rec.get("past_minutes")),gData.lang) + " " + gData.tr("d3ef97de-890b-4722-beb2-2811c225ae82") + ")";
			out += "</p>";

		}

		return out;
	}

}
