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
package org.apache.myfaces.mc.test.core;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import javax.servlet.http.Cookie;

import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;

/**
 * Client that keep track and "translate" the commands done in a JSF component.
 * It simulates the effect of a browser, but without execute any javascript
 * or check the html output. If that level of detail is required, use an
 * in-container alternative like Arquillian and others. This strategy is designed
 * for server-side testing. 
 * 
 * @author Leonardo Uribe
 *
 */
public class MockMyFacesClient
{
    private Map<String, String> parameters = new HashMap<String, String>();
    private Map<String, Cookie> cookies = new HashMap<String, Cookie>();
    private Map<String, Object> headers = new HashMap<String, Object>();
    
    private final FacesContext facesContext;
    
    // It has sense the client has a reference over the test, because 
    // after all this class encapsulate some automatic operations
    private AbstractMyFacesRequestTestCase testCase;
    
    public MockMyFacesClient(FacesContext facesContext, AbstractMyFacesRequestTestCase testCase)
    {
        this.facesContext = facesContext;
        this.testCase = testCase;
    }
    
    public void inputText(UIInput input, String text)
    {
        parameters.put(input.getClientId(), text);
    }
    
    public void submit(UIComponent component) throws Exception
    {
        testCase.processRemainingPhases();
        this.internalSubmit((UICommand)component);
        String viewId = facesContext.getViewRoot().getViewId();
        testCase.tearDownRequest();
        testCase.setupRequest(viewId);
    }
    
    protected void internalSubmit(UICommand command)
    {
        if (command instanceof HtmlCommandButton)
        {
            UIForm form = getParentForm(command);
            VisitContext visitContext = VisitContext.createVisitContext(facesContext);
            form.visitTree(visitContext, new VisitCallback(){

                public VisitResult visit(VisitContext context,
                        UIComponent target)
                {
                    if (target instanceof UIInput)
                    {
                        if (!parameters.containsKey(target.getClientId(facesContext)))
                        {
                            parameters.put(target.getClientId(facesContext), RendererUtils.getStringValue(facesContext, target));
                        }
                    }
                    return VisitResult.ACCEPT;
                }
                
            });
            parameters.put(form.getClientId(facesContext)+"_SUBMIT", "1");
            parameters.put(ResponseStateManager.VIEW_STATE_PARAM, facesContext.getApplication().getStateManager().getViewState(facesContext));
            Object value = command.getValue();
            parameters.put(command.getClientId(), value == null ? "" : value.toString());
            MockHttpServletResponse response = (MockHttpServletResponse) facesContext.getExternalContext().getResponse(); 
            Cookie cookie = response.getCookie("oam.Flash.RENDERMAP.TOKEN");
            getCookies().put("oam.Flash.RENDERMAP.TOKEN", cookie);
        }
    }
    
    public void ajax(UIComponent source, String event, String execute, String render, boolean submit) throws Exception
    {
        testCase.processRemainingPhases();
        this.internalAjax(source, event, execute, render, submit);
        String viewId = facesContext.getViewRoot().getViewId();
        testCase.tearDownRequest();
        testCase.setupRequest(viewId);
    }
    
    public void internalAjax(UIComponent source, String event, String execute, String render, boolean submit)
    {
        parameters.put("javax.faces.partial.ajax", "true");
        parameters.put("javax.faces.behavior.event", event);
        parameters.put("javax.faces.partial.event", "action".equals(event) ? "click" : event);
        parameters.put(ResponseStateManager.VIEW_STATE_PARAM, facesContext.getApplication().getStateManager().getViewState(facesContext));
        parameters.put("javax.faces.source", source.getClientId(facesContext));
        if (execute == null)
        {
            parameters.put("javax.faces.partial.execute", source.getClientId(facesContext));
        }
        else
        {
            parameters.put("javax.faces.partial.execute", execute);
        }
        if (render != null)
        {
            parameters.put("javax.faces.partial.render", render);
        }
        
        if (submit)
        {
            parameters.put(source.getClientId(facesContext)+"_SUBMIT", "1");
            parameters.put(source.getClientId(facesContext), source.getClientId(facesContext));
        }
        
        MockHttpServletResponse response = (MockHttpServletResponse) facesContext.getExternalContext().getResponse(); 
        Cookie cookie = response.getCookie("oam.Flash.RENDERMAP.TOKEN");
        getCookies().put("oam.Flash.RENDERMAP.TOKEN", cookie);
        
        headers.put("Faces-Request", "partial/ajax");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    }
    
    public Map<String, String> getParameters()
    {
        return parameters;
    }
    
    public Map<String, Object> getHeaders()
    {
        return headers;
    }
    
    public Map<String, Cookie> getCookies()
    {
        return cookies;
    }
    
    private UIForm getParentForm(UIComponent component)
    {
        UIComponent parent = component.getParent();
        while ( parent != null)
        {
            if (parent instanceof UIForm)
            {
                return (UIForm) parent;
            }
            parent = parent.getParent().getParent();
        }
        return null;
    }
    
    /**
     * Apply all params, headers and cookies into the request.
     * 
     * @param request
     */
    public void apply(MockHttpServletRequest request)
    {
        Map<String, String> inputFields = getParameters();
        for (Map.Entry<String, String> entry : inputFields.entrySet())
        {
            request.addParameter(entry.getKey(), entry.getValue());
        }
        Map<String, Object> headerFields = getHeaders();
        for (Map.Entry<String, Object> entry : headerFields.entrySet())
        {
            if (entry.getValue() instanceof String)
            {
                request.addHeader(entry.getKey(), (String) entry.getValue());
            }
            else if (entry.getValue() instanceof Integer)
            {
                request.addIntHeader(entry.getKey(), (Integer) entry.getValue());
            }
            else if (entry.getValue() instanceof java.util.Date)
            {
                request.addDateHeader(entry.getKey(), ((java.util.Date) entry.getValue()).getTime());
            }
        }
        Map<String, Cookie> cookies = getCookies();
        for (Map.Entry<String, Cookie> entry : cookies.entrySet())
        {
            request.addCookie(entry.getValue());
        }
    }

    public AbstractMyFacesRequestTestCase getTestCase()
    {
        return testCase;
    }

    public void setTestCase(AbstractMyFacesRequestTestCase testCase)
    {
        this.testCase = testCase;
    }
}
