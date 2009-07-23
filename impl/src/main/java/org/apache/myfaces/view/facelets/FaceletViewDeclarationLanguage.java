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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.ActionSource2;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.render.RenderKit;
import javax.faces.validator.MethodExpressionValidator;
import javax.faces.view.ActionSource2AttachedObjectHandler;
import javax.faces.view.ActionSource2AttachedObjectTarget;
import javax.faces.view.AttachedObjectHandler;
import javax.faces.view.AttachedObjectTarget;
import javax.faces.view.EditableValueHolderAttachedObjectHandler;
import javax.faces.view.EditableValueHolderAttachedObjectTarget;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ValueHolderAttachedObjectHandler;
import javax.faces.view.ValueHolderAttachedObjectTarget;
import javax.faces.view.ViewMetadata;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.DefaultViewHandlerSupport;
import org.apache.myfaces.application.ViewHandlerSupport;
import org.apache.myfaces.view.ViewDeclarationLanguageBase;
import org.apache.myfaces.view.ViewMetadataImpl;
import org.apache.myfaces.view.facelets.FaceletViewHandler.NullWriter;
import org.apache.myfaces.view.facelets.compiler.Compiler;
import org.apache.myfaces.view.facelets.compiler.SAXCompiler;
import org.apache.myfaces.view.facelets.compiler.TagLibraryConfig;
import org.apache.myfaces.view.facelets.impl.DefaultFaceletFactory;
import org.apache.myfaces.view.facelets.impl.DefaultResourceResolver;
import org.apache.myfaces.view.facelets.impl.ResourceResolver;
import org.apache.myfaces.view.facelets.tag.TagDecorator;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
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
    private ViewHandlerSupport _cachedViewHandlerSupport;
    private static final Log log = LogFactory.getLog(FaceletViewDeclarationLanguage.class);
    
    private FaceletFactory faceletFactory;

    public final static long DEFAULT_REFRESH_PERIOD = 2;

    public final static String PARAM_REFRESH_PERIOD = "facelets.REFRESH_PERIOD";

    public final static String PARAM_SKIP_COMMENTS = "facelets.SKIP_COMMENTS";

    public final static String PARAM_VIEW_MAPPINGS = "facelets.VIEW_MAPPINGS";

    public final static String PARAM_LIBRARIES = "facelets.LIBRARIES";

    public final static String PARAM_DECORATORS = "facelets.DECORATORS";

    public final static String PARAM_DEVELOPMENT = "facelets.DEVELOPMENT";

    public final static String PARAM_RESOURCE_RESOLVER = "facelets.RESOURCE_RESOLVER";

    public final static String PARAM_BUILD_BEFORE_RESTORE = "facelets.BUILD_BEFORE_RESTORE";

    public final static String PARAM_BUFFER_SIZE = "facelets.BUFFER_SIZE";

    private final static String STATE_KEY = "~facelets.VIEW_STATE~";

    private final static int STATE_KEY_LEN = STATE_KEY.length();

    private final ViewHandler parent;

    private boolean developmentMode = false;

    private boolean buildBeforeRestore = false;

    private int bufferSize;

    private String defaultSuffix;

    // Array of viewId extensions that should be handled by Facelets
    private String[] extensionsArray;

    // Array of viewId prefixes that should be handled by Facelets
    private String[] prefixesArray;
    
    private StateManagementStrategy stateMgmtStrategy;
    
    /**
     * 
     */
    public FaceletViewDeclarationLanguage(ViewHandler parent)
    {
        this.parent = parent;
    }

    /**
     * Initialize the ViewHandler during its first request.
     */
    protected void initialize(FacesContext context)
    {
        synchronized (this)
        {
            if (this.faceletFactory == null)
            {
                log.trace("Initializing");
                Compiler c = this.createCompiler();
                this.initializeCompiler(c);
                this.faceletFactory = this.createFaceletFactory(c);

                this.initializeMappings(context);
                this.initializeMode(context);
                this.initializeBuffer(context);

                log.trace("Initialization Successful");
            }
        }
    }
    
    private void initializeMode(FacesContext context)
    {
        ExternalContext external = context.getExternalContext();
        String param = external.getInitParameter(PARAM_DEVELOPMENT);
        this.developmentMode = "true".equals(param);

        String restoreMode = external.getInitParameter(PARAM_BUILD_BEFORE_RESTORE);
        this.buildBeforeRestore = "true".equals(restoreMode);
    }

    private void initializeBuffer(FacesContext context)
    {
        ExternalContext external = context.getExternalContext();
        String param = external.getInitParameter(PARAM_BUFFER_SIZE);
        this.bufferSize = (param != null && !"".equals(param)) ? Integer.parseInt(param) : -1;
    }

    /**
     * Initialize mappings, during the first request.
     */
    private void initializeMappings(FacesContext context)
    {
        ExternalContext external = context.getExternalContext();
        String viewMappings = external.getInitParameter(PARAM_VIEW_MAPPINGS);
        if ((viewMappings != null) && (viewMappings.length() > 0))
        {
            String[] mappingsArray = viewMappings.split(";");

            List<String> extensionsList = new ArrayList<String>(mappingsArray.length);
            List<String> prefixesList = new ArrayList<String>(mappingsArray.length);

            for (int i = 0; i < mappingsArray.length; i++)
            {
                String mapping = mappingsArray[i].trim();
                int mappingLength = mapping.length();
                if (mappingLength <= 1)
                {
                    continue;
                }

                if (mapping.charAt(0) == '*')
                {
                    extensionsList.add(mapping.substring(1));
                }
                else if (mapping.charAt(mappingLength - 1) == '*')
                {
                    prefixesList.add(mapping.substring(0, mappingLength - 1));
                }
            }

            extensionsArray = new String[extensionsList.size()];
            extensionsList.toArray(extensionsArray);

            prefixesArray = new String[prefixesList.size()];
            prefixesList.toArray(prefixesArray);
        }
    }

    protected FaceletFactory createFaceletFactory(Compiler c)
    {

        // refresh period
        long refreshPeriod = DEFAULT_REFRESH_PERIOD;
        FacesContext ctx = FacesContext.getCurrentInstance();
        String userPeriod = ctx.getExternalContext().getInitParameter(PARAM_REFRESH_PERIOD);
        if (userPeriod != null && userPeriod.length() > 0)
        {
            refreshPeriod = Long.parseLong(userPeriod);
        }

        // resource resolver
        ResourceResolver resolver = new DefaultResourceResolver();
        String resolverName = ctx.getExternalContext().getInitParameter(PARAM_RESOURCE_RESOLVER);
        if (resolverName != null && resolverName.length() > 0)
        {
            try
            {
                resolver = (ResourceResolver) ReflectionUtil.forName(resolverName).newInstance();
            }
            catch (Exception e)
            {
                throw new FacesException("Error Initializing ResourceResolver[" + resolverName + "]", e);
            }
        }

        // Resource.getResourceUrl(ctx,"/")
        return new DefaultFaceletFactory(c, resolver, refreshPeriod);
    }

    protected Compiler createCompiler()
    {
        return new SAXCompiler();
    }

    protected void initializeCompiler(Compiler c)
    {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ext = ctx.getExternalContext();

        // load libraries
        String libParam = ext.getInitParameter(PARAM_LIBRARIES);
        if (libParam != null)
        {
            libParam = libParam.trim();
            String[] libs = libParam.split(";");
            URL src;
            TagLibrary libObj;
            for (int i = 0; i < libs.length; i++)
            {
                try
                {
                    src = ext.getResource(libs[i].trim());
                    if (src == null)
                    {
                        throw new FileNotFoundException(libs[i]);
                    }
                    libObj = TagLibraryConfig.create(src);
                    c.addTagLibrary(libObj);
                    log.trace("Successfully Loaded Library: " + libs[i]);
                }
                catch (IOException e)
                {
                    log.fatal("Error Loading Library: " + libs[i], e);
                }
            }
        }

        // load decorators
        String decParam = ext.getInitParameter(PARAM_DECORATORS);
        if (decParam != null)
        {
            decParam = decParam.trim();
            String[] decs = decParam.split(";");
            TagDecorator decObj;
            for (int i = 0; i < decs.length; i++)
            {
                try
                {
                    decObj = (TagDecorator) ReflectionUtil.forName(decs[i]).newInstance();
                    c.addTagDecorator(decObj);
                    log.trace("Successfully Loaded Decorator: " + decs[i]);
                }
                catch (Exception e)
                {
                    log.fatal("Error Loading Decorator: " + decs[i], e);
                }
            }
        }

        // skip params?
        String skipParam = ext.getInitParameter(PARAM_SKIP_COMMENTS);
        if (skipParam != null && "true".equals(skipParam))
        {
            c.setTrimmingComments(true);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void buildView(FacesContext context, UIViewRoot view) throws IOException
    {
        // setup our viewId
        String renderedViewId = this.getRenderedViewId(context, view.getViewId());
        view.setViewId(renderedViewId);

        if (log.isTraceEnabled())
        {
            log.trace("Building View: " + renderedViewId);
        }

        // grab our FaceletFactory and create a Facelet
        Facelet f = null;
        FaceletFactory.setInstance(this.faceletFactory);
        try
        {
            f = this.faceletFactory.getFacelet(view.getViewId());
        }
        finally
        {
            FaceletFactory.setInstance(null);
        }

        // populate UIViewRoot
        long time = System.currentTimeMillis();
        f.apply(context, view);
        time = System.currentTimeMillis() - time;
        if (log.isTraceEnabled())        {
            log.trace("Took " + time + "ms to build view: " + view.getViewId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        UIViewRoot viewRoot = super.createView(context, viewId);
        context.setViewRoot(viewRoot);
        
        return viewRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BeanInfo getComponentMetadata(FacesContext context, Resource componentResource)
    {
        checkNull(context, "context");
        checkNull(componentResource, "componentResource");
        
        try {
            UIComponent tempViewRoot;
            Facelet facelet;
            
            // TODO: make sure this is the right implementation.  I believe we need to create
            // the Facelet, apply it against some new viewroot, and pull the BeanInfo out of
            // its attributes.  Caching would probably be nice too.
            
            facelet = faceletFactory.getFacelet(componentResource.getURL());
            
            tempViewRoot = context.getApplication().createComponent (UIViewRoot.COMPONENT_TYPE);
            
            facelet.apply (context, tempViewRoot);
            
            return (BeanInfo) tempViewRoot.getAttributes().get (UIComponent.BEANINFO_KEY);
        }
        
        catch (Throwable e) {
            if (e instanceof FacesException) {
                throw ((FacesException) e);
            }
            
            throw new FacesException ("unable to get component metadata", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getScriptComponentResource(FacesContext context, Resource componentResource)
    {
        checkNull(context, "context");
        checkNull(componentResource, "componentResource");
        
        // TODO: IMPLEMENT HERE
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId)
    {
        checkNull(context, "context");
        checkNull(viewId, "viewId");
        
        // TODO: cache?
        
        return new ViewMetadataImpl (viewId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException
    {
        checkNull(context, "context");
        checkNull(view, "view");
        
     // lazy initialize so we have a FacesContext to use
        if (this.faceletFactory == null)
        {
            this.initialize(context);
        }

        // exit if the view is not to be rendered
        if (!view.isRendered())
        {
            return;
        }

        // if facelets is not supposed to handle this request
        if (!handledByFacelets(view.getViewId()))
        {
            this.parent.renderView(context, view);
            return;
        }

        // log request
        if (log.isTraceEnabled())
        {
            log.trace("Rendering View: " + view.getViewId());
        }

        StateWriter stateWriter = null;
        try
        {
            // build view - but not if we're in "buildBeforeRestore"
            // land and we've already got a populated view. Note
            // that this optimizations breaks if there's a "c:if" in
            // the page that toggles as a result of request processing -
            // should that be handled? Or
            // is this optimization simply so minor that it should just
            // be trimmed altogether?
            if (!this.buildBeforeRestore || view.getChildren().isEmpty())
            {
                this.buildView(context, view);
            }

            // setup writer and assign it to the context
            ResponseWriter origWriter = this.createResponseWriter(context);
            // QUESTION: should we use bufferSize? Or, since the
            // StateWriter usually only needs a small bit at the end,
            // should we always use a much smaller size?
            stateWriter = new StateWriter(origWriter, this.bufferSize != -1 ? this.bufferSize : 1024);

            ResponseWriter writer = origWriter.cloneWithWriter(stateWriter);
            context.setResponseWriter(writer);

            // force creation of session if saving state there
            StateManager stateMgr = context.getApplication().getStateManager();
            if (!stateMgr.isSavingStateInClient(context))
            {
                context.getExternalContext().getSession(true);
            }

            long time = System.currentTimeMillis();

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
                    Object stateObj = stateMgr.saveSerializedView(context);
                    String stateStr;
                    if (stateObj == null)
                    {
                        stateStr = null;
                    }
                    else
                    {
                        stateMgr.writeState(context, (StateManager.SerializedView) stateObj);
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

            time = System.currentTimeMillis() - time;
            if (log.isTraceEnabled())
            {
                log.trace("Took " + time + "ms to render view: " + view.getViewId());
            }

        }
        catch (FileNotFoundException fnfe)
        {
            this.handleFaceletNotFound(context, view.getViewId());
        }
        catch (Exception e)
        {
            this.handleRenderException(context, e);
        }
        finally
        {
            if (stateWriter != null)
                stateWriter.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        checkNull(context, "context");
        checkNull(viewId, "viewId");
        
        if (UIDebug.debugRequest(context))
        {
            return new UIViewRoot();
        }

        if (!this.buildBeforeRestore || !handledByFacelets(viewId))
        {
            return this.parent.restoreView(context, viewId);
        }

        if (this.faceletFactory == null)
        {
            this.initialize(context);
        }

        // In JSF 1.2, restoreView() will only be called on postback.
        // But in JSF 1.1, it will be called for an initial request too,
        // in which case we must return null in order to fall through
        // to createView()

        ViewHandler outerViewHandler = context.getApplication().getViewHandler();
        String renderKitId = outerViewHandler.calculateRenderKitId(context);

        UIViewRoot viewRoot = createView(context, viewId);
        context.setViewRoot(viewRoot);
        try
        {
            this.buildView(context, viewRoot);
        }
        catch (IOException ioe)
        {
            log.fatal("Error Building View", ioe);
        }
        context.getApplication().getStateManager().restoreView(context, viewId, renderKitId);
        return viewRoot;       
    }

    @Override
    protected String calculateViewId(FacesContext context, String viewId)
    {
        if (_cachedViewHandlerSupport == null)
        {
            _cachedViewHandlerSupport = new DefaultViewHandlerSupport();
        }
        
        return _cachedViewHandlerSupport.calculateViewId(context, viewId);
    }

    @Override
    protected void sendSourceNotFound(FacesContext context, String message)
    {
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

    @Override
    public StateManagementStrategy getStateManagementStrategy(
            FacesContext context, String viewId)
    {
        if (stateMgmtStrategy == null) {
            stateMgmtStrategy = new DefaultFaceletsStateManagementStrategy (this);
        }
        
        return stateMgmtStrategy;
    }
    
    @Override
    public void retargetMethodExpressions(FacesContext context, UIComponent topLevelComponent)
    {
        checkNull(context, "context");
        checkNull(topLevelComponent, "topLevelComponent");
        
        BeanInfo beanInfo = (BeanInfo)topLevelComponent.getAttributes().get(UIComponent.BEANINFO_KEY);
        if(beanInfo == null)
        {
            return;  // should we log an error here?  spec doesn't say one way or the other so leaving it alone for now. 
        }
        
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for(PropertyDescriptor curDescriptor : descriptors)
        {
            ExpressionFactory expressionFactory = null;
            ValueExpression valueExpression = null;
            MethodExpression attributeMethodExpression = null;
            Class expectedReturn = null;
            Class expectedParams[] = null;
            
            if(curDescriptor.getValue("type") != null || curDescriptor.getValue("method-signature") == null)
            {
                continue;   
            }
            
            String targets = null;
            valueExpression = (ValueExpression)curDescriptor.getValue("targets");
            if(valueExpression != null)
            {
                targets = (String)valueExpression.getValue(context.getELContext());
            }

            if(targets == null)
            {
                targets = curDescriptor.getName();
            }
            
            if(targets == null)
            {
                continue;   //not explicitly part of the algorithm, but could lead to an NPE if we don't check this
            }
            
            String[] targetArray = targets.split(" ");
            for (String curTarget : targetArray)
            {
                UIComponent target = topLevelComponent.findComponent(curTarget);
                if(target == null)
                {
                    log.error("target not found");
                    continue;
                }
                
                String name = curDescriptor.getName();
                
                ValueExpression attributeValueExpression = (ValueExpression)topLevelComponent.getAttributes().get(name);
                
                if(attributeValueExpression == null)
                {
                    log.error("attributeValueExpression not found");
                    continue;
                }

                if(expressionFactory == null)
                {   //initialize expression factory if hasn't been used yet
                    expressionFactory = context.getApplication().getExpressionFactory();
                }
                
                boolean isAction = name.equals("action"),
                        isActionListener = name.equals("actionListener"),
                        isValidator = name.equals("validator"),
                        isValueChangeListener = name.equals("valueChangeListener");
                
                String expressionString = attributeValueExpression.getExpressionString();
                    
                if(isAction)
                    expectedReturn = Object.class;
                else
                    expectedReturn = Void.class;
                
                if(isAction)
                {
                    expectedParams =  new Class[]{};
                    attributeMethodExpression = expressionFactory.createMethodExpression(context.getELContext(), expressionString, expectedReturn, expectedParams);
                    ((ActionSource2) target).setActionExpression(attributeMethodExpression);
                }
                else if(isActionListener)
                {
                    expectedParams = new Class[]{ActionEvent.class};
                    attributeMethodExpression = expressionFactory.createMethodExpression(context.getELContext(), expressionString, expectedReturn, expectedParams);
                    ((ActionSource2) target).addActionListener(new MethodExpressionActionListener(attributeMethodExpression));
                }
                else if(isValidator)
                {
                    expectedParams = new Class[]{FacesContext.class,UIComponent.class,Object.class};
                    attributeMethodExpression = expressionFactory.createMethodExpression(context.getELContext(), expressionString, expectedReturn, expectedParams);
                    ((EditableValueHolder) target).addValidator(new MethodExpressionValidator(attributeMethodExpression));
                }
                else if(isValueChangeListener)
                {
                    expectedParams = new Class[]{ValueChangeEvent.class};
                    attributeMethodExpression = expressionFactory.createMethodExpression(context.getELContext(), expressionString, expectedReturn, expectedParams);
                    ((EditableValueHolder) target).addValueChangeListener(new MethodExpressionValueChangeListener(attributeMethodExpression));
                }
                else
                {
                    //TODO: implement here - derive attribute name from method-signature
                    //If name is not equal to any of the previously listed strings, call getExpressionString() on the attributeValueExpression and use that string to create a MethodExpression 
                    //where the signature is created based on the value of the "method-signature" attribute of the <composite:attribute /> tag.
                    
                    //Otherwise, assume that the MethodExpression  should be placed in the components attribute set. The runtme must create the MethodExpression instance based on the value of the "method-signature" attribute.
                }
                
                
            }
        }
    }
    
    @Override
    public void retargetAttachedObjects(FacesContext context,
            UIComponent topLevelComponent, List<AttachedObjectHandler> handlers)
    {
        checkNull(context, "context");
        checkNull(topLevelComponent, "topLevelComponent");
        checkNull(handlers, "handlers");
        
        BeanInfo beanInfo = (BeanInfo)topLevelComponent.getAttributes().get(UIComponent.BEANINFO_KEY);
        
        if(beanInfo == null)
        {
            log.error("BeanInfo not found");
            return;
        }
        
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        List<AttachedObjectTarget> targetList = (List<AttachedObjectTarget>)beanDescriptor.getValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY);

        for (AttachedObjectHandler curHandler : handlers)
        {
            String forAttributeValue = curHandler.getFor();

            for(AttachedObjectTarget curTarget : targetList)
            {
                List<UIComponent> targetUIComponents = curTarget.getTargets(topLevelComponent);
                String curTargetName = curTarget.getName();

                if( (curHandler instanceof ActionSource2AttachedObjectHandler) && curHandler instanceof ActionSource2AttachedObjectTarget)
                {
                    applyAttachedObjects(context,targetUIComponents, curHandler);
                    break;
                }
                if( (curHandler instanceof EditableValueHolderAttachedObjectHandler) && curHandler instanceof EditableValueHolderAttachedObjectTarget)
                {
                    applyAttachedObjects(context,targetUIComponents, curHandler);
                    break;
                }
                if( (curHandler instanceof ValueHolderAttachedObjectHandler) && curHandler instanceof ValueHolderAttachedObjectTarget)
                {
                    applyAttachedObjects(context,targetUIComponents, curHandler);
                    break;
                }
            }
        }
    }
    
    protected void applyAttachedObjects(FacesContext context, List<UIComponent> targetUIComponents, AttachedObjectHandler curHandler)
    {
        for (UIComponent target : targetUIComponents)
        {
            curHandler.applyAttachedObject(context, target);
        }
    }
    
    protected String getRenderedViewId(FacesContext context, String actionId)
    {
        ExternalContext extCtx = context.getExternalContext();
        String viewId = actionId;
        if (extCtx.getRequestPathInfo() == null)
        {
            String viewSuffix = this.getDefaultSuffix(context);
            viewId = new StringBuffer(viewId).replace(viewId.lastIndexOf('.'), viewId.length(), viewSuffix).toString();
        }
        if (log.isTraceEnabled())
        {
            log.trace("ActionId -> ViewId: " + actionId + " -> " + viewId);
        }
        return viewId;
    }
    
    protected String getDefaultSuffix(FacesContext context) throws FacesException
    {
        if (this.defaultSuffix == null)
        {
            ExternalContext extCtx = context.getExternalContext();
            String viewSuffix = extCtx.getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
            this.defaultSuffix = (viewSuffix != null) ? viewSuffix : ViewHandler.DEFAULT_FACELETS_SUFFIX;
        }
        return this.defaultSuffix;
    }

    protected void handleRenderException(FacesContext context, Exception e) throws IOException, ELException,
    FacesException
    {
        Object resp = context.getExternalContext().getResponse();

        // always log
        if (log.isFatalEnabled())
        {
            UIViewRoot root = context.getViewRoot();
            StringBuffer sb = new StringBuffer(64);
            sb.append("Error Rendering View");
            if (root != null)
            {
                sb.append('[');
                sb.append(root.getViewId());
                sb.append(']');
            }
            log.fatal(sb.toString(), e);
        }

        // handle dev response
        if (this.developmentMode && !context.getResponseComplete() && resp instanceof HttpServletResponse)
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

    protected void handleFaceletNotFound(FacesContext context, String viewId) throws FacesException, IOException
    {
        if(_cachedViewHandlerSupport == null)
        {
            _cachedViewHandlerSupport = new DefaultViewHandlerSupport();
        }
        
        String actualId = _cachedViewHandlerSupport.calculateActionURL(context, viewId);
        Object respObj = context.getExternalContext().getResponse();
        if (respObj instanceof HttpServletResponse)
        {
            HttpServletResponse respHttp = (HttpServletResponse) respObj;
            respHttp.sendError(HttpServletResponse.SC_NOT_FOUND, actualId);
            context.responseComplete();
        }
    }
    
    /**
     * Determine if Facelets needs to handle this request.
     */
    private boolean handledByFacelets(String viewId)
    {
        // If there's no extensions array or prefixes array, then
        // just make Facelets handle everything
        if ((extensionsArray == null) && (prefixesArray == null))
        {
            return true;
        }

        if (extensionsArray != null)
        {
            for (int i = 0; i < extensionsArray.length; i++)
            {
                String extension = extensionsArray[i];
                if (viewId.endsWith(extension))
                {
                    return true;
                }
            }
        }

        if (prefixesArray != null)
        {
            for (int i = 0; i < prefixesArray.length; i++)
            {
                String prefix = prefixesArray[i];
                if (viewId.startsWith(prefix))
                {
                    return true;
                }
            }
        }

        return false;
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
        if (this.bufferSize != -1)
        {
            response.setBufferSize(this.bufferSize);
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
        if (m.containsKey("facelets.Encoding"))
        {
            encoding = (String) m.get("facelets.Encoding");
            if (log.isTraceEnabled())
            {
                log.trace("Facelet specified alternate encoding '" + encoding + "'");
            }
            sm.put(ViewHandler.CHARACTER_ENCODING_KEY, encoding);
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
            encoding = (String) sm.get(ViewHandler.CHARACTER_ENCODING_KEY);
            if (log.isTraceEnabled())
            {
                log.trace("Session specified alternate encoding '" + encoding + "'");
            }
        }

        // 4. default it
        if (encoding == null)
        {
            encoding = "UTF-8";
            if (log.isTraceEnabled())
            {
                log.trace("ResponseWriter created had a null CharacterEncoding, defaulting to UTF-8");
            }
        }

        return encoding;
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
            if (log.isTraceEnabled())
            {
                log.trace("ResponseWriter created had a null ContentType, defaulting to text/html");
            }
        }

        return contentType;
    }
}
