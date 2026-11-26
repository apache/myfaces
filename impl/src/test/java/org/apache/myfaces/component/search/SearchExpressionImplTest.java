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

package org.apache.myfaces.component.search;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.search.ComponentNotFoundException;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchExpressionHint;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.jupiter.api.Test;

/**
 *
 */
class SearchExpressionImplTest extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES", "org.apache.myfaces.component.search");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
    }

    @Test
    void searchExpression1() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        UIOutput out = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:showName");
        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, out);
        Collection<String> list = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "mainForm:showName");

        boolean found = false;
        for (String clientId : list)
        {
            //System.out.println(clientId);
            found = true;
        }
        assertTrue(found);
        
        String componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@this").get(0);
        assertEquals("mainForm:showName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@this:@parent:showName").get(0);
        assertEquals("mainForm:showName", componentId);

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@form:inputOutsideForm").get(0);
        assertEquals("mainForm:inputOutsideForm", componentId);
        
        final SearchExpressionContext currentSearchContext = searchContext;
        assertThrows(ComponentNotFoundException.class, () ->
            facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(currentSearchContext, ":inputOutsideForm"));

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "mainForm:table:0:baseText").get(0);
        assertEquals("mainForm:table:0:baseText", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@parent:showName:@parent:showName").get(0);
        assertEquals("mainForm:showName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@form:showName").get(0);
        assertEquals("mainForm:showName", componentId);

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, " @namingcontainer:showName ").get(0);
        assertEquals("mainForm:showName", componentId);

        UIOutput name = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:name");
        searchContext = SearchExpressionContext.createSearchExpressionContext(facesContext, name);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@previous").get(0);
        assertEquals("mainForm:labelName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@next").get(0);
        assertEquals("mainForm:msgName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@parent:@id(msgName)").get(0);
        assertEquals("mainForm:msgName", componentId);

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "topLevelOutputText").get(0);
        assertEquals("topLevelOutputText", componentId);
        
        facesContext.getViewRoot().invokeOnComponent(facesContext, "mainForm:table:3:nested:1:nestedButton", 
                new ContextCallback()
        {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent target)
            {
                SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
                SearchExpressionContext searchContext = 
                        SearchExpressionContext.createSearchExpressionContext(context, target);
                String componentId = handler.resolveClientIds(searchContext, "mainForm:showName").get(0);
                assertEquals("mainForm:showName", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "nested:nestedText").get(0);
                assertEquals("mainForm:table:3:nested:1:nestedText", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:baseText").get(0);
                assertEquals("mainForm:table:3:baseText", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:0:baseText").get(0);
                assertEquals("mainForm:table:0:baseText", componentId);

                componentId = handler.resolveClientIds(searchContext, "nested:0:nestedText").get(0);
                assertEquals("mainForm:table:3:nested:0:nestedText", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:nested").get(0);
                assertEquals("mainForm:table:3:nested", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:1:nested:0:nestedText").get(0);
                assertEquals("mainForm:table:1:nested:0:nestedText", componentId);
                
                //System.out.println(componentId);
            }
        });
        
        facesContext.getViewRoot().invokeOnComponent(facesContext, "mainForm:table:3:baseText", 
                new ContextCallback()
        {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent target)
            {
                SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
                SearchExpressionContext searchContext = 
                        SearchExpressionContext.createSearchExpressionContext(context, target);
                assertEquals("mainForm:table:3:nestedText", handler.resolveClientId(searchContext, "@next"));
            }
        });
       
        processRemainingPhases();
    }

    @Test
    void searchExpressionIdKeyword() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, facesContext.getViewRoot());

        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        assertEquals("panelGridId", handler.resolveClientId(searchContext, "mainForm:@parent"));

        processRemainingPhases();
    }

    @Test
    void resolveComponent() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, facesContext.getViewRoot());

        CollectComponentsCallback callback = new CollectComponentsCallback();
        facesContext.getApplication().getSearchExpressionHandler().resolveComponent(
                searchContext, "@id(name)", callback);
        
        assertEquals(1, callback.getComponents().size());
        
        processRemainingPhases();
    }

    @Test
    void resolveComponents() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, facesContext.getViewRoot());

        CollectComponentsCallback callback = new CollectComponentsCallback();
        facesContext.getApplication().getSearchExpressionHandler().resolveComponents(
                searchContext, " @id(name)   @none  ", callback);
        
        assertEquals(2, callback.getComponents().size());
        
        processRemainingPhases();
    }
    
    class CollectComponentsCallback implements ContextCallback
    {
        private ArrayList<UIComponent> components = new ArrayList<>();
        
        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            components.add(target);
        }
        
        public ArrayList<UIComponent> getComponents()
        {
            return components;
        }
    }

    @Test
    void resolveClientId1() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();
        
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        
        UIOutput out = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:showName");
        Set<SearchExpressionHint> expressionHints = new HashSet<>();

        expressionHints.add(SearchExpressionHint.IGNORE_NO_RESULT);
        SearchExpressionContext searchContextWithIgnoreNoResult = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, out, expressionHints, null);
        
        String clientId = handler.resolveClientId(searchContextWithIgnoreNoResult, "@none");
        assertNull(clientId);
        
        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, out);

        try 
        {
            clientId = handler.resolveClientId(searchContext, "@none");
            fail();
        }
        catch (ComponentNotFoundException e)
        {
            //No op
        }
    }

    @Test
    void resolveClientIds1() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();
        
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        
        UIOutput out = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:showName");
        Set<SearchExpressionHint> expressionHints = new HashSet<>();
        expressionHints.add(SearchExpressionHint.IGNORE_NO_RESULT);
        SearchExpressionContext searchContextWithIgnoreNoResult = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, out, expressionHints, null);
        
        List<String> clientId = handler.resolveClientIds(searchContextWithIgnoreNoResult, "@none");
        assertTrue(clientId.isEmpty());
        
        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, out);

        try 
        {
            clientId = handler.resolveClientIds(searchContext, "@none");
            fail();
        }
        catch (ComponentNotFoundException e)
        {
            //No op
        }
    }

    @Test
    void isValid() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, null);
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        assertTrue(handler.isValidExpression(searchContext, "mainForm:showName"));
        assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:nested:1:nestedText"));
        assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:baseText"));
        assertTrue(handler.isValidExpression(searchContext, " mainForm:table:0:baseText"));
        assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:nested:0:nestedText"));
        assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:nested"));
        assertTrue(handler.isValidExpression(searchContext, "mainForm:table:1:nested:0:nestedText"));
        
        assertTrue(handler.isValidExpression(searchContext, " "));
        assertTrue(handler.isValidExpression(searchContext, "@this"));
        assertTrue(handler.isValidExpression(searchContext, "@this:@parent:showName"));
        assertTrue(handler.isValidExpression(searchContext, "@parent:showName:@parent:showName "));
        assertTrue(handler.isValidExpression(searchContext, "@form:showName"));
        assertTrue(handler.isValidExpression(searchContext, "@namingcontainer:showName"));
        assertTrue(handler.isValidExpression(searchContext, "@previous"));
        assertTrue(handler.isValidExpression(searchContext, "@next"));
        assertTrue(handler.isValidExpression(searchContext, "@parent:@id(msgName)"));
        
        assertFalse(handler.isValidExpression(searchContext, "@whoNows"));
        assertFalse(handler.isValidExpression(searchContext, "@parent:@whoNows"));
        assertFalse(handler.isValidExpression(searchContext, "mainForm:@whoNows"));
        assertFalse(handler.isValidExpression(searchContext, "!whoNows"));
        
        assertFalse(handler.isValidExpression(searchContext, "@none:@parent"));
        assertFalse(handler.isValidExpression(searchContext, "@all:@parent"));
    }

    @Test
    void isPassthroughExpression() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, null);
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:showName"));
        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:nested:1:nestedText"));
        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:baseText"));
        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:0:baseText"));
        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:nested:0:nestedText"));
        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:nested"));
        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:1:nested:0:nestedText"));
        
        assertFalse(handler.isPassthroughExpression(searchContext, "@this"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@this:@parent:showName"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@parent:showName:@parent:showName"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@form"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@form:showName"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@namingcontainer:showName"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@previous"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@next"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@parent:@id(msgName)"));
        
        assertFalse(handler.isPassthroughExpression(searchContext, "@whoNows"));
        assertFalse(handler.isPassthroughExpression(searchContext, "@parent:@whoNows"));
        assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:@whoNows"));
        assertFalse(handler.isPassthroughExpression(searchContext, "!whoNows"));
        
        Set<SearchExpressionHint> expressionHints = new HashSet<>();
        expressionHints.add(SearchExpressionHint.RESOLVE_CLIENT_SIDE);
        SearchExpressionContext searchContextWithAjaxResolve = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, null, expressionHints, null);
        
        assertTrue(handler.isPassthroughExpression(searchContextWithAjaxResolve, "@form"));
        assertFalse(handler.isPassthroughExpression(searchContextWithAjaxResolve, "@form:showName"));
        assertFalse(handler.isPassthroughExpression(searchContextWithAjaxResolve, "@form:@child(0)"));
    }

    @Test
    void myFaces4695() throws Exception
    {
        startViewRequest("/search2.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, facesContext.getViewRoot().findComponent("form2:submit"));

        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        assertEquals("form1:one", handler.resolveClientId(searchContext, "form1:one"));

        processRemainingPhases();
    }

    /**
     * Regression test for MYFACES-4764.
     */
    @Test
    void myFaces4764() throws Exception
    {
        startViewRequest("/search_myfaces4764.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        UIComponent body = facesContext.getViewRoot().findComponent("body");
        assertNotNull(body, "body component must exist");

        SearchExpressionContext ctx1 = SearchExpressionContext.createSearchExpressionContext(facesContext, body);

        assertEquals("first", handler.resolveClientId(ctx1, "@child(1)"),
                "@child(1) must resolve to 'first'");

        processRemainingPhases();
    }

    /*
    @Test
    public void testCompositeComponentExpression() throws Exception
    {
        startViewRequest("/testCompositeActionSource.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();
        
        
    }*/
}
