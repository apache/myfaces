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
 * A simple html parser class
 * that handles the stripping of the body
 * and head area properly
 *
 * we assume that the ajax response encapsules
 * the html elements
 * so we only have to strip those
 */


_reserveMyfacesNamespaces();

if(!myfaces._impl._util._LangUtils.exists(myfaces._impl._util,"_HtmlStripper")) {

    myfaces._impl._util._HtmlStripper = function() {
    };



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
    myfaces._impl._util._HtmlStripper.prototype.parse = function(theString) {
        this.tokens = theString.split("");
        this._contentStart = -1;
        this._contentEnd = -1;
        this._tokenPos = 0;

        this._tokenForward = 1;
        this.handleInstructionBlock();
        if(this._contentStart >= 0 && this._contentEnd == -1) {
            this._tokenPos = this.tokens.length - 1;
            this._tokenForward = -1;
            this.handleEndBlock();
        }

        if(this._contentStart >= 0 && this._contentEnd == -1) {
            this._contentEnd = this.tokens.length - 1;
        } else if(this._contentStart == -1) {
            return "";
        } 
        return this.tokens.slice(this._contentStart ,this._contentEnd).join("");
        
    };

    myfaces._impl._util._HtmlStripper.prototype.handleEndBlock = function() {
     
        for(;this._tokenPos >= 0; this._tokenPos += this._tokenForward  ) {
            this._skipBlank(0);
            var token = this._getCurrentToken();
            if(token != ">") {
                continue;
            } else {
                this.handleEndDocument();
            }
        }
    };

    myfaces._impl._util._HtmlStripper.prototype.handleInstructionBlock = function() {
        var len =  this.tokens.length;
        for(;this._tokenPos < len && this._tokenPos >= 0; this._tokenPos += this._tokenForward  ) {
            this._skipBlank();
            var token = this._getCurrentToken();
            if(token != "<") {
                continue;
            } else {
                this.handleDocument();
            } 
        }
    };

    myfaces._impl._util._HtmlStripper.prototype.handleDocument = function() {

        this._skipBlank(1);

        if(this._tokenPos >= this.tokens.length ) {
            throw new Error("Document end reached prematurely");
        }
        var token = this._getCurrentToken();
        switch(token) {
            case "!":
                this.handleDataBlock();
                break;

            default: this.handleContent();
        }
    };

    myfaces._impl._util._HtmlStripper.prototype.handleEndDocument = function() {

        this._skipBlank(1);

        if(this._tokenPos < 0 ) {
            throw new Error("Document end reached prematurely");
        }
        var token = this._getCurrentToken();
        switch(token) {
            case "-":

                this.handleComment(true);
                break;

            default: this.handleContentEnd();
        }
    };

    myfaces._impl._util._HtmlStripper.prototype.handleContentEnd = function() {
        var tagFound = false;
        for( ;this._tokenPos >= 0; this._skipBlank(1)) {
            if(!tagFound) {
                var tagName = this._fetchTagname();
                if(tagName == "lmth") {
                    tagFound = true;
                }
            } else if(tagFound && this.tokens[this._tokenPos] == "<") {
                this._contentEnd = this._tokenPos - 1;
                this._tokenPos = -1;
                return;
            }
            
        }
    }


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


        if(this._tokenPos >= this.tokens.length || this._tokenPos < 0 ) {
            return;
        }
        var token = this.tokens[this._tokenPos];
        switch(token) {
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

        if(this._tokenPos >= this.tokens.length || this._tokenPos < 0 ) {
            throw new Error("Document end reached prematurely");
        }
        var len = this.tokens.length;
        while(this._tokenPos < len && this._tokenPos >= 0 ) {
            //var token = this._getCurrentToken();
            //inlining for speed reasons  we also could use getCurrentToken
            var token = this.tokens[this._tokenPos];
           
            //inlining end
            if(token == ">") {
                return;
            }
            this._tokenPos+=this._tokenForward;
        }

    };


    myfaces._impl._util._HtmlStripper.prototype.handleContent = function() {
        //lookahead head, body, html which means a lookahead of 4;
        this._currentSection = null;


        this._skipBlank();

        var tagName = this._fetchTagname();

        /*we try to avoid lookaheads here hence the shifting of tokens!*/
        switch(tagName) {
            case "html":/*either embedded into html */

                this.handleHtml();
                //after the html tag is processed we can break from the first parsing stage
                this.tokenPos = this.tokens.length;

            default: break;
        }
    };

    myfaces._impl._util._HtmlStripper.prototype.handleHtml = function() {

        while(this.tokens[this._tokenPos] != ">") {
            this._skipBlank(1);
            var token = this._getCurrentToken();
            if((token == "'" || token == '"') && this._isStringStart()) {
                this.handleString(token);
            }
        }

        this._htmlSection = this.html;

        if(this.tokens[this._tokenPos-1] == "/" && this.tokens[this._tokenPos] == ">") {
            this._contentStart = -1;
            this._contentEnd = -1; /*we move to the end*/
        } else {
            this._contentStart = this._tokenPos+1;
        }
    };


    myfaces._impl._util._HtmlStripper.prototype._isStringStart = function() {
        var backTrack = (this._tokenPos > 0)? this.tokens[this._tokenPos-1]:null;
        var token = this.tokens[this._tokenPos];
        return (token == "'" || token == '"') && backTrack != "\\";
    };

   

  
    myfaces._impl._util._HtmlStripper.prototype.handleString = function(stringToken) {
        var backTrack = null;

        while(this.tokens[this._tokenPos] != stringToken || backTrack == "\\") {
            backTrack = this._getCurrentToken();
            this._tokenPos+=this._tokenForward ;
            if(this._tokenPos >= this.tokens.length ) {
                throw Error("Invalid html string opened but not closed");
            }
        }
        this._getCurrentToken();

    };


    myfaces._impl._util._HtmlStripper.prototype._assertValues = function(assertValues) {

        for(var loop = 0; loop < assertValues.length; loop++) {
            this._assertValue(assertValues[loop]);
            this._skipBlank(1);
        }
    };


    myfaces._impl._util._HtmlStripper.prototype._assertValue = function(expectedToken) {
        var token = this._getCurrentToken();
        this._assertLength();
        if(token != expectedToken) {
            throw Error("Invalid Token  "+expectedToken+" was expected instead of "+token);
        }

        return token;
    };

    myfaces._impl._util._HtmlStripper.prototype._assertLength = function() {
        if(this._tokenPos >= this.tokens.length ) {
            throw Error("Invalid html comment opened but not closed");
        }
    };

    /**
     * nested comments are not allowed hence we
     * skip them!
     * comment == "<!--" [-->] "-->"
     */
    myfaces._impl._util._HtmlStripper.prototype.handleComment = function(reverse) {
        this._assertValues(["-","-"]);
        if('undefined' == typeof reverse || null == reverse) {
            reverse = false;
        }

        while(this._tokenPos < this.tokens.length-3) {
            //lookahead3, to save some code
            token = this._getCurrentToken();
            var backTrackBuf = [];

            if(token == "-") {
                backTrackBuf.push(token);
                this._skipBlank(1);
                token = this._getCurrentToken();
                backTrackBuf.push(token);
                this._skipBlank(1);
                token = this._getCurrentToken();
                backTrackBuf.push(token);
                
                if(reverse) {
                    this._skipBlank(1);
                    token = this._getCurrentToken();
                    backTrackBuf.push(token);
                }
                backTrackBuf = backTrackBuf.join("");
                
                if(reverse && backTrackBuf == "<!--") {
                    return;
                } else if(!reverse && backTrackBuf == "-->") {
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

 
    myfaces._impl._util._HtmlStripper.prototype._skipBlank = function(skipVal) {
        var len =  this.tokens.length;
        if('undefined' == typeof  skipVal || null == skipVal) {
            skipVal = 0;
        }

        for(this._tokenPos += (skipVal * this._tokenForward);this._tokenPos < len && this._tokenPos >= 0; this._tokenPos+=this._tokenForward) {
            var token = this.tokens[this._tokenPos];
            if(token != " " && token != "\t" && token != "\n") {
                return;
            }
        }
    };

    myfaces._impl._util._HtmlStripper.prototype._fetchTagname = function() {
        var tagName = [];


        tagName.push(this.tokens[this._tokenPos]);
        this._tokenPos+=this._tokenForward ;
        tagName.push(this._getCurrentToken());
        this._tokenPos+=this._tokenForward ;
        tagName.push(this._getCurrentToken());
        this._tokenPos+=this._tokenForward ;
        tagName.push(this._getCurrentToken());
        this._tokenPos+=this._tokenForward ;

        tagName = tagName.join("").toLowerCase();
        return tagName;
    };
}