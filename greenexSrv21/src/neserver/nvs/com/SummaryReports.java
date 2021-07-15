package neserver.nvs.com;

import java.util.List;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.TblField;
/*
 * //option_maps - добавить пункты навигации
 */
public class SummaryReports extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_links";
	private String tableName = "monitor_links";
	private String SQL = "";
	public String caption = "";



	public String graphGuid = "";
	public String physGuid = "";
	
	public SummaryReports(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";

		out += getBeginPage();
		out += strTopPanel(readCaptionOfPage(this.getClass().getSimpleName()));
		out += readFromTable_options_map(this.getClass().getSimpleName());	//option_maps table
		

		out += getEndPage();

		return out;
	}

}
