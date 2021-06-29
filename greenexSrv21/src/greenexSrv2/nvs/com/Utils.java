package greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

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
		public static String timeConvert(int time) { 
			String out = "";
			int days = time/24/60;
			int hours = time/60%24;
			int minutes = time%60;
			
			
			
			if (days > 0) {
				
				if (days==1) out+= days + " день ";
				else if (days >= 2 && days <= 4) out+= days + " дня ";
				else out+= days + " дней ";
			}
			if (hours > 0) {

				if (hours==1) out+= hours + " час ";
				else if (hours >= 2 && hours <= 4 || hours >= 22 && hours <= 24 ) out+= hours + " часа ";
				else out+= hours + " часов ";			

			}
			if (minutes > 0) {

				if (minutes==1) out+= minutes + " минута ";
				else if (minutes >= 2 && minutes <= 4 ||
						minutes >= 22 && minutes <= 24 ||
						minutes >= 32 && minutes <= 34 ||
						minutes >= 42 && minutes <= 44 ||
						minutes >= 52 && minutes <= 54 										
						) out+= minutes + " минуты ";
				else out+= minutes + " минут ";			

			}		
		
			return out;
		}
		
}
