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


myfaces._impl.xhrCore_Exception = function(sourceClass, threshold) {
	this.m_class = sourceClass
	this.m_threshold = threshold
};

/**
 * [STATIC]
 * static method used by static methods that throw errors
 */
myfaces._impl.xhrCore_Exception.throwNewError = function(sourceClass, func, exception) {
	newException = new myfaces._impl.xhrCore_Exception(sourceClass, "ERROR");
	newException.throwError(func, exception);
};

/**
 * [STATIC]
 * static method used by static methods that throw warnings
 */
myfaces._impl.xhrCore_Exception.throwNewWarning = function(sourceClass, func, message){
	newException = new myfaces._impl.xhrCore_Exception(sourceClass, "WARNING");
	newException.throwWarning(func, message);
};

/**
 * throws errors
 */
myfaces._impl.xhrCore_Exception.prototype.throwError = function(func, exception) {
	if (this.m_threshold == "ERROR") {
		alert("[J4Fry ERROR]\n\n"
				+ "Affected Class: " + this.m_class + "\n"
				+ "Affected Method: " + func + "\n"
				+ "Error name: " + exception.name + "\n"
				+ "Error message: " + exception.message + "\n"
				+ "Error description: " + exception.description	+ "\n"
				+ "Error number: " + exception.number + "\n"
				+ "Error line number: " + exception.lineNumber
				+ "\n\nIf the error occurs many times, \nplease contact one of the J4Fry members on http://www.j4fry.org");
	}
	this.destroy();
};

/**
 * throws warnings
 */
myfaces._impl.xhrCore_Exception.prototype.throwWarning = function(func, message) {
	if (this.m_threshold == "WARNING" || this.m_threshold == "ERROR") {
		alert("[J4Fry WARNING]\n[" + this.m_class + "::" + func + "]\n\n"
				+ message);
	}
	this.destroy();
};

/**
 * cleanup activities if an error occurs
 */
myfaces._impl.xhrCore_Exception.prototype.destroy = function() {
	if (myfaces._impl.xhrCore_AjaxRequestQueue.queue &&
			myfaces._impl.xhrCore_AjaxRequestQueue.queue != null) {
		// clear RequestQueue when an exception occurs
		myfaces._impl.xhrCore_AjaxRequestQueue.queue.clearQueue();
	}
};