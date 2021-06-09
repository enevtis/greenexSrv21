package moni.greenexSrv2.nvs.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import greenexSrv2.nvs.com.globalData;
import simplecrypto.nvs.com.SimpleCrypto;

public class checkCPU extends checkTemplate {

	String SQL_for_list = "";
	
	public checkCPU (globalData gData, batchJob job) {
		
		super(gData,job);
		
	}
	
	public void executeCheck() {
		

		gData.logger.info("executeCheck()");
		
		SQL_for_list = getSqlForServers(job);
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);

		
		gData.logger.info(SQL_for_list);
		
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
		
			checkCpuWindows(system);
		
		}else if (os_version.contains("LINUX")){
		
		    checkCpuLinux(system);
		
		} else if (os_version.contains("HP-UX")){
			
			checkCpuHPUX(system);
		
		} else {
		
			system.params.put("message", "Check canceled. Type OS " + os_version + " is unknown");
		
		}
		
	}

	private void checkCpuHPUX(remoteSystem system) {
	
		String sshReply = "";
		
		String ip = system.params.get("def_ip");
		String user = system.params.get("user");
		String hash = system.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
//		gData.logger.info("ip=" + ip + " user=" + user + " password=" + password);
		
		try {
		sshReply = getSsh(ip, user, password, "TERM=vt100 top -h -d 1");
		
		String lines[] = sshReply.split("\\u001B\\u005B\\u0042");
		
		gData.logger.info(sshReply);
		

		for(int i=0; i < lines.length; i++) {
			
			String buffer = lines[i].trim();
		
			String cLine[] = buffer.split("\\s+");
			
			if (cLine.length > 0) {

				if(cLine[0].contains("Cpu")) {
				String cNextLine[] = lines[i+2].split("\\s+");
				
				String strValue = cNextLine[5];
				
				gData.logger.info("strValue=" + strValue);
				
				strValue = strValue.replace("%", "");
				
				float idleCPUusage = Float.valueOf(strValue);
				
				float usedPercent = (float)100 - idleCPUusage;
				
				system.params.put("result_number",String.valueOf(usedPercent));
				system.params.put("result_text","Idle:" + strValue + " %" );
				
				
				System.out.println(lines[i] + " CpuIdle=" + cNextLine[5]);
				}
			
			}
			
	}
		} catch(Exception e) {
			
			SaveToLog(ip + "\n" + e.toString(), job.job_name);
		}
		
	}
	
	
	
	private void checkCpuWindows(remoteSystem s) {
		
		
		disableSslVerification();
		
		String ip = s.params.get("def_ip");
		String port = "8443";
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
		
		try {

			
			
				URL url = new URL("https://" + ip + ":" + port + "/query/check_cpu");		  
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("Password", password);
				conn.setRequestProperty("Content-type", "application/json");
				conn.setRequestProperty("Accept", "application/json");		
				conn.setRequestMethod("GET");


				if (conn.getResponseCode() != 200) {
						s.params.put("result_text","Failed : HTTP error code : " + conn.getResponseCode());
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream() , "UTF8"));
											

				String output = "";
				String outText = "";
		
					while ((output = br.readLine()) != null) {
						outText += output + "\n";
					}
		
					outText +="                                                             ";
					String cleanTxt = outText.substring(0, outText.indexOf("\"result\":")+16);
//					gData.logger.info(url.toString() + " \n " + cleanTxt);	
				
				String message="";
				String result="";		

				JSONObject obj;
	
				try {
					obj = new JSONObject(cleanTxt);

						JSONObject res = obj.getJSONArray("payload").getJSONObject(0);
			
						JSONArray values = res.getJSONArray("perf");
			
			  
						for (int i = 0; i < values.length(); i++) {
				
							JSONObject current = values.getJSONObject(i); 			    
							String alias = current.getString("alias");
							
							if (alias.contains("total 5s")) {
					
									JSONObject f1 = current.getJSONObject("int_value");
									s.params.put("result_number",String.valueOf(f1.getInt("value")));
									s.params.put("result_text","OK");
									
								}

						}
			  
			
		
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
	private void checkCpuLinux(remoteSystem system) {
	
		
		String sshReply = "";
		
		String ip = system.params.get("def_ip");
		String user = system.params.get("user");
		String hash = system.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);

		
		try {
			sshReply = getSsh(ip, user, password, "TERM=xterm top -n 1 -b | head -n 20");
			gData.logger.info(sshReply);
	

			String lines[] = sshReply.split("\\r?\\n");
		
			for(int i=0; i < lines.length; i++) {
				String cLine[] = lines[i].split("\\s+");
				if (cLine.length > 0) {
				
				if(cLine[0].contains("Cpu(s)")) {

					String strValue = cLine[7];
					strValue = strValue.replace("%", "");
					
					
					gData.logger.info("strValue=" + strValue);
					
					float idleCPUusage = Float.valueOf(strValue);
					
					float usedPercent = (float)100 - idleCPUusage;
					
					system.params.put("result_number",String.valueOf(usedPercent));
					system.params.put("result_text","Idle:" + strValue + " %" );
					
					
					}
				
				}
		}
			
		
	} catch(Exception e) {
		
		SaveToLog(ip + "\n" + e.toString(), job.job_name);
	}
	
	}	
		

}
