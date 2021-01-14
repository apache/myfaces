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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.myfaces.application.StateCache;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.util.MyFacesObjectInputStream;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

class ServerSideStateCacheImpl extends StateCache<Object, Object>
{
    private static final Logger log = Logger.getLogger(ServerSideStateCacheImpl.class.getName());
    
    public static final String SERIALIZED_VIEW_SESSION_ATTR= 
        ServerSideStateCacheImpl.class.getName() + ".SERIALIZED_VIEW";
    
    public static final String RESTORED_SERIALIZED_VIEW_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW";

    public static final String RESTORED_VIEW_KEY_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_VIEW_KEY";
    
    /**
     * Defines the amount (default = 20) of the latest views are stored in session.
     * 
     * <p>Only applicable if state saving method is "server" (= default).
     * </p>
     * 
     */
    @JSFWebConfigParam(defaultValue="20",since="1.1", classType="java.lang.Integer", group="state", tags="performance")
    public static final String NUMBER_OF_VIEWS_IN_SESSION_PARAM = "org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION";

    /**
     * Indicates the amount of views (default is not active) that should be stored in session between sequential
     * POST or POST-REDIRECT-GET if org.apache.myfaces.USE_FLASH_SCOPE_PURGE_VIEWS_IN_SESSION is true.
     * 
     * <p>Only applicable if state saving method is "server" (= default). For example, if this param has value = 2 and 
     * in your custom webapp there is a form that is clicked 3 times, only 2 views
     * will be stored and the third one (the one stored the first time) will be
     * removed from session, even if the view can
     * store more sessions org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION.
     * This feature becomes useful for multi-window applications.
     * where without this feature a window can swallow all view slots so
     * the other ones will throw ViewExpiredException.</p>
     */
    @JSFWebConfigParam(since="2.0.6", classType="java.lang.Integer", group="state", tags="performance")
    public static final String NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM
            = "org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION";
    
    /**
     * Default value for <code>org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION</code> context parameter.
     */
    public static final int DEFAULT_NUMBER_OF_VIEWS_IN_SESSION = 20;

    /**
     * Indicate if the state should be serialized before save it on the session. 
     * <p>
     * Only applicable if state saving method is "server" (= default).
     * If <code>true</code> (default) the state will be serialized to a byte stream before it is written to the session.
     * If <code>false</code> the state will not be serialized to a byte stream.
     * </p>
     */
    @JSFWebConfigParam(defaultValue="true",since="1.1", expectedValues="true,false", group="state", tags="performance")
    public static final String SERIALIZE_STATE_IN_SESSION_PARAM = "org.apache.myfaces.SERIALIZE_STATE_IN_SESSION";

    /**
     * Indicates that the serialized state will be compressed before it is written to the session. By default true.
     * 
     * Only applicable if state saving method is "server" (= default) and if
     * <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> is <code>true</code> (= default).
     * If <code>true</code> (default) the serialized state will be compressed before it is written to the session.
     * If <code>false</code> the state will not be compressed.
     */
    @JSFWebConfigParam(defaultValue="true",since="1.1", expectedValues="true,false", group="state", tags="performance")
    public static final String COMPRESS_SERVER_STATE_PARAM = "org.apache.myfaces.COMPRESS_STATE_IN_SESSION";

    /**
     * Default value for <code>org.apache.myfaces.COMPRESS_STATE_IN_SESSION</code> context parameter.
     */
    public static final boolean DEFAULT_COMPRESS_SERVER_STATE_PARAM = true;

    /**
     * Default value for <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> context parameter.
     */
    public static final boolean DEFAULT_SERIALIZE_STATE_IN_SESSION = true;

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
    @JSFWebConfigParam(defaultValue="off", expectedValues="off, no, hard-soft, soft, soft-weak, weak",
                       since="1.2.5", group="state", tags="performance")
    public static final String CACHE_OLD_VIEWS_IN_SESSION_MODE = "org.apache.myfaces.CACHE_OLD_VIEWS_IN_SESSION_MODE";
    
    /**
     * This option uses an hard-soft ReferenceMap, but it could cause a 
     * memory leak, because the keys are not removed by any method
     * (MYFACES-1660). So use with caution.
     */
    public static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_HARD_SOFT = "hard-soft";
    
    public static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT = "soft";
    
    public static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_SOFT_WEAK = "soft-weak";
    
    public static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_WEAK = "weak";
    
    public static final String CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF = "off";

    /**
     * Allow use flash scope to keep track of the views used in session and the previous ones,
     * so server side state saving can delete old views even if POST-REDIRECT-GET pattern is used.
     * 
     * <p>
     * Only applicable if state saving method is "server" (= default).
     * The default value is false.</p>
     */
    @JSFWebConfigParam(since="2.0.6", defaultValue="false", expectedValues="true, false", group="state")
    public static final String USE_FLASH_SCOPE_PURGE_VIEWS_IN_SESSION
            = "org.apache.myfaces.USE_FLASH_SCOPE_PURGE_VIEWS_IN_SESSION";

    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_NONE = "none";
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM = "secureRandom";
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_RANDOM = "random";
    
    /**
     * Adds a random key to the generated view state session token.
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", expectedValues="secureRandom, random, none",
            defaultValue="secureRandom", group="state")
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN";
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM_DEFAULT = 
            RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM;

    /**
     * Set the default length of the random key added to the view state session token.
     * By default is 8. 
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", defaultValue="8", group="state")
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM 
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH";
    public static final int RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM_DEFAULT = 8;

    /**
     * Sets the random class to initialize the secure random id generator. 
     * By default it uses java.security.SecureRandom
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", group="state")
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_CLASS_PARAM
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_CLASS";
    
    /**
     * Sets the random provider to initialize the secure random id generator.
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", group="state")
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_PROVIDER_PARAM
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_PROVIDER";
    
    /**
     * Sets the random algorithm to initialize the secure random id generator. 
     * By default is SHA1PRNG
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", defaultValue="SHA1PRNG", group="state")
    public static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_ALGORITM_PARAM 
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_ALGORITM";
    
    
    public static final int UNCOMPRESSED_FLAG = 0;
    public static final int COMPRESSED_FLAG = 1;

    private Boolean _useFlashScopePurgeViewsInSession = null;
    
    private Integer _numberOfSequentialViewsInSession = null;
    private boolean _numberOfSequentialViewsInSessionSet = false;

    private SessionViewStorageFactory sessionViewStorageFactory;

    public ServerSideStateCacheImpl()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String randomMode = WebConfigParamUtils.getStringInitParameter(facesContext.getExternalContext(),
                RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM, 
                RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM_DEFAULT);
        if (RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_RANDOM.equals(randomMode))
        {
            sessionViewStorageFactory = new RandomSessionViewStorageFactory(
                    new RandomKeyFactory(facesContext));
        }
        else if (RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_NONE.equals(randomMode))
        {
            sessionViewStorageFactory = new CounterSessionViewStorageFactory(new CounterKeyFactory());
        }
        else
        {
            sessionViewStorageFactory = new RandomSessionViewStorageFactory(
                    new SecureRandomKeyFactory(facesContext));
        }
    }
    
    //------------------------------------- METHODS COPIED FROM JspStateManagerImpl--------------------------------

    protected Object getServerStateId(FacesContext facesContext, Object state)
    {
      if (state != null)
      {
          return getKeyFactory(facesContext).decode(state);
      }
      return null;
    }

    protected void saveSerializedViewInServletSession(FacesContext context,
                                                      Object serializedView)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        SerializedViewCollection viewCollection = (SerializedViewCollection) sessionMap
                .get(SERIALIZED_VIEW_SESSION_ATTR);
        if (viewCollection == null)
        {
            viewCollection = getSessionViewStorageFactory().createSerializedViewCollection(context);
            sessionMap.put(SERIALIZED_VIEW_SESSION_ATTR, viewCollection);
        }

        Map<Object,Object> attributeMap = context.getAttributes();
        
        SerializedViewKey key = null;
        if (getNumberOfSequentialViewsInSession(context.getExternalContext()) != null &&
            getNumberOfSequentialViewsInSession(context.getExternalContext()) > 0)
        {
            key = (SerializedViewKey) attributeMap.get(RESTORED_VIEW_KEY_REQUEST_ATTR);
            
            if (key == null )
            {
                if (isUseFlashScopePurgeViewsInSession(context.getExternalContext()) && 
                    Boolean.TRUE.equals(context.getExternalContext().getRequestMap()
                            .get("oam.Flash.REDIRECT.PREVIOUSREQUEST")))
                {
                    key = (SerializedViewKey)
                            context.getExternalContext().getFlash().get(RESTORED_VIEW_KEY_REQUEST_ATTR);
                }
            }
        }
        
        SerializedViewKey nextKey = getSessionViewStorageFactory().createSerializedViewKey(
                context, context.getViewRoot().getViewId(), getNextViewSequence(context));
        viewCollection.add(context, serializeView(context, serializedView), nextKey, key);

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
                            getSessionViewStorageFactory().createSerializedViewKey(
                            context, viewId, sequence));
                    if (state != null)
                    {
                        serializedView = deserializeView(state);
                    }
                }
            }
            attributeMap.put(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR, serializedView);
            
            if (getNumberOfSequentialViewsInSession(externalContext) != null &&
                getNumberOfSequentialViewsInSession(externalContext) > 0)
            {
                SerializedViewKey key = getSessionViewStorageFactory().
                        createSerializedViewKey(context, viewId, sequence);
                attributeMap.put(RESTORED_VIEW_KEY_REQUEST_ATTR, key);
                
                if (isUseFlashScopePurgeViewsInSession(externalContext))
                {
                    externalContext.getFlash().put(RESTORED_VIEW_KEY_REQUEST_ATTR, key);
                    externalContext.getFlash().keep(RESTORED_VIEW_KEY_REQUEST_ATTR);
                }
            }

            nextViewSequence(context);
        }
        return serializedView;
    }

    public Object getNextViewSequence(FacesContext context)
    {
        Object sequence = context.getAttributes().get(RendererUtils.SEQUENCE_PARAM);
        if (sequence == null)
        {
            sequence = nextViewSequence(context);
            context.getAttributes().put(RendererUtils.SEQUENCE_PARAM, sequence);
        }
        return sequence;
    }

    public Object nextViewSequence(FacesContext facescontext)
    {
        Object sequence = getKeyFactory(facescontext).generateKey(facescontext);
        facescontext.getAttributes().put(RendererUtils.SEQUENCE_PARAM, sequence);
        return sequence;
    }

    protected Object serializeView(FacesContext context, Object serializedView)
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Entering serializeView");
        }

        if(isSerializeStateInSession(context))
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Processing serializeView - serialize state in session");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try
            {
                OutputStream os = baos;
                if(isCompressStateInSession(context))
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

                //Object[] stateArray = (Object[]) serializedView;

                ObjectOutputStream out = new ObjectOutputStream(os);
                
                out.writeObject(serializedView);
                //out.writeObject(stateArray[0]);
                //out.writeObject(stateArray[1]);
                out.close();
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

    /**
     * Reads the value of the <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> context parameter.
     * @see #SERIALIZE_STATE_IN_SESSION_PARAM
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
     * @see #COMPRESS_SERVER_STATE_PARAM
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
                        object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() 
                        {
                            public Object run() throws PrivilegedActionException, IOException, ClassNotFoundException
                            {
                                //return new Object[] {in.readObject(), in.readObject()};
                                return in.readObject();
                            }
                        });
                    }
                    else
                    {
                        //object = new Object[] {in.readObject(), in.readObject()};
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

    public Object encodeSerializedState(FacesContext facesContext, Object serializedView)
    {
        return getKeyFactory(facesContext).encode(getNextViewSequence(facesContext));
    }
    
    @Override
    public boolean isWriteStateAfterRenderViewRequired(FacesContext facesContext)
    {
        return false;
    }

    //------------------------------------- Custom methods -----------------------------------------------------
    
    private boolean isUseFlashScopePurgeViewsInSession(ExternalContext externalContext)
    {
        if (_useFlashScopePurgeViewsInSession == null)
        {
            _useFlashScopePurgeViewsInSession = WebConfigParamUtils.getBooleanInitParameter(
                    externalContext, USE_FLASH_SCOPE_PURGE_VIEWS_IN_SESSION, false);
        }
        return _useFlashScopePurgeViewsInSession;
    }
    
    private Integer getNumberOfSequentialViewsInSession(ExternalContext externalContext)
    {
        if (!_numberOfSequentialViewsInSessionSet)
        {
            _numberOfSequentialViewsInSession = WebConfigParamUtils.getIntegerInitParameter(
                    externalContext, 
                    NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM);
            _numberOfSequentialViewsInSessionSet = true;
        }
        return _numberOfSequentialViewsInSession;
    }
    
    protected KeyFactory getKeyFactory(FacesContext facesContext)
    {
        //return keyFactory;
        return sessionViewStorageFactory.getKeyFactory();
    }
    
    protected SessionViewStorageFactory getSessionViewStorageFactory()
    {
        return sessionViewStorageFactory;
    }
}
