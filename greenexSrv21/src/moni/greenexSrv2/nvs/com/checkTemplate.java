package moni.greenexSrv2.nvs.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import greenexSrv2.nvs.com.globalData;

public class checkTemplate{

	public globalData gData = null;
	protected batchJob job = null;
	protected String buffer = "";

	public checkTemplate (globalData gData, batchJob job) {
		this.gData = gData;
		this.job = job;
		
	}
	protected List<remoteSystem> getListForCheck(String SQL) {
		
		List<remoteSystem> systems = new ArrayList<>();
		
		Connection conn = null ;
				
				try {
				      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
				      
				      	Statement stmt = conn.createStatement();
						
						ResultSet rs = stmt.executeQuery(SQL);

				        ResultSetMetaData rsmd = rs.getMetaData();
			            int cols = rsmd.getColumnCount();
						
		                
		            	String colName = "";
		                String colType = "";
		                String colLabel = "";
			            
			            
			            while (rs.next()) {

							remoteSystem s = new remoteSystem();
							
							
							
							for (int i=1;i<=cols;i++) {
				                
				            	colName = rsmd.getColumnName(i);
				                colType = rsmd.getColumnTypeName(i);
				                colLabel = rsmd.getColumnLabel(i);
				                
				                
				                
	
				                if(colType.toUpperCase().contains("CHAR")) {
				                	
				                	s.params.put(colLabel, rs.getString(colLabel));
				                	
				                }else if (colType.toUpperCase().contains("INT")){
				                	
				                	s.params.put(colLabel, String.valueOf(rs.getInt(colLabel)));
				               
				                }else if (colType.toUpperCase().contains("DATETIME")){
				                	
				                	s.params.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));				                	
				               
				                
				                }else {
				                	
				                	gData.logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType=" + colType + " colLabel=" + colLabel );
				                	
				                }
				                
				                
				                 
				            }							
							
							
							systems.add(s);

						}

						
			            gData.logger.fine("job.job_name=" + job.job_name + " SQL=" + SQL + " records=" + systems.size());
			            
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
	return systems;		
		
	}	
public List<Map<String , String>> getSqlFromSapHana(String connString, String user, String pass, String SQL){

	List<Map<String , String>> resordsMap  = new ArrayList<Map<String,String>>();
	Connection conn = null ;
	
	try {
	      conn = DriverManager.getConnection(connString , user , pass);	
	      
	      	Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(SQL);

	        ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
			
            
        	String colName = "";
            String colType = "";
            String colLabel = "";
            
            
            while (rs.next()) {


				Map<String,String> record = new HashMap<String, String>();
				
				
				for (int i=1;i<=cols;i++) {
	                
	            	colName = rsmd.getColumnName(i);
	                colType = rsmd.getColumnTypeName(i);
	                colLabel = rsmd.getColumnLabel(i);

	               
	               
	                
	                
	                if(colType.toUpperCase().contains("CHAR")) {
	                	
	                	record.put(colLabel, rs.getString(colLabel));
	                	
	                }else if (colType.toUpperCase().contains("INTEGER")){
//	                	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
	                	record.put(colLabel, String.valueOf(rs.getInt(colLabel)));
	                }else if (colType.toUpperCase().contains("BIGINT")){	               

//	                	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
	                	record.put(colLabel, String.valueOf(rs.getLong(colLabel)));
	                

	                }else if (colType.toUpperCase().contains("SMALLINT")){	               

//               	 gData.logger.info(colType + " getInt=" + rs.getInt(colLabel) + " getFloat=" + rs.getFloat(colLabel));
	                	record.put(colLabel, String.valueOf(rs.getInt(colLabel)));
               
	                }
	                else if (colType.toUpperCase().contains("DECIMAL")){
	                	
	                	record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));
	                	
	                }else if (colType.toUpperCase().contains("DATETIME")){
	                	
	                	record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));				                	

	                }else if (colType.toUpperCase().contains("TIMESTAMP")){
	                	
	                	record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));	                
	
	                } else if(colType.toUpperCase().contains("NCLOB")) {	
	                	
	                	record.put(colLabel, rs.getString(colLabel));
	                
	                }else {
	                	
	                	gData.logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType=" + colType + " colLabel=" + colLabel );
	                	
	                }
	                 
	            }							

				
				resordsMap.add(record);

			}
            
            gData.logger.fine("job.job_name=" + job.job_name + " SQL=" + SQL + " records=" + resordsMap.size());
			
//            System.out.println("resordsMap " + resordsMap.size());
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
	
	return resordsMap;
}


	
	public void saveCheckResult (List<remoteSystem> systems) {
		
		
		
		Connection conn = null ;
				
		
		
		
		try {
					
				      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
				      
				      	Statement stmt = conn.createStatement();
						String SQL = "";					
						
						SQL += "insert monitor_results (object_guid,monitor_number,check_date,result_date,result_number,result_text) ";
						SQL += " values(?,?,now(),?,?,?)";

						gData.logger.fine(SQL);
						
					
						for(remoteSystem s: systems) {
							
							PreparedStatement preparedStmt = conn.prepareStatement(SQL);
						  
							
						  int idx = 1;
						  String buffer = ""; 
						    
						    ////////  guid //////////  
							buffer = s.params.get("guid");							//guid
							preparedStmt.setString(idx, buffer); idx++;
							//////////////////////////////////
							
							////////  monitor_number ////////
							preparedStmt.setInt(idx, job.number); idx++;			// monitor_number
							//////////////////////////////////
																					// check date = now();				
							
							////////    check_date    ////////	
							//            now()            ///					
							//////////////////////////////////
							
							
							////////    result_date    ////////	
							String strResultDate = s.params.get("result_date");			//result date. Can be null !!!
							java.sql.Timestamp resultDate = null;	
								
								
							if(strResultDate != null && !strResultDate.isEmpty()) {
							
								if(strResultDate.toUpperCase().contains("NULL")) {
									resultDate = null;
								} else {
									resultDate = java.sql.Timestamp.valueOf(strResultDate);
								}
								
							} else {
								resultDate = null;
							}
						      
//							gData.logger.info("resultDate=" + resultDate);
							preparedStmt.setTimestamp  (idx, resultDate); idx++;
							//////////////////////////////////
							
							////////  result_number //////////
							
							String strResultNumber = s.params.get("result_number");
							float resultNumber = 0;
							
							
							if(strResultNumber != null && !strResultNumber.isEmpty()) {
								
								if(strResultNumber.toUpperCase().contains("NULL")) {
									
									resultNumber = 0;
									
								} else {

									resultNumber = Float.valueOf(strResultNumber);
								}								
								
								
							}
							
							
//							gData.logger.info("resultNumber=" + resultNumber);
							
							preparedStmt.setFloat(idx, resultNumber); idx++;
							
							//////////////////////////////////
							
							////////  result_text //////////

	
							
							
							String strText = s.params.get("result_text");
							
							if ( s.params.get("message")!=null ) {
								
								if(!s.params.get("message").toUpperCase().contains("NULL")) {
									strText += s.params.get("message");
								}
							
							}
							
							if(strText != null && !strText.isEmpty()) {
							
								if(strText.length() > 254) {
									strText = strText.substring(0,254);	
								}
							}

							preparedStmt.setString(idx, strText); idx++;	
							//////////////////////////////////						

						      preparedStmt.executeUpdate();
						
						
						/////////// Save disks (file systems ) ///////////
						      
						     
						      
						      
						     if(s.disks.size() > 0) {
						    	 
						    	 for(remoteDisk d: s.disks ) {
						    	 
						    		 int idx2 = 1;
						    		 String SQL2 = "";
						    		 SQL2 += "insert monitor_disks (server_guid, name, max_size_gb, used_size_gb, check_date) ";
						    		 SQL2 += " values(?, ?, ?, ?, now())";
										
						    		 
						    		 
						    		 
						    		 PreparedStatement preparedStmt2 = conn.prepareStatement(SQL2);
						    	 
						    		 
						    		 preparedStmt2.setString(idx2, s.params.get("guid")); idx2++;
						    		 preparedStmt2.setString(idx2, d.name); idx2++;
						    		 preparedStmt2.setFloat(idx2, d.maxSize); idx2++;
						    		 preparedStmt2.setFloat(idx2, d.usedSize); idx2++;
						    		 
//						    		 gData.logger.info("disk =" + s.params.get("guid") + d.name + " " + d.maxSize + " " +  d.usedSize);
						    		 preparedStmt2.executeUpdate();
						    		 
						    		 gData.logger.fine("job.job_name=" + job.job_name + "object_guid = " + s.params.get("guid") +  " " + d.name + " save successfully");
										
									} 
						    	 
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
	protected void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            @Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            @Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            @Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            @Override
				public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
			
	    } catch (KeyManagementException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
	    }
	}
	
	public String getSqlForDataBase(batchJob job) {
		String out = "";
		
		out += "select a.*, d.user,d.hash,c.short as 'db_version' from db_systems a ";
		out += "left join monitor_links b on a.guid = b.object_guid ";
		out += "left join db_typ c on a.db_type = c.guid ";
		out += "left join monitor_conn_data d on a.guid=d.object_guid ";
		out += "where d.conn_type = '" + job.conn_type + "' and b.monitor_number = " + job.number + " and  b.active='X' ";
		out += " and b.slot = " + job.slot ;		
		
		return out;
	}

	public String getSqlForServers(batchJob job) {
		String out = "";
		
		out += "select a.*, d.user,d.hash,c.short as 'os_version' from servers a ";
		out += "left join monitor_links b on a.guid = b.object_guid ";
		out += "left join os_typ c on a.os_typ = c.guid ";
		out += "left join monitor_conn_data d on a.guid=d.object_guid ";
		out += "where d.conn_type = '" + job.conn_type + "' and b.monitor_number = " + job.number + " and  b.active='X' ";
		out += " and b.slot = " + job.slot ;
		
		return out;
	}
	public String getSqlForAbapSystems(batchJob job) {
		String out = "";
		
		out += "select a.*, d.user,d.hash, d.clnt, c.short as 'app_version', \n";
		out += "f.def_ip as 'db_ip', f.sid as 'db_sid', f.sysnr as 'db_sysnr', f.port as 'db_port', e.user as 'db_user', e.hash as 'db_hash', \n";
		out += "g.short as 'db_version', a.sap_scheme \n";
		out += "from app_systems a \n";
		out += "left join monitor_links b on a.guid = b.object_guid \n";
		out += "left join app_typ c on a.app_typ = c.guid  \n";
		out += "left join monitor_conn_data d on a.guid=d.object_guid \n";
		out += "left join monitor_conn_data e on a.guid_db=e.object_guid \n";
		out += "left join db_systems f on a.guid_db = f.guid  \n";
		out += "left join db_typ g on f.db_type = g.guid   \n";
		out += "where d.conn_type = '" + job.conn_type + "' and b.monitor_number = " + job.number + " and  b.active='X' \n";
		out += " and b.slot = " + job.slot ;		
		
		
		return out;
	}
	
	  public String readTextFromJar(String pathToFile) {
		    InputStream is = null;
		    BufferedReader br = null;
		    String line;
		    String out = "";

		    try { 
		      is = getClass().getResourceAsStream(pathToFile);
		      br = new BufferedReader(new InputStreamReader(is));
		      while (null != (line = br.readLine())) {
		         out += " " + line;
		      }
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		    }
		    finally {
		      try {
		        if (br != null) br.close();
		        if (is != null) is.close();
		      }
		      catch (IOException e) {
		        e.printStackTrace();
		      }
		    }
		    return out;
		  }

		public void saveCheckHanaMetricResult (List<remoteSystem> systems) {
			
			
			
			Connection conn = null ;
					
					try {
						
					      conn = DriverManager.getConnection(gData.commonParams.get("connectionString") , gData.commonParams.get("user") , gData.commonParams.get("password"));	
					      

							String SQL = "";					
							
							SQL += "insert monitor_hana_metrics (object_guid,monitor_number,check_date,definition_id,result_value,unit,description,measured_element_name) ";
							SQL += " values(?,?,now(),?,?,?,?,?)";

						
							for(remoteSystem s: systems) {
								
								
							    for (Map<String, String> rec : s.records) {

								
							    	PreparedStatement preparedStmt = conn.prepareStatement(SQL);
							  
							    	int idx = 1;
							    	String buffer = ""; 
							    

							    	buffer = s.params.get("guid");							//guid
							    	preparedStmt.setString(idx, buffer); idx++;
							    	preparedStmt.setInt(idx, job.number); idx++;			// monitor_number

							    	String strText = rec.get("DEFINITION_ID");
							    	preparedStmt.setString(idx, strText); idx++;	
								
								
								
							    	String strResultNumber = rec.get("VALUE");
							    	float resultNumber = 0;
								
							    	if(strResultNumber != null && !strResultNumber.isEmpty()) {
									
							    		if(strResultNumber.toUpperCase().contains("NULL")) 	resultNumber = 0;	
							    		else resultNumber = Float.valueOf(strResultNumber);
							    	}
								
							    	preparedStmt.setFloat(idx, resultNumber); idx++;


							    	preparedStmt.setString(idx, rec.get("UNIT")); idx++;		
							    	preparedStmt.setString(idx, rec.get("DESCRIPTION")); idx++;
							    	preparedStmt.setString(idx, rec.get("MEASURED_ELEMENT_NAME")); idx++;
														

							    	preparedStmt.executeUpdate();
							
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
	  
protected int parseInt( String value) {
	int number = 0;
	try
	{
	    if(value != null)
	      number = Integer.parseInt(value);
	}
	catch (NumberFormatException e)
	{
	    number = 0;
	}
	
	return number;
	
}
protected float parseFloat( String value) {
	float number = 0;
	try
	{
	    if(value != null)
	      number = Float.parseFloat(value);
	}
	catch (NumberFormatException e)
	{
	    number = 0;
	}
	
	return number;
	
}
protected java.sql.Timestamp parseDateTime( String value){
	java.sql.Timestamp out = null;

	try
	{
		if(value != null && !value.isEmpty()) {
			
			if(value.toUpperCase().contains("NULL")) {
				out = null;
			} else {
				out = java.sql.Timestamp.valueOf(value);
			}
		}

	}
	catch (Exception e)
	{
	    out = null;
	}
		
	
	return out;
}


protected String parseString( String value, int maxLength) {
	String out = "";
	try
	{
	    if(value != null) {
	    	out = value;
	    		if (out.length() > maxLength)  	{
	    			out = out.substring(0,maxLength);
	    		}
	    } else {
	    	out = "";	    	
	    }

	}
	catch (Exception e)
	{
	    out = "ERROR";
	}
	
	gData.logger.fine("value = " + value + " out=" + out);
	
	return out;
	
}	  
protected void SaveToLog(String outText, String fileName) {
	
	String fullFileName = gData.mainPath + File.separator + "log" +  File.separator + fileName + ".log";
	
	  try {
		File fileOut = new File(fullFileName);
			
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		
		

		FileWriter fr = new FileWriter(fileOut, true);
		BufferedWriter br = new BufferedWriter(fr);
		br.write(dateFormat.format(date) + ": " + outText + "\r\n");

		br.close();
		fr.close();
		
			
	    } 
	   catch (Exception e) 
	   {
		gData.logger.info(e.getMessage());
	   } 

}
public String getSsh(String ip, String user, String password, String strCommand) throws Exception {

	   String out = "";

	   int exitStatus = -100;
		
		
	    	
	    	java.util.Properties config = new java.util.Properties(); 
	    	config.put("StrictHostKeyChecking", "no");
	    	JSch jsch = new JSch();
	    	Session session=jsch.getSession(user, ip, 22);
	    	session.setPassword(password);
	    	session.setConfig(config);
	    	session.connect();
	    	
	    	Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(strCommand);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in=channel.getInputStream();
	        channel.connect();
	        byte[] tmp=new byte[1024];
	        while(true){
	          while(in.available()>0){
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;            
	            out += new String(tmp, 0, i);
	          }

	          
	          if(channel.isClosed()){
	        	  exitStatus = channel.getExitStatus();
	            
	            break;
	          }
	          try{Thread.sleep(1000);}catch(Exception ee){}
	        }
	        channel.disconnect();
	        session.disconnect();



	return out;	
		
	}
}
