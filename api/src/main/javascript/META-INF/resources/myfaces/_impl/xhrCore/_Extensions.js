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
 * Version: $Revision: 1.2 $ $Date: 2009/04/09 12:38:48 $
 *
 */

/**
 * Array.contains
 * searches an Array for a String
 * param {String} string_name - String to find
 *
 *
 * TODO please remove this, this causes too much cross interference
 * such things belong into _LangUtils!
 * 
 */
Array.prototype.contains = function(string_name) {
	for ( var i = 0; i < this.length; i++) {
		if (this[i] == string_name) {
			return true;
		}
	}
	return false;
};

/**
 * Array.toString
 * translates an Array to a String @param {String} delimiter - Delimiter
 * to separate Array elements
 *
 * added missing functionality
 */
Array.prototype.toString = function(delimiter) {
	var result = "";
	for ( var i = 0; i < this.length; i++) {
		result += ((delimiter == null) ? ("[" + i + "] ") : "") + this[i];
		if (i != this.length - 1)
			result += (delimiter == null) ? "\n" : delimiter;
	}
	return result;
};

/**
 * String.trim
 * remove blank at head and tail of a string
 *
 * faster implementation in LangUtils
 */
String.prototype.trim = function() {
	var i = 0;
	var x = this.length - 1;
	for (; this.charAt(i) == " "; i++) {}
	for (; this.charAt(x) == " "; x--) {}
	return this.substring(i, x + 1);
};

/**
 * String.splitAndGetLast
 * splits an Array at the Delimiter and returns last element
 *
 * added to LangUtils
 */
String.prototype.splitAndGetLast = function(delimiter) {
	var arr = this.split(delimiter);
	return arr[arr.length - 1];
};