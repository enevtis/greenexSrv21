package analyse.greenexSrv2.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.BatchJobTemplate;

public class AnalyseABAPSystemIsDown extends BatchJobTemplate implements Runnable {

	public String currMonitorNumber = "303";
	public Map<String, String> appservers = new HashMap();

	public AnalyseABAPSystemIsDown(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_regular();

			analyze();

			reSetRunningFlag_regular();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	private void analyze() {

//		checkUsedPercentWorkProcesses();
//		sendLetters();

	}

}
