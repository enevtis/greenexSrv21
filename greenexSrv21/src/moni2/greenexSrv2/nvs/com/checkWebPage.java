package moni2.greenexSrv2.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class checkWebPage extends BatchJobTemplate implements Runnable {

	public checkWebPage(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_shedule();
			doCheck();
			reSetRunningFlag_shedule();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}
	protected void doCheck() {

		String message = "";

		List<remoteSystem> db_systems = readABAP_systemsListForCheck();
		
		gData.truncateLog(params.get("job_name"));
		gData.saveToLog("found " + db_systems.size() + " systems to check.", params.get("job_name"));

		for (remoteSystem s : db_systems) {
			message = s.params.get("short") + " " + s.params.get("ip") + " " + s.params.get("sid") + " "
					+ s.params.get("sysnr");
			message += " " + s.params.get("guid");

			gData.saveToLog(message, params.get("job_name"));

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));

			ConnectionData conData = Utils.readConnectionParameters(gData,pr);

			s.params.put("user", conData.user);
			s.params.put("password", conData.password);
			s.params.put("clnt", conData.clnt);
			s.params.put("job_name", params.get("job_name"));
			s.params.put("protocol",  conData.protocol.isEmpty() ? "https": conData.protocol);
			s.params.put("page",  conData.page.isEmpty() ? "": conData.page);
			
			
			gData.saveToLog("connect with parameters: " + conData.user + " " + conData.clnt, params.get("job_name"));

			int response = 0;
			String protocol = s.params.get("protocol");
			
			if (protocol.toLowerCase().equals("https"))
				doCheckHttpsConnection(s);
			else 
				doCheckHttpConnection(s);


			String SQL_result = "";

			SQL_result += "insert into monitor_results ( ";
			SQL_result += "`object_guid`,`monitor_number`,";
			SQL_result += "`check_date`,`result_number`,";
			SQL_result += "`result_text`,`is_error`) values (";
			SQL_result += "'" + s.params.get("guid") + "',";
			SQL_result += "" + params.get("job_number") + ",";
			SQL_result += "now(),";
			SQL_result += "" + s.params.get("result") + ",";
			SQL_result += "'" + s.params.get("message") + "',";
			SQL_result += "''";
			SQL_result += ")";

			gData.sqlReq.saveResult(SQL_result);

		}

	}
	
	public int doCheckHttpsConnection(remoteSystem s) {
		int out = -1;
		
		int timeOutMs = gData.commonParams.containsKey("WebPageCheckTimeOutSec") ?  
				Integer.valueOf(gData.commonParams.get("WebPageCheckTimeOutSec")) * 1000 :
					5000;
		
		String pageUrl = s.params.get("protocol") + "://" + s.params.get("ip") + ":" + 
				s.params.get("port") + s.params.get("page");

		
		
		Authenticator.setDefault(new Authenticator() {

		    @Override
		    protected PasswordAuthentication getPasswordAuthentication() {          
		        String user = s.params.get("user");
		        String password = s.params.get("password");
		    	return new PasswordAuthentication(user, password.toCharArray());
		    }
		});
		
		disableSslVerification();
		
		
		try {
		URL url = new URL(pageUrl);
		HttpsURLConnection connection;		
			
		connection = (HttpsURLConnection)url.openConnection();
		
		connection.setConnectTimeout(timeOutMs);
		connection.setReadTimeout(timeOutMs);
		connection.setRequestMethod("GET");
		connection.connect();
		int response = connection.getResponseCode();	
		
		s.params.put("result",""+ response);
		s.params.put("message","ok");
		
		} catch (IOException e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.saveToLog("connect to " + pageUrl + " is not successful!\n" + errors.toString(), params.get("job_name"));
			s.params.put("result","10000");
			s.params.put("message",pageUrl + " is not reached:" + e.getMessage());
		}

		
		return out;
	}

	public int doCheckHttpConnection(remoteSystem s) {
		int out = -1;
		
		int timeOutMs = gData.commonParams.containsKey("WebPageCheckTimeOutSec") ?  
				Integer.valueOf(gData.commonParams.get("WebPageCheckTimeOutSec")) * 1000 :
					5000;
		
		String pageUrl = s.params.get("protocol") + "://" + s.params.get("ip") + ":" + 
				s.params.get("port") + s.params.get("page");

//		
//		
//		Authenticator.setDefault(new Authenticator() {
//
//		    @Override
//		    protected PasswordAuthentication getPasswordAuthentication() {          
//		        String user = s.params.get("user");
//		        String password = s.params.get("password");
//		    	return new PasswordAuthentication(user, password.toCharArray());
//		    }
//		});
//		
//		disableSslVerification();
		
		
		try {
		URL url = new URL(pageUrl);
		HttpURLConnection connection;		
			
		connection = (HttpURLConnection)url.openConnection();
		
		connection.setConnectTimeout(timeOutMs);
		connection.setReadTimeout(timeOutMs);
		connection.setRequestMethod("GET");
		connection.connect();
		int response = connection.getResponseCode();	
		
		s.params.put("result",""+ response);
		s.params.put("message","ok");
		
		} catch (IOException e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.saveToLog("connect to " + pageUrl + " is not successful!\n" + errors.toString(), params.get("job_name"));
			s.params.put("result","10000");
			s.params.put("message",pageUrl + " is not reached:" + e.getMessage());
		}

		
		return out;
	}
	
	protected void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            @Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            @Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            @Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            @Override
				public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			
	    } catch (KeyManagementException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

	    }
	}
}
