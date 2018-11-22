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

package org.apache.myfaces.context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.myfaces.shared.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Test cases for our impl, which tests for the CDATA nesting
 *
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */

public class PartialResponseWriterImplTest extends AbstractJsfTestCase {

    static Logger _log = Logger.getLogger(PartialResponseWriterImplTest.class.getName());

    private final String filePath = this.getDirectory();
    
    PartialResponseWriterImpl _writer;
    StringWriter _contentCollector;
    private static final String STD_UPDATE_RESULT = "<changes><update id=\"blaId\"><![CDATA[testing]]></update>";
    private static final String CORR_OUTPUT = "checking for correct output: ";

    public PartialResponseWriterImplTest() {
        super("PartialResponseWriterImplTest");
    }

    protected void setUp() throws Exception {
        super.setUp();
        _contentCollector = new StringWriter(100);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        _contentCollector = null;
    }

    private void checkOutput(File expected, String output) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc1 = db.parse(expected.toURI().toString());
        doc1.normalizeDocument();
        InputSource is2 = new InputSource();
        is2.setCharacterStream(new StringReader(output));
        Document doc2 = db.parse(is2);
        doc2.normalizeDocument();
        assertTrue(doc1.isEqualNode(doc2));
    }

    /**
     * Get the node path
     */
    public String getPath( Node node )
    {
        StringBuilder path = new StringBuilder();

        do
        {           
            path.insert(0, node.getNodeName() );
            path.insert( 0, "/" );
        }
        while( ( node = node.getParentNode() ) != null );

        return path.toString();
    }


    protected URL getLocalFile(String name) throws FileNotFoundException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(this.filePath + "/" + name);
        if (url == null)
        {
            throw new FileNotFoundException(cl.getResource("").getFile() + name
                    + " was not found");
        }
        return url;
    }
    
    protected String getDirectory()
    {
        return this.getClass().getName().substring(0,
                this.getClass().getName().lastIndexOf('.')).replace('.', '/')
                + "/";
    }
    
    public void testNestedScriptCDATA() throws Exception {
        _writer = createTestProbe();
        try {
            _writer.startDocument();
            _writer.startUpdate("blaId");
            _writer.startElement("script", null);
            _writer.writeAttribute("type", "text/javascript", null);
            _writer.write("\n// <![CDATA[\n");
            _writer.write("var a && b;");
            _writer.write("\n// ]]>\n");
            _writer.endElement("script");
            _writer.endUpdate();
            _writer.endDocument();
            
            checkOutput(new File(getLocalFile("nestedScriptCDATA.xml").toURI()), _contentCollector.toString());
        } catch (IOException e) {
            fail(e.toString());
        }
    }
    
    public void testBasicWriteTest() {
        _writer = createTestProbe();
        try {
            //_writer.startCDATA();
            //_writer.startCDATA();
            _writer.write("testing");
            // _writer.endCDATA();
            // _writer.endCDATA();
            _writer.flush();
            _writer.close();
            assertTrue(CORR_OUTPUT, _contentCollector.toString().equals("testing"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void teststandardNestingTest() {
        _writer = createTestProbe();
        try {
            //_writer.startCDATA();
            _writer.startCDATA();
            _writer.write("testing");
            _writer.endCDATA();
            // _writer.endCDATA();
            _writer.flush();
            _writer.close();
            assertTrue(CORR_OUTPUT, _contentCollector.toString().equals("<![CDATA[testing]]>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testIllegalNestingResolvementTest() {
        _writer = createTestProbe();
        try {
            _writer.startCDATA();
            _writer.startCDATA();
            _writer.write("testing");
            _writer.endCDATA();
            _writer.endCDATA();
            _writer.flush();
            _writer.close();
            assertTrue(CORR_OUTPUT+ _contentCollector.toString(), _contentCollector.toString().equals("<![CDATA[<![CDATA[testing]]><![CDATA[]]]]><![CDATA[>]]>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testIllegalNestingResolvementTest2() {
        _writer = createTestProbe();
        try {
            _writer.startCDATA();
            _writer.startCDATA();
            _writer.write("testing");
            _writer.flush();
            _writer.close();
            assertTrue(CORR_OUTPUT+ _contentCollector.toString(), _contentCollector.toString().equals("<![CDATA[<![CDATA[testing]]>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }



    public void testStandardUpdate() {
        _writer = createTestProbe();
        try {
            _writer.startUpdate("blaId");
            _writer.write("testing");
            _writer.endUpdate();
            assertTrue(CORR_OUTPUT, _contentCollector.toString().equals(STD_UPDATE_RESULT));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testStandardUpdateNestedCDATA() {
        _writer = createTestProbe();
        try {
            _writer.startUpdate("blaId");
            _writer.startCDATA();
            _writer.write("testing");
            _writer.endCDATA();
            _writer.endUpdate();
            assertTrue(CORR_OUTPUT+_contentCollector.toString(), _contentCollector.toString().equals("<changes><update id=\"blaId\"><![CDATA[<![CDATA[testing]]><![CDATA[]]]]><![CDATA[>]]></update>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }


    public void testComponentAuthorNestingFailureTest() {
        _writer = createTestProbe();
        try {
            _writer.startUpdate("blaId");
            _writer.startCDATA();
            _writer.startCDATA();
            _writer.write("testing");
            _writer.endUpdate();
            assertTrue(CORR_OUTPUT+_contentCollector.toString(), _contentCollector.toString().equals("<changes><update id=\"blaId\"><![CDATA[<![CDATA[<![CDATA[testing]]></update>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testStandardInsertAfter() {
        _writer = createTestProbe();
        try {
            _writer.startInsertAfter("blaId");
            _writer.write("testing");
            _writer.endInsert();
            assertTrue(CORR_OUTPUT, _contentCollector.toString().equals("<changes><insert><after id=\"blaId\"><![CDATA[testing]]></after></insert>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testStandardInsertBefore() {
        _writer = createTestProbe();
        try {
            _writer.startInsertBefore("blaId");
            _writer.write("testing");
            _writer.endInsert();
            assertTrue(CORR_OUTPUT, _contentCollector.toString().equals("<changes><insert><before id=\"blaId\"><![CDATA[testing]]></before></insert>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testBrokenUserInput() {
        _writer = createTestProbe();
        try {
            _writer.startInsertBefore("blaId");
            _writer.startElement("input", null);
            _writer.writeAttribute("type","text", null);
            _writer.writeAttribute("value","]]>", null);
            _writer.endElement("input");
            _writer.endInsert();
            assertTrue(CORR_OUTPUT+_contentCollector.toString(), _contentCollector.toString().contains("value=\"]]&gt;\""));
        } catch (IOException e) {
            fail(e.toString());
        }
    }



    public void testDelete() {
        _writer = createTestProbe();
        try {
            _writer.delete("blaId");
            assertTrue(CORR_OUTPUT+_contentCollector.toString(), _contentCollector.toString().equals("<changes><delete id=\"blaId\"></delete>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testIssue4266() {
        
    }
    
    public void testWriteIllegalXmlUnicodeCharacters() {
        _writer = createTestProbe();
        try {
            String illegalChars = " \u0001\u0002\u0003\u0004\u0005\u0006\u000B\f\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F \uD7FF\uDBFF\uDC00\uE000��";
            String legalChars = "foo";
            _writer.write(illegalChars + legalChars);
            assertEquals("All illegal XML unicode characters should have been replaced by spaces", legalChars,_contentCollector.toString().trim());
            
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testWriteTextIllegalXmlUnicodeCharacters() {
        _writer = createTestProbe();
        try {
            String illegalChars = " \u0001\u0002\u0003\u0004\u0005\u0006\u000B\f\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F \uD7FF\uDBFF\uDC00\uE000��";
            String legalChars = "foo";
            _writer.writeText(illegalChars + legalChars, null);
            assertEquals("All illegal XML unicode characters should have been replaced by spaces", legalChars,_contentCollector.toString().trim());

        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /**
     * creates a new test probe (aka response writer)
     *
     * @return
     */
    private PartialResponseWriterImpl createTestProbe() {
        return new PartialResponseWriterImpl(new HtmlResponseWriterImpl(_contentCollector, null, "UTF-8"));
    }

}
