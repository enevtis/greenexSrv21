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

public class OracleSingleResult extends BatchJobTemplate implements Runnable {

	public OracleSingleResult(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {


		doCheck();


	}

	protected void doCheck() {

		String message ="",filter = "";
		
		gData.logger.info("*** <p style='color:blue;'>Start " + params.get("job_name") + "</p>");
		gData.sqlReq.saveResult("update monitor_schedule set active=' ' where id=" + params.get("job_id"));
	
		filter = "oracle";
		String remoteSQL = read_from_sql_remote_check(this.getClass().getSimpleName(),params.get("job_name"),filter);

		if (remoteSQL.length() < 10) {
			message = "Warning! <p style='color:red;'>SQL for remote check in sql_remote_check for class= " +this.getClass().getSimpleName() 
			+ "and job_name=" + params.get("job_name") + " and filter=" + filter +  " is not found</p>";
			gData.logger.info(message);
			gData.logger.info(remoteSQL);
			return;
			
		}
		
		
		List<remoteSystem> db_systems = readDB_systemsListForCheck();
		
		if (gData.debugMode) gData.saveToLog("found " + db_systems.size() + " systems to start.", params.get("job_name"),false);

		for (remoteSystem s : db_systems) {

			message = s.params.get("guid") + ": ip=" + s.params.get("ip") + " sid=" + s.params.get("sid") + " db_type=" + s.params.get("db_type");
			
			gData.logger.info("*** <p class='check_db_runnig'>Running " + message + " " + params.get("job_name") + "</p>");
			
			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));	



			ConnectionData conData = readConnectionParameters(pr);

			String SQL_result = "";

			if (doOracleSql(s, conData, remoteSQL)) {

				SQL_result += "insert into monitor_results ( ";
				SQL_result += "`object_guid`,`monitor_number`,";
				SQL_result += "`check_date`,`result_number`,";
				SQL_result += "`result_text`,`is_error`) values (";
				SQL_result += "'" + s.params.get("guid") + "',";
				SQL_result += "" + params.get("job_number") + ",";
				SQL_result += "now(),";
				SQL_result += "" + s.params.get("result") + ",";
				SQL_result += "'ok',";
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

			
			if (gData.debugMode) gData.saveToLog(message, params.get("job_name"));

			
			gData.sqlReq.saveResult(SQL_result);	
		}

		gData.logger.info("*** <p style='color:blue;'>End " + params.get("job_name") + "</p>");
		gData.sqlReq.saveResult(
				"update monitor_schedule set active='X',last_analyze=now(),checks_analyze=checks_analyze+1 where id="
						+ params.get("job_id"));

	}

	protected boolean doOracleSql(remoteSystem s, ConnectionData conData, String remoteSQL) {
		boolean out = false;

		String connString = "jdbc:oracle:thin:@" + s.params.get("ip") + ":" + s.params.get("port") + ":"
				+ s.params.get("sid");

		SqlReturn ret = gData.sqlReq.getSqlFromOracle(connString, conData.user, conData.password, remoteSQL);


		if (ret.isOk) {

			for (Map<String, String> rec : ret.records) {
				out = true;

				String value = rec.get("OUTPUT");
				if (value!=null) s.params.put("result", rec.get("OUTPUT"));
				else s.params.put("result", "-99999");
				
				
			}
		} else {
			out = false;
			s.params.put("result", ret.message);
		}

		return out;
	}
}
