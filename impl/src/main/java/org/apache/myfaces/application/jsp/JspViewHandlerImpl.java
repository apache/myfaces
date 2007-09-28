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
import org.apache.myfaces.application.DefaultViewHandlerSupport;
import org.apache.myfaces.application.InvalidViewIdException;
import org.apache.myfaces.application.ViewHandlerSupport;

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
import javax.faces.render.ResponseStateManager;
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
import java.util.Locale;

/**
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Bruno Aranda
 * @version $Revision$ $Date$
 */
public class JspViewHandlerImpl extends ViewHandler
{
    private static final Log log = LogFactory.getLog(JspViewHandlerImpl.class);
    public static final String FORM_STATE_MARKER = "<!--@@JSF_FORM_STATE_MARKER@@-->";
    public static final int FORM_STATE_MARKER_LEN = FORM_STATE_MARKER.length();

    private static final String AFTER_VIEW_TAG_CONTENT_PARAM = JspViewHandlerImpl.class + ".AFTER_VIEW_TAG_CONTENT";

    private ViewHandlerSupport _viewHandlerSupport;

    public JspViewHandlerImpl()
    {
        if (log.isTraceEnabled())
            log.trace("New ViewHandler instance created");
    }

    /**
     * @param viewHandlerSupport
     *            the viewHandlerSupport to set
     */
    public void setViewHandlerSupport(ViewHandlerSupport viewHandlerSupport)
    {
        _viewHandlerSupport = viewHandlerSupport;
    }

    /**
     * @return the viewHandlerSupport
     */
    protected ViewHandlerSupport getViewHandlerSupport()
    {
        if (_viewHandlerSupport == null)
        {
            _viewHandlerSupport = new DefaultViewHandlerSupport();
        }
        return _viewHandlerSupport;
    }

    public Locale calculateLocale(FacesContext facesContext)
    {
        Application application = facesContext.getApplication();
        for (Iterator<Locale> requestLocales = facesContext.getExternalContext().getRequestLocales(); requestLocales
                .hasNext();)
        {
            Locale requestLocale = requestLocales.next();
            for (Iterator<Locale> supportedLocales = application.getSupportedLocales(); supportedLocales.hasNext();)
            {
                Locale supportedLocale = supportedLocales.next();
                // higher priority to a language match over an exact match
                // that occures further down (see Jstl Reference 1.0 8.3.1)
                if (requestLocale.getLanguage().equals(supportedLocale.getLanguage())
                        && (supportedLocale.getCountry() == null || supportedLocale.getCountry().length() == 0))
                {
                    return supportedLocale;
                }
                else if (supportedLocale.equals(requestLocale))
                {
                    return supportedLocale;
                }
            }
        }

        Locale defaultLocale = application.getDefaultLocale();
        return defaultLocale != null ? defaultLocale : Locale.getDefault();
    }

    public String calculateRenderKitId(FacesContext facesContext)
    {
        Object renderKitId = facesContext.getExternalContext().getRequestMap().get(
                ResponseStateManager.RENDER_KIT_ID_PARAM);
        if (renderKitId == null)
        {
            renderKitId = facesContext.getApplication().getDefaultRenderKitId();
        }
        if (renderKitId == null)
        {
            renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
        }
        return renderKitId.toString();
    }

    /**
     */
    public UIViewRoot createView(FacesContext facesContext, String viewId)
    {
        String calculatedViewId = viewId;
        try
        {
            calculatedViewId = getViewHandlerSupport().calculateViewId(facesContext, viewId);
        }
        catch (InvalidViewIdException e)
        {
            sendSourceNotFound(facesContext, e.getMessage());
        }

        Application application = facesContext.getApplication();
        ViewHandler applicationViewHandler = application.getViewHandler();

        Locale currentLocale = null;
        String currentRenderKitId = null;
        UIViewRoot uiViewRoot = facesContext.getViewRoot();
        if (uiViewRoot != null)
        {
            // Remember current locale and renderKitId
            currentLocale = uiViewRoot.getLocale();
            currentRenderKitId = uiViewRoot.getRenderKitId();
        }

        uiViewRoot = (UIViewRoot) application.createComponent(UIViewRoot.COMPONENT_TYPE);

        uiViewRoot.setViewId(calculatedViewId);

        if (currentLocale != null)
        {
            // set old locale
            uiViewRoot.setLocale(currentLocale);
        }
        else
        {
            // calculate locale
            uiViewRoot.setLocale(applicationViewHandler.calculateLocale(facesContext));
        }

        if (currentRenderKitId != null)
        {
            // set old renderKit
            uiViewRoot.setRenderKitId(currentRenderKitId);
        }
        else
        {
            // calculate renderKit
            uiViewRoot.setRenderKitId(applicationViewHandler.calculateRenderKitId(facesContext));
        }

        if (log.isTraceEnabled())
            log.trace("Created view " + viewId);
        return uiViewRoot;
    }

    private void sendSourceNotFound(FacesContext context, String message)
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

    public String getActionURL(FacesContext facesContext, String viewId)
    {
        return getViewHandlerSupport().calculateActionURL(facesContext, viewId);
    }

    public String getResourceURL(FacesContext facesContext, String path)
    {
        if (path.length() > 0 && path.charAt(0) == '/')
        {
            return facesContext.getExternalContext().getRequestContextPath() + path;
        }

        return path;

    }

    public void renderView(FacesContext facesContext, UIViewRoot viewToRender) throws IOException, FacesException
    {
        if (viewToRender == null)
        {
            log.fatal("viewToRender must not be null");
            throw new NullPointerException("viewToRender must not be null");
        }

        // do not render the view if the rendered attribute for the view is false
        if (!viewToRender.isRendered())
        {
            if (log.isTraceEnabled())
                log.trace("View is not rendered");
            return;
        }

        ExternalContext externalContext = facesContext.getExternalContext();

        String viewId = facesContext.getViewRoot().getViewId();

        if (log.isTraceEnabled())
            log.trace("Rendering JSP view: " + viewId);
        
        ServletResponse response = (ServletResponse) externalContext.getResponse();
        ServletRequest request = (ServletRequest) externalContext.getRequest();

        Locale locale = viewToRender.getLocale();
        response.setLocale(locale);
        Config.set(request, Config.FMT_LOCALE, facesContext.getViewRoot().getLocale());

        if(!buildView(response, externalContext, viewId)) {
            //building the view was unsuccessful - an exception occurred during rendering
            //we need to jump out
            return;
        }

        // handle character encoding as of section 2.5.2.2 of JSF 1.1
        if (externalContext.getRequest() instanceof HttpServletRequest)
        {
            HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();
            HttpSession session = httpServletRequest.getSession(false);

            if (session != null)
            {
                session.setAttribute(ViewHandler.CHARACTER_ENCODING_KEY, response.getCharacterEncoding());
            }
        }

        // render the view in this method (since JSF 1.2)
        RenderKitFactory renderFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderFactory.getRenderKit(facesContext, viewToRender.getRenderKitId());

        StateMarkerAwareWriter stateAwareWriter = new StateMarkerAwareWriter();

        // Create a new response-writer using as an underlying writer the stateAwareWriter
        // Effectively, all output will be buffered in the stateAwareWriter so that later
        // this writer can replace the state-markers with the actual state.
        ResponseWriter oldResponseWriter = facesContext.getResponseWriter();
        ResponseWriter newResponseWriter = hookInStateAwareWriter(
                oldResponseWriter, stateAwareWriter, renderKit, externalContext);


        actuallyRenderView(facesContext, newResponseWriter, oldResponseWriter, viewToRender);

        //We're done with the document - now we can write all content
        //to the response, properly replacing the state-markers on the way out
        //by using the stateAwareWriter
        stateAwareWriter.flushToWriter(response.getWriter());

        // Final step - we output any content in the wrappedResponse response from above to the response,
        // removing the wrappedResponse response from the request, we don't need it anymore
        ViewResponseWrapper afterViewTagResponse = (ViewResponseWrapper) externalContext.getRequestMap().get(
                AFTER_VIEW_TAG_CONTENT_PARAM);
        externalContext.getRequestMap().remove(AFTER_VIEW_TAG_CONTENT_PARAM);

        if (afterViewTagResponse != null)
        {
            afterViewTagResponse.flushToWriter(response.getWriter());
        }

        response.flushBuffer();
    }

    /**Render the view now - properly setting and resetting the response writer
     *
     * @param facesContext
     * @param newResponseWriter
     * @param viewToRender
     * @throws IOException
     */
    private void actuallyRenderView(FacesContext facesContext,
                                    ResponseWriter newResponseWriter, ResponseWriter oldResponseWriter,
                                    UIViewRoot viewToRender) throws IOException {
        // Set the new ResponseWriter into the FacesContext, saving the old one aside.
        facesContext.setResponseWriter(newResponseWriter);

        //Now we actually render the document
        // Call startDocument() on the ResponseWriter.
        newResponseWriter.startDocument();

        // Call encodeAll() on the UIViewRoot
        viewToRender.encodeAll(facesContext);

        // Call endDocument() on the ResponseWriter
        newResponseWriter.endDocument();

        newResponseWriter.flush();

        // If the old ResponseWriter was not null, place the old ResponseWriter back
        // into the FacesContext.
        if (oldResponseWriter != null)
        {
            facesContext.setResponseWriter(oldResponseWriter);
        }
    }

    /**Create a new response-writer using as an underlying writer the stateAwareWriter
     * Effectively, all output will be buffered in the stateAwareWriter so that later
     * this writer can replace the state-markers with the actual state.
     *
     * If the FacesContext has a non-null ResponseWriter create a new writer using its
     * cloneWithWriter() method, passing the response's Writer as the argument.
     * Otherwise, use the current RenderKit to create a new ResponseWriter.
     *
     * @param oldResponseWriter
     * @param stateAwareWriter
     * @param renderKit
     * @param externalContext
     * @return
     */
    private ResponseWriter hookInStateAwareWriter(ResponseWriter oldResponseWriter, StateMarkerAwareWriter stateAwareWriter, RenderKit renderKit, ExternalContext externalContext) {
        ResponseWriter newResponseWriter;
        if (oldResponseWriter != null)
        {
            newResponseWriter = oldResponseWriter.cloneWithWriter(stateAwareWriter);
        }
        else
        {
            if (log.isTraceEnabled())
                log.trace("Creating new ResponseWriter");
            newResponseWriter = renderKit.createResponseWriter(stateAwareWriter, null,
                    ((HttpServletRequest) externalContext.getRequest()).getCharacterEncoding());
        }
        return newResponseWriter;
    }

    /**Build the view-tree before rendering.
     * This is done by dispatching to the underlying JSP-page, effectively processing it, creating
     * components out of any text in between JSF components (not rendering the text to the output of course, this
     * will happen later while rendering), attaching these components
     * to the component tree, and buffering any content after the view-root.
     *
     * @param response The current response - it will be replaced while the view-building happens (we want the text in the component tree, not on the actual servlet output stream)
     * @param externalContext The external context where the response will be replaced while building
     * @param viewId The view-id to dispatch to
     * @return true if successfull, false if an error occurred during rendering
     * @throws IOException
     */
    private boolean buildView(ServletResponse response, ExternalContext externalContext, String viewId) throws IOException {
        ViewResponseWrapper wrappedResponse = new ViewResponseWrapper((HttpServletResponse) response);

        externalContext.setResponse(wrappedResponse);
        externalContext.dispatch(viewId);
        externalContext.setResponse(response);

        boolean errorResponse = wrappedResponse.getStatus() < 200 || wrappedResponse.getStatus() > 299;
        if (errorResponse)
        {
            wrappedResponse.flushToWrappedResponse();
            return false;
        }

        // store the wrapped response in the request, so it is thread-safe
        externalContext.getRequestMap().put(AFTER_VIEW_TAG_CONTENT_PARAM, wrappedResponse);

        return true;
    }

    public UIViewRoot restoreView(FacesContext facesContext, String viewId)
    {
        Application application = facesContext.getApplication();
        ViewHandler applicationViewHandler = application.getViewHandler();
        String renderKitId = applicationViewHandler.calculateRenderKitId(facesContext);
        String calculatedViewId = getViewHandlerSupport().calculateViewId(facesContext, viewId);
        UIViewRoot viewRoot = application.getStateManager().restoreView(facesContext, calculatedViewId, renderKitId);
        return viewRoot;
    }

    /**
     * Writes a state marker that is replaced later by one or more hidden form inputs.
     * 
     * @param facesContext
     * @throws IOException
     */
    public void writeState(FacesContext facesContext) throws IOException
    {
        facesContext.getResponseWriter().write(FORM_STATE_MARKER);
    }

    /**
     * Writes the response and replaces the state marker tags with the state information for the current context
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

            int form_marker;
            while ((form_marker = contentBuffer.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER)) > -1 )
            {
                //FORM_STATE_MARKER found, replace it
                contentBuffer.replace(form_marker, form_marker + FORM_STATE_MARKER_LEN, state.toString());
            }

            int bufferLength = contentBuffer.length();
            int index = 0;
            int bufferSize = 512;

            while (index < bufferLength)
            {
                int maxSize = Math.min(bufferSize, bufferLength - index);
                char[] bufToWrite = new char[maxSize];

                contentBuffer.getChars(index, index + maxSize, bufToWrite, 0);
                writer.write(bufToWrite);

                index += bufferSize;
            }

        }
    }

}
