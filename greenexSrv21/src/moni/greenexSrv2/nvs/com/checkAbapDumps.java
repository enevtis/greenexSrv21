package moni.greenexSrv2.nvs.com;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataProvider;

import greenexSrv2.nvs.com.globalData;

public class checkAbapDumps extends checkTemplate{
	
	String SQL_for_list = "";
	
	public checkAbapDumps (globalData gData, batchJob job) {
		super(gData,job);
	}
	
	public void executeCheck() {
		

		
		SQL_for_list = getSqlForAbapSystems(job);
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);


		
		for(remoteSystem s: systems) {
				
			try {
				
				remoteCheck(s);
			
			}catch(Exception e) {
				
				s.params.put("result_text",e.getMessage());
			}
		}
		
		
		
			saveCheckResult(systems);
}
	private void remoteCheck(remoteSystem system) {
		
		SaveToLog(system.getShortDescr(), job.job_name);
		
		checkAbapDumpsDynamic(system);
		
	}	


	private void checkAbapDumpsDynamic(remoteSystem s) {

		
		HashMap<String, Integer> groupResult = new HashMap<String, Integer>();
		
		
		int pastMin = 15;		// временной интервал допустимого количества дампов
		
		LocalDateTime cNow = LocalDateTime.now();
		LocalDateTime cFrom = cNow.minusMinutes(pastMin);
		
		DateTimeFormatter formatterForDate = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter formatterForUzeit = DateTimeFormatter.ofPattern("HHmmss");
		
		String strDate = cNow.format(formatterForDate);
		String strTimeFrom = cFrom.format(formatterForUzeit);
		String strTimeTo = cNow.format(formatterForUzeit);

		String filter = "datum = '" + strDate +"' and uzeit > '" + strTimeFrom + "' and uzeit < '" + strTimeTo + "' ";		

		
		String out = "";
		SAPR3 sr3 = new SAPR3(gData,s);
		out = sr3.readTable("SNAP","DATUM,UZEIT",filter,"");
		
		
		
		
		String strFields[] = out.split("\\s*::\\s*");
		
		
		for(int i=0; i< strFields.length; i++) {
			
			if (!strFields[i].isEmpty()) {
			
				if (!groupResult.containsKey(strFields[i])) {
					groupResult.put(strFields[i], 1);
				} else {
					groupResult.put(strFields[i], (groupResult.get(strFields[i]) + 1));
				}
			}
			
		}
		

		s.params.put("result_number",String.valueOf(groupResult.size()));
		
		formatterForDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		String message = "new dumps errors in time from: " + cFrom.format(formatterForDate) + " to: " + cNow.format(formatterForDate);
		
		s.params.put("result_text", message );

//		for (Map.Entry<String, Integer> entry : groupResult.entrySet()) {
//			gData.logger.info("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//		}
		
		
	}
	
	
}
