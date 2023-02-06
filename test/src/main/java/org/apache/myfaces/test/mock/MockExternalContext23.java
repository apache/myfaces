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

package org.apache.myfaces.test.mock;

import jakarta.faces.lifecycle.ClientWindow;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>Mock implementation of <code>ExternalContext</code> that includes the semantics
 * added by JavaServer Faces 2.2.</p>
 * 
 * @author Leonardo Uribe
 * @since 1.0.0
 *
 */
public class MockExternalContext23 extends MockExternalContext20
{

    // ------------------------------------------------------------ Constructors

    public MockExternalContext23(ServletContext context,
            HttpServletRequest request, HttpServletResponse response)
    {
        super(context, request, response);
        _clientWindow = null;
    }

    // ------------------------------------------------------ Instance Variables

    private ClientWindow _clientWindow;
    
    // ----------------------------------------------------- Mock Object Methods


    @Override
    public boolean isSecure()
    {
        return request.isSecure();
    }

    @Override
    public int getSessionMaxInactiveInterval()
    {
        HttpSession session = request.getSession();
        return session.getMaxInactiveInterval();
    }

    @Override
    public void setSessionMaxInactiveInterval(int interval)
    {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(interval);
    }

    @Override
    public ClientWindow getClientWindow()
    {
        return _clientWindow;
    }
    
    @Override
    public void setClientWindow(ClientWindow window)
    {
        _clientWindow = window;
    }

    @Override
    public String encodeWebsocketURL(String baseUrl)
    {
        Integer port = 8080;
        port = (port == 0) ? null : port;
        if (port != null && 
            !port.equals(request.getServerPort()))
        {
            String scheme = "http";
            String serverName = request.getServerName();
            String url;
            try
            {
                url = new URL(scheme, serverName, port, baseUrl).toExternalForm();
                url = url.replaceFirst("http", "ws");
                return url;
            }
            catch (MalformedURLException ex)
            {
                //If cannot build the url, return the base one unchanged
                return baseUrl;
            }
        }
        else
        {
            return baseUrl;
        }
    }
    
    @Override
    public String getSessionId(boolean create)
    {
        HttpSession session = ((HttpServletRequest) request).getSession(create);
        if (session != null)
        {
            return session.getId();
        }
        else
        {
            return "";
        }
    }

    @Override
    public String getApplicationContextPath()
    {
        return context.getContextPath();
    }

    // ------------------------------------------------- ExternalContext Methods

    @Override
    public void release()
    {
        
    }
    
}
