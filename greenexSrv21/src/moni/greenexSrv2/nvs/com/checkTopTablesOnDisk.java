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

public class checkTopTablesOnDisk extends checkTemplate{
	String SQL_for_list = "";
	
	public checkTopTablesOnDisk (globalData gData, batchJob job) {
		
		super(gData,job);
		
	}

	public void executeCheck() {
		
		
		SQL_for_list = getSqlForDataBase(job); 
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);


		gData.logger.info("checkTopTablesOnDisk for " + systems.size());
		
		
		for(remoteSystem s: systems) {
				

			try {
				remoteCheck(s);
			
			}catch(Exception e) {
				
				gData.logger.info(s.params.get("guid") + " " + e.getMessage());
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

			checkHanaTopTablesOnDisk(system);

		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}

	private void checkHanaTopTablesOnDisk(remoteSystem s) {

		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		String SQL = "";

		
		SQL = "";
		SQL += "SELECT TOP 20 SCHEMA_NAME,TABLE_NAME,round (DISK_SIZE/1024/1024/1024,2) as \"Gb\" FROM PUBLIC.M_TABLE_PERSISTENCE_STATISTICS ORDER BY DISK_SIZE DESC ";
	
		
	    s.records  = getSqlFromSapHana(connString, user, password, SQL);
	
	    SaveToLog(s.getShortDescr() + " records=" + s.records.size(), job.job_name);
	    
	    
		
	}
	@Override
	public void saveCheckResult (List<remoteSystem> systems) {
		
		
		
		Connection conn = null ;
				
				try {
					
				      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
				      
				      	Statement stmt = conn.createStatement();
						String SQL = "";					
						
						SQL += "insert mon_tables (guid,table_name,size_gb,monitor,check_date) ";
						SQL += " values(?,?,?,?,now())";
						java.util.Date dateNow = new Date();
					
						for(remoteSystem s: systems) {
							
							
							gData.logger.info("s.guid=" + s.params.get("guid"));
							
							
							for (Map<String, String> rec : s.records) {
							
								SQL = "insert mon_tables (guid,table_name,size_gb,monitor,check_date) values(?,?,?,?,?)";
								
								PreparedStatement pStm = conn.prepareStatement(SQL);
						  
								int idx = 1;
								String buffer = ""; 
						    

								buffer = s.params.get("guid");	
								pStm.setString(idx, buffer); idx++;

								buffer = rec.get("SCHEMA_NAME") + "." + rec.get("TABLE_NAME");	
								pStm.setString(idx, buffer); idx++;
								
								pStm.setFloat(idx, Float.valueOf(rec.get("Gb"))); idx++;
								
								pStm.setInt(idx, this.job.number); idx++;


								
								pStm.setTimestamp(idx,new java.sql.Timestamp(dateNow.getTime()));
								

								
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
