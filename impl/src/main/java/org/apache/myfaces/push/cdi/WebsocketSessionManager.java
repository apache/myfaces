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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.websocket.Session;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.push.WebsocketSessionClusterSerializedRestore;
import org.apache.myfaces.push.Json;
import org.apache.myfaces.util.lang.ConcurrentLRUCache;
import org.apache.myfaces.util.lang.Lazy;

@ApplicationScoped
public class WebsocketSessionManager
{
    private Lazy<ConcurrentLRUCache<String, Reference<Session>>> sessionMap;
    private Queue<String> restoreQueue;

    @PostConstruct
    public void init()
    {
        sessionMap = new Lazy<>(() ->
        {
            int size = MyfacesConfig.WEBSOCKET_MAX_CONNECTIONS_DEFAULT;
            return new ConcurrentLRUCache<>((size * 4 + 3) / 3, size);
        });
        restoreQueue = new ConcurrentLinkedQueue<>();
    }

    public ConcurrentLRUCache<String, Reference<Session>> getSessionMap()
    {
        return sessionMap.get();
    }

    public void initSessionMap(ExternalContext context)
    {
        int size = MyfacesConfig.getCurrentInstance(context).getWebsocketMaxConnections();
        ConcurrentLRUCache<String, Reference<Session>> newSessionMap
                = new ConcurrentLRUCache<>((size * 4 + 3) / 3, size);
        
        synchronized (sessionMap)
        {
            if (sessionMap.isInitialized())
            {
                // If a Session has been restored, it could be already a lruCache instantiated, so in this case
                // we need to fill the new one with the old instances, but only the instances that are active
                // at the moment.
                Set<Map.Entry<String, Reference<Session>>> entries = sessionMap.get()
                        .getLatestAccessedItems(MyfacesConfig.WEBSOCKET_MAX_CONNECTIONS_DEFAULT).entrySet();
                for (Map.Entry<String, Reference<Session>> entry : entries)
                {
                    if (entry.getValue() != null && entry.getValue().get() != null && entry.getValue().get().isOpen())
                    {
                        newSessionMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            sessionMap.reset(newSessionMap);
        }
    }

    public void clearSessions()
    {
        if (sessionMap.isInitialized())
        {
            sessionMap.get().clear();
        }
        restoreQueue.clear();
    }
    
    public boolean addOrUpdateSession(String channelToken, Session session)
    {
        Reference oldInstance = getSessionMap().get(channelToken);
        if (oldInstance == null)
        {
            getSessionMap().put(channelToken, new SoftReference<>(session));
        }
        else if (!session.equals(oldInstance.get()))
        {
            getSessionMap().put(channelToken, new SoftReference<>(session));
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
    public boolean removeSession(String channelToken)
    {
        getSessionMap().remove(channelToken);
        return false;
    }
    
    
    protected Set<Future<Void>> send(String channelToken, Object message)
    {
        // Before send, we need to check 
        synchronizeSessionInstances();

        Set<Future<Void>> results = new HashSet<>(1);
        Reference<Session> sessionRef = (channelToken != null) ? getSessionMap().get(channelToken) : null;

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
                getSessionMap().remove(channelToken);
            }
        }
        return results;
    }

    private final String WARNING_TOMCAT_WEB_SOCKET_BOMBED =
            "Tomcat cannot handle concurrent push messages. A push message has been sent only after %s retries."
            + " Consider rate limiting sending push messages. For example, once every 500ms.";    
    
    private void send(Session session, String text, Set<Future<Void>> results, int retries)
    {
        try
        {
            results.add(session.getAsyncRemote().sendText(text));

            if (retries > 0)
            {
                Logger.getLogger(WebsocketSessionManager.class.getName())
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
    private boolean isTomcatWebSocketBombed(Session session, IllegalStateException illegalStateException)
    {
        return session.getClass().getName().startsWith("org.apache.tomcat.websocket.")
                && illegalStateException.getMessage().contains("[TEXT_FULL_WRITING]");
    }
    
    private void synchronizeSessionInstances()
    {
        Queue<String> queue = getRestoredQueue();
        // The queue is always empty, unless a deserialization of Session instances happen. If that happens, 
        // we need to ensure all Session instances that were deserialized are on the LRU cache, so all instances
        // receive the message when a "push" is done.
        // This is not the ideal, but this is the best we have with the current websocket spec.
        if (!queue.isEmpty())
        {
            // It is necessary to have at least 1 registered Session instance to call getOpenSessions() and get all
            // instances associated to javax.faces.push Endpoint.
            Map<String, Reference<Session>> map = getSessionMap().getLatestAccessedItems(1);
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

    public Queue<String> getRestoredQueue()
    {
        return restoreQueue;
    }
        
}
