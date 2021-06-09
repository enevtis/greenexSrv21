package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.globalData;

public class AnalyzeScanOracleTs extends BatchJobTemplate implements Runnable{

	public String monitor_number = "220";
	
	public AnalyzeScanOracleTs(globalData gData) {
		super(gData,null);
	}

	@Override
	public void run() {
		try {

			analyze();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
	}
	private void analyze() {

		checkNewAlerts();
		sendNewProblemLetters();
		fixOldProblems();
//		checkStatusOldAlerts();
//		sendLetters();

	}
	
	
	protected void fixOldProblems() {
		
		String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"fixed_problems");
		
		SQL = SQL.replace("!LIMIT_FREE_PERCENT!",gData.commonParams.get("OracleTablespaceFreeLimitPercent"));
		gData.saveToLog(SQL, this.getClass().getSimpleName(), false);
		
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		for (Map<String, String> rec : records) {

			if (rec.get("action").contains("recovery")) {
				
				gData.saveToLog(rec.get("action") + " " + rec.get("description") + " " + rec.get("short"), this.getClass().getSimpleName(), true);
				
				String[] newValues = rec.get("action").split("_");

				String SQL1 = "";

				SQL1 += "update `problems` set  ";
				SQL1 += "is_fixed='X', ";
				SQL1 += "fixed=now(), ";					
				SQL1 += "fixed_result=" + newValues[1] + ", ";					
				SQL1 += "fixed_limit=" + newValues[2] + ", ";
				SQL1 += "description='limit=" + newValues[2] + "'";						
				SQL1 += " where id=" + rec.get("id");					

				gData.sqlReq.saveResult(SQL1);
				
				
				
				
				
				
			}
		}
		
		
	}
	
	
	protected void checkNewAlerts() {
		
		String message = "", result_number = "", value_limit = "";
		
		
		String SQL = read_from_sql_remote_check(this.getClass().getSimpleName(),"analyze_oracle_ts", "oracle");
		
		SQL = SQL.replace("!LIMIT_FREE_PERCENT!",gData.commonParams.get("OracleTablespaceFreeLimitPercent"));
	
		if (gData.debugMode) gData.saveToLog(SQL, this.getClass().getSimpleName(), false); 

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		gData.saveToLog(SQL, this.getClass().getSimpleName(), false);
		
		
				for (Map<String, String> rec : records_list) {

					if (rec.get("is_alert").contains("alert")) {
						
						if (rec.get("is_alert").contains("percent")) {
							
							result_number = rec.get("pct_free");
							value_limit = gData.commonParams.get("OracleTablespaceFreeLimitPercent");
							message = "free " + result_number + " % of tablespace is less than limit " + value_limit;
							
						} else {
							

							result_number = rec.get("max_free");
							value_limit = rec.get("min_free");;
							message = "free space " + result_number + " GB of tablespace is less than limit " + value_limit;
						}

						
//						gData.saveToLog(rec.get("short") + " " + rec.get("is_alert") + " " + message, this.getClass().getSimpleName(), true);
						
						
						if (!checkIfAlertAlreadyExists(rec.get("object_guid"), monitor_number,
								rec.get("tablespace_name"))) {
							
							
							String insSQL = "";

							insSQL += "insert into `problems` (";
							insSQL += "`guid`,";
							insSQL += "`object_guid`,";
							insSQL += "`details`,";
							insSQL += "`monitor_number`,";
							insSQL += "`last_check_date`,";
							insSQL += "`description`,";
							insSQL += "`result_number`,";
							insSQL += "`value_limit`";
							insSQL += ") values (";
							insSQL += "'" + gData.getRandomUUID() + "',";
							insSQL += "'" + rec.get("object_guid") + "',";
							insSQL += "'" + rec.get("tablespace_name") + "',";
							insSQL += "" + monitor_number + ",";
							insSQL += "'" + rec.get("check_date") + "',";
							insSQL += "'" + message + "',";	
							insSQL += "" + result_number + ",";
							insSQL += "" + value_limit ;
							insSQL += ")";

							gData.sqlReq.saveResult(insSQL);

						}
				
				}

		}
		

		
	}
public void sendNewProblemLetters() {
	

	String port = gData.commonParams.get("webServicePort");
	String ip = gData.getOwnIp();
	String style = "color:red;";	

	String subject = "Problem:";
	String bodyLetter = "";
	List<String> object_guids = new ArrayList<String>();
	List<String> updSqlList = new ArrayList<String>();	
	bodyLetter += "<ul style='" + style + "'>";
	boolean wasProblems = false;
	
	
//	subject += rec.get("short") + " ";
//	object_guids.add(rec.get("object_guid"));
//	problemCounter++;				
	String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"analyze_new_problems");
	
	List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
	
	for (Map<String, String> rec : records) {

		object_guids.add(rec.get("object_guid"));
		subject += rec.get("short") + ";";

		bodyLetter += "<li>";
		bodyLetter += "<a href='https://" + ip + ":" + port + "/dashboard?guid=";
		bodyLetter += rec.get("object_guid") + "'>";						
		bodyLetter += rec.get("short");
		bodyLetter += "</a> ";			
		bodyLetter += " " + rec.get("details");
		bodyLetter += " " + rec.get("description");		
		
			
		String updSQL = "";

		updSQL += "update `problems` set `is_mailed`='X', ";
		updSQL += "`mailed`=NOW() where guid='" + rec.get("guid") + "'";
		updSqlList.add(updSQL);
		wasProblems = true;
		
		
		
		
		
		

		
	}

	bodyLetter += "</ul>";	

	
	gData.sqlReq.saveResult(updSqlList);
			

	if (wasProblems ) {

		MSEcxchange me = new MSEcxchange(gData);

		List<String> recepientsList = readRecepientsByProjects(object_guids);

		String recepientsAll = "";
		for (String s : recepientsList) {
			recepientsAll += s + ";";
		}
		
		if (gData.debugMode) gData.saveToLog("SEND LETTER " + subject + " " + bodyLetter, this.getClass().getSimpleName(), true);
		

		if (gData.commonParams.containsKey("mailSending")) {
			if (gData.commonParams.get("mailSending").equals("true")) {

				me.sendOneLetter(recepientsAll, subject, bodyLetter);
				
				gData.saveToLog("SEND LETTER " + subject + " " + bodyLetter, this.getClass().getSimpleName(), true);
				

			} else {
				gData.logger.info("MailNotificator is disallowed...");
			}
		}

	}
}
}

//
//SELECT a.id, a.guid,a.object_guid,a.details,a.result_number,a.value_limit,a.description,
//b.max_free, b.pct_free ,c.min_free,
//CASE WHEN c.min_free IS NULL THEN CASE WHEN pct_free > 10 THEN  	
//	CONCAT('recovery_',b.pct_free,'_',10)	ELSE 'do_nothing' END
//  ELSE 
//	CASE WHEN max_free > min_free THEN 
//	CONCAT('recovery_',b.max_free,'_',c.min_free) 
//	ELSE 'do_nothing' END
//  END AS action 
//FROM  problems a
//LEFT JOIN monitor_oracle_ts b ON a.object_guid = b.object_guid AND a.details = b.tablespace_name
//LEFT JOIN monitor_oracle_ts_min c ON a.object_guid = c.object_guid AND a.details = c.tablespace_name
// WHERE a.monitor_number=220 AND is_fixed <> 'X'