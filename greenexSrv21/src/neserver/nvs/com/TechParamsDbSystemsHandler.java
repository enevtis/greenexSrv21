package neserver.nvs.com;

import java.util.List;
import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class TechParamsDbSystemsHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "tech_srv_params";
	private String tableName = "servers";
	private String SQL = "";
	public String caption = "";


	public TechParamsDbSystemsHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";
		
		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));
		
		this.caption = gData.tr("Technical parameters for") + " " + pr.typeObj + " " + pr.shortCaption + "<p class='prim'> (" + pr.physGuid + ")</p>";
		
		out += getBeginPage();
		out += strTopPanel(caption);

		
		SQL += "SELECT a.*,b.short AS project,c.short AS os ";
		SQL += " FROM servers a  ";
		SQL += "LEFT JOIN projects b ON a.project_guid = b.guid ";
		SQL += "LEFT JOIN os_typ c ON a.os_typ = c.guid ";
		SQL += "WHERE a.guid='" + pr.physGuid 	+ "' ";
		
		
		this.fields = readTableMetadata(screenName);
		setDefaultValueForField(this.fields,"object_guid",pr.physGuid);
		setDefaultValueForField(this.fields,"conn_type","opersys");
		
		out += buildJavascriptRefreshFunction(SQL, tableName, fields, "SHOW");
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();


		out += getEndPage();

		return out;
	}

}
