package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class checkSAPHanaExpenciveSQL extends checkTemplate{


	public checkSAPHanaExpenciveSQL(globalData gData, batchJob job) {
		super(gData, job);
		// TODO Auto-generated constructor stub
	}

	String SQL_for_list = "";
	public void executeCheck() {
		
		
		SQL_for_list = getSqlForDataBase(job); 
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);


		gData.logger.info("checkTopTablesOnDisk for " + systems.size());
		
		
		for(remoteSystem s: systems) {
				

			try {
				remoteCheck(s);
			
			}catch(Exception e) {
				
				gData.logger.info("Error=" + s.params.get("guid") + " text_error=" + e.getMessage());
			}
		}
		
			saveCheckResult(systems);
		
	}

	private void remoteCheck(remoteSystem system) {
		
		String db_version = system.params.get("db_version").toUpperCase();
		
		gData.logger.info(db_version);
		
		if(db_version.contains("ORACLE")) {
				
				// Nothing !

		} else if(db_version.contains("HANA")) {

			
			checkHanaExpenciveSQL(system);

		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}

	private void checkHanaExpenciveSQL(remoteSystem s) {

		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		String SQL = "";
		
		SQL += "select a.connection_id,b.client_host,b.client_ip,b.client_pid,b.user_name, ";
		SQL += "a.statement_hash,a.app_user,a.start_time,a.duration_microsec,a.memory_size, ";
		SQL += "a.object_name,a.statement_string, ";
		SQL += "a.application_source  ";
		SQL += "from M_EXPENSIVE_STATEMENTS a ";
		SQL += "LEFT JOIN M_CONNECTIONS b on a.connection_id=b.connection_id ";
		SQL += "order by memory_size desc limit 30 ";

		
		
		s.records  = getSqlFromSapHana(connString, user, password, SQL);
		SaveToLog(s.getShortDescr() + " records=" + s.records.size(), job.job_name);
		
		
	}
	public void saveCheckResult (List<remoteSystem> systems) {
		
		Connection conn = null ;
		PreparedStatement pStm = null;		
				try {
					
				      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
				      
				      	Statement stmt = conn.createStatement();
						String SQL = "";					

						java.util.Date dateNow = new Date();
					
						for(remoteSystem s: systems) {
							
							
							gData.logger.info("s.guid=" + s.params.get("guid"));
							
							
							for (Map<String, String> rec : s.records) {
							
								SQL = "insert into monitor_sql (";
								SQL += "guid,connection_id,client_host,client_ip,client_pid,user_name,statement_hash,";
								SQL += "app_user,start_time,duration_microsec,memory_size,object_name,statement_string,application_source,check_date";
								SQL += ") values (";																
								SQL += "?,?,?,?,?,?,?,?,?,?,?,?,?,?,now()";
								SQL += ")";								
								
								
								pStm = conn.prepareStatement(SQL);
						  
								int idx = 1;
								String buffer = ""; 
						    

								buffer = s.params.get("guid");	
								pStm.setString(idx, buffer); idx++;

								buffer = rec.get("CONNECTION_ID");	
								pStm.setLong(idx, parseInt(buffer)); idx++;
								
								buffer = rec.get("CLIENT_HOST");	
								pStm.setString(idx, parseString(buffer, 255)); idx++;								
								
								buffer = rec.get("CLIENT_IP");	
								pStm.setString(idx, parseString(buffer, 44)); idx++;									

								buffer = rec.get("CLIENT_PID");	
								pStm.setLong(idx, parseInt(buffer)); idx++;								
								
								buffer = rec.get("USER_NAME");	
								pStm.setString(idx, parseString(buffer, 255)); idx++;	
	
								buffer = rec.get("STATEMENT_HASH");	
								pStm.setString(idx, parseString(buffer, 32)); idx++;	

								buffer = rec.get("APP_USER");	
								pStm.setString(idx, parseString(buffer, 40)); idx++;	

								
								buffer = rec.get("START_TIME");
								pStm.setTimestamp(idx,parseDateTime(buffer));idx++;


								buffer = rec.get("DURATION_MICTROSEC");	
								pStm.setInt(idx, parseInt(buffer)); idx++;								

								buffer = rec.get("MEMORY_SIZE");
								pStm.setFloat(idx, parseFloat(buffer)); idx++;

								buffer = rec.get("OBJECT_NAME");	
								pStm.setString(idx, parseString(buffer, 255)); idx++;	

								buffer = rec.get("STATEMENT_STRING");	
								pStm.setString(idx, parseString(buffer, 255)); idx++;		
								

								buffer = rec.get("APPLICATION_SOURCE");	
								pStm.setString(idx, parseString(buffer, 255)); idx++;
							

								

								
								pStm.executeUpdate();
						
							}
						
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
		
	}	
}
