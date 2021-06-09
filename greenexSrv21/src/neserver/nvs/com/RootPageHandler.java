package neserver.nvs.com;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;





public class RootPageHandler extends HandlerTemplate{
	
	
	public RootPageHandler(globalData gData) {
		super(gData);

	}

	
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
