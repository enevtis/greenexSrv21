package moni2.greenexSrv2.nvs.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;

public class OracleFreeSpaceCheck extends BatchJobTemplate implements Runnable {

	public OracleFreeSpaceCheck(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		setRunningFlag_shedule();
		doCheck();
		reSetRunningFlag_shedule();
	}

	protected void doCheck() {

		String message = "", filter = "";

		gData.logger.info("*** <p style='color:blue;'>Start " + params.get("job_name") + "</p>");


		List<remoteSystem> db_systems = readDB_systemsListForCheck();

		gData.truncateLog(params.get("job_name"));	
		gData.saveToLog("found " + db_systems.size() + " systems to start.", params.get("job_name"));



		for (remoteSystem s : db_systems) {

			message = s.params.get("guid") + ": ip=" + s.params.get("ip") + " sid=" + s.params.get("sid") + " db_type="
					+ s.params.get("db_type");

			gData.saveToLog(message, params.get("job_name"));

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			
	
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));

			ConnectionData conData = readConnectionParameters(pr);
			
	
			filter = "oracle";
			String remoteSQL = read_from_sql_remote_check(this.getClass().getSimpleName(), params.get("job_name"), filter);

			if (remoteSQL.length() < 10) {
				message = "Warning! <p style='color:red;'>SQL for remote check in sql_remote_check for class= "
						+ this.getClass().getSimpleName() + "and job_name=" + params.get("job_name") + " and filter="
						+ filter + " is not found</p>";
				gData.logger.info(message);
				gData.logger.info(remoteSQL);
				if (gData.debugMode)
					gData.saveToLog(message, params.get("job_name"));
				return;

			}
			
			
			String SQL = "select value_limit from monitor_links where monitor_number=" + params.get("job_number")
					+ " and object_guid='" + s.params.get("guid") + "'";
	
			gData.saveToLog(SQL, params.get("job_name"));
			
			String SAPSR3TablespaceLimitGb = gData.sqlReq.readOneValue(SQL);
			
			gData.saveToLog(SAPSR3TablespaceLimitGb, params.get("job_name"));
			
			String paramTemplate = "!LIMIT_GB!";

				remoteSQL = remoteSQL.replace(paramTemplate, SAPSR3TablespaceLimitGb);

			String paramName = "OracleTablespaceFreeLimitPercent";
			paramTemplate = "!LIMIT_PERCENT!";
			
			if (gData.commonParams.get(paramName) != null) {
				remoteSQL = remoteSQL.replace(paramTemplate, gData.commonParams.get(paramName));

			} else {

				gData.saveToLog("param " + paramName + " not found! ", params.get("job_name"));
				return;

			}		
			
			
			if (gData.debugMode)
				gData.saveToLog(remoteSQL, params.get("job_name"));			
			

			String SQL_result = "";
			List<String> sqlOraTbs = new ArrayList<String>();

			sqlOraTbs.add("delete from monitor_oracle_ts where object_guid='" + s.params.get("guid")+ "'");
			
			
			if (doOracleSql(s, conData, remoteSQL, sqlOraTbs)) {

				SQL_result += "insert into monitor_results ( ";
				SQL_result += "`object_guid`,`monitor_number`,";
				SQL_result += "`check_date`,`result_number`,";
				SQL_result += "`result_text`,`is_error`) values (";
				SQL_result += "'" + s.params.get("guid") + "',";
				SQL_result += "" + params.get("job_number") + ",";
				SQL_result += "now(),";
				SQL_result += "" + s.params.get("result") + ",";
				SQL_result += "'" + s.params.get("message") + "',";
				SQL_result += "''";
				SQL_result += ")";
				message += SQL_result;

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

				message += SQL_result;

			}

			if (gData.debugMode)
				gData.saveToLog(message, params.get("job_name"));

			gData.sqlReq.saveResult(SQL_result);
			gData.sqlReq.saveResult(sqlOraTbs);
			
		}

		gData.logger.info("*** <p style='color:blue;'>End " + params.get("job_name") + "</p>");

	}

	protected boolean doOracleSql(remoteSystem s, ConnectionData conData, String remoteSQL, List<String> sqlOraTbs) {
		boolean out = false;

		String connString = "jdbc:oracle:thin:@" + s.params.get("ip") + ":" + s.params.get("port") + ":"
				+ s.params.get("sid");
		SqlReturn ret = gData.sqlReq.getSqlFromOracle(connString, conData.user, conData.password, remoteSQL);
		
		if (ret.isOk) {
			int alert_count = 0;
			String message = "";
			String line = "";

			for (Map<String, String> rec : ret.records) {
				out = true;
				
				int alert = Integer.valueOf(rec.get("ALERT"));

				if ( alert > 0 ) alert_count ++;
				
				line = "insert into monitor_oracle_ts (";
				line += "object_guid,";
				line += "tablespace_name,";
				line += "max_free,";
				line += "pct_free,";
				line += "alert,";
				line += "moment";
				line += " ) values (";				
				line += "'" + s.params.get("guid") + "',";
				line += "'" + rec.get("TABLESPACE_NAME") + "',";
				line += "" + rec.get("MAX_FREE") + ",";
				line += "" + rec.get("PCT_FREE") + ",";				
				line += "" + rec.get("ALERT") + ",";
				line += "now()";
				line += ")";
			
				sqlOraTbs.add(line);
				
				if (gData.debugMode)
					gData.saveToLog(line, params.get("job_name"));

			}

			gData.saveToLog("OK", params.get("job_name"));
			
			s.params.put("result", "" + alert_count);
			s.params.put("message", message);

		} else {
			
			gData.saveToLog("ERROR", params.get("job_name"));
			
			out = false;
			s.params.put("result", "-1");
			s.params.put("message", ret.message);
		}

		if (gData.debugMode)
			gData.saveToLog("Return from Oracle = " + s.params.get("result") + " " + s.params.get("message"),
					params.get("job_name"));

		return out;
	}

}
