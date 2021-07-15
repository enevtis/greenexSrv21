package neserver.nvs.com;

import greenexSrv2.nvs.com.globalData;

public class EmailControlHandler extends HandlerTemplate {


	public EmailControlHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {
		String out = "";
		out += getBeginPage();
		out += strTopPanel(gData.tr("Mailing control"));
		
		out += readFromTable_options_map(this.getClass().getSimpleName());
		
		out += getEndPage();
		return out;
	}

}
