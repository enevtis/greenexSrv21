package health.greenexSrv2.nvs.com;

import java.awt.Color;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graph.greenexSrv2.com.GraphDisk;
import graph.greenexSrv2.com.GraphTimeValue;
import graph.greenexSrv2.com.PngDisksDiagramPainter;
import graph.greenexSrv2.com.PngTimelineDiagramPainter;
import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.BatchJobTemplate;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class HealthReportTemplate extends BatchJobTemplate implements Runnable {

//	public String imgPrefix = "/img/";
	public String imgPrefix = "cid:";
	public int growthDays = 365 * 5;

	public HealthReportTemplate(globalData gData, Map<String, String> params) {
		super(gData, params);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	protected String getDatabaseGrowth(List<String> guids, List<String> attFiles) {
		String out = "";
		int counter = 0;

		out += "<table class='table1'>";
		out += "<thead><tr>";
		out += "<th>п/п</th>";
		out += "<th>описание</th>";
		out += "<th>данные</th>";
		out += "</tr></thead>";

		out += "<tbody>";
		for (String guid : guids) {

			String SQL = "";
			String ip = "", hostname = "", os = "", role = "";

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(guid);

			LocalDate finishDate = LocalDate.now();
			LocalDate startDate = finishDate.minusDays(growthDays);
			int diapDays = (int) ((float) growthDays / 20f);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

			SQL += "SELECT MAX(s1.result_number) AS max_result, COUNT(*) AS counter,s1.groupdate, ";
			SQL += "DATE_FORMAT(s1.groupdate,\"%Y%m%d\") AS diap_date  ";
			SQL += "FROM  ";
			SQL += "( SELECT *,  ";
			SQL += "FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(check_date) / (" + diapDays + "*24*60*60)) * (" + diapDays
					+ "*24*60*60)) AS groupdate ";
			SQL += "FROM monitor_results WHERE  ";
			SQL += "check_date > '" + startDate.format(formatter) + "' AND check_date < '"
					+ finishDate.format(formatter) + "' ";
			SQL += "AND monitor_number in (203,210) ";
			SQL += "AND object_guid = '" + guid + "' ) s1 ";
			SQL += "GROUP BY s1.groupdate ORDER BY s1.groupdate  ";

			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

//			gData.logger.info("Class=" + this.getClass().asSubclass(this.getClass()).getSimpleName());

			PngTimelineDiagramPainter dp = new PngTimelineDiagramPainter();

			dp.imgPath = gData.mainPath + File.separator + "img" + File.separator;
			dp.timeFormat = "dd-MM-yyyy";
			dp.isPercentGraph = true;

			List<GraphTimeValue> list = new ArrayList();

			for (Map<String, String> rec : records) {

				GraphTimeValue tv = new GraphTimeValue();
				tv.tValue = Float.valueOf(rec.get("max_result"));
				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd");
				tv.dateTime = LocalDate.parse(rec.get("diap_date"), formatter2).atStartOfDay();

				list.add(tv);

			}

			counter++;
			String description = pr.DB + " " + pr.description + " " + pr.sid + " " + pr.sysnr;

			out += "<tr>";
			out += "<td>" + counter + "</td>";
			out += "<td>" + description + "</td>";

			String fileGuid = dp.paintTimeLineDiagram(list, "Размер базы данных (Гб)");
			attFiles.add(fileGuid);

			out += "<td><img src='" + imgPrefix + fileGuid + ".png'></td>";
			out += "</tr>";

		}
		out += "<tbody>";
		out += "</table>";

		gData.saveToLog(out, getJournalName());

		return out;
	}

	protected String getFreeSpaceOnDiskDiagram(List<String> guids, List<String> attFiles) {
		String out = "";

		out += "<style>";
		out += Utils.getStyle();
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
		int pastMinutes = 0;

		for (String guid : guids) {

			String SQL = "";
			String ip = "", hostname = "", os = "", role = "";

			SQL += "SELECT s1.*, b.max_percent, ";
			SQL += "CASE WHEN max_percent IS NULL THEN  ";
			SQL += "CASE WHEN s1.used_percent > 85 THEN 'red' ELSE 'green' END  ";
			SQL += "ELSE   ";
			SQL += "CASE WHEN s1.used_percent > max_percent THEN 'red' ELSE 'green' END  ";
			SQL += "END AS 'color', TIMESTAMPDIFF(minute,s1.check_date,NOW()) AS 'past_minutes'  ";
			SQL += "FROM  ";
			SQL += "(  ";
			SQL += "SELECT server_guid, `name`,`max_size_gb`, `used_size_gb`,`check_date`,  ";
			SQL += "ROUND((`used_size_gb`/ `max_size_gb` * 100),1) AS used_percent  ";
			SQL += "FROM monitor_disks  WHERE server_guid='" + guid + "'  ";
			SQL += "AND check_date = (SELECT MAX(check_date) FROM monitor_disks WHERE server_guid='" + guid
					+ "') ) s1  ";
			SQL += "LEFT JOIN monitor_disks_min b ON s1.server_guid = b.server_guid AND s1.`name` = b.`name`  ";
			SQL += "ORDER BY s1.`name` ";

			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

			List<GraphDisk> disks = new ArrayList();

			for (Map<String, String> rec : records) {
				GraphDisk d = new GraphDisk();
				d.maxSizeGb = Float.valueOf(rec.get("max_size_gb"));
				d.usedSizeGb = Float.valueOf(rec.get("used_size_gb"));
				d.path = rec.get("name");
				pastMinutes = Integer.valueOf(rec.get("past_minutes"));

				if (rec.get("max_percent") != null) {

					float maxPercent = Float.valueOf(rec.get("max_percent"));

				}

				disks.add(d);

			}
			String past_time = Utils.timeConvert(pastMinutes,gData.lang) + " " + gData.tr("d3ef97de-890b-4722-beb2-2811c225ae82");
			PngDisksDiagramPainter dp = new PngDisksDiagramPainter();
			dp.imgPath = gData.mainPath + File.separator + "img";
			String fileGuid = dp.paintDisksDiagram(disks, past_time);

			attFiles.add(fileGuid);

			SQL = "";
			SQL += "select * from servers where guid='" + guid + "'";
			records.clear();
			records = gData.sqlReq.getSelect(SQL);
			for (Map<String, String> rec : records) {

				ip = rec.get("def_ip");
				hostname = rec.get("def_hostname");
				os = rec.get("os_typ");
				role = rec.get("role_typ");

			}

			counter++;
			out += "<tr>";
			out += "<td>" + counter + "</td>";
			out += "<td>" + ip + "</td>";
			out += "<td>" + hostname + "</td>";
			out += "<td>" + os + "</td>";
			out += "<td>" + role + "</td>";
			out += "<td><img src='" + imgPrefix + fileGuid + ".png'></td>";
			out += "</tr>";

		}
		out += "<tbody>";
		out += "</table>";
		return out;
	}




	private String getJournalName() {
		return this.getClass().asSubclass(this.getClass()).getSimpleName();
	}

	protected List<String> readAllRecepients(String filter) {
		List<String> out = new ArrayList();
		String SQL = "";

		SQL = "SELECT * FROM recepients WHERE `filter` = '" + filter + "' and `active`='X'";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {
			out.add(rec.get("email"));
		}

		return out;
	}

}
