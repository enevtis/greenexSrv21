package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.GraphJsObject;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class ConnectionDataHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_conn_data";
	private String tableName = screenName;
	private String SQL = "";
	public String caption = "";


	public ConnectionDataHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {

		String out = "";
		String curStyle = "";
		
		int userRight = Utils.determineUserRights(gData, "connect_data", gData.commonParams.get("currentUser"));
		if (userRight < 1) {

			out += "Sorry, you don't have necessary authorisation! Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}
		
		
		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));
		
		this.caption = gData.tr("Connection data for") + " " + pr.typeObj + " " + pr.shortCaption + "<p class='prim'> (" + pr.physGuid + ")</p>";
		
		out += getBeginPage();
		out += strTopPanel(caption);

		SQL =  "SELECT *, ";	
		SQL +=  "CONCAT(\"<a href='/conn_test?id=\",id,'&guid=',object_guid,\"'>connection test</a>\") AS link";
		SQL +=  " FROM monitor_conn_data ";
		SQL += " WHERE object_guid='" + pr.physGuid 	+ "' ";
		
		
		this.fields = readTableMetadata(screenName);
		
		setDefaultValueForField(this.fields,"conn_type","database");
		setDefaultValueForField(this.fields,"object_guid",params.get("guid"));
		setDefaultValueForField(this.fields,"clnt","000");
		

		
		
		out += buildJavascriptRefreshFunction(SQL, tableName, fields, "SHOW", 120);
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();

		out += getCryptoAreaHtml();
		out += getCryptoAreaJavascript();		

		out += getEndPage();

		return out;
	}
private String getCryptoAreaHtml() {
	String out="";
	
	out +="<dev class='crypto'>";
	out += "<input id='input_crypto' type='password' class='crypto_ctrl' type='text' value='type here' > \n";	
	out += "<input type='button' value='get hash' onclick='onCryptoButtonClick();'>\n";
	out += "<label id='label_crypto'></label>\n";	
	out +="</dev>";
	return out;
}
private String getCryptoAreaJavascript() {
	String out="";
	out +="<script>";	
	out +="function onCryptoButtonClick(){";
	out +="console.log('onCryptoButtonClick');";		
	out +="	var pass=document.getElementById('input_crypto').value;";
	out +="	var hash=getRequest('/free_request?action=get_hash&pass=' + pass );";	
	out +="	document.getElementById('label_crypto').innerHTML=hash;";	
	out +="}";	
	out +="</script>";		
	return out;
}
}


