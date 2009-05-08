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
 * Version: $Revision: 1.6 $ $Date: 2009/05/05 11:50:16 $
 *
 */

/**
 * A simple html parser class
 * that handles the stripping of the body
 * and head area properly
 *
 * @deprecated due to the RC2 spec we only need a simple
 * html stripper for our ajax reponse
 * 
 */
_reserveMyfacesNamespaces();

if(!myfaces._impl._util._LangUtils.exists(myfaces._impl._util,"_SimpleHtmlParser")) {

    myfaces._impl._util._SimpleHtmlParser = function() {
       
     
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
    myfaces._impl._util._SimpleHtmlParser.prototype.parse = function(theString) {
        this.tokens = theString.split("");

        this.html = [];
        this.head = [];
        this.body = [];
        this.docType = [];

        this._currentSection = null;
        this._htmlSection = null;


        this._tokenPos = 0;


        this._tokenPos = 0;
        //console.debug(new Date());
        this.handleInstructionBlock();
    //console.debug(new Date());
    };

    myfaces._impl._util._SimpleHtmlParser.prototype.handleInstructionBlock = function() {
        var len =  this.tokens.length;
        for(;this._tokenPos < len; this._tokenPos++ ) {
            this._skipBlank();
            var token = this._getCurrentToken();
            if(token != "<") {
                continue;
            } else {
                this.handleDocument();
            }
        }
    };

    myfaces._impl._util._SimpleHtmlParser.prototype.handleDocument = function() {
        
        this._skipBlank(1);
     
        if(this._tokenPos >= this.tokens ) {
            throw new Error("Document end reached prematurely");
        }
        var token = this._getCurrentToken();
        switch(token) {
            case "!":
                this._currentSection = this.docType;
               
                this.handleDataBlock();
                break;
           
            default: this.handleContent();
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
    myfaces._impl._util._SimpleHtmlParser.prototype.handleDataBlock = function() {
        this._skipBlank(1);
      

        if(this._tokenPos >= this.tokens ) {
            return;
        }
        var token = this.tokens[this._tokenPos];
        switch(token) {
            case "-":
                this.handleComment();
                break;
            case "[":
                this._getCurrentToken();
                this.handleCData();
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
    myfaces._impl._util._SimpleHtmlParser.prototype.handleDocDefinition = function() {
        this._skipBlank();

        if(this._tokenPos >= this.tokens ) {
            throw new Error("Document end reached prematurely");
        }
        var len = this.tokens.length;
        while(this._tokenPos < len ) {
            //var token = this._getCurrentToken();
            //inlining for speed reasons  we also could use getCurrentToken
            var token = this.tokens[this._tokenPos];
            if(null != this._currentSection) {
                this._currentSection.push(token);
            }
            if(null != this._htmlSection) {
                this._htmlSection.push(token);
            }
            //inlining end
            if(token == ">") {
                return;
            }
            this._tokenPos++
        }

    };


    myfaces._impl._util._SimpleHtmlParser.prototype.handleContent = function() {
        //lookahead head, body, html which means a lookahead of 4;
        this._currentSection = null;

        
        this._skipBlank();

        var tagName = this._fetchTagname();

        /*we try to avoid lookaheads here hence the shifting of tokens!*/
        switch(tagName) {
            case "html":/*either embedded into html */
                
                this.handleHtml();
                this._skipBlank();
                break;
            //TODO double adding in the html part
            case "head":/*or standalone*/
                this._currentSection = this.head;
                this._currentSection.push("<");
                this.handleSection("head");
                this._currentSection = null;
                break;
            case "body":
                this._currentSection = this.body;
                
                this._currentSection.push("<");
                this.handleSection("body");
                this._currentSection = null;
                break;
            /*special case due to recursive descension we might run into an end of document!*/
            case "/htm":
                this._currentSection = null;
                this._htmlSection.splice(this._htmlSection.length-5,5);
                this._tokenPos = this.tokens.length;
                break;
            default: break;
        }
    };

    myfaces._impl._util._SimpleHtmlParser.prototype.handleHtml = function() {

        while(this.tokens[this._tokenPos] != ">") {
            this._skipBlank(1);
            var token = this._getCurrentToken();
            if((token == "'" || token == '"') && this._isStringStart()) {
                this.handleString(token);
            }
        }
        
        this._htmlSection = this.html;

        if(this.tokens[this._tokenPos-1] == "/" && this.tokens[this._tokenPos] == ">") {
           
            this._htmlSection = null;
            this._tokenPos = this._tokens.length; /*we move to the end*/
        } else {
            /*we recursively step back into our instructions to handle our
             *instruction blocks namely head and body as exptected!*/
       
            this._skipBlank(1);
            
            this.handleInstructionBlock();
        }

    //we check for a pending /htm at the end


    };


    myfaces._impl._util._SimpleHtmlParser.prototype.handleSection = function(tagName, ignoreString) {


        this._currentSection.push(tagName);
        
        if('undefined' == typeof ignoreString || null == ignoreString) {
            ignoreString = true;
        }


        var backTrack = "";
        //first we look for the end tag with backtracking of -1
        var token = this._getCurrentToken();
        /*no comments in tag definitions are allowed*/
        
        while(token != ">") {
            this._tokenPos ++;
            //possible String handling
            //we precheck to avoid unneeded calls, performance optimization!!!!
            if((token == "'" || token == '"') && this._isStringStart()) {
                token = this._getCurrentToken();
                this._tokenPos ++;
                this.handleString(token);
                this._tokenPos ++;
            }
            backTrack = token;
            token = this._getCurrentToken();
        }
 
        if(backTrack == "/") {//end of tag reached
            return;
        }

       
        
        //handle section content until the tag end is reached!
        var endSection = false;
        /*[</tagName>]*/
        var lenOuter = this.tokens.length;
        while((!endSection) && this._tokenPos < lenOuter) {
            backTrack = token;
            this._skipBlank(1);
            //token = this._getCurrentToken();

            //inlining get current token for speed reasons
            token = this.tokens[this._tokenPos];
            if(null != this._currentSection) {
                this._currentSection.push(token);
            }
            if(null != this._htmlSection) {
                this._htmlSection.push(token);
            }
            //inlining end


            if((!ignoreString) && this._isStringStart()) {
                this._skipBlank(1);
                this.handleString(token);
            //replaced isDataBlock for performance reasons
            } else if(this.tokens[this._tokenPos + 1] == "!") {
                
                this._skipBlank(1);
                this._getCurrentToken();
                this.handleDataBlock();
            } else {
                //TODO handle scripts as special section
                //which should be handled
                //just like cdata blocks

                //we do some precomparison because we spent way too much time in our lookahead
                if(token == "<" ) {
                    /**
                     *script and style must be handled differently
                     *via ignore string set to false, because in both
                     *tags subtags can be present which have to be ignored
                     **/
                    if( this._isScript() ) {
                        this._tokenPos += 7;

                        this.handleSection("script",false);
                    } else if( this._isStyle() ) {
                        this._tokenPos += 6;
                        this.handleSection("style",false);
                    } else if( this._isCode() ) {
                        this._tokenPos += 5;
                        this.handleSection("code",false);
                    } else if( this._isPre() ) {
                        this._tokenPos += 4;
                        this.handleSection("pre",false);

                    } else {
                        this._skipBlank(1);
                        //inlining get current token for speed reasons
                        token = this.tokens[this._tokenPos];
                        if(null != this._currentSection) {
                            this._currentSection.push(token);
                        }
                        if(null != this._htmlSection) {
                            this._htmlSection.push(token);
                        }
                        //inlining end
                        if(token == "/") {
                            this._skipBlank(1);
                            /*end contstruct reached*/
                            var tokenEnd = this._fetchTokenEnd();
                            

                            endSection = (tagName == tokenEnd.toLowerCase());
                        }
                    }
                }
            }
           
        }
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._fetchTokenEnd = function() {
        var retVal = [];

        do {
            var token = this._getCurrentToken();
            if(token != ">") {
                retVal.push(token);
                this._skipBlank(1);
            }
        } while(token != ">");
        return retVal.join("");
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._isStringStart = function() {
        var backTrack = (this._tokenPos > 0)? this.tokens[this._tokenPos-1]:null;
        var token = this.tokens[this._tokenPos];
        return (token == "'" || token == '"') && backTrack != "\\";
    };

    /**
     * style ll lookup
     */
    myfaces._impl._util._SimpleHtmlParser.prototype._isStyle = function() {
        return this._lookAhead(5, "<style");
    };


    /**
     * parser script ll lookup
     */
    myfaces._impl._util._SimpleHtmlParser.prototype._isScript = function() {
        return this._lookAhead(6, "<script");
    };

    /**
     * parser script ll lookup
     */
    myfaces._impl._util._SimpleHtmlParser.prototype._isCode = function() {
        return this._lookAhead(4, "<code");
    };

    /**
     * parser script ll lookup
     */
    myfaces._impl._util._SimpleHtmlParser.prototype._isPre = function() {
        return this._lookAhead(3, "<pre");
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._isDataBlock = function() {
        return this.tokens[this._tokenPos + 1] == "!";
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._isCData = function() {
        return this.tokens[this._tokenPos + 1] == "!" && this.tokens[this._tokenPos + 2] == "[";
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._isCommentStart = function() {
        return this.tokens[this._tokenPos + 1] == "!" && this.tokens[this._tokenPos + 2] == "-";
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._lookAhead = function(noOfChars, assertVal) {
        if(this._tokenPos >= this.tokens.length - (noOfChars+1)) { //script lookahead
            return false;
        }
        var asserts = assertVal.toLowerCase().split("");
        var preFetchSize = noOfChars+1;

        for(var loop = 0; loop < preFetchSize; loop++) {
            var lookAhead = this.tokens[this._tokenPos+loop].toLowerCase();
            if(asserts[loop] != lookAhead) {
                return false;
            }
        }
        return true;

    };
    
    myfaces._impl._util._SimpleHtmlParser.prototype.handleString = function(stringToken) {
        var backTrack = null;

        while(this.tokens[this._tokenPos] != stringToken || backTrack == "\\") {
            backTrack = this._getCurrentToken();
            this._tokenPos++;
            if(this._tokenPos >= this.tokens.length ) {
                throw Error("Invalid html string opened but not closed");
            }
        }
        this._getCurrentToken();
        
    };


    /**
     * nested comments are not allowed hence we
     * skip them!
     * comment == "<!--" [-->] "-->"
     */
    myfaces._impl._util._SimpleHtmlParser.prototype.handleCData = function() {
        this._skipBlank(1);

        if(this._tokenPos >= this.tokens ) {
            return;
        }


        this._assertValues(["C","D","A","T","A"]);
       
        this._assertValue("[");
     

        var backtrack = null;
        while(this._tokenPos < this.tokens.length-2) {
            //lookahead3, to save some code
            this._skipBlank(1);
            token = this._getCurrentToken();
            var lookAheadBuf = [];

            if(token == "]" && backtrack != "\\") {
                lookAheadBuf.push(token);
                this._skipBlank(1);
                token = this._getCurrentToken();
                lookAheadBuf.push(token);
                this._skipBlank(1);
                token = this._getCurrentToken();
                lookAheadBuf.push(token);
                lookAheadBuf = lookAheadBuf.join("");
                if(lookAheadBuf == "]]>") {
                    return;
                }
            }
            backtrack = token;
        }
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._assertValues = function(assertValues) {

        for(var loop = 0; loop < assertValues.length; loop++) {
            this._assertValue(assertValues[loop]);
            this._skipBlank(1);
        }
    };


    myfaces._impl._util._SimpleHtmlParser.prototype._assertValue = function(expectedToken) {
        var token = this._getCurrentToken();
        this._assertLength();
        if(token != expectedToken) {
            throw Error("Invalid Token  "+expectedToken+" was expected instead of "+token);
        }
       
        return token;
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._assertLength = function() {
        if(this._tokenPos >= this.tokens.length ) {
            throw Error("Invalid html comment opened but not closed");
        }
    };

    /**
     * nested comments are not allowed hence we
     * skip them!
     * comment == "<!--" [-->] "-->"
     */
    myfaces._impl._util._SimpleHtmlParser.prototype.handleComment = function() {
        this._assertValues(["-","-"]);

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
                backTrackBuf = backTrackBuf.join("");
                if(backTrackBuf == "-->") {
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
    myfaces._impl._util._SimpleHtmlParser.prototype._getCurrentToken = function() {
        var token = this.tokens[this._tokenPos];
        if(null != this._currentSection) {
            this._currentSection.push(token);
        }
        if(null != this._htmlSection) {
            this._htmlSection.push(token);
        }
        return token;
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._blank = /[\s\n\t]+/;

    myfaces._impl._util._SimpleHtmlParser.prototype._skipBlank = function(skipVal) {
        var len =  this.tokens.length;
        if('undefined' == typeof  skipVal || null == skipVal) {
            skipVal = 0;
        }

        //five chars prefetching for speedups of long blank sequences




        for(this._tokenPos += skipVal;this._tokenPos < len; this._tokenPos++) {
            var token = this.tokens[this._tokenPos];
            if(token != " " && token != "\t" && token != "\n") {
                return;
            } else {
                //we do not want to lose blanks
                //inlining for speed reasons, why also could use getcurrenttoken

                if(null != this._currentSection) {
                    this._currentSection.push(token);
                }
                if(null != this._htmlSection) {
                    this._htmlSection.push(token);
                }
            //inlining end
                
            }
        }
    };

    myfaces._impl._util._SimpleHtmlParser.prototype._fetchTagname = function() {
        var tagName = [];


        tagName.push(this.tokens[this._tokenPos]);
        this._tokenPos++;
        tagName.push(this._getCurrentToken());
        this._tokenPos++;
        tagName.push(this._getCurrentToken());
        this._tokenPos++;
        tagName.push(this._getCurrentToken());
        this._tokenPos++;

        tagName = tagName.join("").toLowerCase();
        return tagName;
    };
}