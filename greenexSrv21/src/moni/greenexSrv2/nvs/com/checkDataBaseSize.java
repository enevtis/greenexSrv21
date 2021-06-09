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

public class checkDataBaseSize extends checkTemplate {
	
	String SQL_for_list = "";
	
	public checkDataBaseSize (globalData gData, batchJob job) {
		
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
			checkOraDataBaseSize(system);
		
		} else if(db_version.contains("HANA")) {

			checkHanaDataBaseSize(system);
		
		}
		
		else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}
	
	private void checkOraDataBaseSize(remoteSystem s) {
		
		
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
	
					SQL1 += "select  ";
					SQL1 += "( select sum(bytes)/1024/1024/1024 data_size from dba_data_files ) + ";
					SQL1 += "( select nvl(sum(bytes),0)/1024/1024/1024 temp_size from dba_temp_files ) + ";
					SQL1 += "( select sum(bytes)/1024/1024/1024 redo_size from sys.v_$log ) as \"size_GB\"";
					SQL1 += "from dual ";

					
					ResultSet rs1 = stmt1.executeQuery(SQL1);
					
					

						while (rs1.next()) {
							s.params.put("result_number", String.valueOf(rs1.getFloat("size_GB")));
							}
						
				
	
							
//							System.out.println("maxPercent=" + maxPercent + " tableSpaceName="  + maxTableSpaceName );
							
					SaveToLog(s.getShortDescr(), job.job_name);

					conn.close();
					
			
			} catch (Exception e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
				s.params.put("error", errors.toString());
				s.params.put("result_number", "-1");

			} finally {	
				try {
					conn.close();
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(errors.toString());
					s.params.put("error", errors.toString());
					s.params.put("result_number", "-1");
				}
			}
		

	
}	

private void checkHanaDataBaseSize(remoteSystem s) {

		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
	String user = s.params.get("user");
	String hash = s.params.get("hash");
	
	String password = gData.getPasswordFromHash(hash);
	String SQL = "";

	SQL += "select sum (TOTAL_SIZE)/1024/1024/1024 as TOTAL_GB,sum(USED_SIZE)/1024/1024/1024 as USED_GB from M_VOLUME_FILES where FILE_TYPE='DATA'";


    List<Map<String , String>> records  = getSqlFromSapHana(connString, user, password, SQL);

    String message = "";
    float summGB = 0;
    
    for (Map<String, String> rec : records) {
    	
    	summGB += Float.valueOf(rec.get("TOTAL_GB"));
    	message += "Total Gb=" + rec.get("TOTAL_GB") + ", Used Gb=" + rec.get("USED_GB") + "::";
    }

    s.params.put("result_text", message);
    
    if(records.size() > 0) {
    	
    	s.params.put("result_number", String.valueOf(summGB));
    	
    } else {
    	
    	s.params.put("result_number", "-1");	    	
    }

    SaveToLog(s.getShortDescr(), job.job_name);
}
}
