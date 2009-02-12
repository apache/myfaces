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
package com.sun.facelets.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.webapp.pdl.facelets.FaceletException;
import javax.faces.webapp.pdl.facelets.FaceletHandler;

import com.sun.facelets.Facelet;
import com.sun.facelets.FaceletFactory;
import com.sun.facelets.compiler.Compiler;
import com.sun.facelets.util.ParameterCheck;

/**
 * Default FaceletFactory implementation.
 * 
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletFactory.java,v 1.10 2007/04/09 01:13:17 youngm Exp $
 */
public final class DefaultFaceletFactory extends FaceletFactory
{
    protected final static Logger log = Logger.getLogger("facelets.factory");

    private final Compiler _compiler;

    private Map<String, DefaultFacelet> _facelets;

    private Map<String, URL> _relativeLocations;

    private final ResourceResolver _resolver;

    private final URL _baseUrl;

    private final long _refreshPeriod;

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
        _relativeLocations = new HashMap<String, URL>();
        _resolver = resolver;
        _baseUrl = resolver.resolveUrl("/");
        // this.location = url;
        log.fine("Using ResourceResolver: " + resolver);
        _refreshPeriod = (refreshPeriod >= 0) ? refreshPeriod * 1000 : -1;
        log.fine("Using Refresh Period: " + _refreshPeriod);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.FaceletFactory#getFacelet(java.lang.String)
     */
    public Facelet getFacelet(String uri) throws IOException, FaceletException, FacesException, ELException
    {
        URL url = (URL) _relativeLocations.get(uri);
        if (url == null)
        {
            url = this.resolveURL(_baseUrl, uri);
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
            f = this.createFacelet(url);
            if (_refreshPeriod != 0)
            {
                Map<String, DefaultFacelet> newLoc = new HashMap<String, DefaultFacelet>(_facelets);
                newLoc.put(key, f);
                _facelets = newLoc;
            }
        }
        return f;
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
        if (_refreshPeriod == 0)
            return true;
        // if set to -1, never reload
        if (_refreshPeriod == -1)
            return false;
        long ttl = facelet.getCreateTime() + _refreshPeriod;
        URL url = facelet.getSource();
        InputStream is = null;
        if (System.currentTimeMillis() > ttl)
        {
            try
            {
                URLConnection conn = url.openConnection();
                is = conn.getInputStream();
                long atl = conn.getLastModified();
                return atl == 0 || atl > ttl;
            }
            catch (Exception e)
            {
                throw new FaceletException("Error Checking Last Modified for " + facelet.getAlias(), e);
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (Exception e)
                    {
                        // do nothing
                    }
                }
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
    private DefaultFacelet createFacelet(URL url) throws IOException, FaceletException, FacesException, ELException
    {
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Creating Facelet for: " + url);
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
     * Compiler this factory uses
     * 
     * @return final Compiler instance
     */
    public Compiler getCompiler()
    {
        return _compiler;
    }

    public long getRefreshPeriod()
    {
        return _refreshPeriod;
    }
}
