package moni.greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class checkSapHanaLicense extends checkTemplate {
	
	String SQL_for_list = "";
	
	public checkSapHanaLicense (globalData gData, batchJob job) {
		
		super(gData,job);
		
	}
	
	public void executeCheck() {
		
		SQL_for_list = getSqlForDataBase(job);
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);
		
		for(remoteSystem s: systems) {
				

			try {
				remoteCheck(s);
			
			}catch(Exception e) {
				
				s.params.put("result_text",e.getMessage());
			}
		}
		
		
		
			saveCheckResult(systems);
		
	}
	private void remoteCheck(remoteSystem system) {
		
		String db_version = system.params.get("db_version").toUpperCase();
		
		if(db_version.contains("HANA")) {
				
			checkSapHanaLicense(system);

		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}
protected void checkSapHanaLicense(remoteSystem s) {


  		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		String SQL = "";

		SQL += "select HARDWARE_KEY,START_DATE,EXPIRATION_DATE from  M_LICENSE where PERMANENT='TRUE' and valid='TRUE'";

	
	    List<Map<String , String>> records  = getSqlFromSapHana(connString, user, password, SQL);
	
	    String message = "";
	    
	    
	    for (Map<String, String> rec : records) {
	    	message += "Hardware key=" + rec.get("HARDWARE_KEY") + ", Start date=" + rec.get("START_DATE") + ", Expiration date=" + rec.get("EXPIRATION_DATE");
	    }
	
	    s.params.put("result_text", message);
	    
	    if(records.size() > 0) {
	    	
	    	s.params.put("result_number", "100");
	    	
	    } else {
	    	
	    	s.params.put("result_number", "200");	    	
	    }

	    SaveToLog(s.getShortDescr() + " result=" + s.params.get("result_number"), job.job_name);

}

}
