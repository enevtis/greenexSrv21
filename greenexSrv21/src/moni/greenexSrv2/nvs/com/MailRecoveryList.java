package moni.greenexSrv2.nvs.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class MailRecoveryList implements Runnable {
	public globalData gData = new globalData();
	public List<String> recoverySQLList = new ArrayList<String>();
	public List<String> recoveryLetterList = new ArrayList<String>();
	
	public List<Map<String , String>> lettersList = new ArrayList<Map<String,String>>();

	public MailRecoveryList (globalData gData) {
		this.gData = gData;
	}

	@Override
	public void run() {
		try {	
			
//			lettersList.clear();
			recoverySQLList.clear();
			recoveryLetterList.clear();
			


			getRecoveryLetterString();
			sendRecoveryLetter();
			deleteInactiveAlerts();
			


		} catch(Exception e) {
			gData.logger.info("Error - begin new time");
		}		
		
	}
	public void deleteInactiveAlerts() {
		if (recoverySQLList == null ) return;
		if (recoverySQLList.size() == 0 ) return;		
		gData.sqlReq.saveResult(recoverySQLList);	
	
	}
	
	
	public void sendRecoveryLetter(){
		if (recoveryLetterList == null ) return;
		if (recoveryLetterList.size() == 0 ) return;

		if (! gData.allowEmailSending ) {			
			gData.logger.info("MS Exchange letter sending is not allowed. There is no parameter allowEmailSending = true in params.properties. Letter ignored");
			return;
		}



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
				
				SubjectLetter = "Greenex recovery on " + gData.getOwnHostname();
				
				BodyLetter += "<table border=1> ";

				for (String oneRecoveryLine: recoveryLetterList){				
					BodyLetter += oneRecoveryLine;				
				}
				BodyLetter += "</table> ";
				
				
				gData.logger.info(recepientsAll + " " + SubjectLetter + " " + BodyLetter);

				MSEcxchange ms = new MSEcxchange(gData);
				ms.sendOneLetter( recepientsAll, SubjectLetter, BodyLetter );




	}
	
	public void getRecoveryLetterString() {
		
		String SQL = "";
		SQL += "select a.*,b.short,b.obj_typ, c.job_name, c.job_descr \n";
		SQL += "from monitor_errors a  \n";
		SQL += "left join v_objects_all b on a.object_guid = b.guid \n";
		SQL += "left join monitor_schedule c on a.monitor_number = c.number \n";
		SQL += "where a.active = '' and a.monitor_number in (201,101,102,202,203,204,205,106,207) \n";
		
		List<Map<String , String>> records_analyze  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL);


		
		if (records_analyze != null ) {
				String buffer = "<tr>";
				buffer += "<td>" + "object" + "</td>";


				for (Map<String, String> recA : records_analyze) {
				String cId = recA.get("id");
				
				recoverySQLList.add("delete from monitor_errors where id=" + cId);

				String buffer2 = "<tr>";
 				buffer2 +=  "<td>" + recA.get("id") + "</td>";
 				buffer2 +=  "<td>" + recA.get("short") + "</td>";
 				buffer2 +=  "<td>" + recA.get("job_descr") + "</td>"; 
 				buffer2 +=  "<td>" + recA.get("result_text") + "</td>"; 


   				recoveryLetterList.add(buffer2);			
						
				}

			}

	
	
	
	
	
	
	
	}
	
}
