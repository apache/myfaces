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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.push.PushContext;
import org.apache.myfaces.cdi.util.CDIUtils;

public class PushContextImpl implements PushContext
{
    private final String channel;
    private final BeanManager beanManager;
    private final WebsocketScopeManager scopeManager;
    private final WebsocketSessionManager sessionManager;

    public PushContextImpl(String channel, BeanManager beanManager)
    {
        this.channel = channel;
        this.beanManager = beanManager;
        this.scopeManager = CDIUtils.get(beanManager, WebsocketScopeManager.class);
        this.sessionManager = CDIUtils.get(beanManager, WebsocketSessionManager.class);
    }

    public String getChannel()
    {
        return channel;
    }

    @Override
    public Set<Future<Void>> send(Object message)
    {
        //1. locate the channel and define the context
        String channel = getChannel();

        WebsocketScopeManager.AbstractScope applicationScope = scopeManager.getApplicationScope(false);
        WebsocketScopeManager.AbstractScope viewScope = null;
        WebsocketScopeManager.AbstractScope sessionScope = null;

        if (CDIUtils.isRequestScopeActive(beanManager))
        {
            if (CDIUtils.isSessionScopeActive(beanManager))
            {
                sessionScope = scopeManager.getSessionScope(false);
                if (CDIUtils.isViewScopeActive(beanManager))
                {
                    viewScope = scopeManager.getViewScope(false);
                }
            }
        }

        if (applicationScope == null)
        {
            // No base bean to push message
            return Collections.emptySet();
        }
        
        List<String> channelTokens;
        
        if (viewScope != null && viewScope.isChannelAvailable(channel))
        {
            // Use view scope for context
            channelTokens = viewScope.getChannelTokens(channel);
        }
        else if (sessionScope != null && sessionScope.isChannelAvailable(getChannel()))
        {
            // Use session scope for context
            channelTokens = sessionScope.getChannelTokens(channel);
        }
        else if (applicationScope != null && applicationScope.isChannelAvailable(getChannel()))
        {
            // Use application scope for context
            channelTokens = applicationScope.getChannelTokens(channel);
        }
        else
        {
            throw new FacesException("CDI bean not found for push message");
        }
        
        //2. send the message
        if (channelTokens != null && !channelTokens.isEmpty())
        {
            Set<Future<Void>> result = null;
            for (String channelToken : channelTokens)
            {
                if (result == null)
                {
                    result = sessionManager.send(channelToken, message);
                }
                else
                {
                    result.addAll(sessionManager.send(channelToken, message));
                }
            }
            return result;
        }
        
        return Collections.emptySet();
    }

    @Override
    public <S extends Serializable> Set<Future<Void>> send(Object message, S user)
    {
        return send(message, Collections.singleton(user)).get(user);
    }

    @Override
    public <S extends Serializable> Map<S, Set<Future<Void>>> send(Object message, Collection<S> users)
    {
        //1. locate the channel and define the context
        String channel = getChannel();

        WebsocketScopeManager.AbstractScope applicationScope = scopeManager.getApplicationScope(false);
        WebsocketScopeManager.AbstractScope viewScope = null;
        WebsocketScopeManager.AbstractScope sessionScope = null;

        if (CDIUtils.isRequestScopeActive(beanManager))
        {
            if (CDIUtils.isSessionScopeActive(beanManager))
            {
                sessionScope = scopeManager.getSessionScope(false);
                if (CDIUtils.isViewScopeActive(beanManager))
                {
                    viewScope = scopeManager.getViewScope(false);
                }
            }
        }
        
        if (applicationScope == null)
        {
            // No base bean to push message
            return Collections.emptyMap();
        }

        Map<S, Set<Future<Void>>> result = new HashMap<>();

        if (viewScope != null && viewScope.isChannelAvailable(channel))
        {
            // Use view scope for context
            for (S user : users)
            {
                result.put(user, send(viewScope.getChannelTokens(channel, user), message));
            }
        }
        else if (sessionScope != null && sessionScope.isChannelAvailable(getChannel()))
        {
            // Use session scope for context
            for (S user : users)
            {
                result.put(user, send(sessionScope.getChannelTokens(channel, user), message));
            }
        }
        else if (applicationScope != null && applicationScope.isChannelAvailable(getChannel()))
        {
            // Use application scope for context
            for (S user : users)
            {
                result.put(user, send(applicationScope.getChannelTokens(channel, user), message));
            }
        }
        else
        {
            throw new FacesException("CDI bean not found for push message");
        }
        
        //2. send the message
        return result;
    }
    
    private Set<Future<Void>> send(List<String> channelTokens, Object message)
    {
        if (channelTokens != null && !channelTokens.isEmpty())
        {
            Set<Future<Void>> result = null;
            for (int i = 0; i < channelTokens.size(); i++)
            {
                String channelToken = channelTokens.get(i);
                if (result == null)
                {
                    result = sessionManager.send(channelToken, message);
                }
                else
                {
                    result.addAll(sessionManager.send(channelToken, message));
                }
            }
            return result;
        }
        return Collections.emptySet();
    }
}
