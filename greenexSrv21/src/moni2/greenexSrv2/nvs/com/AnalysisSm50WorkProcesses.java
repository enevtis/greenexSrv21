package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;

public class AnalysisSm50WorkProcesses extends BatchJobTemplate implements Runnable {

	public String currMonitorNumber = "303";
	public Map<String, String> appservers = new HashMap();

	public AnalysisSm50WorkProcesses(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_regular();

			analyze();

			reSetRunningFlag_regular();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	private void analyze() {

		checkUsedPercentWorkProcesses();
		sendLetters();
		checkRecoveryAlerts();		
		

	}

	private void checkRecoveryAlerts() {
		
		String maxFreePercent = gData.commonParams.containsKey("AbapWorkProcessFreePercentLimit")
				? gData.commonParams.get("AbapWorkProcessFreePercentLimit")
				: "50";	
		
		String SQL = "select * from problems where monitor_number=" + currMonitorNumber;		
		
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		List<String> recoverySql = new ArrayList();
		
		
		for (Map<String, String> rec : records) {
	
			String problemGuid = rec.get("guid");
			String objectGuid = rec.get("object_guid");
			String details = rec.get("details");
			String parts[] = details.split("::");
			String appServer = parts[2];
			String wpType = parts[3];
			
			
			
			
			String SQL2 = "";
			SQL2 += "";
			SQL2 += "SELECT s1.total_wp,s2.free_wp, (s1.total_wp-s2.free_wp)/s1.total_wp * 100 AS used_percent, ";
			SQL2 += "CASE WHEN (s1.total_wp-s2.free_wp)/s1.total_wp * 100 < " + maxFreePercent + " THEN 'recovery' ELSE 'do_nothing' END AS 'action' ";
			SQL2 += "FROM ";
			SQL2 += "(SELECT COUNT(*) AS total_wp FROM monitor_abap_wp WHERE check_date = (SELECT MAX(check_date) FROM monitor_abap_wp WHERE object_guid='" + objectGuid + "') "; 
			SQL2 += "AND object_guid='" + objectGuid + "' AND app_server = '" + appServer + "' AND wp_typ = '" + wpType + "' ) s1 ";
			SQL2 += "JOIN ";
			SQL2 += "(SELECT COUNT(*) AS free_wp FROM monitor_abap_wp WHERE check_date = (SELECT MAX(check_date) FROM monitor_abap_wp WHERE object_guid='" + objectGuid + "')  ";
			SQL2 += "AND object_guid='" + objectGuid + "' AND app_server = '" + appServer + "' AND wp_typ = '" + wpType + "' AND wp_status='Waiting' ) s2 ";
			
			gData.saveToLog(SQL2, params.get("job_name"));
			
			
			List<Map<String, String>> records3 = gData.sqlReq.getSelect(SQL);
			
			for (Map<String, String> rec3 : records3) {
				String action = rec3.get("action");
				if(action.toLowerCase().contains("recovery")) {
				
					String SQL3 = "update problems set is_fixed='X',";	
					SQL3 += "fixed=now(),fixed_result=" + params.get("used_percent") + ",";
					SQL3 += "fixed_limit=" + maxFreePercent + "";
					SQL3 += " where guid='" + problemGuid+ "'" ;
					
					recoverySql.add(SQL3);
					gData.saveToLog(SQL3, params.get("job_name"));
					
				}
				
				
				
			}
			
		
		}
				
		
		gData.sqlReq.saveResult(recoverySql);
		
	}
	
	
	private void checkUsedPercentWorkProcesses() {

		appservers.clear();

		String maxUsedPercent = gData.commonParams.containsKey("AbapWorkProcessUsedPercentLimit")
				? gData.commonParams.get("AbapWorkProcessUsedPercentLimit")
				: "50";
		String SQL = readFrom_sql_text(this.getClass().getSimpleName(), "check_used_percent_wp");
		SQL = SQL.replace("!MAX_USED_PERCENT!", maxUsedPercent);
		List<String> newAlertsListSql = new ArrayList<String>();

		gData.truncateLog(params.get("job_name"));
		int counter = 0;

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		String insSQL = "";

		for (Map<String, String> rec : records) {

			counter++;

			String details = rec.get("short") + "::";
			details += rec.get("def_ip") + "::";
			details += rec.get("sysnr") + "::";
			details += rec.get("app_server") + "::";
			details += rec.get("wp_typ") ;

			String message = "total:" + rec.get("total_wp") + "-free:" + rec.get("free_wp");

			gData.saveToLog(counter + ")" + details + " " + message, params.get("job_name"));

			if (rec.get("action").equals("alert")) {

				if (!checkIfAlertAlreadyExists(rec.get("object_guid"), currMonitorNumber, details)) {

					insSQL = "insert into `problems` (";
					insSQL += "`guid`,`object_guid`,`details`,`monitor_number`,`result_number`,";
					insSQL += "`value_limit`,`last_check_date`,";
					insSQL += "`is_mailed`,`mailed`,`is_last_mailing`,`last_mailing`";
					insSQL += ") values (";
					insSQL += "'" + gData.getRandomUUID() + "',";
					insSQL += "'" + rec.get("object_guid") + "',";
					insSQL += "'" + details + "',";
					insSQL += "" + currMonitorNumber + ",";
					insSQL += "" + rec.get("used_percent") + ",";
					insSQL += "" + maxUsedPercent + ",";
					insSQL += "'" + rec.get("last_check_date") + "',";
					insSQL += "'X',now(),'X',now()";
					insSQL += ")";

					gData.saveToLog(insSQL, params.get("job_name"));

					newAlertsListSql.add(insSQL);

					appservers.put(rec.get("object_guid") + ":" + rec.get("app_server"),
							details + " used :" + rec.get("used_percent") + "% limit:" + maxUsedPercent);

				}

			}
		}

		gData.sqlReq.saveResult(newAlertsListSql);

	}


	private void sendLetters() {
		if (appservers.size() < 1)
			return;
		
		gData.saveToLog("servers for letter=" + appservers.size(), params.get("job_name"));
		
		List<String> object_guids = new ArrayList<>();
		String SQL = "";
		String body = "";
		String commonSubjectLetter = "Problem in ABAP:";
		String port = gData.commonParams.get("webServicePort");
		String ip = gData.getOwnIp();
		
		body += "<STYLE>" + getTableStyle() + "</STYLE>";
		
		
		for (Map.Entry<String, String> entry : appservers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			String[] parts = key.split(":");

			String objectGuid = parts[0];
			String appserver = (parts.length==2) ? parts[1]:"";
			
			commonSubjectLetter += appserver + " ";		//заголовок письма
			
			object_guids.add(objectGuid);

			SQL = "SELECT * ,TIMESTAMPDIFF(MINUTE,check_date,NOW()) AS 'past_minutes' ";
			SQL += " FROM monitor_abap_wp WHERE check_date = (SELECT MAX(check_date) FROM monitor_abap_wp) ";
			SQL += " AND object_guid='" + objectGuid + "' AND app_server='" + appserver + "' ";
			SQL += " ORDER BY wp_index ";

			
			gData.saveToLog(SQL, params.get("job_name"));


			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

			body += "<p style='color:red;'>" + value + "</p>";
			String timeString = "";
			String pastMinutes = "";


			body += "<a href='https://" + ip + ":" + port + "/dashboard?guid=";
			body += objectGuid + "'>";						
			body += appserver;
			body += "</a> ";


			body += "<table border=1>";
			body += "<thead>";
			body += "<tr>";
			body += "<th>wp_index</th>";
			body += "<th>wp_typ</th>";
			body += "<th>wp_pid</th>";			
			body += "<th>wp_status</th>";			
			body += "<th>wp_dumps</th>";			
			body += "<th>wp_mandt</th>";
			body += "<th>wp_bname</th>";			
			body += "<th>wp_report</th>";			
			body += "<th>wp_action</th>";
			body += "<th>wp_table</th>";			
			body += "</tr>";			
			body += "</thead>";			

			body += "<tbody>";				
						
			for (Map<String, String> rec : records) {
				
				
				body += "<tr>";
				body += "<td>" + rec.get("wp_index") + "</td>";
				body += "<td>" + rec.get("wp_typ") + "</td>";
				body += "<td>" + rec.get("wp_pid") + "</td>";
				body += "<td>" + rec.get("wp_status") + "</td>";
				body += "<td>" + rec.get("wp_dumps") + "</td>";
				body += "<td>" + rec.get("wp_mandt") + "</td>";
				body += "<td>" + rec.get("wp_bname") + "</td>";
				body += "<td>" + rec.get("wp_report") + "</td>";
				body += "<td>" + rec.get("wp_action") + "</td>";
				body += "<td>" + rec.get("wp_table") + "</td>";
				body += "</tr>";
				
				timeString = rec.get("check_date");
				pastMinutes = rec.get("past_minutes");
				
				
	
			}
			body += "</tbody>";
			body += "</table>";


			timeString = "<p style='color:black;'>по состоянию на: " + timeString + "</p>";
			timeString += "<p style='color:black;'>" + Utils.timeConvert(Integer.valueOf(pastMinutes)) + " минут назад.</p>";
	
			body +=  timeString ;


		}

		gData.saveToLog(body, params.get("job_name"));

			MSEcxchange me = new MSEcxchange(gData);

			List<String> recepientsList = readRecepientsByProjects(object_guids);

			String recepientsAll = "";
			for (String s : recepientsList) {
				recepientsAll += s + ";";
			}
			
			
			gData.saveToLog(">Письмо:" + recepientsAll + " " + commonSubjectLetter + " " + body , params.get("job_name"));

			if (gData.commonParams.containsKey("mailSending")) {
				if (gData.commonParams.get("mailSending").equals("true")) {

					me.sendOneLetter(recepientsAll, commonSubjectLetter, body);

				} else {
					gData.logger.info("MailNotificator is disallowed...");
				}
			}

	}

	private String getTableStyle() {
		String out = "";


		out += "table {";
		out += "font-size: 65%; ";
		out += "font-family: Verdana, Arial, Helvetica, sans-serif; ";
		out += "border: 1px solid #399; ";
		out += "border-spacing: 1px 1px; ";
		out += "}";
		out += "td {";
		out += "background: #AAA;";
		out += "border: 1px solid #333;";
		out += "padding: 1px; ";
		out += "}";

		return out;
	}

}
