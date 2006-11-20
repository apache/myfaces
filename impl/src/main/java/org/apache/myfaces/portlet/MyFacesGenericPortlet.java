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
package org.apache.myfaces.portlet;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.context.portlet.PortletExternalContextImpl;
import org.apache.myfaces.context.servlet.ServletFacesContextImpl;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;

/**
 * This portlet initializes MyFaces and converts portlet requests into
 * JSF requests.
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MyFacesGenericPortlet extends GenericPortlet
{
    private static final Log log = LogFactory.getLog(MyFacesGenericPortlet.class);

    // PortletRequest parameter
    public static final String VIEW_ID =
        MyFacesGenericPortlet.class.getName() + ".VIEW_ID";

    // PortletSession attribute
    protected static final String CURRENT_FACES_CONTEXT =
        MyFacesGenericPortlet.class.getName() + ".CURRENT_FACES_CONTEXT";

    // portlet config parameter from portlet.xml
    protected static final String DEFAULT_VIEW = "default-view";

    // portlet config parameter from portlet.xml
    protected static final String DEFAULT_VIEW_SELECTOR = "default-view-selector";

    // On redeploy, the session might still exist, but all values are wiped out.  
    // This depends on the portal implementation.  So we put this flag in
    // the session to detect if a redeploy happened.
    protected static final String REDEPLOY_FLAG =
        MyFacesGenericPortlet.class.getName() + ".REDEPLOY_FLAG";
    
    protected static final String FACES_INIT_DONE =
        MyFacesGenericPortlet.class.getName() + ".FACES_INIT_DONE";
    
    protected static final String SAVED_REQUEST_ATTRIBUTES =
        MyFacesGenericPortlet.class.getName() + ".SAVED_REQUEST_ATTRIBUTES";   

    protected PortletContext portletContext;

    protected FacesContextFactory facesContextFactory;
    protected Lifecycle lifecycle;

    protected String defaultView;
    protected DefaultViewSelector defaultViewSelector;

    /**
     * Creates a new instance of MyFacesPortlet
     */
    public MyFacesGenericPortlet()
    {
    }

    /**
     * Portlet lifecycle.
     */
    public void destroy()
    {
        super.destroy();
        FactoryFinder.releaseFactories();
    }

    /**
     * Portlet lifecycle.
     */
    public void init() throws PortletException, UnavailableException
    {
        this.portletContext = getPortletContext();
        setDefaultView();
        setDefaultViewSelector();
        
        //?????????????????????????????
        // So what do we do about initialization?
        // Is it specific to the JSF impl?
        // What about JEE 5 auto-detection?  Should we do that for portlet?
        initMyFaces();

        facesContextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);

        // Javadoc says: Lifecycle instance is shared across multiple simultaneous requests, it must be
        // implemented in a thread-safe manner.  So we can acquire it here once:
        LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());
    }

    //?????????????????????????
    // Is defaultView really mandatory?
    protected void setDefaultView() throws UnavailableException
    {
        this.defaultView = getPortletConfig().getInitParameter(DEFAULT_VIEW);
        if (defaultView == null)
        {
            String msg = "Fatal: must specify a JSF view id as the default view in portlet.xml";
            throw new UnavailableException(msg);
        }
    }

    //?????????????????????????????
    // Is this useful?
    protected void setDefaultViewSelector() throws UnavailableException
    {
        String selectorClass = getPortletConfig().getInitParameter(DEFAULT_VIEW_SELECTOR);
        if (selectorClass == null) return;

        try
        {
            this.defaultViewSelector = (DefaultViewSelector)Class.forName(selectorClass).newInstance();
            this.defaultViewSelector.setPortletContext(getPortletContext());
        }
        catch (Exception e)
        {
            log.error("Failed to load " + DEFAULT_VIEW_SELECTOR, e);
            throw new UnavailableException(e.getMessage());
        }
    }

    //?????????????????????????????????????????
    // Is this correct?
    protected void setContentType(RenderRequest request, RenderResponse response)
    {

        if (response.getContentType() == null)
        {
            String portalPreferredContentType = request.getResponseContentType();
            if (portalPreferredContentType != null)
            {
                response.setContentType(portalPreferredContentType);
            }
            else
            {
                response.setContentType("text/html");
            }
        }
    }

    //?????????????????????????
    // Should we reuse FacesServlet.LIFECYCLE_ID_ATTR or replace it with our own?
    protected String getLifecycleId()
    {
        String lifecycleId = getPortletConfig().getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        return lifecycleId != null ? lifecycleId : LifecycleFactory.DEFAULT_LIFECYCLE;
    }

    protected void initMyFaces()
    {
        try
        {
            Boolean b = (Boolean)portletContext.getAttribute(FACES_INIT_DONE);

            if (b == null || b.booleanValue() == false)
            {
                log.trace("Initializing MyFaces");

                //Load the configuration
                ExternalContext externalContext = new PortletExternalContextImpl(portletContext, null, null);

                //And configure everything
                new FacesConfigurator(externalContext).configure();

                // parse web.xml - not sure if this is needed for portlet
                WebXml.init(externalContext);

                portletContext.setAttribute(FACES_INIT_DONE, Boolean.TRUE);
            }
            else
            {
                log.info("MyFaces already initialized");
            }
        }
        catch (Exception ex)
        {
            log.error("Error initializing MyFacesGenericPortlet", ex);
        }

        log.info("PortletContext '" + portletContext.getRealPath("/") + "' initialized.");
    }

    /**
     * Called by the portlet container to allow the portlet to process an action request.
     */
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException
    {
        if (log.isTraceEnabled()) log.trace("called processAction");

        if (sessionTimedOut(request)) return;

        setPortletRequestFlag(request);

        FacesContext facesContext = facesContext(request, response);

        try
        {
            lifecycle.execute(facesContext);

            if (!facesContext.getResponseComplete())
            {
                response.setRenderParameter(VIEW_ID, facesContext.getViewRoot().getViewId());
            }

            request.getPortletSession().setAttribute(CURRENT_FACES_CONTEXT, facesContext);
        }
        catch (Throwable e)
        {
            facesContext.release();
            handleExceptionFromLifecycle(e);
        }
        finally
        {
           saveRequestAttributes(request);
        }
    }
    
    // need to save req attribs so that lifecycle.render() can use them later
    protected void saveRequestAttributes(ActionRequest request)
    {
       PortletSession session = request.getPortletSession();
       SavedRequestAttributes reqAttribs = null;
       synchronized(session)
       {
          reqAttribs = (SavedRequestAttributes)session.getAttribute(SAVED_REQUEST_ATTRIBUTES);
          if (reqAttribs == null) 
          {
             reqAttribs = new SavedRequestAttributes();
             session.setAttribute(SAVED_REQUEST_ATTRIBUTES, reqAttribs);
          }
       }
       
       reqAttribs.saveRequestAttributes(request);
    }
    
    // restore req attribs so lifecycle.render() can use them
    protected void restoreRequestAttributes(RenderRequest request)
    {
       PortletSession session = request.getPortletSession();
       SavedRequestAttributes reqAttribs = 
               (SavedRequestAttributes)session.getAttribute(SAVED_REQUEST_ATTRIBUTES);
       reqAttribs.resotreRequestAttributes(request);
    }

    protected void handleExceptionFromLifecycle(Throwable e)
            throws PortletException, IOException
    {
        logException(e, null);

        if (e instanceof IOException)
        {
            throw (IOException)e;
        }

        if (e instanceof PortletException)
        {
            throw (PortletException)e;
        }

        if (e.getMessage() != null)
        {
            throw new PortletException(e.getMessage(), e);
        }

        throw new PortletException(e);
    }

    /**
     * Helper method to serve up the view mode.
     */
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
       try {
          facesRender(request, response);
       } finally {
          renderCleanup(request);
       }
    }

    /**
     * Helper method to serve up the edit mode.  Can be overridden to add
     * the edit mode concept to a JSF application.
     */
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
       try {
          facesRender(request, response);
       } finally {
          renderCleanup(request);
       }
    }

    /**
     * Helper method to serve up the edit mode.  Can be overridden to add
     * the help mode concept to a JSF application.
     */
    protected void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
       try {
         facesRender(request, response);
       } finally {
          renderCleanup(request);
       }
    }

    //???????????????????
    // Is there any other reliable way to pass FacesContext to render?
    protected void renderCleanup(RenderRequest request)
    {
       PortletSession session = request.getPortletSession();
       session.setAttribute(REDEPLOY_FLAG, "portlet was not redeployed");
       session.removeAttribute(this.CURRENT_FACES_CONTEXT);
    }

    /**
     * This method follows JSF Spec section 2.1.1.  It renders the default view from a non-faces
     * request.
     *
     * @param request The portlet render request.
     * @param response The portlet render response.
     */
    protected void nonFacesRequest(RenderRequest request, RenderResponse response) throws PortletException
    {
        nonFacesRequest(request, response, selectDefaultView(request, response));
    }

    /**
     * This method follows JSF Spec section 2.1.1.  It renders a view from a non-faces
     * request.  This is useful for a default view as well as for views that need to
     * be rendered from the portlet's edit and help buttons.
     *
     * @param request The portlet render request.
     * @param response The portlet render response.
     * @param view The name of the view that needs to be rendered.
     */
    protected void nonFacesRequest(RenderRequest request, RenderResponse response, String view)
            throws PortletException
    {
        if (log.isTraceEnabled()) log.trace("Non-faces request: contextPath = " + request.getContextPath());
        setContentType(request, response); // do this in case nonFacesRequest is called by a subclass
        
        FacesContext facesContext = facesContext(request, response);
        setViewRootOnFacesContext(facesContext, view);
        lifecycle.render(facesContext);
    }
    
    // Set the view root on a FacesContext to prepare for rendering
    private void setViewRootOnFacesContext(FacesContext facesContext, String view)
    {
        ApplicationFactory appFactory =
            (ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = appFactory.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(facesContext, view);
        viewRoot.setViewId(view);
        facesContext.setViewRoot(viewRoot);
    }

    protected String selectDefaultView(RenderRequest request, RenderResponse response) throws PortletException
    {
        String view = this.defaultView;
        if (this.defaultViewSelector != null)
        {
            String selectedView = this.defaultViewSelector.selectViewId(request, response);
            if (selectedView != null)
            {
                view = selectedView;
            }
        }

        return view;
    }

    protected FacesContext facesContext(PortletRequest request,
                                        PortletResponse response)
    {
        return facesContextFactory.getFacesContext(portletContext,
                                                   request,
                                                   response,
                                                   lifecycle);
    }

    protected ReleaseableExternalContext makeExternalContext(PortletRequest request,
                                                             PortletResponse response)
    {
        return (ReleaseableExternalContext)new PortletExternalContextImpl(portletContext, request, response);
    }

    protected boolean sessionTimedOut(PortletRequest request)
    {
       return request.getPortletSession(false) == null;
    }
    
    protected boolean sessionInvalidated(PortletRequest request)
    {
        return  sessionTimedOut(request) ||
               (request.getPortletSession().getAttribute(REDEPLOY_FLAG) == null);
    }

    protected void setPortletRequestFlag(PortletRequest request)
    {
        request.getPortletSession().setAttribute(PortletUtil.PORTLET_REQUEST_FLAG, "true");
    }

    /**
     * Render a JSF view.
     */
    protected void facesRender(RenderRequest request, RenderResponse response)
            throws PortletException, java.io.IOException
    {
        if (log.isTraceEnabled()) log.trace("called facesRender");

        setContentType(request, response);

        //??????????????
        // Is there a better way to maintain the view id?
        String viewId = request.getParameter(VIEW_ID);
        if ((viewId == null) || sessionInvalidated(request))
        {
            setPortletRequestFlag(request);
            nonFacesRequest(request,  response);
            return;
        }

        setPortletRequestFlag(request);

        ServletFacesContextImpl facesContext = null;
        try
        {
            facesContext = (ServletFacesContextImpl)request.
                                                    getPortletSession().
                                                    getAttribute(CURRENT_FACES_CONTEXT);
            
            if (facesContext == null) // processAction was not called
            {
               facesContext = (ServletFacesContextImpl)facesContext(request, response);
               setViewRootOnFacesContext(facesContext, viewId);
            }
            
            // TODO: not sure if this can happen.  Also double check this against spec section 2.1.3
            if (facesContext.getResponseComplete()) return;

            facesContext.setExternalContext(makeExternalContext(request, response));
            restoreRequestAttributes(request);
            lifecycle.render(facesContext);
        }
        catch (Throwable e)
        {
            handleExceptionFromLifecycle(e);
        }
    }

    protected void logException(Throwable e, String msgPrefix) {
        String msg;
        if (msgPrefix == null)
        {
            if (e.getMessage() == null)
            {
                msg = "Exception in FacesServlet";
            }
            else
            {
                msg = e.getMessage();
            }
        }
        else
        {
            if (e.getMessage() == null)
            {
                msg = msgPrefix;
            }
            else
            {
                msg = msgPrefix + ": " + e.getMessage();
            }
        }

        portletContext.log(msg, e);

        Throwable cause = e.getCause();
        if (cause != null && cause != e)
        {
            logException(cause, "Root cause");
        }

        if(e instanceof PortletException)
        {
            cause = ((PortletException) e).getCause();

            if(cause != null && cause != e)
            {
                logException(cause, "Root cause of PortletException");
            }
        }
    }

}