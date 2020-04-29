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

package org.apache.myfaces.push.cdi;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import jakarta.faces.context.ExternalContext;
import jakarta.websocket.Session;
import org.apache.myfaces.push.WebsocketSessionClusterSerializedRestore;
import org.apache.myfaces.push.util.Json;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.shared.util.ConcurrentLRUCache;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

/**
 *
 */
public final class WebsocketApplicationSessionHolder
{
    
    public static final String INIT_PARAM_WEBSOCKET_MAX_CONNECTIONS = "org.apache.myfaces.WEBSOCKET_MAX_CONNECTIONS";
    
    public static final Integer INIT_PARAM_WEBSOCKET_MAX_CONNECTIONS_DEFAULT = 5000;
    
    private volatile static WeakHashMap<ClassLoader, ConcurrentLRUCache<String, Reference<Session>>> 
            clWebsocketMap = new WeakHashMap<ClassLoader, ConcurrentLRUCache<String, Reference<Session>>>();
    
    private volatile static WeakHashMap<ClassLoader, Queue<String>> clWebsocketRestoredQueue =
            new WeakHashMap<ClassLoader, Queue<String>>();
    
    /**
     * 
     * @return 
     */
    public static ConcurrentLRUCache<String, Reference<Session>> getWebsocketSessionLRUCache()
    {
        ClassLoader cl = ClassUtils.getContextClassLoader();
        
        ConcurrentLRUCache<String, Reference<Session>> metadata = (ConcurrentLRUCache<String, Reference<Session>>)
                WebsocketApplicationSessionHolder.clWebsocketMap.get(cl);

        if (metadata == null)
        {
            // Ensure thread-safe put over _metadata, and only create one map
            // per classloader to hold metadata.
            synchronized (WebsocketApplicationSessionHolder.clWebsocketMap)
            {
                metadata = createWebsocketSessionLRUCache(cl, metadata, INIT_PARAM_WEBSOCKET_MAX_CONNECTIONS_DEFAULT);
            }
        }

        return metadata;
    }

    /**
     * 
     * @param context
     */
    public static void initWebsocketSessionLRUCache(ExternalContext context)
    {
        ClassLoader cl = ClassUtils.getContextClassLoader();
        
        ConcurrentLRUCache<String, Reference<Session>> lruCache = (ConcurrentLRUCache<String, Reference<Session>>)
                WebsocketApplicationSessionHolder.clWebsocketMap.get(cl);

        int size = WebConfigParamUtils.getIntegerInitParameter(context, 
                INIT_PARAM_WEBSOCKET_MAX_CONNECTIONS, INIT_PARAM_WEBSOCKET_MAX_CONNECTIONS_DEFAULT);

        ConcurrentLRUCache<String, Reference<Session>> newMetadata = 
                new ConcurrentLRUCache<String, Reference<Session>>( (size*4+3)/3, size);
        
        synchronized (WebsocketApplicationSessionHolder.clWebsocketMap)
        {
            if (lruCache == null)
            {
                WebsocketApplicationSessionHolder.clWebsocketMap.put(cl, newMetadata);
                lruCache = newMetadata;
            }
            else
            {
                // If a Session has been restored, it could be already a lruCache instantiated, so in this case
                // we need to fill the new one with the old instances, but only the instances that are active
                // at the moment.
                for (Map.Entry<String, Reference<Session>> entry : 
                        lruCache.getLatestAccessedItems(INIT_PARAM_WEBSOCKET_MAX_CONNECTIONS_DEFAULT).entrySet())
                {
                    if (entry.getValue() != null && entry.getValue().get() != null && entry.getValue().get().isOpen())
                    {
                        newMetadata.put(entry.getKey(), entry.getValue());
                    }
                }
                WebsocketApplicationSessionHolder.clWebsocketMap.put(cl, newMetadata);
                lruCache = newMetadata;
            }
        }
    }

    private static ConcurrentLRUCache<String, Reference<Session>> createWebsocketSessionLRUCache(
            ClassLoader cl, ConcurrentLRUCache<String, Reference<Session>> metadata, int size)
    {
        metadata = (ConcurrentLRUCache<String, Reference<Session>>) 
                WebsocketApplicationSessionHolder.clWebsocketMap.get(cl);
        if (metadata == null)
        {
            metadata = new ConcurrentLRUCache<String, Reference<Session>>( (size*4+3)/3, size);
            WebsocketApplicationSessionHolder.clWebsocketMap.put(cl, metadata);
        }
        return metadata;
    }
    
            

    /**
     * Removes the cached MetadataTarget instances in order to prevent a memory leak.
     */
    public static void clearWebsocketSessionLRUCache()
    {
        clWebsocketMap.remove(ClassUtils.getContextClassLoader());
        clWebsocketRestoredQueue.remove(ClassUtils.getContextClassLoader());
    }
    
    public static boolean addOrUpdateSession(String channelToken, Session session)
    {
        Reference oldInstance = getWebsocketSessionLRUCache().get(channelToken);
        if (oldInstance == null)
        {
            getWebsocketSessionLRUCache().put(channelToken, new SoftReference<Session>(session));
        }
        else if (!session.equals(oldInstance.get()))
        {
            getWebsocketSessionLRUCache().put(channelToken, new SoftReference<Session>(session));
        }
        return true;
    }

    /**
     * Remove the Session associated to the channelToken. This happens when the websocket connection is closed.
     * Please note the connection can be closed/reopened, so this method should not block another connection using
     * the same channelToken. To destroy the channel token, WebsocketViewBean is used to destroy the channel token
     * at view expiration time.
     * 
     * @param channelToken
     * @return 
     */
    public static boolean removeSession(String channelToken)
    {
        getWebsocketSessionLRUCache().remove(channelToken);
        return false;
    }
    
    
    protected static Set<Future<Void>> send(String channelToken, Object message)
    {
        // Before send, we need to check 
        synchronizeSessionInstances();
            
        Set< Future<Void> > results = new HashSet< Future<Void> >(1);
        Reference<Session> sessionRef = (channelToken != null) ? getWebsocketSessionLRUCache().get(channelToken) : null;

        if (sessionRef != null && sessionRef.get() != null)
        {
            String json = Json.encode(message);
            Session session = sessionRef.get();
            if (session.isOpen())
            {
                send(session, json, results, 0);
            }
            else
            {
                //If session is not open, remove the session, because a websocket session after is closed cannot
                //be alive.
                getWebsocketSessionLRUCache().remove(channelToken);
            }
        }
        return results;
    }

    private static final String WARNING_TOMCAT_WEB_SOCKET_BOMBED =
            "Tomcat cannot handle concurrent push messages. A push message has been sent only after %s retries."
            + " Consider rate limiting sending push messages. For example, once every 500ms.";    
    
    private static void send(Session session, String text, Set<Future<Void>> results, int retries)
    {
        try
        {
            results.add(session.getAsyncRemote().sendText(text));

            if (retries > 0)
            {
                Logger.getLogger(WebsocketApplicationSessionHolder.class.getName())
                        .warning(String.format(WARNING_TOMCAT_WEB_SOCKET_BOMBED, retries));
            }
        }
        catch (IllegalStateException e)
        {
            if (isTomcatWebSocketBombed(session, e))
            {
                synchronized (session)
                {
                    send(session, text, results, retries + 1);
                }
            }
            else
            {
                throw e;
            }
        }
    }
    
    // Tomcat related -------------------------------------------------------------------------------------------------
    /**
     * Returns true if the given WS session is from Tomcat and given illegal state exception is caused by a push bomb
     * which Tomcat couldn't handle. See also https://bz.apache.org/bugzilla/show_bug.cgi?id=56026 and
     * https://github.com/omnifaces/omnifaces/issues/234
     *
     * @param session The WS session.
     * @param illegalStateException The illegal state exception.
     * @return Whether it was Tomcat who couldn't handle the push bomb.
     * @since 2.5
     */
    private static boolean isTomcatWebSocketBombed(Session session, IllegalStateException illegalStateException)
    {
        return session.getClass().getName().startsWith("org.apache.tomcat.websocket.")
                && illegalStateException.getMessage().contains("[TEXT_FULL_WRITING]");
    }
    
    private static void synchronizeSessionInstances()
    {
        Queue<String> queue = getRestoredQueue();
        // The queue is always empty, unless a deserialization of Session instances happen. If that happens, 
        // we need to ensure all Session instances that were deserialized are on the LRU cache, so all instances
        // receive the message when a "push" is done.
        // This is not the ideal, but this is the best we have with the current websocket spec.
        if (!queue.isEmpty())
        {
            // It is necessary to have at least 1 registered Session instance to call getOpenSessions() and get all
            // instances associated to jakarta.faces.push Endpoint.
            Map<String, Reference<Session>> map = getWebsocketSessionLRUCache().getLatestAccessedItems(1);
            if (map != null && !map.isEmpty())
            {
                Reference<Session> ref = map.values().iterator().next();
                if (ref != null)
                {
                    Session s = ref.get();
                    if (s != null)
                    {
                        Set<Session> set = s.getOpenSessions();
                        
                        for (Iterator<Session> it = set.iterator(); it.hasNext();)
                        {
                            Session instance = it.next();
                            WebsocketSessionClusterSerializedRestore r = 
                                    (WebsocketSessionClusterSerializedRestore) instance.getUserProperties().get(
                                        WebsocketSessionClusterSerializedRestore.WEBSOCKET_SESSION_SERIALIZED_RESTORE);
                            if (r != null && r.isDeserialized())
                            {
                                addOrUpdateSession(r.getChannelToken(), s);
                            }
                        }
                        
                        // Remove one element from the queue
                        queue.poll();
                    }
                }
            }
        }
    }

    public static Queue<String> getRestoredQueue()
    {
        ClassLoader cl = ClassUtils.getContextClassLoader();
        
        Queue<String> metadata = (Queue<String>)
                WebsocketApplicationSessionHolder.clWebsocketRestoredQueue.get(cl);

        if (metadata == null)
        {
            // Ensure thread-safe put over _metadata, and only create one map
            // per classloader to hold metadata.
            synchronized (WebsocketApplicationSessionHolder.clWebsocketRestoredQueue)
            {
                metadata = createRestoredQueue(cl, metadata);
            }
        }

        return metadata;
    }
    
    private static Queue<String> createRestoredQueue(ClassLoader cl, Queue<String> metadata)
    {
        metadata = (Queue<String>) WebsocketApplicationSessionHolder.clWebsocketRestoredQueue.get(cl);
        if (metadata == null)
        {
            metadata = (Queue<String>) new ConcurrentLinkedQueue<String>();
            WebsocketApplicationSessionHolder.clWebsocketRestoredQueue.put(cl, metadata);
        }
        return metadata;
    }
    
}
