package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.GraphJsObject;

public class FreeRequestHandler extends HandlerTemplate {

	String test = "";

	public List<GraphJsObject> jsObjects = new ArrayList<GraphJsObject>();

	public FreeRequestHandler(globalData gData) {
		super(gData);

	}

	public void getResponse(SSLSocket socket, String paramsString) {

		test = paramsString;

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

	public String getPage() {
		String out = "";
		String action=params.get("action");
		
		switch(action) {
		
		case "get_guid":
			out = UUID.randomUUID().toString();
			break;
		case "get_hash":
			out = gData.getHashFromPassword(params.get("pass"));
			break;
		default:
			break;			
		}
		
		
		return out;
	}

}
