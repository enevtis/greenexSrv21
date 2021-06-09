package moni.greenexSrv2.nvs.com;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;



public class MailNotificator implements Runnable {
	public globalData gData = new globalData();
	public List<String> recoveryList = new ArrayList<String>();
	public List<Map<String , String>> lettersList = new ArrayList<Map<String,String>>();

	public MailNotificator (globalData gData) {
		this.gData = gData;
	}
	
	@Override
	public void run() {
		
		try {	
			
			lettersList.clear();
			
			createAlertLetters();
			
			if (lettersList.size() < 5) {
				sendLetters();				
			} else {
				sendWarningLetterForAdmin();
			}		
			
			setTimeLettersIntoTable();
			
			lettersList.clear();
			
			createRecoveryLetters();


			if (lettersList.size() < 5) {
				sendLetters();				
			} else {
				
				sendWarningLetterForAdmin();
			}			
			
			gData.sqlReq.saveResult(recoveryList);

			gData.logger.info("MailNotificator2");

		} catch(Exception e) {
			gData.logger.info("Error - begin new time");
		}	
		
	}

	protected void setTimeLettersIntoTable(){

		List<String> sqlList = new ArrayList<String>();
		String SQL = "";
		
		for (Map<String, String> letter : lettersList) {

			SQL = "";
			SQL += "update monitor_errors set last_mail = now(), send_times = send_times + 1 " ;
			SQL += " where object_guid = '" + letter.get("object_guid") + "' and monitor_number = " + letter.get("monitor_number");
			
			sqlList.add(SQL);
		
		}
		
		gData.sqlReq.saveResult(sqlList);

	}


	protected void 	sendWarningLetterForAdmin() {

			String SQL_emails = "" ;	
			SQL_emails += "select email from recepients where project_guid = 'all'";
		
			String recepientsAll = "";
		
				List<Map<String , String>> recepients  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_emails);
		
					for (Map<String, String> recEmail : recepients) {
				
						recepientsAll += recEmail.get("email") + ";" ;
					}
				
				recepientsAll = recepientsAll.substring(0, recepientsAll.length() - 1);
				

				String SubjectLetter = "";
				String BodyLetter = "";
				
				
				for (Map<String, String> letter : lettersList) {		
				
					SubjectLetter = "Greenex " + lettersList.size() + " alerts on " + gData.getOwnHostname() ;
					BodyLetter += "<br> " + letter.get("Body");
					
					letter.put("result","true");
				}	
				
				
				
				MSEcxchange ms = new MSEcxchange(gData);
				ms.sendOneLetter( recepientsAll, SubjectLetter, BodyLetter );
		
		
		}

	protected void 	sendLetters() {
		
		if (lettersList.size() <1 ) {
			return;
		}



		for (Map<String, String> letter : lettersList) {

			String SQL_emails = "" ;	

			SQL_emails += "select distinct s1.email from \n";
			SQL_emails += "( select email from recepients where project_guid = '" + letter.get("Project") + "' \n";
			SQL_emails += "union  \n";
			SQL_emails += "select email from recepients where project_guid = 'all')  s1 \n";
		
			String recepientsAll = "";
		
				List<Map<String , String>> recepients  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_emails);
		
					for (Map<String, String> recEmail : recepients) {
				
						recepientsAll += recEmail.get("email") + ";" ;
					}
				
				recepientsAll = recepientsAll.substring(0, recepientsAll.length() - 1);
				
					MSEcxchange ms = new MSEcxchange(gData);
					ms.sendOneLetter( recepientsAll, letter.get("Subject"), letter.get("Body") );
				
				letter.put("result","true");
				
				
				;
		
		}	
		
		
		
		}


	
	
	


	
	

	protected void createAlertLetters() {
		
		
		if (! gData.allowEmailSending ) {
			
			gData.logger.info("MS Exchange letter sending is not allowed. There is no parameter allowEmailSending = true in params.properties. Letter ignored");
		}
		
		
		
		
		String SQL_alert = "" ;
		
		SQL_alert += "SELECT a.*, e.short, e.obj_typ, f.job_descr, \n";
		SQL_alert += "CASE \n";
		SQL_alert += "  WHEN b.project_guid is not null THEN b.project_guid  \n";
		SQL_alert += "  WHEN c.project_guid is not null THEN c.project_guid  \n";
		SQL_alert += "  WHEN d.project_guid is not null THEN d.project_guid   \n";
		SQL_alert += " END as 'project_guid' , \n";
		SQL_alert += " CASE \n";
		SQL_alert += "  WHEN a.last_mail is null THEN 'send_letter'  \n";
		SQL_alert += "  WHEN TIMESTAMPDIFF(MINUTE,a.last_mail, now()) > " + gData.periodBetweenMailMinutes + " THEN 'send_letter'  \n";
		SQL_alert += "  ELSE 'nothing'  \n";
		SQL_alert += "END as 'analyze' \n";
		SQL_alert += "FROM monitor_errors a  \n";
		SQL_alert += "left join servers b on a.object_guid = b.guid \n";
		SQL_alert += "left join db_systems c on a.object_guid = c.guid \n";
		SQL_alert += "left join app_systems d on a.object_guid = d.guid \n";
		SQL_alert += "left join v_objects_all e on a.object_guid = e.guid \n";
		SQL_alert += "left join monitor_schedule f on a.monitor_number = f.number \n";
		SQL_alert += " where a.active = 'X' \n";


		
		
		
		List<Map<String , String>> records  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_alert);
			for (Map<String, String> rec : records) {
				String cParam = rec.get("analyze");
				
				if (cParam.contains("send_letter")) {
				
					Map<String,String> letter = new HashMap<String, String>();
					
					String SubjectLetter = "Greenex alert " + rec.get("short");
					
					String BodyLetter = "<table border=1>";
					
					BodyLetter += "<tr>";
					BodyLetter += "<td>";
					BodyLetter += rec.get("short") + " "+ rec.get("obj_typ");
					BodyLetter += "</td>";					

					BodyLetter += "<td>";
					BodyLetter += rec.get("job_descr");
					BodyLetter += "</td>";					

					BodyLetter += "<td>";
					BodyLetter += rec.get("result_text");
					BodyLetter += "</td>";						
					
					BodyLetter += "</tr>";
					BodyLetter += "</table>";					

					BodyLetter += "Object guid: " + rec.get("object_guid") + "<br>";
					BodyLetter += "Sender: Greenex system on " + gData.getOwnHostname() + "<br>";
					
					letter.put("object_guid", rec.get("object_guid"));
					letter.put("monitor_number", rec.get("monitor_number"));
					letter.put("Project", rec.get("project_guid"));
					letter.put("Subject", SubjectLetter);				
					letter.put("Body", BodyLetter);

					
					lettersList.add(letter);
				
				
				}
				
			}
			
		}



	protected void createRecoveryLetters() {
		
		
		if (! gData.allowEmailSending ) {
			
			gData.logger.info("MS Exchange letter sending is not allowed. There is no parameter MsExchangeSending=true in table user_settings. Letter ignored");
		}
		
		
		
		
		String SQL_alert = "" ;
		
		SQL_alert += "SELECT a.*, e.short, e.obj_typ, f.job_descr, \n";
		SQL_alert += "CASE \n";
		SQL_alert += "  WHEN b.project_guid is not null THEN b.project_guid  \n";
		SQL_alert += "  WHEN c.project_guid is not null THEN c.project_guid  \n";
		SQL_alert += "  WHEN d.project_guid is not null THEN d.project_guid   \n";
		SQL_alert += " END as 'project_guid' , \n";
		SQL_alert += " CASE \n";
		SQL_alert += "  WHEN a.last_mail is null THEN 'send_letter'  \n";
		SQL_alert += "  WHEN TIMESTAMPDIFF(MINUTE,a.last_mail, now()) > " + gData.periodBetweenMailMinutes + " THEN 'send_letter'  \n";
		SQL_alert += "  ELSE 'nothing'  \n";
		SQL_alert += "END as 'analyze' \n";
		SQL_alert += "FROM monitor_errors a  \n";
		SQL_alert += "left join servers b on a.object_guid = b.guid \n";
		SQL_alert += "left join db_systems c on a.object_guid = c.guid \n";
		SQL_alert += "left join app_systems d on a.object_guid = d.guid \n";
		SQL_alert += "left join v_objects_all e on a.object_guid = e.guid \n";
		SQL_alert += "left join monitor_schedule f on a.monitor_number = f.number \n";
		SQL_alert += " where a.active = 'R' \n";


		
		
		
		List<Map<String , String>> records  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_alert);
			for (Map<String, String> rec : records) {
				String cParam = rec.get("analyze");
				
				if (cParam.contains("send_letter")) {
				
					Map<String,String> letter = new HashMap<String, String>();
					
					String SubjectLetter = "Error " + rec.get("short");
					String BodyLetter = "Error " + rec.get("short") + " " + rec.get("obj_typ");
					
					BodyLetter +=  rec.get("job_descr") + " " + rec.get("result_text");
					BodyLetter +=  "<hr>" + rec.get("object_guid");
					
					letter.put("object_guid", rec.get("object_guid"));
					letter.put("monitor_number", rec.get("monitor_number"));
					letter.put("Project", rec.get("project_guid"));
					letter.put("Subject", SubjectLetter);				
					letter.put("Body", BodyLetter);

					
					lettersList.add(letter);
					
					String SQL_delete = "delete from monitor_errors where object_guid = '" + rec.get("object_guid") + "'";
					SQL_delete += " and monitor_number = " + rec.get("monitor_number");
					recoveryList.add(SQL_delete);
				
				
				}
				
			}
			
		}


}
