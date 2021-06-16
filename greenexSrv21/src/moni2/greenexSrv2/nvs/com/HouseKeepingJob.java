package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.batchJob;

public class HouseKeepingJob extends BatchJobTemplate implements Runnable{

	private batchJob job = null;
	
	public HouseKeepingJob (globalData gData, Map<String, String> params) {
		super(gData,params);
		
	}
	
	@Override
	public void run() {

	try {
	
			setRunningFlag_regular();

			deleteOldCheckResults();

			reSetRunningFlag_regular();
			

		} catch(Exception e) {
			gData.logger.info("Error - begin new time");
		}	
		
	}
	

	
	private void deleteOldCheckResults() {
		
		String SQL = "";
		
		List<String> sql_list = new ArrayList<String>();
		
		SQL = "DELETE FROM monitor_results WHERE id IN ( \n"; 
		SQL += "select a.id from monitor_results a \n"; 
		SQL += "left join monitor_schedule b on a.monitor_number = b.number  \n";  
		SQL += "where DATEDIFF(CURDATE(),a.check_date) > b.keep_days)  \n"; 
//		sql_list.add(SQL);		
		
		
		SQL = "delete from monitor_disks where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=101)" ;
		sql_list.add(SQL);
		
		SQL = "delete from mon_top_memory where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=211)" ;
		sql_list.add(SQL);		

		SQL = "delete from mon_hana_alerts where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=212)" ;
		sql_list.add(SQL);		

		SQL = "delete from monitor_sql where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=218)" ;
		sql_list.add(SQL);		

		SQL = "delete from monitor_hana_metrics where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=216)" ;
		sql_list.add(SQL);	
		
		SQL = "delete from problems " ;
		SQL += "WHERE is_fixed='X' AND TIMESTAMPDIFF(HOUR,fixed,NOW()) > 24" ;
		sql_list.add(SQL);		
		
	
		
		
		
		if ( gData.sqlReq.saveResult(sql_list) ) {
			gData.logger.info("Housekeeping job was executed successfully ");
		} else {
			gData.logger.severe("Housekeeping job has ERROR. ");			
			
		};
	}
	
//	private void markOldCheckResults() {
//	
//		
//		Connection conn = null ;
//		
//		try {
//
//		      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
//		      
//		      	Statement stmt = conn.createStatement();
//				String SQL = "";					
//
//				SQL += "select a.id from monitor_results a  ";
//				SQL += "left join monitor_schedule b on a.monitor_number = b.number  ";
//				SQL += "where DATEDIFF(CURDATE(),a.check_date) > b.keep_days  ";		
//				
//				ResultSet rs = stmt.executeQuery(SQL);
//				String in_clause = "";
//
//				while (rs.next()) {
//					in_clause += rs.getInt("id") + ",";
//				}
//
//				in_clause = in_clause.substring(0, in_clause.length() - 1);
//				
//				SQL = "";
//				SQL += "update monitor_results set is_alarm='D' where id in (" + in_clause + ")";				
//				
//				gData.logger.info(SQL);
//				
//				stmt.executeUpdate(SQL);
//				
//				
//				conn.close();
//				
//		
//		} catch (Exception e) {
//
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//			gData.logger.severe(errors.toString());
//
//		} finally {	
//			try {
//				conn.close();
//			} catch (SQLException e) {
//				StringWriter errors = new StringWriter();
//				e.printStackTrace(new PrintWriter(errors));
//				gData.logger.severe(errors.toString());
//			}
//		}
//		
//	}
//	public boolean saveResult (List<String> sqlList) {
//		boolean result = false;
//		
//		
//		if (sqlList == null) return false;
//		
//		if (sqlList.size() == 0) return false;
//		
//		Connection conn = null ;
//				
//				try {
//					
//				      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
//				      	
//				      	for(String s: sqlList) {
//				      		Statement stmt = conn.createStatement();
//						    stmt.executeUpdate(s);
//						}
//
//
//						result = true;
//				      	conn.close();
//						
//						
//				
//				} catch (Exception e) {
//					result = false;
//					StringWriter errors = new StringWriter();
//					e.printStackTrace(new PrintWriter(errors));
//					gData.logger.severe(errors.toString());
//
//				} finally {	
//					try {
//						conn.close();
//					} catch (SQLException e) {
//						result = false;
//						StringWriter errors = new StringWriter();
//						e.printStackTrace(new PrintWriter(errors));
//						gData.logger.severe(errors.toString());
//					}
//				}
//		
//return result;
//	}	
}
