package neserver.nvs.com;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import health.greenexSrv2.nvs.com.RegularHealthDayReport;

public class RReportsPageHandler extends HandlerTemplate {
	public RReportsPageHandler(globalData gData) {
		super(gData);
	}

	@Override
	public String getPage() {
		String out = "";
		String curStyle = "";

		int userRight = Utils.determineUserRights(gData, "connect_data", gData.commonParams.get("currentUser"));
		if (userRight < 1) {

			out += "Sorry, you don't have necessary authorisation! Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}

		out += getBeginPage();
		out += strTopPanel(caption);

		out += getRegularReportsPage();

		out += getEndPage();

		return out;
	}

	public String getRegularReportsPage() {
		String out = "";
		String report = params.get("report");

		if (report == null) {

			out += getListOfRegularReports();

		} else {

			out += getRegularReport(report);
		}

		return out;
	}

	
	private String getRegularReport(String report) {
		String out = "";
		params.put("job_name", report);
		params.put("caption", "");
		RegularHealthDayReport t1 = new RegularHealthDayReport(gData, params);
		t1.imgPrefix = "/img/";
		t1.sendMail = false;
		t1.run();
		out += t1.body;
	
		return out;
	}
	
	private String getListOfRegularReports() {
		String out = "";

		String SQL = "";
		int counter = 0;

		SQL += "SELECT s1.* , b.caption, b.last_run_date, b.interval_min ";
		SQL += "FROM( ";
		SQL += "SELECT job_name FROM regular_reports_data ";
		SQL += "WHERE active = 'X' GROUP BY job_name ) s1 ";
		SQL += "LEFT JOIN regular_schedule b ON s1.job_name = b.job_name ";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		out += "<table class='table2'>";

		out += "<thead><tr>";
		out += "<th>п/п</th>";
		out += "<th>отчет</th>";
		out += "<th>описание</th>";
		out += "<th>время последнего запуска</th>";
		out += "<th>интервал</th>";
		out += "</tr></thead>";

		out += "<tbody>";


		for (Map<String, String> rec : records) {
		
			counter++;
			out += "</tr>";
			out += "<td>" + counter + "</td>";
			
			String link = "";
			link += "<a href='rreports?report=" + rec.get("job_name") + "'>";
			link += rec.get("job_name");
			link += "</a>";
			out += "<td>" + link + "</td>";
			out += "<td>" + rec.get("caption") + "</td>";
			out += "<td>" + rec.get("last_run_date") + "</td>";
			out += "<td>" + Utils.timeConvert(Integer.valueOf(rec.get("interval_min"))) + "</td>";
			out += "</tr>";
		}

		out += "</table>";
		return out;
	}

}
