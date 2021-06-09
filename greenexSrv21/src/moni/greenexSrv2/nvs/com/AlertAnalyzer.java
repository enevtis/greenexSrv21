package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;
import microsoft.exchange.webservices.data.core.ExchangeService;

public class AlertAnalyzer implements Runnable{
	public globalData gData = new globalData();
	private batchJob job = null;
	private List<String> analyzeList = new ArrayList<String>();
	private List<String> alertsList = new ArrayList<String>();
	private List<String> recoveryList = new ArrayList<String>();	
	private List<alertStatistics> statisticList = new ArrayList<alertStatistics>();
	

	public AlertAnalyzer (globalData gData) {
		this.gData = gData;
		
	}
	
	@Override
	public void run() {

	try {
	
			alertsList.clear();
			analyzeList.clear();
			recoveryList.clear();


// 201 - check_full_backup, 101 check_disks, 102 check_memory, 202 check_oracle_free_space, 203 check_database_size, 204 check_logs_size, 205 check_standby_status,	
// 106 check_cpu	207 check_ds_status

			String strMonitorIDs = "201,102,202,203,204,205,106,207";			

			UniOverLimitAnalyzeAlert(strMonitorIDs);
			gData.sqlReq.saveResult(alertsList);

			UniOverLimitAnalyzeRecovery(strMonitorIDs);
			gData.sqlReq.saveResult(recoveryList);			


			alertsList.clear();
			analyzeList.clear();
			recoveryList.clear();

			
			strMonitorIDs = "101";
			
			
			getSQLforStatistics();

			gData.sqlReq.saveResult(analyzeList);

			

		} catch(Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());	
		}	
		
	}
	private void UniOverLimitAnalyzeAlert(String monitorsList) {
	
		
		String[] monitors = monitorsList.split("\\,"); 
		for(int i=0; i < monitors.length; i++) {
			statisticList.add(new alertStatistics(Integer.valueOf(monitors[i])));
		}
		
		
		
		
		String SQL_list = "";
		
		SQL_list += "";
		SQL_list += "select a.object_guid,a.monitor_number, b.active from monitor_links a  \n";
		SQL_list += "left join monitor_schedule b on a.monitor_number=b.number  \n";
		SQL_list += "where a.monitor_number in (" + monitorsList + ") and b.active='X'; \n";
		
		
		String SQL_analyze = "";

		
			List<Map<String , String>> records_list  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_list);

		
		if (records_list != null ) {
			if (records_list.size() > 0) {
			

//				gData.logger.info(records_list.size());
				
				for (Map<String, String> rec : records_list) {
				String cGuid = rec.get("object_guid");
				String cMoniNum = rec.get("monitor_number");
				
				SQL_analyze = "";

				SQL_analyze += " select a.*, b.value_limit, \n";
				SQL_analyze += " TIMESTAMPDIFF(DAY,a.check_date, now()) as days_check, \n";
				SQL_analyze += " TIMESTAMPDIFF(DAY,a.result_date, now()) as days_result,  \n";
				SQL_analyze += " c.id as 'alert_id', c.check_date as 'alert_date', \n";
				SQL_analyze += " CASE \n";
				SQL_analyze += "     WHEN c.id is null && TIMESTAMPDIFF(DAY,a.check_date, now()) > 2 THEN 'create_old_checking' \n";
				SQL_analyze += "     WHEN c.id is null && TIMESTAMPDIFF(DAY,a.result_date, now()) > b.value_limit THEN 'create_overload_date' \n";
				SQL_analyze += " 	WHEN c.id is null && a.result_number > b.value_limit THEN 'create_overload_number' \n";
				SQL_analyze += "     ELSE 'nothing' \n";
				SQL_analyze += " END as 'analyze' \n";
				SQL_analyze += " from monitor_results a \n";
				SQL_analyze += " left join monitor_links b on a.object_guid=b.object_guid and a.monitor_number=b.monitor_number \n";
				SQL_analyze += " left join monitor_errors c on a.object_guid=c.object_guid and a.monitor_number=c.monitor_number \n";
				SQL_analyze += "  where a.check_date =  \n";
				SQL_analyze += " (select max(check_date) from monitor_results where object_guid='" + cGuid + "' and monitor_number = " + cMoniNum + ") \n";
				SQL_analyze += " and a.object_guid='" + cGuid + "'  and a.monitor_number = " + cMoniNum + " \n";			
				
				

			
				List<Map<String , String>> records_analyze  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_analyze);
				int totalAlerts = 0;

				
				if (records_list != null ) {
					if (records_list.size() > 0) {
			
						totalAlerts = 0;
						
						
						for (Map<String, String> recA : records_analyze) {
						String cAnalyze = recA.get("analyze");
						String cdaysCheck = recA.get("days_check");
						String cdaysResult = recA.get("days_result");
						String cvalueLimit = recA.get("value_limit");
						
						String alertSQL = "";
						
//						gData.logger.info("cGuid=" + cGuid + " cMoniNum=" + cMoniNum + " analyze=" + cAnalyze);
						
						
									switch(cAnalyze) 
									{ 
            							case "create_old_checking": 
            							
            								alertSQL = "insert into monitor_errors (object_guid,monitor_number,result_text,check_date) values ";
            								alertSQL += "('" + cGuid + "', " + cMoniNum + ",' monitoring data is old more 2 days ', now())";
            								alertsList.add(alertSQL);

            							
                						break; 
            							case "create_overload_date": 
            							
            								alertSQL = "insert into monitor_errors (object_guid,monitor_number,result_text,check_date) values ";
            								alertSQL += "('" + cGuid + "', " + cMoniNum + ",' exceeded limit in " + cdaysResult +" days ', now())";
            								alertsList.add(alertSQL);

            		
                						break;
            							case "create_overload_number": 
             							
            								alertSQL = "insert into monitor_errors (object_guid,monitor_number,result_text,check_date) values ";
            								alertSQL += "('" + cGuid + "', " + cMoniNum + ",' exceeded limit in " + cvalueLimit + " ', now())";
            								alertsList.add(alertSQL);
     		
                						break;
                						default:

                							
                							

									}
									
						}

					

						
						
					
					}
				}
			
			
			
			}


		  }			//if (records_list.size() > 0)
		}		// if (records_list != null )

	
	}
	


	private void UniOverLimitAnalyzeRecovery(String monitorsList) {
	
		
		String[] monitors = monitorsList.split("\\,"); 
		for(int i=0; i < monitors.length; i++) {
			statisticList.add(new alertStatistics(Integer.valueOf(monitors[i])));
		}
		
		
		
		
		String SQL_list = "";
		
		SQL_list += "";
		SQL_list += "select a.object_guid,a.monitor_number, b.active from monitor_links a  \n";
		SQL_list += "left join monitor_schedule b on a.monitor_number=b.number  \n";
		SQL_list += "where a.monitor_number in (" + monitorsList + ") and b.active='X'; \n";
		
		
		String SQL_analyze = "";

		
			List<Map<String , String>> records_list  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_list);

		
		if (records_list != null ) {
			if (records_list.size() > 0) {
			

//				gData.logger.info(records_list.size());
				
				for (Map<String, String> rec : records_list) {
				String cGuid = rec.get("object_guid");
				String cMoniNum = rec.get("monitor_number");
				
				SQL_analyze = "";

	
				
				SQL_analyze += " select a.*,b.value_limit,s1.check_date as actual_check_date,s1.result_number,s1.result_text \n";
				SQL_analyze += " ,CASE \n"; 
				SQL_analyze += " WHEN TIMESTAMPDIFF(DAY,s1.check_date, now()) > 2 THEN 'create_old_checking' \n";
				SQL_analyze += " WHEN s1.result_number < b.value_limit THEN 'recovery' \n";
				SQL_analyze += " ELSE 'nothing' \n";
				SQL_analyze += " END as 'analyze'  \n";
				SQL_analyze += " from monitor_errors a \n";
				SQL_analyze += " left join monitor_links b on a.object_guid = b.object_guid and a.monitor_number = b.monitor_number \n";
				SQL_analyze += " left join  \n"; 
				SQL_analyze += " ( \n";
				SQL_analyze += " select * from monitor_results where object_guid = '" + cGuid + "' and monitor_number = " + cMoniNum + " and check_date = \n";
				SQL_analyze += " (select max(check_date) from monitor_results where object_guid = '" + cGuid + "' and monitor_number = " + cMoniNum + ") \n";
				SQL_analyze += " ) s1 on a.object_guid = s1.object_guid and a.monitor_number = s1.monitor_number \n";
				SQL_analyze += " where a.object_guid='" + cGuid + "'  and a.monitor_number = " + cMoniNum + " and b.slot = 1 and a.active='X' \n";
				
		

			
				List<Map<String , String>> records_analyze  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_analyze);
				int totalAlerts = 0;

				
				if (records_list != null ) {
					if (records_list.size() > 0) {
			
						totalAlerts = 0;
						
						
						for (Map<String, String> recA : records_analyze) {
						String cAnalyze = recA.get("analyze");
						String cdaysCheck = recA.get("days_check");
						String cdaysResult = recA.get("days_result");
						String cvalueLimit = recA.get("value_limit");
						String cvalueResult = recA.get("result_number");
						String cvalueCheckDate = recA.get("check_date");
						
						String alertSQL = "";
						
//						gData.logger.info("cGuid=" + cGuid + " cMoniNum=" + cMoniNum + " analyze=" + cAnalyze);
						
						
									switch(cAnalyze) 
									{ 
            							case "recovery": 	// recovered alerts
            							
            								alertSQL = "update monitor_errors set active='',result_text = 'fixed " + recA.get("actual_check_date") + " new value: " + recA.get("result_number") + " limit: " + recA.get("value_limit") + "'" ; 
            								alertSQL += " where id=" + recA.get("id");
            								

            								recoveryList.add(alertSQL);
            								
            								statAddCheckAndAlert(Integer.valueOf(cMoniNum));  
            							
                						break; 
             						default:
                							statAddCheck(Integer.valueOf(cMoniNum));
                							
                							

									}
									
						}

					

						
						
					
					}
				}
			
			
			
			}


		  }			//if (records_list.size() > 0)
		}		// if (records_list != null )

	
	}	
	
protected void checkRecoveryAlerts() {
	
	
	String SQL = "";
	String SQL_recovery = "";
	
	SQL += " select a.*,b.value_limit, s2.last_check_date, s2.result_number, \n";
	SQL += " case \n";
	SQL += " when result_number > 0 && result_number < value_limit then 'recovery' \n";
	SQL += " else 'nothing' \n";
	SQL += " end as 'analyze' \n";
	SQL += " from monitor_errors a \n";
	SQL += " left join monitor_links b on a.object_guid = b.object_guid and a.monitor_number=b.monitor_number \n";
	SQL += " left join  \n";
	SQL += " (select s1.object_guid, s1.monitor_number, s1.last_check_date, a.result_number \n";
	SQL += " from monitor_results a \n";
	SQL += " join  \n";
	SQL += " (select object_guid,monitor_number,max(check_date) as last_check_date from greenex.monitor_results  \n";
	SQL += " group by object_guid,monitor_number) as s1 \n";
	SQL += " on a.object_guid = s1.object_guid and a.monitor_number = s1.monitor_number and a.check_date=s1.last_check_date) s2 \n";
	SQL += " on a.object_guid = s2.object_guid and a.monitor_number = s2.monitor_number \n";
	
	List<Map<String , String>> records  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL);
	
	if (records == null) {
		gData.logger.severe("Recovery list is null!");
		return;
	}
	
	for (Map<String, String> rec : records) {	
		String cAnalyze = rec.get("analyze");

									switch(cAnalyze) 
									{ 
            							case "recovery": 
            							
            								SQL_recovery = "update monitor_errors set active = 'R', result_text = 'Recovery with value: " + rec.get("result_number") + "' + result_text ";
            								SQL_recovery += " where object_guid = '" + rec.get("object_guid") + "' and monitor_number = " + rec.get("object_guid");
            								recoveryList.add(SQL_recovery);
             						break;
                						default:
 
									}
		
	}
	
}

protected void getSQLforStatistics() {
	
	String SQL_analyze_monitor_number = "";
	
	for( alertStatistics st: statisticList) {
		
		SQL_analyze_monitor_number = "update monitor_schedule set last_analyze = now(), checks_analyze = " + st.checked_items + ",";
		SQL_analyze_monitor_number += "alerts_analyze = " + st.alert_items + " where number = " +  st.monitor_number;		
		analyzeList.add(SQL_analyze_monitor_number);	

	}
		
	
}
	
protected void statAddCheck(int monitor_number) {
	
	for( alertStatistics st: statisticList) {

		if (st.monitor_number == monitor_number) {
			
			st.checked_items++;
			
		}
	}
	
}
protected void statAddCheckAndAlert(int monitor_number) {
	
	for( alertStatistics st: statisticList) {

		if (st.monitor_number == monitor_number) {
			
			st.checked_items++;
			st.alert_items++;
			
		}
	}
	
}	
}

class alertStatistics{
	int monitor_number = 0;
	int checked_items = 0;
	int alert_items;
	
	alertStatistics(int monitor_number) {
		this.monitor_number = monitor_number;
	}	
	
}


