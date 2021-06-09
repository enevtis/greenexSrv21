package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;

public class TreeSaveHandler extends HandlerTemplate {

	String postData = "";

	public TreeSaveHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {
		String out = "";

//		
		String action=params.get("action");

		switch(action) {
		
		case "menu_treeCtrl_1_import":
			
			String fileName = Utils.cleanInvalidCharacters(postData);
			out += importFileInto_sql_text(fileName);
			
			break;
			
			
			
		default:
			
			break;
		
		}
		
		return out;
	}

protected String importFileInto_sql_text(String fileName) {
	String out = "";
	
	gData.logger.info("importFileInto_sql_text " + fileName);
	
	out += "imported 25 lines";
	return out;
}
	
	
	public void getResponse(SSLSocket socket, String paramsString, String postData) {

		this.postData = postData;
		parseParams(paramsString);
		String resp = getPage();

		try {

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			out.write(header200());
			out.write("Content-Length: " + resp.getBytes("UTF-8").length + "\r\n");
			out.write("\r\n");
			out.write(resp);

			out.close();

		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}
}
