package neserver.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.TreeControl;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.ContextMenuItem;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;

public class ImportFilesHandler extends HandlerTemplate {

	public String treeControlId = "treeCtrl_1";

	public ImportFilesHandler(globalData gData) {
		super(gData);

	}
//C:\dev\greenex\greenex\src\greenex_gui\nvs\com - первоисточник

	public String getPage() {

		String out = "";
		this.caption = gData.tr("IMPORT FILES ");

		out += getBeginPage();
		out += strTopPanel(caption);

		HashMap<String, String> params = new HashMap();

		params.put("id", "treeCtrl_1");
		params.put("rootCaption", "Файлы");
		params.put("firstNodeId", "/usr/nvs/greenex/files");
		params.put("dataFunctionName", "tree_data");


		
		
		TreeControl ctrl1 = new TreeControl(params);
		String funcText = getOnContextMenuFunction(params.get("id"));
		
		ctrl1.menu.add(new ContextMenuItem("import","Импортировать",funcText));
		
		
		out += ctrl1.getTreeControlText();

		out += getEndPage();

		return out;
	}
protected String getOnContextMenuFunction(String id) {
	String out = "";
	out += "var currText=window.selectedElement_"+ id + ".id; \n";
	out += "postRequest('/tree_save?action=' + obj.id , currText); \n";
	out += "console.log('/tree_save?action=' + obj.id , currText); \n";	
//	out += "alert(window.selectedElement_"+ id + ".id + '--' + obj.id);";
	
	return out;
}
}
