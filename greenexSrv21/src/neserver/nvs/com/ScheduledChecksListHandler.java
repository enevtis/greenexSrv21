package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class ScheduledChecksListHandler extends HandlerTemplate {


	private List<TblField> fields = null;
	private String screenName = "monitor_links";
	private String tableName = "monitor_links";
	private String SQL = "";
	public String caption = "";

	public ScheduledChecksListHandler(globalData gData) {
		super(gData);

	}



	public String getPage() {

		String out = "";
		String curStyle = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));
	

		
		SQL = "SELECT DISTINCT a.*,b.job_name from monitor_links a ";
		SQL += "LEFT JOIN monitor_schedule b ON a.monitor_number = b.number ";
		SQL += "where object_guid='" + pr.physGuid 	+ "' "; 
		SQL += "order by monitor_number "; 
		
		
		this.fields = readTableMetadata(this.tableName);
		setDefaultValueForField(this.fields,"object_guid",pr.physGuid);
		setDefaultValueForField(this.fields,"active","X");
		
		
		
		this.caption = gData.tr("Scheduled checks for") + " " + pr.typeObj + " " + pr.shortCaption + "<p class='prim'> (" + pr.physGuid + ")</p>";
		
		out += getBeginPage();
		out += strTopPanel(caption);
		
		out += buildJavascriptRefreshFunction(SQL, tableName, fields, "SHOW");
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();

		out += getEndPage();

		return out;
	}


}
