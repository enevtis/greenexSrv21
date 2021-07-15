package neserver.nvs.com;

import java.util.List;
import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
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

	@Override
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
