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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.faces.context.ExternalContext;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

import org.apache.myfaces.push.WebsocketSessionClusterSerializedRestore;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.push.Json;
import org.apache.myfaces.util.lang.ConcurrentLRUCache;
import org.apache.myfaces.util.lang.Lazy;

import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;

@ApplicationScoped
public class WebsocketSessionManager
{
    private Lazy<ConcurrentLRUCache<String, Collection<Reference<Session>>>> sessionMap;

    private Lazy<ConcurrentHashMap<UserChannelKey, Set<String>>> userMap;
    private Queue<String> restoreQueue;

    private static final CloseReason REASON_EXPIRED = new CloseReason(NORMAL_CLOSURE, "Expired");

    private static final Logger LOG = Logger.getLogger(WebsocketSessionManager.class.getName());

    @PostConstruct
    public void init()
    {
        sessionMap = new Lazy<>(() ->
        {
            int size = MyfacesConfig.WEBSOCKET_MAX_CONNECTIONS_DEFAULT;
            return new ConcurrentLRUCache<>((size * 4 + 3) / 3, size);
        });
        restoreQueue = new ConcurrentLinkedQueue<>();
        userMap = new Lazy<>(ConcurrentHashMap::new);
    }

    public ConcurrentLRUCache<String, Collection<Reference<Session>>> getSessionMap()
    {
        return sessionMap.get();
    }

    public ConcurrentMap<UserChannelKey, Set<String>> getUserMap()
    {
        return userMap.get();
    }

    public void registerSessionToken(String channelToken)
    {
        ConcurrentLRUCache<String, Collection<Reference<Session>>> sessionMap = this.getSessionMap();
        if (sessionMap.get(channelToken) == null)
        {
            sessionMap.put(channelToken, new ConcurrentLinkedQueue<>());
        }
    }

    public void registerUser(Serializable user, String channel, String channelToken)
    {
        UserChannelKey userChannelKey = new UserChannelKey(user, channel);

        Set<String> channelTokenSet = getUserMap().computeIfAbsent(userChannelKey, k -> new HashSet<>(1));
        channelTokenSet.add(channelToken);
    }

    public void deregisterUser(Serializable user, String channel, String channelToken)
    {
        UserChannelKey userChannelKey = new UserChannelKey(user, channel);

        synchronized (getUserMap())
        {
            Set<String> channelTokenSet = getUserMap().get(userChannelKey);
            if (channelTokenSet != null)
            {
                channelTokenSet.remove(channelToken);
                if (channelTokenSet.isEmpty())
                {
                    getUserMap().remove(userChannelKey);
                }
            }
        }
    }

    public Set<String> getChannelTokensForUser(Serializable user, String channel)
    {
        UserChannelKey userChannelKey = new UserChannelKey(user, channel);
        return getUserMap().get(userChannelKey);
    }

    public void initSessionMap(ExternalContext context)
    {
        int size = MyfacesConfig.getCurrentInstance(context).getWebsocketMaxConnections();
        ConcurrentLRUCache<String, Collection<Reference<Session>>> newSessionMap
                = new ConcurrentLRUCache<>((size * 4 + 3) / 3, size);
        
        synchronized (sessionMap)
        {
            if (sessionMap.isInitialized())
            {
                // If a Session has been restored, it could be already a lruCache instantiated, so in this case
                // we need to fill the new one with the old instances, but only the instances that are active
                // at the moment.
                Set<Map.Entry<String, Collection<Reference<Session>>>> entries = sessionMap.get()
                        .getLatestAccessedItems(MyfacesConfig.WEBSOCKET_MAX_CONNECTIONS_DEFAULT).entrySet();
                for (Map.Entry<String, Collection<Reference<Session>>> entry : entries)
                {
                    Collection<Reference<Session>> referenceCollection = entry.getValue();
                    if (referenceCollection != null)
                    {
                        Collection<Reference<Session>> newReferenceCollection =
                                referenceCollection
                                        .stream()
                                        .filter(p -> p.get() != null && p.get().isOpen())
                                        .distinct()
                                        .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
                        newSessionMap.put(entry.getKey(), newReferenceCollection);
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
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log (Level.FINE, "WebsocketSessionManager: addOrUpdateSession for channelToken = {0}, " +
                    "session.id = {1}", new Object[] {channelToken ,session.getId()});
        }
        
        ConcurrentLRUCache<String, Collection<Reference<Session>>> sessionMap = this.getSessionMap();
        Collection<Reference<Session>> sessions = sessionMap.get(channelToken);
        if (sessions == null)
        {
            registerSessionToken(channelToken);
            sessions = sessionMap.get(channelToken);
        }

        Optional<Reference<Session>> referenceOptional =
                sessions.stream().filter(p -> Objects.equals(p.get(), session)).findFirst();

        if (!referenceOptional.isPresent())
        {
            return sessions.add(new SoftReference<>(session));
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
     * @param session
     */
    public void removeSession(String channelToken, Session session)
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log(Level.FINE, "WebsocketSessionManager: removeSession for channelToken = {0}, " +
                    "session.id = {1}", new Object[] {channelToken ,session.getId()});
        }
        Collection<Reference<Session>> collection = getSessionMap().get(channelToken);
        Optional<Reference<Session>> referenceOptional =
                collection.stream().filter(p -> Objects.equals(p.get(), session)).findFirst();
        referenceOptional.ifPresent(collection::remove);
    }

    /**
     * Remove the channelToken and close all sessions associated with it. Happens, when session scope
     * or view scope is destroyed.
     * @param channelToken
     */
    public void removeChannelToken(String channelToken)
    {
        // close all sessions associated with this channelToken
        Collection<Reference<Session>> sessions = getSessionMap().get(channelToken);

        if (sessions != null)
        {
            for (Reference<Session> sessionReference : sessions)
            {
                Session session = sessionReference.get();
                if (session != null && session.isOpen())
                {
                    try
                    {
                        session.close(REASON_EXPIRED);
                    }
                    catch (IOException ignore)
                    {
                        // ignored
                    }
                }
            }
        }

        getSessionMap().remove(channelToken);
    }

    protected Set<Future<Void>> send(String channelToken, Object message)
    {
        // Before send, we need to check 
        synchronizeSessionInstances();

        Set<Future<Void>> results = new HashSet<>(1);
        Collection<Reference<Session>> sessions = (channelToken != null) ? getSessionMap().get(channelToken) : null;

        if (sessions != null && !sessions.isEmpty())
        {
            String json = Json.encode(message);

            sessions.forEach(sessionRef ->
            {
                if (sessionRef != null && sessionRef.get() != null)
                {
                    Session session = sessionRef.get();
                    if (session.isOpen())
                    {
                        send(session, json, results, 0);
                    }
                    else
                    {
                        //If session is not open, remove the session, because a websocket
                        // session after is closed cannot
                        //be alive.
                        removeSession(channelToken, session);
                    }
                }
            });
            return results;
        }
        else
        {
            return Collections.emptySet();
        }
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
                LOG.warning(String.format(WARNING_TOMCAT_WEB_SOCKET_BOMBED, retries));
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
    
    public void synchronizeSessionInstances()
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
            Map<String, Collection<Reference<Session>>> map = getSessionMap().getLatestAccessedItems(1);
            if (map != null && !map.isEmpty())
            {

                Collection<Reference<Session>> collectionRef = map.values().iterator().next();
                collectionRef.stream().filter(ref -> ref != null).forEach(ref ->
                {
                    Session session = ref.get();
                    if (session != null)
                    {
                        Set<Session> sessions = session.getOpenSessions();
                        for (Session instance : sessions)
                        {
                            WebsocketSessionClusterSerializedRestore r = (WebsocketSessionClusterSerializedRestore)
                                    instance.getUserProperties().get(WebsocketSessionClusterSerializedRestore
                                            .WEBSOCKET_SESSION_SERIALIZED_RESTORE);
                            if (r != null && r.isDeserialized())
                            {
                                addOrUpdateSession(r.getChannelToken(), session);
                            }
                        }

                        // Remove one element from the queue
                        queue.poll();
                    }
                });
            }
        }
    }

    public Queue<String> getRestoredQueue()
    {
        return restoreQueue;
    }

    public class UserChannelKey implements Serializable
    {
        private final Serializable user;
        private final String channel;

        public UserChannelKey(Serializable user, String channel)
        {
            this.user = user;
            this.channel = channel;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            UserChannelKey that = (UserChannelKey) o;
            return Objects.equals(user, that.user) && Objects.equals(channel, that.channel);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(user, channel);
        }
    }

}
