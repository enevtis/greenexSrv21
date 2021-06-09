package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;
import simplecrypto.nvs.com.SimpleCrypto;



public class checkFullBackup extends checkTemplate {
	
	String SQL_for_list = "";
	
	public checkFullBackup (globalData gData, batchJob job) {		
		super(gData,job);		
	}


	public void executeCheck() {
		
		List<remoteSystem> systems = getListForCheck();
		
		SaveToLog("total before=" + systems.size(),job.job_name);
	
		int idx = 0;
		
		for(remoteSystem s: systems) {
			
			idx++;
			
			try {
				remoteCheck(s);

				}catch(Exception e) {
					s.params.put("message",e.getMessage());					
				}catch(Error e) {					
					SaveToLog(e.getMessage(),job.job_name);
				}
		
		}
		

			SaveToLog("total after =" + systems.size(),job.job_name);		
		
			saveCheckResult(systems);
		

		
	}
	private void remoteCheck(remoteSystem system) throws Exception {
		
		String db_version = system.params.get("db_version").toUpperCase();
		
		
		if(db_version.contains("HANA")) {
		
			try {			
				checkSAPHanaFullBackup(system);
			} catch (Exception e) {
				SaveToLog(e.getMessage(),"backup_trace");
			}
		
		}else if (db_version.contains("ORACLE")){
		
			try {
//				checkOracleFullBackup(system);
			} catch (Exception e) {
				SaveToLog(e.getMessage(),"backup_trace");
			}				
		
		} else {
		
			system.params.put("result_text", "Check canceled. Type Database " + db_version + "is unknown");
		
		}

		
	}
	
	private void checkOracleFullBackup(remoteSystem s) throws Exception{
		
		
			Connection conn = null ;
		
		  	String conn_str = "";
	   		String user = s.params.get("user");
	   		String hash = s.params.get("hash");
	   		
	   		String password = "";
	   		
	   		
	   		
	   		
	   		try {
				password = SimpleCrypto.decrypt(gData.SecretKey, hash);
			} catch (Exception e1) {
				
				gData.logger.severe(e1.getMessage());

			}
	   		

	        	conn_str = "jdbc:oracle:thin:@" + s.params.get("def_ip") + ":" + s.params.get("port") + ":" + s.params.get("sid");
				gData.logger.fine(conn_str + " " + user + " " + hash );	
				
				try {

				      conn = DriverManager.getConnection(conn_str , user , password);	
				      
				      	Statement stmt = conn.createStatement();
						String SQL = "";					
						
//						SQL += "select max(end_time) as backup_date from V$RMAN_BACKUP_JOB_DETAILS where status='COMPLETED' and (input_type='DB INCR' or input_type='DB FULL') ";
						

						SQL += "select (extract( day from backup_diff ) * 24 + extract( hour from backup_diff )) backup_hours from \n";
						SQL += "( select systimestamp - cast(max(end_time) as timestamp) as backup_diff from V$RMAN_BACKUP_JOB_DETAILS \n";
						SQL += " where status='COMPLETED' and (input_type='DB INCR' or input_type='DB FULL')) \n";

						ResultSet rs = stmt.executeQuery(SQL);

						while (rs.next()) {
							
							s.params.put("result_number", String.valueOf(rs.getFloat("backup_hours")));
							gData.logger.info(conn_str + " " + s.params.get("result_number"));

						}

						SaveToLog(s.getShortDescr() + " result=" + s.params.get("result_number"), job.job_name);
						conn.close();
						
				
				} catch (Exception e) {

					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(errors.toString());
					s.params.put("message", errors.toString());

				} catch (Error e) {
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


	private void checkSAPHanaFullBackup(remoteSystem s) {
	    
	  	String connectionString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false";
   		String user = s.params.get("user");
   		String hash = s.params.get("hash");
   		String password = gData.getPasswordFromHash(hash);
		String SQL = "";
   		
		SQL += " select SECONDS_BETWEEN( \n";
		SQL += "		(select max(utc_end_time) from  M_BACKUP_CATALOG where ENTRY_TYPE_NAME='complete data backup' and STATE_NAME='successful'), \n";
		SQL += "		CURRENT_TIMESTAMP  \n";
		SQL += "		) / 3600 as \"backup_hours\" \n";
		SQL += "		from dummy;	 \n";	
		
		SaveToLog(s.getShortDescr() + " step1 ","backup_trace");
		
		RequestSqlThread req = new RequestSqlThread(gData,connectionString,user,password,SQL, s) ;
   		req.start() ;
	    try {
	        Thread.sleep(2000) ;
	    } catch (InterruptedException e) {}
	    
		if (s.records != null) {

			if (s.records.size() > 0) {

				int indx = 0;
				for (Map<String, String> rec : s.records) {
					s.params.put("result_number", rec.get("backup_hours"));
					SaveToLog(s.params.get("result_number") + " step2 ","backup_trace");
				}
			}
		}
		
		SaveToLog(s.getShortDescr() + " step3 ","backup_trace");
		
	}
	
	private void checkSAPHanaFullBackupOld(remoteSystem s) throws Exception{
	
	Connection conn = null ;
	
	  	String conn_str = "";
   		String user = s.params.get("user");
   		String hash = s.params.get("hash");
   		
   		String password = "";
   		
   		
   		
   		
   		try {
			password = SimpleCrypto.decrypt(gData.SecretKey, hash);
		} catch (Exception e1) {
			
			gData.logger.severe(e1.getMessage());

		}
   		

        	conn_str = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false&socketTimeout=2000"; 
        	
        	SaveToLog(conn_str,"backup_trace");
        	
			try {

				
				SaveToLog(conn_str + "_" + user + "_" + password,"backup_trace");
				
				
				conn = DriverManager.getConnection(conn_str , user , password);	
			      
			      	Statement stmt = conn.createStatement();
					String SQL = "";					

					SQL += " select SECONDS_BETWEEN( \n";
					SQL += "		(select max(utc_end_time) from  M_BACKUP_CATALOG where ENTRY_TYPE_NAME='complete data backup' and STATE_NAME='successful'), \n";
					SQL += "		CURRENT_TIMESTAMP  \n";
					SQL += "		) / 3600 as \"backup_hours\" \n";
					SQL += "		from dummy;	 \n";				
					
					SaveToLog(SQL,"backup_trace");
					
					
					ResultSet rs = stmt.executeQuery(SQL);

					while (rs.next()) {
						
						SaveToLog("result=" + rs.getFloat("backup_hours"),"backup_trace");
						
						s.params.put("result_number", String.valueOf(rs.getFloat("backup_hours")));

					}

					SaveToLog(s.getShortDescr() + " last_backup=" + s.params.get("result_number"), job.job_name);
					
					conn.close();
					
			
			} catch (Exception e) {

				SaveToLog(e.getMessage(),"backup_trace");
				
			} 
	
	}

private List<remoteSystem> getListForCheck() {
	
	List<remoteSystem> systems = new ArrayList<>();
	
	Connection conn = null ;
			
			try {
//				Class.forName(gData.driverName);
			      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
			      
			      	Statement stmt = conn.createStatement();
					String SQL = "";					

/*					
					SQL += "select a.*, b.user,b.hash,c.short as 'db_version' from db_systems a  ";
					SQL += "left join monitor_conn_data b on a.guid=b.object_guid  ";
					SQL += "left join db_typ c on a.db_type = c.guid  ";
					SQL += "left join monitor_links d on a.guid = d.object_guid  ";
					SQL += "where d.monitor_number=" +  job.number + " and d.active='X';  ";
*/					
					
					SQL = getSqlForDataBase(job);
					
					ResultSet rs = stmt.executeQuery(SQL);

					while (rs.next()) {

						remoteSystem s = new remoteSystem();
						
						s.params.put("guid", rs.getString("guid"));
						s.params.put("short", rs.getString("short"));
						s.params.put("sid", rs.getString("sid"));
						s.params.put("sysnr", rs.getString("sysnr"));
						s.params.put("def_ip", rs.getString("def_ip"));
						s.params.put("port", rs.getString("port"));
						s.params.put("multi_sid", rs.getString("multi_sid"));
						s.params.put("role_typ", rs.getString("role_typ"));
						s.params.put("backup_limit", String.valueOf(rs.getInt("backup_limit")));
						s.params.put("user", rs.getString("user"));
						s.params.put("hash", rs.getString("hash"));
						s.params.put("db_version", rs.getString("db_version"));
						
						s.params.put("message", "OK");

						systems.add(s);

					}

					conn.close();
					
			
			} catch (Exception e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());

			} finally {	
				try {
					conn.close();
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(errors.toString());
				}
			}
return systems;		
	
}

}
