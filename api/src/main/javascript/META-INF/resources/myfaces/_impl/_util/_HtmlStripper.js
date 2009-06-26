/*
 * Copyright 2009 Apache Software Foundation
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
 * Author: Werner Punz (latest modification by $Author: werpu $)
 * Version: $Revision: 1.7 $ $Date: 2009/05/06 08:47:08 $
 *
 */

/**
 * <p>
 * A simple html parser class
 * that handles the stripping of the body
 * and head area properly
 * </p>
 * <p>
 * we assume that the ajax response encapsules
 * the html elements
 * so we only have to strip those really needed by the response
 * </p>
 * Due to this fact we can make several shortcuts...
 * First we only have to parse either for html or body for now
 * hence we can skip code and pre parsing to skip embedded tags as well
 *
 * <p>
 * secondly once we have found our section we can parse bottom up
 * which adds an additional speedup to our parsing process!
 * </p>
 * <p>
 * Note we do not solve the head and body stripping via regular expressions
 * because there are usecases where this does not work out
 * for instance comments with embedded head and body sections
 * or javascripts with head and body in strings..
 *
 * We tried to solve that that way in the first place but due to
 * the nature of things a minimal semantic understanding in the parsing
 * process is needed to strip everything out correctly which is not entirely
 * given via normal pattern matching, so we went the hard route
 * of implementing everything with a minimal parser
 * which tries to cover the semantics needed to strip out the correct content!
 * </p>
 */

_reserveMyfacesNamespaces();

if (!myfaces._impl._util._LangUtils.exists(myfaces._impl._util, "_HtmlStripper")) {

    myfaces._impl._util._HtmlStripper = function() {
    };

    myfaces._impl._util._HtmlStripper.prototype.BEGIN_TAG = "html";
    myfaces._impl._util._HtmlStripper.prototype.END_TAG = "lmth";

    /**
     * parses the token array
     * and handles the incoming tokens via an ll
     * parsing and backtracking of one char
     * the handling of the parsing is done via
     * recursive descension
     *
     * note in the subroutines the tokenPos must be at the last char of the operation
     * so that the
     */
    myfaces._impl._util._HtmlStripper.prototype.parse = function(theString, tagNameStart, tagNameEnd) {
        this.tokens = theString.split("");
        this.tagAttributes = {};

        this._contentStart = -1;
        this._contentEnd = -1;
        this._tokenPos = 0;

        this._tokenForward = 1;



        if ('undefined' == typeof tagNameStart || null == tagNameStart) {
            this.tagNameStart = myfaces._impl._util._HtmlStripper.prototype.BEGIN_TAG;
        } else {
            this.tagNameStart = tagNameStart;
        }

        if ('undefined' == typeof tagNameEnd || null == tagNameEnd) {
            this.tagNameEnd = this.tagNameStart.split("").reverse().join("");
        } else {
            this.tagNameEnd = tagNameEnd.split("").reverse().join("");
        }

        this.handleInstructionBlock();

        if (this._contentStart >= 0 && this._contentEnd == -1) {
            this._tokenPos = this.tokens.length - 1;
            this._tokenForward = -1;
            //we now can skip the parsing for the rest of the block and have to roll out the parsing
            //from bottom up, this speeds up the entire process tremendously!
            this.handleEndBlock();
        }

        if (this._contentStart >= 0 && this._contentEnd == -1) {
            this._contentEnd = this.tokens.length - 1;
        } else if (this._contentStart == -1) {
            return "";
        }
        return this.tokens.slice(this._contentStart, this._contentEnd + 1).join("");

    };

    /**
     * normal characters are skipped
     * a < clearly is an indicator for
     * a ... normal chars should not occur here, but
     * in the long run for deeper parsing they might happen!
     */
    myfaces._impl._util._HtmlStripper.prototype.handleInstructionBlock = function() {
        var len = this.tokens.length;
        for (; this._contentStart < 0 && this._tokenPos < len && this._tokenPos >= 0; this._tokenPos += this._tokenForward) {
            this._skipBlank();
            var token = this._getCurrentToken();
            if (token == "<") {
                this.handleDocument();
            }
        }
    };

    /**
     * it is either a datablock or a content tag from which we can start parsing
     */
    myfaces._impl._util._HtmlStripper.prototype.handleDocument = function() {

        this._skipBlank(1);

        if (this._tokenPos >= this.tokens.length) {
            throw new Error("Document end reached prematurely");
        }
        var token = this._getCurrentToken();
        switch (token) {
            case "!":
                this.handleDataBlock();
                break;

            default: this.handleContentTag();
        }
    };

    /**
     * we can skip definitions or comments!
     *
     * a definition or comment section is like following:
     * <! ... > with no </ at the end
     *
     * with comments followed by --
     * and definitions followed by nothing else
     *
     */
    myfaces._impl._util._HtmlStripper.prototype.handleDataBlock = function() {
        this._skipBlank(1);

        if (this._tokenPos >= this.tokens.length || this._tokenPos < 0) {
            return;
        }
        var token = this.tokens[this._tokenPos];
        switch (token) {
            case "-":
                this.handleComment();
                break;

            default:
                this._getCurrentToken();
                this.handleDocDefinition();
                break;
        }

    };

    /**
     * doc definition ==
     * <! [^>] >
     */
    myfaces._impl._util._HtmlStripper.prototype.handleDocDefinition = function() {
        this._skipBlank();

        if (this._tokenPos >= this.tokens.length || this._tokenPos < 0) {
            throw new Error("Document end reached prematurely");
        }
        var len = this.tokens.length;
        while (this._tokenPos < len && this._tokenPos >= 0) {
            //var token = this._getCurrentToken();
            //inlining for speed reasons  we also could use getCurrentToken
            var token = this.tokens[this._tokenPos];

            //inlining end
            if (token == ">") {
                return;
            }
            this._tokenPos += this._tokenForward;
        }

    };

    /**
     * General tag andling section
     * we define identified and unidentified content
     * and script which is a subpart of idenitifed
     *
     * for now we do not handle the special conditions within pre and code
     * segments since we have a specialized usage of this stripper
     * on head and body sections and within head (which is skipped)
     * we neither can have code or pre segments!
     */
    myfaces._impl._util._HtmlStripper.prototype.handleContentTag = function() {
        //lookahead head, body, html which means a lookahead of 4;
        this._currentSection = null;
        this._skipBlank();
        var tagName = this._fetchTagname();

        /*we try to avoid lookaheads here hence the shifting of tokens!*/
        if (tagName == this.tagNameStart) {
            /*either embedded into html */
            this.handleIdentifiedContent();
            //after the html tag is processed we can break from the first parsing stage
            this.tokenPos = this.tokens.length;
        } else    if (tagName == "scri" || tagName == "styl") {
            //script must be handled separately since we can have embedded tags which must be ignored
            this.handleScriptStyle();
        } else {
            //unidentified content we deal with it by skipping to the tag end
            //and then be done with it!
            this.skipToTagEnd();
        }
    };

    /**
     * script skipping routine...
     * we skip automatically over embedded scripts
     * and tags within strings and comments
     * so that we do not trigger against embedded tags in script sections
     * of the head!
     *
     *
     */
    myfaces._impl._util._HtmlStripper.prototype.handleScriptStyle = function() {
        this.skipToTagEnd();
        //singleToken??
        if (this.tokens[this._tokenPos - 1] == "/" && this.tokens[this._tokenPos] == ">") {
            return;
        }
        //lets skip until we hit < by ignoring embedded strings and comments
        do {
            this._skipBlank(1);
            var token = this._getCurrentToken();
            switch (token) {
                case "\'" || "'":
                    this.handleString(token);
                    break;
                case "/" :
                    this.handleJSComment();
                    break;
            }

        } while (this.tokens[this._tokenPos] != "<");
        //now we should be at the end of the script tag at any circumstances!
        this.skipToTagEnd();
    };

    /**
     * javascript comment handler
     * we have to check for escape sequences so that we do not trigger
     * accidentally the comment parsing within embedded regular expressions
     * which means we have to prefetch and backtrack one token!
     * \/* should not start a comment neither should \//
     * if someone places this in the middle of the code
     * then oh well, syntax error on the javascript side
     * we cannot do anything about it
     */
    myfaces._impl._util._HtmlStripper.prototype.handleJSComment = function() {
        var token = this._getCurrentToken();
        var prefetchToken = this.tokens[this._tokenPos + 1];
        var backtrackToken = this.tokens[this._tokenPos - 1];
        var backtrackToken2 = this.tokens[this._tokenPos - 2];

        //comment condition == either /* or // with no \ in backtrack or \\ in backtrack!
        var backTrackIsComment = backtrackToken != '\\' || (backtrackToken == '\\' && backtrackToken2 == '\\');
        if (!backTrackIsComment) {
            return;
        }

        var singleLineComment = prefetchToken == '/';
        var multiLineComment = prefetchToken == "*";

        if (singleLineComment) {
            while (this._tokenPos < this.tokens.length && this._getCurrentToken() != "\n") {
                this._tokenPos++;
            }

        } else if (multiLineComment) {
            this._skipBlank(1);
            while (this._tokenPos < this.tokens.length) {
                this._skipBlank(1);
                token = this._getCurrentToken();
                prefetchToken = this.tokens[this._tokenPos + 1];
                if (token == "*" && prefetchToken == "/") {
                    return;
                }
            }
        }
    };

    /*----------------- reverse parsing --------*/

    /**
     * the end block tos a bottom up resolution of the parsing process
     * via an inverted tag name to look for
     */
    myfaces._impl._util._HtmlStripper.prototype.handleEndBlock = function() {
        for (; this._tokenPos >= 0; this._tokenPos += this._tokenForward) {
            this._skipBlank(0);
            var token = this._getCurrentToken();
            if (token == ">") {
                this.handleEndTagPart();
            }
        }
    };

    myfaces._impl._util._HtmlStripper.prototype.handleEndTagPart = function() {

        this._skipBlank(1);

        if (this._tokenPos < 0) {
            throw new Error("Document end reached prematurely");
        }
        var token = this._getCurrentToken();

        //we can assume we are outside of the html or body sections
        //se we only have to check for comments for the reverse parsing!
        switch (token) {
            case "-":

                this.handleComment(true);
                break;

            default: this.handleContentEnd();
        }

    };

    myfaces._impl._util._HtmlStripper.prototype.handleContentEnd = function() {
        var tagFound = false;
        var first = true;
        for (; this._tokenPos >= 0; this._skipBlank(1)) {
            if (first && !tagFound) {
                var tagName = this._fetchTagname();
                if (tagName == this.tagNameEnd) {
                    tagFound = true;
                }
                first = false;
            } else if (tagFound && this.tokens[this._tokenPos] == "<") {
                this._contentEnd = this._tokenPos - 1;
                this._tokenPos = -1;
                return;
            } else if (this.tokens[this._tokenPos] == "<") {
                this._tokenPos += 1;
                return;
            }

        }
    };

    /*----------------- helpers ----------------*/
    /**
     * skips the current tag definition until the end is reached
     *
     * @param analyzeAttributes if set to true we will end up with
     * a map of determined key value pairs which we then can further process
     * otherwise the key value pair determination is ignored!
     */
    myfaces._impl._util._HtmlStripper.prototype.skipToTagEnd = function(analyzeAttributes) {

        var token = this._getCurrentToken();
        //faster shortcut for tags which have to be nont analyzed
        if (!analyzeAttributes) {
            while (token != ">") {
                if (this._isStringStart()) {
                    this._tokenPos += this._tokenForward;
                    return this.handleString(token)
                }
                this._skipBlank(1);
                token = this._getCurrentToken();
            }

            return null;
        }

        //analyze part

        var keyValuePairs = {};
        var currentWord = [];
        var currentKey = null;
        var openKey = false;
        var lastKey = null;
        while (this.tokens[this._tokenPos] != ">") {
            var currentWord = this._fetchWord();
            var token = this._getCurrentToken();

            if (token == "=") {
                this._tokenPos += this._tokenForward;
                keyValuePairs[currentWord] = this._fetchWord();
            } else {
                keyValuePairs[currentWord] = null;
            }
            this._tokenPos += this._tokenForward;
        }
        return keyValuePairs;
    };

    /**
     * fetches a word which either can be a string or
     * a sequence of non string characters until either = or > or blank is reached!
     */
    myfaces._impl._util._HtmlStripper.prototype._fetchWord = function() {
        this._skipBlank(0);
        var result = [];

        var token = this._getCurrentToken();
        while ((!this._isBlank()) && token != "=" && token != ">") {
            if (this._isStringStart()) {
                this._tokenPos += this._tokenForward;
                return this.handleString(token)
            }
            
            result.push(token);
            this._tokenPos += this._tokenForward;
            token = this._getCurrentToken();
        }
        return result.join("");
    };

    myfaces._impl._util._HtmlStripper.prototype._isBlank = function() {
        var token = this._getCurrentToken();
        return token == " " && token == "\t" && token == "\n";
    };

    /**
     * we can make speedup shorcut assumptions here
     * becase we only try to either identfy head
     * html or body!
     */
    myfaces._impl._util._HtmlStripper.prototype.handleIdentifiedContent = function() {

        this.tagAttributes = this.skipToTagEnd(true);
        //TODO trace down the attributes and store them
        //they must be key value pairs

        if (this.tokens[this._tokenPos - 1] == "/" && this.tokens[this._tokenPos] == ">") {
            this._contentStart = -1;
            this._contentEnd = -1;
            /*we move to the end*/
        } else {
            this._contentStart = this._tokenPos + 1;
        }
    };

    /**
     * returns true if the current token is a string start!
     */
    myfaces._impl._util._HtmlStripper.prototype._isStringStart = function() {
        var backTrack = (this._tokenPos > 0) ? this.tokens[this._tokenPos - 1] : null;
        var token = this.tokens[this._tokenPos];
        return (token == "'" || token == '"') && backTrack != "\\";
    };

    /**
     * skips a string section cintentwise no matter how many other strings
     * are embedded (uses backtracking to check for escapes)
     *
     * @param  {String} stringToken the string token to skip!
     * @return the string value without the enclosing hypenations to be processed later on
     */
    myfaces._impl._util._HtmlStripper.prototype.handleString = function(stringToken) {
        var backTrack = null;
        var resultString = [];
        while (this.tokens[this._tokenPos] != stringToken || backTrack == "\\") {
            backTrack = this._getCurrentToken();
            resultString.push(backTrack);
            this._tokenPos += this._tokenForward;
            if (this._tokenPos >= this.tokens.length) {
                throw Error("Invalid html string opened but not closed");
            }
        }
        this._getCurrentToken();
        return resultString.join("");
    };

    myfaces._impl._util._HtmlStripper.prototype._assertValues = function(assertValues) {

        for (var loop = 0; loop < assertValues.length; loop++) {
            this._assertValue(assertValues[loop]);
            this._skipBlank(1);
        }
    };

    myfaces._impl._util._HtmlStripper.prototype._assertValue = function(expectedToken) {
        var token = this._getCurrentToken();
        this._assertLength();
        if (token != expectedToken) {
            throw Error("Invalid Token  " + expectedToken + " was expected instead of " + token);
        }

        return token;
    };

    myfaces._impl._util._HtmlStripper.prototype._assertLength = function() {
        if (this._tokenPos >= this.tokens.length) {
            throw Error("Invalid html comment opened but not closed");
        }
    };

    /**
     * nested comments are not allowed hence we
     * skip them!
     * comment == "&lt;!--" [--&gt;] "--&gt;"
     */
    myfaces._impl._util._HtmlStripper.prototype.handleComment = function(reverse) {
        this._assertValues(["-","-"]);
        if ('undefined' == typeof reverse || null == reverse) {
            reverse = false;
        }

        while (this._tokenPos < this.tokens.length - 3) {
            //lookahead3, to save some code
            var token = this._getCurrentToken();
            var backTrackBuf = [];

            if (token == "-") {
                backTrackBuf.push(token);
                this._skipBlank(1);
                token = this._getCurrentToken();
                backTrackBuf.push(token);
                this._skipBlank(1);
                token = this._getCurrentToken();
                backTrackBuf.push(token);

                if (reverse) {
                    this._skipBlank(1);
                    token = this._getCurrentToken();
                    backTrackBuf.push(token);
                }
                backTrackBuf = backTrackBuf.join("");

                if (reverse && backTrackBuf == "<!--") {
                    return;
                } else if (!reverse && backTrackBuf == "-->") {
                    return;
                }
            } else {
                this._skipBlank(1);
            }
        }
    };

    /**
     * fetches and stores the current token
     */
    myfaces._impl._util._HtmlStripper.prototype._getCurrentToken = function() {
        return this.tokens[this._tokenPos];
    };

    /**
     * skip blank until the next token is found
     * @param skipVal  the minimum skip forward to happen
     * 1 means it skips 1 no matter if the current token is a blank or not!
     *
     */
    myfaces._impl._util._HtmlStripper.prototype._skipBlank = function(skipVal) {
        var len = this.tokens.length;
        if ('undefined' == typeof  skipVal || null == skipVal) {
            skipVal = 0;
        }

        for (this._tokenPos += (skipVal * this._tokenForward); this._tokenPos < len && this._tokenPos >= 0; this._tokenPos += this._tokenForward) {
            var token = this.tokens[this._tokenPos];
            if (token != " " && token != "\t" && token != "\n") {
                return;
            }
        }
    };

    myfaces._impl._util._HtmlStripper.prototype._fetchTagname = function() {
        var tagName = [];

        //TODO make the tagname prefetch more generic

        tagName.push(this.tokens[this._tokenPos]);
        this._tokenPos += this._tokenForward;
        tagName.push(this._getCurrentToken());
        this._tokenPos += this._tokenForward;
        tagName.push(this._getCurrentToken());
        this._tokenPos += this._tokenForward;
        tagName.push(this._getCurrentToken());
        this._tokenPos += this._tokenForward;

        tagName = tagName.join("").toLowerCase();
        return tagName;
    };
}