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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>Mock implementation of <code>ExternalContext</code> that includes the semantics
 * added by JavaServer Faces 2.2.</p>
 * 
 * @author Leonardo Uribe
 * @since 1.0.0
 *
 */
public class MockExternalContext extends MockExternalContext20
{

    // ------------------------------------------------------------ Constructors

    public MockExternalContext(ServletContext context,
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
