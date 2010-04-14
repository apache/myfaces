if("undefined"==typeof myfaces||null==myfaces){var myfaces=null}if("undefined"==typeof _reserveMyfacesNamespaces||_reserveMyfacesNamespaces==null){var _reserveMyfacesNamespaces=function(){if("undefined"==typeof myfaces||null==myfaces){myfaces=new Object()
}if("undefined"==typeof (myfaces._impl)||null==myfaces._impl){myfaces._impl=new Object()
}if("undefined"==typeof (myfaces._impl._util)||null==myfaces._impl._util){myfaces._impl._util=new Object()
}if("undefined"==typeof (myfaces._impl.core)||null==myfaces._impl.core){myfaces._impl.core=new Object()
}if("undefined"==typeof (myfaces._impl.xhrCore)||null==myfaces._impl.xhrCore){myfaces._impl.xhrCore=new Object()
}if("undefined"==typeof (myfaces.config)||null==myfaces.config){myfaces.config=new Object()
}};_reserveMyfacesNamespaces()}if("undefined"==typeof (myfaces._impl._util._LangUtils)||null==myfaces._impl._util._LangUtils){myfaces._impl._util._LangUtils=function(){};
myfaces._impl._util._LangUtils.global=this;myfaces._impl._util._LangUtils._underTest=false;
myfaces._impl._util._LangUtils._logger=null;myfaces._impl._util._LangUtils.isUnderTest=function(){return this._underTest
};myfaces._impl._util._LangUtils.byId=function(reference){if(myfaces._impl._util._LangUtils.isString(reference)){return document.getElementById(reference)
}return reference};myfaces._impl._util._LangUtils._toArray=function(obj,offset,startWith){var arr=startWith||[];
for(var x=offset||0;x<obj.length;x++){arr.push(obj[x])}return arr};myfaces._impl._util._LangUtils.trimStringInternal=function(it,splitter){return myfaces._impl._util._LangUtils.strToArray(it,splitter).join(splitter)
};myfaces._impl._util._LangUtils.strToArray=function(it,splitter){if(!myfaces._impl._util._LangUtils.isString(it)){throw Error("myfaces._impl._util._LangUtils.strToArray param not of type string")
}var resultArr=it.split(splitter);for(var cnt=0;cnt<resultArr.length;cnt++){resultArr[cnt]=myfaces._impl._util._LangUtils.trim(resultArr[cnt])
}return resultArr};myfaces._impl._util._LangUtils.trim=function(str){str=str.replace(/^\s\s*/,""),ws=/\s/,i=str.length;
while(ws.test(str.charAt(--i))){}return str.slice(0,i+1)};myfaces._impl._util._LangUtils.splitAndGetLast=function(theString,delimiter){var arr=theString.split(delimiter);
return arr[arr.length-1]};myfaces._impl._util._LangUtils.isString=function(it){return !!arguments.length&&it!=null&&(typeof it=="string"||it instanceof String)
};myfaces._impl._util._LangUtils.hitch=function(scope,method){if(arguments.length>2){return myfaces._impl._util._LangUtils._hitchArgs._hitchArgs.apply(myfaces._impl._util._LangUtils._hitchArgs,arguments)
}if(!method){method=scope;scope=null}if(this.isString(method)){scope=scope||window||function(){};
if(!scope[method]){throw (['myfaces._impl._util._LangUtils: scope["',method,'"] is null (scope="',scope,'")'].join(""))
}return function(){return scope[method].apply(scope,arguments||[])}}return !scope?method:function(){return method.apply(scope,arguments||[])
}};myfaces._impl._util._LangUtils._getLogger=function(){if(null==myfaces._impl._util._LangUtils._logger){myfaces._impl._util._LangUtils._logger=myfaces._impl._util._Logger.getInstance()
}return myfaces._impl._util._LangUtils._logger};myfaces._impl._util._LangUtils._hitchArgs=function(scope,method){var pre=this._toArray(arguments,2);
var named=this.isString(method);return function(){var args=this._toArray(arguments);
var f=named?(scope||myfaces._impl._util._LangUtils.global)[method]:method;return f&&f.apply(scope||this,pre.concat(args))
}};myfaces._impl._util._LangUtils.mixMaps=function(destination,source,overwriteDest){var _JSF2Utils=myfaces._impl._util._LangUtils;
var result={};var keyIdx={};var key=null;for(key in source){if(!overwriteDest){result[key]=_JSF2Utils.exists(dest,key)?dest[key]:source[key]
}else{result[key]=_JSF2Utils.exists(source,key)?source[key]:dest[key]}keyIdx[key]=true
}for(key in destination){result[key]=_JSF2Utils.exists(result,key)?result[key]:destination[key]
}return result};myfaces._impl._util._LangUtils.exists=function(root,element){return("undefined"!=typeof root&&null!=root&&"undefined"!=typeof root[element]&&null!=root[element])
};myfaces._impl._util._LangUtils.arrayContains=function(arr,string_name){for(var loop=0;
loop<arr.length;loop++){if(arr[loop]==string_name){return true}}return false};myfaces._impl._util._LangUtils.arrayToString=function(arr,delimiter){if(myfaces._impl._util._LangUtils.isString(arr)){return arr
}var finalDelimiter=(null==delimiter)?"\n":delimiter;var resultArr=[];for(var cnt=0;
cnt<arr.length;cnt++){if(myfaces._impl._util._LangUtils.isString(arr[cnt])){resultArr.push(((delimiter==null)?("["+cnt+"] "):"")+arr[cnt])
}else{resultArr.push(((delimiter==null)?("["+cnt+"] "):"")+arr[cnt].toString())}}return resultArr.join(finalDelimiter)
}}_reserveMyfacesNamespaces();if(!myfaces._impl._util._LangUtils.exists(myfaces,"_ListenerQueue")){myfaces._impl._util._ListenerQueue=function(){this._queue=[]
};myfaces._impl._util._ListenerQueue.prototype._assertListener=function(listener){if("function"!=typeof (listener)){throw Error("Error: myfaces._impl._util._ListenerQueue."+arguments.caller.toString()+"Parameter must be of type function")
}};myfaces._impl._util._ListenerQueue.prototype.add=function(listener){this._assertListener(listener);
this._queue.push(listener)};myfaces._impl._util._ListenerQueue.prototype.remove=function(listener){this._assertListener(listener);
var cnt=0;while(cnt<this._queue.length&&this._queue[cnt]!=listener){cnt+=1}if(cnt<this._queue.length){this._queue[cnt]=null;
this._queue.splice(cnt,1)}};myfaces._impl._util._ListenerQueue.prototype.broadcastScopedEvent=function(scope,argument){for(var cnt=0;
cnt<this._queue.length;cnt++){var varArgs=[];for(var argsCnt=1;argsCnt<arguments.length;
argsCnt++){varArgs.push(arguments[argsCnt])}this._queue[cnt].apply(scope,varArgs)
}};myfaces._impl._util._ListenerQueue.prototype.broadcastEvent=function(argument){for(var cnt=0;
cnt<this._queue.length;cnt++){this._queue[cnt].apply(null,arguments)}}}_reserveMyfacesNamespaces();
if(!myfaces._impl._util._LangUtils.exists(myfaces._impl._util,"_Utils")){myfaces._impl._util._Utils=function(){};
myfaces._impl._util._Utils.browserDetection=function(){var n=navigator;var dua=n.userAgent,dav=n.appVersion,tv=parseFloat(dav);
myfaces._impl._util._Utils.browser={};var d=myfaces._impl._util._Utils.browser;if(dua.indexOf("Opera")>=0){myfaces._impl._util._Utils.isOpera=tv
}if(dua.indexOf("AdobeAIR")>=0){d.isAIR=1}d.isKhtml=(dav.indexOf("Konqueror")>=0)?tv:0;
d.isWebKit=parseFloat(dua.split("WebKit/")[1])||undefined;d.isChrome=parseFloat(dua.split("Chrome/")[1])||undefined;
var index=Math.max(dav.indexOf("WebKit"),dav.indexOf("Safari"),0);if(index&&!d.isChrome){d.isSafari=parseFloat(dav.split("Version/")[1]);
if(!d.isSafari||parseFloat(dav.substr(index+7))<=419.3){d.isSafari=2}}if(dua.indexOf("Gecko")>=0&&!d.isKhtml&&!d.isWebKit){d.isMozilla=d.isMoz=tv
}if(d.isMoz){d.isFF=parseFloat(dua.split("Firefox/")[1]||dua.split("Minefield/")[1]||dua.split("Shiretoko/")[1])||undefined
}if(document.all&&!d.isOpera){d.isIE=parseFloat(dav.split("MSIE ")[1])||undefined;
if(d.isIE>=8&&document.documentMode!=5){d.isIE=document.documentMode}}};myfaces._impl._util._Utils.getXHRObject=function(){if("undefined"!=typeof XMLHttpRequest&&null!=XMLHttpRequest){return new XMLHttpRequest()
}try{return new ActiveXObject("Msxml2.XMLHTTP")}catch(e){}return new ActiveXObject("Microsoft.XMLHTTP")
};myfaces._impl._util._Utils.loadScript=function(src,type,defer,charSet){var xhr=myfaces._impl._util._Utils.getXHRObject();
xhr.open("GET",src,false);if("undefined"!=typeof charSet&&null!=charSet){xhr.setRequestHeader("Content-Type","application/x-javascript; charset:"+charSet)
}xhr.send(null);if(xhr.readyState==4){if(xhr.status==200){if(!defer){myfaces._impl._util._Utils.globalEval(xhr.responseText)
}else{setTimeout(function(){myfaces._impl._util._Utils.globalEval(xhr.responseText)
},1)}}else{throw Error(xhr.responseText)}}else{throw Error("Loading of script "+src+" failed ")
}};myfaces._impl._util._Utils.runScripts=function(request,context,item){if(item.nodeType==1){if(item.tagName.toLowerCase()=="script"){try{if(typeof item.getAttribute("src")!="undefined"&&item.getAttribute("src")!=null&&item.getAttribute("src").length>0){myfaces._impl._util._Utils.loadScript(item.getAttribute("src"),item.getAttribute("type"),false,"ISO-8859-1")
}else{var test=item.text;var go=true;while(go){go=false;if(test.substring(0,1)==" "){test=test.substring(1);
go=true}if(test.substring(0,4)=="<!--"){test=test.substring(4);go=true}if(test.substring(0,11)=="//<![CDATA["){test=test.substring(11);
go=true}}myfaces._impl._util._Utils.globalEval(test)}}catch(e){myfaces._impl.xhrCore._Exception.throwNewError(request,context,"Utils","runScripts",e)
}}else{var child=item.firstChild;while(child){myfaces._impl._util._Utils.runScripts(request,context,child);
child=child.nextSibling}}}};myfaces._impl._util._Utils.deleteItem=function(request,context,itemIdToReplace){var item=document.getElementById(itemIdToReplace);
if(item==null){myfaces._impl.xhrCore._Exception.throwNewWarning(request,context,"Utils","deleteItem","Unknown Html-Component-ID: "+itemIdToReplace);
return }item.parentNode.removeChild(item)};myfaces._impl._util._Utils.replaceHtmlItem=function(request,context,itemIdToReplace,newTag,form){try{newTag=myfaces._impl._util._LangUtils.trim(newTag);
var item=(typeof itemIdToReplace=="object")?itemIdToReplace:myfaces._impl._util._Utils.getElementFromForm(request,context,itemIdToReplace,form);
if(item==null){myfaces._impl.xhrCore._Exception.throwNewWarning(request,context,"Utils","replaceHTMLItem","Unknown Html-Component-ID: "+itemIdToReplace);
return }if(newTag!=""){var evalNode=null;if(typeof window.Range!="undefined"&&typeof Range.prototype.createContextualFragment=="function"){var range=document.createRange();
range.setStartBefore(item);var fragment=range.createContextualFragment(newTag);if(item.id=="myfaces_bodyplaceholder"){var parentNode=item.parentNode;
parentNode.appendChild(fragment);evalNode=parentNode}else{var replaceItem=myfaces._impl._util._Utils.findHtmlItemFromFragment(fragment,item.id);
if(replaceItem==null){replaceItem=fragment}evalNode=item.parentNode.replaceChild(replaceItem,item)
}}else{item.insertAdjacentHTML("beforeBegin",newTag);evalNode=item.previousSibling;
item.parentNode.removeChild(item);if(item.id!="myfaces_bodyplaceholder"&&("undefined"==typeof evalNode.id||null==evalNode.id||evalNode.id!=item.id)){var subNode=document.getElementById(item.id);
subNode.parentNode.removeChild(subNode);evalNode.parentNode.replaceChild(subNode,evalNode)
}}if(myfaces._impl._util._Utils.isManualScriptEval()){myfaces._impl._util._Utils.runScripts(request,context,evalNode)
}return }item.parentNode.removeChild(item)}catch(e){myfaces._impl.xhrCore._Exception.throwNewError(request,context,"Utils","replaceHTMLItem",e)
}};myfaces._impl._util._Utils.findHtmlItemFromFragment=function(fragment,itemId){if(fragment.childNodes==null){return null
}if(fragment.childNodes.length==1&&fragment.childNodes[0].id==itemId){return fragment
}for(var i=0;i<fragment.childNodes.length;i++){var c=fragment.childNodes[i];if(c.id==itemId){return c
}}for(var i=0;i<fragment.childNodes.length;i++){var c=fragment.childNodes[i];var item=myfaces._impl._util._Utils.findHtmlItemFromFragment(c,itemId);
if(item!=null){return item}}return null};myfaces._impl._util._Utils.ieQuircksEvents={"onabort":true,"onload":true,"onunload":true,"onchange":true,"onsubmit":true,"onreset":true,"onselect":true,"onblur":true,"onfocus":true,"onkeydown":true,"onkeypress":true,"onkeyup":true,"onclick":true,"ondblclick":true,"onmousedown":true,"onmousemove":true,"onmouseout":true,"onmouseover":true,"onmouseup":true};
myfaces._impl._util._Utils.setAttribute=function(domNode,attribute,value){if(!myfaces._impl._util._Utils.isUserAgentInternetExplorer()||myfaces._impl._util._Utils.browser.isIE>7){domNode.setAttribute(attribute,value);
return }attribute=attribute.toLowerCase();if(attribute==="class"){domNode.setAttribute("className",value)
}else{if(attribute==="for"){domNode.setAttribute("htmlFor",value)}else{if(attribute==="style"){var styleEntries=value.split(";");
for(var loop=0;loop<styleEntries.length;loop++){var keyVal=styleEntries[loop].split(":");
if(keyVal[0]!=""&&keyVal[0]=="opacity"){var opacityVal=Math.max(100,Math.round(parseFloat(keyVal[1])*10));
domNode.style.setAttribute("filter","alpha(opacity="+opacityVal+")")}else{if(keyVal[0]!=""){domNode.style.setAttribute(keyVal[0],keyVal[1])
}}}}else{if(myfaces._impl._util._Utils.ieQuircksEvents[attribute]){if(myfaces._impl._util._LangUtils.isString(attribute)){domNode.setAttribute(attribute,function(event){myfaces._impl._util._Utils.globalEval(attribute)
})}}else{domNode.setAttribute(attribute,value)}}}}};myfaces._impl._util._Utils.isManualScriptEval=function(){var _LangUtils=myfaces._impl._util._LangUtils;
var retVal=(_LangUtils.exists(myfaces._impl._util._Utils.browser,"isIE")&&(myfaces._impl._util._Utils.browser.isIE>5.5))||(_LangUtils.exists(myfaces._impl._util._Utils.browser,"isKhtml")&&(myfaces._impl._util._Utils.browser.isKhtml>0))||(_LangUtils.exists(myfaces._impl._util._Utils.browser,"isWebKit")&&(myfaces._impl._util._Utils.browser.isWebKit>0))||(_LangUtils.exists(myfaces._impl._util._Utils.browser,"isSafari")&&(myfaces._impl._util._Utils.browser.isSafari>0));
return retVal};myfaces._impl._util._Utils.isUserAgentInternetExplorer=function(){return myfaces._impl._util._Utils.browser.isIE
};myfaces._impl._util._Utils.getElementFromForm=function(request,context,itemIdOrName,form,nameSearch,localSearchOnly){try{if("undefined"==typeof form||form==null){return document.getElementById(itemIdOrName)
}if("undefined"==typeof includeName||nameSearch==null){nameSearch=false}if("undefined"==typeof localSearchOnly||localSearchOnly==null){localSearchOnly=false
}var fLen=form.elements.length;if(nameSearch&&"undefined"!=typeof form.elements[itemIdOrName]&&null!=form.elements[itemIdOrName]){return form.elements[itemIdOrName]
}for(var f=0;f<fLen;f++){var element=form.elements[f];if(element.id!=null&&element.id==itemIdOrName){return element
}}if(!localSearchOnly){return document.getElementById(itemIdOrName)}}catch(e){myfaces._impl.xhrCore._Exception.throwNewError(request,context,"Utils","getElementFromForm",e)
}return null};myfaces._impl._util._Utils.fuzzyFormDetection=function(request,context,element){if(0==document.forms.length){return null
}else{if(1==document.forms.length){return document.forms[0]}}if("undefined"==typeof element||null==element){return null
}var submitIdentifier=("undefined"!=element.id)?element.id:null;var submitName=("undefined"!=element.name)?element.name:null;
submitName=(null==submitName)?submitIdentifier:submitName;if("undefined"!=typeof submitIdentifier&&null!=submitIdentifier&&""!=submitIdentifier){var domElement=myfaces._impl._util._LangUtils.byId(submitIdentifier);
if("undefined"!=typeof domElement&&null!=domElement){var foundForm=myfaces._impl._util._Utils.getParent(null,context,domElement,"form");
if(null!=foundForm){return foundForm}}}var foundElements=new Array();var namedFoundElements=document.getElementsByName(submitName);
if(null!=namedFoundElements){for(var cnt=0;cnt<namedFoundElements.length;cnt++){var foundForm=myfaces._impl._util._Utils.getParent(null,context,namedFoundElements[cnt],"form");
if(null!=foundForm){foundElements.push(foundForm)}}}if(null==foundElements||0==foundElements.length||foundElements.length>1){return null
}return foundElements[0]};myfaces._impl._util._Utils.getParent=function(request,context,item,tagNameToSearchFor){try{if("undefined"==typeof item||null==item){throw Error("myfaces._impl._util._Utils.getParen: item is null or undefined,this not allowed")
}var parentItem=("undefined"!=typeof item.parentNode)?item.parentNode:null;if("undefined"!=typeof item.tagName&&null!=item.tagName&&item.tagName.toLowerCase()==tagNameToSearchFor){return item
}while(parentItem!=null&&parentItem.tagName.toLowerCase()!=tagNameToSearchFor){parentItem=parentItem.parentNode
}if(parentItem!=null){return parentItem}else{myfaces._impl.xhrCore._Exception.throwNewWarning(request,context,"Utils","getParent","The item has no parent with type <"+tagNameToSearchFor+"> it might be outside of the parent or generally detached. ");
return null}}catch(e){myfaces._impl.xhrCore._Exception.throwNewError(request,context,"Utils","getParent",e)
}};myfaces._impl._util._Utils.getChild=function(item,childName,itemName){var childItems=item.childNodes;
for(var c=0,cLen=childItems.length;c<cLen;c++){if(childItems[c].tagName!=null&&childItems[c].tagName.toLowerCase()==childName&&(itemName==null||(itemName!=null&&itemName==childItems[c].getAttribute("name")))){return childItems[c]
}}return null};myfaces._impl._util._Utils.getGlobalConfig=function(configName,defaultValue){var _LangUtils=myfaces._impl._util._LangUtils;
if(_LangUtils.exists(myfaces,"config")&&_LangUtils.exists(myfaces.config,configName)){return myfaces.config[configName]
}return defaultValue};myfaces._impl._util._Utils.globalEval=function(code){if(myfaces._impl._util._Utils.browser.isIE&&window.execScript){window.execScript(code);
return }else{if(undefined!=typeof (window.eval)&&null!=window.eval){var func=function(){window.eval.call(window,code)
};func();return }}eval.call(window,code)};myfaces._impl._util._Utils.getLocalOrGlobalConfig=function(localOptions,configName,defaultValue){var _LangUtils=myfaces._impl._util._LangUtils;
var globalOption=myfaces._impl._util._Utils.getGlobalConfig(configName,defaultValue);
if(!_LangUtils.exists(localOptions,"myfaces")||!_LangUtils.exists(localOptions.myfaces,configName)){return globalOption
}return localOptions.myfaces[configName]};myfaces._impl._util._Utils.concatCDATABlocks=function(node){var cDataBlock=[];
for(var i=0;i<node.childNodes.length;i++){cDataBlock.push(node.childNodes[i].data)
}return cDataBlock.join("")};myfaces._impl._util._Utils.browserDetection()}_reserveMyfacesNamespaces();
if(!myfaces._impl._util._LangUtils.exists(myfaces._impl._util,"_HtmlStripper")){myfaces._impl._util._HtmlStripper=function(){};
myfaces._impl._util._HtmlStripper.prototype.BEGIN_TAG="html";myfaces._impl._util._HtmlStripper.prototype.END_TAG="lmth";
myfaces._impl._util._HtmlStripper.prototype.parse=function(theString,tagNameStart,tagNameEnd){this.tokens=theString.split("");
this.tagAttributes={};this._tagStart=-1;this._tagEnd=-1;this._contentStart=-1;this._contentEnd=-1;
this._tokenPos=0;this._tokenForward=1;if("undefined"==typeof tagNameStart||null==tagNameStart){this.tagNameStart=myfaces._impl._util._HtmlStripper.prototype.BEGIN_TAG
}else{this.tagNameStart=tagNameStart}if("undefined"==typeof tagNameEnd||null==tagNameEnd){this.tagNameEnd=this.tagNameStart.split("").reverse().join("")
}else{this.tagNameEnd=tagNameEnd.split("").reverse().join("")}this.handleInstructionBlock();
if(this._contentStart>=0&&this._contentEnd==-1){this._tokenPos=this.tokens.length-1;
this._tokenForward=-1;this.handleEndBlock()}if(this._contentStart>=0&&this._contentEnd==-1){this._contentEnd=this.tokens.length-1
}else{if(this._contentStart==-1){return""}}return this.tokens.slice(this._contentStart,this._contentEnd+1).join("")
};myfaces._impl._util._HtmlStripper.prototype.getContentBlock=function(){return this.tokens.slice(this._contentStart,this._contentEnd+1).join("")
},myfaces._impl._util._HtmlStripper.prototype.getContentTagBlock=function(){return this.tokens.slice(this._tagStart,this._tagEnd+1).join("")
},myfaces._impl._util._HtmlStripper.prototype.getPreTagBlock=function(){return this.tokens.slice(0,this._tagStart).join("")
},myfaces._impl._util._HtmlStripper.prototype.getPostTagBlock=function(){return this.tokens.slice(this._tagEnd,this.tokens.length).join("")
},myfaces._impl._util._HtmlStripper.prototype.handleInstructionBlock=function(){var len=this.tokens.length;
for(;this._contentStart<0&&this._tokenPos<len&&this._tokenPos>=0;this._tokenPos+=this._tokenForward){this._skipBlank();
var token=this._getCurrentToken();if(token=="<"){this.handleDocument()}}};myfaces._impl._util._HtmlStripper.prototype.handleDocument=function(){this._tagStart=this.tokenPos;
this._skipBlank(1);if(this._tokenPos>=this.tokens.length){throw new Error("Document end reached prematurely")
}var token=this._getCurrentToken();switch(token){case"!":this.handleDataBlock();break;
default:this.handleContentTag()}};myfaces._impl._util._HtmlStripper.prototype.handleDataBlock=function(){this._skipBlank(1);
if(this._tokenPos>=this.tokens.length||this._tokenPos<0){return }var token=this.tokens[this._tokenPos];
switch(token){case"-":this.handleComment();break;default:this._getCurrentToken();
this.handleDocDefinition();break}};myfaces._impl._util._HtmlStripper.prototype.handleDocDefinition=function(){this._skipBlank();
if(this._tokenPos>=this.tokens.length||this._tokenPos<0){throw new Error("Document end reached prematurely")
}var len=this.tokens.length;while(this._tokenPos<len&&this._tokenPos>=0){var token=this.tokens[this._tokenPos];
if(token==">"){return }this._tokenPos+=this._tokenForward}};myfaces._impl._util._HtmlStripper.prototype.handleContentTag=function(){this._currentSection=null;
this._skipBlank();var tagName=this._fetchTagname();if(tagName==this.tagNameStart){this.handleIdentifiedContent();
this.tokenPos=this.tokens.length}else{if(tagName=="scri"||tagName=="styl"){this.handleScriptStyle()
}else{this.skipToTagEnd()}}};myfaces._impl._util._HtmlStripper.prototype.handleScriptStyle=function(){this.skipToTagEnd();
if(this.tokens[this._tokenPos-1]=="/"&&this.tokens[this._tokenPos]==">"){return }do{this._skipBlank(1);
var token=this._getCurrentToken();switch(token){case"'"||"'":this.handleString(token);
break;case"/":this.handleJSComment();break}}while(this.tokens[this._tokenPos]!="<");
this.skipToTagEnd()};myfaces._impl._util._HtmlStripper.prototype.handleJSComment=function(){var token=this._getCurrentToken();
var prefetchToken=this.tokens[this._tokenPos+1];var backtrackToken=this.tokens[this._tokenPos-1];
var backtrackToken2=this.tokens[this._tokenPos-2];var backTrackIsComment=backtrackToken!="\\"||(backtrackToken=="\\"&&backtrackToken2=="\\");
if(!backTrackIsComment){return }var singleLineComment=prefetchToken=="/";var multiLineComment=prefetchToken=="*";
if(singleLineComment){while(this._tokenPos<this.tokens.length&&this._getCurrentToken()!="\n"){this._tokenPos++
}}else{if(multiLineComment){this._skipBlank(1);while(this._tokenPos<this.tokens.length){this._skipBlank(1);
token=this._getCurrentToken();prefetchToken=this.tokens[this._tokenPos+1];if(token=="*"&&prefetchToken=="/"){return 
}}}}};myfaces._impl._util._HtmlStripper.prototype.handleEndBlock=function(){for(;
this._tokenPos>=0;this._tokenPos+=this._tokenForward){this._skipBlank(0);var token=this._getCurrentToken();
if(token==">"){this.handleEndTagPart()}}};myfaces._impl._util._HtmlStripper.prototype.handleEndTagPart=function(){this._tagEnd=this._tokenPos;
this._skipBlank(1);if(this._tokenPos<0){throw new Error("Document end reached prematurely")
}var token=this._getCurrentToken();switch(token){case"-":this.handleComment(true);
break;default:this.handleContentEnd()}};myfaces._impl._util._HtmlStripper.prototype.handleContentEnd=function(){var tagFound=false;
var first=true;for(;this._tokenPos>=0;this._skipBlank(1)){if(first&&!tagFound){var tagName=this._fetchTagname();
if(tagName==this.tagNameEnd){tagFound=true}first=false}else{if(tagFound&&this.tokens[this._tokenPos]=="<"){this._contentEnd=this._tokenPos-1;
this._tokenPos=-1;return }else{if(this.tokens[this._tokenPos]=="<"){this._tokenPos+=1;
return }}}}};myfaces._impl._util._HtmlStripper.prototype.skipToTagEnd=function(analyzeAttributes){var token=this._getCurrentToken();
if(!analyzeAttributes){while(token!=">"){if(this._isStringStart()){this._tokenPos+=this._tokenForward;
return this.handleString(token)}this._skipBlank(1);token=this._getCurrentToken()}return null
}var keyValuePairs={};var currentWord=[];var currentKey=null;var openKey=false;var lastKey=null;
while(this.tokens[this._tokenPos]!=">"){var currentWord=this._fetchWord();var token=this._getCurrentToken();
if(token=="="){this._tokenPos+=this._tokenForward;keyValuePairs[currentWord]=this._fetchWord()
}else{keyValuePairs[currentWord]=null}this._tokenPos+=this._tokenForward}return keyValuePairs
};myfaces._impl._util._HtmlStripper.prototype._fetchWord=function(){this._skipBlank(0);
var result=[];var token=this._getCurrentToken();while((!this._isBlank())&&token!="="&&token!=">"){if(this._isStringStart()){this._tokenPos+=this._tokenForward;
return this.handleString(token)}result.push(token);this._tokenPos+=this._tokenForward;
token=this._getCurrentToken()}return result.join("")};myfaces._impl._util._HtmlStripper.prototype._isBlank=function(){var token=this._getCurrentToken();
return token==" "&&token=="\t"&&token=="\n"};myfaces._impl._util._HtmlStripper.prototype.handleIdentifiedContent=function(){this.tagAttributes=this.skipToTagEnd(true);
if(this.tokens[this._tokenPos-1]=="/"&&this.tokens[this._tokenPos]==">"){this._contentStart=-1;
this._contentEnd=-1}else{this._contentStart=this._tokenPos+1}};myfaces._impl._util._HtmlStripper.prototype._isStringStart=function(){var backTrack=(this._tokenPos>0)?this.tokens[this._tokenPos-1]:null;
var token=this.tokens[this._tokenPos];return(token=="'"||token=='"')&&backTrack!="\\"
};myfaces._impl._util._HtmlStripper.prototype.handleString=function(stringToken){var backTrack=null;
var resultString=[];while(this.tokens[this._tokenPos]!=stringToken||backTrack=="\\"){backTrack=this._getCurrentToken();
resultString.push(backTrack);this._tokenPos+=this._tokenForward;if(this._tokenPos>=this.tokens.length){throw Error("Invalid html string opened but not closed")
}}this._getCurrentToken();return resultString.join("")};myfaces._impl._util._HtmlStripper.prototype._assertValues=function(assertValues){for(var loop=0;
loop<assertValues.length;loop++){this._assertValue(assertValues[loop]);this._skipBlank(1)
}};myfaces._impl._util._HtmlStripper.prototype._assertValue=function(expectedToken){var token=this._getCurrentToken();
this._assertLength();if(token!=expectedToken){throw Error("Invalid Token  "+expectedToken+" was expected instead of "+token)
}return token};myfaces._impl._util._HtmlStripper.prototype._assertLength=function(){if(this._tokenPos>=this.tokens.length){throw Error("Invalid html comment opened but not closed")
}};myfaces._impl._util._HtmlStripper.prototype.handleComment=function(reverse){this._assertValues(["-","-"]);
if("undefined"==typeof reverse||null==reverse){reverse=false}while(this._tokenPos<this.tokens.length-3){var token=this._getCurrentToken();
var backTrackBuf=[];if(token=="-"){backTrackBuf.push(token);this._skipBlank(1);token=this._getCurrentToken();
backTrackBuf.push(token);this._skipBlank(1);token=this._getCurrentToken();backTrackBuf.push(token);
if(reverse){this._skipBlank(1);token=this._getCurrentToken();backTrackBuf.push(token)
}backTrackBuf=backTrackBuf.join("");if(reverse&&backTrackBuf=="<!--"){return }else{if(!reverse&&backTrackBuf=="-->"){return 
}}}else{this._skipBlank(1)}}};myfaces._impl._util._HtmlStripper.prototype._getCurrentToken=function(){return this.tokens[this._tokenPos]
};myfaces._impl._util._HtmlStripper.prototype._skipBlank=function(skipVal){var len=this.tokens.length;
if("undefined"==typeof skipVal||null==skipVal){skipVal=0}for(this._tokenPos+=(skipVal*this._tokenForward);
this._tokenPos<len&&this._tokenPos>=0;this._tokenPos+=this._tokenForward){var token=this.tokens[this._tokenPos];
if(token!=" "&&token!="\t"&&token!="\n"){return }}};myfaces._impl._util._HtmlStripper.prototype._fetchTagname=function(){var tagName=[];
tagName.push(this.tokens[this._tokenPos]);this._tokenPos+=this._tokenForward;tagName.push(this._getCurrentToken());
this._tokenPos+=this._tokenForward;tagName.push(this._getCurrentToken());this._tokenPos+=this._tokenForward;
tagName.push(this._getCurrentToken());this._tokenPos+=this._tokenForward;tagName=tagName.join("").toLowerCase();
return tagName}}_reserveMyfacesNamespaces();if(!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore,"_Exception")){myfaces._impl.xhrCore._Exception=function(sourceClass,threshold){this.m_class=sourceClass;
this.m_threshold=threshold};myfaces._impl.xhrCore._Exception.throwNewError=function(request,context,sourceClass,func,exception){var newException=new myfaces._impl.xhrCore._Exception(request,context,sourceClass,"ERROR");
newException.throwError(request,context,func,exception)};myfaces._impl.xhrCore._Exception.throwNewWarning=function(request,context,sourceClass,func,message){var newException=new myfaces._impl.xhrCore._Exception(request,context,sourceClass,"WARNING");
newException.throwWarning(request,context,func,message)};myfaces._impl.xhrCore._Exception.prototype.throwError=function(request,context,func,exception){if(this.m_threshold=="ERROR"){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR,exception.name,"MyFaces ERROR\nAffected Class: "+this.m_class+"\nAffected Method: "+func+"\nError name: "+exception.name+"\nError message: "+exception.message+"\nError description: "+exception.description+"\nError number: "+exception.number+"\nError line number: "+exception.lineNumber)
}this.destroy();throw exception};myfaces._impl.xhrCore._Exception.prototype.throwWarning=function(request,context,func,message){if(this.m_threshold=="WARNING"||this.m_threshold=="ERROR"){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR,exception.name,"MyFaces WARNING\n["+this.m_class+"::"+func+"]\n\n"+message)
}this.destroy()};myfaces._impl.xhrCore._Exception.prototype.destroy=function(){if(myfaces._impl.xhrCore._AjaxRequestQueue.queue&&myfaces._impl.xhrCore._AjaxRequestQueue.queue!=null){myfaces._impl.xhrCore._AjaxRequestQueue.queue.clearQueue()
}}}_reserveMyfacesNamespaces();if(!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore,"_AjaxUtils")){myfaces._impl.xhrCore._AjaxUtils=function(alarmThreshold){this.alarmThreshold=alarmThreshold;
this.m_exception=new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxUtils",this.alarmThreshold)
};myfaces._impl.xhrCore._AjaxUtils.prototype.processUserEntries=function(request,context,item,parentItem,partialIds){try{var form=parentItem;
if(form==null){this.m_exception.throwWarning(request,context,"processUserEntries","Html-Component is not nested in a Form-Tag");
return null}var stringBuffer=new Array();if(partialIds!=null&&partialIds.length>0){this.addNodes(form,false,partialIds,stringBuffer)
}else{var eLen=form.elements.length;for(var e=0;e<eLen;e++){this.addField(form.elements[e],stringBuffer)
}}if("undefined"!=typeof item&&null!=item&&item.type!=null&&item.type.toLowerCase()=="submit"){stringBuffer[stringBuffer.length]=encodeURIComponent(item.name);
stringBuffer[stringBuffer.length]="=";stringBuffer[stringBuffer.length]=encodeURIComponent(item.value);
stringBuffer[stringBuffer.length]="&"}return stringBuffer.join("")}catch(e){alert(e);
this.m_exception.throwError(request,context,"processUserEntries",e)}};myfaces._impl.xhrCore._AjaxUtils.prototype.addNodes=function(node,insideSubmittedPart,partialIds,stringBuffer){if(node!=null&&node.childNodes!=null){var nLen=node.childNodes.length;
for(var i=0;i<nLen;i++){var child=node.childNodes[i];var id=child.id;var elementName=child.name;
if(child.nodeType==1){var isPartialSubmitContainer=((id!=null)&&myfaces._impl._util._LangUtils.arrayContains(partialIds,id));
if(insideSubmittedPart||isPartialSubmitContainer||(elementName!=null&&elementName==myfaces._impl.core._jsfImpl._PROP_VIEWSTATE)){this.addField(child,stringBuffer);
if(insideSubmittedPart||isPartialSubmitContainer){this.addNodes(child,true,partialIds,stringBuffer)
}}else{this.addNodes(child,false,partialIds,stringBuffer)}}}}};myfaces._impl.xhrCore._AjaxUtils.prototype.addField=function(element,stringBuffer){var elementName=element.name;
var elementTagName=element.tagName.toLowerCase();var elementType=element.type;if(elementType!=null){elementType=elementType.toLowerCase()
}if(((elementTagName=="input"||elementTagName=="textarea"||elementTagName=="select")&&(elementName!=null&&elementName!=""))&&element.disabled==false){if(elementTagName=="select"){if(element.selectedIndex>=0){var uLen=element.options.length;
for(var u=0;u<uLen;u++){if(element.options[u].selected==true){var elementOption=element.options[u];
stringBuffer[stringBuffer.length]=encodeURIComponent(elementName);stringBuffer[stringBuffer.length]="=";
if(elementOption.getAttribute("value")!=null){stringBuffer[stringBuffer.length]=encodeURIComponent(elementOption.value)
}else{stringBuffer[stringBuffer.length]=encodeURIComponent(elementOption.text)}stringBuffer[stringBuffer.length]="&"
}}}}if((elementTagName!="select"&&elementType!="button"&&elementType!="reset"&&elementType!="submit"&&elementType!="image")&&((elementType!="checkbox"&&elementType!="radio")||element.checked)){stringBuffer[stringBuffer.length]=encodeURIComponent(elementName);
stringBuffer[stringBuffer.length]="=";stringBuffer[stringBuffer.length]=encodeURIComponent(element.value);
stringBuffer[stringBuffer.length]="&"}}}}_reserveMyfacesNamespaces();if(!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore,"_AjaxRequestQueue")){myfaces._impl.xhrCore._AjaxRequestQueue=function(){this.m_request=null;
this.m_queuedRequests=[];this.m_exception=new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxRequestQueue","NONE")
};myfaces._impl.xhrCore._AjaxRequestQueue.queue=new myfaces._impl.xhrCore._AjaxRequestQueue();
myfaces._impl.xhrCore._AjaxRequestQueue.handleCallback=function(){if(myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request!=null){myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.requestCallback()
}else{myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_exception.throwWarning(null,null,"doRequestCallback","No request object available")
}};myfaces._impl.xhrCore._AjaxRequestQueue.prototype.queueRequest=function(request){if(typeof request.m_delay=="number"){this.clearDelayTimeout();
this.delayTimeoutId=window.setTimeout(function(){myfaces._impl.xhrCore._AjaxRequestQueue.queue.clearDelayTimeout();
myfaces._impl.xhrCore._AjaxRequestQueue.queue.queueNow(request)},request.m_delay)
}else{this.queueNow(request)}};myfaces._impl.xhrCore._AjaxRequestQueue.prototype.clearDelayTimeout=function(){try{if(typeof this.delayTimeoutId=="number"){window.clearTimeout(this.delayTimeoutId);
delete this.delayTimeoutId}}catch(e){}};myfaces._impl.xhrCore._AjaxRequestQueue.prototype.queueNow=function(request){if(this.m_request==null){this.m_request=request;
this.m_request.send()}else{this.m_queuedRequests.push(request);if(request.m_queuesize>-1&&request.m_queuesize<this.m_queuedRequests.length){this.m_queuedRequests.shift()
}}};myfaces._impl.xhrCore._AjaxRequestQueue.prototype.processQueue=function(){if(this.m_queuedRequests.length>0){this.m_request=this.m_queuedRequests.shift();
this.m_request.send()}else{this.m_request=null}};myfaces._impl.xhrCore._AjaxRequestQueue.prototype.clearQueue=function(){this.m_request=null;
this.m_queuedRequest=null;this.m_requestPending=false}}_reserveMyfacesNamespaces();
if(!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore,"_AjaxRequest")){myfaces._impl.xhrCore._AjaxRequest=function(source,sourceForm,context,passThrough){this.m_exception=new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxRequest",this.alarmThreshold);
try{this.m_contentType="application/x-www-form-urlencoded";this.m_source=source;this.m_xhr=null;
this.m_partialIdsArray=null;var errorlevel="NONE";this.m_queuesize=-1;var _Utils=myfaces._impl._util._Utils;
var _LangUtils=myfaces._impl._util._LangUtils;if(_Utils.getLocalOrGlobalConfig(context,"errorlevel",null)!=null){errorlevel=context.myfaces.errorlevel
}if(_Utils.getLocalOrGlobalConfig(context,"queuesize",null)!=null){this.m_queuesize=context.myfaces.queuesize
}if(_Utils.getLocalOrGlobalConfig(context,"pps",null)!=null&&_LangUtils.exists(passThrough,myfaces._impl.core._jsfImpl._PROP_EXECUTE)&&passThrough[myfaces._impl.core._jsfImpl._PROP_EXECUTE].length>0){this.m_partialIdsArray=passThrough[myfaces._impl.core._jsfImpl._PROP_EXECUTE].split(" ")
}if(_Utils.getLocalOrGlobalConfig(context,"timeout",null)!=null){this.m_timeout=context.myfaces.timeout
}if(_Utils.getLocalOrGlobalConfig(context,"delay",null)!=null){this.m_delay=context.myfaces.delay
}this.m_context=context;this.m_response=new myfaces._impl.xhrCore._AjaxResponse(errorlevel);
this.m_ajaxUtil=new myfaces._impl.xhrCore._AjaxUtils(errorlevel);this.m_sourceForm=sourceForm;
this.m_passThrough=passThrough;this.m_requestParameters=this.getViewState();for(var key in this.m_passThrough){this.m_requestParameters=this.m_requestParameters+"&"+encodeURIComponent(key)+"="+encodeURIComponent(this.m_passThrough[key])
}}catch(e){this.m_exception.throwError(null,context,"Ctor",e)}};myfaces._impl.xhrCore._AjaxRequest.prototype.send=function(){try{this.m_xhr=myfaces._impl._util._Utils.getXHRObject();
this.m_xhr.open("POST",this.m_sourceForm.action,true);this.m_xhr.setRequestHeader("Content-Type",this.m_contentType);
this.m_xhr.setRequestHeader("Faces-Request","partial/ajax");this.m_xhr.onreadystatechange=myfaces._impl.xhrCore._AjaxRequestQueue.handleCallback;
myfaces.ajax.sendEvent(this.m_xhr,this.m_context,myfaces._impl.core._jsfImpl._AJAX_STAGE_BEGIN);
this.m_xhr.send(this.m_requestParameters);if("undefined"!=typeof this.m_timeout){var timeoutId=window.setTimeout(function(){try{if(myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.m_xhr.readyState>0&&myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.m_xhr.readyState<4){myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.m_xhr.abort()
}}catch(e){}},this.m_timeout)}}catch(e){this.m_exception.throwError(this.m_xhr,this.m_context,"send",e)
}};myfaces._impl.xhrCore._AjaxRequest.prototype.requestCallback=function(){var READY_STATE_DONE=4;
try{if(this.m_xhr.readyState==READY_STATE_DONE){if(this.m_xhr.status>=200&&this.m_xhr.status<300){myfaces.ajax.sendEvent(this.m_xhr,this.m_context,myfaces._impl.core._jsfImpl._AJAX_STAGE_COMPLETE);
myfaces.ajax.response(this.m_xhr,this.m_context);myfaces.ajax.sendEvent(this.m_xhr,this.m_context,myfaces._impl.core._jsfImpl._AJAX_STAGE_SUCCESS);
myfaces._impl.xhrCore._AjaxRequestQueue.queue.processQueue()}else{myfaces.ajax.sendEvent(this.m_xhr,this.m_context,myfaces._impl.core._jsfImpl._AJAX_STAGE_COMPLETE);
var errorText;try{errorText="Request failed";if(this.m_xhr.status){errorText+="with status "+this.m_xhr.status;
if(this.m_xhr.statusText){errorText+=" and reason "+this.m_xhr.statusText}}}catch(e){errorText="Request failed with unknown status"
}myfaces.ajax.sendError(this.m_xhr,this.m_context,myfaces._impl.core._jsfImpl._ERROR_HTTPERROR,myfaces._impl.core._jsfImpl._ERROR_HTTPERROR,errorText)
}}}catch(e){this.m_exception.throwError(this.m_xhr,this.m_context,"requestCallback",e)
}};myfaces._impl.xhrCore._AjaxRequest.prototype.getViewState=function(){return this.m_ajaxUtil.processUserEntries(this.m_xhr,this.m_context,this.m_source,this.m_sourceForm,this.m_partialIdsArray)
}}_reserveMyfacesNamespaces();if(!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore,"_AjaxResponse")){myfaces._impl.xhrCore._AjaxResponse=function(alarmThreshold){this.alarmThreshold=alarmThreshold;
this.m_exception=new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxResponse",this.alarmThreshold)
};myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSE_PARTIAL="partial-response";
myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSETYPE_ERROR="error";myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSETYPE_REDIRECT="redirect";
myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSETYPE_REDIRECT="changes";myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_CHANGES="changes";
myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_UPDATE="update";myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_DELETE="delete";
myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_INSERT="insert";myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_EVAL="eval";
myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_ERROR="error";myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_ATTRIBUTES="attributes";
myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_EXTENSION="extension";myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_REDIRECT="redirect";
myfaces._impl.xhrCore._AjaxResponse.prototype.processResponse=function(request,context){try{if("undefined"==typeof (request)||null==request){throw Exception("jsf.ajaxResponse: The response cannot be null or empty!")
}if(!myfaces._impl._util._LangUtils.exists(request,"responseXML")){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_EMPTY_RESPONSE);
return }var xmlContent=request.responseXML;if(xmlContent.firstChild.tagName=="parsererror"){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML);
return }var partials=xmlContent.childNodes[0];if("undefined"==typeof partials||partials==null){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML);
return }else{if(partials.tagName!=this._RESPONSE_PARTIAL){partials=partials.nextSibling;
if("undefined"==typeof partials||partials==null||partials.tagName!=this._RESPONSE_PARTIAL){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML);
return }}}var childNodesLength=partials.childNodes.length;for(var loop=0;loop<childNodesLength;
loop++){var childNode=partials.childNodes[loop];var tagName=childNode.tagName;if(tagName==this._PCMD_ERROR){this.processError(request,context,childNode);
return }else{if(tagName==this._PCMD_REDIRECT){if(!this.processRedirect(request,context,childNode)){return 
}}else{if(tagName==this._PCMD_CHANGES){if(!this.processChanges(request,context,childNode)){return 
}}}}}}catch(e){this.m_exception.throwError(request,context,"processResponse",e)}};
myfaces._impl.xhrCore._AjaxResponse.prototype.processError=function(request,context,node){var errorName=node.firstChild.textContent;
var errorMessage=node.childNodes[1].firstChild.data;if("undefined"==typeof errorName||null==errorName){errorName=""
}if("undefined"==typeof errorMessage||null==errorMessage){errorMessage=""}myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_SERVER_ERROR,errorName,errorMessage)
};myfaces._impl.xhrCore._AjaxResponse.prototype.processRedirect=function(request,context,node){var redirectUrl=node.getAttribute("url");
if("undefined"==typeof redirectUrl||null==redirectUrl){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Redirect without url");
return false}redirectUrl=myfaces._impl._util._LangUtils.trim(redirectUrl);if(redirectUrl==""){return false
}window.location=redirectUrl;return true};myfaces._impl.xhrCore._AjaxResponse.prototype.processChanges=function(request,context,node){var changes=node.childNodes;
for(var i=0;i<changes.length;i++){if(changes[i].tagName=="update"){if(!this.processUpdate(request,context,changes[i])){return false
}}else{if(changes[i].tagName==this._PCMD_EVAL){myfaces._impl._util._Utils.globalEval(changes[i].firstChild.data)
}else{if(changes[i].tagName==this._PCMD_INSERT){if(!this.processInsert(request,context,changes[i])){return false
}}else{if(changes[i].tagName==this._PCMD_DELETE){if(!this.processDelete(request,context,changes[i])){return false
}}else{if(changes[i].tagName==this._PCMD_ATTRIBUTES){if(!this.processAttributes(request,context,changes[i])){return false
}}else{if(changes[i].tagName==this._PCMD_EXTENSION){}else{myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML);
return false}}}}}}}return true};myfaces._impl.xhrCore._AjaxResponse.prototype.processUpdate=function(request,context,node){if(node.getAttribute("id")=="javax.faces.ViewState"){var sourceForm=myfaces._impl._util._Utils.fuzzyFormDetection(null,context,context.source);
if(null!=sourceForm){var element=myfaces._impl._util._Utils.getElementFromForm(request,context,"javax.faces.ViewState",sourceForm,true,true);
if(null==element){element=document.createElement("input");myfaces._impl._util._Utils.setAttribute(element,"type","hidden");
myfaces._impl._util._Utils.setAttribute(element,"name","javax.faces.ViewState");sourceForm.appendChild(element)
}myfaces._impl._util._Utils.setAttribute(element,"value",node.firstChild.nodeValue)
}}else{var cDataBlock=myfaces._impl._util._Utils.concatCDATABlocks(node);switch(node.getAttribute("id")){case"javax.faces.ViewRoot":this._replaceBody(request,context,cDataBlock);
break;case"javax.faces.ViewHead":throw new Error("Head cannot be replaced, due to browser deficiencies!");
break;case"javax.faces.ViewBody":this._replaceBody(request,context,cDataBlock);break;
default:this._replaceElement(request,context,node.getAttribute("id"),cDataBlock);
break}}return true};myfaces._impl.xhrCore._AjaxResponse.prototype._replaceBody=function(request,context,newData){var parser=myfaces.ajax._impl=new (myfaces._impl._util._Utils.getGlobalConfig("updateParser",myfaces._impl._util._HtmlStripper))();
var oldBody=document.getElementsByTagName("body")[0];var newBody=document.createElement("body");
var placeHolder=document.createElement("div");placeHolder.id="myfaces_bodyplaceholder";
var bodyParent=oldBody.parentNode;newBody.appendChild(placeHolder);var bodyData=parser.parse(newData,"body");
bodyParent.replaceChild(newBody,oldBody);this._replaceElement(request,context,placeHolder,bodyData);
for(var key in parser.tagAttributes){var value=parser.tagAttributes[key];myfaces._impl._util._Utils.setAttribute(newBody,key,value)
}};myfaces._impl.xhrCore._AjaxResponse.prototype._replaceElement=function(request,context,oldElement,newData){myfaces._impl._util._Utils.replaceHtmlItem(request,context,oldElement,newData,this.m_htmlFormElement)
};myfaces._impl.xhrCore._AjaxResponse.prototype.processInsert=function(request,context,node){var insertId=node.getAttribute("id");
var beforeId=node.getAttribute("before");var afterId=node.getAttribute("after");var insertSet="undefined"!=typeof insertId&&null!=insertId&&myfaces._impl._util._LangUtils.trim(insertId)!="";
var beforeSet="undefined"!=typeof beforeId&&null!=beforeId&&myfaces._impl._util._LangUtils.trim(beforeId)!="";
var afterSet="undefined"!=typeof afterId&&null!=afterId&&myfaces._impl._util._LangUtils.trim(afterId)!="";
if(!insertSet){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Error in PPR Insert, id must be present");
return false}if(!(beforeSet||afterSet)){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Error in PPR Insert, before id or after id must be present");
return false}var nodeHolder=null;var parentNode=null;var cDataBlock=myfaces._impl._util._Utils.concatCDATABlocks(node);
if(beforeSet){beforeId=myfaces._impl._util._LangUtils.trim(beforeId);var beforeNode=document.getElementById(beforeId);
if("undefined"==typeof beforeNode||null==beforeNode){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Error in PPR Insert, before  node of id "+beforeId+" does not exist in document");
return false}nodeHolder=document.createElement("div");parentNode=beforeNode.parentNode;
parentNode.insertBefore(nodeHolder,beforeNode);myfaces._impl._util._Utils.replaceHtmlItem(request,context,nodeHolder,cDataBlock,null)
}else{afterId=myfaces._impl._util._LangUtils.trim(afterId);var afterNode=document.getElementById(afterId);
if("undefined"==typeof afterNode||null==afterNode){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Error in PPR Insert, after  node of id "+after+" does not exist in document");
return false}nodeHolder=document.createElement("div");parentNode=afterNode.parentNode;
parentNode.insertBefore(nodeHolder,afterNode.nextSibling);myfaces._impl._util._Utils.replaceHtmlItem(request,context,nodeHolder,cDataBlock,null)
}return true};myfaces._impl.xhrCore._AjaxResponse.prototype.processDelete=function(request,context,node){var deleteId=node.getAttribute("id");
if("undefined"==typeof deleteId||null==deleteId){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Error in delete, id not in xml markup");
return false}myfaces._impl._util._Utils.deleteItem(request,context,deleteId,"","");
return true};myfaces._impl.xhrCore._AjaxResponse.prototype.processAttributes=function(request,context,node){var attributesRoot=node;
var elementId=attributesRoot.getAttribute("id");if("undefined"==typeof elementId||null==elementId){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Error in attributes, id not in xml markup");
return false}var childs=attributesRoot.childNodes;if("undefined"==typeof childs||null==childs){return false
}for(var loop2=0;loop2<childs.length;loop2++){var attributesNode=childs[loop2];var attributeName=attributesNode.getAttribute("name");
var attributeValue=attributesNode.getAttribute("value");if("undefined"==typeof attributeName||null==attributeName){continue
}attributeName=myfaces._impl._util._LangUtils.trim(attributeName);if("undefined"==typeof attributeValue||null==attributeValue){attributeValue=""
}switch(elementId){case"javax.faces.ViewRoot":throw new Error("Changing of viewRoot attributes is not supported");
break;case"javax.faces.ViewBody":throw new Error("Changing of head attributes is not supported");
break;case"javax.faces.ViewHead":var element=document.getElementsByTagName("body")[0];
myfaces._impl._util._Utils.setAttribute(element,attributeName,attributeValue);break;
default:myfaces._impl._util._Utils.setAttribute(document.getElementById(elementId),attributeName,attributeValue);
break}}return true}}_reserveMyfacesNamespaces();if(!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore,"_Ajax")){myfaces._impl.xhrCore._Ajax=function(){};
myfaces._impl.xhrCore._Ajax.prototype.getViewState=function(FORM_ELEMENT){return myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.getViewState()
};myfaces._impl.xhrCore._Ajax.prototype._ajaxRequest=function(source,sourceForm,context,passThroughValues){myfaces._impl.xhrCore._AjaxRequestQueue.queue.queueRequest(new myfaces._impl.xhrCore._AjaxRequest(source,sourceForm,context,passThroughValues))
};myfaces._impl.xhrCore._Ajax.prototype._ajaxResponse=function(request,context){myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.m_response.processResponse(request,context)
}}_reserveMyfacesNamespaces();if(!myfaces._impl._util._LangUtils.exists(myfaces._impl.core,"_jsfImpl")){myfaces._impl.core._jsfImpl=function(){this._requestHandler=new (myfaces._impl._util._Utils.getGlobalConfig("transport",myfaces._impl.xhrCore._Ajax))();
this._eventListenerQueue=new (myfaces._impl._util._Utils.getGlobalConfig("eventListenerQueue",myfaces._impl._util._ListenerQueue))();
this._errorListenerQueue=new (myfaces._impl._util._Utils.getGlobalConfig("errorListenerQueue",myfaces._impl._util._ListenerQueue))()
};myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_ALL="@all";myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_NONE="@none";
myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_THIS="@this";myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_FORM="@form";
myfaces._impl.core._jsfImpl._PROP_PARTIAL_SOURCE="javax.faces.source";myfaces._impl.core._jsfImpl._PROP_VIEWSTATE="javax.faces.ViewState";
myfaces._impl.core._jsfImpl._PROP_AJAX="javax.faces.partial.ajax";myfaces._impl.core._jsfImpl._PROP_EXECUTE="javax.faces.partial.execute";
myfaces._impl.core._jsfImpl._PROP_RENDER="javax.faces.partial.render";myfaces._impl.core._jsfImpl._PROP_EVENT="javax.faces.partial.event";
myfaces._impl.core._jsfImpl._MSG_TYPE_ERROR="error";myfaces._impl.core._jsfImpl._MSG_TYPE_EVENT="event";
myfaces._impl.core._jsfImpl._AJAX_STAGE_BEGIN="begin";myfaces._impl.core._jsfImpl._AJAX_STAGE_COMPLETE="complete";
myfaces._impl.core._jsfImpl._AJAX_STAGE_SUCCESS="success";myfaces._impl.core._jsfImpl._ERROR_HTTPERROR="httpError";
myfaces._impl.core._jsfImpl._ERROR_EMPTY_RESPONSE="emptyResponse";myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML="malformedXML";
myfaces._impl.core._jsfImpl._ERROR_SERVER_ERROR="serverError";myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR="clientError";
myfaces._impl.core._jsfImpl.prototype.getViewState=function(formElement){if("undefined"==typeof (formElement)||null==formElement||"undefined"==typeof (formElement.nodeName)||null==formElement.nodeName||formElement.nodeName.toLowerCase()!="form"){throw new Error("jsf.viewState: param value not of type form!")
}return this._requestHandler.getViewState(formElement)};myfaces._impl.core._jsfImpl.prototype._assertElement=function(element){var JSF2Utils=myfaces._impl._util._LangUtils;
if("undefined"==typeof (element)||null==element){throw new Error("jsf.ajax, element must be set!")
}element=JSF2Utils.byId(element);if("undefined"==typeof element||null==element){throw new Error("Element either must be a string to a or must be a valid dom node")
}};myfaces._impl.core._jsfImpl.prototype._assertFunction=function(func){if("undefined"==typeof func||null==func){return 
}if(!(func instanceof Function)){throw new Error("Functioncall "+func+" is not a function! ")
}};myfaces._impl.core._jsfImpl.prototype.request=function(element,event,options){var JSF2Utils=myfaces._impl._util._LangUtils;
var elementId=null;element=JSF2Utils.byId(element);if("undefined"!=typeof element&&null!=element){elementId=("undefined"!=typeof element.id)?element.id:null;
if((elementId==null||elementId=="")&&"undefined"!=typeof element.name){elementId=element.name
}}this._assertFunction(options.onerror);this._assertFunction(options.onevent);var passThroughArguments=JSF2Utils.mixMaps({},options,true);
passThroughArguments.onevent=null;delete passThroughArguments.onevent;passThroughArguments.onerror=null;
delete passThroughArguments.onerror;if("undefined"!=typeof event&&null!=event){passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EVENT]=event.type
}var ajaxContext={};ajaxContext.source=element;ajaxContext.onevent=options.onevent;
ajaxContext.onerror=options.onerror;var sourceForm=myfaces._impl._util._Utils.fuzzyFormDetection(null,ajaxContext,element);
if(null==sourceForm){throw Error("Sourcform could not be determined, either because element is not attached to a form or we have multiple forms with named elements of the same identifier or name, stopping the ajax processing")
}passThroughArguments[myfaces._impl.core._jsfImpl._PROP_PARTIAL_SOURCE]=elementId;
passThroughArguments[myfaces._impl.core._jsfImpl._PROP_AJAX]=true;if(JSF2Utils.exists(passThroughArguments,"execute")){var execString=JSF2Utils.arrayToString(passThroughArguments.execute," ");
var execNone=execString.indexOf(this._OPT_IDENT_NONE)!=-1;var execAll=execString.indexOf(this._OPT_IDENT_ALL)!=-1;
if(!execNone&&!execAll){execString=execString.replace(this._OPT_IDENT_FORM,sourceForm.id);
execString=execString.replace(this._OPT_IDENT_THIS,elementId);passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EXECUTE]=execString
}else{if(execAll){passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EXECUTE]=this._OPT_IDENT_ALL
}}passThroughArguments.execute=null;delete passThroughArguments.execute}else{passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EXECUTE]=elementId
}if(JSF2Utils.exists(passThroughArguments,"render")){var renderString=JSF2Utils.arrayToString(passThroughArguments.render," ");
var renderNone=renderString.indexOf(this._OPT_IDENT_NONE)!=-1;var renderAll=renderString.indexOf(this._OPT_IDENT_ALL)!=-1;
if(!renderNone&&!renderAll){renderString=renderString.replace(this._OPT_IDENT_FORM,sourceForm.id);
renderString=renderString.replace(this._OPT_IDENT_THIS,elementId);passThroughArguments[myfaces._impl.core._jsfImpl._PROP_RENDER]=renderString;
passThroughArguments.render=null}else{if(renderAll){passThroughArguments[myfaces._impl.core._jsfImpl._PROP_RENDER]=this._OPT_IDENT_ALL
}}delete passThroughArguments.render}if("undefined"!=typeof passThroughArguments.myfaces&&null!=passThroughArguments.myfaces){ajaxContext.myfaces=passThroughArguments.myfaces;
delete passThroughArguments.myfaces}this._requestHandler._ajaxRequest(element,sourceForm,ajaxContext,passThroughArguments)
};myfaces._impl.core._jsfImpl.prototype.addOnError=function(errorListener){this._errorListenerQueue.add(errorListener)
};myfaces._impl.core._jsfImpl.prototype.addOnEvent=function(eventListener){this._eventListenerQueue.add(eventListener)
};myfaces._impl.core._jsfImpl.prototype.sendError=function sendError(request,context,name,serverErrorName,serverErrorMessage){var eventData={};
eventData.type=myfaces._impl.core._jsfImpl._MSG_TYPE_ERROR;eventData.status=name;
eventData.serverErrorName=serverErrorName;eventData.serverErrorMessage=serverErrorMessage;
try{eventData.source=context.source;eventData.responseXML=request.responseXML;eventData.responseText=request.responseText;
eventData.responseCode=request.status}catch(e){}if(myfaces._impl._util._LangUtils.exists(context,"onerror")){context.onerror(eventData)
}this._errorListenerQueue.broadcastEvent(eventData);if(jsf.getProjectStage()==="Development"){var defaultErrorOutput=myfaces._impl._util._Utils.getGlobalConfig("defaultErrorOutput",alert);
var finalMessage=[];finalMessage.push(("undefined"!=typeof name&&null!=name)?name:"");
finalMessage.push(("undefined"!=typeof serverErrorName&&null!=serverErrorName)?serverErrorName:"");
finalMessage.push(("undefined"!=typeof serverErrorMessage&&null!=serverErrorMessage)?serverErrorMessage:"");
defaultErrorOutput(finalMessage.join("-"))}};myfaces._impl.core._jsfImpl.prototype.sendEvent=function sendEvent(request,context,name){var eventData={};
eventData.type=myfaces._impl.core._jsfImpl._MSG_TYPE_EVENT;eventData.status=name;
eventData.source=context.source;if(name!==myfaces._impl.core._jsfImpl._AJAX_STAGE_BEGIN){try{eventData.responseXML=request.responseXML;
eventData.responseText=request.responseText;eventData.responseCode=request.status
}catch(e){myfaces.ajax.sendError(request,context,myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR,"ErrorRetrievingResponse","Parts of the response couldn't be retrieved when constructing the event data: "+e);
throw e}}if(myfaces._impl._util._LangUtils.exists(context,"onevent")){context.onevent.call(null,eventData)
}this._eventListenerQueue.broadcastEvent(eventData)};myfaces._impl.core._jsfImpl.prototype.response=function(request,context){this._requestHandler._ajaxResponse(request,context)
};myfaces._impl.core._jsfImpl.prototype.getProjectStage=function(){var scriptTags=document.getElementsByTagName("script");
for(var i=0;i<scriptTags.length;i++){if(scriptTags[i].src.search(/\/javax\.faces\.resource\/jsf\.js.*ln=javax\.faces/)!=-1){var result=scriptTags[i].src.match(/stage=([^&;]*)/);
if(result){if(result[1]=="Production"||result[1]=="Development"||result[1]=="SystemTest"||result[1]=="UnitTest"){return result[1]
}}else{return"Production"}}}return"Production"};myfaces._impl.core._jsfImpl.prototype.chain=function(source,event){var len=arguments.length;
if(len<2){throw new Error(" an event object or unknown must be passed as second parameter ")
}else{if(len<3){if("function"==typeof event||myfaces._impl._util._LangUtils.isString(event)){throw new Error(" an event must be passed down (either a an event object null or undefined) ")
}return true}}if("undefined"==typeof source){throw new Error(" source must be defined")
}else{if("function"==typeof source){throw new Error(" source cannot be a function (probably source and event were not defined or set to null")
}}if(myfaces._impl._util._LangUtils.isString(source)){throw new Error(" source cannot be a string ")
}var thisVal=source;if("function"==typeof event||myfaces._impl._util._LangUtils.isString(event)){throw new Error(" an event must be passed down (either a an event object null or undefined) ")
}for(var loop=2;loop<len;loop++){var retVal;if("function"==typeof arguments[loop]){retVal=arguments[loop].call(thisVal,event)
}else{retVal=new Function("event",arguments[loop]).call(thisVal,event)}if("undefined"!=typeof retVal&&retVal===false){return false
}}return true};myfaces.ajax=new myfaces._impl.core._jsfImpl()}if("undefined"!=typeof OpenAjax&&("undefined"==typeof jsf||null==typeof jsf)){OpenAjax.hub.registerLibrary("jsf","www.sun.com","1.0",null)
}if("undefined"==typeof jsf||null==jsf){jsf=new Object();jsf.specversion=200000;jsf.implversion=0;
jsf.getProjectStage=function(){var impl=myfaces._impl._util._Utils.getGlobalConfig("jsfAjaxImpl",myfaces.ajax);
return impl.getProjectStage()};jsf.getViewState=function(formElement){var impl=myfaces._impl._util._Utils.getGlobalConfig("jsfAjaxImpl",myfaces.ajax);
return impl.getViewState(formElement)}}if("undefined"==typeof jsf.ajax||null==jsf.ajax){jsf.ajax=new Object();
jsf.ajax.request=function(element,event,options){if(!options){options={}}var impl=myfaces._impl._util._Utils.getGlobalConfig("jsfAjaxImpl",myfaces.ajax);
return impl.request(element,event,options)};jsf.ajax.addOnError=function(errorListener){var impl=myfaces._impl._util._Utils.getGlobalConfig("jsfAjaxImpl",myfaces.ajax);
return impl.addOnError(errorListener)};jsf.ajax.addOnEvent=function(eventListener){var impl=myfaces._impl._util._Utils.getGlobalConfig("jsfAjaxImpl",myfaces.ajax);
return impl.addOnEvent(eventListener)};jsf.ajax.response=function(request,context){var impl=myfaces._impl._util._Utils.getGlobalConfig("jsfAjaxImpl",myfaces.ajax);
return impl.response(request,context)}}if("undefined"==typeof jsf.util||null==jsf.util){jsf.util=new Object();
jsf.util.chain=function(source,event){var impl=myfaces._impl._util._Utils.getGlobalConfig("jsfAjaxImpl",myfaces.ajax);
return impl.chain.apply(jsf.ajax._impl,arguments)}}