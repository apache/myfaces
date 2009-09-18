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
package org.apache.myfaces.view.facelets.tag.jsf.html;

import java.io.StringWriter;

import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.shale.test.mock.MockResponseWriter;

public class HtmlOutputScriptTestCase extends FaceletTestCase
{
    public void testSimpleOutputScript() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleOutputScript.xhtml");
        
        UIComponent head = root.findComponent("head");
        assertNotNull(head);
        UIComponent body = root.findComponent("body");
        assertNotNull(body);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        //System.out.print(sw.toString());
    }
    
    public void testSimpleTargetHeadOutputScript() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleTargetHeadOutputScript.xhtml");
        
        UIComponent head = root.findComponent("head");
        assertNotNull(head);
        UIComponent body = root.findComponent("body");
        assertNotNull(body);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        //System.out.print(sw.toString());
    }
    
    public void testMultipleTargetHeadOutputScript() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testMultipleTargetHeadOutputScript.xhtml");
        
        UIComponent head = root.findComponent("head");
        assertNotNull(head);
        UIComponent body = root.findComponent("body");
        assertNotNull(body);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        //System.out.print(sw.toString());
    } 
}
