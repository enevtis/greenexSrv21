package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.GraphJsObject;

public class SystemImageEditHandler2 extends HandlerTemplate {

	String test = "";

	public List<GraphJsObject> jsObjects = new ArrayList<GraphJsObject>();
	public String caption = "";
	public int offsetTop = 30;
	public int offsetLeft = 10;
	private String[] etalon = { "guid", "class", "subclass", "label", "bgcolor", "borderColor", "lineWidth", "left",
			"top", "width", "height", "level", "link" ,"physGuid"};
	private String bgColor = "#EEEEEE";

	public SystemImageEditHandler2(globalData gData) {
		super(gData);

	}

	@Override
	public void getResponse(SSLSocket socket, String paramsString) {

		test = paramsString;

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

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";

		getParametersForGraphPage();

		out += getBeginPage();
		out += strTopPanel(caption);

		out += buildJavascriptRefreshFunction();
		out += buildJavascriptAdditionalFunctionsArea();
		out += buildHtmlArea();

		out += getEndPage();

		return out;

	}

	protected String buildJavascriptRefreshFunction() {
		String out = "";
		out += "<script>  \n";

		/// ************** глобальная область. *********************///

		out += "window.pageGuid =  '" + params.get("page") + "';  \n";
		out += "window.graphObj =  [" + readGrapObjectsFromDataBase() + "];  \n";
		out += "window.selectedElement =  null;  \n";
		out += "window.mouseoverElement =  null;  \n";
		out += "window.curzIndex =  null;  \n";
		out += "window.mode =  'SHOW';  \n";
		out += "window.mouseLeft =  0;  \n";
		out += "window.mouseTop =  0;  \n";
		out += "window.propertiesItem=null; \n";

		/// *********** обновление экрана ***************************////
		out += "function refreshAll(){	 \n";
		out += " removeElementsByClass('graph_obj'); \n";

		out += "for (var i=0; i < window.graphObj.length; i++) { \n";
		out += "	createDivFromGraphObject(window.graphObj[i]); \n";
		out += "  } \n";

		out += readGraphLines();

		/// *********** события для всего документа ***************************////
		out += "document.addEventListener('contextmenu', onContextMenuPage, false); \n";
		out += "document.addEventListener('click', onClickDocument, false); \n";

		out += "} \n";

		/// *********** построение отдельного элемента DIV из члена массива на экране
		/// ***************************////

		out += "function createDivFromGraphObject(curObj){ \n";
		out += " var el = document.createElement('div'); \n";
		out += " el.id = curObj.guid; \n";
		out += " el.className = 'graph_obj '+ curObj.class; \n";
		out += " el.innerHTML = curObj.label; \n";
		out += " el.style.position = 'absolute'; \n";
		out += " el.style.left = curObj.left + 'px'; \n";
		out += " el.style.top = curObj.top + 'px'; \n";
		out += " el.style.height = curObj.height + 'px'; \n";
		out += " el.style.width = curObj.width + 'px'; \n";
		out += " el.style.backgroundColor = curObj.bgcolor; \n";
		out += " el.style.zIndex = curObj.level; \n";

		out += "switch (curObj.class) { \n";
		out += "	case 'group':  \n";
		out += " 	break;  \n";
		out += "	case 'server':  \n";
		out += "	case 'db_system':  \n";
		out += "	case 'app_system':  \n";

		out += " 		var imagePath = '/img/' + curObj.subclass + '.png'; \n";
		out += "        el.style.backgroundColor = 'transparent'; \n";
		out += "        el.style.opacity = '1'; \n";
		out += " 		el.style.backgroundImage = \"url('\" + imagePath + \"')\"; \n";
		out += " 		el.style.backgroundRepeat = 'no-repeat'; \n";

		out += " 	break;  \n";
		out += "	default:  \n";
		out += " 		el.level = 0; \n";
		out += " 	break;  \n";
		out += "} \n";

		/// ******** добавляем события для DIV для режима EDIT (кроме групп)
		/// ****************////

		out += " if (window.mode ===  'EDIT') { \n";
		out += "	el.addEventListener('dblclick', onDblClickDev); \n";
		out += "	el.addEventListener('mouseover', onMouseOverDiv); \n";
		out += "	el.addEventListener('contextmenu', onContextMenuDev, false); \n";
		out += "	el.addEventListener('dragstart', onDragStartDev, false); \n";
		out += " } else {  \n";

		out += "	if (curObj.class !==  'group') { \n";
		out += "		el.style.cursor = 'pointer'; \n";
		out += "		el.addEventListener('click', onClickDev); \n";
		out += "	} \n";

		out += " } \n";

		out += " 	document.body.appendChild(el); \n";
		out += "} \n";

		out += "function removeElementsByClass(className){ \n";
		out += "	var elements = document.getElementsByClassName(className); \n";
		out += "		while(elements.length > 0){ \n";
		out += "		elements[0].parentNode.removeChild(elements[0]); \n";
		out += "	} \n";
		out += "} \n";

		/// ******** добавить новый объект и перерисовать объекта заданной структуры
		/// ****************////

		out += "function addNewGraphObject(classObject, subclassObject, menu){ \n";

		out += " var obj = Object.create(getEmptyObject()); \n";

		out += " obj.guid = getRequest('/free_request?action=get_guid'); \n";
		out += " obj.class = classObject; \n";
		out += " obj.subclass = subclassObject; \n";
		out += " obj.label = 'New ' + classObject; \n";

		out += " var rect = document.getElementById(menu.id).getBoundingClientRect(); \n";
		out += " if (rect) {  \n";
		out += " 	obj.left = rect.x ; \n";
		out += " 	obj.top = rect.y; \n";
		out += " }  \n";

		out += " obj.bgcolor = '" + this.bgColor + "'; \n";
		out += " obj.level = 90; \n";

		out += "var typeObj= classObject + subclassObject;\n";

		out += "switch (typeObj) { \n";
		out += "	case 'group':  \n";
		out += " 		obj.height = '400'; \n";
		out += " 		obj.width = '450'; \n";
		out += " 		obj.level = -5; \n";
		out += " 	break;  \n";
		out += "	case 'serverserver1':  \n";
		out += " 		obj.height = '70'; \n";
		out += " 		obj.width = '90'; \n";
		out += " 		obj.level = 1; \n";
		out += " 	break;  \n";
		out += "	case 'serverserver2':  \n";
		out += " 		obj.height = '200'; \n";
		out += " 		obj.width = '72'; \n";
		out += " 		obj.level = 1; \n";
		out += " 	break;  \n";
		out += "	case 'db_systemsap_hana':  \n";
		out += " 		obj.height = '65'; \n";
		out += " 		obj.width = '106'; \n";
		out += " 		obj.level = 1; \n";
		out += " 	break;  \n";
		out += "	case 'db_systemoracle_db':  \n";
		out += " 		obj.height = '50'; \n";
		out += " 		obj.width = '89'; \n";
		out += " 		obj.level = 1; \n";
		out += " 	break;  \n";

		out += "	case 'app_systemsap_nw':  \n";
		out += " 		obj.height = '48'; \n";
		out += " 		obj.width = '50'; \n";
		out += " 		obj.level = 1; \n";
		out += " 	break;  \n";

		out += "	case 'app_systemhttp':  \n";
		out += " 		obj.height = '50'; \n";
		out += " 		obj.width = '150'; \n";
		out += " 		obj.level = 1; \n";

		out += " 	break;  \n";

		out += "	default:  \n";
		out += " 		obj.level = 0; \n";
		out += " 	break;  \n";
		out += "} \n";
		out += " window.graphObj.push(obj); \n";
		out += " refreshAll(); \n";
		out += "} \n";

		/// ************ установка окончательных событий ************ ///////
		out += "document.addEventListener('DOMContentLoaded', function(){ \n";
		out += "refreshAll(); \n";
		out += "  }); \n";

		out += "</script>  \n";

		return out;
	}

	@Override
	protected String buildJavascriptAdditionalFunctionsArea() {
		String out = "";
		out += "<script>  \n";

		out += "function onSwitchToEditMode() { \n";
		out += "	window.mode =  'EDIT';  \n";
		out += "	refreshAll();  \n";
		out += "} \n";

		out += "function onClosePropertiesBox() { \n";
		out += "	 box = document.getElementById('properties');	  \n";
		out += " if (box) { \n";
		out += " 	 box.style.display = 'none'; \n";
		out += "	} \n";
		out += "} \n";

		out += "function onDeleteDiv(obj) { \n";
		out += " var key=window.mouseoverElement.id; \n";
		out += " if(key) { \n";
		out += " console.log('key=',key); \n";
		out += "  	window.graphObj = window.graphObj.filter(function(e) { return e.guid !== key }); \n";
		out += " 	} \n";
		out += " refreshAll(); \n";
		out += "} \n";

		out += "function onShowProperties() { \n";
		out += "  var item = window.graphObj.find(item => item.guid === window.mouseoverElement.id); \n";
		out += "     window.propertiesItem=item; \n";
		out += "	 box = document.getElementById('properties');	  \n";
		out += " if (box) { \n";
		out += " 	 box.style.left = window.mouseLeft; \n";
		out += " 	 box.style.top = window.mouseTop; \n";
		out += " 	 box.style.display = 'block'; \n";
		out += "	 document.getElementById('prop_caption').innerHTML= '" + gData.tr("Properties for:")
				+ " <b>' + item.label + '</b>'; \n";
		out += "	 document.getElementById('prop_label').value= item.label;	  \n";
		out += "	 document.getElementById('prop_height').value= item.height;	  \n";
		out += "	 document.getElementById('prop_width').value= item.width;	  \n";
		out += "	 document.getElementById('prop_physguid').value= item.physGuid;	  \n";
		out += " } \n";
		out += "} \n";

		out += "function onOkPropertiesInForm(obj) { \n";

		out += " for (var i = 0, len = window.graphObj.length; i < len; i++) {  \n";
		out += "	if (window.graphObj[i].guid===window.propertiesItem.guid) { \n";
		out += "	 window.graphObj[i].label = document.getElementById('prop_label').value;	  \n";
		out += "	 window.graphObj[i].height = document.getElementById('prop_height').value;	  \n";
		out += "	 window.graphObj[i].width = document.getElementById('prop_width').value;	  \n";
		out += "	 window.graphObj[i].physGuid = document.getElementById('prop_physguid').value;	  \n";
		out += " 	} \n";
		out += " } \n";

		out += " box = document.getElementById('properties'); \n";
		out += " if (box) { \n";
		out += "	if (box.style.display === 'block') { \n";
		out += " 	box.style.display = 'none'; \n";
		out += "	} \n";
		out += " } \n";
		out += " refreshAll(); \n";
		out += "} \n";

		out += "function onClickDev(event) { \n";
		out += "if (window.mode === 'EDIT') return; \n";
		out += "   console.log(event.target.id); \n";
		out += "   window.open('/dashboard?guid='+ event.target.id,'_self'); \n";

		out += "} \n";

		out += "function onDblClickDev(event) { \n";
		out += "if (window.mode === 'SHOW') return; \n";
		out += "   console.log(event.target.id); \n";
		out += "	if (window.selectedElement) { \n";
		out += "		 window.selectedElement = null; \n";
		out += " 		 window.selectedElement.style.boxShadow = null; \n";
		out += "		 window.selectedElement.style.zIndex = window.curzIndex;  \n";
		out += "	}   \n";

		out += "  event.target.style.boxShadow = '0 0 5px #999999';   \n";
		out += "  window.selectedElement = event.target;   \n";
		out += "  window.curzIndex =  event.target.style.zIndex;  \n";
		out += "  event.target.addEventListener('mousedown', onMouseDownDiv);   \n";
		out += "  event.target.addEventListener('dblclick', onDblClickDev);  \n";
		out += "} \n";

		out += "function onContextMenuDev(event) { \n";
		out += "if (window.mode === 'SHOW') return; \n";
		out += " window.mouseLeft = event.clientX + 'px';	 \n";
		out += " window.mouseTop = event.clientY + 'px';	 \n";
		out += " contextMenuBox = window.document.getElementById('context_menu_div');  \n";
		out += " contextMenuBox.style.left = event.clientX + 'px';	 \n";
		out += " contextMenuBox.style.top = event.clientY  + 'px';		 \n";
		out += " contextMenuBox.style.display = 'block';	 \n";
		out += "    event.preventDefault();   \n";
		out += "    event.stopPropagation();   \n";
		out += "} \n";

		out += "function onMouseOverDiv(event) { \n";
		out += " 	window.mouseoverElement = event.target; \n";
		out += "} \n";

		out += "function onMouseDownDiv(event) { \n";
		out += " 	event.target.style.position = 'absolute';  \n";
		out += " 	event.target.style.zIndex = 500;  \n";
		out += " 	document.body.append(event.target);  \n";
		out += "  	event.target.addEventListener('mousemove', onMouseMoveDiv);   \n";
		out += "} \n";

		out += "function onMouseOverDiv(event) { \n";
		out += " 	window.mouseoverElement = event.target; \n";
		out += "} \n";

		out += "function onMouseMoveDiv(event) {  \n";
		out += "     event.target.style.left = ( event.pageX - 30 ) + 'px';  \n";
		out += "     event.target.style.top = ( event.pageY - 20 ) + 'px';  \n";
		out += "}  \n";

		out += "function onReleaseDiv(){ \n";
		out += " window.selectedElement.removeEventListener('mousedown', onMouseDownDiv);  \n";
		out += " window.selectedElement.removeEventListener('mousemove', onMouseMoveDiv);  \n";
		out += " window.selectedElement.style.zIndex = window.curzIndex;  \n";
		out += " window.selectedElement.style.boxShadow = null;  \n";
		out += " for (var i = 0; i < window.graphObj.length; i++) { \n";
		out += "	if (window.graphObj[i].guid === window.selectedElement.id) { \n";
		out += " 	 var rect = window.selectedElement.getBoundingClientRect(); \n";
		out += " 	 window.graphObj[i].left = rect.x; \n";
		out += " 	 window.graphObj[i].top = rect.y; \n";
		out += "	} \n";
		out += " } \n";
		out += "} \n";

		out += "function onDragStartDev(){ \n";
		out += " return false; \n";
		out += "} \n";

		out += "function getEmptyObject(){ \n";
		out += " var out; \n";
		out += " out = " + getEtalonObject() + "; \n";
		out += " return out; \n";
		out += "} \n";

		/// ************ контекстное меню всей страницы ************ ///////

		out += "function onContextMenuPage(event) { \n";
		out += "if (document.addEventListener) { \n";

		out += " if (window.mode=='SHOW') { \n";
		out += " 	contextMenuBoxPage = window.document.getElementById('context_page_menu_show');  \n";
		out += " } else { \n";
		out += " 	contextMenuBoxPage = window.document.getElementById('context_page_menu_edit');  \n";
		out += " } ; \n";

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
		out += " contextMenuBox = document.getElementById('context_menu_div'); \n";
		out += " if (contextMenuBox) { \n";
		out += "	if (contextMenuBox.style.display === 'block') { \n";
		out += " 	contextMenuBox.style.display = 'none'; \n";
		out += "	} \n";
		out += " } \n";
		out += " contextMenuBox = document.getElementById('context_page_menu_edit'); \n";
		out += " if (contextMenuBox) { \n";
		out += "	if (contextMenuBox.style.display === 'block') { \n";
		out += " 	contextMenuBox.style.display = 'none'; \n";
		out += "	} \n";
		out += " } \n";
		out += " contextMenuBox = document.getElementById('context_page_menu_show'); \n";
		out += " if (contextMenuBox) { \n";
		out += "	if (contextMenuBox.style.display === 'block') { \n";
		out += " 	contextMenuBox.style.display = 'none'; \n";
		out += "	} \n";
		out += " } \n";

		out += "} \n";

		/// ************ сохранить все ************ ///////

		out += "function onSaveAll(){	 \n";
		out += " var strJSObj = ''; \n";
		out += " var grObj = null; \n";
		out += " for (var i = 0, len = window.graphObj.length; i < len; i++) { \n";
		out += "	grObj = window.graphObj[i]; \n";
		out += "	strJSObj += 'guid::' + grObj.guid + ';;'; \n";
		out += "	strJSObj += 'class::' + grObj.class + ';;'; \n";
		out += "	strJSObj += 'subclass::' + grObj.subclass + ';;'; \n";
		out += "	strJSObj += 'physGuid::' + grObj.physGuid + ';;'; \n";		
		out += "	strJSObj += 'label::' + grObj.label + ';;'; \n";
		out += "	strJSObj += 'bgcolor::' + grObj.bgcolor + ';;'; \n";
		out += "	strJSObj += 'borderColor::' + grObj.borderColor + ';;'; \n";
		out += "	strJSObj += 'level::' + grObj.level + ';;'; \n";
		out += "	strJSObj += 'link::' + grObj.link + ';;'; \n";
		out += "	var rect = document.getElementById(grObj.guid).getBoundingClientRect(); \n";
		out += "	strJSObj += 'top::' + rect.top + ';;'; \n";
		out += "	strJSObj += 'left::' + rect.left + ';;'; \n";
		out += "	strJSObj += 'width::' + grObj.width + ';;'; \n";
		out += "	strJSObj += 'height::' + grObj.height + ';;'; \n";
		out += "	strJSObj += '!!'; \n";
		out += " } \n";
		out += " postRequest('/gr_page_save?page=' + window.pageGuid, strJSObj); \n";
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

		out += "function postRequest(url, text, callback) { \n";
		out += " var xhr = new XMLHttpRequest(); \n";
		out += " xhr.open('POST', url, true); \n";
		out += " xhr.responseType = 'text' \n";
		out += " xhr.onload = function(e) {  \n";
		out += " if (this.status == 200) { \n";
		out += "     console.log(this.responseText); \n";
		out += " if(callback) callback(statuses); \n";
		out += "  }	\n";
		out += " };	 \n";
		out += " xhr.send(text);  \n";
		out += "} \n";

		out += "</script>  \n";
		return out;
	}

	@Override
	protected String buildHtmlArea() {
		String out = "";
		String function = "";

		/// ************ контекстное меню для SHOW режима ************ ///////
		out += "<div class='menu' id='context_page_menu_show'>  \n";

		function = "onSwitchToEditMode";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
				+ gData.tr("Switch to EDIT mode") + "</div>  \n";
		out += "</div>  \n";

		/// ************ контекстное меню для EDIT режима ************ ///////
		out += "<div class='menu' id='context_page_menu_edit'>  \n";

		function = "onAddNewServer";
		out += "<div id='addServer1' class='menu-item' onclick=\"addNewGraphObject('server','server1',this);\">"
				+ gData.tr("Add blade server") + "</div>  \n";

		function = "onAddNewServer";
		out += "<div id='addServer2' class='menu-item' onclick=\"addNewGraphObject('server','server2',this);\">"
				+ gData.tr("Add Mainframe server") + "</div>  \n";

		function = "onAddNewGroup";
		out += "<div id='addGroup' class='menu-item' onclick=\"addNewGraphObject('group','',this);\">"
				+ gData.tr("Add group") + "</div>  \n";

		function = "onAddNewSapHanaDB";
		out += "<div id='addSapHana' class='menu-item' onclick=\"addNewGraphObject('db_system','sap_hana',this);\">"
				+ gData.tr("Add SAP Db") + "</div>  \n";

		function = "onAddNewOracleDB";
		out += "<div id='addOraDB' class='menu-item' onclick=\"addNewGraphObject('db_system','oracle_db',this);\">"
				+ gData.tr("Add Oracle Db") + "</div>  \n";

		function = "onAddNewSapApp";
		out += "<div id='addApplication' class='menu-item' onclick=\"addNewGraphObject('app_system','sap_nw',this);\">"
				+ gData.tr("Add Sap server") + "</div>  \n";

		function = "onAddNewHttpApp";
		out += "<div id='addApplication' class='menu-item' onclick=\"addNewGraphObject('app_system','http',this);\">"
				+ gData.tr("Add Http server") + "</div>  \n";

		function = "onSaveAll";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">" + gData.tr("Save")
				+ "</div>  \n";

		out += "</div>  \n";

		/// ************ контекстное меню для отдельного элемента ************ ///////

		out += "<div class='menu'  id='context_menu_div'>  \n";

		function = "onDeleteDiv";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">" + gData.tr("Delete")
				+ "</div>  \n";

		function = "onShowProperties";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
				+ gData.tr("Properties") + "</div>  \n";

		function = "onReleaseDiv";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
				+ gData.tr("Release") + "</div>  \n";

		out += "</div>  \n";

		out += "<div id='properties' class='properties_class' > \n";
		out += " <div style='float: right;' class='close_button' onclick='onClosePropertiesBox();'>X</div>\n";

		out += " <label id='prop_caption'>Edit:</label><br> \n";
		out += "<hr> \n";

		out += " <label for='prop_label'>" + gData.tr("Label") + NBSP(3) + "</label> \n";
		out += " <input type='text' id='prop_label' size='12'><br>  \n";

		out += " <label for='prop_height'>" + gData.tr("Height") + NBSP(2) + "</label> \n";
		out += " <input type='number' id='prop_height' name='height' min='0' max='10000'><br> \n";

		out += " <label for='prop_width'>" + gData.tr("Width") + NBSP(3) + "</label> \n";
		out += " <input type='number' id='prop_width' name='width' min='0' max='10000'><br>  \n";

		out += " <label for='prop_physgui'>" + gData.tr("PhysGuid") + "</label> \n";
		out += " <input type='text' id='prop_physguid' name='' size='14'> \n";

		out += "<hr> \n";
		out += " <input style='float: right;' type='button' value=' " + gData.tr("ok") + "' \n";
		out += " onclick='onOkPropertiesInForm(this);'> \n";

		out += "</div>  \n";

		return out;
	}

	protected String NBSP(int count) {
		String out = "";
		for (int i = 0; i < count; i++) {
			out += "&nbsp;";
		}

		return out;
	}

	protected String getEtalonObject() {
		String out = "";
		out += "{";
		for (String field : etalon) {
			out += "\"" + field + "\":\"\",";
		}
		out = out.substring(0, out.length() - 1);
		out += "}";
		return out;
	}

	protected String readGrapObjectsFromDataBase() {

		String out = "";

		String SQL = "select * from graph_objects where page='" + params.get("page") + "' order by level";

		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					out += "{";

					for (String field : etalon) {
						out += "\"" + field + "\":\"" + rec.get(field) + "\",";
					}

					out = out.substring(0, out.length() - 1);
					out += "},";
				}
				out = out.substring(0, out.length() - 1);

			}
		}

		return out;

	}

	public String getDocumentReadyScript(List<GraphJsObject> list) {
		String out = "";

		out += "<script>";

		out += "document.addEventListener('DOMContentLoaded', function(){ \n";

		out += "refreshAll(); \n";

		out += getGraphLinks();

		out += "  }); \n";

		out += "</script>";
		return out;
	}

	public String getGraphLinks() {
		String out = "";

		String SQL = "select * from graph_links where page='" + params.get("page") + "'";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += "connect('" + rec.get("from") + "','" + rec.get("to") + "', '" + rec.get("label") + "'); ";

				}
			}
		}

		return out;
	}

	protected void getParametersForGraphPage() {
		String out = "";
		if (params == null) {
			caption = "params is null";
			return;
		}
		if (!params.containsKey("page")) {
			caption = "params doesn't contains parameter page";
			return;
		}

		String SQL = "select * from graph_page where guid='" + params.get("page") + "'";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {
					this.caption = rec.get("caption");

				}
			}
		}

	}

	public String readGraphLines() {
		String out = "";

		String SQL = "select * from graph_links where page='" + params.get("page") + "'";
		List<Map<String, String>> records_list = gData.sqlReq.getSelect(SQL);

		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out += "connect('" + rec.get("from") + "','" + rec.get("to") + "', '" + rec.get("label") + "'); \n";

				}
			}
		}

		return out;
	}

}
