package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;

public class AnalyzeScanDisks extends BatchJobTemplate implements Runnable {
	public String monitor_number = "101";
	
	public AnalyzeScanDisks(globalData gData) {
		super(gData,null);
	}

	@Override
	public void run() {

		try {

			analyze();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	private void analyze() {

		checkNewAlertsForDisks();
		checkRecoveredAlertsForDisks();


	}

	protected void checkRecoveredAlertsForDisks() {
		
		String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"analyze_recovered_disks_space");	
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
		
		for (Map<String, String> rec : records_list) {

			if (rec.get("action").equals("recovery")) {

				if (!checkIfAlertAlreadyExists(rec.get("server_guid"), monitor_number,
						rec.get("name"))) {

					String SQL1 = "";

					SQL1 += "update `problems` set  ";
					SQL1 += "is_fixed='X', ";
					SQL1 += "fixed=now(), ";					
					SQL1 += "fixed_result=" + rec.get("percent") + ", ";					
					SQL1 += "description='permitted percent=" + rec.get("permitted_percent") + "'";						
					SQL1 += " where id=" + rec.get("id");					


					gData.sqlReq.saveResult(SQL1);

				}
			}

		}

		
		
		
		
	}
	
	protected void checkNewAlertsForDisks() {
		
		String SQL = readFrom_sql_text(this.getClass().getSimpleName(),"analyze_disks_space");

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					if (rec.get("is_alert").equals("alert")) {

						if (!checkIfAlertAlreadyExists(rec.get("server_guid"), monitor_number,
								rec.get("name"))) {

							String insSQL = "";

							insSQL += "insert into `problems` (";
							insSQL += "`guid`,";
							insSQL += "`object_guid`,";
							insSQL += "`details`,";
							insSQL += "`monitor_number`,";
							insSQL += "`last_check_date`,";
							insSQL += "`result_number`,";
							insSQL += "`value_limit`";
							insSQL += ") values (";
							insSQL += "'" + gData.getRandomUUID() + "',";
							insSQL += "'" + rec.get("server_guid") + "',";
							insSQL += "'" + rec.get("name") + "',";
							insSQL += "" + monitor_number + ",";
							insSQL += "'" + rec.get("check_date") + "',";
							insSQL += "" + rec.get("percent") + ",";
							insSQL += "" + rec.get("max_percent") ;
							insSQL += ")";

							gData.sqlReq.saveResult(insSQL);

						}
					}

				}
			}
		}

		
		
		
	}
}
