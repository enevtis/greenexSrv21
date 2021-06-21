package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;

public class AbapSm50WorkProcesses extends BatchJobTemplate implements Runnable {

	public AbapSm50WorkProcesses(globalData gData, Map<String, String> params) {
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
		String message = "";
		List<remoteSystem> db_systems = readABAP_systemsListForCheck();
		gData.saveToLog("found " + db_systems.size() + " systems to check.", params.get("job_name"), false);

		for (remoteSystem s : db_systems) {
			message = s.params.get("short") + " " + s.params.get("ip") + " " + s.params.get("sid") + " "
					+ s.params.get("sysnr");
			message += " " + s.params.get("guid");

			gData.saveToLog(message, params.get("job_name"));

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));

			ConnectionData conData = readConnectionParameters(pr);

			s.params.put("user", conData.user);
			s.params.put("password", conData.password);
			s.params.put("clnt", conData.clnt);
			s.params.put("job_name", params.get("job_name"));
			
			List<String> wpListInsert = new ArrayList();
			wpListInsert.add( "delete from monitor_abap_wp where object_guid='" + s.params.get("guid") + "'");
			
			String SQL = "select app_server from monitor_abap_app_servers ";
			SQL += "where object_guid='" + s.params.get("guid") + "' order by id";
			
			
			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
			String serverName = "";

			if ( records.size() > 0 ) {
				for (Map<String, String> rec : records) {
					serverName = rec.get("app_server");
					do_Sm50_AbapRequest(s.params, wpListInsert,serverName);
				}
			} else {
			
					serverName = "";
					do_Sm50_AbapRequest(s.params, wpListInsert,serverName);
			
			}
			
			gData.sqlReq.saveResult(wpListInsert);
			
		}	
			

	
	
	}

		protected boolean do_Sm50_AbapRequest(Map<String, String> params, List<String> wpList, String serverName) {
			boolean out = false;
			SAPR3 sr3 = new SAPR3(gData, params);

			String insPhrase = "insert into monitor_abap_wp (";
			insPhrase += "`object_guid`, ";
			insPhrase += "`app_server`, ";
			insPhrase += "`wp_index`, ";
			insPhrase += "`wp_typ`, ";
			insPhrase += "`wp_pid`, ";
			insPhrase += "`wp_status`, ";
			insPhrase += "`wp_dumps`, ";
			insPhrase += "`wp_mandt`, ";
			insPhrase += "`wp_bname`, ";
			insPhrase += "`wp_report`, ";
			insPhrase += "`wp_action`, ";
			insPhrase += "`wp_table`, ";
			insPhrase += "`check_date` ";
			insPhrase += ") values ( ";

			String SQL = "";

			
			SqlReturn ret  = sr3.th_WPInfo(serverName);
			
		    if (ret.isOk ) {
		    	
		    	for (Map<String, String> rec : ret.records) {
					
		    		SQL = insPhrase;
		    		SQL += "'" + params.get("guid") + "',";
		    		SQL += "'" + serverName + "',";		    		
		    		SQL += "'" + rec.get("WP_INDEX") + "',";
		    		SQL += "'" + rec.get("WP_TYP") + "',";
		    		SQL += "'" + rec.get("WP_PID") + "',";
		    		SQL += "'" + rec.get("WP_STATUS") + "',";
		    		SQL += "'" + rec.get("WP_DUMPS") + "',";
		    		SQL += "'" + rec.get("WP_MANDT") + "',";
		    		SQL += "'" + rec.get("WP_BNAME") + "',";
		    		SQL += "'" + rec.get("WP_REPORT") + "',";
		    		SQL += "'" + rec.get("WP_ACTION") + "',";
		    		SQL += "'" + rec.get("WP_TABLE") + "',";
		    		SQL += "now()";
		    		SQL += ")";

					wpList.add(SQL);	
					
		    	}
			} else {

				out = false;
			}
			
			
	
	
			return out;
	}

}
