package neserver.nvs.com;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.SAPR3;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class ConnectionTestHandler extends HandlerTemplate {
	private List<TblField> fields = null;
	private String screenName = "monitor_conn_data";
	private String tableName = screenName;
	private String SQL = "";
	public String caption = "";
	public ConnectionData conData = null;

	public ConnectionTestHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));

		this.caption = gData.tr("Connection test for") + " " + pr.typeObj + " " + pr.shortCaption;

		out += getBeginPage();
		out += strTopPanel(caption);

		conData = readConnectionParameters(pr);

		gData.logger.info("conData.ip= " + conData.ip + " conData.conn_type =" + conData.conn_type);

		switch (conData.conn_type) {
		case "opersys":
			out += checkConnectLinux(conData, "df -h");
			break;
		case "database":

			if (pr.description.toUpperCase().contains("HANA")) {
				out += checkConnectSAPHANA(conData);
			} else if (pr.description.toUpperCase().contains("ORA")) {
				out += checkConnectOracle(conData);
			} else {

			}

			break;
		case "abap":
			out += checkConnectSAPABAP(conData);
			break;
		default:

			break;

		}

		out += getEndPage();

		return out;
	}

	protected String checkConnectOracle(ConnectionData conData) {
		String out = "";

//create user NAGIOS identified by Password123;
//alter profile DEFAULT limit PASSWORD_REUSE_TIME unlimited;
//alter profile DEFAULT limit PASSWORD_LIFE_TIME  unlimited;

//grant create session to NAGIOS;
//grant select any dictionary to NAGIOS;

		String connString = "jdbc:oracle:thin:@" + conData.ip + ":" + conData.port + ":" + conData.sid;
		Connection conn = null;
		String SQL = "";

		SQL += "SELECT * FROM v$instance";

		try {
			conn = DriverManager.getConnection(connString, conData.user, conData.password);

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(SQL);
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			while (rs.next()) {

				out += "INSTANCE_NAME: " + rs.getString("INSTANCE_NAME") + "<br>";
				out += "HOST: " + rs.getString("HOST_NAME") + "<br>";
				out += "START_TIME: " + rs.getTimestamp("STARTUP_TIME") + "<br>";
				out += "VERSION: " + rs.getString("VERSION") + "<br>";

			}
			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
			out += "<p class='error_msg'>CONNECTION FAILED!<p>" + errors.toString();

		}

		return out;
	}

	protected String checkConnectSAPHANA(ConnectionData conData) {
		String out = "";

		String connString = "jdbc:sap://" + conData.ip + ":" + conData.port + "/?autocommit=false";
		Connection conn = null;
		String SQL = "";

		SQL += "Select * from \"SYS\".\"M_DATABASE\";";

		try {
			conn = DriverManager.getConnection(connString, conData.user, conData.password);

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(SQL);
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			while (rs.next()) {

				out += "DATABASE_NAME: " + rs.getString("DATABASE_NAME") + "<br>";
				out += "HOST: " + rs.getString("HOST") + "<br>";
				out += "START_TIME: " + rs.getTimestamp("START_TIME") + "<br>";
				out += "VERSION: " + rs.getString("VERSION") + "<br>";

			}
			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
			out += "<p class='error_msg'>CONNECTION FAILED!<p>" + errors.toString();

		}

		return out;
	}


//	protected String checkConnectSAPABAP(ConnectionData conData) {
//		String out = "";
//		Map<String, String> params = new HashMap();
//		
//		params.put("ip", conData.ip);
//		params.put("sysnr", conData.sysnr);
//		params.put("user", conData.user);
//		params.put("password", conData.password);
//		params.put("clnt", conData.clnt);
//
//		SAPR3 sr3 = new SAPR3(gData, params);
//		
////		SqlReturn ret  = sr3.th_WPInfo("svo-erp-tst01_EAP_02");
//		SqlReturn ret  = sr3.th_WPInfo("");
//		
//	    if (ret.isOk ) {
//	    	
//	    	for (Map<String, String> rec : ret.records) {
//				out += rec.get("WP_BNAME") + "<br>";
//	    	}
//		} else {
//
//			out += "Connection error";
//		}
//
//
//		return out;
//	}
	
	
	
	protected String checkConnectSAPABAP(ConnectionData conData) {
		String out = "";
		Map<String, String> params = new HashMap();
		
		params.put("ip", conData.ip);
		params.put("sysnr", conData.sysnr);
		params.put("user", conData.user);
		params.put("password", conData.password);
		params.put("clnt", conData.clnt);
		
		gData.logger.info("connection to ip=" + conData.ip + " sysnr=" + conData.sysnr + " user=" + conData.user);
		

		SAPR3 sr3 = new SAPR3(gData, params);
		
		String response=sr3.rfcGetSystemInfo();
		
		gData.logger.info("response =" + response);
		
		
		String lines[] = response.split("::");
		
		for (int i=0; i < lines.length; i++) {
			
			
			out += lines[i] + " <br>";
			
		}

		return out;
	}

	private String checkConnectLinux(ConnectionData conData, String text) {

		String out = "";

		out += "Connection parameters:  IP: " + conData.ip + " USER: " + conData.user + " HASH:" + conData.hash
				+ "<br><hr>";
		try {

			String sshReply = getSsh(conData.ip, conData.user, conData.password, text);

			String lines[] = sshReply.split("\\r?\\n");
			for (int i = 0; i < lines.length; i++) {

				out += lines[i] + "<br>";

			}

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe("ip=" + conData.ip + " user=" + conData.user + " hash=" + conData.hash
					+ " gave an connect error" + errors.toString());
			out += "<p class='error_msg'>CONNECTION FAILED!<p>";
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
		session.setTimeout(Integer.valueOf(gData.commonParams.get("SshTimeoutSec")) * 1000);
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
}
