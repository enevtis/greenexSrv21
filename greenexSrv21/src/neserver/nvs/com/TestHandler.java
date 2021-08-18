package neserver.nvs.com;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
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

import graph.greenexSrv2.com.GraphDisk;
import graph.greenexSrv2.com.PngDisksDiagramPainter;
import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import health.greenexSrv2.nvs.com.RegularOpsbiHealthReport2;
import moni2.greenexSrv2.nvs.com.BatchJobTemplate;
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

		out += getTestPage4();

		out += getEndPage();

		return out;
	}

	public String getTestPage4() {
		String out = "";
		Map<String, String> params = new HashMap<String, String>();
		out += "Test 4";
		RegularOpsbiHealthReport2 t1 = new RegularOpsbiHealthReport2(gData, params);
		t1.imgPrefix = "/img/";
		t1.run();
		out += t1.body;

		
		return out;
	}
	
	public String getTestPage3() {
		String out ="";
		out = "Test!";
		List<GraphDisk> disks = initDisks();
		
		PngDisksDiagramPainter dp = new PngDisksDiagramPainter();
		dp.imgPath = gData.mainPath + File.separator + "img";
		
		String fileName = dp.paintDisksDiagram(disks, "OPSBI") + ".png";

		out += "<br>" + fileName;
		out += "<br><img src='../img/" + fileName + "'>"; 
		
//		Utils.deleteFile(gData.mainPath + File.separator + "img" + File.separator + fileName);

		
		return out;
	}
	
	public String getTestPage2() {
		String out ="";
	
		List<String> object_guids = new ArrayList<String>();
		
//		object_guids.add("79be7fcf-85ef-45af-9c1a-998e2ad185fe");

		
		BatchJobTemplate bjt = new BatchJobTemplate(gData,params);
		List<String> recepients= bjt.readRecepientsByProjects(object_guids);
		
		out += "recepients.size()=" + recepients.size();
		
		for(String s: recepients) {
			
			out += "<br>" + s;
			
		}
		
		
		out += "<br>AAA";
		
		
		
		return out;
	}

	

	
	
	
	public String getTestPage1() {
		String out = "";
	
		List<GraphDisk> disks = initDisks();
		
		PngDisksDiagramPainter dp = new PngDisksDiagramPainter();
		dp.imgPath = gData.mainPath + File.separator + "img";
		
		String fileGuid = dp.paintDisksDiagram(disks, "OPSBI");

		
		
		MSEcxchange me = new MSEcxchange(gData);

		List<String> recepients = new ArrayList<String>();
		recepients.add("enevtis-x@aeroflot.ru");
		List<String> attFiles = new ArrayList<String>();
		attFiles.add(fileGuid);
		
		String commonSubjectLetter = "Test for attachments";
		String bodyLetter = "This is a <h1>body</h1>";
		bodyLetter += "<img src='cid:" + fileGuid + ".png'>";
		
		me.sendOneLetter2(recepients, commonSubjectLetter, bodyLetter, attFiles);
		
		
		out += "E-mail is send";
		
		return out;
		
	}
	public List<String> readRecepientsByProjects(List<String> object_guids) {
		List<String> out = new ArrayList<String>();
		String SQL = "";
	
		
		String strGuids = "";
		
		if (object_guids.size() > 0) {
			
			for (String s : object_guids) {
				strGuids += "'" + s + "',";
			}
		
			strGuids = strGuids.substring(0, strGuids.length() - 1);
			
			SQL = "SELECT DISTINCT (email) FROM recepients WHERE object_guid IN ('all', " + strGuids + ") AND active='X'";
		
		} else {
			
			SQL = "SELECT DISTINCT (email) FROM recepients WHERE object_guid = 'all' AND active='X'";
			
		}
		
		gData.logger.info(SQL);
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					out.add(rec.get("email"));
				}
			}
		}

		return out;
	}	
	public List<GraphDisk> initDisks() {
		List<GraphDisk> out = new ArrayList();

		GraphDisk d = new GraphDisk();
		d.maxSizeGb = 60;
		d.usedSizeGb = 55;
//		d.minUsedSizeGb = 1;
		d.path = "/";
		out.add(d);
		
		d = new GraphDisk();
		d.maxSizeGb = 1024;
		d.usedSizeGb = 235;
		d.path = "/usr/sap/";
		out.add(d);

		d = new GraphDisk();
		d.maxSizeGb = 512;
		d.usedSizeGb = 67;		
		d.path = "/sapmnt/";
		out.add(d);		

		d = new GraphDisk();
		d.maxSizeGb = 512;
		d.usedSizeGb = 370;		
		d.path = "/oracle/";
		out.add(d);
		
		d = new GraphDisk();
		d.maxSizeGb = 450;
		d.usedSizeGb = 123;		
		d.path = "/disk1/";
		out.add(d);
		
		
		d = new GraphDisk();
		d.maxSizeGb = 220;
		d.usedSizeGb = 1.5f;		
		d.path = "/disk2/";
		out.add(d);
		return out;
	}

}
