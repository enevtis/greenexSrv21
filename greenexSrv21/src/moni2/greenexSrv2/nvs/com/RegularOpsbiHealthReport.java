package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;

public class RegularOpsbiHealthReport extends BatchJobTemplate implements Runnable {

		
		public RegularOpsbiHealthReport(globalData gData, Map<String, String> params) {
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
				gData.saveToLog(errors.toString(), params.get("job_name"));
			}

		}
		private void scanForReport() {

			gData.truncateLog(params.get("job_name"));
			String jobParameters = params.get("job_parameters");
			gData.saveToLog(this.getClass().getSimpleName() + jobParameters, params.get("job_name"));
			String bodyLetter = getOpsbiHealthReport();
			sendRegularReportByEmail("Отчет OPSBI Health", bodyLetter);
			
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

					me.sendOneLetter(recepientsAll, subjectLetter, this.getClass().getSimpleName() + " " + bodyLetter);

				} else {
					gData.logger.info("MailNotificator is disallowed...");
				}
			}

		}	

		protected List<String> readAdminRecepients(String filter) {
			List<String> out = new ArrayList();
			String SQL = "";

			SQL = "SELECT * FROM recepients WHERE `filter` = '" + filter + "' and `active`='X'";
			List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

			for (Map<String, String> rec : records_list) {
				out.add(rec.get("email"));
			}

			return out;
		}
		
		
		protected String getOpsbiHealthReport() {
			String out = "";

			String SQL1 = "SELECT * FROM servers WHERE  project_guid='f1743518-e021-4e63-9041-58978eebe89c' AND os_typ LIKE 'Windows%'";
			SQL1 += " ORDER BY role_typ";

			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL1);

			out += "<style>";
			out += getStyle();
			out += "</style>";

			out += "<table class='table1'>";

			out += "<thead><tr>";
			out += "<th>п/п</th>";
			out += "<th>ip</th>";
			out += "<th>hostname</th>";
			out += "<th>os</th>";
			out += "<th>role</th>";
			out += "<th>место на дисках</th>";
			out += "</tr></thead>";

			out += "<tbody>";

			int counter = 0;

			for (Map<String, String> rec : records) {
				counter++;
				out += "<tr>";
				out += "<td>" + counter + "</td>";
				out += "<td>" + rec.get("def_ip") + "</td>";
				out += "<td>" + rec.get("def_hostname") + "</td>";
				out += "<td>" + rec.get("os_typ") + "</td>";
				out += "<td>" + rec.get("role_typ") + "</td>";
				out += "<td>" + getOneSystemDisksTable(rec.get("guid")) + "</td>";

				out += "</tr>";

			}

			out += "</tbody>";
			out += "</table>";

			return out;
		}

		private String getOneSystemDisksTable(String guid) {
			String out = "";
			String style = "";
			String SQL = "";

			SQL += "SELECT s1.*, b.max_percent, ";
			SQL += "CASE WHEN max_percent IS NULL THEN  ";
			SQL += "CASE WHEN s1.used_percent > 85 THEN 'red' ELSE 'green' END ";
			SQL += "ELSE   ";
			SQL += "CASE WHEN s1.used_percent > max_percent THEN 'red' ELSE 'green' END ";
			SQL += "END AS 'color', TIMESTAMPDIFF(minute,s1.check_date,NOW()) AS 'past_minutes' ";
			SQL += "FROM ";
			SQL += "( ";
			SQL += "SELECT server_guid, `name`,`max_size_gb`, `used_size_gb`,`check_date`, ";
			SQL += "ROUND((`used_size_gb`/ `max_size_gb` * 100),1) AS used_percent ";
			SQL += "FROM monitor_disks  WHERE server_guid='" + guid + "' ";
			SQL += "AND check_date = (SELECT MAX(check_date) FROM monitor_disks WHERE server_guid='" + guid + "') ) s1 ";
			SQL += "LEFT JOIN monitor_disks_min b ON s1.server_guid = b.server_guid AND s1.`name` = b.`name` ";
			SQL += "ORDER BY s1.`name` ";

			out += "<table class='table2'>";

			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

			for (Map<String, String> rec : records) {

				out += "<tr>";
				out += "<td>" + rec.get("name") + "</td>";
		
				float usedPercent = Float.valueOf(rec.get("used_percent"));
				int indicatorWidth = 150;
				int leftPart = (int) (indicatorWidth * usedPercent / 100);
				int rightPart = indicatorWidth - leftPart;
			    
//				out += "<td>";		    
//			    out+= "<div style='float:left;background-color:" + gData.clrDiagramFill + ";width:" + leftPart + "px;height:10px;'>&nbsp;</div> ";
//		     	out+= "<div style='float:left;background-color:" + gData.clrDiagramBackground +";width:" + rightPart + "px;height:10px;'>&nbsp;</div> ";
//				out += "</td>";	

				out += "<td style='width:" + (indicatorWidth + 2) + "px;'>";		    
			    out+= "<div style='float:left;display:inline;background-color:" + gData.clrDiagramFill + ";width:" + leftPart + "px;height:10px;'>&nbsp;</div> ";
		     	out+= "<div style='float:left;display:inline;background-color:" + gData.clrDiagramBackground +";width:" + rightPart + "px;height:10px;'>&nbsp;</div> ";
				out += "</td>";	
				
				
				String buffer = gData.tr("1591c399-6dd7-4bac-91b8-cb38ce6d4b2a") + rec.get("used_percent") + " %";
				buffer += "(" + rec.get("used_size_gb") + " из ";
				buffer += rec.get("max_size_gb") + " Гб.) ";
				int pastMinutes = Integer.valueOf(rec.get("past_minutes"));
				
				buffer += gData.tr("84af12fb-090a-428d-8cf6-1331a5523151") + ": " + Utils.timeConvert(pastMinutes,gData.lang) + " " + gData.tr("d3ef97de-890b-4722-beb2-2811c225ae82");

				out += "<td style='color:" + rec.get("color") + ";'>" + buffer + "</td>";
				out += "</tr>";

			}

			out += "</table>";

			return out;
		}

		private String getStyle() {
			String out = "";

				out += ".table1 { \n";
				out += "font-size: 90%; ";
				out += "font-family: Verdana, Arial, Helvetica, sans-serif;  \n";
				out += "border: 1px solid #399;  \n";
				out += "border-spacing: 1px 1px;  \n";
				out += "} \n";
				out += ".table1 td { \n";
				out += "background: #EFEFEF; \n";
				out += "border: 1px solid #333; \n";
				out += "padding: 0px;  \n";
				out += "} \n";


				out += " .table2 { \n";
				out += "font-size: 70%;  \n";
				out += "font-family: Verdana, Arial, Helvetica, sans-serif;  \n";
				out += "border: 1px solid #399;  \n";
				out += "border-spacing: 1px 1px;  \n";
				out += "} \n";
				out += ".table2 td { \n";
				out += "background: #E0E0E0; \n";
				out += "border: 1px solid #333; \n";
				out += "padding: 0px;  \n";
				out += "}";

			return out;
		}	
}
