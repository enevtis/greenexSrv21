package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class MonitorResultTableHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_results";
	private String tableName = "monitor_results";
	public String caption = "";
	private String SQL = "";

	public MonitorResultTableHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {
		String out = "";
		out += getBeginPage();

		String guid = params.get("guid");
		String monitorNumber = params.get("monitor");

		this.caption = gData.tr("Monitor results records ");

		if (guid != null) {

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(guid);
			this.caption += pr.description + " " + pr.sid + " " + pr.sysnr;

			SQL = readFrom_sql_text(this.getClass().getSimpleName(), "object"); //таблица sql_text
			SQL = SQL.replace("!PHYSGUID!", guid);

		} else if (monitorNumber != null) {

			this.caption += monitorNumber + " monitor";
			SQL = readFrom_sql_text(this.getClass().getSimpleName(), "monitor");
			SQL = SQL.replace("!MONITOR!", monitorNumber);

		}

		out += strTopPanel(this.caption);

		this.fields = readTableMetadata(screenName);

		out += buildJavascriptRefreshFunction(SQL, tableName, fields, "READONLY");
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();

		out += getEndPage();

		return out;
	}
 
}
