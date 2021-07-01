package neserver.nvs.com;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.conn.jco.ext.DestinationDataProvider;

import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.SAPR3;
import obj.greenexSrv2.nvs.com.TblField;

public class TestHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_conn_data";
	private String tableName = screenName;
	private String SQL = "";
	public String caption = "Test page";


	public TestHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {

		String out = "";
		String curStyle = "";
		
		int userRight = Utils.determineUserRights(gData, "connect_data", gData.commonParams.get("currentUser"));
		if (userRight < 1) {

			out += "Sorry, you don't have necessary authorisation! Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}
	
		out += getBeginPage();
		out += strTopPanel(caption);
		
		Map<String, String> sparams = new HashMap<String, String>();
		

		
		sparams.put("ip", "172.16.41.173");
		sparams.put("sysnr", "22");
		sparams.put("clnt", "300");		
		sparams.put("user", "ENEVTIS");
		sparams.put("password", "Bar10dak$");

		
		
		out += doTest(sparams);
		
		out += getEndPage();

		return out;
	}
	public String doTest(Map<String, String> params) {
		String out = "";
		String outR3 = "";
		
/*
		SAPR3 sr3 = new SAPR3(gData, params);
		
		
		int pastMin = 15; 
		
		
		
		LocalDateTime cNow = LocalDateTime.now();
		LocalDateTime cFrom = cNow.minusMinutes(pastMin);

		DateTimeFormatter formatterForDate = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter formatterForUzeit = DateTimeFormatter.ofPattern("HHmmss");

		String strDate = cNow.format(formatterForDate);
		String strTimeFrom = cFrom.format(formatterForUzeit);
		String strTimeTo = cNow.format(formatterForUzeit);


		String filter = "CREDAT > '20210630' and CREDAT < '20210702'";
		
		outR3 = sr3.readTable("EDIDC", "STATUS,SNDPRN,MESTYP", filter, ";");
		
		if (outR3.equals("CONNECT_ERROR")) {
			params.put("result", "-999");
			params.put("message", "system " + params.get("short") + " " + params.get("sid") + " " + 
					params.get("sysnr") + " not reached");			
	
			gData.logger.info(params.get("message"));
			
			return "Error connection";
		}
*/		
		
		Map<String,Integer> idocs = new HashMap<String,Integer>();
		outR3 = getTextText();
		String message = "";

		String strFields[] = outR3.split("\\s*::\\s*");

//		return outR3;
		
		
		int counter = 0;
//		String oldValue = "", newValue = "";
//
		for (int i = 0; i < strFields.length; i++) {
			
			String parts[] = strFields[i].split(";");
			out += "<br>" + strFields[i];
			addNewRecord(strFields[i],1,idocs);
			
		}

//			params.put("result", "" + counter);
//			params.put("message", "");		
		
		out += "<hr>";
		
		for (Map.Entry<String, Integer> pair: idocs.entrySet()) {
            out += "<br>" + "key: " + pair.getKey() + " , value: " + pair.getValue();
        }
		
		
		return out;
	}
	private void addNewRecord(String key, int value, Map<String,Integer> totalRec) {
		if(totalRec.containsKey(key)) {
			totalRec.put(key, (int)totalRec.get(key) + value);
		}else {
			totalRec.put(key, value);
		}
		
	}
	
	private String getTextText() {
		String out = "";
	
		out = "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "51;BWPCLNT300;RSRQST::"
				+ "03;QA1CLNT300;RSINFO::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT::"
				+ "53;SABRE ;ZSABRE_FLIGHT";
		
		
		
		return out;
	}
}
