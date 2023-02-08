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

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.HelloWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompositeComponentConditionalButtonTestCase extends FaceletTestCase
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
    public void testConditionalButtonTargets() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testConditionalButtonTargets.xhtml");

        //The first component has a default command button
        UIComponent form = root.findComponent("testForm1");
        Assertions.assertNotNull(form);
        UINamingContainer compositeComponent1 = (UINamingContainer) form.findComponent("actionSource1");
        Assertions.assertNotNull(compositeComponent1);
        UICommand button1 = (UICommand) compositeComponent1.findComponent("button");
        Assertions.assertNotNull(button1);
        Assertions.assertEquals("submit", button1.getActionExpression().invoke(facesContext.getELContext(), null));
        
        Assertions.assertNotNull(button1.getActionListeners());
        Assertions.assertEquals(1, button1.getActionListeners().length);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) form.findComponent("actionSource2");
        Assertions.assertNotNull(compositeComponent2);
        UICommand button2 = (UICommand) compositeComponent2.findComponent("button");
        Assertions.assertNotNull(button2);
        //Since the button is outside cc:implementation, by the spec it cannot be taken into account as a valid "targets" value.
        Assertions.assertEquals("fail", button2.getActionExpression().invoke(facesContext.getELContext(), null));
        //It also cannot be target of cc:actionSource
        Assertions.assertNotNull(button2.getActionListeners());
        Assertions.assertEquals(0, button2.getActionListeners().length);

        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
}
