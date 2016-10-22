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
import javax.faces.context.FacesContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

/**
 *
 */
@ApplicationScoped
public class WebsocketChannelTokenBuilderBean
{
    /**
     * Defines how to generate the csrf session token.
     */
    @JSFWebConfigParam(since="2.2.0", expectedValues="secureRandom, random", 
            defaultValue="none", group="state")
    private static final String RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_PARAM
            = "org.apache.myfaces.RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN";
    private static final String RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_PARAM_DEFAULT = "random";
    
    private static final String RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_SECURE_RANDOM = "secureRandom";
    private static final String RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_RANDOM = "random";
    
    private CsrfSessionTokenFactory csrfSessionTokenFactory;
    
    private boolean initialized;
    
    public WebsocketChannelTokenBuilderBean()
    {
    }
    
    @PostConstruct
    public void init()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null)
        {
            internalInit(facesContext);
        }
    }
    
    private synchronized void internalInit(FacesContext facesContext)
    {
        String csrfRandomMode = WebConfigParamUtils.getStringInitParameter(facesContext.getExternalContext(),
                RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_PARAM, 
                RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_PARAM_DEFAULT);
        if (RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_SECURE_RANDOM.equals(csrfRandomMode))
        {
            csrfSessionTokenFactory = new SecureRandomCsrfSessionTokenFactory(facesContext);
        }
        else
        {
            csrfSessionTokenFactory = new RandomCsrfSessionTokenFactory(facesContext);
        }        
        initialized=true;
    }
    
    public String createChannelToken(FacesContext facesContext, String channel)
    {
        if (!initialized)
        {
            internalInit(facesContext);
        }
        return csrfSessionTokenFactory.createCryptographicallyStrongTokenFromSession(facesContext);
    }
}
