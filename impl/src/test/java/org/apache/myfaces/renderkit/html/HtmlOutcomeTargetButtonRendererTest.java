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

import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlOutcomeTargetButton;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.application.NavigationHandlerImpl;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;

public class HtmlOutcomeTargetButtonRendererTest extends AbstractJsfTestCase {

    private MockResponseWriter writer;
    private HtmlOutcomeTargetButton outcomeTargetButton;
    private HtmlForm form;
    
    public HtmlOutcomeTargetButtonRendererTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(HtmlOutcomeTargetButtonRendererTest.class);
    }
    
    public void setUp() throws Exception {
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
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        writer = null;
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        outcomeTargetButton.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            outcomeTargetButton.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            assertTrue(output.matches(".+id=\".+\".+"));
            assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
    }
    
}
