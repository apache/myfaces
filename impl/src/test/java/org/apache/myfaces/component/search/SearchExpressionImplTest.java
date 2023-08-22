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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class SearchExpressionImplTest extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected boolean isScanAnnotations()
    {
        return true;
    }

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES", "org.apache.myfaces.component.search");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
    }

    @Test
    public void testSearchExpression1() throws Exception
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
        Assertions.assertTrue(found);
        
        String componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@this").get(0);
        Assertions.assertEquals("mainForm:showName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@this:@parent:showName").get(0);
        Assertions.assertEquals("mainForm:showName", componentId);

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@form:inputOutsideForm").get(0);
        Assertions.assertEquals("mainForm:inputOutsideForm", componentId);
        
        final SearchExpressionContext currentSearchContext = searchContext;
        Assertions.assertThrows(ComponentNotFoundException.class, () ->
        {
            facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(currentSearchContext, ":inputOutsideForm");
        });

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "mainForm:table:0:baseText").get(0);
        Assertions.assertEquals("mainForm:table:0:baseText", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@parent:showName:@parent:showName").get(0);
        Assertions.assertEquals("mainForm:showName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@form:showName").get(0);
        Assertions.assertEquals("mainForm:showName", componentId);

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, " @namingcontainer:showName ").get(0);
        Assertions.assertEquals("mainForm:showName", componentId);

        UIOutput name = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:name");
        searchContext = SearchExpressionContext.createSearchExpressionContext(facesContext, name);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@previous").get(0);
        Assertions.assertEquals("mainForm:labelName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@next").get(0);
        Assertions.assertEquals("mainForm:msgName", componentId);
        
        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "@parent:@id(msgName)").get(0);
        Assertions.assertEquals("mainForm:msgName", componentId);

        componentId = facesContext.getApplication().getSearchExpressionHandler().resolveClientIds(
                searchContext, "topLevelOutputText").get(0);
        Assertions.assertEquals("topLevelOutputText", componentId);
        
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
                Assertions.assertEquals("mainForm:showName", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "nested:nestedText").get(0);
                Assertions.assertEquals("mainForm:table:3:nested:1:nestedText", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:baseText").get(0);
                Assertions.assertEquals("mainForm:table:3:baseText", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:0:baseText").get(0);
                Assertions.assertEquals("mainForm:table:0:baseText", componentId);

                componentId = handler.resolveClientIds(searchContext, "nested:0:nestedText").get(0);
                Assertions.assertEquals("mainForm:table:3:nested:0:nestedText", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:nested").get(0);
                Assertions.assertEquals("mainForm:table:3:nested", componentId);
                
                componentId = handler.resolveClientIds(searchContext, "table:1:nested:0:nestedText").get(0);
                Assertions.assertEquals("mainForm:table:1:nested:0:nestedText", componentId);
                
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
                Assertions.assertEquals(handler.resolveClientId(searchContext, "@next"), "mainForm:table:3:nestedText");
            }
        });
       
        processRemainingPhases();
    }

    @Test
    public void testSearchExpressionIdKeyword() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, facesContext.getViewRoot());

        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        Assertions.assertEquals("panelGridId", handler.resolveClientId(searchContext, "mainForm:@parent"));

        processRemainingPhases();
    }
    
    @Test
    public void testResolveComponent() throws Exception
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
        
        Assertions.assertEquals(1, callback.getComponents().size());
        
        processRemainingPhases();
    }
    
    @Test
    public void testResolveComponents() throws Exception
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
        
        Assertions.assertEquals(2, callback.getComponents().size());
        
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
    public void testResolveClientId1() throws Exception
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
        Assertions.assertNull(clientId);
        
        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, out);

        try 
        {
            clientId = handler.resolveClientId(searchContext, "@none");
            Assertions.fail();
        }
        catch (ComponentNotFoundException e)
        {
            //No op
        }
    }
    
    @Test
    public void testResolveClientIds1() throws Exception
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
        Assertions.assertTrue(clientId.isEmpty());
        
        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, out);

        try 
        {
            clientId = handler.resolveClientIds(searchContext, "@none");
            Assertions.fail();
        }
        catch (ComponentNotFoundException e)
        {
            //No op
        }
    }

    @Test
    public void testIsValid() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, null);
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        Assertions.assertTrue(handler.isValidExpression(searchContext, "mainForm:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:nested:1:nestedText"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:baseText"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, " mainForm:table:0:baseText"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:nested:0:nestedText"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "mainForm:table:3:nested"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "mainForm:table:1:nested:0:nestedText"));
        
        Assertions.assertTrue(handler.isValidExpression(searchContext, " "));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@this"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@this:@parent:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@parent:showName:@parent:showName "));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@form:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@namingcontainer:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@previous"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@next"));
        Assertions.assertTrue(handler.isValidExpression(searchContext, "@parent:@id(msgName)"));
        
        Assertions.assertFalse(handler.isValidExpression(searchContext, "@whoNows"));
        Assertions.assertFalse(handler.isValidExpression(searchContext, "@parent:@whoNows"));
        Assertions.assertFalse(handler.isValidExpression(searchContext, "mainForm:@whoNows"));
        Assertions.assertFalse(handler.isValidExpression(searchContext, "!whoNows"));
        
        Assertions.assertFalse(handler.isValidExpression(searchContext, "@none:@parent"));
        Assertions.assertFalse(handler.isValidExpression(searchContext, "@all:@parent"));
    }
    
    @Test
    public void testIsPassthroughExpression() throws Exception
    {
        startViewRequest("/search1.xhtml");
        processLifecycleExecute();
        executeBeforeRender();
        executeBuildViewCycle();

        SearchExpressionContext searchContext = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, null);
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:nested:1:nestedText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:baseText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:0:baseText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:nested:0:nestedText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:3:nested"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:table:1:nested:0:nestedText"));
        
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@this"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@this:@parent:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@parent:showName:@parent:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@form"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@form:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@namingcontainer:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@previous"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@next"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@parent:@id(msgName)"));
        
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@whoNows"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "@parent:@whoNows"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "mainForm:@whoNows"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContext, "!whoNows"));
        
        Set<SearchExpressionHint> expressionHints = new HashSet<>();
        expressionHints.add(SearchExpressionHint.RESOLVE_CLIENT_SIDE);
        SearchExpressionContext searchContextWithAjaxResolve = 
                SearchExpressionContext.createSearchExpressionContext(facesContext, null, expressionHints, null);
        
        Assertions.assertTrue(handler.isPassthroughExpression(searchContextWithAjaxResolve, "@form"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContextWithAjaxResolve, "@form:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchContextWithAjaxResolve, "@form:@child(0)"));
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
