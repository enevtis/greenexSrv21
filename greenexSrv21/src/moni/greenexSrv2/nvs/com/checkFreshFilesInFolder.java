package moni.greenexSrv2.nvs.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import greenexSrv2.nvs.com.globalData;

public class checkFreshFilesInFolder extends checkTemplate {
	
	String SQL_for_list = "";
	
	public checkFreshFilesInFolder (globalData gData, batchJob job) {
		
		super(gData,job);
		
	}
	
	public void executeCheck() {
		

		SQL_for_list = getSqlForServers(job);
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);

		getAdditionalMonitorData(systems);
		

		
		for(remoteSystem s: systems) {
				

			try {
				remoteCheck(s);
			
			}catch(Exception e) {
				
				s.params.put("result_text",e.getMessage());
			}
		}
		
		
		
			saveCheckResult(systems);
		
	}
	protected void getAdditionalMonitorData(List<remoteSystem> systems) {
		
		
		Connection conn = null ;
				
				try {
				      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	

				      for(remoteSystem s: systems) {
				      
				    	  String SQL = "select * from monitor_ext_data where param_name='check_files' and object_guid = '" + s.params.get("guid") +"'";
				    	  Statement stmt = conn.createStatement();
				    	  
				    	  ResultSet rs = stmt.executeQuery(SQL);
				    	  
				    	  while (rs.next()) {
				    		  
				               	s.folders.put(rs.getString("param_value") , "OK");
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

	private void remoteCheck(remoteSystem system) {
		
	String os_version = system.params.get("os_version").toUpperCase();
	
	if(os_version.contains("WINDOWS")) {
			
			checkFreshFilesWindows(system);
	
	}else if (os_version.contains("LINUX")){
	
		checkFreshFilesLinux(system);
	
	} else {
	
		system.params.put("message", "Check canceled. Type OS " + os_version + " is unknown");
	
	}
	
}

	private void checkFreshFilesWindows(remoteSystem s) {
		
		disableSslVerification();
		
		String ip = s.params.get("def_ip");
		
		String port = "8443";
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
		String filter = "";
		String strUrl = "";

		strUrl = "https://" + ip + ":" + port + "/query/check_files";
		if (s.folders.size() == 1) {

			Map.Entry<String,String> entry = s.folders.entrySet().iterator().next();
			String path = entry.getKey();
			strUrl += "?" + path;
		}else {
			
			String message = strUrl + " -not found single record for fresh files. Check that only one record exists in the table monitor_ext_data";
			s.params.put("result_text",message);
			s.params.put("result_number", "1");
			gData.logger.severe(message);
			return;			
		}
		
		
		

		
		
		try {

			
				URL url = new URL(strUrl);		  
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("Password", password);
				conn.setRequestProperty("Content-type", "application/json");
				conn.setRequestProperty("Accept", "application/json");		
				conn.setRequestMethod("GET");


						if (conn.getResponseCode() != 200) {
							s.params.put("result_text","Failed : HTTP error code : " + conn.getResponseCode());
						}

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
											

				String output;
				String outText = "";
		
					while ((output = br.readLine()) != null) { 	outText += output; 	}
		
//					System.out.println(outText);
		
					gData.logger.info(strUrl + "\n" + output);					

					JSONObject obj;

			
				try {

					obj = new JSONObject(outText);

						JSONObject res = obj.getJSONArray("payload").getJSONObject(0);

						String txt_message = res.getString("message");
						String txt_result = res.getString("result");
						
						
						int cRes = extractNumber(txt_message);
						
						

						s.params.put("result_number",String.valueOf(cRes));	
						
						s.params.put("result_text",txt_message);
						
						SaveToLog(s.getShortDescr(), job.job_name);
			
		
					} catch (JSONException e) {
			
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					s.params.put("result_text", strUrl + " " + errors.toString());
					s.params.put("result_number", "1");
					gData.logger.severe(errors.toString());
		
					}
				
				
						
				conn.disconnect();


		
		
		} catch (MalformedURLException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			s.params.put("result_text", strUrl + " " + errors.toString());
			s.params.put("result_number", "1");
			gData.logger.severe(errors.toString());


		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			s.params.put("result_text", strUrl + " " + errors.toString());
			s.params.put("result_number", "1");
			gData.logger.severe(errors.toString());

		} 
	
		
	}
	private void checkFreshFilesLinux(remoteSystem system) {
		
	}

	public int extractNumber( String strText) {
		String buffer = strText;
		int resultInt = -1;

		String intValue = buffer.replaceAll("[^0-9]", ""); 

		if(!intValue.isEmpty()) {
			resultInt = Integer.valueOf(intValue);
		} 
		return resultInt;
		
	}
}
