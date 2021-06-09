package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import com.sun.net.httpserver.HttpExchange;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.TblField;

public class HandlerTemplate {

	public globalData gData;
	public Map<String, String> params;
	public String caption = "";
;

	public HandlerTemplate(globalData gData) {
		this.gData = gData;
	}

	public void getResponse(SSLSocket socket, String paramsString) {

		parseParams(paramsString);
		String resp = getPage();

		try {

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			out.write(header200());
			out.write("Content-Length: " + resp.getBytes("UTF-8").length + "\r\n");
			out.write("\r\n");
			out.write(resp);

			out.close();

		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	
	public String getPage() {
		String out = "";

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

	public void parseParams(String requestString) {

		String strForParse = "";
		String[] parts = requestString.split("\\?+");
		
		if (parts.length == 2) {
			params = queryToMap(parts[1]);
		} else {
			params = queryToMap(requestString);

		}

	}

	public String header200() {
		String out = "";
		out += "HTTP/1.0 200 OK\r\n";
		out += "Content-Type: text/html; charset=utf-8\r\n";

		return out;
	}

	public String header302(String location) {
		String out = "";
		out += "HTTP/1.0 302 Redirect\r\n";
		out += "Location:" + location + "\r\n";
		out += "Content-Type: text/html; charset=utf-8\r\n";
		return out;
	}

	protected String getBeginPage() {
		String out = "";
		out += "<!DOCTYPE html> \n";
		out += "<html> \n";
		out += "<head> \n";
		out += "<meta charset=\"utf-8\"> \n";
		out += "<link rel=\"icon\" href=\"/img/nvs.png\"> \n";
		out += "<script src=\"/src/greenex.js\"></script> \n";
		out += "<link rel=\"stylesheet\" href=\"/src/style.css\"> \n";
		out += "</head>";
		out += "<body>";
		return out;
	}

	protected String getEndPage() {
		String out = "";
		out += " </body> \n";
		out += " </html>  \n";
		return out;
	}

	public String strTopPanel(String text) {
		String out = "";

		out += strPopupMenu();
		out += "<p class='caption1'>&nbsp;&nbsp;" + gData.tr(text) + "</p><br>";

		return out;
	}

	public String strPopupMenu() {
		String out = "";
		String link = "";

		out += "<img id='pic_more' src='/img/menu.png' title='menu'";
//		out += " style='position:absolute;left:10px;top:25px;cursor:pointer;' ";

		out += " style='cursor:pointer;' ";
		out += " onclick='showMenu(this.id)';\" ";
		out += ">";

		out += "<div class='menu'>  \n";

		String SQL = "select * from global_menu where lang='" + gData.lang + "'" + " order by id";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

//					out += "<div id='menu_" + rec.get("id") + "' class='menu-item' onclick=\"window.location='"
//							+ rec.get("link") + "';\">" + rec.get("description") + "</div>  \n";

					out += "<div id='menu_" + rec.get("id") + "' class='menu-item' onclick=\"window.open('"
					+ rec.get("link") + "', '_blank');\">" + rec.get("description") + "</div>  \n";
				
				
				
				}
			}
		}

		out += "</div>  \n";

		return out;
	}

	protected String readFrom_flat_table_caption(String screenName) {
		String out = "";

		String SQL = "select * from flat_table_caption where screen='" + screenName + "' " + " and lang='" + gData.lang
				+ "'";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out = rec.get("text");

				}
			}
		}

		return out;
	}

	protected List<TblField> readTableMetadata(String screenName) {

		List<TblField> out = new ArrayList();

		String SQL = "";

		SQL += "SELECT S1.*, \n";
		SQL += "IF(b.colLabel IS NULL,S1.colName,b.colLabel) AS Label \n";
		SQL += "FROM \n";
		SQL += "(SELECT * FROM data_struct  \n";
		SQL += "WHERE screen = '" + screenName + "'  AND `SHOW`='X') S1 \n";
		SQL += "LEFT OUTER JOIN data_struct_text b \n";
		SQL += "ON S1.tableName = b.tableName AND S1.colName = b.colName \n";
		SQL += "ORDER BY S1.id; \n";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					TblField tf = new TblField();
					tf.fieldName = rec.get("colName");
					tf.fieldType = rec.get("colType");
					tf.fieldWidth = Integer.valueOf(rec.get("colLength"));

					tf.isChangeable = rec.get("editable").equals("Y") ? true : false;
					tf.refTable = rec.get("refTableName");
					tf.refKeyField = rec.get("refKeyColName");
					tf.refValueField = rec.get("refColValue");
					tf.refKeyFieldType = rec.get("refKeyColType");

					out.add(tf);
				}

			}
		}

		return out;

	}

	protected String getTableHeader(List<TblField> fields) {
		String out = "";
		out += "[";

		for (TblField tf : fields) {
			out += "{";
			out += "\"name\":\"" + tf.fieldName + "\",";
			out += "\"type\":\"" + tf.fieldType + "\",";
			out += "\"table\":\"" + tf.refTable + "\",";
			out += "\"key\":\"" + tf.refKeyField + "\",";
			out += "\"key_type\":\"" + tf.refKeyFieldType + "\",";
			out += "\"value\":\"" + tf.refValueField + "\",";
			out += "\"is_changeable\":\"" + (tf.isChangeable ? "y" : "n") + "\",";
			out += "\"width\":\"" + tf.fieldWidth + "\"";
			out += "},";
		}
		out = out.substring(0, out.length() - 1);
		out += "]";
		return out;
	}

	protected String readTableData(String SQL, List<TblField> fields) {

		String out = "";
		String buffer = "";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		out += "[";

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					out += "[";

					for (TblField field : fields) {

						buffer = rec.get(field.fieldName);
						buffer = (buffer == null) ? ""
								: buffer.replaceAll("[^A-Za-z0-9_. :/\\-<>?=&@\\p{IsCyrillic}]", "");

						out += "\"" + buffer + "\",";
					}

					out += "\"A\","; // скрытый столбец статуста строки

					out = out.substring(0, out.length() - 1);
					out += "],";
				}
				out = out.substring(0, out.length() - 1);

			}
		}

		out += "]";

		return out;

	}

	protected void setDefaultValueForField(List<TblField> fields, String fieldName, String fieldValue) {

		for (TblField tf : fields) {

			if (tf.fieldName.equals(fieldName)) {
				tf.defaultValue = fieldValue;
			}
		}

	}

	protected String getTableRowTemplate(List<TblField> fields) {
		String out = "";
		String buffer = "";

		out += "[";

		for (TblField tf : fields) {

			buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : "";

			switch (tf.fieldType) {
			case "int":
				buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : "0";
				out += "\"" + buffer + "\",";
				break;
			case "string":
				buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : "";
				out += "\"" + tf.defaultValue + "\",";
				break;
			case "datetime":

				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				buffer = (tf.defaultValue != null && !tf.defaultValue.isEmpty()) ? tf.defaultValue : timeStamp;
				out += "\"" + buffer + "\",";
				break;

			default:
				out += "\"\",";

			}

		}
		out += "\"I\"";

//		out = out.substring(0, out.length() - 1);

		out += "]";
		return out;
	}

	protected String getTableToolsPanel() {
		String out = "";
		String style = "cursor: pointer;";
		out += "<img src=\"/img/save.png\" onclick=\"saveAll();\" title=\"save\" style=\"" + style + "\">";
		out += "&nbsp;<img  src=\"/img/add.png\" onclick=\"onAddNewRow();\" title=\"insert row\" style=\"" + style + "\">";
		return out;
	}

	protected String buildHtmlArea() {
		String out = "";
		String function = "";

		/// ************ контекстное меню для SHOW режима ************ ///////
		out += "<div class='menu' id='context_page_menu_show'>  \n";
		function = "onSwitchToEditMode";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
				+ gData.tr("Switch to EDIT mode") + "</div>  \n";
		out += "</div>  \n";

		/// ************ контекстное меню для Ключевого поля ************ ///////
		out += "<div class='menu' id='context_menu_key_field'>  \n";

		function = "onDeleteRow";
		out += "<div id='" + function + "' class='menu-item' onclick=\"" + function + "();\">" + gData.tr("Delete row")
				+ "</div>  \n";

		function = "onAddNewRow";
		out += "<div id='" + function + "' class='menu-item' onclick=\"" + function + "();\">" + gData.tr("Add new row")
				+ "</div>  \n";

		function = "saveAll";
		out += "<div id='" + function + "' class='menu-item' onclick=\"" + function + "();\">" + gData.tr("Save")
				+ "</div>  \n";
		out += "</div>  \n";

		out += "</div>  \n";

		out += " <input id='input_txt' type='text' class='input_ctrl' type='text' size='7' onchange='onInputChange();'> \n";
		out += " <select id='input_select' class='input_ctrl input_select' onchange='onSelectInputChange(this);'></select> \n";

//		out += " <input type='button' style='position:absolute;left:10px;top:600px' onclick='onClickButton();' value='ok'> \n";
		return out;
	}

	protected String buildJavascriptRefreshFunction(String SQL, String tableName, List<TblField> fields, String mode) {
		return buildJavascriptRefreshFunction(SQL, tableName, fields, mode, 80);
	}

	protected String buildJavascriptRefreshFunction(String SQL, String tableName, List<TblField> fields, String mode,
			int initTop) {
		String out = "";
		out += "<script>  \n";

		/// ************** глобальная область. *********************///

		out += "window.tableData =  " + readTableData(SQL, fields) + ";  \n";
		out += "window.tableHeader =  " + getTableHeader(fields) + ";  \n";
		out += "window.tableTemplate =  " + getTableRowTemplate(fields) + ";  \n";
		out += "window.tableSelect =  null;  \n";
		out += "window.tableLeft =  40;  \n";
		out += "window.tableTop =  " + initTop + ";  \n";
		out += "window.currLeft =  0;  \n";
		out += "window.currTop =  0;  \n";
		out += "window.currRow =  0;  \n";
		out += "window.currCol =  0;  \n";
		out += "window.tableName = '" + tableName + "';  \n";
		out += "window.leftKoeff =  17;  \n";
		out += "window.tableLineHeight =  17;  \n";
		out += "window.selectedElement =  null;  \n";
		out += "window.mouseoverElement =  null;  \n";
		out += "window.curzIndex =  null;  \n";
		out += "window.mode =  '" + mode + "';  \n";
		out += "window.mouseLeft =  0;  \n";
		out += "window.mouseTop =  0;  \n";
		out += "window.propertiesItem=null; \n";

		/// *********** обновление экрана ***************************////
		out += "function refreshAll(){	 \n";
		out += " removeElementsByClass('table_data'); \n";
		out += " window.currLeft = window.tableLeft; \n";
		out += " window.currTop = window.tableTop; \n";

		out += " var tableWidth = 0; \n";
		out += "	for (var i=0; i < window.tableHeader.length; i++) { \n";
		out += " 		tableWidth += window.tableHeader[i].width * window.leftKoeff; \n";
		out += "  	} \n";
		out += " 	createDivForToolsCell(tableWidth - 3);  \n";

		out += " 	window.currTop += (window.tableLineHeight) + 2; \n";
		out += " 	window.currLeft = window.tableLeft; \n";
		out += "for (var i=0; i <window.tableHeader.length; i++) { \n";
		out += " 	createDivForHeaderCell(i); \n";
		out += "    window.currLeft += window.tableHeader[i].width * window.leftKoeff; \n";
		out += "  	} \n";
		out += "for (var i=0; i< window.tableData.length; i++) { \n";
		out += " 	window.currTop += (window.tableLineHeight) + 2; \n";
		out += " 	window.currLeft = window.tableLeft; \n";

		out += " 	var line = window.tableData[i]; \n";
		out += " 	var status = line[line.length-1]; \n";

		out += "	if(status!=='D') { \n";

		out += "		for (var j=0; j <window.tableHeader.length; j++) { \n";
		out += "			createDivForCell(i,j); \n";
		out += " 			window.currLeft += window.tableHeader[j].width * window.leftKoeff; \n";
		out += "  		} \n";
		out += "  	} \n";

		out += "  } \n";

		/// *********** события для всего документа ***************************////
		out += "document.addEventListener('contextmenu', onContextMenuPage, false); \n";
		out += "document.addEventListener('click', onClickDocument, false); \n";

		out += "} \n";

		out += "document.addEventListener('DOMContentLoaded', function(){ \n";
		out += "	refreshAll(); \n";
		out += "  }); \n";

		out += "</script>";

		return out;
	}

	protected String buildJavascriptAdditionalFunctionsArea() {
		String out = "";

		out += "<script>  \n";
		out += "function buildSelectList(cell){ \n";
		out += "var row=cell.getAttribute('row'); \n";
		out += "var col=cell.getAttribute('col'); \n";
		out += "var curKey=cell.innerHTML; \n";
		out += "window.selectedElement =  cell;  \n";
		out += "var refTable = tableHeader[col].table; \n";
		out += "var refKey = tableHeader[col].key; \n";
		out += "var refKeyType = tableHeader[col].key_type; \n";
		out += "var refValue = tableHeader[col].value; \n";
		out += "var params = '?refTable=' + refTable; \n";
		out += " params += '&refKey=' + refKey; \n";
		out += " params += '&refKeyType=' + refKeyType; \n";
		out += " params += '&refValue=' + refValue; \n";
		out += "var resp = getRequest('/json_sql'+params); \n";

		out += "console.log('params=' +params);  \n";
		out += "console.log('json=' +resp);  \n";

		out += "var el=document.getElementById('input_select'); \n";
		out += "document.body.appendChild(el); \n";
		out += "el.options.length = 0; \n";

		out += "var objJSON = JSON.parse(resp);  \n";

		out += "for (var i = 0; i < objJSON.length; ++i) {  \n";
		out += "	var line = objJSON[i];  \n";
		out += "	var opt = document.createElement('option');	 \n";
		out += "	opt.value = line.key; \n";
		out += "	opt.text = line.value; \n";
		out += "	el.appendChild(opt); \n";
		out += "}  \n";
		out += "	el.style.position = 'absolute'; \n";
		out += "	el.style.left = cell.getBoundingClientRect().left + 'px'; \n";
		out += "	el.style.top =  cell.getBoundingClientRect().top + 'px'; \n";
		out += "	el.style.display = 'block';  \n";
		out += "	for ( var i=0; i < el.length; i++ ) { \n";
		out += " 		if (el[i].value === curKey)	el[i].selected = true; \n";
		out += "	} \n";
		out += "} \n";

		out += "function onSelectInputChange(){ \n";
		out += " var cell=window.selectedElement;  \n";
		out += " cell.innerHTML =  document.getElementById('input_select').value;  \n";
		out += " var row=cell.getAttribute('row'); \n";
		out += " var col=cell.getAttribute('col'); \n";
		out += " var status = window.tableData[row][(window.tableHeader.length)]; \n";

		out += "	window.tableData[row][col] = document.getElementById('input_select').value; \n";
		out += " if (status==='A') { \n";
		out += "	window.tableData[row][(window.tableHeader.length)] = 'U'; \n";

		out += " } \n";

		out += " document.getElementById('input_select').style.display = 'none'; \n";
		out += " refreshAll();";
		out += "} \n";

		out += "function createDivForToolsCell(tableWidth){ \n";
		out += " var el = document.createElement('div'); \n";
		out += " el.className = 'table_data table_tools_panel'; \n";

		out += "if (window.mode === 'EDIT'){ \n";
		out += " el.innerHTML = '" + getTableToolsPanel() + "'; \n";
		out += "} \n";

		out += " el.style.position = 'absolute'; \n";
		out += " el.style.left = window.currLeft + 'px'; \n";
		out += " el.style.top =  window.currTop + 'px'; \n";
		out += " el.style.height = (window.tableLineHeight -2 )  + 'px'; \n";
		out += " el.style.width = tableWidth + 'px'; \n";
		out += " 	document.body.appendChild(el); \n";
		out += "} \n";

		out += "function createDivForHeaderCell(i){ \n";
		out += " var el = document.createElement('div'); \n";
		out += " el.className = 'table_data table_data_header'; \n";
		out += " el.innerHTML = window.tableHeader[i].name; \n";
		out += " el.style.position = 'absolute'; \n";
		out += " el.style.left = window.currLeft + 'px'; \n";
		out += " el.style.top =  window.currTop + 'px'; \n";
		out += " el.style.height = (window.tableLineHeight -2 )  + 'px'; \n";
		out += " el.style.width = ((window.tableHeader[i].width * window.leftKoeff) - 3) + 'px'; \n";
		out += " 	document.body.appendChild(el); \n";
		out += "} \n";

		out += "function createDivForCell(i,j){ \n";
		out += " var el = document.createElement('div'); \n";
		out += " el.className = 'table_data table_data_cell'; \n";
		out += " el.innerHTML = window.tableData[i][j]; \n";
		out += " el.style.position = 'absolute'; \n";
		out += " el.style.left = window.currLeft + 'px'; \n";
		out += " el.style.top =  window.currTop + 'px'; \n";
		out += " el.style.height = (window.tableLineHeight - 1 )+ 'px'; \n";
		out += " el.style.width = ((window.tableHeader[j].width * window.leftKoeff) - 3) + 'px'; \n";
		out += " el.setAttribute('row',i); \n";
		out += " el.setAttribute('col',j); \n";

		out += "	if(j == 0) { \n"; // ключевой столбец

		out += "		el.addEventListener('contextmenu', onContextMenuKeyField, false); \n";
		out += "		el.addEventListener('dblclick', onDblClickCell); \n";
		out += "		el.addEventListener('click', onClickCell); \n";

		out += "	} else { \n";

		out += "		el.addEventListener('dblclick', onDblClickCell); \n";
		out += "		el.addEventListener('click', onClickCell); \n";

		out += "	} \n";

		out += " 	document.body.appendChild(el); \n";
		out += "} \n";

		out += "function onContextMenuKeyField(event) { \n";
		out += "if (window.mode === 'READONLY') return; \n";
		out += "if (window.mode === 'SHOW') return; \n";
		out += " window.selectedElement =  event.target;  \n";
		out += " window.mouseLeft = event.clientX + 'px';	 \n";
		out += " window.mouseTop = event.clientY + 'px';	 \n";
		out += " contextMenuBox = window.document.getElementById('context_menu_key_field');  \n";
		out += " contextMenuBox.style.left = event.clientX + 'px';	 \n";
		out += " contextMenuBox.style.top = event.clientY  + 'px';		 \n";
		out += " contextMenuBox.style.display = 'block';	 \n";
		out += "    event.preventDefault();   \n";
		out += "    event.stopPropagation();   \n";
		out += "} \n";

		out += "function onDblClickCell(event){ \n";
		out += "	var cell = event.target ;  \n";

		out += "	window.currRow = cell.getAttribute('row');  \n";
		out += "	window.currCol =  cell.getAttribute('col');  \n";
		out += "	if(window.tableHeader[window.currCol].is_changeable!=='y'){  \n";
		out += "		return; \n";
		out += "	} \n";

		out += " if(window.tableHeader[window.currCol].table.length<2){   \n";
		out += " 	box = window.document.getElementById('input_txt');  \n";
		out += " 	box.value = event.target.innerHTML ;	 \n";
		out += " 	rect = event.target.getBoundingClientRect();  \n";
		out += " 	box.style.left = (rect.left + window.pageXOffset) + 'px';	 \n";
		out += " 	box.style.top = (rect.top  + window.pageYOffset) + 'px';		 \n";
		out += " 	box.style.height = ( rect.bottom - rect.top - 4)   + 'px'; \n";
		out += " 	box.style.width = ( rect.right - rect.left - 8 )  + 'px'; \n";
		out += " 	box.style.display = 'block';	 \n";
		out += " 	box.style.zIndex = 10;	 \n";
		out += "} else { \n";

		out += "	console.log('cell=' + cell.innerHTML); \n";
		out += "	buildSelectList(cell); \n";
		out += "    	event.preventDefault();   \n";
		out += "    	event.stopPropagation();   \n";
		out += "} \n";

		out += "} \n";

		out += "function onClickCell(event){ \n";
		out += " 	document.getElementById('input_txt').style.display = 'none';  \n";
		out += "	document.getElementById('input_select').style.display = 'none'; \n";

		out += "} \n";

		out += "function onInputChange(){ \n";
		out += " box = window.document.getElementById('input_txt');  \n";
		out += " line = window.tableData[window.currRow];  \n";
		out += " window.tableData[window.currRow][window.currCol] = box.value;   \n";
		out += " var status = line[line.length-1];   \n";
		out += " if(status==='A'){   \n";
		out += " 	line[line.length-1] = 'U';   \n";
		out += " }   \n";

		out += " box.style.display = 'none';	 \n";
		out += " 	refreshAll();   \n";
		out += "} \n";

		out += "function saveAll(){	 \n";
		out += " var strSQL = ''; \n";
		out += "for (var i=0; i <window.tableData.length; i++) { \n";
		out += "var line= window.tableData[i]; \n";
		out += "var status= line[line.length-1]; \n";
		out += "if(status === 'U') { \n"; ////////// Update /////////
		out += "strSQL += 'update ' + window.tableName + ' set '; \n";
		out += "	for (var j=0; j < line.length-1; j++) { \n";
		out += "		if(window.tableHeader[j].is_changeable === 'y') { \n";
		out += "			if(window.tableHeader[j].type === 'string') { \n";
		out += "				strSQL += '\\`' + window.tableHeader[j].name + '\\`=\"' + line[j] + '\",'; \n";
		out += "			} else { \n";
		out += "				strSQL += '\\`' + window.tableHeader[j].name + '\\`=' + line[j] + ','; \n";
		out += "			} \n";
		out += "		} \n";
		out += "	} \n";
		out += "	strSQL = strSQL.slice(0, -1); \n";
		out += "	if(window.tableHeader[0].type === 'string') { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=\"' + line[0] + '\"'; \n";
		out += " 	} else { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=' + line[0] + ''; \n";
		out += " 	} \n";
		out += "	strSQL += ';'; \n";
		out += " } else if (status === 'D') {\n"; ////// Delete
		out += "	strSQL += 'delete from ' + window.tableName ; \n";
		out += "	if(window.tableHeader[0].type === 'string') { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=\"' + line[0] + '\"'; \n";
		out += " 	} else { \n";
		out += "	 	strSQL += ' where \\`' + window.tableHeader[0].name + '\\`=' + line[0] + ''; \n";
		out += " 	} \n";
		out += "	strSQL += ';'; \n";
		out += " } else if (status === 'I') {\n"; ////// Insert
		out += "	strSQL += 'insert into ' + window.tableName ; \n";
		out += "	var fieldList =''; \n";
		out += "	var valueList =''; \n";
		out += "	for (var j=0; j < line.length-1; j++) { \n";
		out += "		if(window.tableHeader[j].is_changeable === 'y') { \n";
		out += "			fieldList +='\\`' + window.tableHeader[j].name + '\\`,'; \n";
		out += "			if(window.tableHeader[j].type === 'string') { \n";
		out += "				valueList += '\"' +  line[j] + '\",'; \n";
		out += " 			} else { \n";
		out += "				valueList +=  line[j] + ','; \n";
		out += " 			} \n";
		out += " 		} \n";
		out += " } \n";
		out += " fieldList = fieldList.slice(0, -1); \n";
		out += " valueList = valueList.slice(0, -1); \n";
		out += "	strSQL += ' (' + fieldList + ') values (' + valueList +')' ; \n";
		out += "	strSQL += ';'; \n";
		out += "} \n";
		out += "} \n";
		out += "strSQL = strSQL.slice(0, -1); \n";
		out += "console.log(strSQL); \n";
		out += " postRequest('/sql_save', strSQL); \n";
		out += " console.log('sql_save executed'); \n";
		out += " sleep(1000); \n";
		out += " location.reload(); \n";
		out += " return false; \n";
		out += "} \n";


		out += "function sleep(delay) { \n";
		out += " var start = new Date().getTime(); \n";
		out += " while (new Date().getTime() < start + delay); \n";
		out += "} \n";


		out += "function onDeleteRow() { \n";
		out += "if(!window.selectedElement) { \n";
		out += "	alert('double click on row for delete before');  \n";
		out += " return; \n";
		out += "} \n";
		out += " for(var i=0; i < tableData.length; i++) { \n";
		out += "    var line =  tableData[i]; \n";
		out += "	if(line[0]===window.selectedElement.innerHTML) { \n";
		out += "      		line[line.length-1] = 'D' \n";
		out += "  	}	\n";
		out += "   }	\n";
		out += "	refreshAll();  \n";
		out += "  }	\n";

		out += "function onAddNewRow() { \n";
		out += " var newItem = Object.create(window.tableTemplate); \n";
		out += " for(var i=0; i<window.tableTemplate.length; i++){ \n";
		out += "   newItem[i]=window.tableTemplate[i]; \n";
		out += "  }	\n";
		out += " window.tableData.push(newItem); \n";
		out += "  console.log('onAddNewRow' + newItem[newItem.lenght-1]); \n";
		out += "	refreshAll();  \n";
		out += "  }	\n";

		out += "function onSwitchToEditMode() { \n";
		out += "if (window.mode === 'READONLY') return; \n";
		out += "	window.mode =  'EDIT';  \n";
		out += "	refreshAll();  \n";
		out += "} \n";

		out += "function onContextMenuPage(event) { \n";
		out += "if (document.addEventListener) { \n";
		out += " if (window.mode=='EDIT') return; \n";
		out += " 	contextMenuBoxPage = window.document.getElementById('context_page_menu_show');  \n";
		out += " 	contextMenuBoxPage.style.left = event.clientX + 'px';	 \n";
		out += " 	contextMenuBoxPage.style.top = event.clientY  + 'px';		 \n";
		out += " 	contextMenuBoxPage.style.display = 'block';	 \n";
		out += "    event.preventDefault();   \n";
		out += "} else {    \n";
		out += "	document.attachEvent('oncontextmenu', function() {    \n";
		out += "	window.event.returnValue = false;    \n";
		out += "});    \n";
		out += "} \n";
		out += "} \n";

		out += "function onClickDocument(event) { \n";
		out += " document.getElementById('context_page_menu_show').style.display = 'none'; \n";
		out += " document.getElementById('context_menu_key_field').style.display = 'none'; \n";
		out += "} \n";

		out += "function postRequest(url, text, callback) { \n";
		out += " var xhr = new XMLHttpRequest(); \n";
		out += " xhr.open('POST', url, true); \n";
		out += " xhr.responseType = 'text' \n";
		out += " xhr.setRequestHeader('Content-Type', 'text/plain'); \n";
		out += " xhr.onload = function(e) {  \n";
		out += " if (this.status == 200) { \n";
		out += "     console.log(this.responseText); \n";
		out += " if(callback) callback(statuses); \n";
		out += "  }	\n";
		out += " };	 \n";
		out += " xhr.send(text);  \n";
		out += "} \n";

		out += "function getRequest(url, callback) { \n";
		out += " var xhr = new XMLHttpRequest(); \n";
		out += " xhr.open('GET', url, false); \n";
		out += " xhr.send();  \n";

		out += " xhr.onreadystatechange = function() {  \n";
		out += " 	if (this.readyState == 4 && this.status == 200) { \n";
		out += "   		var response = this.responseText; \n";
		out += " 		if(callback) callback(response); \n";
		out += "  	}	\n";
		out += " };	 \n";
		out += " return xhr.responseText;	 \n";
		out += "} \n";

		out += "</script>\n";
		return out;
	}



	public String readFrom_sql_text(String className, String filter) {
		String out = "";
		String SQL = "";

		SQL += "select `sql_text` from sql_text where `class` = '" + className + "'";
		SQL += " and `filter`='" + filter + "' order by id";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += " " +rec.get("sql_text");

				}
			}
		}

		return out;
	}

	public String readSQLfrom_flat_table_sql(String screenName, String tableName) {
		String out = "";
		String SQL = "";

		SQL += "select `sql_text` from flat_table_sql where `screen` = '" + screenName + "'";
		SQL += " and `table`='" + tableName + "' order by id";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += rec.get("sql_text") + " ";

				}
			}
		}

		return out;
	}

	public String readOneValue(String SQL) {
		String out = "";
		String[] parts = SQL.split("\\s+");
		String fieldName = parts[1];
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {
				for (Map<String, String> rec : records_list) {
					out = rec.get(fieldName);
				}
			}
		}
		return out;
	}

	protected List<TblField> readFrom_flat_table_fields(String screenName) {

		List<TblField> out = new ArrayList();

		String SQL = "";

		SQL += "SELECT S1.*,  \n";
		SQL += "S1.colName AS Label  \n";
		SQL += "FROM  \n";
		SQL += "(SELECT * FROM flat_table_fields   \n";
		SQL += "WHERE    \n";
		SQL += "`screen` = '" + screenName + "'   \n";
		SQL += "AND `SHOW`='X') S1   \n";
		SQL += "ORDER BY S1.id;   \n";


		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					TblField tf = new TblField();
					tf.fieldName = rec.get("colName");
					tf.fieldType = rec.get("colType");
					tf.fieldWidth = Integer.valueOf(rec.get("colLength"));

					tf.isChangeable = rec.get("editable").equals("Y") ? true : false;
					tf.refTable = rec.get("refTableName");
					tf.refKeyField = rec.get("refKeyColName");
					tf.refValueField = rec.get("refColValue");
					tf.refKeyFieldType = rec.get("refKeyColType");

					out.add(tf);
				}

			}
		}

		return out;

	}

	protected List<TblField> readMetadatafromDB(String className, String screenName) {

		List<TblField> out = new ArrayList();

		String SQL = "";

		SQL += "SELECT S1.*, \n";
		SQL += "IF(b.colLabel IS NULL,S1.colName,b.colLabel) AS Label \n";
		SQL += "FROM \n";
		SQL += "(SELECT * FROM data_struct  \n";
		SQL += "WHERE  \n";
		SQL += "`screen` = '" + className + "'  ";
		SQL += "AND `tableName` = '" + screenName + "'  ";
		SQL += "AND `SHOW`='X') S1 \n";
		SQL += "LEFT OUTER JOIN data_struct_text b \n";
		SQL += "ON S1.tableName = b.tableName AND S1.colName = b.colName \n";
		SQL += "ORDER BY S1.id; \n";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					TblField tf = new TblField();
					tf.fieldName = rec.get("colName");
					tf.fieldType = rec.get("colType");
					tf.fieldWidth = Integer.valueOf(rec.get("colLength"));

					tf.isChangeable = rec.get("editable").equals("Y") ? true : false;
					tf.refTable = rec.get("refTableName");
					tf.refKeyField = rec.get("refKeyColName");
					tf.refValueField = rec.get("refColValue");
					tf.refKeyFieldType = rec.get("refKeyColType");

					out.add(tf);
				}

			}
		}

		return out;

	}

	public String cleanInvalidCharacters(String in) {
		StringBuilder out = new StringBuilder();
		char current;
		if (in == null || ("".equals(in))) {
			return "";
		}
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF))) {
				out.append(current);
			}

		}
		return out.toString().replaceAll("\\s", " ");
	}


	public String readFromTable_options_map(String screen) {
		String out = "";
		String SQL = "";
		SQL = "SELECT * FROM options_map WHERE screen='" + screen + "' AND lang='" + gData.lang + "' ";
		SQL += "AND filter = 'all' order by id";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		out += "<ol class='navigation1'>";
		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += "<li><a href='/" + rec.get("link");
					out += "'>" + rec.get("label") + "</a>";

				}
			}
		}
		out += "</ol>";
		return out;
	}

	public String readFromTable_options_map(String screen, PhisObjProperties pr) {
		String out = "";
		String SQL = "";
		String ObjectKey = "",buffer = "";
		
		switch (pr.obj_typ) {
		case "servers":
			ObjectKey = "servers_" + pr.OS.toLowerCase();
			break;
			
		case "db_systems":
			ObjectKey = "db_systems_" + pr.DB.toLowerCase();			
			break;
			
		case "app_systems":
			ObjectKey = "app_systems_" + pr.APP.toLowerCase();			
			break;
		
		default:
			break;
		
		}
		
		SQL = "SELECT * FROM options_map WHERE screen='" + screen + "' AND lang='" + gData.lang + "' ";
		SQL += "AND filter in('all','" + pr.obj_typ + "','" + ObjectKey +  "') order by id";
		
		
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		out += "<ol class='navigation1'>";
		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					buffer = rec.get("link");
					String symbol = (buffer.contains("=")) ? "&" : "?";

					out += "<li><a href='/" + buffer + symbol + "guid=" + pr.physGuid;
					out += "&obj_typ=" + pr.typeObj;
					out += "'>" + rec.get("label") + "</a>";

				}
			}
		}
		out += "</ol>";
		return out;
	}

	protected ConnectionData readConnectionParameters(PhisObjProperties pr) {
		ConnectionData out = new ConnectionData();
		String SQL = "select * from monitor_conn_data where object_guid='" + pr.physGuid + "'";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
		if (records_list != null) {
			if (records_list.size() > 0) {
				for (Map<String, String> rec : records_list) {
					out.user = rec.get("user");
					out.hash = rec.get("hash");
					out.conn_type = rec.get("conn_type");
					out.clnt = rec.get("clnt");
					out.password = gData.getPasswordFromHash(out.hash);
				}
			}
		}

		switch (pr.obj_typ) {
		case "servers":
			out.ip = readOneValue("SELECT def_ip FROM servers WHERE guid='" + pr.physGuid + "'");

			break;
		case "db_systems":

			out.ip = readOneValue("SELECT def_ip FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.port = readOneValue("SELECT port FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.sid = readOneValue("SELECT sid FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.sysnr = readOneValue("SELECT sysnr FROM db_systems WHERE guid='" + pr.physGuid + "'");
			out.dbType = readOneValue("SELECT db_type FROM db_systems WHERE guid='" + pr.physGuid + "'");

			break;
		case "app_systems":

			break;

		}

		return out;
	}

	public String readTextFile(String fileName) {
		String out = "";

		try {
			out = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		return out;
	}
public String readCaptionOfPage(String className) {
	String out = "";
	String SQL = "select * from `pages_captions` where className='" + className + "'";
	SQL += " and  lang ='" + gData.lang + "'";
	
	List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);
			for (Map<String, String> rec : records_list) {
				out = rec.get("caption_text");
	}
	return out;
}
public String readFromTable_caption(String className, String filter) {
	String out = "";
	String SQL = "";
	SQL = "SELECT * FROM caption WHERE className='" + className + "' AND lang='" + gData.lang + "' ";
	SQL += "AND filter ='" + filter + "' order by id";
	List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

	for (Map<String, String> rec : records_list) {
		out = rec.get("text");
	}
	return out;
}
protected String readFromTable_sql_remote_check(String className, String job_name, String filter) {
	String out = "";
	String SQL = "";
	
	SQL += "SELECT * FROM sql_remote_check WHERE class='" + className + "'";
	SQL += " and job_name='" + job_name + "'";		
	if(!filter.isEmpty()) {
		SQL+= " and filter='" +filter + "'";
	}
	SQL += " order by id";
	
	List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

	if (records_list != null) {
		if (records_list.size() > 0) {

			for (Map<String, String> rec : records_list) {

				out += rec.get("sql_text") + " ";

			}
		}
	}

	return out;
}
protected String readFromTable_ssh_remote_check(String className, String job_name, String filter) {
	String out = "";
	String SQL = "";
	
	SQL += "SELECT * FROM ssh_remote_check WHERE className='" + className + "'";
	SQL += " and job_name='" + job_name + "'";		
	if(!filter.isEmpty()) {
		SQL+= " and os ='" +filter + "'";
	}
	SQL += " order by id";
	
	List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

	if (records_list != null) {
		if (records_list.size() > 0) {

			for (Map<String, String> rec : records_list) {

				out += rec.get("sshText") + " ";

			}
		}
	}

	return out;
}
}
