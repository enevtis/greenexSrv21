package moni.greenexSrv2.nvs.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import greenexSrv2.nvs.com.globalData;

public class checkUpTime extends checkTemplate {
		
		String SQL_for_list = "";
		
		public checkUpTime (globalData gData, batchJob job) {
			
			super(gData,job);
			
		}
		
		public void executeCheck() {
			

			
			SQL_for_list = getSqlForServers(job);
			
			
			List<remoteSystem> systems = getListForCheck(SQL_for_list);


			
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
					
				checkServerUpTimeWindows(system);
			
			}else if (os_version.contains("LINUX")){
			
				checkServerUpTimeLinux(system);
			
			} else {
			
				system.params.put("message", "Check canceled. Type OS " + os_version + " is unknown");
			
			}
			
		}

			private void checkServerUpTimeWindows(remoteSystem s) {
				
				disableSslVerification();
				
				String ip = s.params.get("def_ip");
				
				String port = "8443";
				String user = s.params.get("user");
				String hash = s.params.get("hash");
				
				String password = gData.getPasswordFromHash(hash);
				
				String filter = "";
				String strUrl = "";

				strUrl = "https://" + ip + ":" + port + "/query/check_uptime";
			
//				System.out.println(strUrl);
				
				
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
				
//							System.out.println(outText);
				
							
						JSONObject obj;

					
						try {

							obj = new JSONObject(outText);

								JSONObject res = obj.getJSONArray("payload").getJSONObject(0);

								String txt_message = res.getString("message");
								String txt_result = res.getString("result");
								
								if(txt_result.equals("OK")) {
									s.params.put("result_number","0");	
								} else {
									s.params.put("result_number","1");
								}
								
								s.params.put("result_text",txt_message);
								

					
				
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
			private void checkServerUpTimeLinux(remoteSystem system) {
				
			}
		
}
