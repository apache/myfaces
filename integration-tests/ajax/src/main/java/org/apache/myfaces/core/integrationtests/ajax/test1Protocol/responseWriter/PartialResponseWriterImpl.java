/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.myfaces.core.integrationtests.ajax.test1Protocol.responseWriter;

import jakarta.faces.component.UIComponent;
import java.io.IOException;
import java.util.Map;

/**
 * An implementation of the partial response writer
 * which pushes the scripting part of a component
 * into a separate data structure so that in the long
 * run we can deal with it in a separate eval stage.
 *
 * This is needed to be able to handle the eval properly
 * Although we can deal with scripts also by inline parsing
 * (which we make turnoffable for performance reasons)
 * the proper way is to resolve this over a ResponseWriter
 *
 * We use a wrapper here for now!
 *
 * NOTE the class is a work in progress it is not finished yet
 * and therefore not combined with the rest of the codebase
 *
 * Flow UIViewRoot: insert or update -> eval  javascripts must be deferred
 * until the insert update or delete is done
 *
 * insert or update -> script src type="text/javascript"
 * also must be delayed until the insert
 * or update is done!
 *
 * single elements startElement no script -> passthrough
 * script wait for end...
 *
 * Note our evals are directly stored in a stringbuilder, we have
 * to assume that write access is single threaded the JEE spec
 * definitely disallows multithreaded writes (the server has to provide
 * threading facilities, threads in servlets writing on the responsewriter
 * have to assume that the response writer itself is not thread safe!)
 *
 *
 */
public class PartialResponseWriterImpl extends PartialResponseWriterMockup
{

    private StringBuilder _evals = new StringBuilder();
    StringBuilder _scriptBuffer = null;
    ScriptHandler _scriptEntry = null;
    boolean _deferEval = false;

    public PartialResponseWriterImpl() {
        super();
    }

    @Override
    public void startError(String errorName) throws IOException {
        super.startError(errorName);
        _deferEval = true;
    }

    @Override
    public void startExtension(Map<String, String> attributes) throws IOException {
        super.startExtension(attributes);
        _deferEval = true;
    }

    @Override
    public void startInsertAfter(String targetId) throws IOException {
        super.startInsertAfter(targetId);
        _deferEval = true;
    }

    @Override
    public void startInsertBefore(String targetId) throws IOException {
        super.startInsertBefore(targetId);
        _deferEval = true;
    }

    @Override
    public void startUpdate(String targetId) throws IOException {
        super.startUpdate(targetId);
        _deferEval = true;
    }

    public void startEval() throws IOException {
        if (!_deferEval) {
            super.startEval();
            writeEvals();
        } else {
            //we are already in an insert update or delete
            //lets open a deferrence element
            _scriptEntry = new ScriptHandler();

        }
    }

    public void endEval() throws IOException {
        if (_scriptEntry != null) {
            _evals.append(_scriptEntry.toString());
            _scriptEntry = null;
        }
        if (!_deferEval) {
            super.endEval();
        }
    }



    public void endInsert() throws IOException {
        super.endInsert();
        flushEvals();
    }

    public void endUpdate() throws IOException {
        super.endUpdate();
        flushEvals();
    }

    /**
     * flushes the eval into a separate part
     * of the xhr response cycle
     * so that embedded scripts
     * are properly set in their corresponding
     * eval section
     * @throws java.io.IOException
     */
    private void flushEvals() throws IOException {
        if (_evals.length() == 0) {
            return;
        }
        super.startEval();
        writeEvals();
        super.endEval();
    }

    public void startElement(String name, UIComponent component) throws IOException {
        //it is either <script type="text/javascript>" or <script>

        if (isScript(name)) {
            _scriptEntry = new ScriptHandler();
            _scriptEntry.setComponent(component);
        } else {
            super.startElement(name, component);
        }
    }

    public void write(String str) throws IOException {

        if (_scriptEntry != null) {
            _scriptEntry.append(str);
        } else {
            super.write(str);
        }
    }

    public void endElement(String name) throws IOException {
        //We can probably replace this with a simple scriptBuffer check!
        if (isScript(name) && _scriptEntry != null) {
            _evals.append(_scriptEntry.toString());
            _scriptEntry = null;
        } else {
            super.endElement(name);
        }
    }

    public void writeAttribute(String name, Object value, String property) throws IOException {
        if (_scriptEntry != null && isType(name)) {
            _scriptEntry.setScriptType(value.toString());
            return;
        } else if (_scriptEntry != null && isSource(name)) {
            _scriptEntry.setSource(value.toString());
            return;
        } else if (_scriptEntry != null && isDefer(name)) {
            _scriptEntry.setDefer(value.toString());
            return;
        } else if (_scriptEntry != null && isCharset(name)) {
            _scriptEntry.setCharSet(value.toString());
            return;

        } else if (_scriptEntry != null) {
            //condition reached which we cannot eval, which means usually
            //a script tag outside of what we can process in javascript
            startElement(HTML.SCRIPT_ELEM, _scriptEntry.getComponent());

            if (_scriptEntry.getScriptType() != null) {
                writeAttribute(HTML.TYPE_ATTR, _scriptEntry.getScriptType(), null);
                _scriptEntry.setScriptType(null);
            }

            if (_scriptEntry.getSource() != null) {
                writeAttribute(HTML.SRC_ATTR, _scriptEntry.getSource(), null);
                _scriptEntry.setSource(null);
            }

            if (_scriptEntry.getDefer() != null) {
                writeAttribute("defer", _scriptEntry.getDefer(), null);
                _scriptEntry.setSource(null);
            }

            if (_scriptEntry.getCharSet() != null) {
                writeAttribute(HTML.CHARSET_ATTR, _scriptEntry.getCharSet(), null);
                _scriptEntry.setSource(null);
            }

            /*no eval condition reached*/
            _scriptEntry = null;
        }

        super.writeAttribute(name, value, property);
    }

    private boolean isDefer(String theType) {
        return theType.equalsIgnoreCase("defer");
    }

    private boolean isCharset(String theType) {
        return theType.equalsIgnoreCase(HTML.CHARSET_ATTR);
    }

    private boolean isSource(String theType) {
        return theType.equalsIgnoreCase(HTML.SRC_ATTR);
    }

    private boolean isType(String theType) {
        return theType.equalsIgnoreCase(HTML.TYPE_ATTR);
    }

    private boolean isScript(String name) {
        return name.equalsIgnoreCase(HTML.SCRIPT_ELEM);
    }

    /**
     * @return the _evals
     */
    public String getEvals() {
        return _evals.toString();
    }

    /**
     * @param evals the _evals to set
     */
    public void setEvals(String evals) {
        this._evals = new StringBuilder(evals);
    }

    private void writeEvals() throws IOException {
        super.write(_evals.toString());
        _evals = new StringBuilder();
    }

    public String toString() {
       return super.getTarget().toString();
    }


    private class ScriptHandler {

        private String _scriptType = HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT;
        private String _source = null;
        private String _charSet = null;
        private String _defer = null;
        StringBuilder _content = new StringBuilder();
        private UIComponent _component = null;

        public void append(String content) {
            _content.append(content);
        }

        public String toString() {
            StringBuilder retVal = new StringBuilder(128);
            if (getSource() != null) {
                //TODO replace this with a utils method in one of our utils sections
                retVal.append("myfaces._impl._util._Utils.loadScript('");
                retVal.append(getSource());
                retVal.append("','");
                retVal.append(getScriptType());
                retVal.append("'");
                retVal.append(",");
                retVal.append((getDefer() != null) ? getDefer() : "null");
                retVal.append(",'");
                retVal.append((getCharSet() != null) ? getCharSet() : "null");
                retVal.append("'");
                retVal.append(");");
            }
            retVal.append(_content);
            return retVal.toString();
        }

        /**
         * @return the _scriptType
         */
        public String getScriptType() {
            return _scriptType;
        }

        /**
         * @param scriptType the _scriptType to set
         */
        public void setScriptType(String scriptType) {
            this._scriptType = scriptType;
        }

        /**
         * @return the _source
         */
        public String getSource() {
            return _source;
        }

        /**
         * @param source the _source to set
         */
        public void setSource(String source) {
            this._source = source;
        }

        /**
         * @return the _component
         */
        public UIComponent getComponent() {
            return _component;
        }

        /**
         * @param component the _component to set
         */
        public void setComponent(UIComponent component) {
            this._component = component;
        }

        /**
         * @return the _charSet
         */
        public String getCharSet() {
            return _charSet;
        }

        /**
         * @param charSet the _charSet to set
         */
        public void setCharSet(String charSet) {
            this._charSet = charSet;
        }

        /**
         * @return the _defer
         */
        public String getDefer() {
            return _defer;
        }

        /**
         * @param defer the _defer to set
         */
        public void setDefer(String defer) {
            this._defer = defer;
        }
    }
}
