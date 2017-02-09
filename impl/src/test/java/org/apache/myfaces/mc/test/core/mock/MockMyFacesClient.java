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
package org.apache.myfaces.mc.test.core.mock;

import java.util.HashMap;
import java.util.Map;
import javax.faces.FacesException;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.render.ResponseStateManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;

import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;
import org.junit.Assert;

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
    
    // It has sense the client has a reference over the test, because 
    // after all this class encapsulate some automatic operations
    private ServletMockContainer testCase;
    
    public MockMyFacesClient(FacesContext facesContext, ServletMockContainer testCase)
    {
        this.testCase = testCase;
    }
    
    public void processRedirect()
    {
        HttpServletResponse response = testCase.getResponse();
        HttpServletRequest request = testCase.getRequest();
        if (response.getStatus() == HttpServletResponse.SC_FOUND)
        {
            testCase.processRemainingPhases(); //testCase.processRemainingPhases();
            testCase.endRequest();
            String location = response.getHeader("Location");
            String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
            String servletPath = request.getServletPath() == null ? "" : request.getServletPath();
            int cpi = location.indexOf(contextPath);
            int spi = cpi < 0 ? -1 : location.indexOf(servletPath, cpi+contextPath.length());
            String viewId;
            if (spi >= 0)
            {
                viewId = location.substring(spi+servletPath.length());
            }
            else
            {
                viewId = location;
            }
            testCase.startViewRequest(viewId);
        }
        else
        {
            Assert.fail("Expected redirect not found");
        }
    }
    
    public void inputText(UIInput input, String text)
    {
        parameters.put(input.getClientId(), text);
    }
    
    public void inputText(String clientId, String text)
    {
        UIComponent input = getTestCase().getFacesContext().
            getViewRoot().findComponent(clientId);
        if (input == null)
        {
            throw new FacesException("input with clientId:"+clientId+" not found");
        }
        else
        {
            parameters.put(input.getClientId(), text);
        }
    }
    
    /**
     * Simulate a submit, processing the remaining phases and setting up the new request.
     * It delegates to client.submit, where the necessary data is gathered to be applied
     * later on client.apply method.
     * 
     * @param component
     * @throws Exception
     */
    public void submit(UIComponent component)
    {
        testCase.processRemainingPhases();
        this.internalSubmit((UICommand)component);
        String viewId = testCase.getFacesContext().getViewRoot().getViewId();
        testCase.endRequest();
        testCase.startViewRequest(viewId);
    }
    
    public void submit(String clientId)
    {
        UIComponent button = getTestCase().getFacesContext().
            getViewRoot().findComponent(clientId);
        if (button == null)
        {
            throw new FacesException("button with clientId:"+clientId+" not found");
        }
        else
        {
            submit(button);
        }
    }
    
    protected void internalSubmit(UICommand command)
    {
        if (command instanceof HtmlCommandButton)
        {
            final FacesContext facesContext = testCase.getFacesContext();
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
                            parameters.put(target.getClientId(facesContext), 
                                RendererUtils.getStringValue(facesContext, target));
                        }
                    }
                    return VisitResult.ACCEPT;
                }
                
            });
            parameters.put(form.getClientId(facesContext)+"_SUBMIT", "1");
            Object value = command.getValue();
            parameters.put(command.getClientId(), value == null ? "" : value.toString());
            
            applyStateFromPreviousRequest();
            /*
            parameters.put(ResponseStateManager.VIEW_STATE_PARAM, 
                facesContext.getApplication().getStateManager().getViewState(facesContext));
            if (facesContext.getExternalContext().getClientWindow() != null)
            {
                parameters.put(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, 
                facesContext.getExternalContext().getClientWindow().getId());
            }
            MockHttpServletResponse response = (MockHttpServletResponse) 
                facesContext.getExternalContext().getResponse(); 
            Cookie cookie = response.getCookie("oam.Flash.RENDERMAP.TOKEN");
            getCookies().put("oam.Flash.RENDERMAP.TOKEN", cookie);*/
        }
    }
    
    public void ajax(UIComponent source, String event, String execute, String render, boolean submit) throws Exception
    {
        ajax(source, event, execute, render, submit, false);
    }
    
    public void ajax(String sourceClientId, String event, String execute, String render, boolean submit) throws Exception
    {
        ajax(sourceClientId, event, execute, render, submit, false);
    }    
            
    public void ajax(UIComponent source, String event, String execute, String render, 
        boolean submit, boolean resetValues)
    {
        testCase.processRemainingPhases();
        this.internalAjax(source.getClientId(testCase.getFacesContext()), event, execute, render, submit, resetValues);
        String viewId = testCase.getFacesContext().getViewRoot().getViewId();
        testCase.endRequest();
        testCase.startViewRequest(viewId);
    }
    
    public void ajax(String sourceClientId, String event, String execute, String render, 
        boolean submit, boolean resetValues)
    {
        testCase.processRemainingPhases();
        this.internalAjax(sourceClientId, event, execute, render, submit, resetValues);
        String viewId = testCase.getFacesContext().getViewRoot().getViewId();
        testCase.endRequest();
        testCase.startViewRequest(viewId);
    }
    
    protected void internalAjax(String source, String event, String execute, String render, 
        boolean submit, boolean resetValues)
    {
        parameters.put("javax.faces.partial.ajax", "true");
        parameters.put(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME, event);
        parameters.put(PartialViewContext.PARTIAL_EVENT_PARAM_NAME, "action".equals(event) ? "click" : event);
        applyStateFromPreviousRequest();
        //parameters.put(ResponseStateManager.VIEW_STATE_PARAM, 
        //    facesContext.getApplication().getStateManager().getViewState(facesContext));
        parameters.put(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME, source);
        if (execute == null)
        {
            parameters.put("javax.faces.partial.execute", source);
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
            parameters.put(source+"_SUBMIT", "1");
            parameters.put(source, source);
        }
        
        if (resetValues)
        {
            parameters.put("javax.faces.partial.resetValues", "true");
        }
        
        headers.put("Faces-Request", "partial/ajax");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    }
    
    protected void applyStateFromPreviousRequest()
    {
        FacesContext facesContext = testCase.getFacesContext();
        
        parameters.put(ResponseStateManager.VIEW_STATE_PARAM, 
            facesContext.getApplication().getStateManager().getViewState(facesContext));
        if (facesContext.getExternalContext().getClientWindow() != null)
        {
            parameters.put(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, 
                facesContext.getExternalContext().getClientWindow().getId());
        }
        
        applyCookiesFromPreviousRequest();
    }
    
    protected void applyCookiesFromPreviousRequest()
    {
        MockHttpServletResponse response = (MockHttpServletResponse) testCase.getResponse();
        if (response.getCookies() != null && !response.getCookies().isEmpty())
        {
            for (Map.Entry<String, Cookie> entry : response.getCookies().entrySet())
            {
                getCookies().put(entry.getKey(), entry.getValue());
            }
        }
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
            parent = parent.getParent();
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
    
    public void reset(FacesContext facesContext)
    {
        this.parameters.clear();
        this.headers.clear();
        this.cookies.clear();
    }

    public ServletMockContainer getTestCase()
    {
        return testCase;
    }

    public void setTestCase(AbstractMyFacesRequestTestCase testCase)
    {
        this.testCase = testCase;
    }
}
