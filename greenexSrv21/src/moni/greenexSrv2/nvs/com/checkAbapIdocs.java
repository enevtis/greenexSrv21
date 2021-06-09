package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import greenexSrv2.nvs.com.globalData;
import simplecrypto.nvs.com.SimpleCrypto;

public class checkAbapIdocs extends checkTemplate{
	
	String SQL_for_list = "";
	
	public checkAbapIdocs (globalData gData, batchJob job) {
		super(gData,job);
	}

	public void executeCheck() {
		
		SQL_for_list = getSqlForAbapSystems(job);
		
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
		
		String db_version = system.params.get("db_version").toUpperCase();
		
		
		if(db_version.contains("HANA")) {
		
			checkAbapIdocsfromHana(system);
		
		}else if (db_version.contains("ORACLE")){
		
			checkAbapIdocsfromOracle(system);
				
		
		} else {
		
			system.params.put("result_text", "Check canceled. Type Database " + db_version + "is unknown");
		
		}

		
	}
	
	private void checkAbapIdocsfromOracle(remoteSystem s){
		Connection conn = null ;
		
	  	String conn_str = "";
   		String user = s.params.get("db_user");
   		String hash = s.params.get("db_hash");
   		String ip = s.params.get("db_ip");
   		String port = s.params.get("db_port"); 
   		String sid = s.params.get("db_sid"); 
   		String clnt = s.params.get("clnt"); 
   		String sap_scheme = s.params.get("sap_scheme"); 
  		
   		String password = "";
   		
   		
   		gData.logger.info(conn_str + " " + user + " " + hash );	
   		
   		try {
			password = SimpleCrypto.decrypt(gData.SecretKey, hash);
		} catch (Exception e1) {
			
			gData.logger.severe(e1.getMessage());

		}
   		

        	conn_str = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + sid;
			
			try {

			      conn = DriverManager.getConnection(conn_str , user , password);	
			      
			      	Statement stmt = conn.createStatement();
				
					
					int pastMin = 1440;		// временной интервал 
					
					LocalDateTime cNow = LocalDateTime.now();
					LocalDateTime cFrom = cNow.minusMinutes(pastMin);
					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
					String strDateFrom = cFrom.format(formatter);
					String strDateTo = cNow.format(formatter);

					//select MESTYP,count(*) as kvo from SAPSR3.edidc where mandt = '300' and upddat between '20190507' AND '20191107' group by MESTYP order by kvo desc ;	
					//grant select on SAPABAP1.edidc to SYSTEM;	
					//grant select on schema SAPSR3 to SYSTEM

					String SQL = "";					
	
					SQL = "";	
					SQL += "select count(*) as total_idocs from " + sap_scheme + ".edidc where mandt = '" + clnt + "'";
					ResultSet rs = stmt.executeQuery(SQL);					
					
					int allTimeTotals = 0;
					
					while (rs.next()) {
						allTimeTotals = rs.getInt("total_idocs");
					}					
					
					
					SQL = "";	
			      	SQL += "select s1.* from (";
					SQL += "select MESTYP,count(*) as kvo from " + sap_scheme + ".edidc where mandt = '" + clnt + "' and upddat between '" + strDateFrom + "' AND '" + strDateTo + "'" ;
					SQL += " group by MESTYP order by kvo desc";
					SQL += ") s1 where rownum < 11";

					rs = stmt.executeQuery(SQL);
					String cResultText = "";
					int total = 0;

					while (rs.next()) {
						int cTotal = rs.getInt("kvo");
						total += cTotal;
						cResultText += rs.getString("mestyp") + "=" + cTotal + ";";
					}
					
					s.params.put("result_number", String.valueOf(allTimeTotals));
					s.params.put("result_text", "new " + total + " from " + strDateFrom + " to " + strDateTo + ";" + cResultText);

					SaveToLog(s.getShortDescr(), job.job_name);
					
					conn.close();
					
			
			} catch (Exception e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
				s.params.put("message", errors.toString());

			} finally {	
				try {
					conn.close();
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(errors.toString());
					s.params.put("message", errors.toString());
				}
			}
		
		
		
	}
	private void checkAbapIdocsfromHana(remoteSystem s){
	
		Connection conn = null ;
		
	  	String conn_str = "";
   		String user = s.params.get("db_user");
   		String hash = s.params.get("db_hash");
   		String ip = s.params.get("db_ip");
   		String port = s.params.get("db_port");   		
   		String clnt = s.params.get("clnt"); 
   		String sap_scheme = s.params.get("sap_scheme"); 
   		
   		
   		String password = "";
   		
   		
   		
   		
   		try {
			password = SimpleCrypto.decrypt(gData.SecretKey, hash);
		} catch (Exception e1) {
			
			gData.logger.severe(e1.getMessage());

		}


        	conn_str = "jdbc:sap://" + ip + ":" + port + "/?autocommit=false"; 
   		
        	gData.logger.info(conn_str + " " + user  );		
			
			try {

			      conn = DriverManager.getConnection(conn_str , user , password);	
			      
			      	Statement stmt = conn.createStatement();
			      	
					int pastMin = 1440;		// временной интервал 
					
					LocalDateTime cNow = LocalDateTime.now();
					LocalDateTime cFrom = cNow.minusMinutes(pastMin);
					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
					String strDateFrom = cFrom.format(formatter);
					String strDateTo = cNow.format(formatter);
			      	
			      	String SQL = "";	

			      	SQL = "";	
					SQL += "select count(*) as total_idocs from " + sap_scheme + ".edidc where mandt = '" + clnt + "'";
					ResultSet rs = stmt.executeQuery(SQL);					
					
					int allTimeTotals = 0;
					
					while (rs.next()) {
						allTimeTotals = rs.getInt("total_idocs");
					}					
					
		
					SQL = "";	
					SQL += "select MESTYP,count(*) as kvo from " + sap_scheme + ".edidc where mandt = '" + clnt + "' and upddat between '" + strDateFrom + "' AND '" + strDateTo + "'" ;
					SQL += " group by MESTYP order by kvo desc limit 10";

					
					
					rs = stmt.executeQuery(SQL);
					
					String cResultText = "";
					int total = 0;

					while (rs.next()) {
						int cTotal = rs.getInt("kvo");
						total += cTotal;
						cResultText += rs.getString("mestyp") + "=" + cTotal + ";";
					}
					
					s.params.put("result_number", String.valueOf(allTimeTotals));
					s.params.put("result_text", "new " + total + " from " + strDateFrom + " to " + strDateTo + ";" + cResultText);
					conn.close();
					
			
			} catch (Exception e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
				s.params.put("message", errors.toString());

			} finally {	
				try {
					conn.close();
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(errors.toString());
					s.params.put("message", errors.toString());
				}
			}
		
		
	}	
	
}
