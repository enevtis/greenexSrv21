package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class ScheduleTableHandler extends HandlerTemplate {

	String test = "";

	private List<TblField> fields = null;

	private String screenName = "monitor_schedule";
	private String tableName = "monitor_schedule";
	private String SQL = "";

	public String caption = "";

	public ScheduleTableHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {

		String out = "";
		String curStyle = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));

		tableName = pr.obj_typ;
		screenName = tableName;

		this.caption = gData.tr("Job list");

		out += getBeginPage();
		out += strTopPanel(caption);

		SQL = readFrom_sql_text(this.getClass().getSimpleName(), "jobs");

		gData.logger.info(SQL);
		
		this.fields = readMetadatafromDB(this.getClass().getSimpleName(), "jobs");

//		setDefaultValueForField(this.fields, "object_guid", pr.physGuid);
//		setDefaultValueForField(this.fields, "conn_type", "opersys");


		out += buildJavascriptRefreshFunction(SQL, tableName, fields, "SHOW");
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();

		out += getEndPage();

		return out;
	}

}
