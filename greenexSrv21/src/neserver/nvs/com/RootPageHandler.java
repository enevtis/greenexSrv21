package neserver.nvs.com;

import greenexSrv2.nvs.com.globalData;





public class RootPageHandler extends HandlerTemplate{
	
	
	public RootPageHandler(globalData gData) {
		super(gData);

	}

	
	@Override
	public String getPage() {
		String out = "";
		
		out += getBeginPage();
		
		out += strTopPanel("Welcome to GREENEX");
//		out += "<p class='info_blosk1'>" + gData.commonParams.get("currentUser") + "</p><br>";
		out += readFromTable_options_map("root");		

		
		
		
		
		out += getEndPage();
		return out;
	}



}
