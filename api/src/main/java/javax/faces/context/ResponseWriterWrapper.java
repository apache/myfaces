/*
 * Copyright 2006 The Apache Software Foundation.
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
 */

package javax.faces.context;

import java.io.IOException;
import java.io.Writer;
import javax.faces.component.UIComponent;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Stan Silvert
 */
public abstract class ResponseWriterWrapper extends ResponseWriter {
    
    protected abstract ResponseWriter getWrapped();

    public void endElement(String name) throws IOException {
        getWrapped().endElement(name);
    }

    public void writeComment(Object comment) throws IOException {
        getWrapped().writeComment(comment);
    }

    public void startElement(String name, UIComponent component) throws IOException {
        getWrapped().startElement(name, component);
    }

    public void writeText(Object text, String property) throws IOException {
        getWrapped().writeText(text, property);
    }

    public void writeText(char[] text, int off, int len) throws IOException {
        getWrapped().writeText(text, off, len);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        getWrapped().write(cbuf, off, len);
    }

    public ResponseWriter cloneWithWriter(Writer writer) {
        return getWrapped().cloneWithWriter(writer);
    }

    public void writeURIAttribute(String name, Object value, String property) throws IOException {
        getWrapped().writeURIAttribute(name, value,property);
    }

    public void close() throws IOException {
        getWrapped().close();
    }

    public void endDocument() throws IOException {
        getWrapped().endDocument();
    }

    public void flush() throws IOException {
        getWrapped().flush();
    }

    public String getCharacterEncoding() {
        return getWrapped().getCharacterEncoding();
    }

    public String getContentType() {
        return getWrapped().getContentType();
    }

    public void startDocument() throws IOException {
        getWrapped().startDocument();
    }

    public void writeAttribute(String name, Object value, String property) throws IOException {
        getWrapped().writeAttribute(name, value, property);
    }
    
}
