package neserver.nvs.com;

import java.util.List;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.TblField;

public class EmailControlHandler extends HandlerTemplate {


	public EmailControlHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {
		String out = "";
		out += getBeginPage();
		out += strTopPanel(gData.tr("Mailing control"));
		
		out += readFromTable_options_map(this.getClass().getSimpleName());
		
		out += getEndPage();
		return out;
	}

}
