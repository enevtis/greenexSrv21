package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;

public class RegularReport extends BatchJobTemplate implements Runnable {

	public RegularReport(globalData gData, Map<String, String> params) {
		super(gData,params);
	}

	@Override
	public void run() {

		try {

			scanForReport();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	private void scanForReport() {

		String SQL = "";
		SQL += "SELECT TIMESTAMPDIFF(MINUTE, last_run_date, NOW()) AS past_min, ";
		SQL += "CASE WHEN TIMESTAMPDIFF(MINUTE, last_run_date, NOW()) > interval_min THEN 'start' ELSE 'pause' END AS 'action' ";
		SQL += "FROM regular_schedule a  ";
		SQL += "WHERE job_name = 'regular_report' ";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {

			if (rec.get("action").equals("start")) {

				createReport();

			}
		}

	}

	protected void createReport() {
		String SQL = "";

		List<Map<String, String>> records_list = null;
		String port = gData.commonParams.get("webServicePort");
		String ip = gData.getOwnIp();
		String repTxt = "";
		String strLink = "";

		SQL = readFrom_sql_text(this.getClass().getSimpleName(), "problems_for_regular_report");

		repTxt += "<table style='" + getStyle("table") + "'>";
		SQL = SQL.replace("!HOSTNAME!", ip);
		SQL = SQL.replace("!PORT!", port);

		
		records_list = gData.sqlReq.getSelect(SQL);
		
		repTxt += "<caption style='color:red;'><h3>" + "1.Актуальные проблемы" + "</h3></caption> ";
		repTxt += "<thead>";
		repTxt += "<tr style='" + getStyle("head_row") + "'>";

		repTxt += "<th>Имя объекта</th>";
		repTxt += "<th>Детали</th>";
		repTxt += "<th>Вид проверки</th>";
		repTxt += "<th>Параметр</th>";		
		repTxt += "<th>Лимит</th>";
		repTxt += "<th>Время возникновения</th>";
		repTxt += "<th>Прошло времени</th>";

		repTxt += "</tr>";
		repTxt += "</thead>";

		repTxt += "<tbody>";
		for (Map<String, String> rec : records_list) {

			repTxt += "<tr style='" + getStyle("row") + "'>";

			repTxt += "<td>" + rec.get("link") + "</td>";
			repTxt += "<td>" + rec.get("details") + "</td>";
			repTxt += "<td>" + rec.get("job_descr") + "</td>";
			repTxt += "<td>" + rec.get("result_number") + "</td>";
			repTxt += "<td>" + rec.get("value_limit") + "</td>";
			repTxt += "<td>" + rec.get("created") + "</td>";

			int pastHours = Integer.valueOf(rec.get("past_hours"));
			
			repTxt += "<td>" + Utils.timeConvert(pastHours * 60) + "</td>";			
			
			repTxt += "</tr>";

		}
			repTxt += "</tbody>";
			repTxt += "</table>";
			
			
		SQL = readFrom_sql_text(this.getClass().getSimpleName(), "regular_report");
		SQL = SQL.replace("!HOSTNAME!", ip);
		SQL = SQL.replace("!PORT!", port);

		records_list = gData.sqlReq.getSelect(SQL);

		repTxt += "<table style='" + getStyle("table") + "'>";

		repTxt += "<caption><h3>" + "2.Активные проверки" + "</h3></caption> ";
		repTxt += "<thead>";
		repTxt += "<tr style='" + getStyle("head_row") + "'>";

		repTxt += "<th>п/п</th>";
		repTxt += "<th>Вид проверки</th>";
		repTxt += "<th>Имя объекта</th>";
		repTxt += "<th>Проект</th>";
		repTxt += "<th>Время последней проверки</th>";
		repTxt += "<th>Прошло времени</th>";

		repTxt += "</tr>";
		repTxt += "</thead>";

		repTxt += "<tbody>";
		int rowCounter = 1;
		
		for (Map<String, String> rec : records_list) {

			repTxt += "<tr style='" + getStyle("row") + "'>";

			repTxt += "<td>" + rowCounter + "</td>";
			repTxt += "<td>" + rec.get("job_descr") + "</td>";
			repTxt += "<td>" + rec.get("link") + "</td>";
			repTxt += "<td>" + rec.get("short") + "</td>";
			repTxt += "<td>" + rec.get("last_check_date") + "</td>";
//			repTxt += "<td>" + rec.get("past_min") + "</td>";

			int pastMinuts = Integer.valueOf(rec.get("past_min"));
			
			repTxt += "<td>" + Utils.timeConvert(pastMinuts) + "</td>";			

			
			repTxt += "</tr>";
			
			rowCounter++;

		}
		
		repTxt += "</tbody>";
		repTxt += "</table>";

		String SQL2 = "update regular_schedule set `last_run_date` = NOW(), `counter`=`counter`+1 ";
		SQL2 += " where job_name='regular_report'";

		gData.sqlReq.saveResult(SQL2);

		String subjectLetter = "";
		subjectLetter += "Регулярный отчет Greenex от " + gData.getOwnHostname();
		subjectLetter += " (" + gData.getOwnIp() + ")";

		sendRegularReportByEmail(subjectLetter, repTxt);

	}

	protected void sendRegularReportByEmail(String subjectLetter, String bodyLetter) {

		MSEcxchange me = new MSEcxchange(gData);
		List<String> recepientsList = readAdminRecepients();
		String recepientsAll = "";
		for (String s : recepientsList) {
			recepientsAll += s + ";";
		}
	
		if (gData.debugMode) gData.logger.info(
				"<p style='color:blue;'>Письмо:" + recepientsAll + " " + subjectLetter + " " + bodyLetter + "</p>");

		if (gData.commonParams.containsKey("mailSending")) {
			if (gData.commonParams.get("mailSending").equals("true")) {

				me.sendOneLetter(recepientsAll, subjectLetter, bodyLetter);

			} else {
				gData.logger.info("MailNotificator is disallowed...");
			}
		}

	}

	protected String getStyle(String obj) {
		String out = "";
		switch (obj) {
		case "table":
			out += "font-family:'Courier New';";
			out += "background-color: #113603;";
			out += "border-spacing: 1px 1px;";

			break;

		case "head_row":
			out += "background-color: #235361;";

			break;
		case "row":
			out += "background-color: #dedede;";
			break;
		default:
			out += "font-family:'Courier New';";

			break;

		}
		return out;
	}
}
