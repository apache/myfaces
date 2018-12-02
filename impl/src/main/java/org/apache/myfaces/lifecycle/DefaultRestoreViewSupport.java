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
package org.apache.myfaces.lifecycle;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitContextFactory;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.util.Assert;
import org.apache.myfaces.util.ConcurrentLRUCache;
import org.apache.myfaces.util.WebConfigParamUtils;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultRestoreViewSupport implements RestoreViewSupport
{
    private static final String JAVAX_SERVLET_INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path";

    private static final String JAVAX_SERVLET_INCLUDE_PATH_INFO = "javax.servlet.include.path_info";
    
    /**
     * Constant defined on javax.portlet.faces.Bridge class that helps to 
     * define if the current request is a portlet request or not.
     */
    private static final String PORTLET_LIFECYCLE_PHASE = "javax.portlet.faces.phase";

    private final Logger log = Logger.getLogger(DefaultRestoreViewSupport.class.getName());

    /**
     * Controls the size of the cache used to "remember" if a view exists or not.
     */
    @JSFWebConfigParam(defaultValue = "500", since = "2.0.2", group="viewhandler", tags="performance", 
            classType="java.lang.Integer",
            desc="Controls the size of the cache used to 'remember' if a view exists or not.")
    private static final String CHECKED_VIEWID_CACHE_SIZE_ATTRIBUTE = "org.apache.myfaces.CHECKED_VIEWID_CACHE_SIZE";
    private static final int CHECKED_VIEWID_CACHE_DEFAULT_SIZE = 500;

    /**
     * Enable or disable a cache used to "remember" if a view exists or not and reduce the impact of
     * sucesive calls to ExternalContext.getResource().
     */
    @JSFWebConfigParam(defaultValue = "true", since = "2.0.2", expectedValues="true, false", group="viewhandler", 
            tags="performance",
            desc="Enable or disable a cache used to 'remember' if a view exists or not and reduce the impact " +
                 "of sucesive calls to ExternalContext.getResource().")
    private static final String CHECKED_VIEWID_CACHE_ENABLED_ATTRIBUTE = 
        "org.apache.myfaces.CHECKED_VIEWID_CACHE_ENABLED";
    private static final boolean CHECKED_VIEWID_CACHE_ENABLED_DEFAULT = true;
    
    private static final String SKIP_ITERATION_HINT = "javax.faces.visit.SKIP_ITERATION";
    
    private static final Set<VisitHint> VISIT_HINTS = Collections.unmodifiableSet( 
            EnumSet.of(VisitHint.SKIP_ITERATION));

    private volatile ConcurrentLRUCache<String, Boolean> _checkedViewIdMap = null;
    private Boolean _checkedViewIdCacheEnabled = null;
    
    private RenderKitFactory _renderKitFactory = null;
    private VisitContextFactory _visitContextFactory = null;

    
    public DefaultRestoreViewSupport()
    {
    }
    
    public DefaultRestoreViewSupport(FacesContext facesContext)
    {
    }

    @Override
    public void processComponentBinding(FacesContext facesContext, UIComponent component)
    {
        // JSF 2.0: Old hack related to t:aliasBean was fixed defining a event that traverse
        // whole tree and let components to override UIComponent.processEvent() method to include it.
        
        // Remove this hack SKIP_ITERATION_HINT and use VisitHints.SKIP_ITERATION in JSF 2.1 only
        // is not possible, because jsf 2.0 API-based libraries can use the String
        // hint, JSF21-based libraries can use both.
        try
        {
            facesContext.getAttributes().put(SKIP_ITERATION_HINT, Boolean.TRUE);

            VisitContext visitContext = (VisitContext) getVisitContextFactory().
                    getVisitContext(facesContext, null, VISIT_HINTS);
            component.visitTree(visitContext, new RestoreStateCallback());
        }
        finally
        {
            // We must remove hint in finally, because an exception can break this phase,
            // but lifecycle can continue, if custom exception handler swallows the exception
            facesContext.getAttributes().remove(SKIP_ITERATION_HINT);
        }
    }

    @Override
    public String calculateViewId(FacesContext facesContext)
    {
        Assert.notNull(facesContext);
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();

        String viewId = null;
        boolean traceEnabled = log.isLoggable(Level.FINEST);
        
        if (requestMap.containsKey(PORTLET_LIFECYCLE_PHASE))
        {
            viewId = (String) externalContext.getRequestPathInfo();
        }
        else
        {
            viewId = (String) requestMap.get(JAVAX_SERVLET_INCLUDE_PATH_INFO);
            if (viewId != null)
            {
                if (traceEnabled)
                {
                    log.finest("Calculated viewId '" + viewId + "' from request param '"
                               + JAVAX_SERVLET_INCLUDE_PATH_INFO + '\'');
                }
            }
            else
            {
                viewId = externalContext.getRequestPathInfo();
                if (viewId != null && traceEnabled)
                {
                    log.finest("Calculated viewId '" + viewId + "' from request path info");
                }
            }
    
            if (viewId == null)
            {
                viewId = (String) requestMap.get(JAVAX_SERVLET_INCLUDE_SERVLET_PATH);
                if (viewId != null && traceEnabled)
                {
                    log.finest("Calculated viewId '" + viewId + "' from request param '"
                            + JAVAX_SERVLET_INCLUDE_SERVLET_PATH + '\'');
                }
            }
        }
        
        if (viewId == null)
        {
            viewId = externalContext.getRequestServletPath();
            if (viewId != null && traceEnabled)
            {
                log.finest("Calculated viewId '" + viewId + "' from request servlet path");
            }
        }

        if (viewId == null)
        {
            throw new FacesException("Could not determine view id.");
        }

        return viewId;
    }

    @Override
    public boolean isPostback(FacesContext facesContext)
    {
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String renderkitId = viewHandler.calculateRenderKitId(facesContext);
        ResponseStateManager rsm
                = getRenderKitFactory().getRenderKit(facesContext, renderkitId).getResponseStateManager();
        return rsm.isPostback(facesContext);
    }
    
    protected RenderKitFactory getRenderKitFactory()
    {
        if (_renderKitFactory == null)
        {
            _renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return _renderKitFactory;
    }
    
    protected VisitContextFactory getVisitContextFactory()
    {
        if (_visitContextFactory == null)
        {
            _visitContextFactory = (VisitContextFactory)FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY);
        }
        return _visitContextFactory;
    }
        
    private static class RestoreStateCallback implements VisitCallback
    {
        private PostRestoreStateEvent event;

        @Override
        public VisitResult visit(VisitContext context, UIComponent target)
        {
            if (event == null)
            {
                event = new PostRestoreStateEvent(target);
            }
            else
            {
                event.setComponent(target);
            }

            // call the processEvent method of the current component.
            // The argument event must be an instance of AfterRestoreStateEvent whose component
            // property is the current component in the traversal.
            target.processEvent(event);
            
            return VisitResult.ACCEPT;
        }
    }

    @Override
    public boolean checkViewExists(FacesContext facesContext, String viewId)
    {
      try
        {
            if (isCheckedViewIdCachingEnabled(facesContext))
            {
                Boolean resourceExists = getCheckedViewIDMap(facesContext).get(viewId);
                if (resourceExists == null)
                {
                    ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler()
                            .getViewDeclarationLanguage(facesContext, viewId);
                    if (vdl != null)
                    {
                        resourceExists = vdl.viewExists(facesContext, viewId);
                    }
                    else
                    {
                        // Fallback to default strategy
                        resourceExists = facesContext.getExternalContext().getResource(viewId) != null;
                    }
                    getCheckedViewIDMap(facesContext).put(viewId, resourceExists);
                }
                return resourceExists;
            }
            else
            {
                ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler()
                            .getViewDeclarationLanguage(facesContext, viewId);
                if (vdl != null)
                {
                    if (vdl.viewExists(facesContext, viewId))
                    {
                        return true;
                    }
                }
                else
                {
                    // Fallback to default strategy
                    if (facesContext.getExternalContext().getResource(viewId) != null)
                    {
                        return true;
                    }
                }
            }
        }
        catch (MalformedURLException e)
        {
            //ignore and move on
        }     
        return false;
    }

    
    private ConcurrentLRUCache<String, Boolean> getCheckedViewIDMap(FacesContext context)
    {
        if (_checkedViewIdMap == null)
        {
            int maxSize = getViewIDCacheMaxSize(context);
            _checkedViewIdMap = new ConcurrentLRUCache<String, Boolean>((maxSize * 4 + 3) / 3, maxSize);
        }
        return _checkedViewIdMap;
    }

    private boolean isCheckedViewIdCachingEnabled(FacesContext context)
    {
        if (_checkedViewIdCacheEnabled == null)
        {
            // first, check if the ProjectStage is development and skip caching in this case
            if (context.isProjectStage(ProjectStage.Development))
            {
                _checkedViewIdCacheEnabled = Boolean.FALSE;
            }
            else
            {
                // in all ohter cases, make sure that the cache is not explicitly disabled via context param
                _checkedViewIdCacheEnabled = WebConfigParamUtils.getBooleanInitParameter(context.getExternalContext(),
                        CHECKED_VIEWID_CACHE_ENABLED_ATTRIBUTE,
                        CHECKED_VIEWID_CACHE_ENABLED_DEFAULT);
            }

            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "MyFaces ViewID Caching Enabled="
                        + _checkedViewIdCacheEnabled);
            }
        }
        return _checkedViewIdCacheEnabled;
    }

    private int getViewIDCacheMaxSize(FacesContext context)
    {
        ExternalContext externalContext = context.getExternalContext();

        return WebConfigParamUtils.getIntegerInitParameter(externalContext,
                CHECKED_VIEWID_CACHE_SIZE_ATTRIBUTE, CHECKED_VIEWID_CACHE_DEFAULT_SIZE);
    }
}
