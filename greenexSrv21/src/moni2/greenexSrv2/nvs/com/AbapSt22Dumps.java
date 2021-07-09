package moni2.greenexSrv2.nvs.com;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.SAPR3;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class AbapSt22Dumps extends BatchJobTemplate implements Runnable {

	public AbapSt22Dumps(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		doCheck();

	}

	protected void doCheck() {

		String message = "";

		gData.logger.info("*** <p style='color:blue;'>Running " + params.get("job_name") + "</p>");
		gData.sqlReq.saveResult("update monitor_schedule set running='X' where id=" + params.get("job_id"));

		List<remoteSystem> db_systems = readABAP_systemsListForCheck();
		
		gData.truncateLog(params.get("job_name"));
		gData.saveToLog("found " + db_systems.size() + " systems to start.", params.get("job_name"));

		for (remoteSystem s : db_systems) {
			message = s.params.get("short") + " " + s.params.get("ip") + " " + s.params.get("sid") + " "
					+ s.params.get("sysnr");
			message += " " + s.params.get("guid");

			gData.saveToLog(message, params.get("job_name"));

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));

			ConnectionData conData = readConnectionParameters(pr);

			s.params.put("user", conData.user);
			s.params.put("password", conData.password);
			s.params.put("clnt", conData.clnt);
			s.params.put("job_name",params.get("job_name"));
			doAbapRequest(s.params);

			String SQL_result = "";

			SQL_result += "insert into monitor_results ( ";
			SQL_result += "`object_guid`,`monitor_number`,";
			SQL_result += "`check_date`,`result_number`,";
			SQL_result += "`result_text`,`is_error`) values (";
			SQL_result += "'" + s.params.get("guid") + "',";
			SQL_result += "" + params.get("job_number") + ",";
			SQL_result += "now(),";
			SQL_result += "" + s.params.get("result") + ",";
			SQL_result += "'" + s.params.get("message") + "',";
			SQL_result += "''";
			SQL_result += ")";

			gData.sqlReq.saveResult(SQL_result);

		}

		gData.sqlReq.saveResult("update monitor_schedule set running=' ',last_analyze=now(),checks_analyze=checks_analyze+1 where id=" + params.get("job_id"));
	

	}

	protected boolean doAbapRequest(Map<String, String> params) {
		boolean out = false;

		SAPR3 sr3 = new SAPR3(gData, params);

		int pastMin = 15; // временной интервал допустимого количества дампов

		LocalDateTime cNow = LocalDateTime.now();
		LocalDateTime cFrom = cNow.minusMinutes(pastMin);

		DateTimeFormatter formatterForDate = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter formatterForUzeit = DateTimeFormatter.ofPattern("HHmmss");

		String strDate = cNow.format(formatterForDate);
		String strTimeFrom = cFrom.format(formatterForUzeit);
		String strTimeTo = cNow.format(formatterForUzeit);

		String filter = "datum = '" + strDate + "' and uzeit > '" + strTimeFrom + "' and uzeit < '" + strTimeTo + "' ";

		String outR3 = "";

//		outR3 = sr3.readTable("SNAP","DATUM,UZEIT,UNAME,FLIST",filter,";");
		outR3 = sr3.readTable("SNAP", "DATUM,UZEIT", filter, "");
		
		if (outR3.equals("CONNECT_ERROR")) {
			params.put("result", "-999");
			params.put("message", "system " + params.get("short") + " " + params.get("sid") + " " + 
					params.get("sysnr") + " not reached");			
	
			gData.logger.info(params.get("message"));
			
			return false;
		}
		
		
		
		String message = "";

		String strFields[] = outR3.split("\\s*::\\s*");

		int counter = 0;
		String oldValue = "", newValue = "";

		for (int i = 0; i < strFields.length; i++) {

			if (i == 0) {
				oldValue = strFields[i];
				counter++;
			} else {

				if (!strFields[i].contains(oldValue)) {
					oldValue = strFields[i];
					counter++;

				}

			}


		}

			params.put("result", "" + counter);
			params.put("message", "");		
		
		
		
		return out;
	}

}
