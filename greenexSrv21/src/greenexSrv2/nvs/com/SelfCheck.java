package greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

public class SelfCheck implements Runnable{
	
	globalData gData = null;
	
	public SelfCheck(globalData gData) {
		this.gData = gData;
	}

	@Override
	public void run() {

		refreshGlobalVariables();
		
		
		
	}
	protected void refreshGlobalVariables() {
		
		String SQL = "select * from user_settings ";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {

			String paramName,paramValue = "";
				
			paramName = rec.get("param_name");
			if (paramName.toUpperCase().equals("TRACEFLAG")) {
				
				paramValue = rec.get("param_value");	
				if (paramValue.toUpperCase().equals("TRUE")) {
					gData.debugMode = true;
					
				}
				else gData.debugMode = false;
				
			}



		}
	}
}
