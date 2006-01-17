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
package org.apache.myfaces.context.servlet;

import org.apache.myfaces.util.NullIterator;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.*;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.context.portlet.PortletExternalContextImpl;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class ServletFacesContextImpl
        extends FacesContext
{
    //~ Instance fields ----------------------------------------------------------------------------

    private List                        _messageClientIds = null;
    private List                        _messages         = null;
    private Application                 _application;
    private ReleaseableExternalContext  _externalContext;
    private ResponseStream              _responseStream   = null;
    private ResponseWriter              _responseWriter   = null;
    private FacesMessage.Severity       _maximumSeverity  = null;
    private UIViewRoot                  _viewRoot;
    private boolean                     _renderResponse   = false;
    private boolean                     _responseComplete = false;
    private RenderKitFactory            _renderKitFactory;
    private boolean                     _released = false;

    //~ Constructors -------------------------------------------------------------------------------

    // TODO: FIXME: the name of this class should be changed.
    public ServletFacesContextImpl(PortletContext portletContext,
                                   PortletRequest portletRequest,
                                   PortletResponse portletResponse)
    {
        this(new PortletExternalContextImpl(portletContext,
                portletRequest,
                portletResponse));
    }

    public ServletFacesContextImpl(ServletContext servletContext,
                                   ServletRequest servletRequest,
                                   ServletResponse servletResponse)
    {
        this(new ServletExternalContextImpl(servletContext,
                servletRequest,
                servletResponse));
    }

    private ServletFacesContextImpl(ReleaseableExternalContext externalContext)
    {
        _application = ((ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY))
                .getApplication();
        _renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        _externalContext = externalContext;
        FacesContext.setCurrentInstance(this);  //protected method, therefore must be called from here
    }

    //~ Methods ------------------------------------------------------------------------------------

    public ExternalContext getExternalContext()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return (ExternalContext)_externalContext;
    }

    public FacesMessage.Severity getMaximumSeverity()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _maximumSeverity;
    }

    public Iterator getMessages()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return (_messages != null) ? _messages.iterator() : Collections.EMPTY_LIST.iterator();
    }

    public Application getApplication()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }

        return _application;
    }

    public Iterator getClientIdsWithMessages()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (_messages == null || _messages.isEmpty())
        {
            return NullIterator.instance();
        }

        final Set uniqueClientIds = new LinkedHashSet(_messageClientIds);
        return uniqueClientIds.iterator();
    }

    public Iterator getMessages(String clientId)
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (_messages == null)
        {
            return NullIterator.instance();
        }

        List lst = new ArrayList();
        for (int i = 0; i < _messages.size(); i++)
        {
            Object savedClientId = _messageClientIds.get(i);
            if (clientId == null)
            {
                if (savedClientId == null) lst.add(_messages.get(i));
            }
            else
            {
                if (clientId.equals(savedClientId)) lst.add(_messages.get(i));
            }
        }
        return lst.iterator();
    }

    public RenderKit getRenderKit()
    {
        if (getViewRoot() == null)
        {
            return null;
        }

        String renderKitId = getViewRoot().getRenderKitId();

        if (renderKitId == null)
        {
            return null;
        }

        return _renderKitFactory.getRenderKit(this, renderKitId);
    }

    public boolean getRenderResponse()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _renderResponse;
    }

    public boolean getResponseComplete()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseComplete;
    }

    public void setResponseStream(ResponseStream responseStream)
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (responseStream == null)
        {
            throw new NullPointerException("responseStream");
        }
        _responseStream = responseStream;
    }

    public ResponseStream getResponseStream()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseStream;
    }

    public void setResponseWriter(ResponseWriter responseWriter)
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (responseWriter == null)
        {
            throw new NullPointerException("responseWriter");
        }
        _responseWriter = responseWriter;
    }

    public ResponseWriter getResponseWriter()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseWriter;
    }

    public void setViewRoot(UIViewRoot viewRoot)
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (viewRoot == null)
        {
            throw new NullPointerException("viewRoot");
        }
        _viewRoot = viewRoot;
    }

    public UIViewRoot getViewRoot()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _viewRoot;
    }

    public void addMessage(String clientId, FacesMessage message)
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (message == null)
        {
            throw new NullPointerException("message");
        }

        if (_messages == null)
        {
            _messages             = new ArrayList();
            _messageClientIds     = new ArrayList();
        }
        _messages.add(message);
        _messageClientIds.add((clientId != null) ? clientId : null);
        FacesMessage.Severity serSeverity =  message.getSeverity();
        if (serSeverity != null) {
            if (_maximumSeverity == null)
            {
                _maximumSeverity = serSeverity;
            }
            else if (serSeverity.compareTo(_maximumSeverity) > 0)
            {
                _maximumSeverity = serSeverity;
            }
        }
    }

    public void release()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (_externalContext != null)
        {
            _externalContext.release();
            _externalContext = null;
        }

        _messageClientIds     = null;
        _messages             = null;
        _application          = null;
        _responseStream       = null;
        _responseWriter       = null;
        _viewRoot             = null;

        _released             = true;
        FacesContext.setCurrentInstance(null);
    }

    public void renderResponse()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        _renderResponse = true;
    }

    public void responseComplete()
    {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        _responseComplete = true;
    }

    // Portlet need to do this to change from ActionRequest/Response to
    // RenderRequest/Response
    public void setExternalContext(ReleaseableExternalContext extContext)
    {
        _externalContext = extContext;
        FacesContext.setCurrentInstance(this); //TODO: figure out if I really need to do this
    }
}
