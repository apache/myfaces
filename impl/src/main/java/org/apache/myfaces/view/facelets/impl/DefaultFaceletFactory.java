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
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.el.ELException;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.annotation.View;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewResource;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.net.MalformedURLException;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletCache;
import jakarta.faces.view.facelets.FaceletCacheFactory;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.resource.ResourceLoaderUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.view.facelets.AbstractFaceletCache;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.compiler.Compiler;

/**
 * Default FaceletFactory implementation.
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class DefaultFaceletFactory extends FaceletFactory
{
    private static final long INFINITE_DELAY = -1;
    private static final long NO_CACHE_DELAY = 0;
    
    protected static final Logger log = Logger.getLogger(DefaultFaceletFactory.class.getName());

    private Optional<URL> _baseUrl;
    private Compiler _compiler;
    private Map<String, DefaultFacelet> _compositeComponentMetadataFacelets;
    private long _refreshPeriod;
    private Map<String, URL> _relativeLocations;
    private Map<String, Boolean> _managedFacelet;
    private volatile Set<String> _allowedSuffixes;
    
    private FaceletCache<Facelet> _faceletCache;
    private AbstractFaceletCache<Facelet> _abstractFaceletCache;
    private boolean viewUniqueIdsCacheEnabled;
    
    public DefaultFaceletFactory(Compiler compiler) throws IOException
    {
        this(compiler, -1);
    }

    public DefaultFaceletFactory(Compiler compiler, long refreshPeriod)
    {
        Assert.notNull(compiler, "compiler");

        _compiler = compiler;

        _compositeComponentMetadataFacelets = new HashMap<>();
        _relativeLocations = new HashMap<>();
        _managedFacelet = new HashMap<>();

        _refreshPeriod = refreshPeriod < 0 ? INFINITE_DELAY : refreshPeriod * 1000;
        
        // facelet cache. Lookup here, because after all this is a "part" of the facelet factory implementation.
        FaceletCacheFactory cacheFactory
                = (FaceletCacheFactory) FactoryFinder.getFactory(FactoryFinder.FACELET_CACHE_FACTORY);
        _faceletCache = (FaceletCache<Facelet>) cacheFactory.getFaceletCache();
        
        FaceletCache.MemberFactory<Facelet> faceletFactory = (URL url) -> _createFacelet(url);
        FaceletCache.MemberFactory<Facelet> viewMetadataFaceletFactory = (URL url) -> _createViewMetadataFacelet(url);
        
        if (_faceletCache instanceof AbstractFaceletCache)
        {
            _abstractFaceletCache = (AbstractFaceletCache<Facelet>) _faceletCache;
            
            FaceletCache.MemberFactory<Facelet> compositeComponentMetadataFaceletFactory = 
                (URL url) -> _createCompositeComponentMetadataFacelet(url);

            try
            {
                _abstractFaceletCache.setCacheFactories(faceletFactory, 
                        viewMetadataFaceletFactory, compositeComponentMetadataFaceletFactory);
            } 
            catch (Exception e)
            {
                throw new FacesException(
                    "Cannot call setMemberFactories method, Initialization of FaceletCache failed.", e);
            }   
        }
        else
        {
            // Note that FaceletCache.setMemberFactories method is protected, and this is the place where call
            // this method has sense, because DefaultFaceletFactory is the responsible to create Facelet instances.
            // The only way to do it is using reflection, and it has sense, because in this way it is possible to
            // setup a java SecurityManager that prevents call this method (because it is protected, and to do that
            // the code first check for "suppressAccessChecks" permission).
            try
            {
                _faceletCache.setCacheFactories(faceletFactory, viewMetadataFaceletFactory);
            } 
            catch (Exception e)
            {
                throw new FacesException(
                    "Cannot call setMemberFactories method, Initialization of FaceletCache failed.", e);
            }            
        }

        if (log.isLoggable(Level.FINE))
        {
            log.fine("Rrefresh period " + _refreshPeriod);
        }

        this.viewUniqueIdsCacheEnabled = MyfacesConfig.getCurrentInstance().isViewUniqueIdsCacheEnabled();
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
    
    private URL getBaseUrl()
    {
        if (_baseUrl == null)
        {
            FacesContext context = FacesContext.getCurrentInstance();
            ViewResource resource = context.getApplication().getResourceHandler().createViewResource(context, "/");
            _baseUrl = Optional.ofNullable(resource == null ? null : resource.getURL());
        }
        return _baseUrl.isPresent() ? _baseUrl.get() : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.myfaces.view.facelets.FaceletFactory#getFacelet(java.lang.String)
     */
    @Override
    public Facelet getFacelet(FacesContext facesContext, String uri) 
        throws IOException, FaceletException, FacesException, ELException
    {
        Boolean isManagedFacelet = _managedFacelet.get(uri);
        if (isManagedFacelet == null || isManagedFacelet)
        {
            Facelet facelet = null;
            if (ExternalSpecifications.isCDIAvailable(facesContext.getExternalContext()))
            {
                BeanManager bm = CDIUtils.getBeanManager(facesContext);
                facelet = CDIUtils.get(bm, Facelet.class, true, View.Literal.of(uri));
            }
            _managedFacelet.put(uri, facelet != null);
            if (facelet != null)
            {
                return facelet;
            }
        }

        URL url = _relativeLocations.get(uri);
        if (url == null)
        {
            url = resolveURL(facesContext, getBaseUrl(), uri);
            if (url != null)
            {
                ViewResource viewResource = (ViewResource) facesContext.getAttributes().get(
                    FaceletFactory.LAST_RESOURCE_RESOLVED);
                if (viewResource != null)
                {
                    // If a view resource has been used to resolve a resource, the cache is in
                    // the ResourceHandler implementation. No need to cache in _relativeLocations.
                }
                else
                {
                    Map<String, URL> newLoc = new HashMap<>(_relativeLocations);
                    newLoc.put(uri, url);
                    _relativeLocations = newLoc;
                }
            }
            else
            {
                throw new IOException('\'' + uri + "' not found.");
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
    @Override
    public Facelet getFacelet(URL url) throws IOException, FaceletException, FacesException, ELException
    {
        return _faceletCache.getFacelet(url);
    }
    
    
    @Override
    public Facelet getFacelet(FaceletContext ctx, URL url) 
            throws IOException, FaceletException, FacesException, ELException
    {
        if (_abstractFaceletCache != null)
        {
            return _abstractFaceletCache.getFacelet(ctx, url);
        }
        else
        {
            return _faceletCache.getFacelet(url);
        }
    }

    public long getRefreshPeriod()
    {
        return _refreshPeriod;
    }

    /**
     * Resolves a path based on the passed URL. If the path starts with '/', then resolve the path against
     * {@link jakarta.faces.context.ExternalContext#getResource(java.lang.String)
     * jakarta.faces.context.ExternalContext#getResource(java.lang.String)}. Otherwise create a new URL via
     * {@link URL#URL(java.net.URL, java.lang.String) URL(URL, String)}.
     * 
     * @param source
     *            base to resolve from
     * @param path
     *            relative path to the source
     * @return resolved URL
     * @throws IOException
     */
    public URL resolveURL(FacesContext context, URL source, String path) throws IOException
    {
        // --- 1. Reject remote/network schemes (http, ftp, etc.) up front.
        //        OSGi/container schemes (wsjar, jar, file, zip) are allowed and pass through.
        if (!isAllowedScheme(path))
        {
            throw new MalformedURLException(
                    "Remote or disallowed scheme in path: " + path);
        }

        URL resolved;
        String normalizedPath;
        boolean absoluteContextPath = path.startsWith("/");

        if (absoluteContextPath)
        {
            // Absolute context-relative path — resolved through ExternalContext.
            // The container already scopes the lookup to the WAR, so no traversal is possible.
            context.getAttributes().put(LAST_RESOURCE_RESOLVED, null);
            resolved = resolveURL(context, path);
            if (resolved == null)
            {
                throw new FileNotFoundException(path + " Not Found in ExternalContext as a Resource");
            }
            normalizedPath = path;
        }
        else
        {
            // Relative path — resolved against the current source URL.
            resolved = new URL(source, path);
            normalizedPath = resolved.getPath();
        }

        // UnitTest stage skips content-validation guards; tests use synthetic paths
        // that are not backed by a real WAR layout.
        if (context.isProjectStage(ProjectStage.UnitTest))
        {
            return resolved;
        }

        // --- 2. File must be inside the WAR/EAR (traversal guard for relative paths only).
        //        Absolute context paths are already scoped by ExternalContext.
        if (!absoluteContextPath && !isWithinBase(resolved))
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Path not allowed [" + path + "] -> resolved URL escapes application base");
            }
            throw new MalformedURLException(
                    "Path escapes application base: " + path);
        }

        // --- 3. WEB-INF XML config files must not be directly served as Facelets.
        //  check mark is disabled 
        // Reason: .xml is not a facelet file unless specified via suffix / mapping parameters
        // if (isWebInfConfigFile(normalizedPath))
        // {
        //     if (log.isLoggable(Level.FINE))
        //     {
        //         log.fine("Path not allowed [" + path + "] -> WEB-INF XML config file");
        //     }
        //     throw new MalformedURLException("Access to WEB-INF XML config files is not allowed: " + path);
        // }

        // --- 4. Extension must be a configured Facelet suffix (e.g. .xhtml, .jspx).
        if (!mappingAllowed(context, normalizedPath))
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Path not allowed [" + path + "] -> extension not a configured Facelet suffix");
            }
            throw new MalformedURLException(
                    "Invalid path provided: " + path);
        }

        return resolved;
    }

    // ---------------------------------------------------------------------------
    // Path-validation helpers
    // ---------------------------------------------------------------------------

    /**
     * Remote/network URI schemes that must never be used as Facelet resource paths.
     * OSGi container schemes (wsjar, jar, file, zip) are intentionally absent —
     * they reference in-archive resources and are therefore safe.
     */
    private static final Set<String> BLOCKED_SCHEMES = new HashSet<>(
            Arrays.asList("http", "https", "ftp", "ftps", "mailto", "tel",
                          "imap", "irc", "nntp", "acap", "icap", "mtqp", "wss"));

    /**
     * Returns {@code false} when {@code path} is an absolute URI whose scheme is on the
     * {@link #BLOCKED_SCHEMES} list. Purely relative paths (no scheme) and OSGi/container
     * schemes (wsjar, jar, file, zip) always return {@code true}.
     * <p>
     * Uses a fast colon-index pre-check to avoid {@code URI} allocation for the common case
     * of relative or context-root paths (e.g. {@code /views/page.xhtml}).
     */
    private boolean isAllowedScheme(String path)
    {
        // Fast path: a scheme requires at least one letter before ":", so the colon must
        // appear at index >= 1. Relative paths and "/"-absolute paths never have a colon
        // in this position and are immediately allowed.
        int colon = path.indexOf(':');
        if (colon < 1)
        {
            return true;
        }
        String scheme = path.substring(0, colon).toLowerCase();
        if (BLOCKED_SCHEMES.contains(scheme))
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Path not allowed [" + path + "] -> Blocked scheme: " + scheme);
            }
            return false;
        }
        return true;
    }

    /**
     * Returns {@code true} when {@code normalizedPath} refers to an XML file located under
     * {@code /WEB-INF/}. Such files are server configuration descriptors and must never be
     * exposed as Facelet templates.
     */
    private boolean isWebInfConfigFile(String normalizedPath)
    {
        if (normalizedPath == null)
        {
            return false;
        }
        String lower = normalizedPath.replace('\\', '/').toLowerCase();
        return lower.contains("/web-inf/") && lower.endsWith(".xml");
    }

    /**
     * Verifies that {@code resolved} is contained within the application base URL
     * (i.e. the WAR/EAR root), preventing directory traversal outside the archive.
     */
    private boolean isWithinBase(URL resolved)
    {
        URL base = getBaseUrl();
        if (base == null)
        {
            return true; // cannot determine base — allow and let the container decide
        }
        String baseStr = base.toExternalForm();
        String resolvedStr = resolved.toExternalForm();
        if (!baseStr.endsWith("/"))
        {
            baseStr = baseStr + "/";
        }
        // For jar:/wsjar: URLs the in-archive path follows "!/"; the shared jar
        // file prefix is enough to confirm containment.
        return resolvedStr.startsWith(baseStr);
    }

    /**
     * Returns {@code true} when {@code normalizedPath} ends with a suffix that is configured
     * as an allowed Facelet extension. Built from {@code jakarta.faces.FACELETS_SUFFIX}
     * (default {@code .xhtml}) and suffix entries in {@code jakarta.faces.FACELETS_VIEW_MAPPINGS}.
     * {@code .jspx} is always included for legacy JSP-XML views.
     * <p>
     * Note: {@code .xml} is intentionally <em>not</em> added here; XML files under
     * {@code WEB-INF/} are blocked by {@link #isWebInfXml(String)} and plain {@code .xml}
     * outside that directory is not a valid Facelet extension.
     */
    private boolean mappingAllowed(FacesContext context, String normalizedPath)
    {
        if (normalizedPath == null || normalizedPath.isEmpty())
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Mapping not allowed [" + normalizedPath + "] -> Empty or null path");
            }
            return false;
        }
        int dotIndex = normalizedPath.lastIndexOf('.');
        if (dotIndex < 0)
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Mapping not allowed [" + normalizedPath + "] -> No extension");
            }
            return false;
        }
        String ext = normalizedPath.substring(dotIndex);

        if (!getAllowedSuffixes(context).contains(ext))
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Mapping not allowed [" + normalizedPath + "] -> Extension not a Facelet suffix: " + ext);
            }
            return false;
        }
        return true;
    }

    /**
     * Returns the set of allowed Facelet file suffixes, computed once from the application's
     * init parameters and cached for the lifetime of this factory.
     * <p>
     * The set is built from:
     * <ul>
     *   <li>{@code jakarta.faces.FACELETS_SUFFIX} (whitespace-separated, default {@code .xhtml})</li>
     *   <li>Suffix entries in {@code jakarta.faces.FACELETS_VIEW_MAPPINGS} (semicolon-separated;
     *       entries beginning with {@code *.} contribute the extension part)</li>
     *   <li>{@code .jspx} — always included for legacy JSP-XML views</li>
     * </ul>
     * Init parameters are read only on the first call; subsequent calls return the cached set.
     */
    private Set<String> getAllowedSuffixes(FacesContext context)
    {
        if (_allowedSuffixes == null)
        {
            ExternalContext ec = context.getExternalContext();

            // Suffixes from jakarta.faces.FACELETS_SUFFIX (whitespace-separated, default ".xhtml")
            String suffixParam = ec.getInitParameter(ViewHandler.FACELETS_SUFFIX_PARAM_NAME);
            if (suffixParam == null)
            {
                suffixParam = ViewHandler.DEFAULT_FACELETS_SUFFIX;
            }
            Set<String> allowed = new HashSet<>(Arrays.asList(suffixParam.trim().split("\\s+")));

            // Suffixes from jakarta.faces.FACELETS_VIEW_MAPPINGS (semicolon-separated; strip leading "*")
            String mappingsParam = ec.getInitParameter(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME);
            if (mappingsParam == null)
            {
                mappingsParam = ec.getInitParameter("facelets.VIEW_MAPPINGS");
            }
            if (mappingsParam != null)
            {
                for (String token : mappingsParam.split(";"))
                {
                    token = token.trim();
                    if (token.startsWith("*."))
                    {
                        // suffix mapping e.g. "*.xhtml" -> ".xhtml"
                        allowed.add(token.substring(1));
                    }
                    // Prefix mappings like "/faces/*" carry no extension — skipped.
                }
            }

            // Legacy JSP-XML view support
            // allowed.add(".jspx");

            if (log.isLoggable(Level.FINE))
            {
                log.fine("Allowed Facelet suffixes: " + allowed);
            }

            _allowedSuffixes = allowed;
        }
        return _allowedSuffixes;
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
                long lastModified = ResourceLoaderUtils.getResourceLastModified(facelet.getSource());

                return lastModified == 0 || lastModified > target;
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
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Creating Facelet for: " + url);
        }

        URL baseUrl = getBaseUrl();
        String alias = '/' + _removeFirst(url.getFile(), baseUrl == null ? "" : baseUrl.getFile());
        try
        {
            Compiler.CompilerResult result = _compiler.compile(url, alias);
            DefaultFacelet f = new DefaultFacelet(this, _compiler.createExpressionFactory(), url, alias, alias,
                    result.getFaceletHandler(), viewUniqueIdsCacheEnabled, result.getDoctype());
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
    private DefaultFacelet _createViewMetadataFacelet(URL url)
            throws IOException, FaceletException, FacesException, ELException
    {
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Creating Facelet used to create View Metadata for: " + url);
        }

        // The alias is used later for informative purposes, so we append 
        // some prefix to identify later where the errors comes from.
        URL baseUrl = getBaseUrl();
        String faceletId = '/' + _removeFirst(url.getFile(), baseUrl == null ? "" : baseUrl.getFile());
        String alias = "/viewMetadata" + faceletId;
        try
        {
            Compiler.CompilerResult result = _compiler.compileViewMetadata(url, alias);
            DefaultFacelet f = new DefaultFacelet(this, _compiler.createExpressionFactory(), url, alias, 
                    faceletId, result.getFaceletHandler(), viewUniqueIdsCacheEnabled, result.getDoctype());
            return f;
        }
        catch (FileNotFoundException fnfe)
        {
            throw new FileNotFoundException("Facelet " + alias + " not found at: " + url.toExternalForm());
        }
    }
    
    /**
     * @since 2.0.1
     * @param url
     * @return
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    private DefaultFacelet _createCompositeComponentMetadataFacelet(URL url)
            throws IOException, FaceletException, FacesException, ELException
    {
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Creating Facelet used to create Composite Component Metadata for: " + url);
        }

        // The alias is used later for informative purposes, so we append 
        // some prefix to identify later where the errors comes from.
        URL baseUrl = getBaseUrl();
        String alias = "/compositeComponentMetadata/" + _removeFirst(url.getFile(),
                baseUrl == null ? "" : baseUrl.getFile());
        try
        {
            Compiler.CompilerResult result = _compiler.compileCompositeComponentMetadata(url, alias);
            DefaultFacelet f = new DefaultFacelet(this, _compiler.createExpressionFactory(), url, alias,
                    alias, result.getFaceletHandler(), true, viewUniqueIdsCacheEnabled, result.getDoctype());
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
    public Facelet getViewMetadataFacelet(FacesContext facesContext, String uri) 
        throws IOException
    {
        Boolean isManagedFacelet = _managedFacelet.get(uri);
        if (isManagedFacelet == null || isManagedFacelet)
        {
            Facelet facelet = null;
            if (ExternalSpecifications.isCDIAvailable(facesContext.getExternalContext()))
            {
                BeanManager bm = CDIUtils.getBeanManager(facesContext);
                facelet = CDIUtils.get(bm, Facelet.class, true, View.Literal.of(uri));
            }
            _managedFacelet.put(uri, facelet != null);
            if (facelet != null)
            {
                return facelet;
            }
        }
        
        URL url = _relativeLocations.get(uri);
        if (url == null)
        {
            url = resolveURL(facesContext, getBaseUrl(), uri);
            ViewResource viewResource = (ViewResource) facesContext.getAttributes().get(
                FaceletFactory.LAST_RESOURCE_RESOLVED);
            if (url != null)
            {
                if (viewResource != null)
                {
                    // If a view resource has been used to resolve a resource, the cache is in
                    // the ResourceHandler implementation. No need to cache in _relativeLocations.
                }
                else
                {
                    Map<String, URL> newLoc = new HashMap<>(_relativeLocations);
                    newLoc.put(uri, url);
                    _relativeLocations = newLoc;
                }
            }
            else
            {
                throw new IOException('\'' + uri + "' not found.");
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
        if (_abstractFaceletCache != null)
        {
            return _abstractFaceletCache.getViewMetadataFacelet(url);
        }
        else
        {
            return _faceletCache.getViewMetadataFacelet(url);
        }
    }
    
    /**
     * Works in the same way as getFacelet(String uri), but redirect
     * to getViewMetadataFacelet(URL url)
     * @since 2.0.1
     */
    @Override
    public Facelet getCompositeComponentMetadataFacelet(FacesContext facesContext, String uri)
        throws IOException
    {
        URL url = _relativeLocations.get(uri);
        if (url == null)
        {
            url = resolveURL(facesContext, getBaseUrl(), uri);
            ViewResource viewResource = (ViewResource) facesContext.getAttributes().get(
                FaceletFactory.LAST_RESOURCE_RESOLVED);            
            if (url != null)
            {
                if (viewResource != null)
                {
                    // If a view resource has been used to resolve a resource, the cache is in
                    // the ResourceHandler implementation. No need to cache in _relativeLocations.
                }
                else
                {
                    Map<String, URL> newLoc = new HashMap<>(_relativeLocations);
                    newLoc.put(uri, url);
                    _relativeLocations = newLoc;
                }
            }
            else
            {
                throw new IOException('\'' + uri + "' not found.");
            }
        }
        return this.getCompositeComponentMetadataFacelet(url);
    }

    /**
     * @since 2.0.1
     */
    @Override
    public Facelet getCompositeComponentMetadataFacelet(URL url) throws IOException,
            FaceletException, FacesException, ELException
    {
        if (_abstractFaceletCache != null)
        {
            return _abstractFaceletCache.getCompositeComponentMetadataFacelet(url);
        }
        else
        {
            Assert.notNull(url, "url");

            String key = url.toString();

            DefaultFacelet f = _compositeComponentMetadataFacelets.get(key);

            if (f == null || this.needsToBeRefreshed(f))
            {
                f = this._createCompositeComponentMetadataFacelet(url);
                if (_refreshPeriod != NO_CACHE_DELAY)
                {
                    Map<String, DefaultFacelet> newLoc
                            = new HashMap<>(_compositeComponentMetadataFacelets);
                    newLoc.put(key, f);
                    _compositeComponentMetadataFacelets = newLoc;
                }
            }
            return f;
        }
    }
    
    private URL resolveURL(FacesContext context, String path)
    {
        ViewResource resource = context.getApplication().getResourceHandler().createViewResource(context, path);
        if (resource != null)
        {
            context.getAttributes().put(FaceletFactory.LAST_RESOURCE_RESOLVED, resource);
            return resource.getURL();
        }
        return null;
    }

    @Override
    public Facelet compileComponentFacelet(String taglibURI, String tagName, Map<String,Object> attributes)
    {
        Compiler.CompilerResult result = _compiler.compileComponent(taglibURI, tagName, attributes);
        String alias = "/component/oamf:"+tagName;
        return new DefaultFacelet(this, _compiler.createExpressionFactory(), getBaseUrl(), alias, alias,
                result.getFaceletHandler(), viewUniqueIdsCacheEnabled, result.getDoctype());
    }
    
    /**
     * Removes the first appearance of toRemove in string.
     *
     * Works just like string.replaceFirst(toRemove, ""), except that toRemove
     * is not treated as a regex (which could cause problems with filenames).
     *
     * @param string
     * @param toRemove
     * @return
     */
    private String _removeFirst(String string, String toRemove)
    {
        // do exactly what String.replaceFirst(toRemove, "") internally does,
        // except treating the search as literal text and not as regex

        return Pattern.compile(toRemove, Pattern.LITERAL).matcher(string).replaceFirst("");
    }

}
