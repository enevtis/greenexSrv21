package moni.greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class checkSAPHanaDiskStatistics extends checkTemplate{

	String SQL_for_list = "";

	public checkSAPHanaDiskStatistics(globalData gData, batchJob job) {
		super(gData, job);
		
	}
	public void executeCheck() {
		
		
		SQL_for_list = getSqlForDataBase(job); 
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);


		gData.logger.fine("checkSAPHanaDiskStatistics for " + systems.size());
		
		
		for(remoteSystem s: systems) {
				

			try {
				remoteCheck(s);
			
			}catch(Exception e) {
				
				gData.logger.info("Error=" + s.params.get("guid") + " text_error=" + e.getMessage());
			}
		}
		
		saveCheckHanaMetricResult(systems);
	}
	
	private void remoteCheck(remoteSystem system) {
		
		String db_version = system.params.get("db_version").toUpperCase();
		
		gData.logger.info(db_version);
		
		if(db_version.contains("ORACLE")) {
				
				// Nothing !

		} else if(db_version.contains("HANA")) {

			
			checkHanaDiskStatistics(system);

		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}	
	private void checkHanaDiskStatistics(remoteSystem s) {

		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
		List<Map<String , String>> records_prep =  
				

				gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), "select param_value from monitor_ext_data where param_name='SqlHanaDiskStat' and object_guid='" + s.params.get("guid") + "'");
		

		
		
		String SQL = "";
	    for (Map<String, String> rec : records_prep) {
	    	SQL = rec.get("param_value");
	    }		
		
	    gData.logger.fine("prepSQL=" + SQL);
		
		
		s.records  = getSqlFromSapHana(connString, user, password, SQL);
		SaveToLog(s.getShortDescr() + " records=" + s.records.size(), job.job_name);
		

		
	}	
}
