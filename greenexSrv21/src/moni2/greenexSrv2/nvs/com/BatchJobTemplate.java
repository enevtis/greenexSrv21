package moni2.greenexSrv2.nvs.com;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class BatchJobTemplate {
	public globalData gData;
	public String SQL = "";
	public String SQL_for_list = "";
	Map<String, String> params;

	public BatchJobTemplate(globalData gData, Map<String, String> params) {
		this.gData = gData;
		this.params = params;
	}

	protected Map<String, String> parceFilter(String line) {
		Map<String, String> out = new HashMap<String, String>();
		String[] lines = line.split(";");

		for (String l : lines) {
			String[] parts = l.split("=");
			out.put(parts[0], parts[1]);
		}
		return out;
	}

	protected String read_from_sql_remote_check(String className, String job_name, String filter) {
		String out = "";
		String SQL = "";

		SQL += "SELECT * FROM sql_remote_check WHERE class='" + className + "'";
		SQL += " and job_name='" + job_name + "'";
		if (!filter.isEmpty()) {
			SQL += " and filter='" + filter + "'";
		}
		SQL += " order by id";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += rec.get("sql_text") + " ";

				}
			}
		}

		return out;
	}

	protected String getValue(String inputString, String tagName) {
		String out = "";
		Pattern TAG_REGEX = Pattern.compile("<" + tagName + ">(.+?)</" + tagName + ">", Pattern.DOTALL);
		Matcher matcher = TAG_REGEX.matcher(inputString);
		while (matcher.find()) {
			out = matcher.group(1);
		}
		return out;
	}

	protected ConnectionData readConnectionParameters(PhisObjProperties pr) {
		ConnectionData out = new ConnectionData();
		String SQL = "select * from monitor_conn_data where object_guid='" + pr.physGuid + "'";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
		if (records_list != null) {
			if (records_list.size() > 0) {
				for (Map<String, String> rec : records_list) {
					out.user = rec.get("user");
					out.hash = rec.get("hash");
					out.conn_type = rec.get("conn_type");
					out.clnt = rec.get("clnt");
					out.password = gData.getPasswordFromHash(out.hash);
				}
			}
		}

		switch (pr.obj_typ) {
		case "servers":
			out.ip = gData.sqlReq.readOneValue("SELECT def_ip FROM servers WHERE guid='" + pr.physGuid + "'");

			break;
		case "db_systems":

			out.ip = gData.sqlReq.readOneValue("SELECT def_ip FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.port = gData.sqlReq.readOneValue("SELECT port FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.sid = gData.sqlReq.readOneValue("SELECT sid FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.sysnr = gData.sqlReq.readOneValue("SELECT sysnr FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.dbType = gData.sqlReq.readOneValue("SELECT db_type FROM db_systems WHERE guid='" + pr.physGuid + "'");

			break;
		case "app_systems":

			break;

		}

		return out;
	}

	protected ConnectionData readConnectionParameters(String idRecord, PhisObjProperties pr) {
		ConnectionData out = new ConnectionData();
		String SQL = "select * from monitor_conn_data where id=" + idRecord;

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
		if (records_list != null) {
			if (records_list.size() > 0) {
				for (Map<String, String> rec : records_list) {
					out.user = rec.get("user");
					out.hash = rec.get("hash");
					out.conn_type = rec.get("conn_type");
					out.clnt = rec.get("clnt");
					out.password = gData.getPasswordFromHash(out.hash);
				}
			}
		}

		switch (pr.obj_typ) {
		case "servers":
			out.ip = gData.sqlReq.readOneValue("SELECT def_ip FROM servers WHERE guid='" + pr.physGuid + "'");

			break;
		case "db_systems":

			out.ip = gData.sqlReq.readOneValue("SELECT def_ip FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.port = gData.sqlReq.readOneValue("SELECT port FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.sid = gData.sqlReq.readOneValue("SELECT sid FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.sysnr = gData.sqlReq.readOneValue("SELECT sysnr FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.dbType = gData.sqlReq.readOneValue("SELECT db_type FROM db_systems WHERE guid='" + pr.physGuid + "'");

			break;
		case "app_systems":

			break;

		}

		return out;
	}

	protected List<remoteSystem> readDB_systemsListForCheck() {

		List<remoteSystem> out = new ArrayList<remoteSystem>();
		String SQL = "";

		SQL += "SELECT a.*,b.* FROM db_systems a \n";
		SQL += "JOIN monitor_links b ON a.guid=b.object_guid \n";
		SQL += "WHERE b.monitor_number = '" + params.get("job_number") + "' \n";
		SQL += "AND b.active = 'X' \n";

		if (params.get("parameters") != null) {
			String list = getValue(params.get("parameters"), "list");
			if (!list.isEmpty()) {
				SQL += " and " + list;
			}
		}

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					remoteSystem s = new remoteSystem();
					s.params.put("guid", rec.get("guid"));
					s.params.put("ip", rec.get("def_ip"));
					s.params.put("port", rec.get("port"));
					s.params.put("sid", rec.get("sid"));
					s.params.put("sysnr", rec.get("sysnr"));
					s.params.put("db_type", rec.get("db_type"));

					out.add(s);

				}
			}
		}

		return out;
	}

	protected boolean doSshRequest(remoteSystem s, ConnectionData conData, String remoteSshText) {
		boolean out = false;

		try {

			String sshReply = getSsh(conData.ip, conData.user, conData.password, remoteSshText);
			s.params.put("result", sshReply);
			out = true;

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe("<p stype='color:red;'> ip=" + conData.ip + " user=" + conData.user + " hash="
					+ conData.hash + " gave an connect error" + errors.toString() + "<br>" + remoteSshText + "</p>");
			s.params.put("result", errors.toString());
			out = false;
		}

		return out;
	}

	public String getSsh(String ip, String user, String password, String strCommand) throws Exception {

		String out = "";

		int exitStatus = -100;

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, ip, 22);
		session.setPassword(password);
		session.setConfig(config);
		session.setTimeout(5000);
		session.connect();

		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(strCommand);
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);

		InputStream in = channel.getInputStream();
		channel.connect();
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				out += new String(tmp, 0, i);
			}

			if (channel.isClosed()) {
				exitStatus = channel.getExitStatus();

				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
		channel.disconnect();
		session.disconnect();

		return out;

	}

	protected List<remoteSystem> readServersListForCheck(String job_number) {

		List<remoteSystem> out = new ArrayList<remoteSystem>();
		String SQL = "";

		SQL += "SELECT a.*,b.* FROM servers a \n";
		SQL += "JOIN monitor_links b ON a.guid=b.object_guid  \n";
		SQL += "WHERE b.monitor_number = '" + job_number + "' \n";
		SQL += "AND b.active = 'X' \n";

		if (params.get("parameters") != null) {
			String list = getValue(params.get("parameters"), "list");
			if (!list.isEmpty()) {
				SQL += " and " + list;
			}
		}

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					remoteSystem s = new remoteSystem();
					s.params.put("guid", rec.get("guid"));
					s.params.put("ip", rec.get("def_ip"));
					s.params.put("hostname", rec.get("def_hostname"));
					s.params.put("os_type", rec.get("os_typ"));

					out.add(s);

				}
			}
		}

		return out;
	}

	protected boolean checkIfAlertAlreadyExists(String object_guid, String monitor_number, String details) {
		boolean out = false;

		String SQL = "";
		SQL += "select count(*) as output from `problems` ";
		SQL += " where object_guid='" + object_guid + "' ";
		SQL += " and monitor_number=" + monitor_number + " ";
		if (details != null) {
			SQL += " and details='" + details + "' ";
		}

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					int result = Integer.valueOf(rec.get("output"));
					out = result > 0 ? true : false;

				}
			}
		}

		return out;
	}

	protected String readFrom_sql_text(String className, String filter) {
		String out = "";
		String SQL = "";

		SQL += "SELECT * FROM sql_text WHERE class='" + className + "' ";
		SQL += " and filter='" + filter + "'";
		SQL += " order by id";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += rec.get("sql_text") + " ";

				}
			}
		}

		return out;
	}

	protected List<String> readRecepientsByProjects(List<String> object_guids) {
		List<String> out = new ArrayList<String>();
		String SQL = "";

		SQL += "SELECT s1.* \n";
		SQL += "FROM (SELECT DISTINCT CONCAT(object_guid,'-',monitor_number) AS guid_key,email FROM recepients ) AS s1 \n";
		SQL += "WHERE ";
		SQL += " s1.guid_key LIKE 'all%' \n";
		SQL += "OR s1.guid_key IN (";
		for (String s : object_guids) {
			SQL += "'" + s + "',";
		}
		SQL = SQL.substring(0, SQL.length() - 1);
		SQL += ") \n";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					out.add(rec.get("email"));
				}
			}
		}

		return out;
	}

	protected List<String> readAdminRecepients() {
		List<String> out = new ArrayList();
		String SQL = "";

		SQL = "SELECT * FROM recepients WHERE project_guid = 'all'";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {
			out.add(rec.get("email"));
		}

		return out;
	}

	protected List<remoteSystem> readABAP_systemsListForCheck() {

		List<remoteSystem> out = new ArrayList<remoteSystem>();
		String SQL = "";

		SQL += "SELECT a.*,b.* FROM app_systems a \n";
		SQL += "JOIN monitor_links b ON a.guid=b.object_guid \n";
		SQL += "WHERE b.monitor_number = '" + params.get("job_number") + "' \n";
		SQL += "AND b.active = 'X' \n";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {

			remoteSystem s = new remoteSystem();
			s.params.put("guid", rec.get("guid"));
			s.params.put("ip", rec.get("def_ip"));
			s.params.put("port", rec.get("port"));
			s.params.put("sid", rec.get("sid"));
			s.params.put("sysnr", rec.get("sysnr"));
			s.params.put("app_typ", rec.get("app_typ"));
			s.params.put("sap_scheme", rec.get("sap_scheme"));
			s.params.put("short", rec.get("short"));
			out.add(s);

		}

		return out;
	}
}
