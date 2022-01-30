package greenexSrv2.nvs.com;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;
import obj.greenexSrv2.nvs.com.TblField;
import java.util.logging.Logger;

public class Utils {

	public static String cleanInvalidCharacters(String in) {
		StringBuilder out = new StringBuilder();
		char current;
		if (in == null || ("".equals(in))) {
			return "";
		}
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF))) {
				out.append(current);
			}

		}
		return out.toString().replaceAll("\\s", " ");
	}

	 public static boolean isInvalidSshCommand(String sshText) {
		 boolean out = false;
		 if (sshText == null) return true;
		 if (sshText.isEmpty()) return true;
		 if (sshText.length() < 4) return true;
		 
		 sshText = sshText.replace(";", " ");
		 sshText = sshText.replace("-", " ");
		 sshText = sshText.replace(".", " ");
		 
		 String parts[] = sshText.split("\\s+");
		 for (String s:parts) {
			 if (s.equals("rm")) return true;
			 if (s.equals("del")) return true;			 
			 if (s.equals("delete")) return true;
			 if (s.equals("mv")) return true;
			 if (s.equals("shutdown")) return true;
			 if (s.equals("reboot")) return true;
			 if (s.equals("cp")) return true;
		 }
		 
		 return out;
	 }
	 
		public static int determineUserRights(globalData gData, String screen, String userName) {
			int out = 0;
			float result = 0f;

			String SQL1 = "", SQL2 = "";

			SQL1 = "SELECT CASE WHEN MAX(s1.right) IS NOT NULL THEN MAX(s1.right) ELSE 0 END AS `result` ";
			SQL1 += " FROM (  ";
			SQL1 += " SELECT sum(a.rights) AS `right` FROM sys_roles a  ";
			SQL1 += " LEFT JOIN sys_users_roles b ON a.role_name = b.role_name  ";
			SQL1 += " LEFT JOIN sys_users c ON b.user_name = c.user_name  ";
			SQL1 += " WHERE c.user_name='" + userName + "' AND a.screen_name IN( 'all' ,'" + screen + "')) AS s1  ";
			
			
			List<Map<String, String>> records = gData.sqlReq.getSelect(SQL1);

			for (Map<String, String> rec : records) {
//				gData.logger.info(rec.get("result"));
				result = Float.valueOf(rec.get("result"));
			}

			out = (int) result;
			return out;
		}
		public static String maxMapParameter(Map<String,Integer> values) {
	
			
			String out = "";
			String maxKey = "";
			int maxValue = 0;
			
			for(Map.Entry<String, Integer> entry : values.entrySet()) {
			    String key = entry.getKey();
			    Integer value = entry.getValue();

			    if (maxValue ==0 ) {
			    	maxKey = key;
			    	maxValue = value;
			    }
			    
			    
			    if (value > maxValue) {
			    	maxKey = key;
			    	maxValue = value;
			    	
			    }
			    
			}
			
			out = maxKey + "=" + maxValue;
			return out ;
		}
		public static void addHashMapValue(Map<String,Integer> values,String key, int val) {
			
			if(values.containsKey(key))  values.put(key, val + values.get(key)); 
			else values.put(key, val);
			
		} 

		public static ConnectionData readConnectionParameters(globalData gData, PhisObjProperties pr) {
			ConnectionData out = new ConnectionData();
			String SQL = "select * from monitor_conn_data where object_guid='" + pr.physGuid + "'";

			List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
			if (records_list != null) {
				if (records_list.size() > 0) {
					for (Map<String, String> rec : records_list) {
						out.user = rec.get("user");
						out.hash = rec.get("hash");
						out.conn_type = rec.get("conn_type");
						out.clnt = rec.get("clnt");
						out.password = gData.getPasswordFromHash(out.hash);
					}
				}
			}

			switch (pr.obj_typ) {
			case "servers":
				out.ip = gData.sqlReq.readOneValue("SELECT def_ip FROM servers WHERE guid='" + pr.physGuid + "'");

				break;
			case "db_systems":

				records_list = gData.sqlReq.getSelect("SELECT * FROM db_systems WHERE guid='" + pr.physGuid + "'");
				for (Map<String, String> rec : records_list) {
					out.ip = rec.get("def_ip");
					out.port = rec.get("port");
					out.sid = rec.get("sid");
					out.sysnr = rec.get("sysnr");
					out.dbType = rec.get("db_type");
				}


				break;
			case "app_systems":

				records_list = gData.sqlReq.getSelect("SELECT * FROM app_systems WHERE guid='" + pr.physGuid + "'");
				for (Map<String, String> rec : records_list) {
					out.ip = rec.get("def_ip");
					out.port = rec.get("port");
					out.sid = rec.get("sid");
					out.sysnr = rec.get("sysnr");
					out.appType = rec.get("app_typ");
					out.protocol = rec.get("protocol");
					out.page = rec.get("page");
				}
				
				
				break;

			}

			return out;
		}
		public static String timeConvert(int timeMinutes, String lang) { 
			String out = "";
			int days = timeMinutes/24/60;
			int hours = timeMinutes/60%24;
			int minutes = timeMinutes%60;
			
			HashMap<String, String> descr = getDescriptions();

			if (lang.equals("EN") || lang.equals("RU") || lang.equals("DE")) {
				// do nothing
			} else {
			  lang = "EN";
			}

			
			
			if (days > 0) {
				
				if (days==1) out+= days + " " + descr.get("день" + lang) + " ";
				else if (days >= 2 && days <= 4) out+= days + " " + descr.get("дня" + lang) + " ";
				else out+= days + " " + descr.get("дней" + lang) + " ";
			}
			if (hours > 0) {

				if (hours==1) out+= hours +  " " + descr.get("час" + lang);
				else if (hours >= 2 && hours <= 4 || hours >= 22 && hours <= 24 ) out+= hours +  " " + descr.get("часа" + lang) + " ";
				else out+= hours +  " " + descr.get("часов" + lang);			

			}
			if (minutes > 0) {

				if (minutes==1) out+= minutes + " " + descr.get("минута" + lang) + " ";
				else if (minutes >= 2 && minutes <= 4 ||
						minutes >= 22 && minutes <= 24 ||
						minutes >= 32 && minutes <= 34 ||
						minutes >= 42 && minutes <= 44 ||
						minutes >= 52 && minutes <= 54 										
						) out+= minutes + " " + descr.get("минуты" + lang) + " ";	
				else out+= minutes + " " + descr.get("минут" + lang) + " ";			

			} if (minutes < 1) {
				out += descr.get("менее 1 минуты" + lang);			
			} 
		
		
			return out;
		}
	private static HashMap<String, String> getDescriptions(){
		HashMap<String, String> out = new HashMap<>();
		out.put("день" + "RU", "день");
		out.put("день" + "EN", "day");
		out.put("день" + "DE", "Tag");	

		out.put("дня" + "RU", "дня");
		out.put("дня" + "EN", "days");
		out.put("дня" + "DE", "Tage");

		out.put("дней" + "RU", "дней");
		out.put("дней" + "EN", "days");
		out.put("дней" + "DE", "Tage");

		out.put("час" + "RU", "час");
		out.put("час" + "EN", "hour");
		out.put("час" + "DE", "Stunde");	

		out.put("часа" + "RU", "часа");
		out.put("часа" + "EN", "hours");
		out.put("часа" + "DE", "Stunden");

		out.put("часов" + "RU", "часов");
		out.put("часов" + "EN", "hours");
		out.put("часов" + "DE", "Stunden");


		out.put("минута" + "RU", "минута");
		out.put("минута" + "EN", "minute");
		out.put("минута" + "DE", "Minute");	

		out.put("минуты" + "RU", "минуты");
		out.put("минуты" + "EN", "minutes");
		out.put("минуты" + "DE", "Minuten");

		out.put("минут" + "RU", "минут");
		out.put("минут" + "EN", "minutes");
		out.put("минут" + "DE", "Minuten");

		out.put("менее 1 минуты" + "RU", "менее 1 минуты");
		out.put("менее 1 минуты" + "EN", "less than 1 minute");
		out.put("менее 1 минуты" + "DE", "weniger als 1 Minute");
		
		return out;
	}
		
		public static String getTableStyle1() {
			String out = "";

			out += "table {";
			out += "font-size: 65%; ";
			out += "font-family: Verdana, Arial, Helvetica, sans-serif; ";
			out += "border: 1px solid #399; ";
			out += "border-spacing: 1px 1px; ";
			out += "}";
			out += "td {";
			out += "background: #DDDDDD;";
			out += "border: 1px solid #333;";
			out += "padding: 1px; ";
			out += "}";

			return out;
		}		
		public static String getHtmlTablePageFromSqlreturn(globalData gData, String SQL) {
			String out = "";
			
			SqlReturn rs = gData.sqlReq.getSelect2(SQL);
			
			
			out += "<style>";
			out += Utils.getTableStyle1();
			out += "</style>";
			
			out += "<table>";

			out += "<thead><tr>";

			for(TblField f : rs.fields) {
				String caption = f.fieldLabel.isEmpty() ? f.fieldName : f.fieldLabel;
				out += "<th>" + caption + "</th>";
			}
			out += "</tr></thead>";		
			
			out += "<tbody>";		
			for (Map<String, String> rec : rs.records) {
				out += "<tr>";

				for(TblField f : rs.fields) {
					out += "<td>" + rec.get(f.fieldName) + "</td>";
				}
				
				out += "</tr>";		
			}		

			out += "</tbody>";
			out+="</table>";

		return out;
	}
public static boolean deleteFile(String pathToFile) {
	boolean out = false;
	
	File file = new File(pathToFile);
	try {
		boolean result = Files.deleteIfExists(file.toPath());
	} catch (IOException e) {

	}
	
	return out;
}

public static String getStyleMessage(String color) {
	String out = "";

	if (color.equals("red")) {
		
		out += "color:red;";
		out += "font-weight:bold;";
		
	} else {
		out += "color:black;";
		out += "font-weight:normal;";						
	}
	
	
	
	return out;
}
public static String traceClassInMail(String className) {
	String out = "";
	
	
	out += "<p style='color:white'>" + className + "</p>";
	
	
	
	return out;
}


public static String getStyle() {
	String out = "";

	out += ".table2 { \n";
	out += "font-size: 190%; ";
	out += "font-family: Verdana, Arial, Helvetica, sans-serif;  \n";
	out += "border: 1px solid #399;  \n";
	out += "border-spacing: 1px 1px;  \n";
	out += "} \n";

	out += ".table2 tbody tr:nth-child(odd) { \n";
	out += " background-color: #ff33cc; \n";
	out += "border: 1px solid #333; \n";
	out += "} \n";

	out += ".table2 tbody tr:nth-child(even) { \n";
	out += " background-color: #e495e4; \n";
	out += "border: 1px solid #333; \n";
	out += "} \n";

	out += ".table1 { \n";
	out += "font-family: Verdana, Arial, Helvetica, sans-serif;  \n";
	out += "border: 1px solid #333; \n";
	out += "border-spacing: 1px 1px; \n";
	out += "border-collapse: collapse; \n";
	out += "} \n";

	out += ".table1 td { \n";
	out += "border: 1px solid black; \n";
	out += "} \n";
/*
	
	out += " .table2 { \n";
	out += "font-size: 70%;  \n";
	out += "font-family: Verdana, Arial, Helvetica, sans-serif;  \n";
	out += "border: 1px solid #399;  \n";
	out += "border-spacing: 1px 1px;  \n";
	out += "} \n";
	out += ".table2 td { \n";
	out += "background: #E0E0E0; \n";
	out += "border: 1px solid #333; \n";
	out += "padding: 0px;  \n";
	out += "}";
*/
	out += ".user_info{ \n";
	out += "font-size: 70%;  \n";
	out += "font-family: Verdana, Arial, Helvetica, sans-serif;  \n";
	out += "font-style: italic; \n";
	out += "}";

	out += ".caption_item{ \n";
	out += "font-size: 70%;  \n";
	out += "font-family: Verdana, Arial, Helvetica, sans-serif;  \n";
	out += "font-style: italic; \n";
	out += "font-weight: bold; \n";
	out += "}";

	out += ".ol_1{  \n";
	out += "}";

	return out;
}
}
