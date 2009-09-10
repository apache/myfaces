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
package org.apache.myfaces.view.facelets.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.FaceletHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.compiler.Compiler;
import org.apache.myfaces.view.facelets.util.ParameterCheck;

/**
 * Default FaceletFactory implementation.
 * 
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletFactory.java,v 1.10 2007/04/09 01:13:17 youngm Exp $
 */
public final class DefaultFaceletFactory extends FaceletFactory
{
    private static final long INFINITE_DELAY = -1;
    private static final long NO_CACHE_DELAY = 0;
    
    protected final Log log = LogFactory.getLog(DefaultFaceletFactory.class);

    private URL _baseUrl;

    private Compiler _compiler;

    private Map<String, DefaultFacelet> _facelets;
    
    private Map<String, DefaultFacelet> _viewMetadataFacelets;

    private long _refreshPeriod;

    private Map<String, URL> _relativeLocations;

    private ResourceResolver _resolver;

    public DefaultFaceletFactory(Compiler compiler, ResourceResolver resolver) throws IOException
    {
        this(compiler, resolver, -1);
    }

    public DefaultFaceletFactory(Compiler compiler, ResourceResolver resolver, long refreshPeriod)
    {
        ParameterCheck.notNull("compiler", compiler);
        ParameterCheck.notNull("resolver", resolver);

        _compiler = compiler;

        _facelets = new HashMap<String, DefaultFacelet>();
        
        _viewMetadataFacelets = new HashMap<String, DefaultFacelet>();

        _relativeLocations = new HashMap<String, URL>();

        _resolver = resolver;

        _baseUrl = resolver.resolveUrl("/");

        _refreshPeriod = refreshPeriod < 0 ? INFINITE_DELAY : refreshPeriod * 1000;

        if (log.isDebugEnabled())
        {
            log.debug("Using ResourceResolver: " + _resolver);
            log.debug("Using Refresh Period: " + _refreshPeriod);
        }
    }

    /**
     * Compiler this factory uses
     * 
     * @return final Compiler instance
     */
    public Compiler getCompiler()
    {
        return _compiler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.myfaces.view.facelets.FaceletFactory#getFacelet(java.lang.String)
     */
    public Facelet getFacelet(String uri) throws IOException, FaceletException, FacesException, ELException
    {
        URL url = (URL) _relativeLocations.get(uri);
        if (url == null)
        {
            url = resolveURL(_baseUrl, uri);
            if (url != null)
            {
                Map<String, URL> newLoc = new HashMap<String, URL>(_relativeLocations);
                newLoc.put(uri, url);
                _relativeLocations = newLoc;
            }
            else
            {
                throw new IOException("'" + uri + "' not found.");
            }
        }
        return this.getFacelet(url);
    }

    /**
     * Create a Facelet from the passed URL. This method checks if the cached Facelet needs to be refreshed before
     * returning. If so, uses the passed URL to build a new instance;
     * 
     * @param url
     *            source url
     * @return Facelet instance
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public Facelet getFacelet(URL url) throws IOException, FaceletException, FacesException, ELException
    {
        ParameterCheck.notNull("url", url);
        
        String key = url.toString();
        
        DefaultFacelet f = _facelets.get(key);
        
        if (f == null || this.needsToBeRefreshed(f))
        {
            f = this._createFacelet(url);
            if (_refreshPeriod != NO_CACHE_DELAY)
            {
                Map<String, DefaultFacelet> newLoc = new HashMap<String, DefaultFacelet>(_facelets);
                newLoc.put(key, f);
                _facelets = newLoc;
            }
        }
        
        return f;
    }

    public long getRefreshPeriod()
    {
        return _refreshPeriod;
    }

    /**
     * Resolves a path based on the passed URL. If the path starts with '/', then resolve the path against
     * {@link javax.faces.context.ExternalContext#getResource(java.lang.String)
     * javax.faces.context.ExternalContext#getResource(java.lang.String)}. Otherwise create a new URL via
     * {@link URL#URL(java.net.URL, java.lang.String) URL(URL, String)}.
     * 
     * @param source
     *            base to resolve from
     * @param path
     *            relative path to the source
     * @return resolved URL
     * @throws IOException
     */
    public URL resolveURL(URL source, String path) throws IOException
    {
        if (path.startsWith("/"))
        {
            URL url = _resolver.resolveUrl(path);
            if (url == null)
            {
                throw new FileNotFoundException(path + " Not Found in ExternalContext as a Resource");
            }
            return url;
        }
        else
        {
            return new URL(source, path);
        }
    }

    /**
     * Template method for determining if the Facelet needs to be refreshed.
     * 
     * @param facelet
     *            Facelet that could have expired
     * @return true if it needs to be refreshed
     */
    protected boolean needsToBeRefreshed(DefaultFacelet facelet)
    {
        // if set to 0, constantly reload-- nocache
        if (_refreshPeriod == NO_CACHE_DELAY)
        {
            return true;
        }

        // if set to -1, never reload
        if (_refreshPeriod == INFINITE_DELAY)
        {
            return false;
        }

        long target = facelet.getCreateTime() + _refreshPeriod;
        if (System.currentTimeMillis() > target)
        {
            // Should check for file modification

            try
            {
                URLConnection conn = facelet.getSource().openConnection();
                InputStream is = conn.getInputStream();
                try
                {
                    long lastModified = conn.getLastModified();

                    return lastModified == 0 || lastModified > target;
                }
                finally
                {
                    is.close();
                }
            }
            catch (IOException e)
            {
                throw new FaceletException("Error Checking Last Modified for " + facelet.getAlias(), e);
            }
        }

        return false;
    }

    /**
     * Uses the internal Compiler reference to build a Facelet given the passed URL.
     * 
     * @param url
     *            source
     * @return a Facelet instance
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    private DefaultFacelet _createFacelet(URL url) throws IOException, FaceletException, FacesException, ELException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Creating Facelet for: " + url);
        }

        String alias = "/" + url.getFile().replaceFirst(_baseUrl.getFile(), "");
        try
        {
            FaceletHandler h = _compiler.compile(url, alias);
            DefaultFacelet f = new DefaultFacelet(this, _compiler.createExpressionFactory(), url, alias, h);
            return f;
        }
        catch (FileNotFoundException fnfe)
        {
            throw new FileNotFoundException("Facelet " + alias + " not found at: " + url.toExternalForm());
        }
    }
    
    /**
     * @since 2.0
     * @param url
     * @return
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    private DefaultFacelet _createViewMetadataFacelet(URL url) throws IOException, FaceletException, FacesException, ELException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Creating Facelet used to create View Metadata for: " + url);
        }

        // The alias is used later for informative purposes, so we append 
        // some prefix to identify later where the errors comes from.
        String alias = "/viewMetadata/" + url.getFile().replaceFirst(_baseUrl.getFile(), "");
        try
        {
            FaceletHandler h = _compiler.compileViewMetadata(url, alias);
            DefaultFacelet f = new DefaultFacelet(this, _compiler.createExpressionFactory(), url, alias, h);
            return f;
        }
        catch (FileNotFoundException fnfe)
        {
            throw new FileNotFoundException("Facelet " + alias + " not found at: " + url.toExternalForm());
        }

    }

    /**
     * Works in the same way as getFacelet(String uri), but redirect
     * to getViewMetadataFacelet(URL url)
     * @since 2.0
     */
    @Override
    public Facelet getViewMetadataFacelet(String uri) throws IOException
    {
        URL url = (URL) _relativeLocations.get(uri);
        if (url == null)
        {
            url = resolveURL(_baseUrl, uri);
            if (url != null)
            {
                Map<String, URL> newLoc = new HashMap<String, URL>(_relativeLocations);
                newLoc.put(uri, url);
                _relativeLocations = newLoc;
            }
            else
            {
                throw new IOException("'" + uri + "' not found.");
            }
        }
        return this.getViewMetadataFacelet(url);
    }

    /**
     * @since 2.0
     */
    @Override
    public Facelet getViewMetadataFacelet(URL url) throws IOException,
            FaceletException, FacesException, ELException
    {
        ParameterCheck.notNull("url", url);
        
        String key = url.toString();
        
        DefaultFacelet f = _viewMetadataFacelets.get(key);
        
        if (f == null || this.needsToBeRefreshed(f))
        {
            f = this._createViewMetadataFacelet(url);
            if (_refreshPeriod != NO_CACHE_DELAY)
            {
                Map<String, DefaultFacelet> newLoc = new HashMap<String, DefaultFacelet>(_viewMetadataFacelets);
                newLoc.put(key, f);
                _viewMetadataFacelets = newLoc;
            }
        }
        
        return f;
    }
}
