package neserver.nvs.com;

import java.io.File;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;

public class LogReaderHandler extends HandlerTemplate {

	private String SQL = "";

	public LogReaderHandler(globalData gData) {
		super(gData);

	}

	@Override
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
