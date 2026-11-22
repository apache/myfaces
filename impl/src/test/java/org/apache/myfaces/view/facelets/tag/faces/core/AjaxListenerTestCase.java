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

package org.apache.myfaces.view.facelets.tag.faces.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodNotFoundException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.event.AjaxBehaviorEvent;

import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * f:ajax listener must accept a method taking the AjaxBehaviorEvent or a method taking no arguments.
 * The method taking the event is preferred when both exist.
 */
class AjaxListenerTestCase extends AbstractFaceletTestCase
{
    private AjaxListenerBean bean;

    @BeforeEach
    void setUpBean()
    {
        bean = new AjaxListenerBean();
        facesContext.getExternalContext().getRequestMap().put("ajaxListenerBean", bean);
    }

    @Test
    void oneArgMethod() throws Exception
    {
        assertEquals(List.of("oneArg(event)"), broadcast("oneArg"));
    }

    @Test
    void zeroArgMethod() throws Exception
    {
        assertEquals(List.of("zeroArg()"), broadcast("zeroArg"));
    }

    @Test
    void bothMethodsPrefersEvent() throws Exception
    {
        assertEquals(List.of("both(event)"), broadcast("both"));
    }

    @Test
    void missingMethod() throws Exception
    {
        assertThrows(MethodNotFoundException.class, () -> broadcast("missing"));
    }

    @Test
    void stateSaving() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajaxListener.xhtml");

        ELContext elContext = facesContext.getELContext();
        ExpressionFactory ef = facesContext.getApplication().getExpressionFactory();
        String el = "#{ajaxListenerBean.zeroArg}";
        AjaxBehaviorListenerImpl listener = new AjaxBehaviorListenerImpl(
                ef.createMethodExpression(elContext, el, Void.TYPE, new Class<?>[] { AjaxBehaviorEvent.class }),
                ef.createMethodExpression(elContext, el, Void.TYPE, new Class<?>[0]));

        Object state = listener.saveState(facesContext);
        AjaxBehaviorListenerImpl restored = new AjaxBehaviorListenerImpl();
        restored.restoreState(facesContext, state);

        restored.processAjaxBehavior(new AjaxBehaviorEvent(facesContext,
                root.findComponent("form:zeroArg"), getAjaxBehavior(root, "zeroArg")));

        assertEquals(List.of("zeroArg()"), bean.getInvocations());
    }

    private List<String> broadcast(String id) throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajaxListener.xhtml");

        UIComponent input = root.findComponent("form:" + id);
        AjaxBehavior behavior = getAjaxBehavior(root, id);
        behavior.broadcast(new AjaxBehaviorEvent(facesContext, input, behavior));

        return bean.getInvocations();
    }

    private AjaxBehavior getAjaxBehavior(UIViewRoot root, String id)
    {
        UIComponent input = root.findComponent("form:" + id);
        assertNotNull(input);

        Map<String, List<ClientBehavior>> behaviors = ((ClientBehaviorHolder) input).getClientBehaviors();
        List<ClientBehavior> change = behaviors.get("change");
        assertNotNull(change);
        assertEquals(1, change.size());
        return (AjaxBehavior) change.get(0);
    }
}
