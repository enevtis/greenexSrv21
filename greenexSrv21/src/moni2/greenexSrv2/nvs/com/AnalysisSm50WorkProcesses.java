package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class AnalysisSm50WorkProcesses extends BatchJobTemplate implements Runnable {

	public String currMonitorNumber = "303";

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

//		checkNewAlerts();
//		checkStatusOldAlerts();
//		sendLetters();

	}

	private void checkUsedPercentWorkProcesses() {

		String maxUsedPercent = gData.commonParams.containsKey("AbapWorkProcessUsedPercentLimit")
				? gData.commonParams.get("AbapWorkProcessUsedPercentLimit")
				: "50";
		String SQL = readFrom_sql_text(this.getClass().getSimpleName(), "check_used_percent_wp");
		SQL = SQL.replace("!MAX_USED_PERCENT!", maxUsedPercent);
		List<String> newAlertsListSql = new ArrayList<String>();
//		gData.saveToLog(SQL, params.get("job_name"));

		gData.truncateLog(params.get("job_name"));
		int counter = 0;

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		String insSQL = "";
		
		for (Map<String, String> rec : records) {

			counter++;

			String details = rec.get("short") + "_";
			details += rec.get("def_ip") + "_";
			details += rec.get("sysnr") + "_";
			details += rec.get("app_server") + "_";
			details += rec.get("wp_typ") + "_";

			String message = "total:" + rec.get("total_wp") + "-free:" + rec.get("free_wp");

			gData.saveToLog(counter + ")" + details + " " + message, params.get("job_name"));

			if (rec.get("action").equals("alert")) {

				if (!checkIfAlertAlreadyExists(rec.get("object_guid"), currMonitorNumber, details)) {

					insSQL = "insert into `problems` (";
					insSQL += "`guid`,`object_guid`,`details`,`monitor_number`,`result_number`,";
					insSQL += "`value_limit`,`last_check_date`";
					insSQL += ") values (";
					insSQL += "'" + gData.getRandomUUID() + "',";
					insSQL += "'" + rec.get("object_guid") + "',";
					insSQL += "'" + details + "',";
					insSQL += "" + currMonitorNumber + ",";
					insSQL += "" + rec.get("used_percent") + ",";
					insSQL += "" + maxUsedPercent + ",";
					insSQL += "'" + rec.get("last_check_date") + "'";
					insSQL += ")";

					gData.saveToLog(insSQL, params.get("job_name"));

					newAlertsListSql.add(insSQL);

				}

			}
		}

		gData.sqlReq.saveResult(newAlertsListSql);

	}

	private void checkNewAlerts() {

		String SQL = "SELECT object_guid,app_server FROM monitor_abap_wp GROUP BY object_guid,app_server";
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		List<String> newAlertsListSql = new ArrayList<String>();

		for (Map<String, String> rec : records) {
			checkOneSystem(rec.get("object_guid"), rec.get("app_server"), newAlertsListSql);
		}
		gData.sqlReq.saveResult(newAlertsListSql);

	}

	private void checkOneSystem(String objectGuid, String appServer, List<String> newAlertsListSql) {

		String SQL = readFrom_sql_text(this.getClass().getSimpleName(), "check_free_wp");

		String freeLimitAbapWpPercent = gData.commonParams.containsKey("AbapWorkProcessFreeLimitPercent")
				? gData.commonParams.get("AbapWorkProcessFreeLimitPercent")
				: "50";

		SQL = SQL.replace("!LIMIT_FREE_WP_PERCENT!", freeLimitAbapWpPercent);
		SQL = SQL.replace("!OBJECT_GUID!", objectGuid);
		SQL = SQL.replace("!APP_SERVER!", appServer);

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		gData.saveToLog(SQL, params.get("job_name"));

		for (Map<String, String> rec : records) {
			String message = "";
			message += objectGuid + " " + appServer + " " + rec.get("wp_typ") + " " + rec.get("total_wp") + " "
					+ rec.get("free_wp") + " " + rec.get("free_percent") + " ";
			gData.saveToLog(message, params.get("job_name"));

			if (rec.get("action").equals("alert")) {
				String insSQL = "";

				String details = appServer + "_" + rec.get("wp_typ");

				insSQL += "insert into `problems` (";
				insSQL += "`guid`,`object_guid`,`details`,`monitor_number`,`result_number`,";
				insSQL += "`value_limit`,`last_check_date`";
				insSQL += ") values (";
				insSQL += "'" + gData.getRandomUUID() + "',";
				insSQL += "'" + rec.get("object_guid") + "',";
				insSQL += "'" + details + "',";
				insSQL += "" + params.get("monitor_number") + ",";
				insSQL += "" + rec.get("free_percent") + ",";
				insSQL += "" + freeLimitAbapWpPercent + ",";
				insSQL += "'" + rec.get("last_check_date") + "'";
				insSQL += ")";

				gData.saveToLog(insSQL, params.get("job_name"));

				newAlertsListSql.add(insSQL);

			}

		}

	}

	private void checkStatusOldAlerts() {

	}

	private void sendLetters() {

	}

}
