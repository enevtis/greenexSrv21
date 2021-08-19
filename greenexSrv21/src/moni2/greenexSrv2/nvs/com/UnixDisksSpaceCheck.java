package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class UnixDisksSpaceCheck extends BatchJobTemplate implements Runnable {

	public String nowForSQL = "";
	
	public UnixDisksSpaceCheck(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {
		try {

			nowForSQL = gData.nowForSQL();
			
			setRunningFlag_shedule();
			doCheck();
			reSetRunningFlag_shedule();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}


	}

	protected void doCheck() {

		String SQL="select count(*) as output from monitor_links WHERE monitor_number=" + params.get("job_number");
		String kvo_links = gData.sqlReq.readOneValue(SQL);
		
		int counter = 0;
		
		
		
		gData.logger.info("<p style='color:blue;'>Start " + params.get("job_name") + "</p>");
//		gData.sqlReq.saveResult("update monitor_schedule set active=' ' where id=" + params.get("job_id"));

		List<remoteSystem> servers = readServersListForCheck(params.get("job_number"));

		List<String> sqlList = new ArrayList<String>();
		String SQL_result = "";

		gData.truncateLog(params.get("job_name"));
		gData.saveToLog("found " + servers.size() + " systems to start.", params.get("job_name"));
		
		for (remoteSystem s : servers) {

			
//			  gData.logger.info("*** <p class='check_db_runnig'>Running " +
//			  s.params.get("ip") + " " + s.params.get("hostname") + " " +
//			  s.params.get("os_type") + " " + params.get("job_name") + "</p>");
			  
			  ObjectParametersReader parReader = new ObjectParametersReader(gData);
			  PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));	

			  ConnectionData conData = readConnectionParameters(pr);
			  gData.saveToLog(conData.ip + " " + conData.user ,params.get("job_name"));
			  
			  
			  String remoteSshText = readFrom_ssh_remote_check(s.params.get("os_type"));
			  
			  gData.saveToLog("GUID=" +s.params.get("guid") + " IP=" + s.params.get("ip") + " remoteSshText= " + remoteSshText, params.get("job_name"));
			  gData.saveToLog(s.params.get("guid") + " " + s.params.get("ip") + "remoteSshText= " + remoteSshText, params.get("job_name"));
			  
			  
			  
			  if (doSshRequest(s, conData, remoteSshText)) {
			  
			 String result = s.params.get("result");
			 String lines[] = result.split("\\r?\\n");
			 
			 
			 
			 sqlList.clear();
			 sqlList.add("delete from monitor_disks where server_guid='" + s.params.get("guid") + "'");	

			 for(String line : lines) {
	
				 String[] parts = line.split("\\s+"); 
//				SQL_result = "insert into monitor_disks (";
//				SQL_result +="`server_guid`,`name`,`max_size_gb`,`used_size_gb`) values (";
//				SQL_result +="'" + s.params.get("guid") + "',";
//				SQL_result +="'" + parts[2] + "',";
//				SQL_result += parts[0] + ",";
//				SQL_result += parts[1] + ")";	

				String diskName = parts[2];
				String maxValue = parts[0];
				String usedValue = parts[1];
				
				float fMaxValue = Float.valueOf(maxValue) / 1024 /1024 ;
				float fUsedValue = Float.valueOf(usedValue) / 1024 /1024;

				SQL_result = "insert into monitor_disks (";
				SQL_result += "`server_guid`,";
				SQL_result += "`name`,";
				SQL_result += "`max_size_gb`,";
				SQL_result += "`used_size_gb`,";
				SQL_result += "`check_date`";
				SQL_result += ") values (";
				SQL_result += "'" + s.params.get("guid") + "',";
				SQL_result += "'" + diskName + "',";
				SQL_result += "" + String.format("%.1f", fMaxValue) + ",";
				SQL_result += "" + String.format("%.1f", fUsedValue) + ",";
				SQL_result += "'" + nowForSQL + "'";
				SQL_result += ")";


//				gData.saveToLog(SQL_result, params.get("job_name"));
				
				sqlList.add(SQL_result);
				
			 }
			 
			   
				  
			  SQL_result = "insert into monitor_results ( "; 
			  SQL_result += "`object_guid`,`monitor_number`,"; 
			  SQL_result += "`check_date`,`result_number`,"; 
			  SQL_result += "`result_text`,`is_error`) values ("; 
			  SQL_result += "'" +  s.params.get("guid") + "',"; 
			  SQL_result += "" + params.get("job_number") + ","; 
			  SQL += "'" + nowForSQL + "',";
//			  SQL_result += "now(),"; 
			  SQL_result += "0,";
			  SQL_result += "'ok',"; 
			  SQL_result += "''"; 
			  SQL_result += ")";
			  
			  sqlList.add(SQL_result);
			  
			  
			  } else {
			  
			  SQL_result += "insert into monitor_results ( "; 
			  SQL_result +=  "`object_guid`,`monitor_number`,`check_date`,`result_number`,"; 
			  SQL_result +=  "`result_text`,`is_error`) values ("; 
			  SQL_result += "'" +  s.params.get("guid") + "',"; 
			  SQL_result += "" + params.get("job_number") +  ","; 
			  SQL_result += "now(),"; 
			  SQL_result += "0,"; 
			  SQL_result += "'" +  s.params.get("result") + "',"; 
			  SQL_result += "'X'"; 
			  SQL_result += ")";
			  
			  }
			  
			  counter ++;
			  
			  gData.saveToLog(s.params.get("guid") + " " + s.params.get("ip") + "SQL_result= " + SQL_result, params.get("job_name"));
			  gData.sqlReq.saveResult(sqlList);
			  
			 
		}

//		gData.logger.info("*** <p style='color:blue;'>End " + params.get("job_name") + "</p>");
//		gData.sqlReq.saveResult(
//				"update monitor_schedule set active='X',last_analyze=now(),checks_analyze=checks_analyze+1 where id="
//						+ params.get("job_id"));
		
		String message = params.get("job_name") + " finished succesfully. Quantity links =" + kvo_links + " counter=" + counter;
		
		gData.saveToLog(message, params.get("job_name"));

	}
	protected String readFrom_ssh_remote_check(String os_type) {
		String out = "";
		String SQL = "";
		
		String filter = "";
		if (os_type.toUpperCase().contains("LINUX")) {
			filter = "LINUX";
		}else if (os_type.toUpperCase().contains("AIX")) {
			filter = "AIX";
		}else {
			filter = "UNKNOWN";
		}
		
		SQL += "SELECT * FROM ssh_remote_check WHERE className='" + this.getClass().getSimpleName() + "'";
		SQL += " and job_name='" + params.get("job_name") + "'";
		SQL += " and os='" + filter + "'";
		SQL += " order by id";
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += rec.get("sshText") + " ";

				}
			}
		}

		if (out.isEmpty()) out = "echo 'error reading from ssh_remote_check'";
		
		return out;
	}


}
