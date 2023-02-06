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

import jakarta.faces.component.UIParameter;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlOutcomeTargetButton;

import org.apache.myfaces.application.NavigationHandlerImpl;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.jupiter.api.Assertions;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for HtmlOutcomeTargetButtonRenderer.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlOutcomeTargetButtonRendererTest extends AbstractJsfTestCase 
{

    private MockResponseWriter writer;
    private HtmlOutcomeTargetButton outcomeTargetButton;
    private HtmlForm form;

    @Override
    @BeforeEach
    public void setUp() throws Exception 
    {
        super.setUp();
        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        facesContext.getApplication().setNavigationHandler(new NavigationHandlerImpl());
        outcomeTargetButton = new HtmlOutcomeTargetButton();
        form = new HtmlForm();
        outcomeTargetButton.setParent(form);
        
        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                outcomeTargetButton.getFamily(),
                outcomeTargetButton.getRendererType(),
                new HtmlOutcomeTargetButtonRenderer());
        facesContext.getRenderKit().addRenderer(
                form.getFamily(),
                form.getRendererType(),
                new HtmlFormRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }
    
    @Override
    @AfterEach
    public void tearDown() throws Exception 
    {
        super.tearDown();
        writer = null;
        form = null;
        outcomeTargetButton = null;
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        outcomeTargetButton.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            outcomeTargetButton.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertTrue(output.matches(".+id=\".+\".+"));
            Assertions.assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
        
    }
    
    /**
     * Tests if h:button correctly includes all parameters of the implicit
     * navigation case created from the outcome.
     * 
     * @throws Exception
     */
    @Test
    public void testOutcomeTargetRendersNavigationCaseParameters() throws Exception
    {
        // configure the button
        outcomeTargetButton.getAttributes().put("includeViewParams", false);
        outcomeTargetButton.getAttributes().put("outcome", 
                "test.xhtml?param1=value1&param2=value2");
        
        // render the button
        outcomeTargetButton.encodeAll(facesContext);
        String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
        
        // make sure the parameters are rendered
        Assertions.assertTrue(output.contains("param1=value1"));
        Assertions.assertTrue(output.contains("param2=value2"));
    }
    
    /**
     * Tests if the fragment attribute is correctly rendered.
     * @throws Exception
     */
    @Test
    public void testFragment() throws Exception
    {
        // configure the button
        final String fragment = "end";
        outcomeTargetButton.getAttributes().put("fragment", fragment);
        outcomeTargetButton.getAttributes().put("outcome", 
                "test.xhtml?param1=value1");
        
        // render the button
        outcomeTargetButton.encodeAll(facesContext);
        String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
        
        // make sure the fragment is rendered
        Assertions.assertTrue(output.contains("param1=value1#" + fragment));
    }
    
    /**
     * Tests if the h:button correctly includes an UIParameter
     * with a non-null-name when creating the URL.
     */
    @Test
    public void testIncludesUIParameterInURL()
    {
        // create the UIParameter and attach it
        UIParameter param = new UIParameter();
        param.setName("myParameter");
        param.setValue("myValue");
        outcomeTargetButton.getChildren().add(param);
        
        try
        {
            outcomeTargetButton.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertTrue(output.contains("myParameter=myValue"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }
    
    /**
     * Tests if the h:button correctly skips an UIParameter
     * with a null-name when creating the URL.
     */
    @Test
    public void testSkipsNullValueOfUIParameterInURL()
    {
        // create the UIParameter with value = null and attach it
        UIParameter param = new UIParameter();
        param.setName("myNullParameter");
        param.setValue(null);
        outcomeTargetButton.getChildren().add(param);
        
        try
        {
            outcomeTargetButton.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertFalse(output.contains("myNullParameter"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }
    
    /**
     * Tests if the h:button is rendered accordingly if disabled is true.
     */
    @Test
    public void testDisabledAttribute() 
    {
        outcomeTargetButton.getAttributes().put(ComponentAttrs.DISABLED_ATTR, Boolean.TRUE);
        try 
        {
            outcomeTargetButton.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            
            // Assertions
            Assertions.assertFalse(output.contains(HTML.ONCLICK_ATTR)); // the output must not contain onclick 
            Assertions.assertTrue(output.contains(HTML.DISABLED_ATTR)); // the ouput must contain disabled
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
        
    }
    
}
