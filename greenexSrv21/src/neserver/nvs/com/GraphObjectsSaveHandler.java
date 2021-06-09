package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.GraphJsObject;

public class GraphObjectsSaveHandler extends HandlerTemplate {

	String postData = "";

	public List<GraphJsObject> jsObjects = new ArrayList<GraphJsObject>();
	public String caption = "";
	public int offsetTop = 30;
	public int offsetLeft = 10;

	public GraphObjectsSaveHandler(globalData gData) {
		super(gData);

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

	public String getPage() {
		String out = "";

		List<String> sqlList = getSqlListForGraphObjects();
		gData.sqlReq.saveResult(sqlList);
		out = "return from Command page" + params.get("page");

		return out;
	}

	protected List<String> getSqlListForGraphObjects() {

		List<String> out = new ArrayList();

		String tableName = "graph_objects";

		String sqlString = "delete from `" + tableName + "` where page='" + params.get("page") + "';";

		out.add(sqlString);

		String[] lines = postData.split("!!");

		for (String s : lines) {

			s = s.trim();

			if (s != null && !s.isEmpty()) {

				sqlString = "";
				String fields = "";
				String values = "";
				String[] columns = s.split(";;");

				fields += "`page`,";
				values += "'" + params.get("page") + "',";

				for (String col : columns) {

					if (col != null && !col.isEmpty()) {
						String[] parts = col.split("::");

						fields += "`" + parts[0] + "`,";

						if (parts.length == 2)
							values += "'" + parts[1] + "',";
						else
							values += "'',";
					}
				}

				fields = fields.substring(0, fields.length() - 1);
				values = values.substring(0, values.length() - 1);

				sqlString = "insert into " + tableName + " (" + fields + ") values (" + values + ");";
				out.add(sqlString);

			}
		}

		return out;
	}
}
