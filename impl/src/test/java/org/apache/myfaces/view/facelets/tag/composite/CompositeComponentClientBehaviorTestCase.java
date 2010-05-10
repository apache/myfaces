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

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.HelloWorld;

public class CompositeComponentClientBehaviorTestCase extends FaceletTestCase
{
    public void testSimpleClientBehavior() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleClientBehavior.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        assertNotNull(button);
        assertNotNull(button.getClientBehaviors().get("action"));
        assertEquals(1, button.getClientBehaviors().get("action").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
    
    public void testSimpleClientBehaviorDefault() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleClientBehaviorDefault.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        assertNotNull(button);
        assertNotNull(button.getClientBehaviors().get("dblclick"));
        assertEquals(1, button.getClientBehaviors().get("dblclick").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }

    public void testSimpleClientBehaviorDefaultNoEvent() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleClientBehaviorDefaultNoEvent.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        assertNotNull(button);
        assertNotNull(button.getClientBehaviors().get("action"));
        assertEquals(1, button.getClientBehaviors().get("action").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }

    public void testSimpleClientBehaviorAjaxWrap() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleClientBehaviorAjaxWrap.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        assertNotNull(button);
        assertNotNull(button.getClientBehaviors().get("action"));
        assertEquals(1, button.getClientBehaviors().get("action").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }

    public void testSimpleClientBehaviorDefaultAjaxWrap() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleClientBehaviorDefaultAjaxWrap.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        assertNotNull(button);
        assertNotNull(button.getClientBehaviors().get("dblclick"));
        assertEquals(1, button.getClientBehaviors().get("dblclick").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }

    public void testSimpleClientBehaviorDefaultNoEventAjaxWrap() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleClientBehaviorDefaultNoEventAjaxWrap.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        assertNotNull(button);
        assertNotNull(button.getClientBehaviors().get("action"));
        assertEquals(1, button.getClientBehaviors().get("action").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
    
    public void testCompositeClientBehavior() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeClientBehavior.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("button3");
        assertNotNull(compositeComponent2);
        UICommand button = (UICommand) compositeComponent2.findComponent("button");
        assertNotNull(button);
        //One added in testCompositeActionSource, the other one
        //inside compositeActionSource.xhtml
        assertNotNull(button.getClientBehaviors().get("action"));
        assertEquals(1, button.getClientBehaviors().get("action").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }

    public void testCompositeDoubleClientBehavior() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeDoubleClientBehavior.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        assertNotNull(compositeComponent);
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("compositeClientBehavior");
        assertNotNull(compositeComponent2);
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent2.findComponent("button3");
        assertNotNull(compositeComponent3);
        UICommand button = (UICommand) compositeComponent3.findComponent("button");
        assertNotNull(button);
        //One added in testCompositeActionSource, the other one
        //inside compositeActionSource.xhtml
        assertNotNull(button.getClientBehaviors().get("action"));
        assertEquals(1, button.getClientBehaviors().get("action").size());
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
}
