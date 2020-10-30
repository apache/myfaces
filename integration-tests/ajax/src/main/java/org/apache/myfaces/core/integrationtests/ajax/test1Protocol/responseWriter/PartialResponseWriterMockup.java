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
package org.apache.myfaces.core.integrationtests.ajax.test1Protocol.responseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author werpu
 */
public class PartialResponseWriterMockup extends MockupResponseWriter {

    public static final String RENDER_ALL_MARKER = "jakarta.faces.ViewRoot";
    public static final String VIEW_STATE_MARKER = "jakarta.faces.ViewState";
    private boolean hasChanges;
    private String insertType;

    public void delete(String targetId) throws IOException {
        startChanges();

        super.startElement("delete", null);
        super.writeAttribute("id", targetId, null);
        super.endElement("delete");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws IOException {
        if (hasChanges) {
            // Close the <insert> element, if any.

            endInsert();

            super.endElement("changes");

            hasChanges = false;
        }

        super.endElement("partial-response");
    }

    public void endError() throws IOException {
        // Close open <error-message> element.

        endCDATA();
        super.endElement("error-message");
        super.endElement("error");
    }

    public void endEval() throws IOException {
        // Close open <eval> element.

        endCDATA();
        super.endElement("eval");
    }

    public void endExtension() throws IOException {
        super.endElement("extension");
    }

    public void endInsert() throws IOException {
        if (insertType == null) {
            // No insert started; ignore.

            return;
        }

        // Close open <insert> element.

        endCDATA();
        super.endElement(insertType);
        super.endElement("insert");

        insertType = null;
    }

    public void endUpdate() throws IOException {
        endCDATA();
        super.endElement("update");
    }

    public void redirect(String url) throws IOException {
        super.startElement("redirect", null);
        super.writeAttribute("url", url, null);
        super.endElement("redirect");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() throws IOException {
        super.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        super.startElement("partial-response", null);
    }

    public void startError(String errorName) throws IOException {
        super.startElement("error", null);

        super.startElement("error-name", null);
        super.write(errorName);
        super.endElement("error-name");

        super.startElement("error-message", null);
        startCDATA();

        // Leave open; caller will write message.
    }

    public void startEval() throws IOException {
        startChanges();

        super.startElement("eval", null);
        startCDATA();

        // Leave open; caller will write statements.
    }

    public void startExtension(Map<String, String> attributes) throws IOException {
        Iterator<String> attrNames;

        startChanges();

        super.startElement("extension", null);

        // Write out extension attributes.
        // TODO: schema mentions "id" attribute; not used?

        attrNames = attributes.keySet().iterator();

        while (attrNames.hasNext()) {
            String attrName = attrNames.next();

            super.writeAttribute(attrName, attributes.get(attrName), null);
        }

        // Leave open; caller will write extension elements.
    }

    public void startInsertAfter(String targetId) throws IOException {
        startInsertCommon("after", targetId);
    }

    public void startInsertBefore(String targetId) throws IOException {
        startInsertCommon("before", targetId);
    }

    public void startUpdate(String targetId) throws IOException {
        startChanges();

        super.startElement("update", null);
        super.writeAttribute("id", targetId, null);
        startCDATA();

        // Leave open; caller will write content.
    }

    public void updateAttributes(String targetId, Map<String, String> attributes) throws IOException {
        Iterator<String> attrNames;

        startChanges();

        super.startElement("attributes", null);
        super.writeAttribute("id", targetId, null);

        attrNames = attributes.keySet().iterator();

        while (attrNames.hasNext()) {
            String attrName = attrNames.next();

            super.startElement("attribute", null);
            super.writeAttribute("name", attrName, null);
            super.writeAttribute("value", attributes.get(attrName), null);
            super.endElement("attribute");
        }

        super.endElement("attributes");
    }

    private void startChanges() throws IOException {
        if (!hasChanges) {
            super.startElement("changes", null);

            hasChanges = true;
        }
    }

    private void startInsertCommon(String type, String targetId) throws IOException {
        if (insertType != null) {
            // An insert has already been started; ignore.

            return;
        }

        insertType = type;

        startChanges();

        super.startElement("insert", null);
        super.startElement(insertType, null);
        super.writeAttribute("id", targetId, null);
        startCDATA();

        // Leave open; caller will write content.
    }

    /*
     * These methods are needed since we can't be sure that the data written by the caller will not
     * contain reserved characters.
     */
    public void endCDATA() throws IOException {
        super.write("]]>");
    }

    public void startCDATA() throws IOException {
        super.write("<![CDATA[");
    }
}
