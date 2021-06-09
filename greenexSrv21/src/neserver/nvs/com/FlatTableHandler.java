package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class FlatTableHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "";
	private String tableName = "";
	public String caption = "";
	private String SQL = "";
	private int initialTop = 80;
	private boolean showCrypto = false;
	public int userRight = 0;

	public FlatTableHandler(globalData gData) {
		super(gData);

	}

	public String getPage() {
		String out = "";

		out += getBeginPage();

		screenName = params.get("screen");
		String editMode = (params.containsKey("mode")) ? params.get("mode") : "READONLY";
		tableName = (params.containsKey("table")) ? params.get("table") : screenName;

		userRight = determineUserRights(screenName, gData.commonParams.get("currentUser"));
		if (userRight == 0) {

			out += "Недостаточно полномочий.";
			out += getEndPage();

			return out;
		}

		if (userRight == 1)
			editMode = "READONLY";
		if (userRight == 2)
			editMode = "SHOW";

		this.caption = (params.containsKey("caption")) ? params.get("caption") : "";

		if (params.containsKey("show_crypto")) {
			showCrypto = true;
			initialTop = 120;

		} else {
			showCrypto = false;
			initialTop = 80;
		}

		SQL = "select colLabel from data_struct_text where tableName='" + this.getClass().getSimpleName() + "' ";
		SQL += "and colName='" + screenName + "' and lang='" + gData.lang + "'";

		if (!this.caption.isEmpty())
			caption = gData.decode(this.caption);
		else
			caption = readFrom_flat_table_caption(screenName);

		out += strTopPanel(this.caption);

		SQL = readSQLfrom_flat_table_sql(screenName, tableName);

		SQL = replaceTemplates(SQL);

		this.fields = readFrom_flat_table_fields(screenName); // flat_table_fields where screen=screenName

		out += buildJavascriptRefreshFunction(SQL, tableName, fields, editMode.toUpperCase(), initialTop);
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();

		if (showCrypto) {
			out += getCryptoAreaHtml();
			out += getCryptoAreaJavascript();
		}

		out += getEndPage();

		return out;
	}

	protected int determineUserRights(String screen, String userName) {
		int out = 0;

		String SQL1 = "", SQL2 = "";
		SQL1 = "SELECT * FROM sql_text WHERE class='" + this.getClass().getSimpleName() + "' "
				+ " AND filter='check_user_rights' ORDER BY id \n";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL1);

		for (Map<String, String> rec : records) {
			SQL2 += rec.get("sql_text") + " ";
		}

		SQL2 = SQL2.replace("!SCREEN!", screen);
		SQL2 = SQL2.replace("!USERNAME!", userName);

		records = gData.sqlReq.getSelect(SQL2);

		for (Map<String, String> rec : records) {
			out = Integer.valueOf(rec.get("result"));
		}

		return out;
	}

	protected String replaceTemplates(String SQL_first) {
		String out = SQL_first;
		String paramName = "";

		paramName = "monitor_number";
		if (params.containsKey(paramName))
			out = SQL_first.replace("!MONITOR_NUMBER!", params.get(paramName));

		return out;
	}

	private String getCryptoAreaHtml() {
		String out = "";

		out += "<dev class='crypto'>";
		out += "<input id='input_crypto' type='password' class='crypto_ctrl' type='text' value='type here' > \n";
		out += "<input type='button' value='get hash' onclick='onCryptoButtonClick();'>\n";
		out += "<label id='label_crypto'></label>\n";
		out += "</dev>";
		return out;
	}

	private String getCryptoAreaJavascript() {
		String out = "";
		out += "<script>";
		out += "function onCryptoButtonClick(){";
		out += "console.log('onCryptoButtonClick');";
		out += "	var pass=document.getElementById('input_crypto').value;";
		out += "	var hash=getRequest('/free_request?action=get_hash&pass=' + pass );";
		out += "	document.getElementById('label_crypto').innerHTML=hash;";
		out += "}";
		out += "</script>";
		return out;
	}

}
