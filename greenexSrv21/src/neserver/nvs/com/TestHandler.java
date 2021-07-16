package neserver.nvs.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.TblField;

public class TestHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_conn_data";
	private String tableName = screenName;
	private String SQL = "";
	public String caption = "Test page";

	public TestHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";

		int userRight = Utils.determineUserRights(gData, "connect_data", gData.commonParams.get("currentUser"));
		if (userRight < 1) {

			out += "Sorry, you don't have necessary authorisation! Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}

		out += getBeginPage();
		out += strTopPanel(caption);

		Map<String, String> sparams = new HashMap<String, String>();

		out += getDataFromWindows();

		out += getEndPage();

		return out;
	}

	
	public String getDataFromWindows() {
		String out = "";
	
		String testLink = getTestLink();
		
		String dirtyResponse = doCheckHttpsConnection(testLink);
		
		if (dirtyResponse.contains("]}")) {

			String cleanJsonString = dirtyResponse.substring(0,dirtyResponse.lastIndexOf("]}")+2);
			out += "test link (NSCPclientTestLink): " + testLink;
			out += "<br>" + parseJson(cleanJsonString);

//			out += cleanJsonString;
		
		} else {
			return dirtyResponse;
		}
		
		
		
		

		
		return out;
		
	}
	
	public String parseJson(String jsonString) {
		String out = "";
		
		JSONObject obj;
		
		try {
			obj = new JSONObject(jsonString);
			
			JSONObject res = obj.getJSONArray("payload").getJSONObject(0);

			JSONArray lines=res.getJSONArray("lines");
			JSONArray perf = lines.getJSONObject(0).getJSONArray("perf");
			
			for (int i = 0; i < perf.length(); i++) {

				String alias = perf.getJSONObject(i).getString("alias");
				
				float maxValue = perf.getJSONObject(i).getJSONObject("float_value").getFloat("maximum");
				
				float usedValue = perf.getJSONObject(i).getJSONObject("float_value").getFloat("value");
				String unit = perf.getJSONObject(i).getJSONObject("float_value").getString("unit");
				
				out += "<br>alias=" + alias + " maxValue= " + maxValue + " usedValue=" + usedValue + " unit=" + unit;
				
			}			
			
			


		} catch (JSONException e) {
	
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			out = errors.toString();
			gData.logger.severe(jsonString + "\n" + errors.toString());

		}
	return out;
	
	}
	public String doCheckHttpsConnection(String testLink) {
		String out = "";
		
		int timeOutMs = 10000;

		String pageUrl = testLink;
		
		disableSslVerification();
		HttpsURLConnection c = null;
		
		try {
		URL url = new URL(pageUrl);
			
			
		c = (HttpsURLConnection)url.openConnection();
		

		
		c.setRequestProperty ("Password", "init1234");
		c.setRequestMethod("GET");
//		c.setRequestProperty("Content-length", "0");
		
		c.setRequestProperty("Content-Type", "application/json");
		c.setRequestProperty("Accept", "application/json");
		
		
//		c.setUseCaches(false);
 //       c.setAllowUserInteraction(false);
		c.setConnectTimeout(timeOutMs);
		c.setReadTimeout(timeOutMs);	
		c.connect();
		int response = c.getResponseCode();
		
		
	       switch (response) {
           case 200:
           case 201:
           try(BufferedReader br = new BufferedReader(
        		   new InputStreamReader(c.getInputStream(), "utf-8"))) {
        		     StringBuilder sb = new StringBuilder();
        		     String responseLine = null;
        		     while ((responseLine = br.readLine()) != null) {
        		         sb.append(responseLine.trim());
        		     }
       		     out = "" + sb.toString();
           
           }
      }

		} catch (MalformedURLException e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(testLink + "\n" + errors.toString());
			

		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(testLink + "\n" + errors.toString());

		} finally {
  
	   
	   if (c != null) {
         try {
        	 c.disconnect();
         } catch (Exception e) {

 			StringWriter errors = new StringWriter();
 			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(testLink + "\n" + errors.toString());
        	 
         }
      }		
   }	
//		s.params.put("result",""+ response);
//		s.params.put("message","ok");

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
protected String getTestLink() {
	String out = "";
	String SQL = "select * from user_settings";
	List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);
			
			for (Map<String, String> rec : records) {
				
				out = rec.get("param_value");

			}
	
	
	
	return out;
}

}
