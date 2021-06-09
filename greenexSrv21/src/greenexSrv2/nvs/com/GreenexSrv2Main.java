package greenexSrv2.nvs.com;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import moni.greenexSrv2.nvs.com.AlertAnalyzer;
import moni.greenexSrv2.nvs.com.AlertAnalyzerDisks;
import moni.greenexSrv2.nvs.com.MailNotificator;
import moni.greenexSrv2.nvs.com.MailRecoveryList;
import moni.greenexSrv2.nvs.com.MonitorService;
import moni.greenexSrv2.nvs.com.SelfControl;
import moni.greenexSrv2.nvs.com.batchJob;
import moni2.greenexSrv2.nvs.com.AnalyzeScan;
import moni2.greenexSrv2.nvs.com.AnalyzeScanDisks;
import moni2.greenexSrv2.nvs.com.AnalyzeScanOracleTs;
import moni2.greenexSrv2.nvs.com.HouseKeepingJob;
import moni2.greenexSrv2.nvs.com.JobScan;
import moni2.greenexSrv2.nvs.com.MailingScan;
import moni2.greenexSrv2.nvs.com.RegularReport;

public class GreenexSrv2Main {

	public static GreenexSrv2Main srv = null;
	public static WebUIService webSrv = null;
//	public static ScheduledExecutorService serviceMail = Executors.newSingleThreadScheduledExecutor();

	public globalData gData = new globalData();

	public static void main(String[] args) {
		if (args.length == 0) {
			start();
			while (true) {
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (args.length == 1) {

			if (args[0].equals("start")) {

				start();
				while (true) {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} else if (args[0].equals("stop")) {
				System.exit(0);
			}

		}
	}

	public static void start() {
		srv = new GreenexSrv2Main();

		LoadLibrary();
		srv.gData.init();

		
		System.setProperty("javax.net.ssl.keyStore","servercert.jks");
		System.setProperty("javax.net.ssl.keyStorePassword","password");
		System.setProperty("file.encoding","UTF-8");
		System.setProperty("sun.jnu.encoding","UTF-8");

		
		
		
		srv.gData.logger.info("starting greenex gui server...Version:" + srv.gData.getVersionInfo());
		srv.gData.logger.info("runtime details:" + srv.gData.getSystemProperties());

		webSrv = new WebUIService(srv.gData);
		srv.gData.service.execute(webSrv);

		
		if (srv.gData.commonParams.containsKey("systemScanning")) {
			
			if (srv.gData.commonParams.get("systemScanning").equals("true")) {
				
				srv.gData.executor.scheduleAtFixedRate(new JobScan(srv.gData), 0,1, TimeUnit.MINUTES);
				srv.gData.logger.info("systemScanning started...");

			} else {
				srv.gData.logger.info("systemScanning is disallowed...");
			}
		
		}


		
		
		srv.gData.executor.scheduleAtFixedRate(new AnalyzeScan(srv.gData), 0, 60, TimeUnit.SECONDS);

		srv.gData.executor.scheduleAtFixedRate(new AnalyzeScanDisks(srv.gData), 30, 60, TimeUnit.SECONDS);
		
		srv.gData.executor.scheduleAtFixedRate(new AnalyzeScanOracleTs(srv.gData), 7, 60, TimeUnit.SECONDS);
		
		srv.gData.executor.scheduleAtFixedRate(new RegularReport(srv.gData), 3, 60, TimeUnit.SECONDS);
		
		srv.gData.executor.scheduleAtFixedRate(new SelfCheck(srv.gData), 5, 60, TimeUnit.SECONDS);
		
		srv.gData.service.scheduleAtFixedRate(new HouseKeepingJob(srv.gData), 60, 120, TimeUnit.MINUTES);
		
		srv.gData.logger.info("All services Greenex monitor srarted...");

		
		
		srv.gData.logger.info("User GUI on port " + srv.gData.commonParams.get("webServicePort") + " srarted...");
		
		
		while (true) {
			try {
				Thread.sleep(300 * 1000);
			} catch (InterruptedException e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				srv.gData.logger.severe(errors.toString());
			}
		}
	}

	public static void stop() {

		System.exit(0);

	}

	private static void LoadLibrary() {
		String currentLibraryPath = System.getProperty("user.dir") + File.separator + "lib";
		String fileName = "";
		File folder = new File(currentLibraryPath);

		String[] files = folder.list();

		for (String file : files) {
			try {
				fileName = currentLibraryPath + File.separator + file;

				if (fileName.endsWith("jar")) {
					addSoftwareLibrary(new File(fileName));
				}
			} catch (Exception e) {
				srv.gData.logger.severe(e.getMessage());
			}

		}

	}

	private static void addSoftwareLibrary(File file) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
	}

	public List<batchJob> ReadMonitorList(globalData gData) {

		List<batchJob> jobs = new ArrayList<>();

		Connection conn = null;

		try {

			conn = DriverManager.getConnection(gData.commonParams.get("connectionString"),
					gData.commonParams.get("user"), gData.commonParams.get("password"));

			Statement stmt = conn.createStatement();
			String SQL = "";

			SQL += "select * from monitor_schedule ";
			SQL += " where active = 'X' ";
//
//			if (gData.debugMode) {
//				SQL += "and slot = 0"; // For test proposes we restrict requests.
//			} else {
//				SQL += "and slot > 0";
//			}

			ResultSet rs = stmt.executeQuery(SQL);

			while (rs.next()) {

				batchJob job = new batchJob();

				job.job_name = rs.getString("job_name");
				job.delay = rs.getInt("delay_min");
				job.interval = rs.getInt("interval_min");
				job.number = rs.getInt("number");
				job.conn_type = rs.getString("conn_type");
				job.slot = rs.getInt("slot");
				jobs.add(job);

			}

			conn.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
			}
		}

		return jobs;
	}
}
