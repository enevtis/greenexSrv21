package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class checkSAPHanaAlerts extends checkTemplate{
	String SQL_for_list = "";
	
	public checkSAPHanaAlerts (globalData gData, batchJob job) {
		super(gData,job);
	}

	public void executeCheck() {
		
		
		SQL_for_list = getSqlForDataBase(job); 
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);
		
		
		for(remoteSystem s: systems) {
				

			try {
				remoteCheck(s);
			
			}catch(Exception e) {
				
				gData.logger.severe("Error=" + s.params.get("guid") + " text_error=" + e.getMessage());
			}
		}
		
			saveCheckResult(systems);
		
	}

	private void remoteCheck(remoteSystem system) {
		
		String db_version = system.params.get("db_version").toUpperCase();
		
		if(db_version.contains("ORACLE")) {
				
				// Nothing !

		} else if(db_version.contains("HANA")) {

			
			checkHanaDatabaseAlerts(system);

		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}

	private void checkHanaDatabaseAlerts(remoteSystem s) {

		String connString = "jdbc:sap://" + s.params.get("def_ip") + ":" + s.params.get("port") + "/?autocommit=false"; 
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		String SQL = readTextFromJar("/resources/saphana/HANA_StatisticsServer_Alerts_Current.txt");
		
		
		
		SQL = "select s1.* from ( " + SQL + " ) s1  where SECONDS_BETWEEN(s1.alert_time,CURRENT_TIMESTAMP)/60 < " + this.job.interval ;	
		
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

						java.util.Date dateNow = new Date();
					
						for(remoteSystem s: systems) {
							
							
							gData.logger.info("s.guid=" + s.params.get("guid"));
							
							
							for (Map<String, String> rec : s.records) {
							
								SQL = "insert into mon_hana_alerts (guid, alert_id, alert_time, rating, alert_details, check_date) values(?,?,?,?,?,?)";
								
								PreparedStatement pStm = conn.prepareStatement(SQL);
						  
								int idx = 1;
								String buffer = ""; 
						    

								buffer = s.params.get("guid");	
								pStm.setString(idx, buffer); idx++;

								buffer = rec.get("ALERT_ID").trim();	
								pStm.setInt(idx, Integer.valueOf(buffer)); idx++;
								
								
								buffer = rec.get("ALERT_TIME");
								gData.logger.info(buffer);								
	
//								2019/11/27 09:30:10
								
						        SimpleDateFormat df1 = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
						        Date alertDate = df1.parse(buffer);
						
						        
								pStm.setTimestamp(idx,new java.sql.Timestamp(alertDate.getTime())); idx++;
								
								
								
								buffer = rec.get("RATING");	
								if(buffer.length() > 6) buffer = buffer.substring(0,6);								
								pStm.setString(idx, buffer); idx++;
								

								
								buffer = rec.get("ALERT_DETAILS");									
								if(buffer.length() > 510) buffer = buffer.substring(0,510);
								pStm.setString(idx, buffer); idx++;
							

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
