package neserver.nvs.com;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class TimeLineHandler extends HandlerTemplate {

	public TimeLineHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {
		String out = "", osType = "";

		
		String curStyle = "";
		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));

		this.caption = readFromTable_time_line_caption(params.get("screen"));
		this.caption += " " + pr.description + " " + pr.sid + " " + pr.sysnr;

		out += getBeginPage();
		out += strTopPanel(caption);

		String SQL = readFrom_sql_text(this.getClass().getSimpleName(), "time_line");
		
		LocalDate today = LocalDate.now();
		
		int days = Integer.valueOf(params.get("days"));
		LocalDate inPast = today.minus(days, ChronoUnit.DAYS);
		LocalDate inFuture = today.plus(1,ChronoUnit.DAYS);
		DateTimeFormatter formatters = DateTimeFormatter.ofPattern("uuuuMMdd");

		SQL = SQL.replace("!START_DATE!", inPast.format(formatters));
		SQL = SQL.replace("!FINISH_DATE!", inFuture.format(formatters));
		SQL = SQL.replace("!MONITOR_NUMBER!", params.get("monitor"));
		SQL = SQL.replace("!OBJECT_GUID!", params.get("guid"));
		SQL = SQL.replace("!MOD_VALUE!", params.get("mod"));

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		if(gData.debugMode) gData.logger.info(SQL);
		
		
		out += getStyleText();
		out += buildTimeLineDiagram(records);

		out += getEndPage();

		return out;
	}

	protected String buildTimeLineDiagram(List<Map<String, String>> records) {
		String out = "";
	
	     String style = gData.diagramStyle + ";position:absolute;letf:50px;top:100px;";
		out += "<table style='" + style + "' border=0> \n";
		
		out += "<tr> \n";
	
		int maxHeight = 300;
		
		
		
				for (Map<String, String> rec : records) {

				float koeff = 3f;
				
				int valueHight = (int)(Float.valueOf(rec.get("max_value")) * koeff);	
					
					
		out += "	<td> \n";	
		out += "		<div class='vTxt'> \n";					
		out += "			<div style='display:inline-block;background-color:" + gData.clrDiagramFill + ";width:" + valueHight + "px;height:20px;' ";
		out += " 			title='" + rec.get("kvo") + "'";
		out += " 		> ";
		
		out += 			"<div style='color:white;'>" + rec.get("max_value") + "</div>";
		out += "		</div> \n";		
		
		

		out += "		</div> \n";
		out += "	</td> \n";	
				}

		
		out += "</tr> \n";
		

		out += "<tr> \n";
		

				for (Map<String, String> rec : records) {
		out += "	<th> \n";
		out += "		<div class='vTxt'> \n";					
		out += 			rec.get("timeline");
		out += "		</div> \n";
		out += "	</th> \n";
				}

			
		out += "</tr> \n";		
		
		
		out += "</table> \n";

		return out;
	}

	protected String readFromTable_time_line_caption(String screen) {
		String out = "";
		String SQL = "";
		SQL = "SELECT * FROM time_line_caption WHERE screen='" + screen + "' AND lang='" + gData.lang + "' ";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records_list) {
			out = rec.get("text");
		}
		return out;
	}

	protected String getStyleText() {
		String out = "";
		out += "<style> \n";

		out += "th {\n";

		out += "	background-color: black; \n";
		out += "	color: white; \n";
		out += "	text-align: center; \n";
		out += "	vertical-align: bottom; \n";
		out += "	height: 150px; \n";
		out += "	padding-bottom: 3px; \n";
		out += "	padding-left: 5px; \n";
		out += "	padding-right: 5px; \n";
		out += "	} \n";


		out += "	td {\n";

		out += "	background-color: " + gData.clrDiagramBackground +"; \n";
		out += "	color: blue; \n";
		out += "	text-align: center; \n";
		out += "	vertical-align: bottom; \n";
		out += "	height: 400px; \n";
		out += "	padding-bottom: 5px; \n";
		out += "	padding-left: 5px; \n";
		out += "	padding-right: 5px; \n";
		out += "	} \n";

		out += "	 .vTxt {\n";
		out += "	  text-align: center; \n";
		out += "	  vertical-align: middle; \n";
		out += "	  width: 5px; \n";
		out += "	  margin: 0px; \n";
		out += "	  padding: 0px; \n";
		out += "	  padding-left: 1px; \n";
		out += "	  padding-right: 1px; \n";
		out += "	   white-space: nowrap; \n";
		out += "	    -webkit-transform: rotate(-90deg);  \n";
		out += "	    -moz-transform: rotate(-90deg);   \n";
		out += "	 } \n";
		out += "	.grLine{ \n";
		out += "		background-color:red; \n";
		out += "	} \n";

		out += "	</style> \n";

		return out;

	}
}
