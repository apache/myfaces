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
package org.apache.myfaces.application.jsp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.faces.FactoryFinder;
import javax.faces.application.StateManager;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.myfaces.application.MyfacesStateManager;
import org.apache.myfaces.application.TreeStructureManager;
import org.apache.myfaces.renderkit.MyfacesResponseStateManager;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.util.MyFacesObjectInputStream;

/**
 * Default StateManager implementation for use when views are defined
 * via tags in JSP pages.
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Manfred Geiler
 * @version $Revision$ $Date$
 */
public class JspStateManagerImpl extends MyfacesStateManager
{
    //private static final Log log = LogFactory.getLog(JspStateManagerImpl.class);
    private static final Logger log = Logger.getLogger(JspStateManagerImpl.class.getName());
    
    private static final String SERIALIZED_VIEW_SESSION_ATTR= 
        JspStateManagerImpl.class.getName() + ".SERIALIZED_VIEW";
    
    private static final String SERIALIZED_VIEW_REQUEST_ATTR = 
        JspStateManagerImpl.class.getName() + ".SERIALIZED_VIEW";
    
    private static final String RESTORED_SERIALIZED_VIEW_REQUEST_ATTR = 
        JspStateManagerImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW";

    /**
     * Only applicable if state saving method is "server" (= default).
     * Defines the amount (default = 20) of the latest views are stored in session.
     */
    private static final String NUMBER_OF_VIEWS_IN_SESSION_PARAM = "org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION";

    /**
     * Default value for <code>org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION</code> context parameter.
     */
    private static final int DEFAULT_NUMBER_OF_VIEWS_IN_SESSION = 20;

    /**
     * Only applicable if state saving method is "server" (= default).
     * If <code>true</code> (default) the state will be serialized to a byte stream before it is written to the session.
     * If <code>false</code> the state will not be serialized to a byte stream.
     */
    private static final String SERIALIZE_STATE_IN_SESSION_PARAM = "org.apache.myfaces.SERIALIZE_STATE_IN_SESSION";

    /**
     * Only applicable if state saving method is "server" (= default) and if <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> is <code>true</code> (= default).
     * If <code>true</code> (default) the serialized state will be compressed before it is written to the session.
     * If <code>false</code> the state will not be compressed.
     */
    private static final String COMPRESS_SERVER_STATE_PARAM = "org.apache.myfaces.COMPRESS_STATE_IN_SESSION";

    /**
     * Default value for <code>org.apache.myfaces.COMPRESS_STATE_IN_SESSION</code> context parameter.
     */
    private static final boolean DEFAULT_COMPRESS_SERVER_STATE_PARAM = true;

    /**
     * Default value for <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> context parameter.
     */
    private static final boolean DEFAULT_SERIALIZE_STATE_IN_SESSION = true;

    /**
     * Define the way of handle old view references(views removed from session), making possible to
     * store it in a cache, so the state manager first try to get the view from the session. If is it
     * not found and soft or weak ReferenceMap is used, it try to get from it.
     * <p>
     * Only applicable if state saving method is "server" (= default).
     * </p>
     * <p>
     * The gc is responsible for remove the views, according to the rules used for soft, weak or phantom
     * references. If a key in soft and weak mode is garbage collected, its values are purged.
     * </p>
     * <p>
     * By default no cache is used, so views removed from session became phantom references.
     * </p>
     * <ul> 
     * <li> off, no: default, no cache is used</li> 
     * <li> hard-soft: use an ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.SOFT)</li>
     * <li> soft: use an ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true) </li>
     * <li> soft-weak: use an ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.WEAK, true) </li>
     * <li> weak: use an ReferenceMap(AbstractReferenceMap.WEAK, AbstractReferenceMap.WEAK, true) </li>
     * </ul>
     * 
     */
    private static final String CACHE_OLD_VIEWS_IN_SESSION_MODE = "org.apache.myfaces.CACHE_OLD_VIEWS_IN_SESSION_MODE";
    
    /**
     * This option uses an hard-soft ReferenceMap, but it could cause a 
     * memory leak, because the keys are not removed by any method
     * (MYFACES-1660). So use with caution.
     */
    private static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT = "hard-soft";
    
    private static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT = "soft";
    
    private static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK = "soft-weak";
    
    private static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK = "weak";
    
    private static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF = "off";

    private static final int UNCOMPRESSED_FLAG = 0;
    private static final int COMPRESSED_FLAG = 1;

    private static final int JSF_SEQUENCE_INDEX = 0;

    private RenderKitFactory _renderKitFactory = null;

    public JspStateManagerImpl()
    {
        if (log.isLoggable(Level.FINEST)) log.finest("New JspStateManagerImpl instance created");
    }

    @Override
    protected Object getComponentStateToSave(FacesContext facesContext)
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering getComponentStateToSave");

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }

        Object serializedComponentStates = viewRoot.processSaveState(facesContext);
        //Locale is a state attribute of UIViewRoot and need not be saved explicitly
        if (log.isLoggable(Level.FINEST)) log.finest("Exiting getComponentStateToSave");
        return serializedComponentStates;
    }

    /**
     * Return an object which contains info about the UIComponent type
     * of each node in the view tree. This allows an identical UIComponent
     * tree to be recreated later, though all the components will have
     * just default values for their members.
     */
    @Override
    protected Object getTreeStructureToSave(FacesContext facesContext)
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering getTreeStructureToSave");
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }
        TreeStructureManager tsm = new TreeStructureManager();
        Object retVal = tsm.buildTreeStructureToSave(viewRoot);
        if (log.isLoggable(Level.FINEST)) log.finest("Exiting getTreeStructureToSave");
        return retVal;
    }

    /**
     * Given a tree of UIComponent objects created the default constructor
     * for each node, retrieve saved state info (from either the client or
     * the server) and walk the tree restoring the members of each node
     * from the saved state information.
     */
    @Override
    protected void restoreComponentState(FacesContext facesContext,
                                         UIViewRoot uiViewRoot,
                                         String renderKitId)
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering restoreComponentState");

        //===========================================
        // first, locate the saved state information
        //===========================================

        RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
        ResponseStateManager responseStateManager = renderKit.getResponseStateManager();

        Object serializedComponentStates;
        if (isSavingStateInClient(facesContext))
        {
            if (isLegacyResponseStateManager(responseStateManager))
            {
                serializedComponentStates = responseStateManager.getComponentStateToRestore(facesContext);
            }
            else
            {
                serializedComponentStates = responseStateManager.getState(facesContext, uiViewRoot.getViewId());
            }
            if (serializedComponentStates == null)
            {
                log.severe("No serialized component state found in client request!");
                // mark UIViewRoot invalid by resetting view id
                uiViewRoot.setViewId(null);
                return;
            }
        }
        else
        {
            Integer serverStateId = getServerStateId((Object[]) responseStateManager.getState(facesContext, uiViewRoot.getViewId()));

            Object[] stateObj = (Object[])( (serverStateId == null)? null : getSerializedViewFromServletSession(facesContext, uiViewRoot.getViewId(), serverStateId) );
            if (stateObj == null)
            {
                 log.severe("No serialized view found in server session!");
                // mark UIViewRoot invalid by resetting view id
                uiViewRoot.setViewId(null);
                return;
            }
            SerializedView serializedView = new SerializedView(stateObj[0], stateObj[1]);
            serializedComponentStates = serializedView.getState();
            if (serializedComponentStates == null)
            {
                log.severe("No serialized component state found in server session!");
                return;
            }
        }

        if (uiViewRoot.getRenderKitId() == null)
        {
            //Just to be sure...
            uiViewRoot.setRenderKitId(renderKitId);
        }

        // now ask the view root component to restore its state
        uiViewRoot.processRestoreState(facesContext, serializedComponentStates);

        if (log.isLoggable(Level.FINEST)) log.finest("Exiting restoreComponentState");
    }

      protected Integer getServerStateId(Object[] state)
      {
        if (state != null)
        {
            Object serverStateId = state[JSF_SEQUENCE_INDEX];
            if (serverStateId != null)
            {
                return Integer.valueOf((String) serverStateId, Character.MAX_RADIX);
            }
        }
        return null;
    }

    /**
     * See getTreeStructureToSave.
     */
    @Override
    protected UIViewRoot restoreTreeStructure(FacesContext facesContext,
                                              String viewId,
                                              String renderKitId)
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering restoreTreeStructure");

        RenderKit rk = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
        ResponseStateManager responseStateManager = rk.getResponseStateManager();

        UIViewRoot uiViewRoot;
        if (isSavingStateInClient(facesContext))
        {
            //reconstruct tree structure from request
            Object treeStructure = responseStateManager.getTreeStructureToRestore(facesContext, viewId);
            if (treeStructure == null)
            {
                if (log.isLoggable(Level.FINE)) log.fine("Exiting restoreTreeStructure - No tree structure state found in client request");
                return null;
            }

            TreeStructureManager tsm = new TreeStructureManager();
            uiViewRoot = tsm.restoreTreeStructure(treeStructure);
            if (log.isLoggable(Level.FINEST)) log.finest("Tree structure restored from client request");
        }
        else
        {
            //reconstruct tree structure from ServletSession
            Integer serverStateId = getServerStateId((Object[]) responseStateManager.getState(facesContext, viewId));

            Object[] stateObj = (Object[])( (serverStateId == null)? null : getSerializedViewFromServletSession(facesContext, viewId, serverStateId) );
            if (stateObj == null)
            {
                if (log.isLoggable(Level.FINE)) log.fine("Exiting restoreTreeStructure - No serialized view found in server session!");
                return null;
            }

            SerializedView serializedView = new SerializedView(stateObj[0], stateObj[1]);
            Object treeStructure = serializedView.getStructure();
            if (treeStructure == null)
            {
                if (log.isLoggable(Level.FINE)) log.fine("Exiting restoreTreeStructure - No tree structure state found in server session, former UIViewRoot must have been transient");
                return null;
            }

            TreeStructureManager tsm = new TreeStructureManager();
            uiViewRoot = tsm.restoreTreeStructure(serializedView.getStructure());
            if (log.isLoggable(Level.FINEST)) log.finest("Tree structure restored from server session");
        }

        if (log.isLoggable(Level.FINEST)) log.finest("Exiting restoreTreeStructure");
        return uiViewRoot;
    }

    @Override
    public UIViewRoot restoreView(FacesContext facesContext, String viewId, String renderKitId)
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering restoreView - viewId: "+viewId+" ; renderKitId: "+renderKitId);

        UIViewRoot uiViewRoot = null;
        
        ViewDeclarationLanguage vdl = facesContext.getApplication().
            getViewHandler().getViewDeclarationLanguage(facesContext,viewId);
        StateManagementStrategy sms = null; 
        if (vdl != null)
        {
            sms = vdl.getStateManagementStrategy(facesContext, viewId);
        }
        
        if (sms != null)
        {
            if (log.isLoggable(Level.FINEST)) log.finest("Redirect to StateManagementStrategy: "+sms.getClass().getName());
            
            uiViewRoot = sms.restoreView(facesContext, viewId, renderKitId);
        }
        else
        {
            RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
            ResponseStateManager responseStateManager = renderKit.getResponseStateManager();

            Object state;
            if (isSavingStateInClient(facesContext))
            {
                if (log.isLoggable(Level.FINEST)) log.finest("Restoring view from client");

                state = responseStateManager.getState(facesContext, viewId);
            }
            else
            {
                if (log.isLoggable(Level.FINEST)) log.finest("Restoring view from session");

                Integer serverStateId = getServerStateId((Object[]) responseStateManager.getState(facesContext, viewId));

                state = (serverStateId == null) ? null : getSerializedViewFromServletSession(facesContext, viewId, serverStateId);
            }

            if (state != null) {
                Object[] stateArray = (Object[])state;
                TreeStructureManager tsm = new TreeStructureManager();
                uiViewRoot = tsm.restoreTreeStructure(stateArray[0]);

                if (uiViewRoot != null) {
                    facesContext.setViewRoot (uiViewRoot);
                    uiViewRoot.processRestoreState(facesContext, stateArray[1]);
                }
            }            
        }
        if (log.isLoggable(Level.FINEST)) log.finest("Exiting restoreView - "+viewId);

        return uiViewRoot;
    }

    /**
     * Wrap the original method and redirect to VDL StateManagementStrategy when
     * necessary
     */
    @Override
    public Object saveView(FacesContext facesContext)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();
        
        String viewId = uiViewRoot.getViewId();
        ViewDeclarationLanguage vdl = facesContext.getApplication().
            getViewHandler().getViewDeclarationLanguage(facesContext,viewId);
        try
        {
            facesContext.getAttributes().put(StateManager.IS_SAVING_STATE, Boolean.TRUE);
            if (vdl != null)
            {
                StateManagementStrategy sms = vdl.getStateManagementStrategy(facesContext, viewId);
                
                if (sms != null)
                {
                    if (log.isLoggable(Level.FINEST)) log.finest("Calling saveView of StateManagementStrategy: "+sms.getClass().getName());
                    
                    return sms.saveView(facesContext);
                }
            }
    
            // In StateManagementStrategy.saveView there is a check for transient at
            // start, but the same applies for VDL without StateManagementStrategy,
            // so this should be checked before call parent (note that parent method
            // does not do this check).
            if (uiViewRoot.isTransient())
            {
                return null;
            }
    
            return super.saveView(facesContext);
        }
        finally
        {
            facesContext.getAttributes().remove(StateManager.IS_SAVING_STATE);
        }
    }
    
    @Override
    public SerializedView saveSerializedView(FacesContext facesContext) throws IllegalStateException
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering saveSerializedView");

        checkForDuplicateIds(facesContext, facesContext.getViewRoot(), new HashSet<String>());

        if (log.isLoggable(Level.FINEST)) log.finest("Processing saveSerializedView - Checked for duplicate Ids");

        ExternalContext externalContext = facesContext.getExternalContext();

        // SerializedView already created before within this request?
        Object serializedView = externalContext.getRequestMap()
                                                            .get(SERIALIZED_VIEW_REQUEST_ATTR);
        if (serializedView == null)
        {
            if (log.isLoggable(Level.FINEST)) log.finest("Processing saveSerializedView - create new serialized view");

            // first call to saveSerializedView --> create SerializedView
            Object treeStruct = getTreeStructureToSave(facesContext);
            Object compStates = getComponentStateToSave(facesContext);
            serializedView = new Object[] {treeStruct, compStates};
            externalContext.getRequestMap().put(SERIALIZED_VIEW_REQUEST_ATTR,
                                                serializedView);

            if (log.isLoggable(Level.FINEST)) log.finest("Processing saveSerializedView - new serialized view created");
        }

        Object[] serializedViewArray = (Object[]) serializedView;

        if (!isSavingStateInClient(facesContext))
        {
            if (log.isLoggable(Level.FINEST)) log.finest("Processing saveSerializedView - server-side state saving - save state");
            //save state in server session
            saveSerializedViewInServletSession(facesContext, serializedView);

            if (log.isLoggable(Level.FINEST)) log.finest("Exiting saveSerializedView - server-side state saving - saved state");
            return new SerializedView(serializedViewArray[0], new Object[0]);
        }

        if (log.isLoggable(Level.FINEST)) log.finest("Exiting saveSerializedView - client-side state saving");

        return new SerializedView(serializedViewArray[0], serializedViewArray[1]);
    }

    private static void checkForDuplicateIds(FacesContext context,
                                             UIComponent component,
                                             Set<String> ids)
    {
        String id = component.getId();
        if (id != null && !ids.add(id))
        {
            throw new IllegalStateException("Client-id : " + id +
                                            " is duplicated in the faces tree. Component : " + 
                                            component.getClientId(context)+", path: " +
                                            getPathToComponent(component));
        }
        
        if (component instanceof NamingContainer)
        {
            ids = new HashSet<String>();
        }
        
        Iterator<UIComponent> it = component.getFacetsAndChildren();
        while (it.hasNext())
        {
            UIComponent kid = it.next();
            checkForDuplicateIds(context, kid, ids);
        }
    }

    private static String getPathToComponent(UIComponent component)
    {
        StringBuffer buf = new StringBuffer();

        if(component == null)
        {
            buf.append("{Component-Path : ");
            buf.append("[null]}");
            return buf.toString();
        }

        getPathToComponent(component,buf);

        buf.insert(0,"{Component-Path : ");
        buf.append("}");

        return buf.toString();
    }

    private static void getPathToComponent(UIComponent component, StringBuffer buf)
    {
        if(component == null)
            return;

        StringBuffer intBuf = new StringBuffer();

        intBuf.append("[Class: ");
        intBuf.append(component.getClass().getName());
        if(component instanceof UIViewRoot)
        {
            intBuf.append(",ViewId: ");
            intBuf.append(((UIViewRoot) component).getViewId());
        }
        else
        {
            intBuf.append(",Id: ");
            intBuf.append(component.getId());
        }
        intBuf.append("]");

        buf.insert(0,intBuf.toString());

        getPathToComponent(component.getParent(),buf);
    }

    @Override
    public void writeState(FacesContext facesContext,
                           SerializedView serializedView) throws IOException
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering writeState");

        UIViewRoot uiViewRoot = facesContext.getViewRoot();
        //save state in response (client)
        RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, uiViewRoot.getRenderKitId());
        ResponseStateManager responseStateManager = renderKit.getResponseStateManager();

        if (isLegacyResponseStateManager(responseStateManager))
        {
            responseStateManager.writeState(facesContext, serializedView);
        }
        else if (!isSavingStateInClient(facesContext))
        {
            Object[] state = new Object[2];
            state[JSF_SEQUENCE_INDEX] = Integer.toString(getNextViewSequence(facesContext), Character.MAX_RADIX);
            responseStateManager.writeState(facesContext, state);
        }
        else
        {
            Object[] state = new Object[2];
            state[0] = serializedView.getStructure();
            state[1] = serializedView.getState();
            responseStateManager.writeState(facesContext, state);
        }

        if (log.isLoggable(Level.FINEST)) log.finest("Exiting writeState");

    }

    @Override
    public String getViewState(FacesContext facesContext)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();
        String viewId = uiViewRoot.getViewId();
        ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(facesContext,viewId);
        if (vdl != null)
        {
            StateManagementStrategy sms = vdl.getStateManagementStrategy(facesContext, viewId);
            
            if (sms != null)
            {
                if (log.isLoggable(Level.FINEST)) log.finest("Calling saveView of StateManagementStrategy from getViewState: "+sms.getClass().getName());
                
                return facesContext.getRenderKit().getResponseStateManager().getViewState(facesContext, saveView(facesContext));
            }
        }
        Object[] savedState = (Object[]) saveView(facesContext);
        
        if (!isSavingStateInClient(facesContext))
        {
            Object[] state = new Object[2];
            state[JSF_SEQUENCE_INDEX] = Integer.toString(getNextViewSequence(facesContext), Character.MAX_RADIX);
            return facesContext.getRenderKit().getResponseStateManager().getViewState(facesContext, state);
        }
        else
        {
            return facesContext.getRenderKit().getResponseStateManager().getViewState(facesContext, savedState);
        }
    }

    /**
     * MyFaces extension
     * @param facesContext
     * @param serializedView
     * @throws IOException
     */
    @Override
    public void writeStateAsUrlParams(FacesContext facesContext,
                                      SerializedView serializedView) throws IOException
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering writeStateAsUrlParams");

        if (isSavingStateInClient(facesContext))
        {
            if (log.isLoggable(Level.FINEST)) log.finest("Processing writeStateAsUrlParams - client-side state saving writing state");

            UIViewRoot uiViewRoot = facesContext.getViewRoot();
            //save state in response (client)
            RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, uiViewRoot.getRenderKitId());
            ResponseStateManager responseStateManager = renderKit.getResponseStateManager();
            if (responseStateManager instanceof MyfacesResponseStateManager)
            {
                ((MyfacesResponseStateManager)responseStateManager).writeStateAsUrlParams(facesContext,
                                                                                          serializedView);
            }
            else
            {
                log.severe("ResponseStateManager of render kit " + uiViewRoot.getRenderKitId() + " is no MyfacesResponseStateManager and does not support saving state in url parameters.");
            }
        }

        if (log.isLoggable(Level.FINEST)) log.finest("Exiting writeStateAsUrlParams");
    }

    //helpers

    protected RenderKitFactory getRenderKitFactory()
    {
        if (_renderKitFactory == null)
        {
            _renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return _renderKitFactory;
    }

    protected void saveSerializedViewInServletSession(FacesContext context,
                                                      Object serializedView)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        SerializedViewCollection viewCollection = (SerializedViewCollection) sessionMap
                .get(SERIALIZED_VIEW_SESSION_ATTR);
        if (viewCollection == null)
        {
            viewCollection = new SerializedViewCollection();
            sessionMap.put(SERIALIZED_VIEW_SESSION_ATTR, viewCollection);
        }
        viewCollection.add(context, serializeView(context, serializedView));
        // replace the value to notify the container about the change
        sessionMap.put(SERIALIZED_VIEW_SESSION_ATTR, viewCollection);
    }

    protected Object getSerializedViewFromServletSession(FacesContext context, String viewId, Integer sequence)
    {
        ExternalContext externalContext = context.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();
        Object serializedView = null;
        if (requestMap.containsKey(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR))
        {
            serializedView = requestMap.get(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR);
        }
        else
        {
            SerializedViewCollection viewCollection = (SerializedViewCollection) externalContext
                    .getSessionMap().get(SERIALIZED_VIEW_SESSION_ATTR);
            if (viewCollection != null)
            {
                /*
                String sequenceStr = externalContext.getRequestParameterMap().get(
                       RendererUtils.SEQUENCE_PARAM);
                Integer sequence = null;
                if (sequenceStr == null)
                {
                    // use latest sequence
                    Map map = externalContext.getSessionMap();
                    sequence = (Integer) map.get(RendererUtils.SEQUENCE_PARAM);
                }
                else
                {
                    sequence = new Integer(sequenceStr);
                }
                */
                if (sequence != null)
                {
                    Object state = viewCollection.get(sequence, viewId);
                    if (state != null)
                    {
                        serializedView = deserializeView(state);
                    }
                }
            }
            requestMap.put(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR, serializedView);
            nextViewSequence(context);
        }
        return serializedView;
    }

    protected int getNextViewSequence(FacesContext context)
    {
        ExternalContext externalContext = context.getExternalContext();

        if (!externalContext.getRequestMap().containsKey(RendererUtils.SEQUENCE_PARAM))
        {
            nextViewSequence(context);
        }

        Integer sequence = (Integer) externalContext.getRequestMap().get(RendererUtils.SEQUENCE_PARAM);
        return sequence.intValue();
    }

    protected void nextViewSequence(FacesContext facescontext)
    {
        ExternalContext externalContext = facescontext.getExternalContext();
        Object sessionObj = externalContext.getSession(true);
        synchronized(sessionObj) // synchronized to increase sequence if multiple requests
                                 // are handled at the same time for the session
        {
            Map<String, Object> map = externalContext.getSessionMap();
            Integer sequence = (Integer) map.get(RendererUtils.SEQUENCE_PARAM);
            if(sequence == null || sequence.intValue() == Integer.MAX_VALUE)
            {
                sequence = Integer.valueOf(1);
            }
            else
            {
                sequence = Integer.valueOf(sequence.intValue() + 1);
            }
            map.put(RendererUtils.SEQUENCE_PARAM, sequence);
            externalContext.getRequestMap().put(RendererUtils.SEQUENCE_PARAM, sequence);
        }
    }

    protected Object serializeView(FacesContext context, Object serializedView)
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering serializeView");

        if(isSerializeStateInSession(context))
        {
            if (log.isLoggable(Level.FINEST)) log.finest("Processing serializeView - serialize state in session");

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try
            {
                OutputStream os = baos;
                if(isCompressStateInSession(context))
                {
                    if (log.isLoggable(Level.FINEST)) log.finest("Processing serializeView - serialize compressed");

                    os.write(COMPRESSED_FLAG);
                    os = new GZIPOutputStream(os, 1024);
                }
                else
                {
                    if (log.isLoggable(Level.FINEST)) log.finest("Processing serializeView - serialize uncompressed");

                    os.write(UNCOMPRESSED_FLAG);
                }

                Object[] stateArray = (Object[]) serializedView;

                ObjectOutputStream out = new ObjectOutputStream(os);
                out.writeObject(stateArray[0]);
                out.writeObject(stateArray[1]);
                out.close();
                baos.close();

                if (log.isLoggable(Level.FINEST)) log.finest("Exiting serializeView - serialized. Bytes : "+baos.size());
                return baos.toByteArray();
            }
            catch (IOException e)
            {
                log.log(Level.SEVERE, "Exiting serializeView - Could not serialize state: " + e.getMessage(), e);
                return null;
            }
        }


        if (log.isLoggable(Level.FINEST))
            log.finest("Exiting serializeView - do not serialize state in session.");

        return serializedView;

    }

    /**
     * Reads the value of the <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> context parameter.
     * @see SERIALIZE_STATE_IN_SESSION_PARAM
     * @param context <code>FacesContext</code> for the request we are processing.
     * @return boolean true, if the server state should be serialized in the session
     */
    protected boolean isSerializeStateInSession(FacesContext context)
    {
        String value = context.getExternalContext().getInitParameter(
                SERIALIZE_STATE_IN_SESSION_PARAM);
        boolean serialize = DEFAULT_SERIALIZE_STATE_IN_SESSION;
        if (value != null)
        {
           serialize = Boolean.valueOf(value);
        }
        return serialize;
    }

    /**
     * Reads the value of the <code>org.apache.myfaces.COMPRESS_STATE_IN_SESSION</code> context parameter.
     * @see COMPRESS_SERVER_STATE_PARAM
     * @param context <code>FacesContext</code> for the request we are processing.
     * @return boolean true, if the server state steam should be compressed
     */
    protected boolean isCompressStateInSession(FacesContext context)
    {
        String value = context.getExternalContext().getInitParameter(
                COMPRESS_SERVER_STATE_PARAM);
        boolean compress = DEFAULT_COMPRESS_SERVER_STATE_PARAM;
        if (value != null)
        {
           compress = Boolean.valueOf(value);
        }
        return compress;
    }

    protected Object deserializeView(Object state)
    {
        if (log.isLoggable(Level.FINEST)) log.finest("Entering deserializeView");

        if(state instanceof byte[])
        {
            if (log.isLoggable(Level.FINEST)) log.finest("Processing deserializeView - deserializing serialized state. Bytes : "+((byte[]) state).length);

            try
            {
                ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) state);
                InputStream is = bais;
                if(is.read() == COMPRESSED_FLAG)
                {
                    is = new GZIPInputStream(is);
                }
                ObjectInputStream ois = null;
                try
                {
                    final ObjectInputStream in = new MyFacesObjectInputStream(is);
                    ois = in;
                    Object object = null;
                    if (System.getSecurityManager() != null) 
                    {
                        object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object []>() 
                        {
                            public Object[] run() throws PrivilegedActionException, IOException, ClassNotFoundException
                            {
                                return new Object[] {in.readObject(), in.readObject()};                                    
                            }
                        });
                    }
                    else
                    {
                        object = new Object[] {in.readObject(), in.readObject()};
                    }
                    return object;
                }
                finally
                {
                    if (ois != null)
                    {
                        ois.close();
                        ois = null;
                    }
                }
            }
            catch (PrivilegedActionException e) 
            {
                log.log(Level.SEVERE, "Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
            catch (IOException e)
            {
                log.log(Level.SEVERE, "Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
            catch (ClassNotFoundException e)
            {
                log.log(Level.SEVERE, "Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
        }
        else if (state instanceof Object[])
        {
            if (log.isLoggable(Level.FINEST)) log.finest("Exiting deserializeView - state not serialized.");

            return state;
        }
        else if(state == null)
        {
            log.severe("Exiting deserializeView - this method should not be called with a null-state.");
            return null;
        }
        else
        {
            log.severe("Exiting deserializeView - this method should not be called with a state of type : "+state.getClass());
            return null;
        }
    }

    private boolean isLegacyResponseStateManager(ResponseStateManager instance) {

        Method[] methods = instance.getClass().getMethods();
        for (Method m : methods)
        {
            if (m.getName().equals("getState") &&
                    Arrays.equals(m.getParameterTypes(),new Class[] {FacesContext.class, String.class}))
            {
                 return false;
            }
        }

        return true;
    }

    protected static class SerializedViewCollection implements Serializable
    {
        private static final long serialVersionUID = -3734849062185115847L;

        private final List<Object> _keys = new ArrayList<Object>(DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
        private final Map<Object, Object> _serializedViews = new HashMap<Object, Object>();

        // old views will be hold as soft references which will be removed by
        // the garbage collector if free memory is low
        private transient Map<Object, Object> _oldSerializedViews = null;

        public synchronized void add(FacesContext context, Object state)
        {
            Object key = new SerializedViewKey(context);
            _serializedViews.put(key, state);

            while (_keys.remove(key));
            _keys.add(key);

            int views = getNumberOfViewsInSession(context);
            while (_keys.size() > views)
            {
                key = _keys.remove(0);
                Object oldView = _serializedViews.remove(key);
                if (oldView != null && 
                    !CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF.equals(getCacheOldViewsInSessionMode(context))) 
                {
                    getOldSerializedViewsMap().put(key, oldView);
                }
            }
        }

        /**
         * Reads the amount (default = 20) of views to be stored in session.
         * @see NUMBER_OF_VIEWS_IN_SESSION_PARAM
         * @param context FacesContext for the current request, we are processing
         * @return Number vf views stored in the session
         */
        protected int getNumberOfViewsInSession(FacesContext context)
        {
            String value = context.getExternalContext().getInitParameter(
                    NUMBER_OF_VIEWS_IN_SESSION_PARAM);
            int views = DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
            if (value != null)
            {
                try
                {
                    views = Integer.parseInt(value);
                    if (views <= 0)
                    {
                        log.severe("Configured value for " + NUMBER_OF_VIEWS_IN_SESSION_PARAM
                                  + " is not valid, must be an value > 0, using default value ("
                                  + DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
                        views = DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
                    }
                }
                catch (Throwable e)
                {
                    log.log(Level.SEVERE, "Error determining the value for " + NUMBER_OF_VIEWS_IN_SESSION_PARAM
                              + ", expected an integer value > 0, using default value ("
                              + DEFAULT_NUMBER_OF_VIEWS_IN_SESSION + "): " + e.getMessage(), e);
                }
            }
            return views;
        }

        /**
         * @return old serialized views map
         */
        @SuppressWarnings("unchecked")
        protected Map<Object, Object> getOldSerializedViewsMap()
        {
            FacesContext context = FacesContext.getCurrentInstance();
            if (_oldSerializedViews == null && context != null)
            {
                String cacheMode = getCacheOldViewsInSessionMode(context); 
                if (CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK.equals(cacheMode))
                {
                    _oldSerializedViews = new ReferenceMap(AbstractReferenceMap.WEAK, AbstractReferenceMap.WEAK, true);
                }
                else if (CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK.equals(cacheMode))
                {
                    _oldSerializedViews = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.WEAK, true);
                }
                else if (CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT.equals(cacheMode))
                {
                    _oldSerializedViews = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true);
                }
                else if (CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT.equals(cacheMode))
                {
                    _oldSerializedViews = new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.SOFT);
                }
            }
            
            return _oldSerializedViews;
        }
        
        /**
         * Reads the value of the <code>org.apache.myfaces.CACHE_OLD_VIEWS_IN_SESSION_MODE</code> context parameter.
         * 
         * @since 1.2.5
         * @param context
         * @return constant indicating caching mode
         * @see CACHE_OLD_VIEWS_IN_SESSION_MODE
         */
        protected String getCacheOldViewsInSessionMode(FacesContext context)
        {
            String value = context.getExternalContext().getInitParameter(
                    CACHE_OLD_VIEWS_IN_SESSION_MODE);
            if (value == null)
            {
                return CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF;
            }
            else if (value.equalsIgnoreCase(CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT))
            {
                return CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT;
            }
            else if (value.equalsIgnoreCase(CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK))
            {
                return CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK;
            }            
            else if (value.equalsIgnoreCase(CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK))
            {
                return CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK;
            }
            else if (value.equalsIgnoreCase(CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT))
            {
                return CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT;
            }
            else
            {
                return CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF;
            }
        }
        
        public Object get(Integer sequence, String viewId)
        {
            Object key = new SerializedViewKey(viewId, sequence);
            Object value = _serializedViews.get(key);
            if (value == null)
            {
                Map<Object,Object> oldSerializedViewMap = getOldSerializedViewsMap();
                if (oldSerializedViewMap != null)
                {
                    value = oldSerializedViewMap.get(key);
                }
            }
            return value;
        }
    }

    protected static class SerializedViewKey implements Serializable
    {
        private static final long serialVersionUID = -1170697124386063642L;

        private final String _viewId;
        private final Integer _sequenceId;

        public SerializedViewKey(String viewId, Integer sequence)
        {
            _sequenceId = sequence;
            _viewId = viewId;
        }

        public SerializedViewKey(FacesContext context)
        {
            _sequenceId = RendererUtils.getViewSequence(context);
            _viewId = context.getViewRoot().getViewId();
        }

        @Override
        public int hashCode()
        {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((_sequenceId == null) ? 0 : _sequenceId.hashCode());
            result = PRIME * result + ((_viewId == null) ? 0 : _viewId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final SerializedViewKey other = (SerializedViewKey) obj;
            if (_sequenceId == null)
            {
                if (other._sequenceId != null)
                    return false;
            }
            else if (!_sequenceId.equals(other._sequenceId))
                return false;
            if (_viewId == null)
            {
                if (other._viewId != null)
                    return false;
            }
            else if (!_viewId.equals(other._viewId))
                return false;
            return true;
        }

    }
}