package moni.greenexSrv2.nvs.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import greenexSrv2.nvs.com.globalData;

public class checkMemory extends checkTemplate{
	String SQL_for_list = "";
	
	public checkMemory (globalData gData, batchJob job) {
		
		super(gData,job);
		
	}
	
	public void executeCheck() {
		

		
		SQL_for_list = getSqlForServers(job);
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);

		
		System.out.println(systems.size());
		
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
		
		String os_version = system.params.get("os_version").toUpperCase();
		
		if(os_version.contains("WINDOWS")) {
		
			checkMemoryWindows(system);
		
		}else if (os_version.contains("LINUX")){
		
		    checkMemoryLinux(system);
		
		} else if (os_version.contains("HP-UX")){

			checkMemoryHPUX(system);
			
		} else {
		
			system.params.put("message", "Check canceled. Type OS " + os_version + " is unknown");
		
		}
		
	}
	private void checkMemoryWindows(remoteSystem s) {
		
		
		disableSslVerification();
		
		String ip = s.params.get("def_ip");
		String port = "8443";
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
		
		try {

			
			
				URL url = new URL("https://" + ip + ":" + port + "/query/check_memory");		  
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("Password", password);
				conn.setRequestProperty("Content-type", "application/json");
				conn.setRequestProperty("Accept", "application/json");		
				conn.setRequestMethod("GET");


				if (conn.getResponseCode() != 200) {
						s.params.put("result_text","Failed : HTTP error code : " + conn.getResponseCode());
				}

				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream() , "UTF8"));
											

				String output ="";
				String outText = "";
		
					while ((output = br.readLine()) != null) {
						outText += output;
					}
		
//					String cleanTxt = outText.replaceAll("[^\\p{IsAlphabetic}^\\p{IsDigit}\\{\\}\\(\\)\\[\\]\\.\\+\\*\\?\\^\\$\\\\\\|\\:\\,\\\"]", "");

//					outText +="                                                             ";
//					String cleanTxt = outText.substring(0, outText.indexOf("\"result\":")+16);
//					gData.logger.info(url.toString() + " \n " + cleanTxt);				
					
		
				String message="";
				String result="";		

				JSONObject obj;
	
				try {

					obj = new JSONObject(outText);




						JSONObject res = obj.getJSONArray("payload").getJSONObject(0);
			
						JSONArray values = res.getJSONArray("perf");
			
			  
						for (int i = 0; i < values.length(); i++) {
				
							JSONObject current = values.getJSONObject(i); 			    
							String alias = current.getString("alias");
							
							if (alias.equals("physical")) {
					
									JSONObject f1 = current.getJSONObject("float_value");
									
									float maxPhysicalMemory = (float)f1.getDouble("maximum");
									float usedPhysicalMemory = (float)f1.getDouble("value");
									
									float usedPercent = usedPhysicalMemory/maxPhysicalMemory * 100;
									
									s.params.put("result_number",String.valueOf(usedPercent));
									s.params.put("result_text","Used: " + usedPhysicalMemory + " from " + maxPhysicalMemory + " GB" );

									
								}

						}

						SaveToLog(s.getShortDescr() + " result=" + s.params.get("result_number"), job.job_name);		
						
			
		
				} catch (JSONException e) {
			
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					s.params.put("result_text", errors.toString());
					gData.logger.severe(errors.toString());
		
				}
				
						
						
				conn.disconnect();

		} catch (MalformedURLException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			s.params.put("result_text", errors.toString());
			gData.logger.severe(errors.toString());


		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			s.params.put("result_text", errors.toString());
			gData.logger.severe(errors.toString());

		} 
		
		
	}
	private void checkMemoryLinux(remoteSystem system) {
		
		String sshReply = "";
		
		String ip = system.params.get("def_ip");
		String user = system.params.get("user");
		String hash = system.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
		
		try {
		sshReply = getSsh(ip, user, password, "free");
		
		String lines[] = sshReply.split("\\r?\\n");
		for(int i=0; i < lines.length; i++) {
			
			String cLine[] = lines[i].split("\\s+");;
		
				if(cLine[0].contains("Mem:")) {
			
					
					float maxPhysicalMemory = Float.valueOf(cLine[1]) / 1024 / 1024;
					float usedPhysicalMemory = Float.valueOf(cLine[2]) / 1024 / 1024;
					
					float usedPercent = usedPhysicalMemory/maxPhysicalMemory * 100;
					
					system.params.put("result_number",String.valueOf(usedPercent));
					system.params.put("result_text","Used: " + usedPhysicalMemory + " from " + maxPhysicalMemory + " GB" );
					


				} 

		}
		
		SaveToLog(system.getShortDescr() + " result=" + system.params.get("result_number"), job.job_name);
		
		} catch(Exception e) {
			
			SaveToLog(ip + "\n" + e.toString(), job.job_name);
		}	
		
	}		
	private void checkMemoryHPUX(remoteSystem system) {
		
		String sshReply = "";
		
		String ip = system.params.get("def_ip");
		String user = system.params.get("user");
		String hash = system.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
		try {
			sshReply = getSsh(ip, user, password, "swapinfo -tam");
		
			String lines[] = sshReply.split("\\r?\\n");

			gData.logger.info(sshReply); 
		
			for(int i=0; i < lines.length; i++) {
			
			String buffer = lines[i].trim();
			String cLine[] = buffer.split("\\s+");

			
			if (cLine.length > 0) {
			
				if(cLine[0].contains("memory")) {
					String maxMemory = cLine[1];
					String usedMemory = cLine[2];
					
					
					float maxPhysicalMemory = Float.valueOf(maxMemory) / 1024 ;
					float usedPhysicalMemory = Float.valueOf(usedMemory) / 1024 ;
					
					float usedPercent = usedPhysicalMemory/maxPhysicalMemory * 100;
					
					system.params.put("result_number",String.valueOf(usedPercent));
					system.params.put("result_text","Used: " + usedPhysicalMemory + " from " + maxPhysicalMemory + " GB" );				
					
					
						gData.logger.info(lines[i] + " Max memory=" + maxMemory + " " + usedMemory);
					
				
					}
				}
				
			}
			
			SaveToLog(system.getShortDescr() + " result=" + system.params.get("result_number"), job.job_name);
			
			
	} catch(Exception e) {
		
		SaveToLog(ip + "\n" + e.toString(), job.job_name);
	}	
		
	}
}