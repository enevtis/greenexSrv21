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

public class checkDataServiceMsgStatus extends checkTemplate{

	String SQL_for_list = "";
	
	
	public checkDataServiceMsgStatus(globalData gData, batchJob job) {
		super(gData, job);
		// TODO Auto-generated constructor stub
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
				
			
			checkDataServiceMsgStatus(system);

		}  else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}
	private void checkDataServiceMsgStatus(remoteSystem s) {
		
		
		Connection conn = null ;
	
	  	String conn_str = "";
   		String user = s.params.get("user");
   		String hash = s.params.get("hash");

		String password = gData.getPasswordFromHash(hash);
   		
 
        	conn_str = "jdbc:oracle:thin:@" + s.params.get("def_ip") + ":" + s.params.get("port") + ":" + s.params.get("sid");
			
 
       	
        	try {

			      conn = DriverManager.getConnection(conn_str , user , password);	
			      
			      	Statement stmt = conn.createStatement();
					String SQL = "";	
					long dsDiff = 0;
					
					SQL += "select (cast (systimestamp as date) - cast(to_timestamp_tz((select substr(max(TIMESTAMP),1,12) || ' +3:00' from SAPSR3.ZWT_MSGC where TYPE='NLHUBLEG'), 'YYYYMMDDHH24MI TZH:TZM') as date))* 1440 as diff_min from dual ";
				
					ResultSet rs = stmt.executeQuery(SQL);
					
					

					while (rs.next()) {
						
						dsDiff = rs.getInt("diff_min");
					    if (rs.wasNull()) {
					    	s.params.put("result_text","Error: checking of DataService returns null!! ");
					    	s.params.put("result_number","-1");
					    
					    } else {
					    	
					    	gData.logger.info("diff_min=" + dsDiff);
					    	s.params.put("result_number", String.valueOf(dsDiff));
					    	
					    }
							

							}
		
					SaveToLog(s.getShortDescr(), job.job_name);

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
}
