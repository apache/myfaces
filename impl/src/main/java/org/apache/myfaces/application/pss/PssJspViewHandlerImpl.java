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
package org.apache.myfaces.application.pss;


import org.apache.myfaces.application.jsp.JspViewHandlerImpl;
import org.apache.myfaces.portlet.PortletUtil;
import org.apache.myfaces.shared_impl.webapp.webxml.ServletMapping;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;
import org.apache.myfaces.util.DebugUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.ViewHandler;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIViewRoot;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.RenderKit;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import java.util.Locale;
import java.util.List;
import java.io.IOException;

/**
 * @author Martin Haimberger
 */
public class PssJspViewHandlerImpl extends ViewHandler
{
    private static final Log log = LogFactory.getLog(JspViewHandlerImpl.class);
    private ViewHandler oldViewHandler  = null;

    private static final String PARTIAL_STATE_SAVING_METHOD_PARAM_NAME = "javax.faces.PARTIAL_STATE_SAVING_METHOD";
    private static final String PARTIAL_STATE_SAVING_METHOD_ON = "true";
    private static final String PARTIAL_STATE_SAVING_METHOD_OFF = "false";

    private Boolean _partialStateSaving = null;


    private int bufSize = 32;

    private boolean isPartialStateSavingOn(FacesContext context)
    {
        if(context == null) throw new NullPointerException("context");
        if (_partialStateSaving != null) return _partialStateSaving.booleanValue();
        String stateSavingMethod = context.getExternalContext().getInitParameter(PARTIAL_STATE_SAVING_METHOD_PARAM_NAME);
        if (stateSavingMethod == null)
        {
            _partialStateSaving = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("No context init parameter '"+PARTIAL_STATE_SAVING_METHOD_PARAM_NAME+"' found; no partial state saving method defined, assuming default partial state saving method off.");
        }
        else if (stateSavingMethod.equals(PARTIAL_STATE_SAVING_METHOD_ON))
        {
            _partialStateSaving = Boolean.TRUE;
        }
        else if (stateSavingMethod.equals(PARTIAL_STATE_SAVING_METHOD_OFF))
        {
            _partialStateSaving = Boolean.FALSE;
        }
        else
        {
            _partialStateSaving = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("Illegal partial state saving method '" + stateSavingMethod + "', default partial state saving will be used (partial state saving off).");
        }
        return _partialStateSaving.booleanValue();
    }


    private ViewHandler getOldViewHandler()
    {
        if (oldViewHandler == null)
        {
            oldViewHandler = new JspViewHandlerImpl();
        }
        return oldViewHandler;
    }

    public PssJspViewHandlerImpl() {
        super();
        if (log.isTraceEnabled()) log.trace("New partial ViewHandler instance created");
    }

    public Locale calculateLocale(FacesContext context)
    {
        return getOldViewHandler().calculateLocale(context);
    }

    public String calculateRenderKitId(FacesContext context)
    {
        return getOldViewHandler().calculateRenderKitId(context);
    }

    public UIViewRoot createView(FacesContext context, String viewId)
    {
        return getOldViewHandler().createView(context,viewId);
    }

    public String getActionURL(FacesContext context, String viewId)
    {
        return getOldViewHandler().getActionURL(context,viewId);
    }

    public String getResourceURL(FacesContext context, String path)
    {
        return getOldViewHandler().getResourceURL(context,path);
    }

    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        if (!isPartialStateSavingOn(context)) {
            log.fatal("Partial state saving ViewHandler is installed, but partial state saving is not enabled. Please enable partial state saving or use another ViewHandler.");
        }


        UIViewRoot root = null;

        Application application = context.getApplication();
        ViewHandler applicationViewHandler = application.getViewHandler();
        String renderKitId = applicationViewHandler.calculateRenderKitId(context);

        root = context.getApplication().getStateManager().restoreView(context,viewId,renderKitId);
        return root;
    }




    public void writeState(FacesContext context) throws IOException
    {
        getOldViewHandler().writeState(context);
    }

    public void renderView(FacesContext facesContext, UIViewRoot viewToRender)
            throws IOException, FacesException
    {
        if (viewToRender == null)
        {
            log.fatal("viewToRender must not be null");
            throw new NullPointerException("viewToRender must not be null");
        }

        ExternalContext externalContext = facesContext.getExternalContext();

        String viewId = facesContext.getViewRoot().getViewId();

        if (PortletUtil.isPortletRequest(facesContext)) {
            externalContext.dispatch(viewId);
            return;
        }

        ServletMapping servletMapping = getServletMapping(externalContext);

        if (servletMapping != null && servletMapping.isExtensionMapping())
        {
            String defaultSuffix = externalContext.getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
            String suffix = defaultSuffix != null ? defaultSuffix : ViewHandler.DEFAULT_SUFFIX;
            DebugUtils.assertError(suffix.charAt(0) == '.',
                                   log, "Default suffix must start with a dot!");
            if (!viewId.endsWith(suffix))
            {
                int slashPos = viewId.lastIndexOf('/');
                int extensionPos = viewId.lastIndexOf('.');
                if (extensionPos == -1 || extensionPos <= slashPos)
                {
                    if (log.isTraceEnabled()) log.trace("Current viewId has no extension, appending default suffix " + suffix);
                    viewId = viewId + suffix;
                }
                else
                {
                    if (log.isTraceEnabled()) log.trace("Replacing extension of current viewId by suffix " + suffix);
                    viewId = viewId.substring(0, extensionPos) + suffix;
                }
                facesContext.getViewRoot().setViewId(viewId);
            }
        }

        if (log.isTraceEnabled()) log.trace("Dispatching to " + viewId);

        // handle character encoding as of section 2.5.2.2 of JSF 1.1
      if (externalContext.getResponse() instanceof ServletResponse) {
            ServletResponse response = (ServletResponse) externalContext.getResponse();
            response.setLocale(viewToRender.getLocale());
        }

        ResponseWriter oldWriter = facesContext.getResponseWriter();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        RenderKitFactory renderFactory = (RenderKitFactory)
        FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit =
                renderFactory.getRenderKit(facesContext, viewToRender.getRenderKitId());


        BufferedStringWriter strWriter = new BufferedStringWriter(facesContext, bufSize);
        ResponseWriter newWriter;
        if (null != oldWriter) {
            newWriter = oldWriter.cloneWithWriter(strWriter);
        } else {
            newWriter = renderKit.createResponseWriter(strWriter, null,
                    request.getCharacterEncoding());
        }
        facesContext.setResponseWriter(newWriter);


        ResponseWriter responseWriter;
        if (null != oldWriter) {
            responseWriter = oldWriter.cloneWithWriter(response.getWriter());
        } else {
            response.reset();
            responseWriter = newWriter.cloneWithWriter(response.getWriter());
        }

        // now render the UIViewRoot
        newWriter.startDocument();
        newWriter.flush();
        EncodeAllComponentUtil.encodeAll(facesContext,viewToRender);
        newWriter.flush();
        newWriter.endDocument();

        facesContext.setResponseWriter(responseWriter);
        strWriter.flushToWriter(responseWriter);

        if (oldWriter != null)
        {
            facesContext.setResponseWriter(oldWriter);
        }

        if (facesContext.getExternalContext().getResponse()  instanceof ServletResponseWrapper)
            ((ServletResponseWrapper)facesContext.getExternalContext().getResponse()).setContentType("text/html");


        // handle character encoding as of section 2.5.2.2 of JSF 1.1
        if (externalContext.getRequest() instanceof HttpServletRequest) {
         HttpSession session = request.getSession(false);

            if (session != null) {
                session.setAttribute(ViewHandler.CHARACTER_ENCODING_KEY, response.getCharacterEncoding());
            }
        }

    }


    private static ServletMapping getServletMapping(ExternalContext externalContext)
    {
        String servletPath = externalContext.getRequestServletPath();
        String requestPathInfo = externalContext.getRequestPathInfo();

        WebXml webxml = WebXml.getWebXml(externalContext);
        List mappings = webxml.getFacesServletMappings();


        if (requestPathInfo == null)
        {
            // might be extension mapping
            for (int i = 0, size = mappings.size(); i < size; i++)
            {
                ServletMapping servletMapping = (ServletMapping) mappings.get(i);
                String urlpattern = servletMapping.getUrlPattern();
                String extension = urlpattern.substring(1, urlpattern.length());
                if (servletPath.endsWith(extension))
                {
                    return servletMapping;
                } else if (servletPath.equals(urlpattern)) {
                    // path mapping with no pathInfo for the current request
                    return servletMapping;
                }
            }
        }
        else
        {
            // path mapping
            for (int i = 0, size = mappings.size(); i < size; i++)
            {

                ServletMapping servletMapping = (ServletMapping) mappings.get(i);
                String urlpattern = servletMapping.getUrlPattern();
                urlpattern = urlpattern.substring(0, urlpattern.length() - 2);
                // servletPath starts with "/" except in the case where the
                // request is matched with the "/*" pattern, in which case
                // it is the empty string (see Servlet Sepc 2.3 SRV4.4)
                if (servletPath.equals(urlpattern))
                {
                    return servletMapping;
                }
            }
        }

        // handle cases as best possible where servletPath is not a faces servlet,
        // such as when coming through struts-faces
        if (mappings.size() > 0)
        {
            return (ServletMapping) mappings.get(0);
        }
        else
        {
            log.warn("no faces servlet mappings found");
            return null;
        }
    }
}
