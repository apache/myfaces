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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;

import org.apache.myfaces.util.CommentUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for HtmlResponseWriterImpl.
 */
public class HtmlResponseWriterImplTest extends AbstractFacesTestCase
{
    
    private static final String COMMENT_START = "<!--";
    private static final String COMMENT_END = "//-->";

    private StringWriter _stringWriter;
    private HtmlResponseWriterImpl _writer;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        
        _stringWriter = new StringWriter();
        _writer = new HtmlResponseWriterImpl(_stringWriter, "text/html", "ISO-8859-1");
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        _writer = null;
        _stringWriter = null;
        
        super.tearDown();
    }
    
    /**
     * This test tests if it is possible to render HTML elements inside
     * a script section without confusing the HtmlResponseWriterImpl.
     * The related issue to this test is MYFACES-2668.
     * 
     * @throws IOException
     * @throws NoSuchFieldException 
     * @throws SecurityException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @Test
    public void testHtmlElementsInsideScript() throws IOException, SecurityException, 
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        // use reflection to get the field _isInsideScript to verify
        // the internal behavior of HtmlResponseWriterImpl
        Field insideScriptField = _writer.getClass().getDeclaredField("_isInsideScript");
        insideScriptField.setAccessible(true);
        
        _writer.startDocument();
        _writer.startElement("head", null);
        
        Assertions.assertFalse(getFieldBooleanValue(insideScriptField, _writer, false),
                "We have not entered a script element yet, so _isInsideScript should be false (or null).");
        
        _writer.startElement("script", null);
        
        Assertions.assertTrue(getFieldBooleanValue(insideScriptField, _writer, false),
                "We have now entered a script element, so _isInsideScript should be true.");
        
        _writer.startElement("table", null);
        _writer.startElement("tr", null);
        _writer.startElement("td", null);
        
        Assertions.assertTrue(getFieldBooleanValue(insideScriptField, _writer, false),
                "We have now opened various elements inside a script element, "+
                "but _isInsideScript should still be true.");
        
        _writer.write("column value");
        
        Assertions.assertTrue(getFieldBooleanValue(insideScriptField, _writer, false),
                "We have now written some text inside a script element, "+
                "but _isInsideScript should still be true.");
        
        _writer.endElement("td");
        _writer.endElement("tr");
        _writer.endElement("table");
        _writer.endElement("script");
        
        Assertions.assertFalse(getFieldBooleanValue(insideScriptField, _writer, false),
                "We have now closed the script element, so _isInsideScript should be " +
                "false (or null).");
        
        _writer.endElement("head");
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertTrue(output.contains(COMMENT_START), 
                "A script start was rendered, so the output has to " +
                "contain " + COMMENT_START);
        Assertions.assertTrue(output.contains(COMMENT_END), 
                "A script end was rendered so the output has to " + 
                "contain " + COMMENT_END);
    }
    
    /**
     * Utility method to get the value of the given Field, which is of
     * type java.lang.Boolean. If it is null, the given defaulValue will
     * be returned.
     * 
     * @param field
     * @param instance
     * @param defaultValue
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private boolean getFieldBooleanValue(Field field, Object instance, boolean defaultValue) 
            throws IllegalArgumentException, IllegalAccessException
    {
        Boolean b = (Boolean) field.get(instance);
        return b == null ? defaultValue : b;
    }

    @Test
    public void testScriptOnHtmlIsoEncodingAndScriptXhmlComments() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "text/html", "ISO-8859-1", true);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.SCRIPT_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.SCRIPT_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertTrue(output.contains(CommentUtils.COMMENT_SIMPLE_START), "script does not have start comment <!-- ");
        Assertions.assertTrue(output.contains("//"+CommentUtils.COMMENT_SIMPLE_END), "script does not have end comment --> ");
    }
    
    @Test
    public void testScriptOnHtmlIsoEncodingAndNoScriptXhmlComments() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "text/html", "ISO-8859-1", false);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.SCRIPT_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.SCRIPT_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertFalse(output.contains(CommentUtils.COMMENT_SIMPLE_START), "script have start comment <!-- ");
        Assertions.assertFalse(output.contains("//"+CommentUtils.COMMENT_SIMPLE_END), "script have end comment --> ");
    }

    @Test
    public void testScriptOnHtmlUTF8AndScriptXhmlComments() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "text/html", "UTF-8", true);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.SCRIPT_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.SCRIPT_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertTrue(output.contains(CommentUtils.COMMENT_SIMPLE_START), "script does not have start comment <!-- ");
        Assertions.assertTrue(output.contains("//"+CommentUtils.COMMENT_SIMPLE_END), "script does not have end comment --> ");
    }
    
    @Test
    public void testScriptOnHtmlUTF8AndNoScriptXhmlComments() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "text/html", "UTF-8", false);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.SCRIPT_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.SCRIPT_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertFalse(output.contains(CommentUtils.COMMENT_SIMPLE_START), "script have start comment <!-- ");
        Assertions.assertFalse(output.contains("//"+CommentUtils.COMMENT_SIMPLE_END), "script have end comment --> ");
    }

    @Test
    public void testScriptOnXhtmlIsoEncoding() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "application/xhtml+xml", "ISO-8859-1", true);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.SCRIPT_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.SCRIPT_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertTrue(output.contains(CommentUtils.INLINE_SCRIPT_COMMENT+CommentUtils.CDATA_SIMPLE_START), "script does not have start <![CDATA[ ");
        Assertions.assertTrue(output.contains(CommentUtils.INLINE_SCRIPT_COMMENT+CommentUtils.CDATA_SIMPLE_END), "script does not have end ]]> ");
    }

    @Test
    public void testScriptOnXhtmlUTF8Encoding() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "application/xhtml+xml", "UTF-8", false);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.SCRIPT_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.SCRIPT_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertTrue(output.contains(CommentUtils.INLINE_SCRIPT_COMMENT+CommentUtils.CDATA_SIMPLE_START), "script does not have start <![CDATA[ ");
        Assertions.assertTrue(output.contains(CommentUtils.INLINE_SCRIPT_COMMENT+CommentUtils.CDATA_SIMPLE_END), "script does not have end ]]> ");
    }
    
    @Test
    public void testStyleOnXhtmlIsoEncoding() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "application/xhtml+xml", "ISO-8859-1", true);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.STYLE_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.STYLE_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertTrue(output.contains(CommentUtils.CDATA_SIMPLE_START), "script does not have start <![CDATA[ ");
        Assertions.assertTrue(output.contains(CommentUtils.CDATA_SIMPLE_END), "script does not have end ]]> ");
    }

    @Test
    public void testStyleOnXhtmlUTF8Encoding() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "application/xhtml+xml", "UTF-8", false);
        String innerScript = "document.write('HELLO');"; 
        _writer.startDocument();
        _writer.startElement(HTML.STYLE_ELEM, null);
        _writer.write(innerScript);
        _writer.endElement(HTML.STYLE_ELEM);
        _writer.endDocument();
        
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains(innerScript), "script does not contain body:" + innerScript);
        Assertions.assertTrue(output.contains(CommentUtils.CDATA_SIMPLE_START), "script does not have start <![CDATA[ ");
        Assertions.assertTrue(output.contains(CommentUtils.CDATA_SIMPLE_END), "script does not have end ]]> ");
    }
    
    /**
     * In html, it is not valid to have an empty tag with content
     * 
     * @throws IOException
     */
    @Test
    public void testEmptyTagNotRenderEnd() throws IOException
    {
        _writer.startDocument();
        _writer.startElement("body", null);
        _writer.startElement("br", null);
        _writer.writeText("hello", null);
        _writer.endElement("br");
        _writer.endElement("body");
        _writer.endDocument();
       
        // the following should render <br />hello
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("<br />"));
        Assertions.assertFalse(output.contains("</br>"));
    }
    
    /**
     * In xhtml, it is valid to have an html empty tag with content.
     * 
     * @throws IOException
     */
    @Test
    public void testEmptyTagNotRenderEndOnXml() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "application/xml", "UTF-8", false);
        
        _writer.startDocument();
        _writer.startElement("body", null);
        _writer.startElement("br", null);
        _writer.writeText("hello", null);
        _writer.endElement("br");
        _writer.endElement("body");
        _writer.endDocument();
        
     // the following should render <br>hello</br>
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("<br>"));
        Assertions.assertTrue(output.contains("</br>"));
    }
    
    /**
     * In html, it is not valid to have an empty tag with content
     * 
     * @throws IOException
     */
    @Test
    public void testEmptyTagNotRenderEndUppercase() throws IOException
    {
        _writer.startDocument();
        _writer.startElement("body", null);
        _writer.startElement("BR", null);
        _writer.writeText("hello", null);
        _writer.endElement("BR");
        _writer.endElement("body");
        _writer.endDocument();
       
        // the following should render <br />hello
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("<BR />"));
        Assertions.assertFalse(output.contains("</BR>"));
    }
    
    /**
     * In xhtml, it is valid to have an html empty tag with content.
     * 
     * @throws IOException
     */
    @Test
    public void testEmptyTagNotRenderEndOnXhtmlUppercase() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "application/xml", "UTF-8", false);
        
        _writer.startDocument();
        _writer.startElement("body", null);
        _writer.startElement("BR", null);
        _writer.writeText("hello", null);
        _writer.endElement("BR");
        _writer.endElement("body");
        _writer.endDocument();
        
     // the following should render <br>hello</br>
        String output = _stringWriter.toString();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("<BR>"));
        Assertions.assertTrue(output.contains("</BR>"));
    }
}
