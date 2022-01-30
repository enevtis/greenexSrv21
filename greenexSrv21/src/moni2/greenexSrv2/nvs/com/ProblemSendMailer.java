package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;

public class ProblemSendMailer extends BatchJobTemplate implements Runnable {


	public ProblemSendMailer(globalData gData, Map<String, String> params) {
		super(gData,params);
	}

	@Override
	public void run() {
		try {
			
			
			currJobName = params.get("job_name");
			gData.truncateLog(currJobName);
			
			setRunningFlag_regular();
			
			analyze();
			
			reSetRunningFlag_regular();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.saveToLog(errors.toString(), currJobName);	
			gData.logger.severe(errors.toString());
		}
		
	}

	private void analyze() {

		
		gData.saveToLog("begin", currJobName);
		
		sendLettersForNewProblems();
		sendLettersForRecoveredProblems();
		gData.saveToLog("end", currJobName);		
		
		
	}

	protected void sendLettersForNewProblems() {
	

		String SQL = "";
		SQL += "SELECT a.*,b.short,c.job_descr,c.job_descr_guid FROM problems a ";
		SQL += "LEFT JOIN v_objects_all b ON a.object_guid = b.guid ";
		SQL += "LEFT JOIN monitor_schedule c ON a.monitor_number = c.number ";
		SQL += "WHERE is_mailed <> 'X' AND is_fixed <> 'X'";
		
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
		List<String> object_guids = new ArrayList<>();

		String port = gData.commonParams.get("webServicePort");
		String ip = gData.getOwnIp();
		
		int counter = 0;

		String subjectLetter = "Problem:";
		
		String bodyLetter = "";
		String style = "";

		bodyLetter = "<ul>";


				for (Map<String, String> rec : records_list) {

						counter++;
						object_guids.add(rec.get("object_guid"));

						subjectLetter += rec.get("short") + ",";
						style = "color:red;";
						bodyLetter += "<li style='" + style + "'>";
						
						bodyLetter += "<a href='https://" + ip + ":" + port + "/dashboard?guid=";
						bodyLetter += rec.get("object_guid") + "'>";						
						bodyLetter += rec.get("short");
						bodyLetter += "</a> ";						

						bodyLetter += rec.get("details") + " ";
//						bodyLetter += rec.get("job_descr") + " ";


						bodyLetter += gData.tr(rec.get("job_descr_guid")) + " ";
						bodyLetter += gData.tr("df3298d7-c6ac-4d3b-bb71-0880538bdd55") + ": " + rec.get("result_number") + " ";
						bodyLetter += gData.tr("51dea9da-58ba-4796-baa2-307763c167c8") + " " + rec.get("value_limit") + " ";

						
//						bodyLetter += "превышение : " + rec.get("result_number") + " ";
//						bodyLetter += "лимит " + rec.get("value_limit") + " ";

						bodyLetter += "<br> <i>" + rec.get("description") + "</i> ";
						
						String updSQL = "update `problems` set `is_mailed`= 'X',  mailed=NOW()";
						updSQL += " where guid='" + rec.get("guid") + "'";

						gData.sqlReq.saveResult(updSQL);

					}


		bodyLetter += "</ul>";

		////////// если есть записи , отправляем письмо.

		if (counter > 0) {
			sendMsExchageLetter(object_guids, subjectLetter, bodyLetter);
		}

	}
	protected void sendLettersForRecoveredProblems() {
		
		
		String SQL = "";
		SQL += "SELECT a.*,b.short,c.job_descr,c.job_descr_guid FROM problems a ";
		SQL += "LEFT JOIN v_objects_all b ON a.object_guid = b.guid ";
		SQL += "LEFT JOIN monitor_schedule c ON a.monitor_number = c.number ";
		SQL += "WHERE is_mailed = 'X' AND is_fixed = 'X' AND is_recovery_mailing <> 'X'";	
		
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
		List<String> object_guids = new ArrayList<>();

		String port = gData.commonParams.get("webServicePort");
		String ip = gData.getOwnIp();
		
		int counter = 0;

		String subjectLetter = "Recovery:";
		
		String bodyLetter = "";
		String style = "";

		bodyLetter = "<ul>";

				for (Map<String, String> rec : records_list) {

					counter++;
					object_guids.add(rec.get("object_guid"));

					subjectLetter += rec.get("short") + ",";
					style = "color:green;";
					bodyLetter += "<li style='" + style + "'>";
					
					bodyLetter += "<a href='https://" + ip + ":" + port + "/dashboard?guid=";
					bodyLetter += rec.get("object_guid") + "'>";						
					bodyLetter += rec.get("short");
					bodyLetter += "</a> ";						
					bodyLetter += rec.get("details") + " ";
//					bodyLetter += rec.get("job_descr") + " ";
					bodyLetter += gData.tr(rec.get("job_descr_guid")) + " ";
					
					bodyLetter += gData.tr("f35b8f86-ec8b-44a7-a197-e86181bb36d1") + ": " + rec.get("fixed_result") + " ";
					
//					bodyLetter += "новое значение: " + rec.get("fixed_result") + " ";

					bodyLetter += gData.tr("51dea9da-58ba-4796-baa2-307763c167c8") + ": " + rec.get("fixed_limit") + " ";

					String updSQL = "update `problems` set `is_last_mailing`='X',last_mailing=NOW(),";
					updSQL += "is_recovery_mailing ='X',recovery_mailing=NOW() ";
					updSQL += " where guid='" + rec.get("guid") + "'";

					gData.sqlReq.saveResult(updSQL);

					}


		bodyLetter += "</ul>";
		bodyLetter += Utils.traceClassInMail(this.getClass().getSimpleName());
		
		////////// если есть записи , отправляем письмо.

		if (counter > 0) {
			sendMsExchageLetter(object_guids, subjectLetter, bodyLetter);
		}
		
	}
	
	public void sendMsExchageLetter(List<String> object_guids, String subjectLetter, String bodyLetter) {
		
		MSEcxchange me = new MSEcxchange(gData);

		List<String> recepients = readRecepientsByProjects(object_guids);

		String recepientsAll = "";
		for (String s : recepients) {
			recepientsAll += s + ";";
		}
		
		
		
		if (subjectLetter.length() > 125) subjectLetter = subjectLetter.substring(0,125) + gData.tr("6a16e3b0-7d27-4d5d-8cbb-7c2c6b6a2b19") + " ...";
		
		gData.saveToLog("Письмо:" + recepientsAll + " " + subjectLetter + " " + bodyLetter, currJobName);

		if (gData.commonParams.containsKey("mailSending")) {
			if (gData.commonParams.get("mailSending").equals("true")) {

				me.sendOneLetter2(recepients, subjectLetter, this.getClass().getSimpleName() + " " +bodyLetter);

			} else {
				gData.logger.info("MailNotificator is disallowed...");
			}
		}

		
	}
	
}
