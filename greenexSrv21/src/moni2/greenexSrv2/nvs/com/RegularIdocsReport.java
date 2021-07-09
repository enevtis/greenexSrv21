package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;

public class RegularIdocsReport extends BatchJobTemplate implements Runnable {

	
	public RegularIdocsReport(globalData gData, Map<String, String> params) {
		super(gData,params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_regular();

			scanForReport();

			reSetRunningFlag_regular();
			

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}
	private void scanForReport() {

		gData.truncateLog(params.get("job_name"));
		String jobParameters = params.get("job_parameters");
		gData.saveToLog("AAA " + jobParameters, params.get("job_name"));
		String bodyLetter = getDataForAllSystems();
		sendRegularReportByEmail("Отчет IDOC", bodyLetter);
		
	}

	
	
	public String getDataForAllSystems() {
		String out = "";
		String SQL = "";
		
		SQL += "SELECT c.short AS 'project', b.short AS 'sap_system', b.sid, b.def_ip, s1.*, ";
		SQL += "TIMESTAMPDIFF(HOUR,s1.check_date_old,s1.check_date_new) AS 'past_hours' ";
		SQL += "FROM ( ";
		SQL += "SELECT object_guid, MAX(check_date) AS check_date_new, MIN(check_date) AS check_date_old FROM monitor_idocs ";
		SQL += "GROUP BY object_guid ) s1 ";
		SQL += "LEFT JOIN app_systems b ON s1.object_guid = b.guid ";
		SQL += "LEFT JOIN projects c ON b.project_guid = c.guid ";
		
		
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		
		for (Map<String, String> rec : records) {
			
			String objectGuid = rec.get("object_guid");
			String maxCheckDate = rec.get("check_date_new");
			String minCheckDate = rec.get("check_date_old");
			
			out += "<hr>";
			out += " Проект:" + rec.get("project") + "<br>";
			out += " SAP система:" + rec.get("sap_system") + "<br>";
			int minutes = Integer.valueOf(rec.get("past_hours")) * 60;	
			out += " интервал наблюдения " + Utils.timeConvert(minutes) + "<br>";
			out += "с " + rec.get("check_date_old") + " по " + rec.get("check_date_new") + "<br>";

			String link = "<a href='https://" + gData.getOwnIp() + ":" + gData.commonParams.get("webServicePort") ;
			link += "/idocs?guid=" + objectGuid + "'>подробнее</a><br>данные для входа demo/demo";
			out += link;								
			
			
			String SQL2 = "";
			SQL2 += "SELECT monthname(STR_TO_DATE(s1.month,'%m')) AS 'месяц', s1.year, ";
			SQL2 += "FORMAT(s1.total_new,0) AS 'количество_idoc', FORMAT(s2.total_old,0) AS 'старое значение', ";
			SQL2 += "FORMAT(s1.total_new - s2.total_old,0) AS 'рост', ";
			SQL2 += "TIMESTAMPDIFF(HOUR,s2.check_date_old,s1.check_date_new) AS 'за часов' ";
			SQL2 += "FROM ( ";
			SQL2 += "SELECT a.object_guid, b.sid, b.def_ip, b.short,a.year, a.month, ";
			SQL2 += "sum(a.total) AS total_new, a.check_date AS check_date_new FROM monitor_idocs a ";
			SQL2 += "LEFT JOIN app_systems b ON a.object_guid = b.guid ";
			SQL2 += "WHERE ABS(TIMESTAMPDIFF(MINUTE,check_date,'" + maxCheckDate + "')) < 5 ";
			SQL2 += "GROUP BY short, sid, def_ip, YEAR, MONTH ";
			SQL2 += "ORDER BY short,YEAR,MONTH ) s1 ";
			SQL2 += "LEFT JOIN  ";
			SQL2 += "( ";
			SQL2 += "SELECT a.object_guid, b.sid, b.def_ip, b.short,a.year, a.month,  ";
			SQL2 += "sum(a.total) AS total_old, a.check_date AS check_date_old FROM monitor_idocs a ";
			SQL2 += "LEFT JOIN app_systems b ON a.object_guid = b.guid ";
			SQL2 += "WHERE ABS(TIMESTAMPDIFF(MINUTE,check_date,'" + minCheckDate + "')) < 5 ";
			SQL2 += "GROUP BY short, sid, def_ip, YEAR, MONTH ";
			SQL2 += ") s2 ON s1.object_guid = s2.object_guid AND s1.year = s2.year AND s1.month = s2.month ";
			SQL2 += "WHERE s1.object_guid = '" + objectGuid + "' ";			
			
			out += Utils.getHtmlTablePageFromSqlreturn(gData, SQL2);
			
			

		}
		
		
		return out;
	}
	protected void sendRegularReportByEmail(String subjectLetter, String bodyLetter) {

		MSEcxchange me = new MSEcxchange(gData);
		
		List<String> recepientsList = readAdminRecepients(params.get("job_name"));
		String recepientsAll = "";
		for (String s : recepientsList) {
			recepientsAll += s + ";";
		}
	
		gData.saveToLog("Письмо:" + recepientsAll + " " + subjectLetter + " " + bodyLetter + "", params.get("job_name"));


		if (gData.commonParams.containsKey("mailSending")) {
			if (gData.commonParams.get("mailSending").equals("true")) {

				me.sendOneLetter(recepientsAll, subjectLetter, bodyLetter);

			} else {
				gData.logger.info("MailNotificator is disallowed...");
			}
		}

	}	

	protected List<String> readAdminRecepients(String filter) {
		List<String> out = new ArrayList();
		String SQL = "";

		SQL = "SELECT * FROM recepients WHERE filter = '" + filter + "'";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {
			out.add(rec.get("email"));
		}

		return out;
	}
}
