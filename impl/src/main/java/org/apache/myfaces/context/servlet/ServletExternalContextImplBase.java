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
package org.apache.myfaces.context.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.lifecycle.ClientWindow;
import javax.servlet.ServletContext;

import org.apache.myfaces.util.lang.Assert;

/**
 * Provides a base implementation of the ExternalContext for Servlet
 * environments. This impl provides all methods which only rely on the
 * ServletContext and thus are also provided at startup and shutdown.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class ServletExternalContextImplBase extends ExternalContext
{
    
    private static final String INIT_PARAMETER_MAP_ATTRIBUTE = InitParameterMap.class.getName();
    
    private ServletContext _servletContext;
    private Map<String, Object> _applicationMap;
    private Map<String, String> _initParameterMap;
    private ClientWindow _clientWindow;

    
    public ServletExternalContextImplBase(ServletContext servletContext)
    {
        _servletContext = servletContext;
        _applicationMap = null;
        _initParameterMap = null;
        _clientWindow = null;
    }
    
    @Override
    public void release()
    {
        _servletContext = null;
        _applicationMap = null;
        _initParameterMap = null;
        _clientWindow = null;
    }
    
    // ~ Methods which only rely on the ServletContext-------------------------
    
    @Override
    public Map<String, Object> getApplicationMap()
    {
        if (_applicationMap == null)
        {
            _applicationMap = new ApplicationMap(_servletContext);
        }
        return _applicationMap;
    }
    
    @Override
    public String getMimeType(String file)
    {
        Assert.notNull(file, "file");
        return _servletContext.getMimeType(file);
    }
    
    @Override
    public Object getContext()
    {
        return _servletContext;
    }
    
    @Override
    public String getContextName() 
    {
        return _servletContext.getServletContextName();
    }
    
    @Override
    public String getInitParameter(final String name)
    {
        Assert.notNull(name, "name");
        return _servletContext.getInitParameter(name);
    }

    @Override
    public Map<String, String> getInitParameterMap()
    {
        if (_initParameterMap == null)
        {
            // We cache it as an attribute in ServletContext itself (is this circular reference a problem?)
            _initParameterMap = (Map<String, String>) _servletContext.getAttribute(INIT_PARAMETER_MAP_ATTRIBUTE);
            if (_initParameterMap == null)
            {
                _initParameterMap = new InitParameterMap(_servletContext);
                _servletContext.setAttribute(INIT_PARAMETER_MAP_ATTRIBUTE, _initParameterMap);
            }
        }
        return _initParameterMap;
    }
    
    @Override
    public URL getResource(final String path) throws MalformedURLException
    {
        Assert.notNull(path, "path");
        return _servletContext.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(final String path)
    {
        Assert.notNull(path, "path");
        return _servletContext.getResourceAsStream(path);
    }

    @Override
    public Set<String> getResourcePaths(final String path)
    {
        Assert.notNull(path, "path");
        return _servletContext.getResourcePaths(path);
    }

    @Override
    public void log(final String message)
    {
        Assert.notNull(message, "message");
        _servletContext.log(message);
    }

    @Override
    public void log(final String message, final Throwable exception)
    {
        Assert.notNull(message, "message");
        Assert.notNull(exception, "exception");
        _servletContext.log(message, exception);
    }
    
    @Override
    public String getRealPath(String path)
    {
        Assert.notNull(path, "path");
        return _servletContext.getRealPath(path);
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
    public String getApplicationContextPath() 
    {
        return _servletContext.getContextPath();
        
    }    
}
