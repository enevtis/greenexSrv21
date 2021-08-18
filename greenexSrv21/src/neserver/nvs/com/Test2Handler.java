package neserver.nvs.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import health.greenexSrv2.nvs.com.RegularOpsbiHealthReport2;
import obj.greenexSrv2.nvs.com.TblField;

public class Test2Handler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_conn_data";
	private String tableName = screenName;
	private String SQL = "";
	public String caption = "Test page";

	public Test2Handler(globalData gData) {
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

		Map<String, String> sparams = new HashMap<String, String>();

		out += getTestPage();

		out += getEndPage();

		return out;
	}

	
	protected String getTestPage() {
		String out = "";


		Map<String, String> params = new HashMap<String, String>();
		out += "Test 4";
		RegularOpsbiHealthReport2 t1 = new RegularOpsbiHealthReport2(gData, params);
		t1.imgPrefix = "cid:";
		t1.run();
		out += t1.body;
		List<String> attFiles = t1.attFiles;
		
		MSEcxchange me = new MSEcxchange(gData);

		List<String> recepients = new ArrayList<String>();
		recepients.add("enevtis-x@aeroflot.ru");
		
		String commonSubjectLetter = "Test for ОПСБИАЙ";

		String bodyLetter = t1.body;
		
		
		me.sendOneLetter2(recepients, commonSubjectLetter, bodyLetter, attFiles);
		
		
		out += "E-mail is send";
		
		
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
		    
			out += "<td style='width:" + (indicatorWidth + 2) + "px;'>";		    
		    out+= "<div style='float:left;display:inline;background-color:" + gData.clrDiagramFill + ";width:" + leftPart + "px;height:10px;'>&nbsp;</div> ";
	     	out+= "<div style='float:left;display:inline;background-color:" + gData.clrDiagramBackground +";width:" + rightPart + "px;height:10px;'>&nbsp;</div> ";
			out += "</td>";	
	
			String buffer = "занято " + rec.get("used_percent") + " %";
			buffer += "(" + rec.get("used_size_gb") + " из ";
			buffer += rec.get("max_size_gb") + " Гб.) ";
			int pastMinutes = Integer.valueOf(rec.get("past_minutes"));
			
			buffer += " Последняя проверка была: " + Utils.timeConvert(pastMinutes) + " назад";

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