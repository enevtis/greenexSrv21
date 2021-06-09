package moni.greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class checkActiveThreadsHana extends checkTemplate{
	String SQL_for_list = "";
	
	public checkActiveThreadsHana (globalData gData, batchJob job) {
		
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
				
			checkActiveThreadsCount(system);

		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}
	
	protected void checkActiveThreadsCount(remoteSystem s) {


  		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		String SQL = "";

		SQL += "select count(*) as THREADS from SYS.M_SERVICE_THREADS where THREAD_STATE<>'Inactive'";

	
	    List<Map<String , String>> records  = getSqlFromSapHana(connString, user, password, SQL);
	
	    String message = "";
	    int threads = 0;
	    
	    for (Map<String, String> rec : records) {
	    	threads = Integer.valueOf(rec.get("THREADS"));;
	    }
	
	    gData.logger.info("THreads=" + threads);
	    
	    s.params.put("result_text", message);
	    s.params.put("result_number", String.valueOf(threads));
	    
	    SaveToLog(s.getShortDescr(), job.job_name);
	    
	    

}
}
