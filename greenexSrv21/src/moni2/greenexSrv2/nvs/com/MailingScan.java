package moni2.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;

import greenexSrv2.nvs.com.globalData;

public class MailingScan implements Runnable {
	public globalData gData;

	public MailingScan(globalData gData) {
		this.gData = gData;
	}

	@Override
	public void run() {

		try {

			mailing();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	private void mailing() {
		gData.logger.info("mailing");
	}
}
