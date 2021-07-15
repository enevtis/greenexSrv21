package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import greenexSrv2.nvs.com.globalData;

public class checkOracleFreeSpace extends checkTemplate {
	
	String SQL_for_list = "";
	
	public checkOracleFreeSpace (globalData gData, batchJob job) {
		
		super(gData,job);
		
	}
	
	public void executeCheck() {
		
	
		SQL_for_list = getSqlForDataBase(job);
		
		
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
		
		if(db_version.contains("ORACLE")) {
				
			checkOraFreeSpace(system);
		
		
		} else {
			system.params.put("result_text", "Check canceled. Type DB " + db_version + " is unknown");
		}
	}
	private void checkOraFreeSpace(remoteSystem s) {
		
		
		Connection conn = null ;
	
	  	String conn_str = "";
   		String user = s.params.get("user");
   		String hash = s.params.get("hash");

		String password = gData.getPasswordFromHash(hash);
   		
 
        	conn_str = "jdbc:oracle:thin:@" + s.params.get("def_ip") + ":" + s.params.get("port") + ":" + s.params.get("sid");
			
//        	System.out.println(conn_str);

       	
        	try {

			      conn = DriverManager.getConnection(conn_str , user , password);	
			      
			      	Statement stmt1 = conn.createStatement();
			      	Statement stmt2 = conn.createStatement();
					String SQL1 = "";					
					String SQL2 = "";
					
					SQL1 += "select tablespace_name from dba_tablespaces ";
				
					ResultSet rs1 = stmt1.executeQuery(SQL1);
					

					String tableSpaceName = "";
					int maxPercent = 0;
					int currentPercent = 0;
					String maxTableSpaceName = "";
					String sumString = "";
					String message = "";
					

					while (rs1.next()) {
						
						tableSpaceName = rs1.getString("tablespace_name");
						SQL2 = "";
						SQL2 += "select distinct ";
						SQL2 += "a.tablespace_name, ";
						SQL2 += "SUM(a.bytes)/1024/1024 \"Used_Size_MB\", ";
						SQL2 += "SUM(decode(b.maxextend, null, A.BYTES/1024/1024, b.maxextend*8192/1024/1024)) \"Max_Size_Mb\", ";
						SQL2 += "(SUM(a.bytes)/1024/1024 - round(c.\"Free\"/1024/1024)) \"Total_Used_MB\", ";
						SQL2 += "(SUM(decode(b.maxextend, null, A.BYTES/1024/1024, b.maxextend*8192/1024/1024)) - (SUM(a.bytes)/1024/1024 - round(c.\"Free\"/1024/1024))) \"Total_Free_MB\", ";
						SQL2 += "round(100*(SUM(a.bytes)/1024/1024 - round(c.\"Free\"/1024/1024))/(SUM(decode(b.maxextend, null, A.BYTES/1024/1024, b.maxextend*8192/1024/1024)))) \"Used_Percentage\" ";
						SQL2 += "from ";
						SQL2 += "dba_data_files a, ";
						SQL2 += "sys.filext$ b, ";
						SQL2 += "(SELECT d.tablespace_name , sum(nvl(c.bytes,0)) \"Free\" FROM dba_tablespaces d, DBA_FREE_SPACE c where d.tablespace_name = c.tablespace_name(+) group by d.tablespace_name) c ";
						SQL2 += "where a.file_id = b.file#(+) and a.tablespace_name = c.tablespace_name and a.tablespace_name = '" + tableSpaceName + "' GROUP by a.tablespace_name, c.\"Free\"/1024 ";						
						
						
						ResultSet rs2 = stmt2.executeQuery(SQL2);
						
						
							while (rs2.next()) {
						
								currentPercent = rs2.getInt("Used_Percentage");
							
								if (currentPercent > maxPercent) { 
									maxPercent = currentPercent; 
									maxTableSpaceName = tableSpaceName;
								}
									sumString += tableSpaceName + "=" + currentPercent + ":"; 
							

							}
						
						}
	
	
							sumString = sumString.substring(0, sumString.length() - 1);
							
//							System.out.println("maxPercent=" + maxPercent + " tableSpaceName="  + maxTableSpaceName );
							
							if(maxPercent > gData.maxOracleUsedTableSpacePercent) {
							
								message = "Error: Tablespace " + maxTableSpaceName + " uses " + (100 - maxPercent) + "%";
								s.params.put("result_text", message + sumString);
								s.params.put("result_number","1");
							
							} else {

								s.params.put("result_text", "All is OK:" + sumString);
								s.params.put("result_number","0");							
							
							}
					
							SaveToLog(s.getShortDescr() + " result=" + s.params.get("result_number"), job.job_name);

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
