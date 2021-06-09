package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class AlertAnalyzerDisks implements Runnable{
	public globalData gData = new globalData();
	private batchJob job = null;
	
	private HashMap<String, String> existedAlertsList = new HashMap<String, String>();
	private List<String> alertsList = new ArrayList<String>();
	private List<String> recoveryList = new ArrayList<String>();	
	private List<alertStatistics> statisticList = new ArrayList<alertStatistics>();

	public AlertAnalyzerDisks (globalData gData) {
		this.gData = gData;
		
	}

	@Override
	public void run() {

		try {

			alertsList.clear();
			existedAlertsList.clear();
			recoveryList.clear();
	
			readExistedAlerts("101");

			DiskAlertLimitAnalyzer("101");
			gData.sqlReq.saveResult(alertsList);			
			
			DiskRecoveryLimitAnalyzer("101");			
			gData.sqlReq.saveResult(recoveryList);	

		
		
		
		} catch(Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());	
		}			
	}


	private void DiskRecoveryLimitAnalyzer(String monitorsList) {
		
		String[] monitors = monitorsList.split("\\,"); 
		for(int i=0; i < monitors.length; i++) {
			statisticList.add(new alertStatistics(Integer.valueOf(monitors[i])));
		}
		
		
		
		
		String SQL_list = "";
		
		SQL_list += "";
		SQL_list += "select object_guid,monitor_number,name from monitor_errors a  \n";
		SQL_list += "where monitor_number in (" + monitorsList + ") and active='X'; \n";
		
		
		String SQL_analyze = "";

		
			List<Map<String , String>> records_list  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_list);

		
		if (records_list != null ) {
			if (records_list.size() > 0) {
			


				
				for (Map<String, String> rec : records_list) {
				String cGuid = rec.get("object_guid");
				String cMoniNum = rec.get("monitor_number");
				String cName = rec.get("name");
				SQL_analyze = "";

	
				SQL_analyze += " select s1.*,b.min_free_gb,c.short,  \n";
				SQL_analyze += " CASE  \n";
				SQL_analyze += " WHEN TIMESTAMPDIFF(DAY,s1.check_date, now()) > 2 THEN 'create_old_checking'  \n";
				SQL_analyze += " WHEN b.min_free_gb is not null && (max_size_gb-used_size_gb) > b.min_free_gb THEN 'recovery'  \n";
				SQL_analyze += " WHEN b.min_free_gb is null && 100*((max_size_gb-used_size_gb)/max_size_gb) > 15 THEN 'recovery'   \n"; 
				SQL_analyze += " ELSE 'nothing'    \n";
				SQL_analyze += " END as 'analyze',    \n";
				SQL_analyze += " CASE    \n";
				SQL_analyze += " WHEN b.min_free_gb is null THEN '> 15% free'   \n";
				SQL_analyze += " WHEN b.min_free_gb is not null THEN concat('> ',b.min_free_gb,' Gb free')	  \n";  
				SQL_analyze += " END as 'reason'   \n";
				SQL_analyze += " from (select *   \n";
				SQL_analyze += "  from monitor_disks    \n";
				SQL_analyze += "  where server_guid='" + cGuid + "' and name='" + cName + "' and check_date=   \n";
				SQL_analyze += "  (select max(check_date) from monitor_disks where server_guid='" + cGuid + "')) s1  \n";
				SQL_analyze += " left join monitor_disks_min b on s1.server_guid=b.server_guid and s1.name=b.name   \n";
				SQL_analyze += " left join v_objects_all c on s1.server_guid = c.guid  \n";
	
	
				

			
				List<Map<String , String>> records_analyze  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_analyze);
				int totalAlerts = 0;

				
				if (records_list != null ) {
					if (records_list.size() > 0) {
			
						totalAlerts = 0;
						int diskIndex = 1000;
						
						for (Map<String, String> recA : records_analyze) {
						String cAnalyze = recA.get("analyze");
						
						String alertSQL = "";
						
					
						
									switch(cAnalyze) 
									{ 

            							case "recovery": 	// recovered alerts
            							
            								String buffer = " used " + recA.get("used_size_gb") + " (from " + recA.get("max_size_gb") + ") Gb this is " + recA.get("reason");
            								alertSQL = "update monitor_errors set active='',result_text = 'fixed " + recA.get("check_date") + " new value: " + buffer + "'" ; 
            								alertSQL += " where id=" + recA.get("id");
            								

            								recoveryList.add(alertSQL);
   
 
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

	private void DiskAlertLimitAnalyzer(String monitorsList) {
		
		String[] monitors = monitorsList.split("\\,"); 
		for(int i=0; i < monitors.length; i++) {
			statisticList.add(new alertStatistics(Integer.valueOf(monitors[i])));
		}
		
		
		
		
		String SQL_list = "";
		
		SQL_list += "";
		SQL_list += "select a.object_guid,a.monitor_number, b.active from monitor_links a  \n";
		SQL_list += "left join monitor_schedule b on a.monitor_number=b.number  \n";
		SQL_list += "where a.monitor_number in (" + monitorsList + ") and b.active='X' and b.slot=1; \n";
		
		
		String SQL_analyze = "";

		
			List<Map<String , String>> records_list  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_list);

		
		if (records_list != null ) {
			if (records_list.size() > 0) {
			

//				gData.logger.info(records_list.size());
				
				for (Map<String, String> rec : records_list) {
				String cGuid = rec.get("object_guid");
				String cMoniNum = rec.get("monitor_number");
				
				SQL_analyze = "";

	
				SQL_analyze += " select s1.*,b.min_free_gb,c.short, round(s1.max_size_gb-s1.used_size_gb,1) as free_gb, \n";
				SQL_analyze += " CASE \n";
				SQL_analyze += " WHEN TIMESTAMPDIFF(DAY,s1.check_date, now()) > 2 THEN 'create_old_checking' \n";
				SQL_analyze += " WHEN b.min_free_gb is not null && (max_size_gb-used_size_gb) < b.min_free_gb THEN 'alert' \n";
				SQL_analyze += " WHEN b.min_free_gb is null && 100*((max_size_gb-used_size_gb)/max_size_gb) < 15 THEN 'alert' \n";  	  
				SQL_analyze += " ELSE 'nothing'  \n";
				SQL_analyze += " END as 'analyze',  \n";
				SQL_analyze += " CASE   \n";
				SQL_analyze += " WHEN b.min_free_gb is null THEN '< 15% free'  \n";
				SQL_analyze += " WHEN b.min_free_gb is not null THEN concat('< ',b.min_free_gb,' Gb free')	  \n";  
				SQL_analyze += " END as 'reason'  \n";
				SQL_analyze += " from \n";
				SQL_analyze += " (select * \n";
				SQL_analyze += " from monitor_disks  \n";
				SQL_analyze += " where server_guid='" + cGuid + "' and check_date= \n";
				SQL_analyze += " (select max(check_date) from monitor_disks where server_guid='" + cGuid + "')) s1  \n";
				SQL_analyze += " left join monitor_disks_min b on s1.server_guid=b.server_guid and s1.name=b.name  \n";
				SQL_analyze += " left join v_objects_all c on s1.server_guid = c.guid  \n";				
			
				

			
				List<Map<String , String>> records_analyze  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL_analyze);
				int totalAlerts = 0;

				
				if (records_list != null ) {
					if (records_list.size() > 0) {
			
						totalAlerts = 0;
						int diskIndex = 1000;
						
						for (Map<String, String> recA : records_analyze) {
						String cAnalyze = recA.get("analyze");
						
						String alertSQL = "";
						
					
						
									switch(cAnalyze) 
									{ 
            							
            							case "create_old_checking": 
 
            								alertSQL = "insert into monitor_errors (object_guid,monitor_number,result_text,check_date) values ";
            								alertSQL += "('" + cGuid + "', " + cMoniNum + ",' monitoring data is old more 2 days ', now())";
            								
            								addFreshAlert(alertsList,(cGuid + ":" + recA.get("name")),alertSQL);
            								
  
  
            							
                						break; 
            							case "alert": 

            								String buffer = recA.get("short") + ":" + recA.get("name") + " used " + recA.get("used_size_gb") + " (from ";
            								buffer += recA.get("max_size_gb") + ") free:" + recA.get("free_gb") + " Gb, this is " + recA.get("reason");
            							
            								 
            								alertSQL = "insert into monitor_errors (object_guid,monitor_number,name,result_text,check_date) values ";
            								alertSQL += "('" + cGuid + "', " + cMoniNum + ",'" + recA.get("name") + "','" + buffer + "', now())";
 
            								addFreshAlert(alertsList,(cGuid + ":" + recA.get("name")), alertSQL);
            								
            								//gData.logger.info(alertSQL); // For debug only!!!
            								
    		
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
	
	
	
	
	
	
protected void addFreshAlert(List<String> alertsList , String keyText, String sqlText) {
	
	if(!existedAlertsList.containsKey(keyText)) {
		
		alertsList.add(sqlText);
		
	}
	
	
	
}
	
	
	
	
	protected void readExistedAlerts(String monitors) {
	
	String SQL = "select * from monitor_errors where active='X' and monitor_number in (" + monitors + ")";	

	String recepientsAll = "";

		List<Map<String , String>> result_rows  = gData.sqlReq.getSelect(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"), SQL);

			for (Map<String, String> rowA : result_rows) {
		
				existedAlertsList.put(rowA.get("object_guid") + ":" + rowA.get("name"),rowA.get("result_text"));
			}
	
}

}