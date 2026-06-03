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
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for HtmlResponseWriterImpl.
 */
public class HtmlResponseWriterImplTest extends AbstractJsfTestCase
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
     * writeText(char[]) inside a textarea must NOT map successive spaces to &amp;nbsp;
     * or newlines to &lt;br/&gt; — this is the key purpose of the _isTextArea flag.
     */
    @Test
    public void testTextareaWriteTextPreservesSpacesAndNewlines() throws IOException
    {
        _writer.startDocument();
        _writer.startElement("form", null);
        _writer.startElement(HTML.TEXTAREA_ELEM, null);
        _writer.writeText("hello   world\nfoo".toCharArray(), 0, 17);
        _writer.endElement(HTML.TEXTAREA_ELEM);
        _writer.endElement("form");
        _writer.endDocument();

        String output = _stringWriter.toString();
        // Spaces must not be turned into &nbsp; inside a textarea
        Assertions.assertFalse(output.contains("&nbsp;"),
                "textarea: spaces must not be encoded as &nbsp;");
        // Newlines must not be turned into <br/> inside a textarea
        Assertions.assertFalse(output.contains("<br"),
                "textarea: newlines must not be encoded as <br/>");
        // The original characters must appear verbatim
        Assertions.assertTrue(output.contains("hello   world"),
                "textarea: original spaces must be present in output");
    }

    /**
     * Same as testTextareaWriteTextPreservesSpacesAndNewlines but with uppercase element name
     * to verify case-insensitive textarea detection.
     */
    @Test
    public void testTextareaWriteTextUppercase() throws IOException
    {
        _writer.startDocument();
        _writer.startElement("form", null);
        _writer.startElement("TEXTAREA", null);
        _writer.writeText("hello   world".toCharArray(), 0, 13);
        _writer.endElement("TEXTAREA");
        _writer.endElement("form");
        _writer.endDocument();

        String output = _stringWriter.toString();
        Assertions.assertFalse(output.contains("&nbsp;"),
                "TEXTAREA (uppercase): spaces must not be encoded as &nbsp;");
    }

    /**
     * writeText(Object) with a non-String value must produce the same output as
     * writeText(Object) with the equivalent String — exercises the toString() fallback path.
     */
    @Test
    public void testWriteTextObjectNonString() throws IOException
    {
        _writer.startDocument();
        _writer.startElement("body", null);
        _writer.writeText(Integer.valueOf(42), null);
        _writer.endElement("body");
        _writer.endDocument();

        Assertions.assertTrue(_stringWriter.toString().contains("42"),
                "Non-String value (Integer 42) must be rendered as its toString()");
    }

    /**
     * Style content on plain text/html must be written verbatim without toString() overhead
     * (exercises the StreamCharBuffer.writeTo path in writeStyleContent).
     */
    @Test
    public void testStyleOnHtmlWritesContentVerbatim() throws IOException
    {
        _writer = new HtmlResponseWriterImpl(_stringWriter, "text/html", "UTF-8", false);
        String css = "body { color: red; }";
        _writer.startDocument();
        _writer.startElement(HTML.STYLE_ELEM, null);
        _writer.write(css);
        _writer.endElement(HTML.STYLE_ELEM);
        _writer.endDocument();

        String output = _stringWriter.toString();
        Assertions.assertTrue(output.contains(css),
                "Plain HTML style content must appear verbatim in output");
        // In plain text/html there is no CDATA wrapping
        Assertions.assertFalse(output.contains(CommentUtils.CDATA_SIMPLE_START),
                "Plain HTML style must not be wrapped in CDATA");
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