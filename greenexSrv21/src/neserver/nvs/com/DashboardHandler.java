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
import obj.greenexSrv2.nvs.com.GraphJsObject;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class DashboardHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_links";
	private String tableName = "monitor_links";
	private String SQL = "";
	public String caption = "";



	public String graphGuid = "";
	public String physGuid = "";
	
	public DashboardHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {

		String out = "";
		String curStyle = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));

		this.caption = gData.tr("Dashboard for ") ;
		this.caption += pr.description + " " + pr.sid + " " + pr.sysnr;
		
		out += getBeginPage();
		out += strTopPanel(caption);
		out += readFromTable_options_map("dashboard",pr);	
		

		out += getEndPage();

		return out;
	}

	

}
