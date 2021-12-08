package health.greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class ClusterHealth extends HealthTemplate{

	public ClusterHealth(globalData gData) {
		super(gData);

	}

	public String getHPSG_status(String reportName) {
		String out = "";
		List<String> guids = getListGuidsForJobName("hpsg", reportName);
		if (guids.size() == 0)
			return "";
		out += "<p class='caption_item'>" + gData.tr("9530396f-217a-43b1-83c7-86f26ea490bd") + ":</p>";
		out += "<ul class='ol_1'>";
		for (String guid: guids){		
			out += "<li>" + checkHealthHPSG(guid);		
		}
		out += "</ul>";

		return out;
	}
	public String getReplication_status(String reportName) {
		String out = "";
		List<String> guids = getListGuidsForJobName("replication", reportName);
		if (guids.size() == 0)
			return "";
		
		out += "<p class='caption_item'>" + gData.tr("1d8a6b92-12ad-4460-a895-99d0e3aef2bf") + ":</p>";
		out += "<ul class='ol_1'>";
		for (String guid: guids){		
			
			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(guid);
			
			if(pr.DB.contains("ORACLE")) {
				out += "<li>" + checkReplicationOracle(guid);	
			}else if (pr.DB.contains("HANA")) {
				out += "<li>" + checkReplicationSAPHana(guid);				
			}
				
		}
		out += "</ul>";

		return out;
	}
	protected String checkReplicationOracle(String guid) {
		String out = "";	//222
		out += "Repl Ora";
		return out;
	}
	protected String checkReplicationSAPHana(String guid) {
		String out = "";	//205

		String SQL = "";	
		SQL += "SELECT a.*,b.short,b.db_type, "; 
		SQL += "TIMESTAMPDIFF(MINUTE,a.check_date, NOW()) AS 'past_minutes',  ";
		SQL += "CASE WHEN result_number < 100 THEN 'работает ок' ELSE 'есть ошибки' END AS 'work_status',  ";
		SQL += "CASE WHEN result_number < 100 THEN 'black' ELSE 'red' END AS 'status_color'  ";
		SQL += "FROM monitor_results a  ";
		SQL += "LEFT JOIN db_systems b ON a.object_guid = b.guid ";
		SQL += "WHERE monitor_number = 205 and object_guid='" + guid + "' ";
		SQL += "AND check_date = (SELECT MAX(check_date) FROM monitor_results WHERE monitor_number = 205 and object_guid='" + guid + "') ";
		
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {

			String color = rec.get("status_color");
			out += "<p color='" + color + "'>";
			out +=  gData.tr("98b510de-a0c2-45af-9f79-3ee6eb24ed4d") + " ";
			out += rec.get("db_type")  + " " + rec.get("short") + ": " + rec.get("work_status");
			out += "(" + Utils.timeConvert(Integer.valueOf(rec.get("past_minutes"))) + " " + gData.tr("d3ef97de-890b-4722-beb2-2811c225ae82") + ")";
			out += "</p>";

		}
		
	
		return out;
	}	
	
	
	public String checkHealthHPSG(String guid) {
		// 108 monitor
		String out = "";

		String SQL = "";
		SQL += "SELECT a.*,b.def_hostname, ";
		SQL += "TIMESTAMPDIFF(MINUTE,a.check_date, NOW()) AS 'past_minutes', ";
		SQL += "CASE WHEN result_number < 10 THEN 'работает ок' ELSE 'есть ошибки' END AS 'work_status', ";
		SQL += "CASE WHEN result_number < 10 THEN 'black' ELSE 'red' END AS 'status_color' ";
		SQL += "FROM monitor_results a ";
		SQL += "LEFT JOIN servers b ON a.object_guid = b.guid ";
		SQL += "WHERE monitor_number = 108 and object_guid='" + guid + "'  ";
		SQL += "AND check_date = (SELECT MAX(check_date) FROM monitor_results WHERE monitor_number = 108 and object_guid='"
				+ guid + "') ";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {

			String color = rec.get("status_color");
			out += "<p color='" + color + "'>";
			out += "HP Service guarg on ";
			out += rec.get("def_hostname") + ": " + rec.get("work_status");
			out += "(" + Utils.timeConvert(Integer.valueOf(rec.get("past_minutes"))) + " " + gData.tr("d3ef97de-890b-4722-beb2-2811c225ae82") + ")";
			out += "</p>";

		}

		return out;
	}

}
