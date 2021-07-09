package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class TechServiceScan extends BatchJobTemplate implements Runnable {

	public TechServiceScan(globalData gData) {
		super(gData,null);
	}

	@Override
	public void run() {
		
		try {
			
			scanTechJobs();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		
	}

	protected void scanTechJobs() {
	
		String packageName = "moni2.greenexSrv2.nvs.com.";
		List<String> sqlUpd = new ArrayList<>();

		String SQL = "";
		SQL += "SELECT a.*, TIMESTAMPDIFF(MINUTE, a.last_start, NOW()) AS past_min, \n";
		SQL += "CASE WHEN TIMESTAMPDIFF(MINUTE, a.last_start, NOW()) > a.interval_min THEN 'start' ELSE 'pause' END AS 'action' \n";
		SQL += "FROM regular_schedule a \n";
		SQL += "WHERE a.active = 'X' and a.running <> 'X' \n";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					try {

						Map<String, String> params = new HashMap<String, String>();


						params.put("job_name", rec.get("job_name"));
						params.put("job_id", rec.get("id"));
						params.put("job_parameters", rec.get("parameters"));

						if (rec.get("action").equals("start")) {

							String className = packageName + rec.get("className");
							Class cl = Class.forName(className);
							Constructor cnstr = Class.forName(className)
									.getConstructor(greenexSrv2.nvs.com.globalData.class, Map.class);
							Object obj = cnstr.newInstance(gData, params);

							Thread tr = new Thread((Runnable) obj);
							tr.start();

							sqlUpd.add("update regular_schedule set last_start = now() where id=" + rec.get("id"));
							
							

						}

					} catch (Exception e) {

						StringWriter errors = new StringWriter();
						e.printStackTrace(new PrintWriter(errors));
						gData.logger.severe(errors.toString());
					}

				}

				gData.sqlReq.saveResult(sqlUpd);

			}
		}

	}
		

	
}
