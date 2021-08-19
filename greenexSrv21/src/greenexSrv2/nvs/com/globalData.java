package greenexSrv2.nvs.com;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

import simplecrypto.nvs.com.SimpleCrypto;

import java.util.jar.Manifest;
import java.util.jar.Attributes;

public class globalData {

	public final Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	public ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(50);
	public ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

	public Hashtable<String, String> commonParams = new Hashtable<String, String>();
	public Hashtable<String, String> checksPages = new Hashtable<String, String>();
	public Hashtable<String, String> connTypes = new Hashtable<String, String>();

	public String mainPath = "";
	public SqlRequest sqlReq = null;
	public SshRequest sshReq = null;
	
	public String SecretKey = "";

	public String debugText = "";
	public volatile boolean debugMode = false;
	public String periodBetweenMailMinutes = "180";
	public int maxOracleUsedTableSpacePercent = 95;

	public String TableCellbgColor="#EEEEEE";
	
	public boolean allowEmailSending = false;
	public boolean allowRemoteChecking = false;
	
	
	public String clrDiagramFill = "#0524aa";
	public String clrDiagramBackground = "#A0A0A0";
	public String diagramStyle = "background-color:black;color:white;font-family:Courier New, monospace;font-size:12px;";

	public Map<String, String> router = new HashMap();
	public String lang = "EN";	
	public Hashtable<String, String> msgText = new Hashtable<String, String>(); 
	
	public void init() {

		mainPath = System.getProperty("user.dir");

		SecretKey = ReadSecretKey();
		initFromProprties();
		sqlReq = new SqlRequest(this);
		sshReq = new SshRequest(this);
		initFromDataBase();
//		initChecksPages();
		initServerPageRouter();
		this.lang = this.commonParams.get("interfaceLang");

	}

	
	public void initServerPageRouter() {
		
		String pack = "neserver.nvs.com.";

		router.put("/", pack + "RootPageHandler");
		router.put("/img", pack + "ImgHandler");
		router.put("/src", pack + "SrcHandler");
		router.put("/help", pack + "HelpHandler");	
		router.put("/about", pack + "AboutProgramHandler");
		
		router.put("/landscape", pack + "LandscapeHandler");
		router.put("/system_image", pack + "SystemImageEditHandler2");
		
		router.put("/schedule", pack + "ScheduleTableHandler");
		router.put("/jobs", pack + "RootPageHandler");
		router.put("/flat_table", pack + "FlatTableHandler");
		router.put("/gr_page_save", pack + "GraphObjectsSaveHandler");
		router.put("/free_request", pack + "FreeRequestHandler");		
		router.put("/dashboard", pack + "DashboardHandler");
		router.put("/tech_params", pack + "TechParamsHandler");
		router.put("/sql_save", pack + "SqlSaveHandler");
		router.put("/sch_checks", pack + "ScheduledChecksListHandler");
		router.put("/json_sql", pack + "JsonSqlHandler");
		router.put("/monitor_results", pack + "MonitorResultTableHandler");
		router.put("/connect_data", pack + "ConnectionDataHandler");
		router.put("/conn_test", pack + "ConnectionTestHandler");
		router.put("/log",pack + "LogReaderHandler");
		router.put("/sql_request",pack + "SQLRequestHandler2");
		router.put("/email_control",pack + "EmailControlHandler");
		router.put("/send_mail",pack + "SendMailHandler");
		router.put("/summary_reports",pack + "SummaryReports");
		router.put("/ssh_request",pack + "SshRequestHandler");
		router.put("/oracle_tablespace",pack + "OracleTableSpaceHandler");	
		router.put("/import_files" ,pack + "ImportFilesHandler");	
		router.put("/tree_data", pack + "TreeDataHandler");	
		router.put("/tree_save", pack + "TreeSaveHandler");
		router.put("/time_line", pack + "TimeLineHandler");
		router.put("/utils", pack + "UtilsHandler");
		router.put("/test", pack + "TestHandler");
		router.put("/test2", pack + "Test2Handler");
		router.put("/idocs", pack + "IdocsPageHandler");
		
	}
	
	
	
	
	public String tr(String key) {
		String out = "";

		return key;
	}	
	

	
	public String getRandomUUID() {
		String out = "";
		UUID uuid = UUID.randomUUID();
		return uuid.toString();

	}
	
	
	public void initFromProprties() {

		Properties params = new Properties();
//		
		try {

			params.load(new FileInputStream(new File(this.mainPath + File.separator + "common.properties")));

			Enumeration e = params.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = params.getProperty(key);
				this.commonParams.put(key, value);

			}

			if (this.commonParams.containsKey("hash")) {
				SimpleCrypto crypto = new SimpleCrypto();
				this.commonParams.put("password", SimpleCrypto.decrypt(SecretKey, commonParams.get("hash")));
			}

		} catch (FileNotFoundException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());

		} catch (IOException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());
		}

	}


	protected void initFromDataBase() {

		String SQL = "select * from user_settings where user_name='admin'";

		List<Map<String, String>> records_list = sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					String paramName = rec.get("param_name");
					String paramValue = rec.get("param_value");
					commonParams.put(paramName, paramValue);

				}
			}
		}

	}


	public void initFromDataBase(String SQL) {


		List<Map<String, String>> records_list = sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					String paramName = rec.get("param_name");
					String paramValue = rec.get("param_value");
					commonParams.put(paramName, paramValue);

				}
			}
		}

	}
	
	
	
	
	public static String getVersionInfo() {
		String out = "";

		try {

			Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
					.getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				URL manifestUrl = resources.nextElement();
				Manifest manifest = new Manifest(manifestUrl.openStream());
				Attributes mainAttributes = manifest.getMainAttributes();
				String softVendor = mainAttributes.getValue("Soft-Vendor");

				if (softVendor != null && !softVendor.isEmpty()) {

					String softTitle = mainAttributes.getValue("Soft-Title");
					String softVersion = mainAttributes.getValue("Soft-Version");

					out += softVersion;

				}
			}

		} catch (IOException E) {
			out = "ERROR getting version";
		}

		return out;
	}

	public String getSystemProperties() {
		String out = "";
		out += "\njava.version=" + System.getProperty("java.version");
		out += "\njava.runtime.version=" + System.getProperty("java.runtime.version");
		out += "\nos.name=" + System.getProperty("os.name");
		out += "\nos.version=" + System.getProperty("os.version");
		out += "\nos.arch=" + System.getProperty("os.arch");

		return out;
	}

	public static String getHostName() {
		String out = "";

		try {
			out = InetAddress.getLocalHost().getHostName();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return out;
	}

	public Map<String, String> queryToMap(String query) {

		if (query == null || query.isEmpty())
			return null;

		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			} else {
				result.put(entry[0], "");
			}
		}
		return result;
	}

	public String getDebugText() {
		String out = "";

		out = debugText;

		return out;
	}

	public String ReadSecretKey() {
		String out = "";
		Properties key_prop = new Properties();

		try {
			key_prop.load(new FileInputStream(new File(this.mainPath + File.separator + "secret.key")));
			String SecretKey = key_prop.getProperty("secret");
			out = SecretKey.trim();

		} catch (FileNotFoundException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());
		} catch (IOException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());
		}

		return out;

	}

	public String getPasswordFromHash(String hash) {

		String out = "";
		try {
			out = SimpleCrypto.decrypt(this.SecretKey, hash);
		} catch (Exception e1) {

			StringWriter errors = new StringWriter();
			e1.printStackTrace(new PrintWriter(errors));
			this.logger.severe(errors.toString());

		}
		return out;
	}

	public String getHashFromPassword(String password) {

		String out = "";
		try {
			out = SimpleCrypto.encrypt(this.SecretKey, password);
		} catch (Exception e1) {

			StringWriter errors = new StringWriter();
			e1.printStackTrace(new PrintWriter(errors));
			this.logger.severe(errors.toString());

		}
		return out;
	}

	public String getOwnHostname() {
		String out = "";

		InetAddress ip;
		String hostname;

		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			out = hostname;
		} catch (UnknownHostException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			this.logger.severe(errors.toString());
		}
		return out;
	}

	public String getOwnIp() {
		String out = "";
	     
		InetAddress ip;
	        String hostname;
	        try {
	            ip = InetAddress.getLocalHost();
	            hostname = ip.getHostName();
	            String parts[] = ip.toString().split("\\/");
	            out = parts[1];
	 
	        } catch (UnknownHostException e) {

	        	StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				this.logger.severe(errors.toString());
	        }
		
		return out;
	}


	public void saveToLog(String outText, String fileName) {

		String fullFileName = this.mainPath + File.separator + "log" + File.separator + fileName + ".log";

	
		try {
			File fileOut = new File(fullFileName);

			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();

			FileWriter fr = new FileWriter(fileOut, true);
			BufferedWriter br = new BufferedWriter(fr);
			br.write(dateFormat.format(date) + ": " + outText + "\r\n");

			br.close();
			fr.close();

		} catch (Exception e) {
			this.logger.info(e.getMessage());
		}

	}
	public void truncateLog(String fileName) {

		String fullFileName = this.mainPath + File.separator + "log" + File.separator + fileName + ".log";

	
		try {
			File fileOut = new File(fullFileName);

			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();

			FileWriter fr = new FileWriter(fileOut, false);
			BufferedWriter br = new BufferedWriter(fr);
			br.write("");

			br.close();
			fr.close();

		} catch (Exception e) {
			this.logger.info(e.getMessage());
		}

	}

	public String getCurrentTime() {

		return new SimpleDateFormat("HH:mm:ss  dd.MM.yyyy").format(new Date());

	}
	public static String decode(String url)  
    {  
              try {  
                   String prevURL="";  
                   String decodeURL=url;  
                   while(!prevURL.equals(decodeURL))  
                   {  
                        prevURL=decodeURL;  
                        decodeURL=URLDecoder.decode( decodeURL, "UTF-8" );  
                   }  
                   return decodeURL;  
              } catch (UnsupportedEncodingException e) {  
                   return "Issue while decoding" +e.getMessage();  
              }  
    } 

public String nowForSQL() {
	return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
}
}
