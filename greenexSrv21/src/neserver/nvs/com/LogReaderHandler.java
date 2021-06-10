package neserver.nvs.com;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.List;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.TblField;

public class LogReaderHandler extends HandlerTemplate {

	private String SQL = "";

	public LogReaderHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {
		String out = "";
		int lines = 1000;
		out += getBeginPage();
		out += strTopPanel("Last " + lines + " lines of log:");
		String fileName = gData.mainPath + File.separator + "log" + File.separator + "logger.trc";

		int userRight = Utils.determineUserRights(gData, this.getClass().getSimpleName(), gData.commonParams.get("currentUser"));
		if (userRight < 1) {

			out += "Sorry, you don't have necessary authorisation! Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}
		
		
		out += getLastNLogLines(new File(fileName), lines);

		out += getEndPage();

		return out;
	}

	public String getLastNLogLines(File file, int nLines) {
		StringBuilder s = new StringBuilder();
		try {
			Process p = Runtime.getRuntime().exec("tail -" + nLines + " " + file);
			java.io.BufferedReader input = new java.io.BufferedReader(
					new java.io.InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				s.append(line + "<br>");
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		return s.toString();
	}

}
