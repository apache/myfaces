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

package org.apache.myfaces.view.facelets.tag.jstl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;

import org.apache.myfaces.renderkit.html.HtmlButtonRenderer;
import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.Employee;
import org.apache.myfaces.view.facelets.util.FastWriter;
import org.apache.shale.test.mock.MockResponseWriter;

public final class JstlCoreTestCase extends FaceletTestCase {

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
        application.addComponent(HtmlForm.COMPONENT_TYPE,
                HtmlForm.class.getName());
        application.addComponent(HtmlCommandButton.COMPONENT_TYPE,
                HtmlCommandButton.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY,
                "javax.faces.Form", new HtmlFormRenderer());
        renderKit.addRenderer(HtmlCommandButton.COMPONENT_FAMILY,
                "javax.faces.Button", new HtmlButtonRenderer());
    }
    
    public void testIf() throws Exception {
        Map session = facesContext.getExternalContext().getSessionMap();
        Employee e = new Employee();
        session.put("employee", e);

        UIViewRoot root = facesContext.getViewRoot();

        // make sure the form is there
        e.setManagement(true);
        vdl.buildView(facesContext, root,"if.xml");
        UIComponent c = root.findComponent("form");
        assertNotNull("form is null", c);
        
        // now make sure it isn't
        e.setManagement(false);
        
        facesContext.setViewRoot(facesContext.getApplication().getViewHandler()
                .createView(facesContext, "/test"));
        root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"if.xml");
        c = root.findComponent("form");
        assertNull("form is not null", c);
    }
    
    public void testForEach() throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        Collection c = new ArrayList();
        for (int i = 0; i < 10; i++) {
            c.add(new Character((char)('A' + i)));
        }
        session.put("list", c);
        Map m = new HashMap();
        for (int i = 0; i < 10; i++) {
            m.put("" + i, "" + i);
        }
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"forEach.xml");
        
        FastWriter fw = new FastWriter();
        MockResponseWriter mrw = new MockResponseWriter(fw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
        
        //System.out.println(root);
    }

}
