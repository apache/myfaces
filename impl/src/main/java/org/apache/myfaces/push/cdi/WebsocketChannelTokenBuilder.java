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
import org.apache.myfaces.util.token.CsrfSessionTokenFactory;
import org.apache.myfaces.util.token.CsrfSessionTokenFactoryRandom;
import org.apache.myfaces.util.token.CsrfSessionTokenFactorySecureRandom;
import org.apache.myfaces.config.MyfacesConfig;

@ApplicationScoped
public class WebsocketChannelTokenBuilder
{
    private CsrfSessionTokenFactory csrfSessionTokenFactory;
    
    private boolean initialized;
    
    public WebsocketChannelTokenBuilder()
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
        String csrfRandomMode = MyfacesConfig.getCurrentInstance(facesContext).getRandomKeyInViewStateSessionToken();
        if (MyfacesConfig.RANDOM_KEY_IN_WEBSOCKET_SESSION_TOKEN_RANDOM.equals(csrfRandomMode))
        {
            csrfSessionTokenFactory = new CsrfSessionTokenFactoryRandom(facesContext);
        }
        else
        {
            csrfSessionTokenFactory = new CsrfSessionTokenFactorySecureRandom(facesContext);
        }        
        initialized = true;
    }
    
    public String createChannelToken(FacesContext facesContext, String channel)
    {
        if (!initialized)
        {
            internalInit(facesContext);
        }
        return csrfSessionTokenFactory.createToken(facesContext);
    }
}
