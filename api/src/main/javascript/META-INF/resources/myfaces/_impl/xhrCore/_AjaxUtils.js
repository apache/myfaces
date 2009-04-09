/*
 * Copyright 2009 Ganesh Jung
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Ganesh Jung (latest modification by $Author: werpu $)
 * Version: $Revision: 1.2 $ $Date: 2009/04/09 13:02:00 $
 *
 */

_reserveMyfacesNamespaces();


/**
 * Constructor
 * @param {String} alarmThreshold - Error Level
 */
myfaces._impl.xhrCore_AjaxUtils = function(alarmThreshold) {
	// Exception Objekt
	this.alarmThreshold = alarmThreshold;
	this.PARTIAL_PREFIX = "_j4fry_partial_submit_";
	this.m_exception = new myfaces._impl.xhrCore_Exception("myfaces._impl.xhrCore_AjaxUtils", this.alarmThreshold);
};

/**
 * determines fields to submit
 * @param {HtmlElement} item - item that triggered the event
 * @param {HtmlElement} parentItem - form element item is nested in
 * @param {Array} partialIds - ids fo PPS
 */
myfaces._impl.xhrCore_AjaxUtils.prototype.processUserEntries = function(item,
		parentItem,	partialIds) {
	try {
		var form = parentItem;

		if (form == null) {
			this.m_exception.throwWarning("processUserEntries",
					"Html-Component is not nested in a Form-Tag");
			return null;
		}

		var stringBuffer = new Array();

		if (partialIds != null && partialIds.length > 0) {
			// recursivly check items
			this.addNodes(form, false, partialIds, stringBuffer);
		} else {
			// add all nodes
			var eLen = form.elements.length;
			for ( var e = 0; e < eLen; e++) {
				this.addField(form.elements[e], stringBuffer);
			} // end of for (formElements)
		}

		// if triggered by a Button send it along
		if (item.type != null && item.type.toLowerCase() == "submit") {
			stringBuffer[stringBuffer.length] = encodeURIComponent(item.name);
			stringBuffer[stringBuffer.length] = "=";
			stringBuffer[stringBuffer.length] = encodeURIComponent(item.value);
			stringBuffer[stringBuffer.length] = "&";
		}

		return stringBuffer.join("");
	} catch (e) {
		alert(e);
		this.m_exception.throwError("processUserEntries", e);
	}
};

/**
 * checks recursively if contained in PPS
 * @param {} node - 
 * @param {} insideSubmittedPart -
 * @param {} partialIds -
 * @param {} disableFlag -
 * @param {} disableTypesArr -
 * @param {} stringBuffer -
 */
myfaces._impl.xhrCore_AjaxUtils.prototype.addNodes = function(node, insideSubmittedPart,
		partialIds, stringBuffer) {
	if (node != null && node.childNodes != null) {
		var nLen = node.childNodes.length;
		for ( var i = 0; i < nLen; i++) {
			var child = node.childNodes[i];
			var id = child.id;
			var elementName = child.name;
			if (child.nodeType == 1) {
				var isPartialSubmitContainer = ((id != null) && partialIds
						.contains(id.splitAndGetLast(":")));
				if (insideSubmittedPart
						|| isPartialSubmitContainer
						|| (elementName != null && jsfKeywords
								.contains(elementName))) {
					// send id for PPS if uppermost submitted container
					if (isPartialSubmitContainer) {
						stringBuffer[stringBuffer.length] = this.PARTIAL_PREFIX;
						stringBuffer[stringBuffer.length] = encodeURIComponent(id);
						stringBuffer[stringBuffer.length] = "=&";
					}
					// node required fpr PPS
					this.addField(child, stringBuffer);
					if (insideSubmittedPart || isPartialSubmitContainer) {
						// check for further children
						this.addNodes(child, true, partialIds, stringBuffer);
					}
				} else {
					// check for further children
					this.addNodes(child, false, partialIds, stringBuffer);
				}
			}
		}
	}
}

/**
 * add a single field to stringbuffer for param submission
 * @param {HtmlElement} element - 
 * @param {} stringBuffer - 
 */
myfaces._impl.xhrCore_AjaxUtils.prototype.addField = function(element, stringBuffer) {
	var elementName = element.name;
	var elementTagName = element.tagName.toLowerCase();
	var elementType = element.type;
	if (elementType != null) {
		elementType = elementType.toLowerCase();
	}

	// routine for all elements
	// rules:
	// - process only inputs, textareas and selects
	// - elements muest have attribute "name"
	// - elements must not be disabled
	if (((elementTagName == "input" || elementTagName == "textarea" || elementTagName == "select") && 
		 (elementName != null && elementName != "")) && element.disabled == false) {

		// routine for select elements
		// rules:
		// - if select-one and value-Attribute exist => "name=value"
		// (also if value empty => "name=")
		// - if select-one and value-Attribute don't exist =>
		// "name=DisplayValue"
		// - if select multi and multple selected => "name=value1&name=value2"
		// - if select and selectedIndex=-1 don't submit
		if (elementTagName == "select") {
			// selectedIndex must be >= 0 sein to be submittet
			if (element.selectedIndex >= 0) {
				var uLen = element.options.length;
				for ( var u = 0; u < uLen; u++) {
					// find all selected options
					if (element.options[u].selected == true) {
						var elementOption = element.options[u];
						stringBuffer[stringBuffer.length] = encodeURIComponent(elementName);
						stringBuffer[stringBuffer.length] = "=";
						if (elementOption.getAttribute("value") != null) {
							stringBuffer[stringBuffer.length] = encodeURIComponent(elementOption.value);
						} else {
							stringBuffer[stringBuffer.length] = encodeURIComponent(elementOption.text);
						}
						stringBuffer[stringBuffer.length] = "&";
					}
				}
			}
		}

		// routine for remaining elements
		// rules:
		// - don't submit no selects (processed above), buttons, reset buttons, submit buttons,
		// - submit checkboxes and radio inputs only if checked
		if ((elementTagName != "select" && elementType != "button"
				&& elementType != "reset" && elementType != "submit" && elementType != "image")
				&& ((elementType != "checkbox" && elementType != "radio") || element.checked)) {
			stringBuffer[stringBuffer.length] = encodeURIComponent(elementName);
			stringBuffer[stringBuffer.length] = "=";
			stringBuffer[stringBuffer.length] = encodeURIComponent(element.value);
			stringBuffer[stringBuffer.length] = "&";
		}

	}
}
