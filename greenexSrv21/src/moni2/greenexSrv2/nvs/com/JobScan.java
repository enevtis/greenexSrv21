package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class JobScan implements Runnable {
	public globalData gData;

	public JobScan(globalData gData) {
		this.gData = gData;
	}

	@Override
	public void run() {

		try {
			scanJobs();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	protected void scanJobs() {

		String SQL = "";
		String packageName = "moni2.greenexSrv2.nvs.com.";
		List<String> sqlUpd = new ArrayList<>();

		SQL += "SELECT a.id, a.number, a.job_name, a.className, a.parameters, TIMESTAMPDIFF(MINUTE, a.last_analyze, NOW()) AS past_min, \n";
		SQL += "CASE WHEN TIMESTAMPDIFF(MINUTE, a.last_analyze, NOW()) > a.interval_min THEN 'start' ELSE 'pause' END AS 'action' \n";
		SQL += "FROM monitor_schedule a  \n";
		SQL += "WHERE a.active = 'X' and a.running <> 'X'";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					try {

						Map<String, String> params = new HashMap<String, String>();
						params.put("parameters", rec.get("parameters"));
						params.put("job_number", rec.get("number"));
						params.put("job_name", rec.get("job_name"));
						params.put("job_id", rec.get("id"));

						if (rec.get("action").equals("start")) {

							String className = packageName + rec.get("className");
							Class cl = Class.forName(className);
							Constructor cnstr = Class.forName(className)
									.getConstructor(greenexSrv2.nvs.com.globalData.class, Map.class);
							Object obj = cnstr.newInstance(gData, params);

							Thread tr = new Thread((Runnable) obj);
							tr.start();

							sqlUpd.add("update monitor_schedule set last_start = now() where id=" + rec.get("id"));

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
