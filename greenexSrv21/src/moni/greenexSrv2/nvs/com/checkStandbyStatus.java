package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class checkStandbyStatus extends checkTemplate {
	
	String SQL_for_list = "";
	
	public checkStandbyStatus (globalData gData, batchJob job) {
		
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
		
		gData.logger.info(db_version);
		
		if(db_version.contains("ORACLE")) {
				
			checkOraStandbyStatus(system);

		} else if(db_version.contains("HANA")) {

			checkHanaStandbyStatus(system);

		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}
	private void checkOraStandbyStatus(remoteSystem s) {
		
		
		Connection conn = null ;
	
	  	String conn_str = "";
   		String user = s.params.get("user");
   		String hash = s.params.get("hash");

		String password = gData.getPasswordFromHash(hash);
   		
 
        	conn_str = "jdbc:oracle:thin:@" + s.params.get("def_ip") + ":" + s.params.get("port") + ":" + s.params.get("sid");
			
//        	System.out.println(conn_str);

       	
        	try {

			      conn = DriverManager.getConnection(conn_str , user , password);	
			      
			      	Statement stmt = conn.createStatement();
					String SQL = "";	
					long stbyDiff = 0;
					
					SQL += "select ((select CURRENT_SCN from V$DATABASE) - (select APPLIED_SCN FROM v$ARCHIVE_DEST WHERE TARGET='STANDBY')) as stby_diff from dual ";
				
					ResultSet rs = stmt.executeQuery(SQL);
					
					

					while (rs.next()) {
						
						stbyDiff = rs.getInt("stby_diff");
					    if (rs.wasNull()) {
					    	s.params.put("result_text","Error: Standby is not working");
					    	s.params.put("result_number","-1");
					    
					    } else {
					    	
					    	s.params.put("result_number", String.valueOf(stbyDiff));
					    	
					    }
							

							}
	
					SaveToLog(s.getShortDescr() + " result=" + s.params.get("result_number"), job.job_name);
					conn.close();
					
			
			} catch (Exception e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
				s.params.put("message", errors.toString());

			} finally {	
				try {
					conn.close();
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(errors.toString());
					s.params.put("message", errors.toString());
				}
			}
		

	
}
	private void checkHanaStandbyStatus(remoteSystem s) {

		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		String SQL = "";

		
		SQL = "";
		SQL += "select distinct REPLICATION_STATUS from SYS.M_SERVICE_REPLICATION ";
		gData.logger.info(SQL);		
		
	    List<Map<String , String>> records  = getSqlFromSapHana(connString, user, password, SQL);
	    String cStatus = "unknown";
	   
	    gData.logger.info(SQL + " records.size()=" + records.size());
	    
	    if (records.size() == 1 ) {

	    		for (Map<String, String> rec : records) {

	    			cStatus = rec.get("REPLICATION_STATUS");
	    			
	    			 gData.logger.info(" rec.get(replication_status)=" + cStatus);
	    			
	    			if (cStatus.toUpperCase().contains("ACTIVE")) {
	    				s.params.put("result_number", "100");
	    				s.params.put("result_text", "OK:Replication is active");
	    			}else {
	    				s.params.put("result_number", "200");
	    				s.params.put("result_text", "OK:Replication is " + cStatus );
	    			}

	    		}	

	    } else {
	    	
			s.params.put("result_number", "-1");
			s.params.put("result_text", "OK:Replication is " + cStatus );
	    	
	    }
	    SaveToLog(s.getShortDescr() + " result=" + s.params.get("result_number"), job.job_name);	
		
	}

}
