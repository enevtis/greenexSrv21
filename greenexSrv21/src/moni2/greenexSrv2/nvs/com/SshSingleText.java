package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class SshSingleText extends BatchJobTemplate implements Runnable {

	public SshSingleText(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {
		
		doCheck();
	}

	public void doCheck() {

		gData.logger.info("<p style='color:blue;'>Start " + params.get("job_name") + "</p>");
		gData.sqlReq.saveResult("update monitor_schedule set active=' ' where id=" + params.get("job_id"));
		
		

		
		List<remoteSystem> db_systems = readServersListForCheck(params.get("job_number"));

		gData.truncateLog(params.get("job_name"));
		gData.saveToLog("found " + db_systems.size() + " systems to start.", params.get("job_name"));
		
		
		for (remoteSystem s : db_systems) {


			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));

			ConnectionData conData = readConnectionParameters(pr);
			if (gData.debugMode) gData.saveToLog("conData.user =" + conData.user + " conData.hash=" + conData.hash , params.get("job_name"));
			
			
			String remoteSshText = readFromTable_ssh_remote_check(this.getClass().getSimpleName(), params.get("job_name"), pr.OS, pr.physGuid);
			

			if (Utils.isInvalidSshCommand(remoteSshText)) {
				gData.saveToLog("Exit! Remote command is dangerous!" + s.params.get("guid") + " " + s.params.get("ip") + "remoteSshText= " + remoteSshText, params.get("job_name"));
				return;
			}
			

			if (gData.debugMode) gData.saveToLog(s.params.get("guid") + " " + s.params.get("ip") + "remoteSshText= " + remoteSshText, params.get("job_name"));
			
			String SQL_result = "";

			if (doSshRequest(s, conData, remoteSshText)) {

				SQL_result += "insert into monitor_results ( ";
				SQL_result += "`object_guid`,`monitor_number`,";
				SQL_result += "`check_date`,`result_number`,";
				SQL_result += "`result_text`,`is_error`) values (";
				SQL_result += "'" + s.params.get("guid") + "',";
				SQL_result += "" + params.get("job_number") + ",";
				SQL_result += "now(),";
				SQL_result += "0,";
				SQL_result += "'" + s.params.get("result") + "',";
				SQL_result += "''";
				SQL_result += ")";

			} else {

				SQL_result += "insert into monitor_results ( ";
				SQL_result += "`object_guid`,`monitor_number`,`check_date`,`result_number`,";
				SQL_result += "`result_text`,`is_error`) values (";
				SQL_result += "'" + s.params.get("guid") + "',";
				SQL_result += "" + params.get("job_number") + ",";
				SQL_result += "now(),";
				SQL_result += "0,";
				SQL_result += "'" + s.params.get("result") + "',";
				SQL_result += "'X'";
				SQL_result += ")";

			}

			if (gData.debugMode) gData.saveToLog(s.params.get("guid") + " " + s.params.get("ip") + " SQL_result= " + SQL_result, params.get("job_name"));

			gData.sqlReq.saveResult(SQL_result);

		}

		gData.logger.info("*** <p style='color:blue;'>End " + params.get("job_name") + "</p>");
		
		
		gData.sqlReq.saveResult(
				"update monitor_schedule set active='X',last_analyze=now(),checks_analyze=checks_analyze+1 where id="
						+ params.get("job_id"));
		
		if (gData.debugMode) gData.saveToLog(params.get("job_name") + " finished succesfully", params.get("job_name"));

	}

	@Override
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


	protected String readFromTable_ssh_remote_check(String className, String job_name, String os, String object_guid) {
		String out = "";
		String SQL = "";
		
		SQL += "SELECT * FROM ssh_remote_check WHERE className='" + className + "'";
		SQL += " and job_name='" + job_name + "'";		
		SQL += " and os ='" + os + "'";
		SQL += " and object_guid in('" + object_guid + "','all')";
		SQL += " order by id";
		
		if (gData.debugMode) gData.saveToLog(SQL, params.get("job_name"));
		
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out = rec.get("sshText") + " ";

				}
			}
		}

		return out;
	}


}
