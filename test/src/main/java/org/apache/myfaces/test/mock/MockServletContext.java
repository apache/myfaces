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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

/**
 * <p>Mock implementation of <code>ServletContext</code>.</p>
 *
 * <p><strong>WARNING</strong> - Before you can get meaningful results from
 * calls to the <code>getResource()</code>, <code>getResourceAsStream()</code>,
 * <code>getResourcePaths()</code>, or <code>getRealPath()</code> methods,
 * you must configure the <code>documentRoot</code> property, passing in a
 * <code>File</code> object pointing at a directory that simulates a
 * web application structure.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockServletContext implements ServletContext
{

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Add a context initialization parameter to the set of
     * parameters recognized by this instance.</p>
     *
     * @param name Parameter name
     * @param value Parameter value
     */
    public void addInitParameter(String name, String value)
    {
        parameters.put(name, value);
    }

    /**
     * <p>Add a new MIME type mapping to the set of mappings
     * recognized by this instance.</p>
     *
     * @param extension Extension to check for (without the period)
     * @param contentType Corresponding content type
     */
    public void addMimeType(String extension, String contentType)
    {
        mimeTypes.put(extension, contentType);
    }

    /**
     * <p>Set the document root for <code>getRealPath()</code>
     * resolution.  This parameter <strong>MUST</strong> represent
     * a directory.</p>
     *
     * @param documentRoot The new base directory
     */
    public void setDocumentRoot(File documentRoot)
    {
        this.documentRoot = documentRoot;
    }

    public void setDocumentRoot(URI base)
    {
        File f = new File(base);
        if (!f.exists())
        {
            throw new IllegalArgumentException("File: " + base.getPath()
                    + " doesn't exist");
        }
        this.documentRoot = f;
    }

    /**
     * <p>Add a new listener instance that should be notified about
     * attribute changes.</p>
     *
     * @param listener Listener to be added
     */
    public void addAttributeListener(ServletContextAttributeListener listener)
    {
        MockWebContainer container = getWebContainer();
        if (container == null)
        {
            attributeListeners.add(listener);
        }
        else
        {
            container.subscribeListener(listener);
        }
    }
    
    public MockWebContainer getWebContainer()
    {
        return webContainer;
    }
    
    public void setWebContainer(MockWebContainer container)
    {
        webContainer = container;
    }

    // ------------------------------------------------------ Instance Variables

    private Hashtable attributes = new Hashtable();
    private File documentRoot = null;
    private Hashtable mimeTypes = new Hashtable();
    private Hashtable parameters = new Hashtable();
    private List attributeListeners = new ArrayList();
    private MockWebContainer webContainer;
    private Map<String, ServletRegistration> servletRegistrations = 
            new HashMap<String, ServletRegistration>();
    
    private ClassLoader classLoader;

    // -------------------------------------------------- ServletContext Methods

    /** {@inheritDoc} */
    public Object getAttribute(String name)
    {

        return attributes.get(name);

    }

    /** {@inheritDoc} */
    public Enumeration getAttributeNames()
    {

        return attributes.keys();

    }

    /** {@inheritDoc} */
    public String getInitParameter(String name)
    {

        return (String) parameters.get(name);

    }

    /** {@inheritDoc} */
    public Enumeration getInitParameterNames()
    {

        return parameters.keys();

    }

    /** {@inheritDoc} */
    public int getMajorVersion()
    {

        return 2;

    }

    /** {@inheritDoc} */
    public String getMimeType(String path)
    {

        int period = path.lastIndexOf('.');
        if (period < 0)
        {
            return null;
        }
        String extension = path.substring(period + 1);
        return (String) mimeTypes.get(extension);

    }

    /** {@inheritDoc} */
    public int getMinorVersion()
    {

        return 4;

    }

    /** {@inheritDoc} */
    public String getRealPath(String path)
    {

        if (documentRoot != null)
        {
            if (!path.startsWith("/"))
            {
                throw new IllegalArgumentException("The specified path ('"
                        + path + "') does not start with a '/' character");
            }
            File resolved = new File(documentRoot, path.substring(1));
            try
            {
                return resolved.getCanonicalPath();
            }
            catch (IOException e)
            {
                return resolved.getAbsolutePath();
            }
        }
        else
        {
            return null;
        }

    }

    /** {@inheritDoc} */
    public URL getResource(String path) throws MalformedURLException
    {

        if (documentRoot != null)
        {
            if (!path.startsWith("/"))
            {
                throw new MalformedURLException("The specified path ('" + path
                        + "') does not start with a '/' character");
            }
            File resolved = new File(documentRoot, path.substring(1));
            if (resolved.exists())
            {
                return resolved.toURL();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }

    }

    /** {@inheritDoc} */
    public InputStream getResourceAsStream(String path)
    {
        try
        {
            URL url = getResource(path);
            if (url != null)
            {
                return url.openStream();
            }
        }
        catch (Exception e)
        {
            //No op
        }
        return null;
    }

    /** {@inheritDoc} */
    public Set getResourcePaths(String path)
    {

        if (documentRoot == null)
        {
            return null;
        }

        // Enforce the leading slash restriction
        if (!path.startsWith("/"))
        {
            throw new IllegalArgumentException("The specified path ('" + path
                    + "') does not start with a '/' character");
        }

        // Locate the File node for this path's directory (if it exists)
        File node = new File(documentRoot, path.substring(1));
        if (!node.exists())
        {
            return null;
        }
        if (!node.isDirectory())
        {
            return null;
        }

        // Construct a Set containing the paths to the contents of this directory
        Set set = new HashSet();
        String[] files = node.list();
        if (files == null)
        {
            return null;
        }
        for (int i = 0; i < files.length; i++)
        {
            String subfile = path + files[i];
            File subnode = new File(node, files[i]);
            if (subnode.isDirectory())
            {
                subfile += "/";
            }
            set.add(subfile);
        }

        // Return the completed set
        return set;
    }

    /** {@inheritDoc} */
    public void log(String message)
    {
        System.out.println(message);
    }

    /** {@inheritDoc} */
    public void log(Exception exception, String message)
    {
        System.out.println(message);
        exception.printStackTrace();
    }

    /** {@inheritDoc} */
    public void log(String message, Throwable exception)
    {
        System.out.println(message);
        exception.printStackTrace();
    }

    /** {@inheritDoc} */
    public void removeAttribute(String name)
    {
        if (attributes.containsKey(name))
        {
            Object value = attributes.remove(name);
            fireAttributeRemoved(name, value);
        }
    }

    /** {@inheritDoc} */
    public void setAttribute(String name, Object value)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (value == null)
        {
            removeAttribute(name);
            return;
        }
        if (attributes.containsKey(name))
        {
            Object oldValue = attributes.get(name);
            attributes.put(name, value);
            fireAttributeReplaced(name, oldValue);
        }
        else
        {
            attributes.put(name, value);
            fireAttributeAdded(name, value);
        }
    }

    /** {@inheritDoc} */
    public ServletContext getContext(String uripath)
    {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getContextPath()
    {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Servlet getServlet(String name) throws ServletException
    {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public String getServletContextName()
    {
        return "MockServletContext";
    }

    /** {@inheritDoc} */
    public String getServerInfo()
    {
        return "MockServletContext";
    }

    /** {@inheritDoc} */
    public Enumeration getServlets()
    {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Enumeration getServletNames()
    {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public RequestDispatcher getNamedDispatcher(String name)
    {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public RequestDispatcher getRequestDispatcher(String path)
    {
        URI uri = this.resolve(path);
        if (uri != null)
        {
            File f = new File(uri);
            if (f.exists())
            {
                try
                {
                    return new MockRequestDispatcher(uri.toURL());
                }
                catch (MalformedURLException e)
                {
                    this.log(e.getMessage());
                    return null;
                }
            }

        }
        return null;
    }

    // --------------------------------------------------------- Private Methods

    private final URI resolve(String path)
    {
        if (path == null)
        {
            throw new NullPointerException("Path cannot be null");
        }
        if (path.charAt(0) == '/')
        {
            if (path.length() > 1)
            {
                return documentRoot.toURI().resolve(path.substring(1));
            }
            return documentRoot.toURI();
        }
        return null;
    }

    /**
     * <p>Fire an attribute added event to interested listeners.</p>
     *
     * @param key Attribute whose value has been added
     * @param value The new value
     */
    void fireAttributeAdded(String key, Object value)
    {
        MockWebContainer container = getWebContainer();
        if (container == null)
        {        
            if (attributeListeners.size() < 1)
            {
                return;
            }
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(
                    this, key, value);
            Iterator listeners = attributeListeners.iterator();
            while (listeners.hasNext())
            {
                ServletContextAttributeListener listener = (ServletContextAttributeListener) listeners
                        .next();
                listener.attributeAdded(event);
            }
        }
        else
        {
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(
                    this, key, value);
            container.attributeAdded(event);
        }
    }

    /**
     * <p>Fire an attribute removed event to interested listeners.</p>
     *
     * @param key Attribute whose value has been removed
     * @param value The value that was removed
     */
    void fireAttributeRemoved(String key, Object value)
    {
        MockWebContainer container = getWebContainer();
        if (container == null)
        {
            if (attributeListeners.size() < 1)
            {
                return;
            }
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(
                    this, key, value);
            Iterator listeners = attributeListeners.iterator();
            while (listeners.hasNext())
            {
                ServletContextAttributeListener listener = (ServletContextAttributeListener) listeners
                        .next();
                listener.attributeRemoved(event);
            }
        }
        else
        {
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(
                    this, key, value);
            container.attributeRemoved(event);
        }
    }

    /**
     * <p>Fire an attribute replaced event to interested listeners.</p>
     *
     * @param key Attribute whose value has been replaced
     * @param value The original value
     */
    void fireAttributeReplaced(String key, Object value)
    {
        MockWebContainer container = getWebContainer();
        if (container == null)
        {
            if (attributeListeners.size() < 1)
            {
                return;
            }
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(
                    this, key, value);
            Iterator listeners = attributeListeners.iterator();
            while (listeners.hasNext())
            {
                ServletContextAttributeListener listener = (ServletContextAttributeListener) listeners
                        .next();
                listener.attributeReplaced(event);
            }
        }
        else
        {
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(
                    this, key, value);            
            container.attributeReplaced(event);
        }
    }

    public boolean setInitParameter(String name, String value)
    {
        addInitParameter(name, value);
        return true;
    }

    public ServletRegistration.Dynamic addServlet(String name, String string1) 
            throws IllegalArgumentException, IllegalStateException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public ServletRegistration.Dynamic addServlet(String name, Servlet srvlt) 
            throws IllegalArgumentException, IllegalStateException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public ServletRegistration.Dynamic addServlet(String name, Class<? extends Servlet> type) 
            throws IllegalArgumentException, IllegalStateException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public <T extends Servlet> T createServlet(Class<T> type) throws ServletException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public ServletRegistration getServletRegistration(String name)
    {
        return servletRegistrations.get(name);
    }

    public Map<String, ? extends ServletRegistration> getServletRegistrations()
    {
        return servletRegistrations;
    }
    
    public void addServletRegistration(String name, String servletClassName, String ... mappings)
    {
        ServletRegistration sr = new MockServletRegistration(name, servletClassName, mappings);
        this.servletRegistrations.put(name, sr);
    }

    public FilterRegistration.Dynamic addFilter(String string, String string1) 
            throws IllegalArgumentException, IllegalStateException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public FilterRegistration.Dynamic addFilter(String string, Filter filter) 
            throws IllegalArgumentException, IllegalStateException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public FilterRegistration.Dynamic addFilter(String string, Class<? extends Filter> type) 
            throws IllegalArgumentException, IllegalStateException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public <T extends Filter> T createFilter(Class<T> type) throws ServletException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public FilterRegistration getFilterRegistration(String string)
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public Map<String, ? extends FilterRegistration> getFilterRegistrations()
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public void addListener(Class<? extends EventListener> type)
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public void addListener(String string)
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public <T extends EventListener> void addListener(T t)
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public <T extends EventListener> T createListener(Class<T> type) throws ServletException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public void declareRoles(String... strings)
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public SessionCookieConfig getSessionCookieConfig()
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> set)
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public int getEffectiveMajorVersion() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public int getEffectiveMinorVersion() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public ClassLoader getClassLoader()
    {
        if (classLoader != null)
        {
            return classLoader;
        }
        else
        {
            return Thread.currentThread().getContextClassLoader();
        }
    }
    
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    public JspConfigDescriptor getJspConfigDescriptor()
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String string, String string1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVirtualServerName()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSessionTimeout()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSessionTimeout(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRequestCharacterEncoding()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRequestCharacterEncoding(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getResponseCharacterEncoding()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setResponseCharacterEncoding(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
