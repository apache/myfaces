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

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockValueExpression;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlTextRendererTest extends AbstractJsfTestCase
{

    public static Test suite()
    {
        return new TestSuite(HtmlTextRendererTest.class); // needed in maven
    }

    private MockResponseWriter writer ;
    private HtmlOutputText outputText;
    private HtmlInputText inputText;

    public HtmlTextRendererTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        outputText = new HtmlOutputText();
        inputText = new HtmlInputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        // TODO remove these two lines once myfaces-test goes alpha, see MYFACES-1155
        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                outputText.getFamily(),
                outputText.getRendererType(),
                new HtmlTextRenderer());
        facesContext.getRenderKit().addRenderer(
                inputText.getFamily(),
                inputText.getRendererType(),
                new HtmlTextRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        outputText = null;
        inputText = null;
        writer = null;
    }

    public void testStyleClassAttr() throws IOException
    {
        outputText.setValue("Output");
        outputText.setStyleClass("myStyleClass");

        outputText.encodeBegin(facesContext);
        outputText.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("<span class=\"myStyleClass\">Output</span>", output);
        assertNotSame("Output", output);
    }
    
    /**
     * Don't add span over escape
     * @throws IOException
     */
    public void testEscapeNoSpan() throws IOException
    {
        outputText.setValue("Output");
        outputText.setEscape(true);

        outputText.encodeBegin(facesContext);
        outputText.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("Output", output);
    }

    public void testHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicAttrs();
        

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                inputText, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testWhenSubmittedValueIsNullDefaultShouldDissapearFromRendering() {
        //See MYFACES-2161 and MYFACES-1549 for details
        UIViewRoot root = new UIViewRoot();
        UIForm form = new UIForm();
        form.setId("formId");
        
        form.getChildren().add(inputText);
        root.getChildren().add(form);
        
        Converter converter = new Converter()
        {
            public Object getAsObject(FacesContext context,
                    UIComponent component, String value)
                    throws ConverterException
            {
                if (value == null || "".equals(value))
                {
                    return null;
                }
                else
                {
                    return value;
                }
            }

            public String getAsString(FacesContext context,
                    UIComponent component, Object value)
                    throws ConverterException
            {
                if (value == null)
                {
                    return "";
                }
                else
                {
                    return value.toString();
                }
            }
        };
        
        inputText.setConverter(converter);
        
        ValueExpression expression = new MockValueExpression("#{requestScope.someDefaultValueOnBean}",String.class);
        expression.setValue(facesContext.getELContext(), "defaultValue");
        inputText.setValueExpression("value", expression);
        
        // 1) user enters an empty string in an input-component: ""
        //Call to setSubmittedValue on HtmlRendererUtils.decodeUIInput(facesContext, component), 
        //that is called from renderer decode()
        externalContext.addRequestParameterMap(inputText.getClientId(facesContext), "");
        
        inputText.decode(facesContext);
        
        // 2) conversion and validation phase: "" --> setValue(null);
        // isLocalValueSet = true; setSubmittedValue(null);
        inputText.validate(facesContext);
        
        // 3) validation fails in some component on the page --> update model
        // phase is skipped
        // No OP
        
        // 4) renderer calls getValue(); --> getValue() evaluates the
        // value-binding, as the local-value is 'null', and I get the
        // default-value of the bean shown again
        assertNotSame(expression.getValue(facesContext.getELContext()), inputText.getValue());
        assertNull(inputText.getValue());
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        inputText.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            inputText.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            assertTrue(output.matches("(?s).+id=\".+\".+"));
            assertTrue(output.matches("(?s).+name=\".+\".+"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
    }
    
    /**
     * Tests if a JavaScript user code is correctly escaped.
     * e.g. alert('test') has to become alert(\'test\')
     */
    public void testClientBehaviorUserCodeJavaScriptEscaping()
    {
        inputText.getAttributes().put("onchange", "alert('test')");
        inputText.addClientBehavior("change", new AjaxBehavior());
        try 
        {
            inputText.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            // onchange="jsf.util.chain(document.getElementById(&apos;j_id0&apos;), event,
            //                          &apos;alert(\&apos;test\&apos;)&apos;);"
            assertTrue(output.contains("&apos;alert(\\&apos;test\\&apos;)&apos;"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests if a JavaScript user code that already contains ' is correctly escaped.
     * e.g. test = 'a\'b'; has to become test = \'a\\\'b\';
     */
    public void testClientBehaviorUserCodeJavaScriptDoubleEscaping()
    {
        inputText.getAttributes().put("onchange", "var test = \'a\\\'b\'; alert(test);");
        inputText.addClientBehavior("change", new AjaxBehavior());
        try 
        {
            inputText.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            // onchange="jsf.util.chain(document.getElementById(&apos;j_id0&apos;), event,
            //               &apos;var test = \&apos;a\\\&apos;b\&apos;; alert(test);&apos;);"
            assertTrue(output.contains("&apos;var test = \\&apos;a\\\\\\&apos;b\\&apos;; alert(test);&apos;"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
}
