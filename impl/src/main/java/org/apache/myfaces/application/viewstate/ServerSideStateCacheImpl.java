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
package org.apache.myfaces.application.viewstate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.faces.FacesWrapper;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.ClientWindow;

import org.apache.myfaces.application.StateCache;
import org.apache.myfaces.application.viewstate.token.ServerSideStateTokenProcessor;
import org.apache.myfaces.application.viewstate.token.StateTokenProcessor;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.util.MyFacesObjectInputStream;
import org.apache.myfaces.spi.ViewScopeProvider;
import org.apache.myfaces.spi.ViewScopeProviderFactory;
import org.apache.myfaces.view.ViewScopeProxyMap;

class ServerSideStateCacheImpl extends StateCache<Object, Object>
{
    private static final Logger log = Logger.getLogger(ServerSideStateCacheImpl.class.getName());
    
    public static final String SERIALIZED_VIEW_SESSION_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".SERIALIZED_VIEW";
    
    public static final String RESTORED_SERIALIZED_VIEW_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW";
    
    public static final String RESTORED_SERIALIZED_VIEW_ID_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW_ID";
    public static final String RESTORED_SERIALIZED_VIEW_KEY_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW_KEY";

    public static final String RESTORED_VIEW_KEY_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_VIEW_KEY";

    public static final int UNCOMPRESSED_FLAG = 0;
    public static final int COMPRESSED_FLAG = 1;

    private final boolean useFlashScopePurgeViewsInSession;
    private final int numberOfSequentialViewsInSession;
    private final boolean serializeStateInSession;
    private final boolean compressStateInSession;
    
    private final SessionViewStorageFactory sessionViewStorageFactory;
    private final CsrfSessionTokenFactory csrfSessionTokenFactory;
    private final StateTokenProcessor stateTokenProcessor;
    
    public ServerSideStateCacheImpl()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(facesContext);
        
        useFlashScopePurgeViewsInSession = config.isUseFlashScopePurgeViewsInSession();
        numberOfSequentialViewsInSession = config.getNumberOfSequentialViewsInSession();
        serializeStateInSession = config.isSerializeStateInSession();
        compressStateInSession = config.isCompressStateInSession();
        
        String randomMode = config.getRandomKeyInViewStateSessionToken();
        if (MyfacesConfig.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM.equals(randomMode))
        {
            sessionViewStorageFactory = new RandomSessionViewStorageFactory( new SecureRandomKeyFactory(facesContext));
        }
        else if (MyfacesConfig.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_RANDOM.equals(randomMode))
        {
            sessionViewStorageFactory = new RandomSessionViewStorageFactory(new RandomKeyFactory(facesContext));
        }
        else
        {
            if (randomMode != null && !randomMode.isEmpty())
            {
                log.warning(MyfacesConfig.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN + " \""
                        + randomMode + "\" is not supported (anymore)."
                        + " Fallback to \"random\"");
            }
            sessionViewStorageFactory = new RandomSessionViewStorageFactory(new RandomKeyFactory(facesContext));
        }
        
        String csrfRandomMode = config.getRandomKeyInCsrfSessionToken();
        if (MyfacesConfig.RANDOM_KEY_IN_CSRF_SESSION_TOKEN_SECURE_RANDOM.equals(csrfRandomMode))
        {
            csrfSessionTokenFactory = new SecureRandomCsrfSessionTokenFactory(facesContext);
        }
        else
        {
            csrfSessionTokenFactory = new RandomCsrfSessionTokenFactory(facesContext);
        }
        
        stateTokenProcessor = new ServerSideStateTokenProcessor();
    }
    
    //------------------------------------- METHODS COPIED FROM JspStateManagerImpl--------------------------------

    protected Object getServerStateId(FacesContext facesContext, Object state)
    {
        if (state != null)
        {
            return sessionViewStorageFactory.getKeyFactory().decode((String) state);
        }
        return null;
    }

    protected void saveSerializedViewInServletSession(FacesContext context, Object serializedView)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        SerializedViewCollection viewCollection = (SerializedViewCollection) sessionMap
                .get(SERIALIZED_VIEW_SESSION_ATTR);
        if (viewCollection == null)
        {
            viewCollection = sessionViewStorageFactory.createSerializedViewCollection(context);
            sessionMap.put(SERIALIZED_VIEW_SESSION_ATTR, viewCollection);
        }

        Map<Object,Object> attributeMap = context.getAttributes();
        
        SerializedViewKey key = null;
        if (numberOfSequentialViewsInSession > 0)
        {
            key = (SerializedViewKey) attributeMap.get(RESTORED_VIEW_KEY_REQUEST_ATTR);
            
            if (key == null )
            {
                // Check if clientWindow is enabled and if the last view key is stored
                // into session, so we can use it to chain the precedence in GET-GET
                // cases.
                ClientWindow clientWindow = context.getExternalContext().getClientWindow();
                if (clientWindow != null)
                {
                    key = (SerializedViewKey) viewCollection.getLastWindowKey(context, clientWindow.getId());
                }
                else if (useFlashScopePurgeViewsInSession && Boolean.TRUE.equals(
                        context.getExternalContext().getRequestMap().get("oam.Flash.REDIRECT.PREVIOUSREQUEST")))
                {
                    key = (SerializedViewKey)
                            context.getExternalContext().getFlash().get(RESTORED_VIEW_KEY_REQUEST_ATTR);
                }
            }
        }
        
        SerializedViewKey nextKey = sessionViewStorageFactory.createSerializedViewKey(
                context, context.getViewRoot().getViewId(), getNextViewSequence(context));
        // Get viewScopeMapId
        ViewScopeProxyMap viewScopeProxyMap = null;
        Object viewMap = context.getViewRoot().getViewMap(false);
        if (viewMap != null)
        {
            while (viewMap != null)
            {
                if (viewMap instanceof ViewScopeProxyMap)
                {
                    viewScopeProxyMap = (ViewScopeProxyMap)viewMap;
                    break;
                }
                else if (viewMap instanceof FacesWrapper)
                {
                    viewMap = ((FacesWrapper)viewMap).getWrapped();
                }
            }

        }
        if (viewScopeProxyMap != null)
        {
            ViewScopeProviderFactory factory = ViewScopeProviderFactory.getViewScopeHandlerFactory(
                context.getExternalContext());
            ViewScopeProvider handler = factory.getViewScopeHandler(context.getExternalContext());
            viewCollection.put(context, serializeView(context, serializedView), nextKey, key,
                    handler, viewScopeProxyMap.getViewScopeId());
        }
        else
        {
            viewCollection.put(context, serializeView(context, serializedView), nextKey, key);
        }

        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        if (clientWindow != null)
        {
            //Update the last key generated for the current windowId in session map
            viewCollection.putLastWindowKey(context, clientWindow.getId(), nextKey);
        }
        
        // replace the value to notify the container about the change
        sessionMap.put(SERIALIZED_VIEW_SESSION_ATTR, viewCollection);
    }

    protected Object getSerializedViewFromServletSession(FacesContext context, String viewId, Object sequence)
    {
        ExternalContext externalContext = context.getExternalContext();
        Map<Object, Object> attributeMap = context.getAttributes();
        Object serializedView = null;
        if (attributeMap.containsKey(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR))
        {
            serializedView = attributeMap.get(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR);
        }
        else
        {
            SerializedViewCollection viewCollection = (SerializedViewCollection) externalContext
                    .getSessionMap().get(SERIALIZED_VIEW_SESSION_ATTR);
            if (viewCollection != null)
            {
                if (sequence != null)
                {
                    Object state = viewCollection.get(
                            sessionViewStorageFactory.createSerializedViewKey(context, viewId, sequence));
                    if (state != null)
                    {
                        serializedView = deserializeView(state);
                    }
                }
            }
            attributeMap.put(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR, serializedView);
            
            if (numberOfSequentialViewsInSession > 0)
            {
                SerializedViewKey key = sessionViewStorageFactory.createSerializedViewKey(context, viewId, sequence);
                attributeMap.put(RESTORED_VIEW_KEY_REQUEST_ATTR, key);
                
                if (useFlashScopePurgeViewsInSession)
                {
                    externalContext.getFlash().put(RESTORED_VIEW_KEY_REQUEST_ATTR, key);
                    externalContext.getFlash().keep(RESTORED_VIEW_KEY_REQUEST_ATTR);
                }
            }

            if (context.getPartialViewContext().isAjaxRequest() || context.getPartialViewContext().isPartialRequest())
            {
                // Save the information used to restore. The idea is use this information later
                // to decide if it is necessary to generate a new view sequence or use the existing
                // one.
                attributeMap.put(RESTORED_SERIALIZED_VIEW_KEY_REQUEST_ATTR, sequence);
                attributeMap.put(RESTORED_SERIALIZED_VIEW_ID_REQUEST_ATTR, viewId);
            }
            else
            {
                // Ensure a new sequence is used for the next view
                nextViewSequence(context);
            }
        }
        return serializedView;
    }

    protected Object getNextViewSequence(FacesContext context)
    {
        Object sequence = context.getAttributes().get(RendererUtils.SEQUENCE_PARAM);
        if (sequence == null)
        {
            if (context.getPartialViewContext().isAjaxRequest() ||
                context.getPartialViewContext().isPartialRequest())
            {
                String restoredViewId = (String) context.getAttributes().get(RESTORED_SERIALIZED_VIEW_ID_REQUEST_ATTR);
                Object restoredKey = context.getAttributes().get(RESTORED_SERIALIZED_VIEW_KEY_REQUEST_ATTR);
                if (restoredViewId != null && restoredKey != null)
                {
                    if (restoredViewId.equals(context.getViewRoot().getViewId()))
                    {
                        // The same viewId that was restored is the same that is being processed 
                        // and the request is partial or ajax. In this case we can reuse the restored
                        // key.
                        sequence = restoredKey;
                    }
                }
            }
            
            if (sequence == null)
            {
                sequence = nextViewSequence(context);
            }
            context.getAttributes().put(RendererUtils.SEQUENCE_PARAM, sequence);
        }
        return sequence;
    }

    protected Object nextViewSequence(FacesContext facescontext)
    {
        Object sequence = sessionViewStorageFactory.getKeyFactory().generateKey(facescontext);
        facescontext.getAttributes().put(RendererUtils.SEQUENCE_PARAM, sequence);
        return sequence;
    }

    protected Object serializeView(FacesContext context, Object serializedView)
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Entering serializeView");
        }

        if (serializeStateInSession)
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Processing serializeView - serialize state in session");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try
            {
                OutputStream os = baos;
                if (compressStateInSession)
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("Processing serializeView - serialize compressed");
                    }

                    os.write(COMPRESSED_FLAG);
                    os = new GZIPOutputStream(os, 1024);
                }
                else
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("Processing serializeView - serialize uncompressed");
                    }

                    os.write(UNCOMPRESSED_FLAG);
                }

                try (ObjectOutputStream out = new ObjectOutputStream(os))
                {
                    out.writeObject(serializedView);
                }
                
                baos.close();

                if (log.isLoggable(Level.FINEST))
                {
                    log.finest("Exiting serializeView - serialized. Bytes : " + baos.size());
                }
                return baos.toByteArray();
            }
            catch (IOException e)
            {
                log.log(Level.SEVERE, "Exiting serializeView - Could not serialize state: " + e.getMessage(), e);
                return null;
            }
        }


        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Exiting serializeView - do not serialize state in session.");
        }

        return serializedView;

    }

    protected Object deserializeView(Object state)
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Entering deserializeView");
        }

        if(state instanceof byte[])
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Processing deserializeView - deserializing serialized state. Bytes : "
                        + ((byte[]) state).length);
            }

            try
            {
                ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) state);

                InputStream is = bais;
                if (is.read() == COMPRESSED_FLAG)
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
                        object = AccessController.doPrivileged((PrivilegedExceptionAction) () -> in.readObject());
                    }
                    else
                    {
                        object = in.readObject();
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
            catch (PrivilegedActionException | IOException | ClassNotFoundException e) 
            {
                log.log(Level.SEVERE, "Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
        }
        else if (state instanceof Object[])
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Exiting deserializeView - state not serialized.");
            }

            return state;
        }
        else if(state == null)
        {
            log.severe("Exiting deserializeView - this method should not be called with a null-state.");
            return null;
        }
        else
        {
            log.severe("Exiting deserializeView - this method should not be called with a state of type : "
                       + state.getClass());
            return null;
        }
    }
    
    //------------------------------------- METHOD FROM StateCache ------------------------------------------------

    @Override
    public Object saveSerializedView(FacesContext facesContext, Object serializedView)
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Processing saveSerializedView - server-side state saving - save state");
        }
        //save state in server session
        saveSerializedViewInServletSession(facesContext, serializedView);
        
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Exiting saveSerializedView - server-side state saving - saved state");
        }
        
        return encodeSerializedState(facesContext, serializedView);
    }

    @Override
    public Object restoreSerializedView(FacesContext facesContext, String viewId, Object viewState)
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Restoring view from session");
        }

        Object serverStateId = getServerStateId(facesContext, viewState);

        return (serverStateId == null)
                ? null
                : getSerializedViewFromServletSession(facesContext, viewId, serverStateId);
    }

    @Override
    public Object encodeSerializedState(FacesContext facesContext, Object serializedView)
    {
        return sessionViewStorageFactory.getKeyFactory().encode(getNextViewSequence(facesContext));
    }
    
    @Override
    public boolean isWriteStateAfterRenderViewRequired(FacesContext facesContext)
    {
        return false;
    }

    @Override
    public String createCryptographicallyStrongTokenFromSession(FacesContext context)
    {
        return csrfSessionTokenFactory.createCryptographicallyStrongTokenFromSession(context);
    }
    
    @Override
    public StateTokenProcessor getStateTokenProcessor(FacesContext context)
    {
        return stateTokenProcessor;
    }

    // SPI used by OmniFaces
    protected SessionViewStorageFactory getSessionViewStorageFactory()
    {
        return sessionViewStorageFactory;
    }
}
