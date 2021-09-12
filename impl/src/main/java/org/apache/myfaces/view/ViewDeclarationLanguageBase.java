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
package org.apache.myfaces.view;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;
import jakarta.faces.view.ViewDeclarationLanguage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.application.InvalidViewIdException;
import org.apache.myfaces.application.TreeStructureManager;
import org.apache.myfaces.context.RequestViewContext;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * @since 2.0
 */
public abstract class ViewDeclarationLanguageBase extends ViewDeclarationLanguage
{
    private static final Logger log = Logger.getLogger(ViewDeclarationLanguageBase.class.getName());

    private RenderKitFactory _renderKitFactory;

    /**
     * Process the specification required algorithm that is generic to all PDL.
     * 
     * @param context
     * @param viewId
     */
    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        Assert.notNull(context, "context");

        try
        {
            viewId = calculateViewId(context, viewId);
            
            Application application = context.getApplication();

            // Create a new UIViewRoot object instance using Application.createComponent(UIViewRoot.COMPONENT_TYPE).
            UIViewRoot newViewRoot = (UIViewRoot) application.createComponent(context,
                    UIViewRoot.COMPONENT_TYPE, null);
            UIViewRoot oldViewRoot = context.getViewRoot();
            if (oldViewRoot == null)
            {
                // If not, this method must call calculateLocale() and calculateRenderKitId(), and store the results
                // as the values of the locale and renderKitId, proeprties, respectively, of the newly created
                // UIViewRoot.
                ViewHandler handler = application.getViewHandler();
                newViewRoot.setLocale(handler.calculateLocale(context));
                newViewRoot.setRenderKitId(handler.calculateRenderKitId(context));
            }
            else
            {
                // If there is an existing UIViewRoot available on the FacesContext, this method must copy its locale
                // and renderKitId to this new view root
                newViewRoot.setLocale(oldViewRoot.getLocale());
                newViewRoot.setRenderKitId(oldViewRoot.getRenderKitId());
            }
            
            newViewRoot.setViewId(viewId);

            return newViewRoot;
        }
        catch (InvalidViewIdException e)
        {
            // If no viewId could be identified, or the viewId is exactly equal to the servlet mapping, 
            // send the response error code SC_NOT_FOUND with a suitable message to the client.
            sendSourceNotFound(context, e.getMessage());
            
            // TODO: VALIDATE - Spec is silent on the return value when an error was sent
            return null;
        }
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        Assert.notNull(context, "context");

        Application application = context.getApplication();
        
        ViewHandler applicationViewHandler = application.getViewHandler();
        
        String renderKitId = applicationViewHandler.calculateRenderKitId(context);

        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Entering restoreView - viewId: " + viewId + " ; renderKitId: " + renderKitId);
        }

        UIViewRoot viewRoot = null;
        
        StateManagementStrategy sms = getStateManagementStrategy(context, viewId);
        if (sms != null)
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Redirect to StateManagementStrategy: " + sms.getClass().getName());
            }
            
            viewRoot = sms.restoreView(context, viewId, renderKitId);
        }
        else
        {
            RenderKit renderKit = getRenderKitFactory().getRenderKit(context, renderKitId);
            ResponseStateManager responseStateManager = renderKit.getResponseStateManager();

            Object state = responseStateManager.getState(context, viewId);

            if (state != null)
            {
                Object[] stateArray = (Object[])state;
                viewRoot = TreeStructureManager.restoreTreeStructure(((Object[]) stateArray[0])[0]);

                if (viewRoot != null)
                {
                    context.setViewRoot(viewRoot);
                    viewRoot.processRestoreState(context, stateArray[1]);
                    
                    RequestViewContext.getCurrentInstance(context).refreshRequestViewContext(
                            context, viewRoot);
                    
                    // If state is saved fully, there outer f:view tag handler will not be executed,
                    // so "contracts" attribute will not be set properly. We need to save it and
                    // restore it from here. With PSS, the view will always be built so it is not
                    // necessary to save it on the state.
                    Object rlc = ((Object[]) stateArray[0])[1];
                    if (rlc != null)
                    {
                        context.setResourceLibraryContracts((List<String>) UIComponentBase.
                            restoreAttachedState(context, rlc));
                    }
                }
            }            
        }
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Exiting restoreView - " + viewId);
        }

        return viewRoot;
    }

    /**
     * Calculates the effective view identifier for the specified raw view identifier.
     * 
     * @param context le current FacesContext
     * @param viewId the raw view identifier
     * 
     * @return the effective view identifier
     */
    protected abstract String calculateViewId(FacesContext context, String viewId);
    
    /**
     * Send a source not found to the client. Although it can be considered ok in JSP mode,
     * I think it's pretty lame to have this kind of requirement at VDL level considering VDL 
     * represents the page --&gt; JSF tree link, not the transport layer required to send a 
     * SC_NOT_FOUND.
     * 
     * @param context le current FacesContext
     * @param message the message associated with the error
     */
    protected abstract void sendSourceNotFound(FacesContext context, String message);

    protected RenderKitFactory getRenderKitFactory()
    {
        if (_renderKitFactory == null)
        {
            _renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return _renderKitFactory;
    }
}

