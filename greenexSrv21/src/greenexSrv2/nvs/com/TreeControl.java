package greenexSrv2.nvs.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import obj.greenexSrv2.nvs.com.ContextMenuItem;

public class TreeControl {
	
	
	
	HashMap<String,String>params = null;
	public List<ContextMenuItem>menu = new ArrayList();
	public String id,rootCaption,dataFunctionName,treeControlId,firstNodeId;
	public String width,height ;

	
	public TreeControl(HashMap<String,String> params) {
		this.params = params;
		init();
	}
	
	protected void init() {
		String keyValue = "";
		
		keyValue = "id";		
		treeControlId = params.containsKey(keyValue) ?  params.get(keyValue) : "treeControl";
		keyValue = "rootCaption";
		rootCaption = params.containsKey(keyValue) ?  params.get(keyValue) : "root";
		keyValue = "dataFunctionName";
		dataFunctionName = params.containsKey(keyValue)? params.get(keyValue): "tree_data1";

		keyValue = "firstNodeId";
		firstNodeId = params.containsKey(keyValue)? params.get(keyValue): "first";

		keyValue = "width";
		width = params.containsKey(keyValue)? params.get(keyValue): "400px";
		keyValue = "height";
		height = params.containsKey(keyValue)? params.get(keyValue): "600px";

	}
	
	public String getTreeControlText() {
		String out = "";
		out += getStyleTreeCode();
		out += getJavascriptTreeCode();
		out += getHtmlTreeControlCode();
		out += getHtmlContextMenuCode();
		out += getJavascriptContextMenu();
		return out;
	}
	
	protected String getHtmlTreeControlCode() {
		String out = "";

		out += "<div class='workplace_" + treeControlId + "' >";	
		out += "<ul class='Container' id='" + treeControlId + "'> \n" ;
		out += " <li id='" + firstNodeId + "' class='Node IsRoot IsLast ExpandClosed'> \n" ;
		out += " <div class='Expand'></div> \n" ;
		out += " <div  class='Content'>" + rootCaption + "</div> \n" ;
		out += " <ul class='Container'> \n" ;
		out += "</ul> \n" ;
		out += "</li> \n" ;
		out += "</ul> \n" ;
		out += 	"</div>";	
		
		return out;
	}
	protected String getHtmlContextMenuCode() {
		String out = "";
		out += "<div class='menu_" + treeControlId + "' id = 'contextMenu_"+ treeControlId + "'";
		out +=  " >";		
	
		for(ContextMenuItem i: menu) {

			out += "<div id='menu_" + treeControlId +"_"+ i.id + "' class='menu-item' onclick=\"onContext_" + treeControlId + "_" + i.id +  "(this);\">"
				+  i.caption + "</div>  \n";		
		}
	
		out += 	"</div>";			
		return out;
	}
	protected String getJavascriptContextMenu() {
		String out = "";		
		out += "<script >\n" ;
		for(ContextMenuItem i: menu) {

			out += "function onContext_" + treeControlId + "_" + i.id + "(obj){ \n";
//			out += "alert( window.selectedElement_"+ treeControlId + ".id + '--' + obj.id); \n";
			out += i.onFunctionCode ;

			out += "} \n";
			
		}		
		
		
		out += "</script>\n" ;		
		return out;				
	}
	protected String getJavascriptTreeCode() {
		String out = "";
		out += "<script >\n" ;	

		out += " window.selectedElement_"+ treeControlId +";  \n";



		out += " function buildTreeControl_" + treeControlId + "(id, url) { \n" ;
		out += "  var element = document.getElementById(id); \n" ;

		out += " function hasClass(elem, className) { \n" ;
		out += "  return new RegExp(\"(^|\\\\s)\"+className+\"(\\\\s|$)\").test(elem.className); \n" ;
		out += " } \n" ;

		out += " function toggleNode(node) { \n" ;

		out += "  var newClass = hasClass(node, 'ExpandOpen') ? 'ExpandClosed' : 'ExpandOpen'; \n" ;


		out += "  var re =  /(^|\\s)(ExpandOpen|ExpandClosed)(\\s|$)/ ; \n" ;
		out += "  node.className = node.className.replace(re, '$1'+newClass+'$3'); \n" ;
		out += " } \n" ;



		out += " function load(node) { \n" ;


		
		out += "  function showLoading(on) { \n" ;
		out += "   var expand = node.getElementsByTagName('DIV')[0]; \n" ;
		out += "   expand.className = on ? 'ExpandLoading' : 'Expand'; \n" ;
		out += "  } \n" ;



		out += " function onLoaded(data) {   \n" ;
		
		out += " var jsonData = JSON.parse(data); \n";	
		
		out += " 	for(var i=0; i< jsonData.length; i++) {   \n" ;
		out += " 		var child = jsonData[i];   \n" ;
		out += " 		var li = document.createElement('LI');   \n" ;
		out += " 	li.id = child.id ;  \n" ;


		out += " 	if (!child.isFolder) {   \n" ;

		out += " 	li.style.fontWeight='normal';  \n" ;

		out += " 	 showLoading(false); \n" ;
		
		out += " 	li.ondblclick = function onDblClick(){ " ;
		out += "  		console.log('Go to:' + this.id ); \n";
		out += "	 } \n" ;

		out += " 	li.addEventListener('contextmenu', onContextMenu, false); \n";


		
		out += " 	li.onmouseover = function onMouseOver(){  \n" ;
		out += " 	  	this.style.textDecoration ='underline'; \n" ;
		out += " 	}   \n" ;
		out += " 	li.onmouseout = function onMouseOut(){  \n" ;
		out += " 	  this.style.textDecoration ='none'; \n" ;
		out += " 	}   \n" ;	
		out += " 	}   \n" ;	
		out += " 	else {   \n" ;	
		out += "		li.style.fontWeight='normal';  \n" ;
		out += " 	}   \n" ;		
		

		out += " 		li.className = 'Node Expand' + (child.isFolder ? 'Closed' : 'Leaf');   \n" ;

		out += " 		if (i === jsonData.length-1) { \n";

		out += "			li.className += ' IsLast' ;  \n" ;

		out += " 		} \n";

		out += " 		li.innerHTML = '<div class=\"Expand\"></div><div class=\"Content\" >'+child.title+'</div>';   \n" ;
		out += " 		if (child.isFolder) {   \n" ;
		out += " 			li.innerHTML += '<ul class=\"Container\"></ul>';   \n" ;
		out += " 		}   \n" ;
		out += " 		node.getElementsByTagName('UL')[0].appendChild(li);   \n" ;
		out += " 		}   \n" ;

		out += " 		node.isLoaded = true;   \n" ;
		out += " 		toggleNode(node);   \n" ;
		out += " 	}   \n" ;

	
		
		out += " 			showLoading(true);   \n" ;

		out += "			var curUrl = '/"+dataFunctionName+"?node_id=' + node.id ; \n";

		out += "			var resp = getRequest(curUrl,onLoaded); \n";
		out += " 		}   \n" ;

		out += " 		element.onclick = function(event) {   \n" ;
		
		out += " 			event = event || window.event;   \n" ;
		out += " 			var clickedElem = event.target || event.srcElement;   \n" ;

		out += " 			if (!hasClass(clickedElem, 'Expand')) {   \n" ;
		out += " 				return;    \n" ;
		out += " 			}   \n" ;

		out += " 			var node = clickedElem.parentNode;   \n" ;
		out += " 			if (hasClass(node, 'ExpandLeaf')) {   \n" ;
		out += " 				return;    \n" ;
		out += " 			}   \n" ;
		
		out += " 			if (node.isLoaded || node.getElementsByTagName('LI').length) {   \n" ;
		out += " 				toggleNode(node);   \n" ;
		out += " 				return;   \n" ;
		out += " 			}   \n" ;


		out += " 			if (node.getElementsByTagName('LI').length) {   \n" ;
		out += " 				toggleNode(node);   \n" ;
		out += " 				return;   \n" ;
		out += " 			}   \n" ;

		out += " 			load(node);   \n" ;
		out += " 		}   \n" ;
		
	
		out += "	function getRequest(url, callback) { \n";
		out += " 		var xhr = new XMLHttpRequest(); \n";

		out += " 		console.log('url=' + url); \n";

		out += " 		xhr.open('GET', url, true); \n";
		out += "		 xhr.send();  \n";

		out += " 		xhr.onreadystatechange = function() {  \n";
		out += " 			if (this.readyState == 4 && this.status == 200) { \n";
		out += "   				var response = this.responseText; \n";
		out += " 			if(callback) callback(this.responseText); \n";
		out += "  		}	\n";
		out += " 	};	 \n";
		out += " 	return xhr.responseText;	 \n";
		out += "	} \n";		
		out += " 		var node = document.getElementById('" +firstNodeId +"');\n" ;		
		out += " 		load(node);   \n" ;

		out += " 	}   \n" ;
		
		
		out += "function onContextMenu(event) { \n";
		out += " window.selectedElement_"+ treeControlId +" =  event.target.parentElement;  \n";

		out += " window.mouseLeft = event.clientX + 'px';	 \n";
		out += " window.mouseTop = event.clientY + 'px';	 \n";
		out += " contextMenuBox = window.document.getElementById('contextMenu_" + treeControlId + "');  \n";
		out += " contextMenuBox.style.left = event.clientX + 'px';	 \n";
		out += " contextMenuBox.style.top = event.clientY  + 'px';		 \n";
		out += " contextMenuBox.style.display = 'block';	 \n";
		out += "    event.preventDefault();   \n";
		out += "    event.stopPropagation();   \n";
		out += "} \n";
		
		out += "function onClickDocument(event) { \n";
		out += " document.getElementById('contextMenu_" + treeControlId + "').style.display = 'none'; \n";
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
		out += "</script> \n" ;
		
		
		out += "<script> \n" ;	
		out += "document.addEventListener('DOMContentLoaded', function(){ \n";

		out += "buildTreeControl_" + treeControlId + "('" + treeControlId + "', '" + dataFunctionName + "');\n" ;
	
		out += "  }); \n";
		out += "document.addEventListener('click', onClickDocument, false); \n";
		out += "</script>\n" ;
		return out;
	}

	public String getStyleTreeCode(){
		String out = "";

	out += " <style type='text/css'> \n" ;

	out += ".workplace_" + treeControlId + " {  \n";
	out += " 	float:left;  \n";	
	out += " 	width: " + width + ";	  \n";
	out += " 	height: " + height +";	  \n";
	out += " 	background-color : white; \n" ;	
	out += "	border-style: solid; \n" ;
	out += "	border-width: 1px; \n" ;
	out += "	border-color: grey; \n" ;
	out += "	border-radius: 2px; \n" ;

	out += " } \n" ;
	
	
	out += " .Container { \n" ;
	out += "  font-family: Verdana, Helvetica, sans-serif; \n" ;
	out += "  font-size: 12px; \n" ;
	out += "  padding: 0; \n" ;
	out += "  margin: 0; \n" ;
	out += " } \n" ;

	out += " .Container li { \n" ;
 	out += "  list-style-type: none; \n" ;
	out += " } \n" ;



	out += " .Node { \n" ;
	out += "   background-image : url('img/i.gif'); \n" ;

	out += "   background-position : top left; \n" ;
	out += "   background-repeat : repeat-y; \n" ;
	out += "   margin-left: 18px; \n" ;
	out += "   zoom: 1; \n" ;
	out += " } \n" ;

	out += " .IsRoot { \n" ;
	out += "  margin-left: 0; \n" ;
	out += " } \n" ;



	out += " .IsLast { \n" ;
	out += "  background-image : url('img/i_half.gif'); \n" ;
	out += "  background-repeat : no-repeat; \n" ;
	out += " } \n" ;

	out += " .ExpandOpen .Expand { \n" ;
	out += "  background-image : url('img/open_folder.gif'); \n" ;
	out += " } \n" ;


	out += " .ExpandClosed .Expand { \n" ;
	out += "  background-image : url('img/closed_folder.gif'); \n" ;
	out += " } \n" ;


	out += " .ExpandLeaf .Expand { \n" ;
	out += "  background-image : url('img/expand_leaf.gif'); \n" ;	

	out += " } \n" ;

	out += " .Content { \n" ;
	out += "  min-height: 18px; \n" ;
	out += "  margin-left:18px; \n" ;
	out += " } \n" ;

	out += " * html .Content { \n" ;
	out += " height: 18px; \n" ;
	out += " } \n" ;

	out += " .Expand { \n" ;
	out += "  width: 18px; \n" ;
	out += "  height: 18px; \n" ;
	out += "  float: left; \n" ;
	out += " } \n" ;


	out += " .ExpandOpen .Container { \n" ;
	out += "   display: block; \n" ;
	out += " } \n" ;

	out += " .ExpandClosed .Container { \n" ;
	out += "   display: none; \n" ;
	out += " } \n" ;

	out += " .ExpandOpen .Expand, .ExpandClosed .Expand { \n" ;
	out += "    cursor: pointer; \n" ;
	out += " } \n" ;
	out += " .ExpandLeaf .Expand { \n" ;
	out += "   cursor: auto; \n" ;
	out += " } \n" ;

	out += " .ExpandLoading   { \n" ;
	out += "     width: 18px; \n" ;
	out += "     height: 18px; \n" ;
	out += "     float: left; \n" ;
	out += "     background-image : url('img/expand_loading.gif'); \n" ;	

	out += " } \n" ;

	out += " .menu_" + treeControlId + "   { \n" ;
	out += "    position: absolute; \n" ;
	out += "    font-family: Courier New; \n" ;
	out += "    font-size: 80%; \n" ;
	out += "    display: none; \n" ;
	out += "	width: 160px; \n" ;
	out += "	background-color: white; \n" ;
	out += "	box-shadow: 3px 3px 5px #888888; \n" ;
	out += "	border-style: solid; \n" ;
	out += "	border-width: 1px; \n" ;
	out += "	border-color: grey; \n" ;
	out += "	border-radius: 2px; \n" ;
	out += "	padding-left: 5px; \n" ;
	out += "	padding-right: 5px; \n" ;
	out += "	padding-top: 3px; \n" ;
	out += "	padding-bottom: 3px; \n" ;
	out += "	position: fixed; \n" ;
	out += "	z-index: 1000;	 \n" ;
	out += " } \n" ;	
	
	
	
	out += "</style> \n" ;
		return out;
	}


}
