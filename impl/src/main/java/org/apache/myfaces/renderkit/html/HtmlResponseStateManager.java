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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;

import org.apache.myfaces.application.StateCache;
import org.apache.myfaces.application.StateCacheFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.renderkit.MyfacesResponseStateManager;
import org.apache.myfaces.renderkit.StateCacheFactoryImpl;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.shared.renderkit.html.HTML;
import org.apache.myfaces.shared.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared.util.StateUtils;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlResponseStateManager extends MyfacesResponseStateManager
{
    //private static final Log log = LogFactory.getLog(HtmlResponseStateManager.class);
    private static final Logger log = Logger.getLogger(HtmlResponseStateManager.class.getName());

    //private static final int TREE_PARAM = 2;
    private static final int STATE_PARAM = 0;
    private static final int VIEWID_PARAM = 1;

    public static final String STANDARD_STATE_SAVING_PARAM = "javax.faces.ViewState";
    
    @JSFWebConfigParam(since="2.0.6", expectedValues="true, false", defaultValue="true")
    public static final String INIT_PARAM_HANDLE_STATE_CACHING_MECHANICS = "org.apache.myfaces.HANDLE_STATE_CACHING_MECHANICS";
    
    private Boolean _handleStateCachingMechanics;
    
    private StateCacheFactory _stateCacheFactory;
    
    public HtmlResponseStateManager()
    {
        _stateCacheFactory = new StateCacheFactoryImpl();
    }
    
    protected boolean isHandlingStateCachingMechanics(FacesContext facesContext)
    {
        if (_handleStateCachingMechanics == null)
        {
            _handleStateCachingMechanics = WebConfigParamUtils.getBooleanInitParameter(facesContext.getExternalContext(), INIT_PARAM_HANDLE_STATE_CACHING_MECHANICS, true);
        }
        return _handleStateCachingMechanics.booleanValue();
    }
    
    public void writeState(FacesContext facesContext, Object state) throws IOException
    {
        ResponseWriter responseWriter = facesContext.getResponseWriter();

        Object token = null;
        Object[] savedState = new Object[2];
        
        if (isHandlingStateCachingMechanics(facesContext))
        {
            //token = getStateCache(facesContext).saveSerializedView(facesContext, state);
            token = getStateCache(facesContext).encodeSerializedState(facesContext, state);
        }
        else
        {
            token = state;
        }

        if (log.isLoggable(Level.FINEST))
            log.finest("Writing state in client");


        if (token != null)
        {
            savedState[STATE_PARAM] = token;
        }
        else
        {
            if (log.isLoggable(Level.FINEST))
                log.finest("No component states to be saved in client response!");
        }

        savedState[VIEWID_PARAM] = facesContext.getViewRoot().getViewId();
        
        if (log.isLoggable(Level.FINEST))
            log.finest("Writing view state and renderKit fields");

        // write the view state field
        writeViewStateField(facesContext, responseWriter, savedState);

        // renderKitId field
        writeRenderKitIdField(facesContext, responseWriter);
    }
    
    @Override
    public void saveState(FacesContext facesContext, Object state)
    {
        if (isHandlingStateCachingMechanics(facesContext))
        {
            getStateCache(facesContext).saveSerializedView(facesContext, state);
        }
        else
        {
            //This is done outside
        }
    }

    private void writeViewStateField(FacesContext facesContext, ResponseWriter responseWriter, Object savedState)
        throws IOException
    {
        String serializedState = StateUtils.construct(savedState, facesContext.getExternalContext());
        ExternalContext extContext = facesContext.getExternalContext();
        MyfacesConfig myfacesConfig = MyfacesConfig.getCurrentInstance(extContext);
        // Write Javascript viewstate if enabled and if javascript is allowed,
        // otherwise write hidden input
        if (JavascriptUtils.isJavascriptAllowed(extContext) && myfacesConfig.isViewStateJavascript())
        {
            HtmlRendererUtils.renderViewStateJavascript(facesContext, STANDARD_STATE_SAVING_PARAM, serializedState);
        }
        else
        {
            responseWriter.startElement(HTML.INPUT_ELEM, null);
            responseWriter.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
            responseWriter.writeAttribute(HTML.NAME_ATTR, STANDARD_STATE_SAVING_PARAM, null);
            if (myfacesConfig.isRenderViewStateId())
            {
                responseWriter.writeAttribute(HTML.ID_ATTR, STANDARD_STATE_SAVING_PARAM, null);
            }
            responseWriter.writeAttribute(HTML.VALUE_ATTR, serializedState, null);
            responseWriter.endElement(HTML.INPUT_ELEM);
        }
    }

    private void writeRenderKitIdField(FacesContext facesContext, ResponseWriter responseWriter) throws IOException
    {

        String defaultRenderKitId = facesContext.getApplication().getDefaultRenderKitId();
        if (defaultRenderKitId != null && !RenderKitFactory.HTML_BASIC_RENDER_KIT.equals(defaultRenderKitId))
        {
            responseWriter.startElement(HTML.INPUT_ELEM, null);
            responseWriter.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
            responseWriter.writeAttribute(HTML.NAME_ATTR, ResponseStateManager.RENDER_KIT_ID_PARAM, null);
            responseWriter.writeAttribute(HTML.VALUE_ATTR, defaultRenderKitId, null);
            responseWriter.endElement(HTML.INPUT_ELEM);
        }
    }

    @Override
    public Object getState(FacesContext facesContext, String viewId)
    {
        Object[] savedState = getSavedState(facesContext);
        if (savedState == null)
        {
            return null;
        }

        if (isHandlingStateCachingMechanics(facesContext))
        {
            return getStateCache(facesContext).restoreSerializedView(facesContext, viewId, savedState[STATE_PARAM]);
        }
        else
        {
            return savedState[STATE_PARAM];
        }
    }

    /* There methods are no longer required
    @Override
    public Object getTreeStructureToRestore(FacesContext facesContext, String viewId)
    {
        // Although this method won't be called anymore,
        // it has been kept for backward compatibility.
        Object[] savedState = getSavedState(facesContext);
        if (savedState == null)
        {
            return null;
        }

        return savedState[TREE_PARAM];
    }

    @Override
    public Object getComponentStateToRestore(FacesContext facesContext)
    {
        // Although this method won't be called anymore,
        // it has been kept for backward compatibility.
        Object[] savedState = getSavedState(facesContext);
        if (savedState == null)
        {
            return null;
        }

        return savedState[STATE_PARAM];
    }*/

    /**
     * Reconstructs the state from the "javax.faces.ViewState" request parameter.
     * 
     * @param facesContext
     *            the current FacesContext
     * 
     * @return the reconstructed state, or <code>null</code> if there was no saved state
     */
    private Object[] getSavedState(FacesContext facesContext) {
        Object encodedState = 
            facesContext.getExternalContext().
                getRequestParameterMap().get(STANDARD_STATE_SAVING_PARAM);
        if(encodedState==null || (((String) encodedState).length() == 0)) { 
            return null;
        }

        Object[] savedState = (Object[])StateUtils.reconstruct((String)encodedState, facesContext.getExternalContext());


        if (savedState == null)
        {
            if (log.isLoggable(Level.FINEST)) {
                log.finest("No saved state");
            }
            return null;
        }
        
        String restoredViewId = (String)savedState[VIEWID_PARAM];

        if (restoredViewId == null)
        {
            // no saved state or state of different viewId
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("No saved state or state of a different viewId: " + restoredViewId);
            }

            return null;
        }

        return savedState;
    }

    /**
     * Checks if the current request is a postback
     * 
     * @since 1.2
     */
    @Override
    public boolean isPostback(FacesContext context)
    {
        return context.getExternalContext().getRequestParameterMap().containsKey(ResponseStateManager.VIEW_STATE_PARAM);
    }

    @Override
    public String getViewState(FacesContext facesContext, Object baseState)
    {
        if (baseState == null)
        {
            return null;
        }
        
        Object state = null;
        if (isHandlingStateCachingMechanics(facesContext))
        {
            state = getStateCache(facesContext).saveSerializedView(facesContext, baseState);
        }
        else
        {
            state = baseState;
        }
        
        Object[] savedState = new Object[2];
        
        if (state != null)
        {
            savedState[STATE_PARAM] = state;
        }
        
        savedState[VIEWID_PARAM] = facesContext.getViewRoot().getViewId();
        
        return StateUtils.construct(savedState, facesContext.getExternalContext());
    }
    
    @Override
    public boolean isWriteStateAfterRenderViewRequired(FacesContext facesContext)
    {
        return getStateCache(facesContext).isWriteStateAfterRenderViewRequired(facesContext);
    }

    protected StateCache getStateCache(FacesContext facesContext)
    {
        return _stateCacheFactory.getStateCache(facesContext);
    }

}
