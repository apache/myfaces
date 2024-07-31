/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package jakarta.faces.context;

import jakarta.faces.component.UIComponent;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class PartialResponseWriterWrapper extends PartialResponseWriter
{
    private PartialResponseWriter wrapped;

    public PartialResponseWriterWrapper(PartialResponseWriter wrapped)
    {
        super(wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public PartialResponseWriter getWrapped()
    {
        return wrapped;
    }

    @Override
    public void startDocument() throws IOException
    {
        getWrapped().startDocument();
    }

    @Override
    public void endDocument() throws IOException
    {
        getWrapped().endDocument();
    }

    @Override
    public void startInsertBefore(String targetId) throws IOException
    {
        getWrapped().startInsertBefore(targetId);
    }

    @Override
    public void startInsertAfter(String targetId) throws IOException
    {
        getWrapped().startInsertAfter(targetId);
    }

    @Override
    public void endInsert() throws IOException
    {
        getWrapped().endInsert();
    }

    @Override
    public void startUpdate(String targetId) throws IOException
    {
        getWrapped().startUpdate(targetId);
    }

    @Override
    public void endUpdate() throws IOException
    {
        getWrapped().endUpdate();
    }

    @Override
    public void updateAttributes(String targetId, Map<String, String> attributes) throws IOException
    {
        getWrapped().updateAttributes(targetId, attributes);
    }

    @Override
    public void delete(String targetId) throws IOException
    {
        getWrapped().delete(targetId);
    }

    @Override
    public void redirect(String url) throws IOException
    {
        getWrapped().redirect(url);
    }

    @Override
    public void startEval() throws IOException
    {
        getWrapped().startEval();
    }

    @Override
    public void endEval() throws IOException
    {
        getWrapped().endEval();
    }

    @Override
    public void startExtension(Map<String, String> attributes) throws IOException
    {
        getWrapped().startExtension(attributes);
    }

    @Override
    public void endExtension() throws IOException
    {
        getWrapped().endExtension();
    }

    @Override
    public void startError(String errorName) throws IOException
    {
        getWrapped().startError(errorName);
    }

    @Override
    public void endError() throws IOException
    {
        getWrapped().endError();
    }

    @Override
    public String getContentType()
    {
        return getWrapped().getContentType();
    }

    @Override
    public String getCharacterEncoding()
    {
        return getWrapped().getCharacterEncoding();
    }

    @Override
    public void flush() throws IOException
    {
        getWrapped().flush();
    }

    @Override
    public void startElement(String name, UIComponent component) throws IOException
    {
        getWrapped().startElement(name, component);
    }

    @Override
    public void startCDATA() throws IOException
    {
        getWrapped().startCDATA();
    }

    @Override
    public void endCDATA() throws IOException
    {
        getWrapped().endCDATA();
    }

    @Override
    public void endElement(String name) throws IOException
    {
        getWrapped().endElement(name);
    }

    @Override
    public void writeAttribute(String name, Object value, String property) throws IOException
    {
        getWrapped().writeAttribute(name, value, property);
    }

    @Override
    public void writeURIAttribute(String name, Object value, String property) throws IOException
    {
        getWrapped().writeURIAttribute(name, value, property);
    }

    @Override
    public void writeComment(Object comment) throws IOException
    {
        getWrapped().writeComment(comment);
    }

    @Override
    public void writeText(Object text, String property) throws IOException
    {
        getWrapped().writeText(text, property);
    }

    @Override
    public void writeText(Object text, UIComponent component, String property) throws IOException
    {
        getWrapped().writeText(text, component, property);
    }

    @Override
    public void writeText(char[] text, int off, int len) throws IOException
    {
        getWrapped().writeText(text, off, len);
    }

    @Override
    public ResponseWriter cloneWithWriter(Writer writer)
    {
        return getWrapped().cloneWithWriter(writer);
    }

    @Override
    public void close() throws IOException
    {
        getWrapped().close();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        getWrapped().write(cbuf, off, len);
    }

    @Override
    public void write(int c) throws IOException
    {
        getWrapped().write(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException
    {
        getWrapped().write(cbuf);
    }

    @Override
    public void write(String str) throws IOException
    {
        getWrapped().write(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException
    {
        getWrapped().write(str, off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException
    {
        return getWrapped().append(csq);
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException
    {
        return getWrapped().append(csq, start, end);
    }

    @Override
    public Writer append(char c) throws IOException
    {
        return getWrapped().append(c);
    }
}
