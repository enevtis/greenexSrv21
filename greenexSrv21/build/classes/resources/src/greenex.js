var currentElement;

function connect(idFrom, idTo, labelText) {

var elFrom = document.getElementById(idFrom);
var elTo = document.getElementById(idTo);

if (!elFrom) {
	console.log('connecting element From ' + idFrom + ' not found. Exit');
}
if (!elTo) {
	console.log('connecting element To ' + idTo + ' not found!.Exit');
}

if (!elFrom || !elTo) {

	console.log('Table graph_links ');	
	console.log('From ' + idFrom + ' To ' + idTo + ' ' + labelText + ' is error. Ignored');	
	
	return;
}

var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();	

var diffVertical = rectFrom.height;
var diffHorizont = rectFrom.width;

var direction = getDirection(idFrom, idTo);

switch (direction) {
	case "RU":
		connectRU(idFrom, idTo, labelText);
	break;
	case "RD":
		connectRD(idFrom, idTo, labelText);
	break;	
	case "LU":
		connectLU(idFrom, idTo, labelText);
	break;
	case "LD":
		connectLD(idFrom, idTo, labelText);
	break;
	case "U":
		connectU(idFrom, idTo, labelText);
	break;
	case "D":
		connectD(idFrom, idTo, labelText);
	break;
	case "L":
		connectL(idFrom, idTo, labelText);
	break;
	case "R":
		connectR(idFrom, idTo, labelText);
	break;

	default:
	alert('Ошибка: неизвестное направление:' + direction);
	break;
}

}
function getDirection (idFrom, idTo){
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();	
var out = "";

var dHl = rectFrom.height;
var dVl = rectFrom.width;

var dH = rectFrom.left - rectTo.left;
var dV = rectFrom.top - rectTo.top;


if (dH < 0 && Math.abs(dH) > dHl && dV < 0 && Math.abs(dV) > dVl) {
	out = "RD";
}else if (Math.abs(dH) < dHl && dV < 0 && Math.abs(dV) > dVl) {
	out = "D";
}else if (dH > 0 && Math.abs(dH) > dHl && dV < 0 && Math.abs(dV) > dVl) {
	out = "LD";	
}else if (dH > 0 && Math.abs(dH) > dHl && Math.abs(dV) < dVl) {
	out = "L";	
}else if (dH > 0 && Math.abs(dH) > dHl && dV > 0  && Math.abs(dV) > dVl) {
	out = "LU";	
}else if (Math.abs(dH) < dHl && dV > 0  && Math.abs(dV) > dVl) {
	out = "U";	
}else if (dH < 0 && Math.abs(dH) > dHl && dV > 0 && Math.abs(dV) > dVl) {
	out = "RU";	
}else if (dH < 0 && Math.abs(dH) > dHl && Math.abs(dV) < dVl) {
	out = "R";	
}else {
	alert('ошибка определения направления');
}
return out;

}	
function connectU(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectFrom.left + (rectFrom.width / 2)) + 'px;' ;
curStyle += 'top:' + ( rectTo.bottom + 20 ) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:' + (rectFrom.top - rectTo.top - rectFrom.height - 20 ) + 'px;';
curStyle += 'zIndex:2000;';

elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addVerticalLabel(elem1,labelText);
}

drawUpArrow(elem1);
}

function connectD(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectFrom.left + (rectFrom.width / 2)) + 'px;' ;
curStyle += 'top:' + ( rectFrom.bottom + 20 ) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:' + (rectTo.top - rectFrom.top - rectTo.height - 20 ) + 'px;';
curStyle += 'zIndex:2000;';

elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addVerticalLabel(elem1,labelText);
}

drawDownArrow(elem1);
}
function addVerticalLabel(obj, text) {

var color = '#FFF';
var objRect= obj.getBoundingClientRect();

var label1 = document.createElement('div');
label1.className = 'link_label';
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left - (text.length * 4))+'px;' ;
curStyle += 'top:' + (objRect.top + objRect.height / 2) + 'px;' ;
//curStyle += 'transform: rotate3d(0, 0, 1, 90deg);';
curStyle += 'zIndex:2000;';
label1.style.cssText = curStyle;
label1.innerHTML = text;
document.body.appendChild(label1);
}

function connectL(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectTo.left + rectTo.width + 4) + 'px;' ;
curStyle += 'top:' + ( rectFrom.top + rectFrom.height /2 ) + 'px;' ;
curStyle += 'width:' + (rectFrom.left - rectTo.left - (rectTo.width) - 5 ) + 'px;' ;
curStyle += 'height:1px;';
curStyle += 'zIndex:2000;';

elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addLabel(elem1,labelText);
}
drawLeftArrow(elem1);
}
function connectR(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectFrom.left + rectFrom.width) + 'px;' ;
curStyle += 'top:' + ( rectFrom.top + rectFrom.height /2 ) + 'px;' ;
curStyle += 'width:' + (rectTo.left - rectFrom.left - (rectTo.width) - 5 ) + 'px;' ;
curStyle += 'height:1px;';
curStyle += 'zIndex:2000;';
elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addLabel(elem1,labelText);
}

drawRightArrow(elem1);
}
function drawLeftArrow(obj){
var color = '#000';
var objRect= obj.getBoundingClientRect();
var strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left  )+'px;' ;
curStyle += 'top:' + (objRect.bottom - 3) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, 120deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);

strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left  )+'px;' ;
curStyle += 'top:' + (objRect.bottom - 6) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, -120deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);

}
function drawRightArrow(obj){
var color = '#000';
var objRect= obj.getBoundingClientRect();
var strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.right - 3 )+'px;' ;
curStyle += 'top:' + (objRect.bottom - 3) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, 60deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);

strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.right - 3 )+'px;' ;
curStyle += 'top:' + (objRect.bottom - 6) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, -60deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);

}

function drawDownArrow(obj){
var color = '#000';
var objRect= obj.getBoundingClientRect();
var strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left + 2 )+'px;' ;
curStyle += 'top:' + (objRect.bottom - 8) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, 30deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);

strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left - 2 )+'px;' ;
curStyle += 'top:' + (objRect.bottom - 8) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, -30deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);
}
function drawUpArrow(obj){
var color = '#000';
var objRect= obj.getBoundingClientRect();
var strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left + 2) + 'px;' ;
curStyle += 'top:' + (objRect.top ) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, -30deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);

strelka1 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left - 2 )+'px;' ;
curStyle += 'top:' + (objRect.top) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:8px;';
curStyle += 'transform: rotate3d(0, 0, 1, 30deg);';
curStyle += 'zIndex:2000;';
strelka1.style.cssText = curStyle;
document.body.appendChild(strelka1);

}

function addLabel(obj, text) {
var color = '#FFF';
var objRect= obj.getBoundingClientRect();
var label1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (objRect.left + objRect.width /2 )+'px;' ;
curStyle += 'top:' + (objRect.bottom) + 'px;' ;
curStyle += 'zIndex:2000;';
label1.className = 'link_label';
label1.style.cssText = curStyle;
label1.innerHTML = text;
document.body.appendChild(label1);
}

function addLabel2(id, text) {
var color = '#FFF';
var objRect= document.getElementById(id).getBoundingClientRect();
var label1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';

	curStyle += 'left:' + objRect.left +'px;' ;
	curStyle += 'top:' + (objRect.top - 20) + 'px;' ;
	curStyle += 'font-size: small;' ;
label1.className = 'link_label';
curStyle += 'zIndex:2000;';
label1.style.cssText = curStyle;
label1.innerHTML = text;
document.body.appendChild(label1);
}
function connectLD(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectTo.left + rectTo.width /2) + 'px;' ;
curStyle += 'top:' + ( rectFrom.top + rectFrom.height /2 ) + 'px;' ;
curStyle += 'width:' + (rectFrom.left - rectTo.left - (rectFrom.width / 2)) + 'px;' ;
curStyle += 'height:1px;';
curStyle += 'zIndex:2000;';
elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addLabel(elem1,labelText);
}
var prevElement = elem1.getBoundingClientRect();

elem2 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + prevElement.left + 'px;' ;
curStyle += 'top:' + ( prevElement.top ) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:' + (rectTo.top - rectFrom.top - (rectTo.height /2 )) +'px;';
curStyle += 'zIndex:2000;';
elem2.style.cssText = curStyle;
document.body.appendChild(elem2);

drawDownArrow(elem2);
}
function connectLU(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectTo.left + rectTo.width /2) + 'px;' ;
curStyle += 'top:' + ( rectFrom.top + rectFrom.height /2 ) + 'px;' ;
curStyle += 'width:' + (rectFrom.left - rectTo.left - (rectFrom.width / 2)) + 'px;' ;
curStyle += 'height:1px;';
curStyle += 'zIndex:2000;';
elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addLabel(elem1,labelText);
}
var prevElement = elem1.getBoundingClientRect();

elem2 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + prevElement.left + 'px;' ;
curStyle += 'top:' + ( rectTo.top + rectTo.height + 20 ) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:' + (rectFrom.top - rectTo.top - (rectTo.height /2 ) - 20) +'px;';
curStyle += 'zIndex:2000;';
elem2.style.cssText = curStyle;
document.body.appendChild(elem2);

drawUpArrow(elem2);
}
function connectRD(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectFrom.left + rectFrom.width) + 'px;' ;
curStyle += 'top:' + ( rectFrom.top + rectFrom.height /2 ) + 'px;' ;
curStyle += 'width:' + (rectTo.left - rectFrom.left - (rectTo.width / 2)) + 'px;' ;
curStyle += 'height:1px;';
curStyle += 'zIndex:2000;';
elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addLabel(elem1,labelText);
}

var prevElement = elem1.getBoundingClientRect();

elem2 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + prevElement.right + 'px;' ;
curStyle += 'top:' + ( prevElement.top ) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:' + (rectTo.top - rectFrom.top - (rectTo.height /2 )) +'px;';
curStyle += 'zIndex:2000;';
elem2.style.cssText = curStyle;
document.body.appendChild(elem2);

drawDownArrow(elem2);
}
function connectRU(idFrom, idTo, labelText){

var color = '#000';
var rectFrom = document.getElementById(idFrom).getBoundingClientRect();
var rectTo = document.getElementById(idTo).getBoundingClientRect();

var elem1 = document.createElement('div');
var curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + (rectFrom.left + rectFrom.width) + 'px;' ;
curStyle += 'top:' + ( rectFrom.top + rectFrom.height /2 ) + 'px;' ;
curStyle += 'width:' + (rectTo.left - rectFrom.left - (rectTo.width / 2)) + 'px;' ;
curStyle += 'height:1px;';
curStyle += 'zIndex:2000;';
elem1.style.cssText = curStyle;
document.body.appendChild(elem1);

if (labelText) {
	addLabel(elem1,labelText);
}

elem2 = document.createElement('div');
curStyle = 'position:absolute;background:' + color + ';';
curStyle += 'left:' + elem1.getBoundingClientRect().right + 'px;' ;
curStyle += 'top:' + ( rectTo.top +  rectTo.height  + 20) + 'px;' ;
curStyle += 'width:1px;' ;
curStyle += 'height:' + (rectFrom.top - rectTo.top - (rectFrom.height / 2) - 20)  +'px;';
curStyle += 'zIndex:2000;';
elem2.style.cssText = curStyle;
document.body.appendChild(elem2);

drawUpArrow(elem2);
}
function docReady(fn) {
    // see if DOM is already available
    if (document.readyState === "complete" || document.readyState === "interactive") {
        // call on next available tick
        setTimeout(fn, 1);
    } else {
        document.addEventListener("DOMContentLoaded", fn);
    }
} 
function showMenu(divId) {

	var rect = document.getElementById(divId).getBoundingClientRect();

	menuBox = window.document.querySelector('.menu');
	menuBox.style.left = (rect.left + 16) + 'px';
	menuBox.style.top = (rect.top + 16) + 'px';
	menuBox.style.display = 'block';
}
function setCurrentId(obj){	
	currentElement = obj;
	console.log(currentElement.id);
}
function resetCurrentId(){
	currentElement = null;
}


window.addEventListener('click', function(event) {
	menuBox = window.document.querySelector('.menu');
	if( menuBox ) {
		menuBox.style.display = 'none';
	}

	contextMenuBox = window.document.querySelector('.context_menu');
	if( contextMenuBox ) {
		contextMenuBox.style.display = 'none';
	}

}, true); 
document.addEventListener('mousemove', (event) => {
//	console.log(`Mouse X: ${event.clientX}, Mouse Y: ${event.clientY}`);

});

function removeElementsByClass(className){ 
	var elements = document.getElementsByClassName(className); 
		while(elements.length > 0){
		elements[0].parentNode.removeChild(elements[0]); 
	} 
} 