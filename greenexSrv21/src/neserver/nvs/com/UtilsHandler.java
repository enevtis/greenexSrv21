package neserver.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.TblField;

public class UtilsHandler extends HandlerTemplate {

	public UtilsHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {
		String out = "";
		String caption = "Утилиты";
		String action = params.get("action");
		out += getBeginPage();
		out += strTopPanel(caption);

		if (action == null) {

			out += readFromTable_options_map("utils");
		} else {

			switch (action) {

			case "show_links_sql_insert_for_check":
				out += showLinksFor_sql_insert_for_check();
				break;

			case "show_links_sql_insert_for_check_for_project":
				out += showLinksFor_show_links_sql_insert_for_check_for_project(params.get("guid"));

				break;
			case "show_links_sql_insert_for_check_for_project_list":

				out += showLinksFor_show_links_sql_insert_for_check_for_project_list(params.get("guid"),
						params.get("list"));
				break;

			case "show_links_sql_insert_for_check_for_schedule":
				out += showLinksFor_show_links_sql_insert_for_check_for_schedule(params.get("guid"), params.get("list"),
						params.get("monitor"));

				break;

			default:
				break;

			}

		}

		out += getEndPage();

		return out;
	}

	protected String showLinksFor_sql_insert_for_check() {
		String out = "";
		String SQL = "SELECT * FROM projects";

		out += "<ol>";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		for (Map<String, String> rec : records) {
			out += "<li><a href='/utils?action=show_links_sql_insert_for_check_for_project";
			out += "&guid=" + rec.get("guid") + "'>" + rec.get("short") + "</a>";
		}
		out += "</ ol>";
		return out;
	}

	protected String showLinksFor_show_links_sql_insert_for_check_for_project(String project_guid) {
		String out = "";
		String projectName = "";
		String SQL = "SELECT * FROM projects WHERE guid='" + project_guid + "'";
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		for (Map<String, String> rec : records) {
			projectName = rec.get("short");
		}

		out += "<ol>";
		out += "<li><a href='/utils?action=show_links_sql_insert_for_check_for_project_list";
		out += "&guid=" + project_guid + "&list=servers'> Сервера для " + projectName + "</a>";

		out += "<li><a href='/utils?action=show_links_sql_insert_for_check_for_project_list";
		out += "&guid=" + project_guid + "&list=db_systems'> БД для " + projectName + "</a>";

		out += "<li><a href='/utils?action=show_links_sql_insert_for_check_for_project_list";
		out += "&guid=" + project_guid + "&list=app_systems'> App Сервера для " + projectName + "</a>";

		out += "</ol>";

		return out;
	}

	protected String showLinksFor_show_links_sql_insert_for_check_for_project_list(String project_guid, String list) {
		String out = "";
		String projectName = "";

		String SQL = "SELECT * FROM projects WHERE guid='" + project_guid + "'";
		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
		for (Map<String, String> rec : records) {
			projectName = rec.get("short");
		}

		switch (list) {
		case "servers":
			SQL = "select * from monitor_schedule where conn_type='opersys'";
			records = gData.sqlReq.getSelect(SQL);
			out += "<ol>";
			for (Map<String, String> rec : records) {
				out += "<li><a href='/utils?action=show_links_sql_insert_for_check_for_schedule";
				out += "&guid=" + project_guid + "&list=" + list + "&monitor=" + rec.get("number") + "'>";
				out += rec.get("job_name") + " " + rec.get("job_descr");
			}
			out += "</ol>";

			break;
		case "db_systems":

			SQL = "select * from monitor_schedule where conn_type='database'";
			records = gData.sqlReq.getSelect(SQL);
			out += "<ol>";
			for (Map<String, String> rec : records) {
				out += "<li><a href='/utils?action=show_links_sql_insert_for_check_for_schedule";
				out += "&guid=" + project_guid + "&list=" + list + "&monitor=" + rec.get("number") + "'>";
				out += rec.get("job_name") + " " + rec.get("job_descr");
			}
			out += "</ol>";

			break;
		case "app_systems":

			break;

		}

		return out;
	}

	protected String showLinksFor_show_links_sql_insert_for_check_for_schedule(String project_guid, String list,
			String monitor_number) {
		String out = "";
		String SQL = "";
		List<Map<String, String>> records = null;
		String defaultLimit = "";
		String monitor = "";
		String projectName = gData.sqlReq.readOneValue("select short from projects where guid='" + project_guid + "'");
		String monitorName = gData.sqlReq.readOneValue("select job_name from monitor_schedule where number = " + monitor_number );

		out += "<h2>" + projectName + " " + monitor_number + " " + monitorName + "</h2><br>";
		
		switch (list) {
		case "servers":

			out += ServersLinks(project_guid, monitor_number);

			break;

		case "db_systems":

			out += Db_SystemsLinks(project_guid, monitor_number);

			break;

		default:
			out += "";
			break;

		}

		return out;

	}

	protected String ServersLinks(String project_guid, String monitor_number) {
		String out = "";
		String SQL = "";
		List<Map<String, String>> records = null;

		String defaultLimit = "";
		SQL = "select * from monitor_schedule where number=" + monitor_number;
		records = gData.sqlReq.getSelect(SQL);
		for (Map<String, String> rec : records) {
			defaultLimit = rec.get("default_limit");
		}

		SQL = "SELECT a.*, ";
		SQL += " CASE WHEN b.id IS NULL THEN 'black' ELSE 'lightGray' END AS 'color' ";
		SQL += " FROM servers a  ";
		SQL += " LEFT JOIN monitor_links b ON a.guid = b.object_guid AND b.monitor_number =" + monitor_number;
		SQL += " WHERE a.project_guid='" + project_guid + "' order by a.os_typ";

		records = gData.sqlReq.getSelect(SQL);

		out +=

				out += "<table border=1>";
		for (Map<String, String> rec : records) {

			out += "<tr style='color:" + rec.get("color") + "'>";
			out += "<td>" + rec.get("guid") + "</td>";
			out += "<td>" + rec.get("short") + "</td>";
			out += "<td>" + rec.get("def_ip") + "</td>";
			out += "<td>" + rec.get("def_hostname") + "</td>";
			out += "<td>" + rec.get("os_typ") + "</td>";
			out += "</tr>";

		}
		out += "</table>";
		out += "<hr>";

		for (Map<String, String> rec : records) {

			out += "<div style='color:" + rec.get("color") + "'>";
			out += "insert into monitor_links (";
			out += "object_guid,monitor_number,active,value_limit";
			out += ") values (";
			out += "'" + rec.get("guid") + "',";
			out += "" + monitor_number + ",";
			out += "'X',";
			out += "'" + defaultLimit + "'";
			out += ");";
			out += "</div>";
		}
		return out;
	}

	protected String Db_SystemsLinks(String project_guid, String monitor_number) {
		String out = "";
		String SQL = "";
		List<Map<String, String>> records = null;

		String defaultLimit = "";
		SQL = "select * from monitor_schedule where number=" + monitor_number;
		records = gData.sqlReq.getSelect(SQL);
		for (Map<String, String> rec : records) {
			defaultLimit = rec.get("default_limit");
		}

		SQL = "SELECT a.*, ";
		SQL += " CASE WHEN b.id IS NULL THEN 'black' ELSE 'lightGray' END AS 'color' ";
		SQL += " FROM db_systems a  ";
		SQL += " LEFT JOIN monitor_links b ON a.guid = b.object_guid AND b.monitor_number =" + monitor_number;
		SQL += " WHERE a.project_guid='" + project_guid + "' order by a.db_type";

		records = gData.sqlReq.getSelect(SQL);

		out +=

				out += "<table border=1>";
		for (Map<String, String> rec : records) {

			out += "<tr style='color:" + rec.get("color") + "'>";
			out += "<td>" + rec.get("guid") + "</td>";
			out += "<td>" + rec.get("short") + "</td>";
			out += "<td>" + rec.get("def_ip") + "</td>";
			out += "<td>" + rec.get("sid") + "</td>";
			out += "<td>" + rec.get("db_type") + "</td>";
			out += "</tr>";

		}
		out += "</table>";
		out += "<hr>";

		for (Map<String, String> rec : records) {

			out += "<div style='color:" + rec.get("color") + "'>";
			out += "insert into monitor_links (";
			out += "object_guid,monitor_number,active,value_limit";
			out += ") values (";
			out += "'" + rec.get("guid") + "',";
			out += "" + monitor_number + ",";
			out += "'X',";
			out += "'" + defaultLimit + "'";
			out += ");";
			out += "</div>";
		}
		return out;
	}
}
