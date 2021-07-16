package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class ResetStuckJobScan extends BatchJobTemplate implements Runnable {

	public ResetStuckJobScan(globalData gData) {
		super(gData,null);
	}

	@Override
	public void run() {
		
		try {
			
			resetRegularStuckJobs();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		
	}

	protected void resetRegularStuckJobs() {
	
		String packageName = "moni2.greenexSrv2.nvs.com.";
		List<String> sqlUpd = new ArrayList<>();

		String SQL = "";

		SQL += "SELECT a.*, TIMESTAMPDIFF(MINUTE, a.last_start, NOW()) AS past_min, \n"; 
		SQL += "CASE WHEN TIMESTAMPDIFF(MINUTE, a.last_run_date, NOW()) > a.interval_min * 2  THEN 'start' ELSE 'pause' END AS 'action' \n";
		SQL += "FROM regular_schedule a  \n";
		SQL += "WHERE a.active = 'X' \n";
		

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

				for (Map<String, String> rec : records_list) {
						if (rec.get("action").equals("start")) {

							sqlUpd.add("update regular_schedule set running = '',running_errors = running_errors + 1 where id=" + rec.get("id"));

						}


				}

				gData.sqlReq.saveResult(sqlUpd);


	}

}
