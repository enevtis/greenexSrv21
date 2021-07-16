package moni2.greenexSrv2.nvs.com;

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
import java.util.ArrayList;
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

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.remoteSystem;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class winServersDisksDataCollect extends BatchJobTemplate implements Runnable {

	public String nowForSQL = "";

	public winServersDisksDataCollect(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {
		try {

			nowForSQL = gData.nowForSQL();
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
		List<remoteSystem> db_systems = readServersListForCheck(params.get("job_number"));

		List<String> insSql = new ArrayList();

		gData.truncateLog(params.get("job_name"));
		gData.saveToLog("found " + db_systems.size() + " systems to start.", params.get("job_name"));

		for (remoteSystem s : db_systems) {

			ObjectParametersReader parReader = new ObjectParametersReader(gData);
			PhisObjProperties pr = parReader.getParametersPhysObject(s.params.get("guid"));

			ConnectionData conData = readConnectionParameters(pr);
//			gData.saveToLog("ip =" + conData.ip + " appType.hash=" + conData.appType, params.get("job_name"));
//			gData.saveToLog("conData.user =" + conData.user + " conData.hash=" + conData.hash, params.get("job_name"));

			collectDisksDataFromWindowsServer(s, insSql);

		}

		for (String s : insSql) {

			gData.saveToLog(s, params.get("job_name"));

		}

		gData.sqlReq.saveResult(insSql);

		gData.saveToLog("all " + db_systems.size() + " finished successfuly.", params.get("job_name"));

	}

	protected boolean collectDisksDataFromWindowsServer(remoteSystem s, List<String> insSql) {
		boolean out = false;

		String nscpClientLink = "https://" + s.params.get("ip") + ":8443/query/check_drivesize";

		String dirtyResponse = doCheckHttpsConnection(nscpClientLink);
		String cleanJsonString = "";
		if (dirtyResponse.contains("]}")) {

			cleanJsonString = dirtyResponse.substring(0, dirtyResponse.lastIndexOf("]}") + 2);
			parseJson(s, cleanJsonString, insSql);
			out = true;

		}

//		gData.saveToLog("request =" + nscpClientLink + "\n" +  cleanJsonString, params.get("job_name"));		

		return out;
	}

	public String parseJson(remoteSystem s, String jsonString, List<String> insSql) {
		String out = "";

		JSONObject obj;

		try {
			obj = new JSONObject(jsonString);

			JSONObject res = obj.getJSONArray("payload").getJSONObject(0);

			JSONArray lines = res.getJSONArray("lines");
			JSONArray perf = lines.getJSONObject(0).getJSONArray("perf");

			for (int i = 0; i < perf.length(); i++) {

				String alias = perf.getJSONObject(i).getString("alias");

				if (alias.contains("used") && !alias.contains("%")) {

					String diskName = alias.substring(0, 2);
					float maxValue = perf.getJSONObject(i).getJSONObject("float_value").getFloat("maximum");

					float usedValue = perf.getJSONObject(i).getJSONObject("float_value").getFloat("value");

					String unit = perf.getJSONObject(i).getJSONObject("float_value").getString("unit");
					
					if (unit.contains("TB")) {
						
						maxValue = maxValue * 1024;
						usedValue = usedValue * 1024;
					}
					
					
					String SQL = "";
					SQL += "insert into monitor_disks (";
					SQL += "`server_guid`,";
					SQL += "`name`,";
					SQL += "`max_size_gb`,";
					SQL += "`used_size_gb`,";
					SQL += "`check_date`";
					SQL += ") values (";
					SQL += "'" + s.params.get("guid") + "',";
					SQL += "'" + diskName + "',";
					SQL += "" + maxValue + ",";
					SQL += "" + usedValue + ",";
					SQL += "'" + nowForSQL + "'";
					SQL += ")";
					// nowForSQL

					if (maxValue > 0) {
						insSql.add(SQL);
					}
				}

			}

		} catch (JSONException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.saveToLog(errors.toString(), params.get("job_name"));

		}
		return out;

	}

	public String doCheckHttpsConnection(String nscpClientLink) {
		String out = "";

		int timeOutMs = 10000;

		String pageUrl = nscpClientLink;

		disableSslVerification();
		HttpsURLConnection c = null;

		try {
			URL url = new URL(pageUrl);

			c = (HttpsURLConnection) url.openConnection();

			c.setRequestProperty("Password", "init1234");
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-Type", "application/json");
			c.setRequestProperty("Accept", "application/json");
			c.setConnectTimeout(timeOutMs);
			c.setReadTimeout(timeOutMs);
			c.connect();
			int response = c.getResponseCode();

			switch (response) {
			case 200:
			case 201:
				try (BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), "utf-8"))) {
					StringBuilder sb = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						sb.append(responseLine.trim());
					}
					out = "" + sb.toString();

				}
				break;
			default:
				out = "error:" + response;

			}

		} catch (MalformedURLException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(nscpClientLink + "\n" + errors.toString());

		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(nscpClientLink + "\n" + errors.toString());

		} finally {

			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception e) {

					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(nscpClientLink + "\n" + errors.toString());

				}
			}
		}

		return out;
	}

	protected void disableSslVerification() {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
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
			} };

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
