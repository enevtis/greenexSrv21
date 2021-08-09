package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.globalData;

public class AnalyzeScan extends BatchJobTemplate implements Runnable {


	public AnalyzeScan(globalData gData, Map<String, String> params) {
		super(gData,params);
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

		currJobName = params.get("job_name");
		
		gData.truncateLog(currJobName);
	
		checkNewAlerts();
		checkStatusOldAlerts();
		sendLetters();
		resetStuckStarts();
		clearOldFixedProblems();

	}

	protected void clearOldFixedProblems() {
		
		String SQL = "SELECT * FROM problems WHERE is_fixed = 'X' AND is_recovery_mailing = 'X' AND timestampdiff(HOUR,is_recovery_mailing,NOW()) > 3 ";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		List<String> updSql = new ArrayList<>();
		
		for (Map<String, String> rec : records) {
			
			String SQLdel = "delete from problems where id=" + rec.get("id");
			updSql.add(SQLdel);
			gData.saveToLog(SQLdel,this.currJobName);
			

		}
		
		gData.sqlReq.saveResult(updSql);
		
		
	}
	
	
	protected void resetStuckStarts() {
		
		String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"reset_stuck_starts");
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		List<String> updSql = new ArrayList<>();
		
		for (Map<String, String> rec : records) {
			
			String action = (rec.get("action") == null) ? "" : rec.get("action");
			
			if (action.equals("alert")) {
			
				updSql.add("update monitor_schedule set running = ' ', "
						+ "running_errors = running_errors+1  where id=" + rec.get("id"));
				String message = rec.get("job_name") + " has been reseted after running " + rec.get("past_min");

				gData.saveToLog(message,params.get("job_name"));
				
			}
		}
		
		gData.sqlReq.saveResult(updSql);
		
	}
	
	
	protected void sendLetters() {
		String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"process_alerts");
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
		List<String> object_guids = new ArrayList<>();

		String port = gData.commonParams.get("webServicePort");
		String ip = gData.getOwnIp();
		
		int newAlerts = 0;
		int recoversOld = 0;
		String subjectLetterRecovery = "Recovery:";
		String subjectLetterProblem = "Problem:";
		
		String bodyLetter = "";
		String style = "";

		bodyLetter = "<ul>";

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					String action = (rec.get("action") == null) ? "" : rec.get("action");

					if (action.equals("recover_letter")) {
						recoversOld++;
						object_guids.add(rec.get("object_guid"));

						subjectLetterRecovery += rec.get("short") + ",";
						style = "color:green;";
						bodyLetter += "<li style='" + style + "'>";
						
						bodyLetter += "<a href='https://" + ip + ":" + port + "/dashboard?guid=";
						bodyLetter += rec.get("object_guid") + "'>";						
						bodyLetter += rec.get("short");
						bodyLetter += "</a> ";						
						bodyLetter += rec.get("details") + " ";
						bodyLetter += rec.get("job_descr") + " ";
						bodyLetter += "новое значение: " + rec.get("fixed_result") + " ";
						bodyLetter += "лимит " + rec.get("fixed_limit") + " ";

						String updSQL = "update `problems` set `is_last_mailing`='X',last_mailing=NOW(),";
						updSQL += "is_recovery_mailing ='X',recovery_mailing=NOW() ";
						updSQL += " where guid='" + rec.get("guid") + "'";

						gData.sqlReq.saveResult(updSQL);

					} else if (action.equals("create_alert")) {
						newAlerts++;
						object_guids.add(rec.get("object_guid"));

						subjectLetterProblem += rec.get("short") + ",";
						style = "color:red;";
						bodyLetter += "<li style='" + style + "'>";
						
						bodyLetter += "<a href='https://" + ip + ":" + port + "/dashboard?guid=";
						bodyLetter += rec.get("object_guid") + "'>";						
						bodyLetter += rec.get("short");
						bodyLetter += "</a> ";						

						bodyLetter += rec.get("details") + " ";
						bodyLetter += rec.get("job_descr") + " ";
						bodyLetter += "превышение : " + rec.get("result_number") + " ";
						bodyLetter += "лимит " + rec.get("value_limit") + " ";
						bodyLetter += "<br> <i>" + rec.get("description") + "</i> ";
						
						String updSQL = "update `problems` set `is_mailed`= 'X',  mailed=NOW()";
						updSQL += " where guid='" + rec.get("guid") + "'";

						gData.sqlReq.saveResult(updSQL);

					}

				}
			}
		}

		bodyLetter += "</ul>";

		////////// если есть записи , отправляем письмо.

		if ((newAlerts + recoversOld) > 0) {

			MSEcxchange me = new MSEcxchange(gData);

			List<String> recepientsList = readRecepientsByProjects(object_guids);

			String recepientsAll = "";
			for (String s : recepientsList) {
				recepientsAll += s + ";";
			}
			
			String commonSubjectLetter = "";
			if (subjectLetterRecovery.length() > 9) commonSubjectLetter += subjectLetterRecovery;
			if (subjectLetterProblem.length() > 8) commonSubjectLetter += subjectLetterProblem;
			
			
			if (commonSubjectLetter.length() > 125) commonSubjectLetter = commonSubjectLetter.substring(0,125) + " и другие ...";
			
			
			gData.logger.info(
					"<p style='color:blue;'>Письмо:" + recepientsAll + " " + commonSubjectLetter + " " + bodyLetter + "</p>");

			if (gData.commonParams.containsKey("mailSending")) {
				if (gData.commonParams.get("mailSending").equals("true")) {

					me.sendOneLetter(recepientsAll, commonSubjectLetter, bodyLetter);

				} else {
					gData.logger.info("MailNotificator is disallowed...");
				}
			}

		}

	}



	protected void checkStatusOldAlerts() {

		String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"analyze_old_problems");
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					if (rec.get("action").equals("recover")) {

						String updSQL = "";

						updSQL += "update `problems` set `is_fixed`='X', ";
						updSQL += "`fixed_result`=" + rec.get("new_result_number") + ",";
						updSQL += "`fixed_limit`=" + rec.get("new_value_limit") + ",";						
						updSQL += "`fixed`=NOW() where guid='" + rec.get("guid") + "'";

						gData.sqlReq.saveResult(updSQL);

					}

				}
			}
		}

	}

	protected void checkNewAlerts() {

		String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"analyze_monitor_results");

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		

		
		
		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					if (rec.get("is_alert").equals("alert")) {

						if (!checkIfAlertAlreadyExists(rec.get("object_guid"), rec.get("monitor_number"),
								rec.get("details"))) {

							String resultText = rec.get("result_text") == null ? "": rec.get("result_text");
							String insSQL = "";

							insSQL += "insert into `problems` (";
							insSQL += "`guid`,`object_guid`,`monitor_number`,`result_number`,";
							insSQL += "`value_limit`,`last_check_date`,`description`,`details`";
							insSQL += ") values (";
							insSQL += "'" + gData.getRandomUUID() + "',";
							insSQL += "'" + rec.get("object_guid") + "',";
							insSQL += "" + rec.get("monitor_number") + ",";
							insSQL += "" + rec.get("result_number") + ",";
							insSQL += "" + rec.get("value_limit") + ",";
							insSQL += "'" + rec.get("last_check_date") + "',";
							insSQL += "'" + resultText  + "',";
							
							String details = rec.get("result_text") == null ?  "": rec.get("result_text");
							
							insSQL += "'" + details.trim() + "'";
							insSQL += ")";

							gData.sqlReq.saveResult(insSQL);

						}
					}

				}
			}
		}

	}


}
