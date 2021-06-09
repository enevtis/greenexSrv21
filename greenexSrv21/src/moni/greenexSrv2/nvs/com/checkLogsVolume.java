package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import greenexSrv2.nvs.com.globalData;

public class checkLogsVolume extends checkTemplate {
	
	String SQL_for_list = "";
	
	public checkLogsVolume (globalData gData, batchJob job) {
		
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
		
		if(db_version.contains("ORACLE")) {
			checkOraLogVolumeSize(system);
		
		} else if(db_version.contains("HANA")) {

			checkHanaLogVolumeSize(system);
		
		}
		
		else {
			system.params.put("message", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}
	
	private void checkOraLogVolumeSize(remoteSystem s) {
		
		
		Connection conn = null ;
	
	  	String conn_str = "";
   		String user = s.params.get("user");
   		String hash = s.params.get("hash");

		String password = gData.getPasswordFromHash(hash);
   		
 
        	conn_str = "jdbc:oracle:thin:@" + s.params.get("def_ip") + ":" + s.params.get("port") + ":" + s.params.get("sid");
			
//        	System.out.println(conn_str);

       	
        	try {

			      conn = DriverManager.getConnection(conn_str , user , password);	
			      
			      	Statement stmt1 = conn.createStatement();
					String SQL1 = "";					
	

					SQL1 += "select SIZE_GB FROM ( SELECT SUM(BLOCKS * BLOCK_SIZE)/1024/1024/1024 SIZE_GB ";
					SQL1 += "FROM V$ARCHIVED_LOG where COMPLETION_TIME > ( sysdate - 1 )) GROUP BY SIZE_GB  ";
					

					
					ResultSet rs1 = stmt1.executeQuery(SQL1);
					
					

						while (rs1.next()) {
							s.params.put("result_number", String.valueOf(rs1.getFloat("SIZE_GB")));
							}
						
				
	
							
//							System.out.println("maxPercent=" + maxPercent + " tableSpaceName="  + maxTableSpaceName );
							

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

	private void checkHanaLogVolumeSize(remoteSystem s) {
		
	}

}
