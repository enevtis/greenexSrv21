package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.TblField;

public class LandscapeHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "landscape";
	private String tableName = "projects";
	
	private String SQL = "";

	public LandscapeHandler(globalData gData) {
		super(gData);

	}


	public String getPage() {
		String out = "";
		out += getBeginPage();
		out += strTopPanel("Landscape");

		String SQL = "";

		SQL += "SELECT counter as 'number',short as 'name', ";
		SQL += "CONCAT(\"<a href='/system_image?page=\",pagePrd,\"'>Production</a>\") AS prdLink, ";
		SQL += "CONCAT(\"<a href='/system_image?page=\",pageNoPrd,\"'>Non production</a>\") AS noPrdLink ";
		SQL += "from projects ORDER BY counter ";

		this.fields = readTableMetadata(screenName);

		

		out += buildJavascriptRefreshFunction(SQL, tableName, fields, "READONLY");
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();



		out += getEndPage();

		return out;
	}

	public String getPageOld() {

		String out = "";
		String curStyle = "";

		out += getBeginPage();
		out += strTopPanel("Landscape");


		SQL += "SELECT counter as 'number',short as 'name', ";
		SQL += "CONCAT(\"<a href='/system_image?page=\",pagePrd,\"'>Production</a>\") AS prdLink, ";
		SQL += "CONCAT(\"<a href='/system_image?page=\",pageNoPrd,\"'>Non production</a>\") AS noPrdLink ";
		SQL += "from projects ORDER BY counter ";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		out += "<table class='table1'>";

		out += "<tr>";
		out += "<th>" + gData.tr("Project") + "</th>";
		out += "<th>" + gData.tr("PRD") + "</th>";
		out += "<th>" + gData.tr("DEV-QAS") + "</th>";

		out += "</tr>";

		String buffer = "";

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					out += "<tr>";
					out += "<td>" + rec.get("counter") + "</td>";
					out += "<td>" + rec.get("short") + "</td>";
					out += "<td>" + rec.get("prdLink") + "</td>";
					out += "<td>" + rec.get("noPrdLink") + "</td>";

					out += "</tr>";

				}

			}
		}

		out += "</table>";

		out += getEndPage();

		return out;

	}

}
