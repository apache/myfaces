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
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.faces.application.ProjectStage;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.myfaces.application.StateCache;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.util.MyFacesObjectInputStream;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

class ServerSideStateCacheImpl extends StateCache<Object, Object>
{
    private static final Logger log = Logger.getLogger(ServerSideStateCacheImpl.class.getName());
    
    private static final String SERIALIZED_VIEW_SESSION_ATTR= 
        ServerSideStateCacheImpl.class.getName() + ".SERIALIZED_VIEW";
    
    private static final String RESTORED_SERIALIZED_VIEW_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW";

    private static final String RESTORED_VIEW_KEY_REQUEST_ATTR = 
        ServerSideStateCacheImpl.class.getName() + ".RESTORED_VIEW_KEY";
    
    /**
     * Defines the amount (default = 20) of the latest views are stored in session.
     * 
     * <p>Only applicable if state saving method is "server" (= default).
     * </p>
     * 
     */
    @JSFWebConfigParam(defaultValue="20",since="1.1", classType="java.lang.Integer", group="state", tags="performance")
    private static final String NUMBER_OF_VIEWS_IN_SESSION_PARAM = "org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION";

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
    private static final String NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM
            = "org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION";
    
    /**
     * Default value for <code>org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION</code> context parameter.
     */
    private static final int DEFAULT_NUMBER_OF_VIEWS_IN_SESSION = 20;

    /**
     * Indicate if the state should be serialized before save it on the session. 
     * <p>
     * Only applicable if state saving method is "server" (= default).
     * If <code>true</code> (default) the state will be serialized to a byte stream before it is written to the session.
     * If <code>false</code> the state will not be serialized to a byte stream.
     * </p>
     */
    @JSFWebConfigParam(defaultValue="true",since="1.1", expectedValues="true,false", group="state", tags="performance")
    private static final String SERIALIZE_STATE_IN_SESSION_PARAM = "org.apache.myfaces.SERIALIZE_STATE_IN_SESSION";

    /**
     * Indicates that the serialized state will be compressed before it is written to the session. By default true.
     * 
     * Only applicable if state saving method is "server" (= default) and if
     * <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> is <code>true</code> (= default).
     * If <code>true</code> (default) the serialized state will be compressed before it is written to the session.
     * If <code>false</code> the state will not be compressed.
     */
    @JSFWebConfigParam(defaultValue="true",since="1.1", expectedValues="true,false", group="state", tags="performance")
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
    @JSFWebConfigParam(defaultValue="off", expectedValues="off, no, hard-soft, soft, soft-weak, weak",
                       since="1.2.5", group="state", tags="performance")
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

    /**
     * Allow use flash scope to keep track of the views used in session and the previous ones,
     * so server side state saving can delete old views even if POST-REDIRECT-GET pattern is used.
     * 
     * <p>
     * Only applicable if state saving method is "server" (= default).
     * The default value is false.</p>
     */
    @JSFWebConfigParam(since="2.0.6", defaultValue="false", expectedValues="true, false", group="state")
    private static final String USE_FLASH_SCOPE_PURGE_VIEWS_IN_SESSION
            = "org.apache.myfaces.USE_FLASH_SCOPE_PURGE_VIEWS_IN_SESSION";

    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_NONE = "none";
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM = "secureRandom";
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_RANDOM = "random";
    
    /**
     * Adds a random key to the generated view state session token.
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", expectedValues="secureRandom, random, none", 
            defaultValue="none", group="state")
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN";
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM_DEFAULT = 
            RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_NONE;

    /**
     * Set the default length of the random key added to the view state session token.
     * By default is 8. 
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", defaultValue="8", group="state")
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM 
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH";
    private static final int RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM_DEFAULT = 8;

    /**
     * Sets the random class to initialize the secure random id generator. 
     * By default it uses java.security.SecureRandom
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", group="state")
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_CLASS_PARAM
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_CLASS";
    
    /**
     * Sets the random provider to initialize the secure random id generator.
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", group="state")
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_PROVIDER_PARAM
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_PROVIDER";
    
    /**
     * Sets the random algorithm to initialize the secure random id generator. 
     * By default is SHA1PRNG
     */
    @JSFWebConfigParam(since="2.1.9, 2.0.15", defaultValue="SHA1PRNG", group="state")
    private static final String RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_ALGORITM_PARAM 
            = "org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_ALGORITM";
    
    
    private static final int UNCOMPRESSED_FLAG = 0;
    private static final int COMPRESSED_FLAG = 1;
    
    private static final Object[] EMPTY_STATES = new Object[]{null, null};

    //private static final int JSF_SEQUENCE_INDEX = 0;
    
    private Boolean _useFlashScopePurgeViewsInSession = null;
    
    private Integer _numberOfSequentialViewsInSession = null;
    private boolean _numberOfSequentialViewsInSessionSet = false;

    //private final KeyFactory keyFactory;
    private SessionViewStorageFactory sessionViewStorageFactory;

    public ServerSideStateCacheImpl()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String randomMode = WebConfigParamUtils.getStringInitParameter(facesContext.getExternalContext(),
                RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM, 
                RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_PARAM_DEFAULT);
        if (RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM.equals(randomMode))
        {
            //keyFactory = new SecureRandomKeyFactory(facesContext);
            sessionViewStorageFactory = new RandomSessionViewStorageFactory(
                    new SecureRandomKeyFactory(facesContext));
        }
        else if (RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_RANDOM.equals(randomMode))
        {
            //keyFactory = new RandomKeyFactory(facesContext);
            sessionViewStorageFactory = new RandomSessionViewStorageFactory(
                    new RandomKeyFactory(facesContext));
        }
        else
        {
            //keyFactory = new CounterKeyFactory();
            sessionViewStorageFactory = new CounterSessionViewStorageFactory(new CounterKeyFactory());
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
    
    protected static class SerializedViewCollection implements Serializable
    {
        private static final long serialVersionUID = -3734849062185115847L;

        private final List<SerializedViewKey> _keys
                = new ArrayList<SerializedViewKey>(DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
        private final Map<SerializedViewKey, Object> _serializedViews = new HashMap<SerializedViewKey, Object>();
        
        private final Map<SerializedViewKey, SerializedViewKey> _precedence = 
            new HashMap<SerializedViewKey, SerializedViewKey>();

        // old views will be hold as soft references which will be removed by
        // the garbage collector if free memory is low
        private transient Map<Object, Object> _oldSerializedViews = null;

        public synchronized void add(FacesContext context, Object state, SerializedViewKey key,
                                     SerializedViewKey previousRestoredKey)
        {
            if (state == null)
            {
                state = EMPTY_STATES;
            }
            else if (state instanceof Object[] &&
                ((Object[])state).length == 2 &&
                ((Object[])state)[0] == null &&
                ((Object[])state)[1] == null)
            {
                // The generated state can be considered zero, set it as null
                // into the map.
                state = null;
            }
            
            Integer maxCount = getNumberOfSequentialViewsInSession(context);
            if (maxCount != null)
            {
                if (previousRestoredKey != null)
                {
                    if (!_serializedViews.isEmpty())
                    {
                        _precedence.put((SerializedViewKey) key, previousRestoredKey);
                    }
                    else
                    {
                        // Note when the session is invalidated, _serializedViews map is empty,
                        // but we could have a not null previousRestoredKey (the last one before
                        // invalidate the session), so we need to check that condition before
                        // set the precence. In that way, we ensure the precedence map will always
                        // have valid keys.
                        previousRestoredKey = null;
                    }
                }
            }
            _serializedViews.put(key, state);

            while (_keys.remove(key))
            {
                // do nothing
            }
            _keys.add(key);

            if (previousRestoredKey != null && maxCount != null && maxCount > 0)
            {
                int count = 0;
                SerializedViewKey previousKey = (SerializedViewKey) key;
                do
                {
                  previousKey = _precedence.get(previousKey);
                  count++;
                } while (previousKey != null && count < maxCount);
                
                if (previousKey != null)
                {
                    SerializedViewKey keyToRemove = (SerializedViewKey) previousKey;
                    // In theory it should be only one key but just to be sure
                    // do it in a loop, but in this case if cache old views is on,
                    // put on that map.
                    do
                    {
                        while (_keys.remove(keyToRemove))
                        {
                            // do nothing
                        }

                        if (_serializedViews.containsKey(keyToRemove) &&
                                !CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF.equals(getCacheOldViewsInSessionMode(context)))
                        {
                            getOldSerializedViewsMap().put(keyToRemove, _serializedViews.remove(keyToRemove));
                        }
                        else
                        {
                            _serializedViews.remove(keyToRemove);
                        }
                    
                        keyToRemove = _precedence.remove(keyToRemove);
                    }  while(keyToRemove != null);
                }
            }

            int views = getNumberOfViewsInSession(context);
            while (_keys.size() > views)
            {
                key = _keys.remove(0);
                
                if (maxCount != null && maxCount > 0)
                {
                    SerializedViewKey keyToRemove = (SerializedViewKey) key;
                    // Note in this case the key to delete is the oldest one, 
                    // so it could be at least one precedence, but to be safe
                    // do it with a loop.
                    do
                    {
                        keyToRemove = _precedence.remove(keyToRemove);
                    } while (keyToRemove != null);
                }

                if (_serializedViews.containsKey(key) &&
                    !CACHE_OLD_VIEWS_IN_SESSION_MODE_OFF.equals(getCacheOldViewsInSessionMode(context)))
                {
                    
                    getOldSerializedViewsMap().put(key, _serializedViews.remove(key));
                }
                else
                {
                    _serializedViews.remove(key);
                }
            }
        }

        protected Integer getNumberOfSequentialViewsInSession(FacesContext context)
        {
            return WebConfigParamUtils.getIntegerInitParameter(context.getExternalContext(), 
                    NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM);
        }
        
        /**
         * Reads the amount (default = 20) of views to be stored in session.
         * @see #NUMBER_OF_VIEWS_IN_SESSION_PARAM
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
         * @see #CACHE_OLD_VIEWS_IN_SESSION_MODE
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
        
        public Object get(SerializedViewKey key)
        {
            Object value = _serializedViews.get(key);
            if (value == null)
            {
                if (_serializedViews.containsKey(key))
                {
                    return EMPTY_STATES;
                }
                Map<Object,Object> oldSerializedViewMap = getOldSerializedViewsMap();
                if (oldSerializedViewMap != null)
                {
                    value = oldSerializedViewMap.get(key);
                    if (value == null && oldSerializedViewMap.containsKey(key) )
                    {
                        return EMPTY_STATES;
                    }
                }
            }
            else if (value instanceof Object[] &&
                ((Object[])value).length == 2 &&
                ((Object[])value)[0] == null &&
                ((Object[])value)[1] == null)
            {
                // Remember inside the state map null is stored as an empty array.
                return null;
            }
            return value;
        }
    }

    /**
     * Base implementation where all keys used to identify the state of a view should
     * extend.
     */
    protected abstract static class SerializedViewKey implements Serializable
    {
    }

    /**
     * Implementation of SerializedViewKey, where the hashCode of the viewId is used
     * and the sequenceId is a numeric value.
     */
    private static class IntIntSerializedViewKey extends SerializedViewKey 
        implements Serializable
    {
        private static final long serialVersionUID = -1170697124386063642L;
        
        private final int _viewId;
        private final int _sequenceId;
        
        public IntIntSerializedViewKey(int viewId, int sequence)
        {
            _sequenceId = sequence;
            _viewId = viewId;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final IntIntSerializedViewKey other = (IntIntSerializedViewKey) obj;
            if (this._viewId != other._viewId)
            {
                return false;
            }
            if (this._sequenceId != other._sequenceId)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 83 * hash + this._viewId;
            hash = 83 * hash + this._sequenceId;
            return hash;
        }
    }

    /**
     * Implementation of SerializedViewKey, where the hashCode of the viewId is used
     * and the sequenceId is a string value.
     */
    private static class IntByteArraySerializedViewKey extends SerializedViewKey
        implements Serializable
    {
        private final int _viewId;
        private final byte[] _sequenceId;
        
        public IntByteArraySerializedViewKey(int viewId, byte[] sequence)
        {
            _sequenceId = sequence;
            _viewId = viewId;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final IntByteArraySerializedViewKey other = (IntByteArraySerializedViewKey) obj;
            if (this._viewId != other._viewId)
            {
                return false;
            }
            if (!Arrays.equals(this._sequenceId, other._sequenceId))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 37 * hash + this._viewId;
            hash = 37 * hash + Arrays.hashCode(this._sequenceId);
            return hash;
        }
    }
    
    
    /**
     * Implementation of SerializedViewKey, where the viewId and the sequenceId can be
     * anything.
     */
    private static class ReferenceSerializedViewKey<I,K> extends SerializedViewKey
        implements Serializable
    {
        private static final long serialVersionUID = -1170697124386063642L;

        private final I _viewId;
        private final K _sequenceId;

        public ReferenceSerializedViewKey()
        {
            _sequenceId = null;
            _viewId = null;
        }
        public ReferenceSerializedViewKey(I viewId, K sequence)
        {
            _sequenceId = sequence;
            _viewId = viewId;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final ReferenceSerializedViewKey<I, K> other = (ReferenceSerializedViewKey<I, K>) obj;
            if (this._viewId != other._viewId && (this._viewId == null || !this._viewId.equals(other._viewId)))
            {
                return false;
            }
            if (this._sequenceId != other._sequenceId && 
                (this._sequenceId == null || !this._sequenceId.equals(other._sequenceId)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 83 * hash + (this._viewId != null ? this._viewId.hashCode() : 0);
            hash = 83 * hash + (this._sequenceId != null ? this._sequenceId.hashCode() : 0);
            return hash;
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
            

    protected abstract static class KeyFactory<K, V>
    {
        
        /**
         * Generates a unique key per session 
         * 
         * @param facesContext
         * @return 
         */
        public abstract K generateKey(FacesContext facesContext);
        
        /**
         * Encode a Key into a value that will be used as view state session token
         * 
         * @param key
         * @return 
         */
        public abstract V encode(K key);

        /**
         * Decode a view state session token into a key
         * 
         * @param value
         * @return 
         */
        public abstract K decode(V value);

    }

    private static class CounterKeyFactory extends KeyFactory<Integer, String>
    {
        /**
         * Take the counter from session scope and increment
         * 
         * @param facesContext
         * @return 
         */
        @Override
        public Integer generateKey(FacesContext facesContext)
        {
            ExternalContext externalContext = facesContext.getExternalContext();
            Object sessionObj = externalContext.getSession(true);
            Integer sequence = null;
            synchronized(sessionObj) // synchronized to increase sequence if multiple requests
                                    // are handled at the same time for the session
            {
                Map<String, Object> map = externalContext.getSessionMap();
                sequence = (Integer) map.get(RendererUtils.SEQUENCE_PARAM);
                if(sequence == null || sequence.intValue() == Integer.MAX_VALUE)
                {
                    sequence = Integer.valueOf(1);
                }
                else
                {
                    sequence = Integer.valueOf(sequence.intValue() + 1);
                }
                map.put(RendererUtils.SEQUENCE_PARAM, sequence);
            }
            return sequence;
        }
        
        public String encode(Integer sequence)
        {
            return Integer.toString(sequence, Character.MAX_RADIX);
        }
                
        public Integer decode(String serverStateId)
        {
             return Integer.valueOf((String) serverStateId, Character.MAX_RADIX);
        }
    }
    
    /**
     * This factory generate a key composed by a counter and a random number. The
     * counter ensures uniqueness, and the random number prevents guess the next
     * session token.
     */
    private static class SecureRandomKeyFactory extends KeyFactory<byte[], String>
    {
        private final SessionIdGenerator sessionIdGenerator;
        private final int length;

        public SecureRandomKeyFactory(FacesContext facesContext)
        {
            length = WebConfigParamUtils.getIntegerInitParameter(facesContext.getExternalContext(),
                    RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM, 
                    RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM_DEFAULT);
            sessionIdGenerator = new SessionIdGenerator();
            sessionIdGenerator.setSessionIdLength(length);
            String secureRandomClass = WebConfigParamUtils.getStringInitParameter(
                    facesContext.getExternalContext(),
                    RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_CLASS_PARAM);
            if (secureRandomClass != null)
            {
                sessionIdGenerator.setSecureRandomClass(secureRandomClass);
            }
            String secureRandomProvider = WebConfigParamUtils.getStringInitParameter(
                    facesContext.getExternalContext(),
                    RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_PROVIDER_PARAM);
            if (secureRandomProvider != null)
            {
                sessionIdGenerator.setSecureRandomProvider(secureRandomProvider);
            }
            String secureRandomAlgorithm = WebConfigParamUtils.getStringInitParameter(
                    facesContext.getExternalContext(), 
                    RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_SECURE_RANDOM_ALGORITM_PARAM);
            if (secureRandomAlgorithm != null)
            {
                sessionIdGenerator.setSecureRandomAlgorithm(secureRandomAlgorithm);
            }
        }
        
        public Integer generateCounterKey(FacesContext facesContext)
        {
            ExternalContext externalContext = facesContext.getExternalContext();
            Object sessionObj = externalContext.getSession(true);
            Integer sequence = null;
            synchronized(sessionObj) // synchronized to increase sequence if multiple requests
                                    // are handled at the same time for the session
            {
                Map<String, Object> map = externalContext.getSessionMap();
                sequence = (Integer) map.get(RendererUtils.SEQUENCE_PARAM);
                if(sequence == null || sequence.intValue() == Integer.MAX_VALUE)
                {
                    sequence = Integer.valueOf(1);
                }
                else
                {
                    sequence = Integer.valueOf(sequence.intValue() + 1);
                }
                map.put(RendererUtils.SEQUENCE_PARAM, sequence);
            }
            return sequence;
        }

        @Override
        public byte[] generateKey(FacesContext facesContext)
        {
            byte[] array = new byte[length];
            byte[] key = new byte[length+4];
            
            sessionIdGenerator.getRandomBytes(array);
            for (int i = 0; i < array.length; i++)
            {
                key[i] = array[i];
            }
            int value = generateCounterKey(facesContext);
            key[array.length] =  (byte) (value >>> 24);
            key[array.length+1] =  (byte) (value >>> 16);
            key[array.length+2] =  (byte) (value >>> 8);
            key[array.length+3] =  (byte) (value);
            
            return key;
        }

        @Override
        public String encode(byte[] key)
        {
            return new String(Hex.encodeHex(key));
        }
        
        @Override
        public byte[] decode(String value)
        {
            try
            {
                return Hex.decodeHex(value.toCharArray());
            }
            catch (DecoderException ex)
            {
                // Cannot decode, ignore silently, later it will be handled as
                // ViewExpiredException
            }
            return null;
        }
    }
    
    private static class RandomKeyFactory extends KeyFactory<byte[], String>
    {
        private final Random random;
        private final int length;
        
        public RandomKeyFactory(FacesContext facesContext)
        {
            length = WebConfigParamUtils.getIntegerInitParameter(facesContext.getExternalContext(),
                    RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM, 
                    RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN_LENGTH_PARAM_DEFAULT);
            random = new Random(((int)System.nanoTime())+this.hashCode());
        }
        
        public Integer generateCounterKey(FacesContext facesContext)
        {
            ExternalContext externalContext = facesContext.getExternalContext();
            Object sessionObj = externalContext.getSession(true);
            Integer sequence = null;
            synchronized(sessionObj) // synchronized to increase sequence if multiple requests
                                    // are handled at the same time for the session
            {
                Map<String, Object> map = externalContext.getSessionMap();
                sequence = (Integer) map.get(RendererUtils.SEQUENCE_PARAM);
                if(sequence == null || sequence.intValue() == Integer.MAX_VALUE)
                {
                    sequence = Integer.valueOf(1);
                }
                else
                {
                    sequence = Integer.valueOf(sequence.intValue() + 1);
                }
                map.put(RendererUtils.SEQUENCE_PARAM, sequence);
            }
            return sequence;
        }

        @Override
        public byte[] generateKey(FacesContext facesContext)
        {
            byte[] array = new byte[length];
            byte[] key = new byte[length+4];
            
            //sessionIdGenerator.getRandomBytes(array);
            random.nextBytes(array);
            for (int i = 0; i < array.length; i++)
            {
                key[i] = array[i];
            }
            int value = generateCounterKey(facesContext);
            key[array.length] =  (byte) (value >>> 24);
            key[array.length+1] =  (byte) (value >>> 16);
            key[array.length+2] =  (byte) (value >>> 8);
            key[array.length+3] =  (byte) (value);

            return key;
        }

        @Override
        public String encode(byte[] key)
        {
            return new String(Hex.encodeHex(key));
        }
        
        @Override
        public byte[] decode(String value)
        {
            try
            {
                return Hex.decodeHex(value.toCharArray());
            }
            catch (DecoderException ex)
            {
                // Cannot decode, ignore silently, later it will be handled as
                // ViewExpiredException
            }
            return null;
        }
    }
    
    /**
     * 
     * @param <T>
     * @param <K>
     * @param <V> 
     */
    protected abstract static class SessionViewStorageFactory <T extends KeyFactory<K,V>, K, V >
    {
        private KeyFactory<K, V> keyFactory;
        
        public SessionViewStorageFactory(KeyFactory<K, V> keyFactory)
        {
            this.keyFactory = keyFactory;
        }
        
        public KeyFactory<K, V> getKeyFactory()
        {
            return keyFactory;
        }
        
        public abstract SerializedViewCollection createSerializedViewCollection(
                FacesContext context);
        
        public abstract SerializedViewKey createSerializedViewKey(
                FacesContext facesContext, String viewId, K key);
        
    }
    
    private static class CounterSessionViewStorageFactory 
        extends SessionViewStorageFactory <KeyFactory <Integer,String>, Integer, String>
    {
        public CounterSessionViewStorageFactory(KeyFactory<Integer, String> keyFactory)
        {
            super(keyFactory);
        }

        @Override
        public SerializedViewCollection createSerializedViewCollection(
                FacesContext context)
        {
            return new SerializedViewCollection();
        }

        @Override
        public SerializedViewKey createSerializedViewKey(
                FacesContext context, String viewId, Integer key)
        {
            if (context.isProjectStage(ProjectStage.Production))
            {
                return new IntIntSerializedViewKey(viewId == null ? 0 : viewId.hashCode(), key);
            }
            else
            {
                return new ReferenceSerializedViewKey(viewId, key);
            }
        }
    }
    
    private static class RandomSessionViewStorageFactory
        extends SessionViewStorageFactory <KeyFactory <byte[],String>, byte[], String>
    {
        public RandomSessionViewStorageFactory(KeyFactory<byte[], String> keyFactory)
        {
            super(keyFactory);
        }

        @Override
        public SerializedViewCollection createSerializedViewCollection(
                FacesContext context)
        {
            return new SerializedViewCollection();
        }

        @Override
        public SerializedViewKey createSerializedViewKey(
                FacesContext context, String viewId, byte[] key)
        {
            if (context.isProjectStage(ProjectStage.Production))
            {
                return new IntByteArraySerializedViewKey(viewId == null ? 0 : viewId.hashCode(), key);
            }
            else
            {
                return new ReferenceSerializedViewKey(viewId, key);
            }
        }
    }
}
