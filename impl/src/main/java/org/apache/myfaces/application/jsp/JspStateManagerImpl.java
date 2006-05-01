/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.MyfacesStateManager;
import org.apache.myfaces.application.TreeStructureManager;
import org.apache.myfaces.renderkit.MyfacesResponseStateManager;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.util.MyFacesObjectInputStream;

/**
 * Default StateManager implementation for use when views are defined
 * via tags in JSP pages.
 * 
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Manfred Geiler
 * @version $Revision$ $Date$
 */
public class JspStateManagerImpl
    extends MyfacesStateManager
{
    private static final Log log = LogFactory.getLog(JspStateManagerImpl.class);
    private static final String SERIALIZED_VIEW_SESSION_ATTR
            = JspStateManagerImpl.class.getName() + ".SERIALIZED_VIEW";
    private static final String SERIALIZED_VIEW_REQUEST_ATTR
            = JspStateManagerImpl.class.getName() + ".SERIALIZED_VIEW";
    private static final String RESTORED_SERIALIZED_VIEW_REQUEST_ATTR
    = JspStateManagerImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW";

    /**
     * Only applicable if state saving method is "server" (= default).
     * Defines the amount (default = 20) of the latest views are stored in session.
     */
    /**
     * Only applicable if state saving method is "server" (= default).
     * Defines the amount (default = 20) of the latest views are stored in session.
     */
    private static final String NUMBER_OF_VIEWS_IN_SESSION_PARAM = "org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION";

    /**
     * Default value for <code>org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION</code> context parameter.
     */
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

    private static final int UNCOMPRESSED_FLAG = 0;
    private static final int COMPRESSED_FLAG = 1;

    private RenderKitFactory _renderKitFactory = null;

    public JspStateManagerImpl()
    {
        if (log.isTraceEnabled()) log.trace("New JspStateManagerImpl instance created");
    }

    protected Object getComponentStateToSave(FacesContext facesContext)
    {
        if (log.isTraceEnabled()) log.trace("Entering getComponentStateToSave");

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }

        Object serializedComponentStates = viewRoot.processSaveState(facesContext);
        //Locale is a state attribute of UIViewRoot and need not be saved explicitly
        if (log.isTraceEnabled()) log.trace("Exiting getComponentStateToSave");
        return serializedComponentStates;
    }

    /**
     * Return an object which contains info about the UIComponent type
     * of each node in the view tree. This allows an identical UIComponent
     * tree to be recreated later, though all the components will have
     * just default values for their members.
     */
    protected Object getTreeStructureToSave(FacesContext facesContext)
    {
        if (log.isTraceEnabled()) log.trace("Entering getTreeStructureToSave");
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }
        TreeStructureManager tsm = new TreeStructureManager();
        Object retVal = tsm.buildTreeStructureToSave(viewRoot);
        if (log.isTraceEnabled()) log.trace("Exiting getTreeStructureToSave");
        return retVal;
    }

    /**
     * Given a tree of UIComponent objects created the default constructor
     * for each node, retrieve saved state info (from either the client or
     * the server) and walk the tree restoring the members of each node
     * from the saved state information.
     */
    protected void restoreComponentState(FacesContext facesContext,
                                         UIViewRoot uiViewRoot,
                                         String renderKitId)
    {
        if (log.isTraceEnabled()) log.trace("Entering restoreComponentState");

        //===========================================
        // first, locate the saved state information
        //===========================================

        Object serializedComponentStates;
        if (isSavingStateInClient(facesContext))
        {
            RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
            ResponseStateManager responseStateManager = renderKit.getResponseStateManager();
            serializedComponentStates = responseStateManager.getComponentStateToRestore(facesContext);
            if (serializedComponentStates == null)
            {
                log.error("No serialized component state found in client request!");
                // mark UIViewRoot invalid by resetting view id
                uiViewRoot.setViewId(null);
                return;
            }
        }
        else
        {
            SerializedView serializedView = getSerializedViewFromServletSession(facesContext,
                                                                                uiViewRoot.getViewId());
            if (serializedView == null)
            {
                log.error("No serialized view found in server session!");
                // mark UIViewRoot invalid by resetting view id
                uiViewRoot.setViewId(null);
                return;
            }
            serializedComponentStates = serializedView.getState();
            if (serializedComponentStates == null)
            {
                log.error("No serialized component state found in server session!");
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

        if (log.isTraceEnabled()) log.trace("Exiting restoreComponentState");
    }

    /**
     * See getTreeStructureToSave.
     */
    protected UIViewRoot restoreTreeStructure(FacesContext facesContext,
                                              String viewId,
                                              String renderKitId)
    {
        if (log.isTraceEnabled()) log.trace("Entering restoreTreeStructure");

        UIViewRoot uiViewRoot;
        if (isSavingStateInClient(facesContext))
        {
            //reconstruct tree structure from request
            RenderKit rk = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
            ResponseStateManager responseStateManager = rk.getResponseStateManager();
            Object treeStructure = responseStateManager.getTreeStructureToRestore(facesContext, viewId);
            if (treeStructure == null)
            {
                if (log.isDebugEnabled()) log.debug("Exiting restoreTreeStructure - No tree structure state found in client request");
                return null;
            }

            TreeStructureManager tsm = new TreeStructureManager();
            uiViewRoot = tsm.restoreTreeStructure((TreeStructureManager.TreeStructComponent)treeStructure);
            if (log.isTraceEnabled()) log.trace("Tree structure restored from client request");
        }
        else
        {
            //reconstruct tree structure from ServletSession
            SerializedView serializedView = getSerializedViewFromServletSession(facesContext,
                                                                                viewId);
            if (serializedView == null)
            {
                if (log.isDebugEnabled()) log.debug("Exiting restoreTreeStructure - No serialized view found in server session!");
                return null;
            }

            Object treeStructure = serializedView.getStructure();
            if (treeStructure == null)
            {
                if (log.isDebugEnabled()) log.debug("Exiting restoreTreeStructure - No tree structure state found in server session, former UIViewRoot must have been transient");
                return null;
            }

            TreeStructureManager tsm = new TreeStructureManager();
            uiViewRoot = tsm.restoreTreeStructure((TreeStructureManager.TreeStructComponent)serializedView.getStructure());
            if (log.isTraceEnabled()) log.trace("Tree structure restored from server session");
        }

        if (log.isTraceEnabled()) log.trace("Exiting restoreTreeStructure");
        return uiViewRoot;
    }

    public UIViewRoot restoreView(FacesContext facescontext, String viewId, String renderKitId)
    {
        if (log.isTraceEnabled()) log.trace("Entering restoreView");

        UIViewRoot uiViewRoot = restoreTreeStructure(facescontext, viewId, renderKitId);
        if (uiViewRoot != null)
        {
            uiViewRoot.setViewId(viewId);
            restoreComponentState(facescontext, uiViewRoot, renderKitId);
            String restoredViewId = uiViewRoot.getViewId();
            if (restoredViewId == null || !(restoredViewId.equals(viewId)))
            {
                if (log.isTraceEnabled()) log.trace("Exiting restoreView - restored view is null.");
                return null;
            }
        }

        if (log.isTraceEnabled()) log.trace("Exiting restoreView");

        return uiViewRoot;
    }

    public SerializedView saveSerializedView(FacesContext facesContext) throws IllegalStateException
    {
        if (log.isTraceEnabled()) log.trace("Entering saveSerializedView");

        checkForDuplicateIds(facesContext, facesContext.getViewRoot(), new HashSet());

        if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - Checked for duplicate Ids");

        ExternalContext externalContext = facesContext.getExternalContext();

        // SerializedView already created before within this request?
        SerializedView serializedView = (SerializedView)externalContext.getRequestMap()
                                                            .get(SERIALIZED_VIEW_REQUEST_ATTR);
        if (serializedView == null)
        {
            if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - create new serialized view");

            // first call to saveSerializedView --> create SerializedView
            Object treeStruct = getTreeStructureToSave(facesContext);
            Object compStates = getComponentStateToSave(facesContext);
            serializedView = new StateManager.SerializedView(treeStruct, compStates);
            externalContext.getRequestMap().put(SERIALIZED_VIEW_REQUEST_ATTR,
                                                serializedView);

            if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - new serialized view created");
        }

        if (!isSavingStateInClient(facesContext))
        {
            if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - server-side state saving - save state");
            //save state in server session
            saveSerializedViewInServletSession(facesContext, serializedView);

            if (log.isTraceEnabled()) log.trace("Exiting saveSerializedView - server-side state saving - saved state");
            return null;
        }

        if (log.isTraceEnabled()) log.trace("Exiting saveSerializedView - client-side state saving");

        return serializedView;
    }

    private static void checkForDuplicateIds(FacesContext context,
                                             UIComponent component,
                                             Set ids)
    {
        String id = component.getId();
        if (id != null && !ids.add(id))
        {
            throw new IllegalStateException("Client-id : "+id +
                                            " is duplicated in the faces tree. Component : "+component.getClientId(context)+", path: "+
                                            getPathToComponent(component));
        }
        Iterator it = component.getFacetsAndChildren();
        boolean namingContainer = component instanceof NamingContainer;
        while (it.hasNext())
        {
            UIComponent kid = (UIComponent) it.next();
            if (namingContainer)
            {
                checkForDuplicateIds(context, kid, new HashSet());
            }
            else
            {
                checkForDuplicateIds(context, kid, ids);
            }
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

        if(component!=null)
        {
            getPathToComponent(component.getParent(),buf);
        }
    }

    public void writeState(FacesContext facesContext,
                           SerializedView serializedView) throws IOException
    {
        if (log.isTraceEnabled()) log.trace("Entering writeState");

        if (isSavingStateInClient(facesContext))
        {
            if (log.isTraceEnabled()) log.trace("Processing writeState - client-side state-saving writing state");

            UIViewRoot uiViewRoot = facesContext.getViewRoot();
            //save state in response (client)
            RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, uiViewRoot.getRenderKitId());
            renderKit.getResponseStateManager().writeState(facesContext, serializedView);
        }

        if (log.isTraceEnabled()) log.trace("Exiting writeState");


    }

    /**
     * MyFaces extension
     * @param facesContext
     * @param serializedView
     * @throws IOException
     */
    public void writeStateAsUrlParams(FacesContext facesContext,
                                      SerializedView serializedView) throws IOException
    {
        if (log.isTraceEnabled()) log.trace("Entering writeStateAsUrlParams");

        if (isSavingStateInClient(facesContext))
        {
            if (log.isTraceEnabled()) log.trace("Processing writeStateAsUrlParams - client-side state saving writing state");

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
                log.error("ResponseStateManager of render kit " + uiViewRoot.getRenderKitId() + " is no MyfacesResponseStateManager and does not support saving state in url parameters.");
            }
        }

        if (log.isTraceEnabled()) log.trace("Exiting writeStateAsUrlParams");
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
                                                      SerializedView serializedView)
    {
        Map sessionMap = context.getExternalContext().getSessionMap();
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

    protected SerializedView getSerializedViewFromServletSession(FacesContext context, String viewId)
    {
        ExternalContext externalContext = context.getExternalContext();
        Map requestMap = externalContext.getRequestMap();
        SerializedView serializedView = null;
        if (requestMap.containsKey(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR))
        {
            serializedView = (SerializedView) requestMap.get(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR);
        }
        else
        {
            SerializedViewCollection viewCollection = (SerializedViewCollection) externalContext
                    .getSessionMap().get(SERIALIZED_VIEW_SESSION_ATTR);
            if (viewCollection != null)
            {
                String sequenceStr = (String) externalContext.getRequestParameterMap().get(
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

    protected void nextViewSequence(FacesContext facescontext)
    {
        ExternalContext externalContext = facescontext.getExternalContext();
        Object sessionObj = externalContext.getSession(true);
        synchronized(sessionObj) // synchronized to increase sequence if multiple requests 
                                 // are handled at the same time for the session
        {
            Map map = externalContext.getSessionMap();
            Integer sequence = (Integer) map.get(RendererUtils.SEQUENCE_PARAM);
            if(sequence == null || sequence.intValue() == Integer.MAX_VALUE)
            {
                sequence = new Integer(1);
            }
            else
            {
                sequence = new Integer(sequence.intValue() + 1);
            }
            map.put(RendererUtils.SEQUENCE_PARAM, sequence);
            externalContext.getRequestMap().put(RendererUtils.SEQUENCE_PARAM, sequence);
        }
    }

    protected Object serializeView(FacesContext context, SerializedView serializedView)
    {
        if (log.isTraceEnabled()) log.trace("Entering serializeView");

        if(isSerializeStateInSession(context))
        {
            if (log.isTraceEnabled()) log.trace("Processing serializeView - serialize state in session");

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try
            {
                OutputStream os = baos;
                if(isCompressStateInSession(context))
                {
                    if (log.isTraceEnabled()) log.trace("Processing serializeView - serialize compressed");

                    os.write(COMPRESSED_FLAG);
                    os = new GZIPOutputStream(os, 1024);
                }
                else
                {
                    if (log.isTraceEnabled()) log.trace("Processing serializeView - serialize uncompressed");

                    os.write(UNCOMPRESSED_FLAG);
                }
                ObjectOutputStream out = new ObjectOutputStream(os);
                out.writeObject(serializedView.getStructure());
                out.writeObject(serializedView.getState());
                out.close();
                baos.close();

                if (log.isTraceEnabled()) log.trace("Exiting serializeView - serialized. Bytes : "+baos.size());
                return baos.toByteArray();
            }
            catch (IOException e)
            {
                log.error("Exiting serializeView - Could not serialize state: " + e.getMessage(), e);
                return null;
            }
        }
        else
        {
            if (log.isTraceEnabled()) log.trace("Exiting serializeView - do not serialize state in session.");
            return new Object[] {serializedView.getStructure(), serializedView.getState()};
        }
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
           serialize = new Boolean(value).booleanValue();
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
           compress = new Boolean(value).booleanValue();
        }
        return compress;
    }

    protected SerializedView deserializeView(Object state)
    {
        if (log.isTraceEnabled()) log.trace("Entering deserializeView");

        if(state instanceof byte[])
        {
            if (log.isTraceEnabled()) log.trace("Processing deserializeView - deserializing serialized state. Bytes : "+((byte[]) state).length);

            try
            {
                ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) state);
                InputStream is = bais;
                if(is.read() == COMPRESSED_FLAG)
                {
                    is = new GZIPInputStream(is);
                }
                ObjectInputStream in = new MyFacesObjectInputStream(
                        is);
                return new SerializedView(in.readObject(), in.readObject());
            }
            catch (IOException e)
            {
                log.error("Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
            catch (ClassNotFoundException e)
            {
                log.error("Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
        }
        else if (state instanceof Object[])
        {
            if (log.isTraceEnabled()) log.trace("Exiting deserializeView - state not serialized.");

            Object[] value = (Object[]) state;
            return new SerializedView(value[0], value[1]);
        }
        else if(state == null)
        {
            log.error("Exiting deserializeView - this method should not be called with a null-state.");
            return null;
        }
        else
        {
            log.error("Exiting deserializeView - this method should not be called with a state of type : "+state.getClass());
            return null;
        }
    }

    protected static class SerializedViewCollection implements Serializable
    {
        private static final long serialVersionUID = -3734849062185115847L;

        private final List _keys = new ArrayList(DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
        private final Map _serializedViews = new HashMap();

        // old views will be hold as soft references which will be removed by 
        // the garbage collector if free memory is low
        private transient Map _oldSerializedViews = null;

        public synchronized void add(FacesContext context, Object state)
        {
            Object key = new SerializedViewKey(context);
            _serializedViews.put(key, state);
            _keys.add(key);

            int views = getNumberOfViewsInSession(context);
            while (_keys.size() > views)
            {
                key = _keys.remove(0);
                getOldSerializedViewsMap().put(key, _serializedViews.remove(key));
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
                        log.error("Configured value for " + NUMBER_OF_VIEWS_IN_SESSION_PARAM
                                  + " is not valid, must be an value > 0, using default value ("
                                  + DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
                        views = DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
                    }
                }
                catch (Throwable e)
                {
                    log.error("Error determining the value for " + NUMBER_OF_VIEWS_IN_SESSION_PARAM
                              + ", expected an integer value > 0, using default value ("
                              + DEFAULT_NUMBER_OF_VIEWS_IN_SESSION + "): " + e.getMessage(), e);
                }
            }
            return views;
        }

        /**
         * @return old serialized views map
         */
        protected Map getOldSerializedViewsMap()
        {
            if (_oldSerializedViews == null)
            {
                _oldSerializedViews = new ReferenceMap();
            }
            return _oldSerializedViews;
        }

        public Object get(Integer sequence, String viewId)
        {
            Object key = new SerializedViewKey(viewId, sequence);
            Object value = _serializedViews.get(key);
            if (value == null)
            {
                value = getOldSerializedViewsMap().get(key);
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

        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof SerializedViewKey)
            {
                SerializedViewKey other = (SerializedViewKey) obj;
                return new EqualsBuilder().append(other._viewId, _viewId).append(other._sequenceId,
                                                                                 _sequenceId).isEquals();
            }
            return false;
        }

        public int hashCode()
        {
            return new HashCodeBuilder().append(_viewId).append(_sequenceId).toHashCode();
        }
    }
}
