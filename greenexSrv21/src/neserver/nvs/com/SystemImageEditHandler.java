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






public class SystemImageEditHandler extends HandlerTemplate{
	
	String test = "";
	
	public List<GraphJsObject> jsObjects = new ArrayList<GraphJsObject>();
	public String caption = "";
	public int offsetTop = 30;
	public int offsetLeft = 10;
	
	
	public SystemImageEditHandler(globalData gData) {
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
		String curStyle= "";
		
		jsObjects=readGrapObjectsFromDataBase();	
		
		getParametersForGraphPage();		
		
		out += getBeginPage();
		out += strTopPanel(caption);

		out += buildGlobalJavascriptArea();	
	
		out += buildFunctionsForContextMenu();
		out += buildContextMenu();		
		





	
		out += jsFunctions();
		out += buildJSLayout();

		
		
		out += getDocumentReadyScript(jsObjects);

		out += getEndPage();
		
		return out;
		
	}

	protected String buildGlobalJavascriptArea(){
		String out = "";
		out += "<script>  \n";
		out += "window.pageGuid =  '" + params.get("page") + "';  \n";	
		out += "window.graphObj =  null;  \n";
		out += "window.selectedElement =  null;  \n";
		out += "window.mouseoverElement =  null;  \n";
		out += "window.curzIndex =  null;  \n";		
		out += "window.mode =  'EDIT';  \n";
		out += "window.mouseLeft =  null;  \n";
		out += "window.mouseTop =  'EDIT';  \n";		
			
		out += "</script>  \n";		
		return out;
}
	public String getDocumentReadyScript(List<GraphJsObject> list){
		String out = "";

		out += "<script>";

		out += "document.addEventListener('DOMContentLoaded', function(){ \n";
		
		out += "refreshAll(); \n";
		
		out += getGraphLinks();


		out += "  }); \n";
		


		out += "</script>";
		return out;
	}

	public String getGraphLinks(){
		String out = "";

		String SQL = "select * from graph_links where page='" + params.get("page") + "'"; 
		List<Map<String , String>> records_list  = gData.sqlReq.getSelect(SQL);

		
		if (records_list != null ) {
			if (records_list.size() > 0) {
				
				for (Map<String, String> rec : records_list) {
					
					out += "connect('" + rec.get("from") + "','" + rec.get("to") + "', '" + rec.get("label") + "'); ";
					
				}
			}
		}



		return out;
	}

	protected List<GraphJsObject> readGrapObjectsFromDataBase() {
		
		List<GraphJsObject>outList = new ArrayList<GraphJsObject>();
		
		String SQL = "select * from graph_objects where page='" + params.get("page") + "' order by level"; 
		
		List<Map<String , String>> records_list  = gData.sqlReq.getSelect(SQL);

		
		if (records_list != null ) {
			if (records_list.size() > 0) {
			
				
				for (Map<String, String> rec : records_list) {
					String paramName = rec.get("param_name");
					String paramValue = rec.get("param_value");

					GraphJsObject o;
					o = new GraphJsObject();
					o.guid = rec.get("guid");
					o.Class = rec.get("class"); 
					o.subClass = rec.get("subclass");
					o.label = rec.get("label"); 
					o.show_label = rec.get("show_label"); 
					o.bgcolor = rec.get("bgcolor");
					o.borderColor = rec.get("borderColor");  
					o.lineWidth = Integer.valueOf(rec.get("lineWidth"));  
					o.left = Integer.valueOf(rec.get("left")); 
					o.top = Integer.valueOf(rec.get("top")); 
					o.width = Integer.valueOf(rec.get("width"));
					o.height = Integer.valueOf(rec.get("height")); 
					o.level = Integer.valueOf(rec.get("level")); 
					o.link = rec.get("link"); 
					outList.add(o);

					
				}
			}
		}
		
		
	return 	outList;
		
	}
	protected void getParametersForGraphPage() {
		String out = "";
		if (params==null) { caption = "params is null"; 
		return;
		}
		if (!params.containsKey("page")) { 
			caption = "params doesn't contains parameter page";
		return;
		}
		
		String SQL = "select * from graph_page where guid='" + params.get("page") + "'"; 
		List<Map<String , String>> records_list  = gData.sqlReq.getSelect(SQL);

		
		if (records_list != null ) {
			if (records_list.size() > 0) {
				
				for (Map<String, String> rec : records_list) {
					this.caption = rec.get("caption");
					
				}
			}
		}

	}

 
	

	
	protected String buildContextMenu() {
		String out = "";
		String function = "";

///        page context show  //////////		
		out += "<div class='menu' id='context_page_menu_show'>  \n";

		function = "onSwitchToEditMode";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
						+ gData.tr("Switch to EDIT mode") + "</div>  \n";
		out += "</div>  \n";		
		
///        page context menu  //////////		
		out += "<div class='menu' id='context_page_menu_edit'>  \n";

		function = "onAddNewServer";
		out += "<div class='menu-item' onclick=\"AddNewGraphObject('blade');\">"
				+ gData.tr("Add blade server") + "</div>  \n";

		function = "onAddNewServer";
		out += "<div class='menu-item' onclick=\"AddNewGraphObject('mainframe');\">"
				+ gData.tr("Add Mainframe server") + "</div>  \n";

		function = "onAddNewGroup";
		out += "<div class='menu-item' onclick=\"AddNewGraphObject('group');\">"
				+ gData.tr("Add group") + "</div>  \n";

		function = "onAddNewSapHanaDB";
		out += "<div class='menu-item' onclick=\"AddNewGraphObject('sapHanaDb');\">"
				+ gData.tr("Add SAP Db") + "</div>  \n";

		function = "onAddNewOracleDB";
		out += "<div class='menu-item' onclick=\"AddNewGraphObject('oracleDb');\">"
				+ gData.tr("Add Oracle Db") + "</div>  \n";

		function = "onAddNewApp";
		out += "<div class='menu-item' onclick=\"AddNewGraphObject('app');\">"
				+ gData.tr("Add App server") + "</div>  \n";


		function = "onSaveAll";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
				+ gData.tr("Save") + "</div>  \n";

		out += "</div>  \n";

///////     element context menu /////////////

		out += "<div class='menu'  id='context_menu_div'>  \n";

		function = "onDeleteDiv";
		out += "<div id='menu_" + function + "' class='menu-item' onclick=\"" + function + "();\">"
				+ gData.tr("Delete") + "</div>  \n";

		function = "onProperties";
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

		out += " <label for='prop_label'>" + gData.tr("Label") + "&nbsp;" + "</label> \n";
		out += " <input type='text' id='prop_label' size='7'> \n";

		out += " <label for='prop_height'>" + gData.tr("Height") + "</label> \n";
		out += " <input type='number' id='prop_height' name='height' min='0' max='10000'> \n";		

		out += " <label for='prop_width'>" + gData.tr("Width") + "&nbsp;" + "</label> \n";
		out += " <input type='number' id='prop_width' name='width' min='0' max='10000'> \n";		
		out += "<hr> \n"; 
		out += " <input style='float: right;' type='button' value=' " + gData.tr("Change") + "' \n";	
		out += " onclick='onChangePropertiesInForm(this);'> \n";


		out += "</div>  \n";
		
		return out;
	}

	protected String buildFunctionsForContextMenu() {
		String out = "";
		
		out += "\n<script>  \n";

		out += "var currentElement = null;  \n";

		out += "function onReleaseDiv(){ \n";
	
		out += " window.selectedElement.removeEventListener('mousedown', mouseDownSelectedElement);  \n";
		out += " window.selectedElement.removeEventListener('mousemove', onMouseMove);  \n";
		out += " window.selectedElement.style.zIndex = window.curzIndex;  \n";
		out += " window.selectedElement.style.boxShadow = null;  \n";
		out += "} \n";		
		
		out += "function onAddNewServer(){ \n";

		out += " alert('onAddNewServer ');  \n";
		out += "} \n";
	
		out += "function onDeleteServer(){ \n";
		out += " alert('onDeleteServer ');  \n";
		out += "} \n";

		out += "function setCurrentId(obj){	 \n";
		out += "window.selectedElement = obj; \n";
		out += "console.log(obj.id); \n";
		out += "} \n";

		out += "function resetCurrentId(){ \n";
		out += "window.selectedElement = null; \n";
		out += "}; \n";
		


//********** vvv  onSaveAll vvv *************************

		out += "function onSaveAll(){	 \n";		

		out += "var strJSObj = ''; \n";
		out += "var grObj = null; \n";

		out += "for (var i = 0, len = window.graphObj.length; i < len; i++) { \n";

		out += "grObj = window.graphObj[i]; \n";

		out += "strJSObj += 'guid::' + grObj.guid + ';;'; \n";
		out += "strJSObj += 'class::' + grObj.class + ';;'; \n";
		out += "strJSObj += 'subclass::' + grObj.subclass + ';;'; \n";		
		out += "strJSObj += 'label::' + grObj.label + ';;'; \n";		
		out += "strJSObj += 'bgcolor::' + grObj.bgcolor + ';;'; \n";
		out += "strJSObj += 'borderColor::' + grObj.borderColor + ';;'; \n";		
		out += "strJSObj += 'level::' + grObj.level + ';;'; \n";
		out += "strJSObj += 'link::' + grObj.link + ';;'; \n";		
		
		
		out += "var rect = document.getElementById(grObj.guid).getBoundingClientRect(); \n";

		out += "strJSObj += 'top::' + rect.top + ';;'; \n";		
		out += "strJSObj += 'left::' + rect.left + ';;'; \n";
		out += "strJSObj += 'width::' + grObj.width + ';;'; \n";	
		out += "strJSObj += 'height::' + grObj.height + ';;'; \n";		
		
		
		out += "strJSObj += '!!'; \n";		
		
		out += "} \n";


		out += "sendRequest('/gr_page_save?page=' + window.pageGuid, strJSObj); \n";

		out += "} \n";

//********** ^^^  onSaveAll ^^^ *************************

	
		out += "</script>  \n";
		
		return out;
	}
	protected String buildJSLayout(){
		String out = "";
		out += "<script>  \n";



		out += "function refreshAll(){ \n";
		out += " getObjectsList(); \n";
		out += " buildLayoutGroups(); \n";
		out += " buildLayoutHardAndSoft(); \n";
		out += " addListenEventsOnDiv(); \n";

		out += "document.addEventListener('contextmenu', onContextMenuPage, false); \n";
		out += "document.addEventListener('click', onClickDocument, false); \n";


		out += "}	\n";
		
		
		out += "function getObjectsList(){ \n";
		out += "window.graphObj = [ \n";

		for(GraphJsObject o: jsObjects) {

		out += "{ ";
		out += "'guid':'" + o.guid + "',";
		out += "'class':'" + o.Class + "',";
		out += "'subclass':'" + o.subClass + "',";
		out += "'label':'" + o.label + "',";
		out += "'show_label':'" + o.show_label + "',";
		out += "'bgcolor':'" + o.bgcolor + "',";		
		out += "'borderColor':'" + o.borderColor + "',";		
		out += "'left':'" + o.left + "',";		
		out += "'top':'" + o.top + "',";
		out += "'width':'" + o.width + "',";		
		out += "'height':'" + o.height + "',";		
		out += "'level':'" + o.level + "',";
		out += "'link':'" + o.link + "'";						
		out += "}, \n";
		}
		
		out = out.substring(0, out.length() - 1);
		
		out += "];	\n";
		out += "}	\n";



		out += "function buildLayoutHardAndSoft(){ \n";

		out += "removeElementsByClass(\"server\"); \n";
		out += "removeElementsByClass(\"db_system\"); \n";
		out += "removeElementsByClass(\"app_system\"); \n";


		
		out += "for (var i = 0, len = window.graphObj.length; i < len; i++) {  \n";


		out += "if (window.graphObj[i].class == 'server' ||   \n";
		out += "window.graphObj[i].class == 'app_system' ||   \n";
		out += "window.graphObj[i].class == 'db_system') {   \n";

		out += "var newEl = document.createElement('div');  \n";

		out += "newEl.id = window.graphObj[i].guid;   \n";
		
		
		out += "newEl.className = window.graphObj[i].class + ' ' + graphObj[i].subclass;  \n";
		out += "newEl.innerHTML = window.graphObj[i].label;  \n";
		out += "newEl.style.position = 'absolute';  \n";
		out += "newEl.style.left = window.graphObj[i].left + 'px';  \n";
		out += "newEl.style.top = window.graphObj[i].top + 'px'; \n";
 		out += "newEl.style.zIndex = window.graphObj[i].level; \n"; 
 		out += "var imagePath = '/img/' + window.graphObj[i].subclass + '.png'; \n"; 
  		out += "newEl.style.backgroundImage = \"url('\" + imagePath + \"')\"; \n"; 		
 		out += "newEl.style.backgroundRepeat = 'no-repeat'; \n"; 
		out += "document.body.appendChild(newEl); \n";
		out += "		}	\n";
		out += "	}	\n";
		out += "}	\n";



		out += "function buildLayoutGroups(){ \n";

		out += "removeElementsByClass(\"group\"); \n";

		out += "for (var i = 0, len = window.graphObj.length; i < len; i++) {  \n";

		out += "if (graphObj[i].class == 'group') {   \n";

		out += "var newEl = document.createElement('div');  \n";

		out += "newEl.id = graphObj[i].guid;   \n";
		out += "newEl.className = graphObj[i].class ;  \n";
		out += "newEl.innerHTML = graphObj[i].label;  \n";
		out += "newEl.style.position = 'absolute';  \n";
		out += "newEl.style.left = graphObj[i].left + 'px';  \n";
		out += "newEl.style.top = graphObj[i].top + 'px'; \n";
		out += "newEl.style.width = graphObj[i].width + 'px'; \n"; 	
		out += "newEl.style.height = graphObj[i].height + 'px'; \n";  	
 		out += "newEl.style.zIndex = graphObj[i].level; \n"; 
		out += "newEl.style.background = graphObj[i].bgcolor; \n"; 
		out += "document.body.appendChild(newEl); \n";

		out += "		}	\n";
		out += "	}	\n";
		out += "}	\n";


		out += "function addListenEventsOnDiv(){ \n";


		out += "for (var i = 0, len = window.graphObj.length; i < len; i++) {  \n";

		out += "if ( window.graphObj[i].class == 'server' ||   \n";
		out += " window.graphObj[i].class == 'group' ||   \n";
		out += " window.graphObj[i].class == 'app_system' ||   \n";
		out += " window.graphObj[i].class == 'db_system') {   \n";

		out += "var newEl = document.getElementById(window.graphObj[i].guid);  \n";
	
		out += "newEl.addEventListener('dblclick', onDblClickSelectedElement); \n";
		out += "newEl.addEventListener('mouseover', onMouseOverElement); \n";
		out += "newEl.addEventListener('contextmenu', onContextMenuDev, false); \n";
		out += "newEl.ondragstart = function() {  \n";
		out += "	return false;  \n";
		out += "};  \n";
		
		

		out += "  }	\n";
		out += " }	\n";
		out += "}	\n";



		out += "function removeElementsByClass(className){ \n";
		out += "var elements = document.getElementsByClassName(className); \n";
		out += "while(elements.length > 0){ \n";
		out += "elements[0].parentNode.removeChild(elements[0]); \n";
		out += "} \n";
		out += "} \n";


			
		out += "</script>  \n";
		return out;
}


	protected String jsFunctions() {
		String out = "";
		out += "<script>  \n";

		out += "function onMouseMove(event) {  \n";

		out += "     event.target.style.left = ( event.pageX - 30 ) + 'px';  \n";
		out += "     event.target.style.top = ( event.pageY - 20 ) + 'px';  \n";


		out += "}  \n";



		out += "function mouseDownSelectedElement(event) { \n";

		out += " event.target.style.position = 'absolute';  \n";
		out += " event.target.style.zIndex = 500;  \n";

		out += " document.body.append(event.target);  \n";

		out += "  event.target.addEventListener('mousemove', onMouseMove);   \n";
		out += "} \n";	

		

		out += "function onMouseOverElement(event) { \n";
		out += " window.mouseoverElement = event.target; \n";	
		out += "} \n";

		
		

		
		
		
		out += "function onDblClickSelectedElement(event) { \n";
	
		out += "if (window.mode === 'SHOW') return; \n";

		out += "console.log('onDblClickSelectedElement') \n";	
		
		out += "	if (window.selectedElement) { \n";		
		out += "		 window.selectedElement = null; \n";	

		out += " 		 window.selectedElement.style.boxShadow = null; \n";
		out += "		 window.selectedElement.style.zIndex = window.curzIndex;  \n";
		
		out += "	}   \n";

		out += "  event.target.style.boxShadow = '0 0 5px #999999';   \n";
		out += "  window.selectedElement = event.target;   \n";	
		out += "  window.curzIndex =  event.target.style.zIndex;  \n";  				
		out += "  event.target.addEventListener('mousedown', mouseDownSelectedElement);   \n";
		out += "  event.target.addEventListener('dblclick', onDblClickSelectedElement);  \n";

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


		out += "function onContextMenuPage(event) { \n";

		out += "if (document.addEventListener) { \n";
		
		out += "	if (window.mode=='SHOW') { \n";
		out += " 	contextMenuBoxPage = window.document.getElementById('context_page_menu_show');  \n";
		out += "	} else { \n";
		out += " 	contextMenuBoxPage = window.document.getElementById('context_page_menu_edit');  \n";
		out += "	} ; \n";

		out += " contextMenuBoxPage.style.left = event.clientX + 'px';	 \n";
		out += " contextMenuBoxPage.style.top = event.clientY  + 'px';		 \n";
		out += " contextMenuBoxPage.style.display = 'block';	 \n";	
		out += "    event.preventDefault();   \n";			
		out += "} else {    \n";
		out += "document.attachEvent('oncontextmenu', function() {    \n";
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







		out += "function onChangePropertiesInForm(obj) { \n";
		out += "console.log('onChangePropertiesInForm' + obj.id); \n";

		
		out += "for (var i = 0, len = window.graphObj.length; i < len; i++) {  \n";	
		out += " if (window.graphObj[i].guid===window.mouseoverElement.id) { \n";	
		out += " window.graphObj[i].label = document.getElementById('prop_label').value;	  \n";		
		out += " window.graphObj[i].height = document.getElementById('prop_height').value;	  \n";	
		out += " window.graphObj[i].width = document.getElementById('prop_width').value;	  \n";	


		out += " 	} \n";	
		out += " } \n";			

//		out += "item.height = document.getElementById('prop_height').value;	  \n";	
//		out += "item.width = document.getElementById('prop_width').value;	  \n";



		out += " box = document.getElementById('properties'); \n";	
		out += " if (box) { \n";	
		out += "	if (box.style.display === 'block') { \n";
		out += " 	box.style.display = 'none'; \n";
		out += "	} \n";
		out += " } \n";	

		out += "} \n";


		out += "function onSwitchToEditMode() { \n";
		out += "	window.mode =  'EDIT';  \n";		
		out += "} \n";
		

		out += "function onDeleteDiv() { \n";
		out += "	console.log('Delete Div');  \n";		
		out += "} \n";		
		
		out += "function onClosePropertiesBox() { \n";
		out += "	 box = document.getElementById('properties');	  \n";	
		out += " if (box) { \n";
		out += " 	 box.style.left = window.mouseLeft; \n";
		out += " 	 box.style.top = window.mouseTop; \n";		
		out += " 	 box.style.display = 'none'; \n";	
		out += "	} \n";	
		out += "} \n";



		out += "function onProperties() { \n";

		out += "var item = window.graphObj.find(item => item.guid === window.mouseoverElement.id); \n";
		out += "	 box = document.getElementById('properties');	  \n";	
		out += " if (box) { \n";
		out += " 	 box.style.left = window.mouseLeft; \n";
		out += " 	 box.style.top = window.mouseTop; \n";		
		out += " 	 box.style.display = 'block'; \n";
		out += "	 document.getElementById('prop_caption').innerHTML= '" +gData.tr("Properties for:")+ " <b>' + item.label + '</b>'; \n";
		out += "	 document.getElementById('prop_label').value= item.label;	  \n";
		out += "	 document.getElementById('prop_height').value= item.height;	  \n";	
		out += "	 document.getElementById('prop_width').value= item.width;	  \n";

		out += " } \n";



		out += "} \n";		

		

		out += "function AddNewGraphObject(typeObj) { \n";

		out += "var newSingleObj = []; \n";
		out += "newSingleObj.push({'key':'guid','value':'AAA'}); \n";
		out += "newSingleObj.push({'key':'class','value':'group'}); \n";
		out += "newSingleObj.push({'key':'subclass','value':'S'}); \n";		
			
		out += "console.log('AddNewGraphObject=' + typeObj);\n";

		
		out += "for (var i = 0, len = newSingleObj.length; i < len; i++) {  \n";		
		out += "console.log(newSingleObj[i].key + ' ' + newSingleObj[i].value);\n";		
		out += "} \n";		
		
/*		
		
		out += "switch (typeObj) { \n"; 
		
		out += "	case 'blade':  \n";

		out += " 	break;  \n";		

		out += "	case 'mainframe':  \n";

		out += " 	break;  \n";		

		out += "	case 'group':  \n";
		out += "		newEl.style.width = '300px'; \n"; 	
		out += "		newEl.style.height = '400px'; \n"; 
		out += "		newEl.style.zIndex = -20; \n"; 	
		out += "		newEl.style.background = '#EEEEEE'; \n";

		out += " 	break;  \n";

		out += "	case 'sapHanaDb':  \n";

		out += " 	break;  \n";
		out += "	case 'oracleDb':  \n";

		out += " 	break;  \n";

		out += "	case 'app':  \n";

		out += " 	break;  \n";
		
		out += "	default:  \n";

		out += " 	break;  \n";			
		
		out += "} \n"; 		

/////////////////////////////////////////////
		out += "var newEl = document.createElement('div');  \n";
		out += "newEl.id = '';   \n";
		out += "newEl.className = graphObj[i].class ;  \n";
		out += "newEl.innerHTML = graphObj[i].label;  \n";
		out += "newEl.style.position = 'absolute';  \n";
		out += "newEl.style.left = window.mouseLeft + 'px';  \n";
		out += "newEl.style.top = window.mouseTop + 'px'; \n";
 		out += "newEl.style.zIndex = 5; \n"; 
		out += "newEl.style.background = graphObj[i].bgcolor; \n"; 



		out += "document.body.appendChild(newEl); \n";		
		
*/		
		out += "} \n";		
		
		
	
	
	
	
	
		
		

		out += "function sendRequest(url, text, callback) { \n";

		out += "var xhr = new XMLHttpRequest(); \n";
		out += "xhr.open('POST', url, true); \n";
		out += "xhr.responseType = 'text' \n";
	    out += "xhr.onload = function(e) {  \n";		
	    out += " if (this.status == 200) { \n";
	    out += "     console.log(this.responseText); \n";
	    out += "  }	\n";	
		out += " };	 \n";	

		out += " xhr.send(text);  \n";

		out += "} \n";
		
		
		out += "</script>  \n";
		return out;
	}
	
	



}
