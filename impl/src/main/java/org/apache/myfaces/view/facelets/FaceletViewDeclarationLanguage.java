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
package org.apache.myfaces.view.facelets;

import java.beans.BeanInfo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewMetadata;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.DefaultViewHandlerSupport;
import org.apache.myfaces.application.ViewHandlerSupport;
import org.apache.myfaces.shared_impl.util.StringUtils;
import org.apache.myfaces.view.ViewDeclarationLanguageBase;
import org.apache.myfaces.view.ViewMetadataBase;
import org.apache.myfaces.view.facelets.FaceletViewHandler.NullWriter;
import org.apache.myfaces.view.facelets.compiler.Compiler;
import org.apache.myfaces.view.facelets.compiler.SAXCompiler;
import org.apache.myfaces.view.facelets.compiler.TagLibraryConfig;
import org.apache.myfaces.view.facelets.impl.DefaultFaceletFactory;
import org.apache.myfaces.view.facelets.impl.DefaultResourceResolver;
import org.apache.myfaces.view.facelets.impl.ResourceResolver;
import org.apache.myfaces.view.facelets.tag.TagDecorator;
import org.apache.myfaces.view.facelets.tag.ui.UIDebug;
import org.apache.myfaces.view.facelets.util.DevTools;
import org.apache.myfaces.view.facelets.util.ReflectionUtil;

/**
 * This class represents the abstraction of Facelets as a ViewDeclarationLanguage.
 * 
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-21 14:57:08 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class FaceletViewDeclarationLanguage extends ViewDeclarationLanguageBase
{
    private static final Log log = LogFactory.getLog(FaceletViewDeclarationLanguage.class);

    public static final String CHARACTER_ENCODING_KEY = "javax.faces.request.charset";

    public final static long DEFAULT_REFRESH_PERIOD = 2;

    public final static String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    public final static String PARAM_BUFFER_SIZE = "facelets.BUFFER_SIZE";

    public final static String PARAM_BUILD_BEFORE_RESTORE = "facelets.BUILD_BEFORE_RESTORE";

    public final static String PARAM_DECORATORS = "facelets.DECORATORS";

    public final static String PARAM_ENCODING = "facelets.Encoding";

    public final static String PARAM_LIBRARIES = "facelets.LIBRARIES";

    public final static String PARAM_REFRESH_PERIOD = "facelets.REFRESH_PERIOD";

    public final static String PARAM_RESOURCE_RESOLVER = "facelets.RESOURCE_RESOLVER";

    public final static String PARAM_SKIP_COMMENTS = "facelets.SKIP_COMMENTS";

    public final static String PARAM_VIEW_MAPPINGS = "facelets.VIEW_MAPPINGS";
    
    /**
     * Marker to indicate tag handlers the view currently being built is using
     * partial state saving and it is necessary to call UIComponent.markInitialState
     * after component instances are populated. 
     */
    public final static String MARK_INITIAL_STATE_KEY = "org.apache.myfaces.MARK_INITIAL_STATE";

    private final static String STATE_KEY = "<!--@@JSF_FORM_STATE_MARKER@@-->";

    private final static int STATE_KEY_LEN = STATE_KEY.length();

    private int _bufferSize;

    // This param evolve in jsf 2.0 to partial state saving
    //private boolean _buildBeforeRestore = false;

    private ViewHandlerSupport _cachedViewHandlerSupport;

    private String _defaultSuffix;

    private FaceletFactory _faceletFactory;

    private StateManagementStrategy stateMgmtStrategy;
    
    private boolean _partialStateSaving;
    
    private Set<String> _viewIds;

    /**
     * 
     */
    public FaceletViewDeclarationLanguage(FacesContext context)
    {
        initialize(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildView(FacesContext context, UIViewRoot view) throws IOException
    {
        if (isFilledView(context, view))
            return;
        
        // setup our viewId
        String renderedViewId = getRenderedViewId(context, view.getViewId());

        view.setViewId(renderedViewId);

        if (log.isTraceEnabled())
        {
            log.trace("Building View: " + renderedViewId);
        }

        boolean usePartialStateSavingOnThisView = _usePartialStateSavingOnThisView(renderedViewId);
        
        if (usePartialStateSavingOnThisView)
        {
            // Before apply we need to make sure the current view has
            // a clientId that will be used as a key to save and restore
            // the current view. Note that getClientId is never called (or used)
            // from UIViewRoot.
            if (view.getId() == null)
            {
                view.setId(view.createUniqueId(context,null));
            }
            
            //Add a key to indicate ComponentTagHandlerDelegate to 
            //call UIComponent.markInitialState after it is populated
            context.getAttributes().put(MARK_INITIAL_STATE_KEY, Boolean.TRUE);
        }

        // populate UIViewRoot
        _getFacelet(renderedViewId).apply(context, view);
        
        // set this view as filled
        setFilledView(context, view);
        
        // Suscribe listeners if we are using partialStateSaving
        if (usePartialStateSavingOnThisView)
        {
            // UIViewRoot.markInitialState() is not called because it does
            // not have a facelet tag handler class that create it, instead
            // new instances are created programatically.
            view.markInitialState();
            
            //Remove the key that indicate we need to call UIComponent.markInitialState
            //on the current tree
            context.getAttributes().remove(MARK_INITIAL_STATE_KEY);
            
            ((DefaultFaceletsStateManagementStrategy) stateMgmtStrategy).suscribeListeners(view);
        }
    }
    
    private boolean isFilledView(FacesContext context, UIViewRoot view)
    {
        return context.getAttributes().containsKey(view.toString());
    }
    
    private void setFilledView(FacesContext context, UIViewRoot view)
    {
        context.getAttributes().put(view.toString(), Boolean.TRUE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BeanInfo getComponentMetadata(FacesContext context, Resource componentResource)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getScriptComponentResource(FacesContext context, Resource componentResource)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId)
    {
        // Use partial state saving strategy only if javax.faces.PARTIAL_STATE_SAVING is "true" and
        // the current view is not on javax.faces.FULL_STATE_SAVING_VIEW_IDS.
        return _usePartialStateSavingOnThisView(viewId) ? stateMgmtStrategy : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId)
    {
        return new FaceletViewMetadata(viewId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException
    {
        if (!view.isRendered())
        {
            return;
        }

        // log request
        if (log.isDebugEnabled())
        {
            log.debug("Rendering View: " + view.getViewId());
        }

        try
        {
            // build view - but not if we're in "buildBeforeRestore"
            // land and we've already got a populated view. Note
            // that this optimizations breaks if there's a "c:if" in
            // the page that toggles as a result of request processing -
            // should that be handled? Or
            // is this optimization simply so minor that it should just
            // be trimmed altogether?
            if (!isFilledView(context, view))
            {
                buildView(context, view);
            }

            // setup writer and assign it to the context
            ResponseWriter origWriter = createResponseWriter(context);

            StateWriter stateWriter = new StateWriter(origWriter, 1024);
            try
            {
                ResponseWriter writer = origWriter.cloneWithWriter(stateWriter);
                try
                {
                    context.setResponseWriter(writer);

                    // force creation of session if saving state there
                    StateManager stateMgr = context.getApplication().getStateManager();
                    if (!stateMgr.isSavingStateInClient(context))
                    {
                        context.getExternalContext().getSession(true);
                    }

                    // render the view to the response
                    writer.startDocument();

                    view.encodeAll(context);

                    writer.endDocument();

                    // finish writing
                    writer.close();

                    boolean writtenState = stateWriter.isStateWritten();
                    // flush to origWriter
                    if (writtenState)
                    {
                        String content = stateWriter.getAndResetBuffer();
                        int end = content.indexOf(STATE_KEY);
                        // See if we can find any trace of the saved state.
                        // If so, we need to perform token replacement
                        if (end >= 0)
                        {
                            // save state
                            Object stateObj = stateMgr.saveView(context);
                            String stateStr;
                            if (stateObj == null)
                            {
                                stateStr = null;
                            }
                            else
                            {
                                stateMgr.writeState(context, stateObj);
                                stateStr = stateWriter.getAndResetBuffer();
                            }

                            int start = 0;

                            while (end != -1)
                            {
                                origWriter.write(content, start, end - start);
                                if (stateStr != null)
                                {
                                    origWriter.write(stateStr);
                                }
                                start = end + STATE_KEY_LEN;
                                end = content.indexOf(STATE_KEY, start);
                            }

                            origWriter.write(content, start, content.length() - start);
                            // No trace of any saved state, so we just need to flush
                            // the buffer
                        }
                        else
                        {
                            origWriter.write(content);
                        }
                    }
                }
                finally
                {
                    // The Facelets implementation must close the writer used to write the response
                    writer.close();
                }
            }
            finally
            {
                stateWriter.release();
            }
        }
        catch (FileNotFoundException fnfe)
        {
            handleFaceletNotFound(context, view.getViewId());
        }
        catch (Exception e)
        {
            handleRenderException(context, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        if (UIDebug.debugRequest(context))
        {
            return new UIViewRoot();
        }
        //else if (!_buildBeforeRestore)
        //{
            //return super.restoreView(context, viewId);
        //}
        //else
        //{
            // TODO: VALIDATE - Is _buildBeforeRestore relevant at all for 2.0? -= SL =-
            // ANS: buildBeforeRestore evolved to partial state saving, so this logic 
            // is now on StateManagerStrategy implementation -= Leo U =- 
            // CJH: uncommenting for now, seems to work.
        
            UIViewRoot viewRoot = createView(context, viewId);

            context.setViewRoot(viewRoot);

            try
            {
                buildView(context, viewRoot);
            }
            catch (IOException ioe)
            {
                log.error("Error Building View", ioe);
            }

            Application application = context.getApplication();

            ViewHandler applicationViewHandler = application.getViewHandler();

            String renderKitId = applicationViewHandler.calculateRenderKitId(context);

            application.getStateManager().restoreView(context, viewId, renderKitId);

            return viewRoot;
        //}
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String calculateViewId(FacesContext context, String viewId)
    {
        if (_cachedViewHandlerSupport == null)
        {
            _cachedViewHandlerSupport = new DefaultViewHandlerSupport();
        }

        return _cachedViewHandlerSupport.calculateViewId(context, viewId);
    }

    /**
     * Creates the Facelet page compiler.
     * 
     * @param context
     *            the current FacesContext
     * 
     * @return the application's Facelet page compiler
     */
    protected Compiler createCompiler(FacesContext context)
    {
        Compiler compiler = new SAXCompiler();

        loadLibraries(context, compiler);
        loadDecorators(context, compiler);
        loadOptions(context, compiler);

        return compiler;
    }

    /**
     * Creates a FaceletFactory instance using the specified compiler.
     * 
     * @param context
     *            the current FacesContext
     * @param compiler
     *            the compiler to be used by the factory
     * 
     * @return the factory used by this VDL to load pages
     */
    protected FaceletFactory createFaceletFactory(FacesContext context, Compiler compiler)
    {
        ExternalContext eContext = context.getExternalContext();

        // refresh period
        long refreshPeriod = _getLongParameter(eContext, PARAM_REFRESH_PERIOD, DEFAULT_REFRESH_PERIOD);

        // resource resolver
        ResourceResolver resolver = _getInstanceParameter(eContext, PARAM_RESOURCE_RESOLVER, null);
        if (resolver == null)
        {
            resolver = new DefaultResourceResolver();
        }

        return new DefaultFaceletFactory(compiler, resolver, refreshPeriod);
    }

    protected ResponseWriter createResponseWriter(FacesContext context) throws IOException, FacesException
    {
        ExternalContext extContext = context.getExternalContext();
        RenderKit renderKit = context.getRenderKit();
        // Avoid a cryptic NullPointerException when the renderkit ID
        // is incorrectly set
        if (renderKit == null)
        {
            String id = context.getViewRoot().getRenderKitId();
            throw new IllegalStateException("No render kit was available for id \"" + id + "\"");
        }

        ServletResponse response = (ServletResponse) extContext.getResponse();

        // set the buffer for content
        if (_bufferSize != -1)
        {
            response.setBufferSize(_bufferSize);
        }

        // get our content type
        String contentType = (String) extContext.getRequestMap().get("facelets.ContentType");

        // get the encoding
        String encoding = (String) extContext.getRequestMap().get("facelets.Encoding");

        ResponseWriter writer;
        // append */* to the contentType so createResponseWriter will succeed no matter
        // the requested contentType.
        if (contentType != null && !contentType.equals("*/*"))
        {
            contentType += ",*/*";
        }
        // Create a dummy ResponseWriter with a bogus writer,
        // so we can figure out what content type the ReponseWriter
        // is really going to ask for
        try
        {
            writer = renderKit.createResponseWriter(NullWriter.Instance, contentType, encoding);
        }
        catch (IllegalArgumentException e)
        {
            // Added because of an RI bug prior to 1.2_05-b3. Might as well leave it in case other
            // impls have the same problem. https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=613
            log.trace("The impl didn't correctly handled '*/*' in the content type list.  Trying '*/*' directly.");
            writer = renderKit.createResponseWriter(NullWriter.Instance, "*/*", encoding);
        }

        // Override the JSF provided content type if necessary
        contentType = getResponseContentType(context, writer.getContentType());
        encoding = getResponseEncoding(context, writer.getCharacterEncoding());

        // apply them to the response
        response.setContentType(contentType + "; charset=" + encoding);

        // removed 2005.8.23 to comply with J2EE 1.3
        // response.setCharacterEncoding(encoding);

        // Now, clone with the real writer
        writer = writer.cloneWithWriter(response.getWriter());

        return writer;
    }

    protected String getDefaultSuffix(FacesContext context) throws FacesException
    {
        if (_defaultSuffix == null)
        {
            ExternalContext eContext = context.getExternalContext();

            String viewSuffix = eContext.getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);

            _defaultSuffix = viewSuffix == null ? ViewHandler.DEFAULT_FACELETS_SUFFIX : viewSuffix;
        }

        return _defaultSuffix;
    }

    protected String getRenderedViewId(FacesContext context, String actionId)
    {
        ExternalContext eContext = context.getExternalContext();
        String viewId = actionId;
        if (eContext.getRequestPathInfo() == null)
        {
            String viewSuffix = getDefaultSuffix(context);

            StringBuilder builder = new StringBuilder(viewId);

            viewId = builder.replace(viewId.lastIndexOf('.'), viewId.length(), viewSuffix).toString();
        }

        if (log.isTraceEnabled())
        {
            log.trace("ActionId -> ViewId: " + actionId + " -> " + viewId);
        }

        return viewId;
    }

    /**
     * Generate the content type
     * 
     * @param context
     * @param orig
     * @return
     */
    protected String getResponseContentType(FacesContext context, String orig)
    {
        String contentType = orig;

        // see if we need to override the contentType
        Map<String, Object> m = context.getExternalContext().getRequestMap();
        if (m.containsKey("facelets.ContentType"))
        {
            contentType = (String) m.get("facelets.ContentType");
            if (log.isTraceEnabled())
            {
                log.trace("Facelet specified alternate contentType '" + contentType + "'");
            }
        }

        // safety check
        if (contentType == null)
        {
            contentType = "text/html";
            log.trace("ResponseWriter created had a null ContentType, defaulting to text/html");
        }

        return contentType;
    }

    /**
     * Generate the encoding
     * 
     * @param context
     * @param orig
     * @return
     */
    protected String getResponseEncoding(FacesContext context, String orig)
    {
        String encoding = orig;

        // see if we need to override the encoding
        Map<String, Object> m = context.getExternalContext().getRequestMap();
        Map<String, Object> sm = context.getExternalContext().getSessionMap();

        // 1. check the request attribute
        if (m.containsKey(PARAM_ENCODING))
        {
            encoding = (String) m.get(PARAM_ENCODING);
            if (encoding != null && log.isTraceEnabled())
            {
                log.trace("Facelet specified alternate encoding '" + encoding + "'");
            }
            
            sm.put(CHARACTER_ENCODING_KEY, encoding);
        }

        // 2. get it from request
        Object request = context.getExternalContext().getRequest();
        if (encoding == null && request instanceof ServletRequest)
        {
            encoding = ((ServletRequest) request).getCharacterEncoding();
        }

        // 3. get it from the session
        if (encoding == null)
        {
            encoding = (String) sm.get(CHARACTER_ENCODING_KEY);
            if (encoding != null && log.isTraceEnabled())
            {
                log.trace("Session specified alternate encoding '" + encoding + "'");
            }
        }

        // 4. default it
        if (encoding == null)
        {
            encoding = DEFAULT_CHARACTER_ENCODING;
            if (log.isTraceEnabled())
            {
                log.trace("ResponseWriter created had a null CharacterEncoding, defaulting to " + encoding);
            }
        }

        return encoding;
    }

    protected void handleFaceletNotFound(FacesContext context, String viewId) throws FacesException, IOException
    {
        String actualId = context.getApplication().getViewHandler().getActionURL(context, viewId);
        Object respObj = context.getExternalContext().getResponse();
        if (respObj instanceof HttpServletResponse)
        {
            HttpServletResponse respHttp = (HttpServletResponse) respObj;
            respHttp.sendError(HttpServletResponse.SC_NOT_FOUND, actualId);
            context.responseComplete();
        }
    }

    protected void handleRenderException(FacesContext context, Exception e) throws IOException, ELException,
            FacesException
    {
        Object resp = context.getExternalContext().getResponse();
        
        UIViewRoot root = context.getViewRoot();
        StringBuffer sb = new StringBuffer(64);
        sb.append("Error Rendering View");
        if (root != null)
        {
            sb.append('[');
            sb.append(root.getViewId());
            sb.append(']');
        }
        
        log.error(sb.toString(), e);

        // handle dev response
        if (_isDevelopmentMode(context) && !context.getResponseComplete() && resp instanceof HttpServletResponse)
        {
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            if (!httpResp.isCommitted())
            {
                httpResp.reset();
                httpResp.setContentType("text/html; charset=UTF-8");
                Writer w = httpResp.getWriter();
                DevTools.debugHtml(w, context, e);
                w.flush();
                context.responseComplete();
            }
        }
        else if (e instanceof RuntimeException)
        {
            throw (RuntimeException) e;
        }
        else if (e instanceof IOException)
        {
            throw (IOException) e;
        }
        else
        {
            throw new FacesException(e.getMessage(), e);
        }
    }

    /**
     * Initialize the ViewHandler during its first request.
     */
    protected void initialize(FacesContext context)
    {
        log.trace("Initializing");

        Compiler compiler = createCompiler(context);

        _faceletFactory = createFaceletFactory(context, compiler);

        ExternalContext eContext = context.getExternalContext();
        _initializeBuffer(eContext);
        _initializeMode(eContext);

        if (_partialStateSaving)
        {
            stateMgmtStrategy = new DefaultFaceletsStateManagementStrategy(this);
        }
        
        log.trace("Initialization Successful");
    }

    /**
     * Load the various decorators for Facelets.
     * 
     * @param context
     *            the current FacesContext
     * @param compiler
     *            the page compiler
     */
    protected void loadDecorators(FacesContext context, Compiler compiler)
    {
        String param = _getStringParameter(context.getExternalContext(), PARAM_DECORATORS);
        if (param != null)
        {
            for (String decorator : param.split(";"))
            {
                try
                {
                    compiler.addTagDecorator((TagDecorator) ReflectionUtil.forName(decorator).newInstance());
                    if (log.isDebugEnabled())
                    {
                        log.debug("Successfully loaded decorator: " + decorator);
                    }
                }
                catch (Exception e)
                {
                    log.error("Error Loading decorator: " + decorator, e);
                }
            }
        }
    }

    /**
     * Load the various tag libraries for Facelets.
     * 
     * @param context
     *            the current FacesContext
     * @param compiler
     *            the page compiler
     */
    protected void loadLibraries(FacesContext context, Compiler compiler)
    {
        ExternalContext eContext = context.getExternalContext();

        String param = _getStringParameter(eContext, PARAM_LIBRARIES);
        if (param != null)
        {
            for (String library : param.split(";"))
            {
                try
                {
                    URL src = eContext.getResource(library.trim());
                    if (src == null)
                    {
                        throw new FileNotFoundException(library);
                    }

                    compiler.addTagLibrary(TagLibraryConfig.create(src));
                    if (log.isDebugEnabled())
                    {
                        log.debug("Successfully loaded library: " + library);
                    }
                }
                catch (IOException e)
                {
                    log.error("Error Loading library: " + library, e);
                }
            }
        }
    }

    /**
     * Load the various options for Facelets compiler. Currently only comment skipping is supported.
     * 
     * @param context
     *            the current FacesContext
     * @param compiler
     *            the page compiler
     */
    protected void loadOptions(FacesContext context, Compiler compiler)
    {
        ExternalContext eContext = context.getExternalContext();

        // skip comments?
        compiler.setTrimmingComments(_getBooleanParameter(eContext, PARAM_SKIP_COMMENTS, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendSourceNotFound(FacesContext context, String message)
    {
        // This is incredibly lame, but I see no other option. -= SL =-
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        try
        {
            context.responseComplete();
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }
        catch (IOException ioe)
        {
            throw new FacesException(ioe);
        }
    }

    /**
     * Gets the boolean init parameter value from the specified context. If the parameter was not specified, the default
     * value is used instead.
     * 
     * TODO: This method should be in an utility class. Anyone knows if we have such a class already? -= SL =-
     * 
     * @param context
     *            the application's external context
     * @param name
     *            the init parameter's name
     * @param defaultValue
     *            the default value to return in case the parameter was not set
     * 
     * @return the init parameter value as a boolean
     * 
     * @throws NullPointerException
     *             if context or name is <code>null</code>
     */
    private boolean _getBooleanParameter(ExternalContext context, String name, boolean defaultValue)
    {
        String param = _getStringParameter(context, name);
        if (param == null)
        {
            return defaultValue;
        }
        else
        {
            return Boolean.parseBoolean(param.toLowerCase());
        }
    }

    /**
     * Gets the int init parameter value from the specified context. If the parameter was not specified, the default
     * value is used instead.
     * 
     * TODO: This method should be in an utility class. Anyone knows if we have such a class already? -= SL =-
     * 
     * @param context
     *            the application's external context
     * @param name
     *            the init parameter's name
     * @param defaultValue
     *            the default value to return in case the parameter was not set
     * 
     * @return the init parameter value as a int
     * 
     * @throws NullPointerException
     *             if context or name is <code>null</code>
     */
    private int _getIntegerParameter(ExternalContext context, String name, int defaultValue)
    {
        String param = _getStringParameter(context, name);
        if (param == null)
        {
            return defaultValue;
        }
        else
        {
            return Integer.parseInt(param);
        }
    }

    /**
     * Gets the Facelet representing the specified view identifier.
     * 
     * @param viewId
     *            the view identifier
     * 
     * @return the Facelet representing the specified view identifier
     * 
     * @throws IOException
     *             if a read or parsing error occurs
     */
    private Facelet _getFacelet(String viewId) throws IOException
    {
        // grab our FaceletFactory and create a Facelet
        FaceletFactory.setInstance(_faceletFactory);
        try
        {
            return _faceletFactory.getFacelet(viewId);
        }
        finally
        {
            FaceletFactory.setInstance(null);
        }
    }

    /**
     * Gets the init parameter value from the specified context and instanciate it. If the parameter was not specified,
     * the default value is used instead.
     * 
     * TODO: This method should be in an utility class. Anyone knows if we have such a class already? -= SL =-
     * 
     * @param context
     *            the application's external context
     * @param name
     *            the init parameter's name
     * @param defaultValue
     *            the default value to return in case the parameter was not set
     * 
     * @return the init parameter value as an object instance
     * 
     * @throws NullPointerException
     *             if context or name is <code>null</code>
     */
    @SuppressWarnings("unchecked")
    private <T> T _getInstanceParameter(ExternalContext context, String name, T defaultValue)
    {
        String param = _getStringParameter(context, name);
        if (param == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return (T) ReflectionUtil.forName(param).newInstance();
            }
            catch (Exception e)
            {
                throw new FacesException("Error Initializing Object[" + param + "]", e);
            }
        }
    }

    /**
     * Gets the long init parameter value from the specified context. If the parameter was not specified, the default
     * value is used instead.
     * 
     * TODO: This method should be in an utility class. Anyone knows if we have such a class already? -= SL =-
     * 
     * @param context
     *            the application's external context
     * @param name
     *            the init parameter's name
     * @param defaultValue
     *            the default value to return in case the parameter was not set
     * 
     * @return the init parameter value as a long
     * 
     * @throws NullPointerException
     *             if context or name is <code>null</code>
     */
    private long _getLongParameter(ExternalContext context, String name, long defaultValue)
    {
        String param = _getStringParameter(context, name);
        if (param == null)
        {
            return defaultValue;
        }
        else
        {
            return Long.parseLong(param);
        }
    }

    /**
     * Gets the String init parameter value from the specified context. If the parameter is an empty String or a String
     * containing only white space, this method returns <code>null</code>
     * 
     * TODO: This method should be in an utility class. Anyone knows if we have such a class already? -= SL =-
     * 
     * @param context
     *            the application's external context
     * @param name
     *            the init parameter's name
     * 
     * @return the parameter if it was specified and was not empty, <code>null</code> otherwise
     * 
     * @throws NullPointerException
     *             if context or name is <code>null</code>
     */
    private String _getStringParameter(ExternalContext context, String name)
    {
        String param = context.getInitParameter(name);
        if (param == null)
        {
            return null;
        }

        param = param.trim();
        if (param.length() == 0)
        {
            return null;
        }

        return param;
    }

    private void _initializeBuffer(ExternalContext context)
    {
        _bufferSize = _getIntegerParameter(context, PARAM_BUFFER_SIZE, -1);
    }

    private void _initializeMode(ExternalContext context)
    {
        // In jsf 2.0 this code evolve as PartialStateSaving feature
        //_buildBeforeRestore = _getBooleanParameter(context, PARAM_BUILD_BEFORE_RESTORE, false);
        _partialStateSaving = _getBooleanParameter(context, 
                StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, false);
        
        String [] viewIds = StringUtils.splitShortString(_getStringParameter(context,
                StateManager.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME), ',');
        
        if (viewIds.length > 0)
        {
            _viewIds = new HashSet<String>(viewIds.length, 1.0f);
            Collections.addAll(_viewIds, viewIds);
        }
        else
        {
            _viewIds = null;
        }
    }
    
    private boolean _usePartialStateSavingOnThisView(String viewId)
    {
        return _partialStateSaving && !(_viewIds != null && _viewIds.contains(viewId) );
    }
    
    /**
     * Determines if the application is currently in devlopment mode.
     * 
     * @param context the current FacesContext
     * 
     * @return <code>true</code> if the application is in devlopment mode, <code>false</code> otherwise
     */
    private boolean _isDevelopmentMode(FacesContext context)
    {
        return ProjectStage.Development.equals(context.getApplication().getProjectStage());
    }

    private class FaceletViewMetadata extends ViewMetadataBase
    {
        /**
         * 
         */
        public FaceletViewMetadata(String viewId)
        {
            super(viewId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UIViewRoot createMetadataView(FacesContext context)
        {
            try
            {
                context.setProcessingEvents(false);

                String viewId = getViewId();
                UIViewRoot view = createView(context, viewId);
                buildView(context, view);

                return view;
            }
            catch (IOException ioe)
            {
                throw new FacesException(ioe);
            }
            finally
            {
                context.setProcessingEvents(true);
            }
        }
    }

}
