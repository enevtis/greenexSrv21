package greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

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
				gData.logger.info(rec.get("result"));
				result = Float.valueOf(rec.get("result"));
			}

			out = (int) result;
			return out;
		}
}
