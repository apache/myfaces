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

import java.io.StringWriter;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectManyListbox;
import javax.faces.component.html.HtmlSelectManyMenu;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.model.SelectItem;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.renderkit.html.base.HtmlResponseWriterImpl;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;

public class HtmlEscapeRendererTest extends AbstractJsfTestCase
{
    
    private static String ISO_8859_1 = "ISO-8859-1";
    private static String UTF_8 = "UTF-8";
    private static String HTML_CONTENT_TYPE = "text/html";
    private static String XHTML_CONTENT_TYPE = "application/xhtml+xml";
    private static String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    private static String TEXT_XML_CONTENT_TYPE = "text/xml";
    
    private static String[] CONTENT_TYPES = {HTML_CONTENT_TYPE,XHTML_CONTENT_TYPE,APPLICATION_XML_CONTENT_TYPE,TEXT_XML_CONTENT_TYPE};
    private static String[] ENCODINGS = {ISO_8859_1,UTF_8};
    
    private static String TEST_STRING = "<b>Out&p&amp;ut&aacute;</b>";
    private static String TEST_STRING_ESCAPED = "&lt;b&gt;Out&amp;p&amp;amp;ut&amp;aacute;&lt;/b&gt;";
    
    public static Test suite()
    {
        return new TestSuite(HtmlEscapeRendererTest.class); // needed in maven
    }
    
    private HtmlOutputText outputText;
    private HtmlOutputLabel outputLabel;
    private HtmlSelectOneRadio selectOneRadio;
    private HtmlSelectOneListbox selectOneListbox;
    private HtmlSelectOneMenu selectOneMenu;
    private HtmlSelectManyCheckbox selectManyCheckbox;
    private HtmlSelectManyListbox selectManyListbox;
    private HtmlSelectManyMenu selectManyMenu;
 
    public HtmlEscapeRendererTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        outputText = new HtmlOutputText();
        outputLabel = new HtmlOutputLabel();
        selectOneRadio = new HtmlSelectOneRadio();
        selectOneListbox = new HtmlSelectOneListbox();
        selectOneMenu = new HtmlSelectOneMenu();
        selectManyCheckbox = new HtmlSelectManyCheckbox();
        selectManyListbox = new HtmlSelectManyListbox();
        selectManyMenu = new HtmlSelectManyMenu();

        // TODO remove these two lines once myfaces-test goes alpha, see MYFACES-1155
        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                outputText.getFamily(),
                outputText.getRendererType(),
                new HtmlTextRenderer());
        facesContext.getRenderKit().addRenderer(
                outputLabel.getFamily(),
                outputLabel.getRendererType(),
                new HtmlLabelRenderer());
        facesContext.getRenderKit().addRenderer(
                selectOneRadio.getFamily(),
                selectOneRadio.getRendererType(),
                new HtmlRadioRenderer());
        facesContext.getRenderKit().addRenderer(
                selectOneListbox.getFamily(),
                selectOneListbox.getRendererType(),
                new HtmlListboxRenderer());
        facesContext.getRenderKit().addRenderer(
                selectOneMenu.getFamily(),
                selectOneMenu.getRendererType(),
                new HtmlMenuRenderer());
        facesContext.getRenderKit().addRenderer(
                selectManyCheckbox.getFamily(),
                selectManyCheckbox.getRendererType(),
                new HtmlCheckboxRenderer());
        facesContext.getRenderKit().addRenderer(
                selectManyListbox.getFamily(),
                selectManyListbox.getRendererType(),
                new HtmlListboxRenderer());
        facesContext.getRenderKit().addRenderer(
                selectManyMenu.getFamily(),
                selectManyMenu.getRendererType(),
                new HtmlMenuRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        outputText = null;
    }

    public void testOutputTextEscapeValue() throws Exception
    {
        for (String contentType: CONTENT_TYPES)
        {
            for (String encoding : ENCODINGS)
            {
                StringWriter swriter = new StringWriter();
                HtmlResponseWriterImpl writer = new HtmlResponseWriterImpl(swriter, contentType , encoding);
                facesContext.setResponseWriter(writer);
                // note if appear a &amp;, the & is escaped too,
                // but when you see the output in a html browser
                // the output it the same.
                outputText.setValue(TEST_STRING);
                outputText.setEscape(true);
                outputText.encodeEnd(facesContext);
                facesContext.renderResponse();
                String output = swriter.toString();
                assertTrue(output.contains(TEST_STRING_ESCAPED));
            }
        }
    }

    public void testOutputTextNoEscapeValue() throws Exception
    {

        for (String contentType: CONTENT_TYPES)
        {
            for (String encoding : ENCODINGS)
            {
                StringWriter swriter = new StringWriter();
                HtmlResponseWriterImpl writer = new HtmlResponseWriterImpl(swriter, contentType , encoding);
                facesContext.setResponseWriter(writer);
                outputText.setValue(TEST_STRING);
                outputText.setEscape(false);
                outputText.encodeEnd(facesContext);
                facesContext.renderResponse();
                String output = swriter.toString();
                assertTrue(output.contains(TEST_STRING));
            }
        }
    }
    
    public void testOutputLabelEscapeValue() throws Exception
    {
        for (String contentType: CONTENT_TYPES)
        {
            for (String encoding : ENCODINGS)
            {
                StringWriter swriter = new StringWriter();
                HtmlResponseWriterImpl writer = new HtmlResponseWriterImpl(swriter, contentType , encoding);
                facesContext.setResponseWriter(writer);
                // note if appear a &amp;, the & is escaped too,
                // but when you see the output in a html browser
                // the output it the same.
                outputLabel.setValue(TEST_STRING);
                outputLabel.setEscape(true);
                outputLabel.encodeAll(facesContext);
                facesContext.renderResponse();
                String output = swriter.toString();
                assertTrue(output.contains(TEST_STRING_ESCAPED));
            }
        }
    }
    
    public void testOutputLabelNoEscapeValue() throws Exception
    {

        for (String contentType: CONTENT_TYPES)
        {
            for (String encoding : ENCODINGS)
            {
                StringWriter swriter = new StringWriter();
                HtmlResponseWriterImpl writer = new HtmlResponseWriterImpl(swriter, contentType , encoding);
                facesContext.setResponseWriter(writer);
                outputLabel.setValue(TEST_STRING);
                outputLabel.setEscape(false);
                outputLabel.encodeAll(facesContext);
                facesContext.renderResponse();
                String output = swriter.toString();
                assertTrue(output.contains(TEST_STRING));
            }
        }
    }
    
    public void testUISelectOneEscapeValue() throws Exception
    {
        for (String contentType: CONTENT_TYPES)
        {
            for (String encoding : ENCODINGS)
            {
                tryUISelectEscapeValue(selectOneRadio, contentType, encoding);
                tryUISelectEscapeValue(selectOneListbox, contentType, encoding);
                tryUISelectEscapeValue(selectOneMenu, contentType, encoding);
                tryUISelectEscapeValue(selectManyCheckbox, contentType, encoding);
                tryUISelectEscapeValue(selectManyListbox, contentType, encoding);
                tryUISelectEscapeValue(selectManyMenu, contentType, encoding);
            }
        }
    }
    
    public void tryUISelectEscapeValue(UIComponent component, String contentType, String encoding) throws Exception
    {
        StringWriter swriter = new StringWriter();
        HtmlResponseWriterImpl writer = new HtmlResponseWriterImpl(swriter, contentType , encoding);
        facesContext.setResponseWriter(writer);
        UISelectItem uiSelectItem = new UISelectItem();
        SelectItem item = new SelectItem("Output",TEST_STRING,"",false,true);
        uiSelectItem.setValue(item);
        component.getChildren().add(uiSelectItem);
        component.encodeAll(facesContext);
        facesContext.renderResponse();
        String output = swriter.toString();
        assertTrue(output.contains(TEST_STRING_ESCAPED));
    }
    
    public void testUISelectNoEscapeValue() throws Exception
    {
        for (String contentType: CONTENT_TYPES)
        {
            for (String encoding : ENCODINGS)
            {
                tryUISelectNoEscapeValue(selectOneRadio, contentType, encoding);
                tryUISelectNoEscapeValue(selectOneListbox, contentType, encoding);
                tryUISelectNoEscapeValue(selectOneMenu, contentType, encoding);
                tryUISelectNoEscapeValue(selectManyCheckbox, contentType, encoding);
                tryUISelectNoEscapeValue(selectManyListbox, contentType, encoding);
                tryUISelectNoEscapeValue(selectManyMenu, contentType, encoding);
            }
        }
    }
    
    public void tryUISelectNoEscapeValue(UIComponent component, String contentType, String encoding) throws Exception
    {
        StringWriter swriter = new StringWriter();
        HtmlResponseWriterImpl writer = new HtmlResponseWriterImpl(swriter, contentType , encoding);
        facesContext.setResponseWriter(writer);
        UISelectItem uiSelectItem = new UISelectItem();
        SelectItem item = new SelectItem("Output",TEST_STRING,"",false,false);
        uiSelectItem.setValue(item);
        component.getChildren().add(uiSelectItem);
        component.encodeAll(facesContext);
        facesContext.renderResponse();
        String output = swriter.toString();
        assertTrue(output.contains(TEST_STRING));
    }
}
