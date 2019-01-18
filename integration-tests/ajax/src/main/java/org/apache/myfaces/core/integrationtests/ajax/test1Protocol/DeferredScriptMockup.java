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

package org.apache.myfaces.core.integrationtests.ajax.test1Protocol;

import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.responseWriter.PartialResponseWriterImpl;

import java.io.IOException;

/**
 *
 * @author werpu
 *
 * code to test script deferring if a script
 * and script src tag is discovered
 */
public class DeferredScriptMockup
{

    public String testScriptMockup() throws IOException {
        PartialResponseWriterImpl writer = new PartialResponseWriterImpl();
        writer.startDocument();
        writer.startUpdate(PartialResponseWriterImpl.RENDER_ALL_MARKER);
        writer.startElement("h1", null);
        writer.write("Dies ist ein text");
        writer.endElement("h1");
        
        writer.startElement("script",null);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("alert('hello world');");
        writer.endElement("script");

        writer.startElement("h1", null);
        writer.write("Dies ist ein text");
        writer.endElement("h1");


        writer.startEval();
        writer.write("alert('hello world from eval');");
        writer.endEval();

        writer.endUpdate();

        writer.endDocument();

        return writer.toString();

    }

}
