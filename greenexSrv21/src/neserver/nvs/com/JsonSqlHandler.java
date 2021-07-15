package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;

public class JsonSqlHandler extends HandlerTemplate {

	String postData = "";

	public JsonSqlHandler(globalData gData) {
		super(gData);

	}

	@Override
	public void getResponse(SSLSocket socket, String paramsString) {

	
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

	@Override
	public String getPage() {
		String out = "";
		
/*		
		out += "[";
		out += "{";
		out += "\"id\": \"456\",";
		out += " \"full_name\":\"GOOBER ANGELA\"";
		out += "},";
		out += " {";
		out += " \"id\": \"123\",";
		out += " \"full_name\":\"BOB, STEVE\"";
		out += "}";
		out += "]";
		
		
	
	 	out += "var params = '?refTable=' + refTable; \n"; 		 		
 		out += " params += '&refKey=' + refKey; \n"; 		
 		out += " params += '&refKeyType=' + refKeyType; \n"; 
 		out += " params += '&refValue=' + refValue; \n"; 
*/	
	
		out = getJson(params.get("refTable"),params.get("refKey"),params.get("refKeyType"),params.get("refValue"));
		
		return out;
	}

	public String getJson(String refTable, String refKey, String refKeyType, String refValue) {
		String out = "";
		
		String SQL = "";
		SQL += "select `" + refKey + "`,`" + refValue + "` from " + refTable + ";";


		out += "[";

		
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {


				for (Map<String, String> rec : records_list) {
					out += "{";
					out += "\"key\":\"" + rec.get(refKey) + "\",\"value\":\"" + rec.get(refValue) + "\"";
					out += "},";
				}

			
			}
		}

		out = out.substring(0, out.length() - 1);

		out += "]";


		
		return out;
	}
}
