package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class AbapSm13Records extends BatchJobTemplate implements Runnable {

	public AbapSm13Records(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_shedule();
			doCheck();
			reSetRunningFlag_shedule();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	protected void doCheck() {

		String message = "";

		gData.logger.info("*** <p style='color:blue;'>Running " + params.get("job_name") + "</p>");

//		gData.sqlReq.saveResult("update monitor_schedule set running='X' where id=" + params.get("job_id"));

		List<remoteSystem> db_systems = readABAP_systemsListForCheck();
		gData.saveToLog("found " + db_systems.size() + " systems to check.", params.get("job_name"), false);

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
			s.params.put("job_name", params.get("job_name"));

			gData.saveToLog("connect with parameters: " + conData.user + " " + conData.clnt, params.get("job_name"));

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

//		gData.sqlReq.saveResult("update monitor_schedule set running=' ',last_analyze=now(),checks_analyze=checks_analyze+1 where id=" + params.get("job_id"));

	}

	protected boolean doAbapRequest(Map<String, String> params) {
		boolean out = false;

		SAPR3 sr3 = new SAPR3(gData, params);

		int pastMin = 1440; // временной интервал (сутки)

		LocalDateTime cNow = LocalDateTime.now();
		LocalDateTime cFrom = cNow.minusMinutes(pastMin);

		DateTimeFormatter formatterForDate = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		String strDate = cNow.format(formatterForDate);
		String strDateTimeFrom = cFrom.format(formatterForDate);
		String strDateTimeTo = cNow.format(formatterForDate);

		String filter = "vbdate > '" + strDateTimeFrom + "' and vbdate < '" + strDateTimeTo + "' ";

		String outR3 = "";

		outR3 = sr3.readTable("VBHDR", "VBUSR,VBREPORT,VBDATE", filter, "");

		if (outR3.equals("CONNECT_ERROR")) {
			params.put("result", "-999");
			params.put("message", "system " + params.get("short") + " " + params.get("sid") + " " + params.get("sysnr")
					+ " not reached");

			gData.logger.info(params.get("message"));

			return false;
		}

		gData.saveToLog(outR3, params.get("job_name"));

		String message = "";

		String strFields[] = outR3.split("\\s*::\\s*");

		int counter = 0;
		String oldValue = "", newValue = "";

		for (int i = 0; i < strFields.length; i++) {

//			gData.saveToLog(i + ") " + strFields[i], params.get("job_name"));

			counter++;

		}

		params.put("result", "" + counter);
		params.put("message", "");

		return out;
	}
}
