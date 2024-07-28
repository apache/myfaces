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

import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIParameter;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlOutcomeTargetLink;
import jakarta.faces.component.html.HtmlOutputLink;

import org.apache.myfaces.application.NavigationHandlerImpl;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlLinkRendererTest extends AbstractFacesTestCase
{

    private MockResponseWriter writer;
    private HtmlCommandLink commandLink;
    private HtmlOutputLink outputLink;
    private HtmlOutcomeTargetLink outcomeTargetLink;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        UIForm form = new UIForm();

        commandLink = new HtmlCommandLink();
        outputLink = new HtmlOutputLink();
        outputLink.setValue("http://someurl");
        outcomeTargetLink = new HtmlOutcomeTargetLink();

        form.getChildren().add(commandLink);

        writer = new MockResponseWriter(new StringWriter(), null, "UTF-8");
        facesContext.setResponseWriter(writer);
        facesContext.getApplication().setNavigationHandler(new NavigationHandlerImpl());
       


        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                commandLink.getFamily(),
                commandLink.getRendererType(),
                new HtmlLinkRenderer());
        facesContext.getRenderKit().addRenderer(
                form.getFamily(),
                form.getRendererType(),
                new HtmlFormRenderer());
        facesContext.getRenderKit().addRenderer(
                outputLink.getFamily(),
                outputLink.getRendererType(),
                new HtmlLinkRenderer());
        facesContext.getRenderKit().addRenderer(
                outcomeTargetLink.getFamily(),
                outcomeTargetLink.getRendererType(),
                new HtmlLinkRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        writer = null;
    }
    
    @Test
    public void testHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = {
            //_AccesskeyProperty
            new HtmlRenderedAttr("accesskey"),
            //_UniversalProperties
            new HtmlRenderedAttr("dir"), 
            new HtmlRenderedAttr("lang"), 
            new HtmlRenderedAttr("title"),
            //_FocusBlurProperties
            new HtmlRenderedAttr("onfocus"), 
            new HtmlRenderedAttr("onblur"),
            //_EventProperties
            new HtmlRenderedAttr("ondblclick"), 
            new HtmlRenderedAttr("onkeydown"), 
            new HtmlRenderedAttr("onkeypress"),
            new HtmlRenderedAttr("onkeyup"), 
            new HtmlRenderedAttr("onmousedown"), 
            new HtmlRenderedAttr("onmousemove"), 
            new HtmlRenderedAttr("onmouseout"),
            new HtmlRenderedAttr("onmouseover"), 
            new HtmlRenderedAttr("onmouseup"),
            //_StyleProperties
            new HtmlRenderedAttr("style"), 
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
            //_TabindexProperty
            new HtmlRenderedAttr("tabindex")
        };
        
        commandLink.setValue("outputdata");
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                commandLink, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    @Test
    public void testOutputLink() throws Exception 
    {
        HtmlRenderedAttr[] attrs = {
            //_AccesskeyProperty
            new HtmlRenderedAttr("accesskey"),
            //_UniversalProperties
            new HtmlRenderedAttr("dir"), 
            new HtmlRenderedAttr("lang"), 
            new HtmlRenderedAttr("title"),
            //_FocusBlurProperties
            new HtmlRenderedAttr("onfocus"), 
            new HtmlRenderedAttr("onblur"),
            //_EventProperties
            new HtmlRenderedAttr("onclick"), 
            new HtmlRenderedAttr("ondblclick"), 
            new HtmlRenderedAttr("onkeydown"), 
            new HtmlRenderedAttr("onkeyup"), 
            new HtmlRenderedAttr("onmousedown"), 
            new HtmlRenderedAttr("onmousemove"), 
            new HtmlRenderedAttr("onmouseout"),
            new HtmlRenderedAttr("onmouseover"), 
            new HtmlRenderedAttr("onmouseup"),
            //_StyleProperties
            new HtmlRenderedAttr("style"), 
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
            //_TabindexProperty
            new HtmlRenderedAttr("tabindex")
        };

        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                outputLink, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
    public void testClientBehaviorHolderRendersIdAndNameOutputLink() 
    {
        outputLink.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            outputLink.encodeAll(facesContext);
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
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
    public void testClientBehaviorHolderRendersIdAndNameCommandLink() 
    {
        commandLink.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            commandLink.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            //System.out.println("----OUTPUT----"+output);
            Assertions.assertTrue(output.matches("(?s).+id=\".+\".+"));
            Assertions.assertTrue(output.matches("(?s).+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
        
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
    public void testClientBehaviorHolderRendersIdAndNameOutcomeTargetLink() 
    {
        outcomeTargetLink.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            outcomeTargetLink.encodeAll(facesContext);
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
     * Tests if h:link correctly includes all parameters of the implicit
     * navigation case created from the outcome.
     * 
     * @throws Exception
     */
    @Test
    public void testOutcomeTargetRendersNavigationCaseParameters() throws Exception
    {
        // configure the link
        outcomeTargetLink.getAttributes().put("includeViewParams", false);
        outcomeTargetLink.getAttributes().put("outcome", 
                "test.xhtml?param1=value1&param2=value2");
        
        // render the link
        outcomeTargetLink.encodeAll(facesContext);
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
    public void testOutcomeTargetLinkFragment() throws Exception
    {
        // configure the link
        final String fragment = "end";
        outcomeTargetLink.getAttributes().put("fragment", fragment);
        outcomeTargetLink.getAttributes().put("outcome", 
                "test.xhtml?param1=value1");
        
        // render the link
        outcomeTargetLink.encodeAll(facesContext);
        String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
        
        // make sure the fragment is rendered
        Assertions.assertTrue(output.contains("param1=value1#" + fragment));
    }
    
    /**
     * Test for the right use of the fragment attribute.
     * The value of the fragment attribute is appended to the end of target URL following a hash (#) mark.
     * @throws Exception
     */
    @Test
    public void testOutputLinkFragment() throws Exception
    {
        outputLink.setFragment("fragment");
        outputLink.setValue("http://www.irian.at");
        outputLink.encodeAll(facesContext);
        String output = writer.getWriter().toString();
        Assertions.assertEquals("<a href=\"http://www.irian.at#fragment\"></a>", output);
    }
    
    /**
     * If the disable attribute of a child UIParameter is true,
     * he should be ignored.
     * @throws Exception
     */
    @Test
    public void testDisabledUIParameterNotRenderedCommandLink() throws Exception
    {
        UIParameter param1 = new UIParameter();
        param1.setName("param1");
        param1.setValue("value1");
        param1.setDisable(true);
        UIParameter param2 = new UIParameter();
        param2.setName("param2");
        param2.setValue("value2");
        commandLink.getChildren().add(param1);
        commandLink.getChildren().add(param2);
        
        commandLink.encodeAll(facesContext);
        String output = writer.getWriter().toString();
        Assertions.assertFalse(output.contains("param1"));
        Assertions.assertFalse(output.contains("value1"));
        Assertions.assertTrue(output.contains("param2"));
        Assertions.assertTrue(output.contains("value2"));
    }
    
    /**
     * If the disable attribute of a child UIParameter is true,
     * he should be ignored.
     * @throws Exception
     */
    @Test
    public void testDisabledUIParameterNotRenderedOutputLink() throws Exception
    {
        UIParameter param1 = new UIParameter();
        param1.setName("param1");
        param1.setValue("value1");
        param1.setDisable(true);
        UIParameter param2 = new UIParameter();
        param2.setName("param2");
        param2.setValue("value2");
        outputLink.getChildren().add(param1);
        outputLink.getChildren().add(param2);
        
        outputLink.encodeAll(facesContext);
        String output = writer.getWriter().toString();
        Assertions.assertFalse(output.contains("param1"));
        Assertions.assertFalse(output.contains("value1"));
        Assertions.assertTrue(output.contains("param2"));
        Assertions.assertTrue(output.contains("value2"));
    }
    
    /**
     * Tests if the h:link correctly includes an UIParameter
     * with a non-null-name when creating the URL.
     */
    @Test
    public void testOutcomeTargetLinkIncludesUIParameterInURL()
    {
        // create the UIParameter and attach it
        UIParameter param = new UIParameter();
        param.setName("myParameter");
        param.setValue("myValue");
        outcomeTargetLink.getChildren().add(param);
        
        try
        {
            outcomeTargetLink.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertTrue(output.contains("myParameter=myValue"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }
    
    /**
     * Tests if the h:link correctly skips an UIParameter
     * with a null-name when creating the URL.
     */
    @Test
    public void testOutcomeTargetLinkSkipsNullValueOfUIParameterInURL()
    {
        // create the UIParameter with value = null and attach it
        UIParameter param = new UIParameter();
        param.setName("myNullParameter");
        param.setValue(null);
        outcomeTargetLink.getChildren().add(param);
        
        try
        {
            outcomeTargetLink.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertFalse(output.contains("myNullParameter"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }
    
}
