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

package org.apache.myfaces.view.facelets.tag.composite;

import java.io.StringWriter;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.HelloWorld;
import org.junit.Assert;
import org.junit.Test;

public class CompositeComponentCCRefTestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        super.setupComponents();
    }
    
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    @Test
    public void testCCRefOnStylesheet() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCCRefOnStylesheet.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains(".myCustomStyle { background:red }"));
    }
    
    @Test
    public void testCCRefOnScript() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCCRefOnScript.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains(".myCustomStyle { background:red }"));
        Assert.assertTrue(sw.toString().contains(".myCustomScript = 'myvalue'"));
    }
}
