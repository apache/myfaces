/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.application.jsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.portlet.MyFacesGenericPortlet;
import org.apache.myfaces.portlet.PortletUtil;
import org.apache.myfaces.shared_impl.webapp.webxml.ServletMapping;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;
import org.apache.myfaces.util.DebugUtils;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Bruno Aranda
 * @version $Revision$ $Date$
 */
public class JspViewHandlerImpl
        extends ViewHandler
{
    private static final Log log = LogFactory.getLog(JspViewHandlerImpl.class);
    public static final String FORM_STATE_MARKER     = "<!--@@JSF_FORM_STATE_MARKER@@-->";
    public static final int    FORM_STATE_MARKER_LEN = FORM_STATE_MARKER.length();

    private static final String AFTER_VIEW_TAG_CONTENT_PARAM = JspViewHandlerImpl.class+".AFTER_VIEW_TAG_CONTENT";

    public JspViewHandlerImpl()
    {
        if (log.isTraceEnabled()) log.trace("New ViewHandler instance created");
    }

    public Locale calculateLocale(FacesContext facesContext)
    {
        Iterator locales = facesContext.getExternalContext().getRequestLocales();
        while (locales.hasNext())
        {
            Locale locale = (Locale)locales.next();
            for (Iterator it = facesContext.getApplication().getSupportedLocales(); it.hasNext();)
            {
                Locale supportLocale = (Locale)it.next();
                // higher priority to a language match over an exact match
                // that occures further down (see Jstl Reference 1.0 8.3.1)
                if (locale.getLanguage().equals(supportLocale.getLanguage()) &&
                    (supportLocale.getCountry() == null ||
                     supportLocale.getCountry().length() == 0))
                {
                    return supportLocale;
                }
                else if (supportLocale.equals(locale))
                {
                    return supportLocale;
                }
            }
        }

        Locale defaultLocale = facesContext.getApplication().getDefaultLocale();
        return defaultLocale != null ? defaultLocale : Locale.getDefault();
    }

    public String calculateRenderKitId(FacesContext facesContext)
    {
    	String renderKitId = facesContext.getApplication().getDefaultRenderKitId();
        return (renderKitId!=null) ? renderKitId : RenderKitFactory.HTML_BASIC_RENDER_KIT;
        //TODO: how to calculate from client?
    }

    /**
     */
    public UIViewRoot createView(FacesContext facesContext, String viewId)
    {
        Application application = facesContext.getApplication();
        ViewHandler applicationViewHandler = application.getViewHandler();

        Locale currentLocale = null;
        String currentRenderKitId = null;
        UIViewRoot uiViewRoot = facesContext.getViewRoot();
        if (uiViewRoot != null)
        {
            //Remember current locale and renderKitId
            currentLocale = uiViewRoot.getLocale();
            currentRenderKitId = uiViewRoot.getRenderKitId();
        }

        uiViewRoot = (UIViewRoot)application.createComponent(UIViewRoot.COMPONENT_TYPE);
//      as of JSF spec page 7-16:
//      "It is the callers responsibility to ensure that setViewId() is called
//      on the returned view, passing the same viewId value."
//      so we do not set the viewId here

//      ok, but the RI does so, so let's do it, too.
        uiViewRoot.setViewId(viewId);

        if (currentLocale != null)
        {
            //set old locale
            uiViewRoot.setLocale(currentLocale);
        }
        else
        {
            //calculate locale
            uiViewRoot.setLocale(applicationViewHandler.calculateLocale(facesContext));
        }

        if (currentRenderKitId != null)
        {
            //set old renderKit
            uiViewRoot.setRenderKitId(currentRenderKitId);
        }
        else
        {
            //calculate renderKit
            uiViewRoot.setRenderKitId(applicationViewHandler.calculateRenderKitId(facesContext));
        }

        if (log.isTraceEnabled()) log.trace("Created view " + viewId);
        return uiViewRoot;
    }

    public String getActionURL(FacesContext facesContext, String viewId)
    {
        if (PortletUtil.isRenderResponse(facesContext))
        {
            RenderResponse response = (RenderResponse)facesContext.getExternalContext().getResponse();
            PortletURL url = response.createActionURL();
            url.setParameter(MyFacesGenericPortlet.VIEW_ID, viewId);
            return url.toString();
        }

        String path = getViewIdPath(facesContext, viewId);
        if (path.length() > 0 && path.charAt(0) == '/')
        {
            return facesContext.getExternalContext().getRequestContextPath() + path;
        }
        else
        {
            return path;
        }
    }

    public String getResourceURL(FacesContext facesContext, String path)
    {
        if (path.length() > 0 && path.charAt(0) == '/')
        {
            return facesContext.getExternalContext().getRequestContextPath() + path;
        }
        else
        {
            return path;
        }
    }

    public void renderView(FacesContext facesContext, UIViewRoot viewToRender)
            throws IOException, FacesException
    {
        if (viewToRender == null)
        {
            log.fatal("viewToRender must not be null");
            throw new NullPointerException("viewToRender must not be null");
        }

        // do not render the view if the rendered attribute for the view is false
        if (!viewToRender.isRendered())
        {
            if (log.isTraceEnabled()) log.trace("View is not rendered");
            return;
        }

        ExternalContext externalContext = facesContext.getExternalContext();

        String viewId = facesContext.getViewRoot().getViewId();

        if (log.isTraceEnabled()) log.trace("Rendering JSP view: "+viewId);

        if (PortletUtil.isPortletRequest(facesContext)) {
             if (log.isTraceEnabled()) log.trace("It is a portlet request. Dispatching to view");
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
                int dot = viewId.lastIndexOf('.');
                if (dot == -1)
                {
                    if (log.isTraceEnabled()) log.trace("Current viewId has no extension, appending default suffix " + suffix);
                    viewId = viewId + suffix;
                }
                else
                {
                    if (log.isTraceEnabled()) log.trace("Replacing extension of current viewId by suffix " + suffix);
                    viewId = viewId.substring(0, dot) + suffix;
                }
                facesContext.getViewRoot().setViewId(viewId);
            }
        }

        if (log.isTraceEnabled()) log.trace("Dispatching to " + viewId);

        ServletResponse response = (ServletResponse) externalContext.getResponse();
        ServletRequest request = (ServletRequest) externalContext.getRequest();

            Locale locale = viewToRender.getLocale();
            response.setLocale(locale);
            Config.set(request,
                       Config.FMT_LOCALE,
                       facesContext.getViewRoot().getLocale());

        ViewResponseWrapper wrappedResponse = new ViewResponseWrapper((HttpServletResponse)response);

        externalContext.setResponse(wrappedResponse);
        externalContext.dispatch(viewId);
        externalContext.setResponse(response);

        boolean errorResponse = wrappedResponse.getStatus() < 200 || wrappedResponse.getStatus() > 299;
        if (errorResponse) {
            wrappedResponse.flushToWrappedResponse();
        }

        // store the wrapped response in the request, so it is thread-safe
        externalContext.getRequestMap().put(AFTER_VIEW_TAG_CONTENT_PARAM, wrappedResponse);
        

        // handle character encoding as of section 2.5.2.2 of JSF 1.1
        if (externalContext.getRequest() instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();
            HttpSession session = httpServletRequest.getSession(false);

            if (session != null) {
                session.setAttribute(ViewHandler.CHARACTER_ENCODING_KEY, response.getCharacterEncoding());
            }
        }
        
        // render the view in this method (since JSF 1.2)
        RenderKitFactory renderFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderFactory.getRenderKit(facesContext, viewToRender.getRenderKitId());

        ResponseWriter newResponseWriter;
        StateMarkerAwareWriter stateAwareWriter = new StateMarkerAwareWriter();

        // If the FacesContext has a non-null ResponseWriter create a new writer using its
        // cloneWithWriter() method, passing the response's Writer as the argument.
        // Otherwise, use the current RenderKit to create a new ResponseWriter.
        ResponseWriter oldResponseWriter = facesContext.getResponseWriter();
        if (oldResponseWriter != null)
        {
            newResponseWriter = oldResponseWriter.cloneWithWriter(stateAwareWriter);
        }
        else
        {
            if (log.isTraceEnabled()) log.trace("Creating new ResponseWriter");
            newResponseWriter = renderKit.createResponseWriter(stateAwareWriter, null,
                    ((HttpServletRequest) externalContext.getRequest()).getCharacterEncoding());
        }

        // Set the new ResponseWriter into the FacesContext, saving the old one aside.
        facesContext.setResponseWriter(newResponseWriter);

        // Call startDocument() on the ResponseWriter.
        newResponseWriter.startDocument();

        // Call encodeAll() on the UIViewRoot
        viewToRender.encodeAll(facesContext);

        ResponseWriter responseWriter;
        if (oldResponseWriter != null) {
            responseWriter = oldResponseWriter.cloneWithWriter(response.getWriter());
        } else {
            responseWriter = newResponseWriter.cloneWithWriter(response.getWriter());
        }
        facesContext.setResponseWriter(responseWriter);

        //response.getWriter().write(stateAwareWriter.parseResponse());
        stateAwareWriter.flushToWriter(response.getWriter());

        // Output any content in the wrappedResponse response from above to the response, removing the
        // wrappedResponse response from the thread-safe storage.
        ViewResponseWrapper afterViewTagResponse = (ViewResponseWrapper) externalContext.getRequestMap().get(AFTER_VIEW_TAG_CONTENT_PARAM);
        externalContext.getRequestMap().remove(AFTER_VIEW_TAG_CONTENT_PARAM);
        response.getWriter().write(afterViewTagResponse.toString());

        // Call endDocument() on the ResponseWriter
        newResponseWriter.endDocument();

        // If the old ResponseWriter was not null, place the old ResponseWriter back
        // into the FacesContext.
        if (oldResponseWriter != null)
        {
            facesContext.setResponseWriter(oldResponseWriter);
        }
        
        response.flushBuffer();
    }


    public UIViewRoot restoreView(FacesContext facesContext, String viewId)
    {
        Application application = facesContext.getApplication();
        ViewHandler applicationViewHandler = application.getViewHandler();
        String renderKitId = applicationViewHandler.calculateRenderKitId(facesContext);
        UIViewRoot viewRoot = application.getStateManager().restoreView(facesContext,
                                                                        viewId,
                                                                        renderKitId);
        return viewRoot;
    }

    /**
     * Writes a state marker that is replaced later by one or more hidden form
     * inputs.
     * @param facesContext
     * @throws IOException
     */
    public void writeState(FacesContext facesContext) throws IOException
    {
        facesContext.getResponseWriter().write(FORM_STATE_MARKER);
    }


    protected String getViewIdPath(FacesContext facescontext, String viewId)
    {
        if (viewId == null)
        {
            log.error("ViewId must not be null");
            throw new NullPointerException("ViewId must not be null");
        }
        if (!viewId.startsWith("/"))
        {
            log.error("ViewId must start with '/' (viewId = " + viewId + ")");
            throw new IllegalArgumentException("ViewId must start with '/' (viewId = " + viewId + ")");
        }

        if (PortletUtil.isPortletRequest(facescontext))
        {
            return viewId;
        }

        ServletMapping servletMapping = getServletMapping(facescontext.getExternalContext());

        if (servletMapping !=null)
        {
            if(servletMapping.isExtensionMapping())
            {
                // extension mapping
                String urlpattern = servletMapping.getUrlPattern();
                if (urlpattern.startsWith("*"))
                {
                    urlpattern = urlpattern.substring(1, urlpattern.length());
                }
                if (viewId.endsWith(urlpattern))
                {
                    return viewId;
                }
                else
                {
                    int idx = viewId.lastIndexOf(".");
                    if (idx >= 0)
                    {
                        return viewId.substring(0, idx) + urlpattern;
                    }
                    else
                    {
                        return viewId + urlpattern;
                    }

                }
            }
            else
            {
                // prefix mapping
                String urlpattern = servletMapping.getUrlPattern();
                if (urlpattern.endsWith("/*"))
                {
                    urlpattern = urlpattern.substring(0, urlpattern.length() - 2);
                }
                return urlpattern + viewId;
            }
        }
        else
        {
            return viewId;
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

    /**
     * Writes the response and replaces the state marker tags with the state information
     * for the current context
     */
    private static class StateMarkerAwareWriter extends StringWriter
    {
         public StateMarkerAwareWriter()
         {
         }

        public void flushToWriter(Writer writer) throws IOException
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            StateManager stateManager = facesContext.getApplication().getStateManager();

            StringWriter stateWriter = new StringWriter();
            ResponseWriter realWriter = facesContext.getResponseWriter();
            facesContext.setResponseWriter(realWriter.cloneWithWriter(stateWriter));

            Object serializedView = stateManager.saveView(facesContext);

            stateManager.writeState(facesContext, serializedView);
            facesContext.setResponseWriter(realWriter);

            StringBuffer contentBuffer = getBuffer();
            StringBuffer state = stateWriter.getBuffer();

            int form_marker = contentBuffer.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER);
            contentBuffer.replace(form_marker, form_marker+FORM_STATE_MARKER_LEN, state.toString());

            int bufferLength = contentBuffer.length();
            int index = 0;
            int bufferSize = 512;

            while (index < bufferLength)
            {
                int maxSize = Math.min(bufferSize,bufferLength-index);
                char[] bufToWrite = new char[maxSize];

                contentBuffer.getChars(index, index+maxSize, bufToWrite, 0);
                writer.write(bufToWrite);

                index += bufferSize;
            }

        }
    }


}
