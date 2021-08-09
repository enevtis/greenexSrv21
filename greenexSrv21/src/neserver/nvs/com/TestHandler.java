package neserver.nvs.com;

import java.io.BufferedReader;
import java.io.File;
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

import greenexSrv2.nvs.com.MSEcxchange;
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

		out += getTestPage();

		out += getEndPage();

		return out;
	}

	public String getTestPage() {
		String out ="";
	
		
		
		
		return out;
	}

/*	
	public String getTestPage1() {
		String out = "";
	
		MSEcxchange me = new MSEcxchange(gData);

		List<String> recepients = new ArrayList<String>();
		recepients.add("enevtis-x@aeroflot.ru");
		List<String> attFiles = new ArrayList<String>();
		attFiles.add(gData.mainPath + File.separator + "img" + File.separator + "111.png");
		
		String commonSubjectLetter = "Test for attachments";
		String bodyLetter = "This is a <h1>body</h1>";
		bodyLetter += "<img src='111.png'>";
		
		me.sendOneLetter2(recepients, commonSubjectLetter, bodyLetter, attFiles);
		
		
		out += "E-mail is send";
		
		return out;
		
	}
*/	


}
