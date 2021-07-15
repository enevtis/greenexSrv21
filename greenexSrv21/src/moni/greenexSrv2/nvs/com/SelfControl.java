package moni.greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import greenexSrv2.nvs.com.globalData;

public class SelfControl implements Runnable{
	public globalData gData = new globalData();

	public SelfControl (globalData gData) {
		this.gData = gData;
		
	}
	@Override
	public void run() {
		try {
			
			DoCheck();
			

		} catch(Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());	
		}	
		
	}

protected boolean DoCheck() {
	boolean result = false;
	
//	gData.logger.info("gData.executor.getActiveCount()= " + gData.executor.getActiveCount());
	
	
	return result;
}
	
}
