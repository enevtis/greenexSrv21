package neserver.nvs.com;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;



public class HelpHandler extends HandlerTemplate{
	
	
	public HelpHandler(globalData gData) {
		super(gData);

	}


	public String getPage() {
		String out = "";

		out += getBeginPage();
		

		String page_id = params.containsKey("page") ? params.get("page") : "index";
		String SQL = "select * from help_text where page_id='" + page_id + "' ";
		SQL += " and lang='" + gData.lang + "' order by id";
		
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);


		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += rec.get("text") ;


				}
			}
		}
		
		
		
/*		
		String fileName = gData.mainPath + "/files/help/main_help.html";
		
		if (params!=null) {
			if (params.containsKey("page")) {
				fileName = gData.mainPath + "/files/help/main_help.html";
			} 
		}

		
		
		out += readTextFile(fileName);
*/
		out += getEndPage();
		return out;
	}

	

	
	
	
	public String getHelp(String page) {
		String out = "";


		InputStream in = getClass().getResourceAsStream(page);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;

		byte[] data = new byte[1048576];

		try {
			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);

				buffer.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		out = buffer.toString();

		return out;
	}
}
