package health.greenexSrv2.nvs.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class HealthTemplate {

	public globalData gData;
	
	public HealthTemplate(globalData gData) {
		this.gData = gData;
	}
	protected List<String> getListGuidsForJobName(String repType, String job_name ) {
		List<String> out = new ArrayList<String>();

		String SQL = "";
		SQL += "select * from regular_reports_data where `job_name`='" + job_name + "'";
		SQL += " AND `rep_type`='" + repType + "' and `active`='X' order by id";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {

			out.add(rec.get("guid"));

		}

		return out;
	}


}
