package neserver.nvs.com;

import java.util.List;
import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class TechParamsHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "tech_srv_params";
	private String tableName = "servers";
	private String SQL = "";
	public String caption = "";
	public int userRight = 0;

	public TechParamsHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));
		
		tableName = pr.obj_typ;
		screenName = tableName;
		
		
		userRight = Utils.determineUserRights(gData, "tech_params", gData.commonParams.get("currentUser"));
		if (userRight < 1) {

			out += "Sorry, you don't have necessary authorisation! Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}

		
		
		
		this.caption = gData.tr("Technical parameters for") + " " + pr.obj_typ + " " + pr.description
				+ "<p class='prim'> (" + pr.physGuid + ")</p>";

		out += getBeginPage();
		out += strTopPanel(caption);

		SQL = readFrom_sql_text(this.getClass().getSimpleName(),pr.obj_typ);
		
		SQL = SQL.replace("<PHISGUID>", params.get("guid"));
		
		gData.logger.info(SQL);
		
		

		this.fields = readMetadatafromDB(this.getClass().getSimpleName(),pr.obj_typ);

		setDefaultValueForField(this.fields, "object_guid", pr.physGuid);
		setDefaultValueForField(this.fields, "conn_type", "opersys");

		out += buildJavascriptRefreshFunction(SQL, tableName, fields, "SHOW");
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();

		out += getEndPage();

		return out;
	}


}
