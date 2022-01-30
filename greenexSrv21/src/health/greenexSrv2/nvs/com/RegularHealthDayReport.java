package health.greenexSrv2.nvs.com;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graph.greenexSrv2.com.GraphDisk;
import graph.greenexSrv2.com.GraphTimeValue;
import graph.greenexSrv2.com.PngDisksDiagramPainter;
import graph.greenexSrv2.com.PngTimelineDiagramPainter;
import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class RegularHealthDayReport extends HealthReportTemplate implements Runnable {

	public String body = "";
	public boolean sendMail = true;

	public List<String> attFiles = new ArrayList<String>();

	public RegularHealthDayReport(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_regular();
			doCheck();
			reSetRunningFlag_regular();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	protected void doCheck() {

		String job_name = getJobName();
		
		gData.truncateLog(job_name);
		attFiles.clear();


		body += "<style>";
		body += Utils.getStyle();
		body += "</style>";

		body += getUserInfo();
		body += getFreeSpaceOnDiskDiagram();
		
		ClusterHealth clh = new ClusterHealth(gData);
		body += clh.getHPSG_status(job_name);
		
		body += clh.getReplication_status(job_name);
		
		DatabaseHealth dbh = new DatabaseHealth(gData);

		body += dbh.getBackup_status(job_name);
		
		body += getDatabaseGrowth(job_name);
		
		
		this.body += getBottomInfo();
		
		
		gData.saveToLog(" Files = " + attFiles.size(), getJobName());
		gData.saveToLog(body, getJobName());

		if (sendMail) {
			MSEcxchange me = new MSEcxchange(gData);

			List<String> recepients = readAllRecepients(params.get("job_name"));

			String caption = params.containsKey("caption") ? params.get("caption") : "Регулярный отчет " + getJobName();
			me.sendOneLetter2(recepients, caption, this.getClass().getSimpleName() + " " + body, attFiles);

		}

		gData.saveToLog("Email is send", getJobName());

	}

	protected String getUserInfo() {
		String out = "";
		out += "<p class='user_info'>";
		out += gData.tr("343787d5-25c7-4598-a12e-4c8b6cb69951");
		out += "</p>";
		return out;
	}
	protected String getBottomInfo() {
		String out = "";
		out += "<p class='user_info'>";
		out += gData.tr("bb064147-2d3c-4451-80c6-4183202b00a7") + " " + gData.getOwnHostname();
		out += " " + gData.getOwnIp() + " <br>";

			String link = "";
		
		String monitorPort = gData.commonParams.get("webServicePort");
		String monitorIp = gData.getOwnIp();

		out += "<a href='https://" + monitorIp + ":" + monitorPort + "/rreports'>";
		out += gData.tr("2d35d54a-8d63-4e94-a667-d217e659ca8e");
		out += "</a> ";

		out += "</p>"; 
		return out;
	}	
	
	protected String getJobName() {
		String out = "";
		out = params.containsKey("job_name") ? params.get("job_name") : this.getClass().getSimpleName();
		return out;
	}

	protected String getFreeSpaceOnDiskDiagram() {
		String out = "";

		List<String> guids = getListGuidsForJobName("disks");
		if (guids.size() == 0)
			return "";


		
		

		out += "<p class='caption_item'>" + gData.tr("9c01211b-57d6-444f-a0e6-1d5f78ee9657") +":</p>";
		out += "<table class='table1'>";

		out += "<thead><tr>";
		out += "<th>п/п</th>";
		out += "<th>ip</th>";
		out += "<th>hostname</th>";
		out += "<th>os</th>";
		out += "<th>role</th>";
		out += "<th>" + gData.tr("bfcad6d9-4bbe-4a1b-b49e-33a2e9bb980f") +"</th>";
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
			SQL += "SELECT `id`,`server_guid`, `name`,`max_size_gb`, `used_size_gb`,`check_date`,  ";
			SQL += "ROUND((`used_size_gb`/ `max_size_gb` * 100),1) AS used_percent  ";
			SQL += "FROM monitor_disks  WHERE server_guid='" + guid + "'  ";
			SQL += "AND check_date = (SELECT MAX(check_date) FROM monitor_disks WHERE server_guid='" + guid
					+ "') ) s1  ";
			SQL += "LEFT JOIN monitor_disks_min b ON s1.server_guid = b.server_guid AND s1.`name` = b.`name`  ";
			SQL += "ORDER BY s1.`id` ";

			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

			List<GraphDisk> disks = new ArrayList();

			float koeff_Gb = getValuesKoeff(guid);
			
			for (Map<String, String> rec : records) {
				GraphDisk d = new GraphDisk();
				
				
				d.maxSizeGb = koeff_Gb * Float.valueOf(rec.get("max_size_gb"));
				d.usedSizeGb = koeff_Gb * Float.valueOf(rec.get("used_size_gb"));

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
			out += "<td>" + linkToDashboard(guid, hostname) + "</td>";
			out += "<td>" + os + "</td>";
			out += "<td>" + role + "</td>";
			out += "<td><img src='" + imgPrefix + fileGuid + ".png'></td>";
			out += "</tr>";

		}
		out += "<tbody>";
		out += "</table>";
		return out;
	}

	
	float getValuesKoeff(String guid) {
		float out = 1f;
		
		String SQL = "SELECT * from monitor_disks WHERE server_guid='" + guid + "' AND NAME='/'";
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {
			float rootVolume = Float.valueOf(rec.get("max_size_gb"));
			if (rootVolume > 1000f) out = 1f / 1024f / 1024f ;
		}
		
		gData.logger.info("Koef=" + String.format("%.9f", out));
		
		return out;
	}
	
	public String linkToDashboard(String guid, String text) {
		String out = "";
		String monitorPort = gData.commonParams.get("webServicePort");
		String monitorIp = gData.getOwnIp();

		out += "<a href='https://" + monitorIp + ":" + monitorPort + "/dashboard?guid=";
		out += guid + "'>";
		out += text;
		out += "</a> ";

		return out;
	}

	protected List<String> getListGuidsForJobName(String repType) {
		List<String> out = new ArrayList<String>();
		String job_name = getJobName();

		String SQL = "";
		SQL += "select * from regular_reports_data where `job_name`='" + job_name + "'";
		SQL += " AND `rep_type`='" + repType + "' and `active`='X' order by id";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {

			out.add(rec.get("guid"));

		}

		return out;
	}

	protected String getDatabaseGrowth(String job_name) {
		String out = "";

		List<String> guids = getListGuidsForJobName("db_growth");
		if (guids.size() == 0)
			return "";

		int counter = 0;

		out += "<p class='caption_item'>" + gData.tr("8d73fe2e-dff8-4944-a658-68f583c00baa") + " " + Utils.timeConvert(growthDays * 24 * 60,gData.lang) + "</p>";

		out += "<table class='table1'>";
		out += "<thead><tr>";
		out += "<th>" + gData.tr("77afa40e-b523-4cb3-8b70-9d7159d1cdcb") + "</th>";
		out += "<th>" + gData.tr("69cb73ae-4d53-4b4f-9eb4-396428e4b7d2") + "</th>";
		out += "<th>" + gData.tr("8eb3e582-d051-490c-b33c-c3259ef48fc3") + "</th>";
		out += "</tr></thead>";

		out += "<tbody>";
		for (String guid : guids) {

			String SQL = "";
			String ip = "", hostname = "", os = "", role = "";

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(guid);

			LocalDate finishDate = LocalDate.now();
			LocalDate startDate = finishDate.minusDays(growthDays);
			int diapDays = (int) ((float) growthDays / 100f);
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
			String description = pr.description + " " + pr.sid + " " + pr.sysnr;

			out += "<tr>";
			out += "<td>" + counter + "</td>";
			out += "<td>" + linkToDashboard(guid, description) + "</td>";

			String fileGuid = dp.paintTimeLineDiagram(list, gData.tr("8d066870-d020-468a-880a-ecea5f31a7e4"));
			attFiles.add(fileGuid);

			out += "<td><img src='" + imgPrefix + fileGuid + ".png'></td>";
			out += "</tr>";

		}
		out += "<tbody>";
		out += "</table>";

		gData.saveToLog(out, job_name);

		return out;
	}


}
