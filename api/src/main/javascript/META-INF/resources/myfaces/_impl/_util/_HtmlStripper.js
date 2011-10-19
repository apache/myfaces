/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/**
 * @class
 * @name _HtmlStripper
 * @memberOf myfaces._impl._util
 * @extends myfaces._impl.core._Runtime
 * @description
 *  Fallback routine if the browser embedded xml parser fails on the document
 *  This fallback is not failsafe but should give enough cover to handle all cases
 */

/** @namespace myfaces._impl._util._HtmlStripper */
_MF_CLS(_PFX_UTIL+"_HtmlStripper", Object,
/** @lends myfaces._impl._util._HtmlStripper.prototype */
{
    BEGIN_TAG: "html",
    END_TAG: "lmth",
    /**
     * main parse routine parses the document for a given tag name
     *
     *
     * @param theString  the markup string to be parsed
     * @param tagNameStart the tag name to be parsed for
     */
    parse : function(theString, tagNameStart) {
        this.tokens = theString.split("");
        this.tagAttributes = {};

        this._tagStart = -1;
        this._tagEnd = -1;

        this._contentStart = -1;
        this._contentEnd = -1;
        this._tokenPos = 0;

        this._tokenForward = 1;

        this.tagNameStart = (!tagNameStart) ? this.BEGIN_TAG : tagNameStart;

        //no need for ll parsing a handful of indexofs instead of slower regepx suffices

        var proposedTagStartPos = theString.indexOf("<"+tagNameStart);

        while(this._contentStart == -1 && proposedTagStartPos != -1) {
            if(this.checkBackForComment(theString, proposedTagStartPos))  {
                this._tagStart = proposedTagStartPos;
                this._contentStart = proposedTagStartPos+theString.substring(proposedTagStartPos).indexOf(">")+1;
            }
            proposedTagStartPos = theString.substring(proposedTagStartPos+tagNameStart.length+2).indexOf("<"+tagNameStart);
        }

        var proposedEndTagPos = theString.lastIndexOf("</"+tagNameStart);
        while(this._contentEnd == -1 && proposedEndTagPos > 0) {
            if(this.checkForwardForComment(theString, proposedEndTagPos))  {
                this._tagEnd = proposedEndTagPos;
                this._contentEnd = proposedEndTagPos;
            }
            proposedTagStartPos = theString.substring(proposedTagStartPos-tagNameStart.length-2).lastIndexOf("</"+tagNameStart);
        }
        if(this._contentStart != -1 && this._contentEnd != -1) {
            return theString.substring(this._contentStart, this._contentEnd);
        }
        return null;
    },
    
    checkForwardForComment: function(theStr, tagPos) {
        var toCheck = theStr.substring(tagPos);
        var firstBeginComment = toCheck.indexOf("<!--");
        var firstEndComment = toCheck.indexOf("-->");

        var firstBeginCDATA = toCheck.indexOf("<[CDATA[");
        var firstEndCDATA = toCheck.indexOf("]]>");
        
        if(this.isValidPositionCombination(firstBeginComment, firstEndComment, firstBeginCDATA, firstEndCDATA)) {
            return true;
        }

        return firstBeginComment <= firstEndComment && firstBeginCDATA <= firstEndCDATA;
    },

    checkBackForComment: function(theStr, tagPos) {
        var toCheck = theStr.substring(tagPos);
        var lastBeginComment = toCheck.lastIndexOf("<!--");
        var lastEndComment = toCheck.lastIndexOf("-->");

        var lastBeginCDATA = toCheck.lastIndexOf("<[CDATA[");
        var lastEndCDATA = toCheck.lastIndexOf("]]>");


        if(this.isValidPositionCombination(lastBeginComment, lastEndComment, lastBeginCDATA, lastEndCDATA)) {
            //TODO we have to handle the embedded cases, for now we leave them out
            return true;
        }

    },

    isValidPositionCombination: function(pos1, pos2, pos3, pos4) {
        return pos1 <= pos2 && pos3 <= pos4;
    },

    isFullyEmbedded: function(pos1, pos2, embedPos1, embedPos2) {
        return embedPos1 < pos1 < pos2 < embedPos2;
    },

    isPartiallyEmbedded: function(pos1, pos2, embedPos1, embedPos2) {
        return embedPos1 < pos1 <  embedPos2 < pos2 || pos1 < embedPos1 < pos2 <  embedPos2  ;    
    }

});



