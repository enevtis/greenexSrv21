package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;

public class AbapIdocStat extends BatchJobTemplate implements Runnable {

	public String currMonitorNumber = "305";
	public String currentNow = "";

	public AbapIdocStat(globalData gData, Map<String, String> params) {
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
		currentNow = gData.nowForSQL();

		List<remoteSystem> db_systems = readABAP_systemsListForCheck();

		gData.truncateLog(params.get("job_name"));
		gData.saveToLog("found " + db_systems.size() + " systems to check.", params.get("job_name"));

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

			getIdocStatLastNmonth(s.params, 3);

			String SQL_result = "";

			SQL_result += "insert into monitor_results ( ";
			SQL_result += "`object_guid`,`monitor_number`,";
			SQL_result += "`check_date`,`result_number`,";
			SQL_result += "`result_text`,`is_error`) values (";
			SQL_result += "'" + s.params.get("guid") + "',";
			SQL_result += "" + currMonitorNumber + ",";
			SQL_result += "'" + currentNow + "',";
			SQL_result += "" + s.params.get("result") + ",";
			SQL_result += "'" + s.params.get("message") + "',";
			SQL_result += "''";
			SQL_result += ")";

			gData.sqlReq.saveResult(SQL_result);
			gData.saveToLog("system is finished: " + message, params.get("job_name"));

		}
		gData.saveToLog("All systems are  finished: ", params.get("job_name"));
	}

	protected void getIdocStatLastNmonth(Map<String, String> params, int month) {

		int pastMonth = 6;
		List<String> sqlList = new ArrayList();

		for (int i = 1; i < (pastMonth + 1); i++) {
			doIdocStatRequest(params, (pastMonth - i), sqlList);
		}

		doIdocStatRequest(params, 5, sqlList);

//		for(String s: sqlList) {
//			gData.saveToLog(s, params.get("job_name"));
//		}

		gData.sqlReq.saveResult(sqlList);
	}

	protected boolean doIdocStatRequest(Map<String, String> params, int pastMonth, List<String> sqlList) {
		boolean out = false;
//		String outR3 = "";

		SAPR3 sr3 = new SAPR3(gData, params);

		gData.saveToLog("system is : " + params.get("sid") + " month shift : " + pastMonth, params.get("job_name"));

		DateTimeFormatter formatterForDate = DateTimeFormatter.ofPattern("yyyyMMdd");

		LocalDate cNow = LocalDate.now().minusMonths(pastMonth);
		LocalDate firstDay = cNow.withDayOfMonth(1);
		LocalDate lastDay = cNow.withDayOfMonth(cNow.lengthOfMonth());

//		String filter = "CREDAT GE '" + firstDay.format(formatterForDate) + "'";
//		filter += " AND CREDAT LE '" + lastDay.format(formatterForDate) + "'";

		String filter = "CREDAT >= '" + firstDay.format(formatterForDate) + "'";
		filter += " AND CREDAT <= '" + lastDay.format(formatterForDate) + "'";

		Locale l = Locale.forLanguageTag("ru");
		String strMonthYear = cNow.format(DateTimeFormatter.ofPattern("LLL-yyyy"));
		int Year = Integer.valueOf(cNow.format(DateTimeFormatter.ofPattern("yyyy")));
		int Month = Integer.valueOf(cNow.format(DateTimeFormatter.ofPattern("M")));

		gData.saveToLog("system is : " + params.get("sid") + " filter : " + filter, params.get("job_name"));

		SqlReturn outR3 = sr3.readBigTable("EDIDC", "STATUS,SNDPRN,RCVPRN,MESTYP", filter, ";");

		gData.saveToLog("response outR3 outR3.records.size() is : \n(" + outR3.records.size() + ")",
				params.get("job_name"));

		if (! outR3.isOk ) {
			params.put("result", "-999");
			params.put("message", "system " + params.get("short") + " " + params.get("sid") + " " + params.get("sysnr")
					+ " not reached");
			gData.saveToLog(params.get("message"), params.get("job_name"));

			return false;
		}

		Map<String, Integer> idocs = new HashMap<String, Integer>();
		String message = "";
		int counter = 0;

		if (outR3.records.size() == 0) {
			idocs.put("0;-;-;-", 0);
		} else {

			for (Map<String, String> rec : outR3.records) {
				counter++;
				String key = rec.get("STATUS") + ";";
				key += rec.get("SNDPRN") + ";";
				key += rec.get("RCVPRN") + ";";
				key += rec.get("MESTYP") + ";";

				addNewRecord(key, 1, idocs);

				if (counter < 15)
					gData.saveToLog(key, params.get("job_name"));

			}

		}

		outR3.records.clear();

		
	for (Map.Entry<String, Integer> pair : idocs.entrySet()) {
	 
	 String curKey = pair.getKey(); String parts[] = curKey.split(";");
	 
	 String sqlIns = ""; 
	 sqlIns += "insert into monitor_idocs ("; 
	 sqlIns += "`object_guid`,"; 
	 sqlIns += "`year`,"; 
	 sqlIns += "`month`,"; 
	 sqlIns += 	 "`status`,"; 
	 sqlIns += "`sender`,"; 
	 sqlIns += "`reciever`,"; 
	 sqlIns += "`idoc_type`,"; 
	 sqlIns += "`total`,"; 
	 sqlIns += "`check_date`"; 
	 sqlIns +=  ") values ("; 
	 sqlIns += "'" + params.get("guid") + "',"; 
	 sqlIns += "'" + Year  + "',"; 
	 sqlIns += "'" + Month + "',"; 
	 sqlIns += "'" + ((parts.length >= 0) ? parts[0].trim() : "-") + "',"; 
	 sqlIns += "'" + ((parts.length >= 1) ? parts[1].trim() : "-") + "',"; 
	 sqlIns += "'" + ((parts.length >= 2) ? parts[2].trim() : "-") + "',"; 
	 sqlIns += "'" + ((parts.length >= 3) ? parts[3].trim() : "-") + "',"; 
	 sqlIns += "" + pair.getValue() + ","; 
	 sqlIns += "'" + currentNow + "'"; 
	 sqlIns += ")";
	 
	 sqlList.add(sqlIns);

	}	
		
		
		
		
		/*
		 * 
		 * if (outR3.contains("::")) {
		 * 
		 * String strFields[] = outR3.split("\\s*::\\s*");
		 * 
		 * for (int i = 0; i < strFields.length; i++) {
		 * 
		 * String parts[] = strFields[i].split(";"); addNewRecord(strFields[i], 1,
		 * idocs);
		 * 
		 * } } else {
		 * 
		 * idocs.put("0;-;-;-", 0);
		 * 
		 * }
		 * 
		 * params.put("result", "" + counter); params.put("message", "ok");
		 * 
		 * 
		 * for (Map.Entry<String, Integer> pair : idocs.entrySet()) {
		 * 
		 * String curKey = pair.getKey(); String parts[] = curKey.split(";");
		 * 
		 * String sqlIns = ""; sqlIns += "insert into monitor_idocs ("; sqlIns +=
		 * "`object_guid`,"; sqlIns += "`year`,"; sqlIns += "`month`,"; sqlIns +=
		 * "`status`,"; sqlIns += "`sender`,"; sqlIns += "`reciever`,"; sqlIns +=
		 * "`idoc_type`,"; sqlIns += "`total`,"; sqlIns += "`check_date`"; sqlIns +=
		 * ") values ("; sqlIns += "'" + params.get("guid") + "',"; sqlIns += "'" + Year
		 * + "',"; sqlIns += "'" + Month + "',"; sqlIns += "'" + ((parts.length >= 0) ?
		 * parts[0].trim() : "-") + "',"; sqlIns += "'" + ((parts.length >= 1) ?
		 * parts[1].trim() : "-") + "',"; sqlIns += "'" + ((parts.length >= 2) ?
		 * parts[2].trim() : "-") + "',"; sqlIns += "'" + ((parts.length >= 3) ?
		 * parts[3].trim() : "-") + "',"; sqlIns += "" + pair.getValue() + ","; sqlIns
		 * += "'" + currentNow + "'"; sqlIns += ")";
		 * 
		 * sqlList.add(sqlIns);
		 * 
		 * }
		 */
		return out;
	}

	private void addNewRecord(String key, int value, Map<String, Integer> totalRec) {
		if (totalRec.containsKey(key)) {
			totalRec.put(key, totalRec.get(key) + value);
		} else {
			totalRec.put(key, value);
		}

	}
}
