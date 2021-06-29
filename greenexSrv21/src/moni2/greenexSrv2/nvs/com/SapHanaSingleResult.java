package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;

public class SapHanaSingleResult extends BatchJobTemplate implements Runnable {

	public SapHanaSingleResult(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_shedule();
			doCheck();
			reSetRunningFlag_shedule();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	protected void doCheck() {

		String message ="",filter = "";
		
		
		filter = "sap_hana";
		String remoteSQL = read_from_sql_remote_check(this.getClass().getSimpleName(),params.get("job_name"),filter);

		if (remoteSQL.length() < 10) {
			message = "Warning! <p style='color:red;'>SQL for remote check in sql_remote_check for class= " +this.getClass().getSimpleName() 
			+ "and job_name=" + params.get("job_name") + " and filter=" + filter +  " is not found</p>";
			gData.logger.info(message);
			return;
			
		}
		
		
		
		List<remoteSystem> db_systems = readDB_systemsListForCheck();
		
		gData.truncateLog(params.get("job_name"));
		gData.saveToLog("found " + db_systems.size() + " systems to start.", params.get("job_name"));
		
		
		for(remoteSystem s: db_systems) {
		
			message = s.params.get("guid") + ": ip=" + s.params.get("ip") + " sid=" + s.params.get("sid") + " db_type=" + s.params.get("db_type");
			
			
			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));	


			ConnectionData conData = readConnectionParameters(pr);
			
			String SQL_result = "";
			if(doSapHanaSql(s, conData, remoteSQL)) {

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


		
			gData.saveToLog(message, params.get("job_name"));
			gData.sqlReq.saveResult(SQL_result);		
		}

		
	}
	
	protected boolean doSapHanaSql(remoteSystem s, ConnectionData conData, String remoteSQL) {
		boolean out = false;

  		String connString = "jdbc:sap://" + s.params.get("ip") + ":" + s.params.get("port") + "/?autocommit=false"; 

	    SqlReturn ret  = gData.sqlReq.getSqlFromSapHana(connString, conData.user, conData.password, remoteSQL);
	    
	    if (ret.isOk ) {
	    	
	    	for (Map<String, String> rec : ret.records) {
	    		out = true;
				String value = rec.get("OUTPUT");
				if (value!=null) s.params.put("result", rec.get("OUTPUT"));
				else s.params.put("result", "-99999");
	    	}
		} else {
				out= false;
				s.params.put("result", ret.message);
		}
		return out;
}


}
