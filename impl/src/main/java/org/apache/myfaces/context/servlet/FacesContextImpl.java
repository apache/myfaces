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
package org.apache.myfaces.context.servlet;

import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.context.portlet.PortletExternalContextImpl;
import org.apache.myfaces.el.unified.FacesELContext;
import org.apache.myfaces.shared_impl.util.NullIterator;

import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
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
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.*;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class FacesContextImpl extends FacesContext
{
    // ~ Instance fields ----------------------------------------------------------------------------

    
    // TODO: I think a Map<String, List<FacesMessage>> would more efficient than those two -= Simon Lessard =-
    private List<FacesMessage> _messages = null;
    private List<String> _messageClientIds = null;
    
    private Application _application;
    private ReleaseableExternalContext _externalContext;
    private ResponseStream _responseStream = null;
    private ResponseWriter _responseWriter = null;
    private FacesMessage.Severity _maximumSeverity = null;
    private UIViewRoot _viewRoot;
    private boolean _renderResponse = false;
    private boolean _responseComplete = false;
    private RenderKitFactory _renderKitFactory;
    private boolean _released = false;
    private ELContext _elContext;

    // ~ Constructors -------------------------------------------------------------------------------

    public FacesContextImpl(final PortletContext portletContext, final PortletRequest portletRequest,
                            final PortletResponse portletResponse)
    {
        this(new PortletExternalContextImpl(portletContext, portletRequest, portletResponse));
    }

    public FacesContextImpl(final ServletContext servletContext, final ServletRequest servletRequest,
                            final ServletResponse servletResponse)
    {
        this(new ServletExternalContextImpl(servletContext, servletRequest, servletResponse));
    }

    private FacesContextImpl(final ReleaseableExternalContext externalContext)
    {
        _application = ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();
        _renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        _externalContext = externalContext;
        FacesContext.setCurrentInstance(this); // protected method, therefore must be called from here
    }

    // ~ Methods ------------------------------------------------------------------------------------

    public final ExternalContext getExternalContext()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        return (ExternalContext) _externalContext;
    }

    public final FacesMessage.Severity getMaximumSeverity()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        
        return _maximumSeverity;
    }

    public final Iterator<FacesMessage> getMessages()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        
        if (_messages == null)
        {
            return NullIterator.instance();
        }
        
        return _messages.iterator();
    }

    public final Application getApplication()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }

        return _application;
    }
    
    public final Iterator<String> getClientIdsWithMessages()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        
        if (_messages == null || _messages.isEmpty())
        {
            return NullIterator.instance();
        }

        final Set<String> uniqueClientIds = new LinkedHashSet<String>(_messageClientIds);
        
        return uniqueClientIds.iterator();
    }

    public final Iterator<FacesMessage> getMessages(final String clientId)
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        
        if (_messages == null)
        {
            return NullIterator.instance();
        }

        List<FacesMessage> lst = new ArrayList<FacesMessage>();
        for (int i = 0; i < _messages.size(); i++)
        {
            Object savedClientId = _messageClientIds.get(i);
            if (clientId == null)
            {
                if (savedClientId == null)
                {
                    lst.add(_messages.get(i));
                }
            }
            else
            {
                if (clientId.equals(savedClientId))
                {
                    lst.add(_messages.get(i));
                }
            }
        }
        
        return lst.iterator();
    }

    public final RenderKit getRenderKit()
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

    public final boolean getRenderResponse()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        return _renderResponse;
    }

    public final boolean getResponseComplete()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseComplete;
    }

    public final void setResponseStream(final ResponseStream responseStream)
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        if (responseStream == null)
        {
            throw new NullPointerException("responseStream");
        }
        _responseStream = responseStream;
    }

    public final ResponseStream getResponseStream()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseStream;
    }

    public final void setResponseWriter(final ResponseWriter responseWriter)
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        if (responseWriter == null)
        {
            throw new NullPointerException("responseWriter");
        }
        _responseWriter = responseWriter;
    }

    public final ResponseWriter getResponseWriter()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseWriter;
    }

    public final void setViewRoot(final UIViewRoot viewRoot)
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        if (viewRoot == null)
        {
            throw new NullPointerException("viewRoot");
        }
        _viewRoot = viewRoot;
    }

    public final UIViewRoot getViewRoot()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        return _viewRoot;
    }

    public final void addMessage(final String clientId, final FacesMessage message)
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        if (message == null)
        {
            throw new NullPointerException("message");
        }

        if (_messages == null)
        {
            _messages = new ArrayList<FacesMessage>();
            _messageClientIds = new ArrayList<String>();
        }
        _messages.add(message);
        _messageClientIds.add((clientId != null) ? clientId : null);
        FacesMessage.Severity serSeverity = message.getSeverity();
        if (serSeverity != null)
        {
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

    public final void release()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        if (_externalContext != null)
        {
            _externalContext.release();
            _externalContext = null;
        }

        _messageClientIds = null;
        _messages = null;
        _application = null;
        _responseStream = null;
        _responseWriter = null;
        _viewRoot = null;

        _released = true;
        FacesContext.setCurrentInstance(null);
    }

    public final void renderResponse()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        _renderResponse = true;
    }

    public final void responseComplete()
    {
        if (_released)
        {
            throw new IllegalStateException("FacesContext already released");
        }
        _responseComplete = true;
    }

    // Portlet need to do this to change from ActionRequest/Response to
    // RenderRequest/Response
    public final void setExternalContext(ReleaseableExternalContext extContext)
    {
        _externalContext = extContext;
        FacesContext.setCurrentInstance(this); // TODO: figure out if I really need to do this
    }

    public final ELContext getELContext()
    {
        if (_elContext != null)
            return _elContext;

        _elContext = new FacesELContext(getApplication().getELResolver(), this);

        ELContextEvent event = new ELContextEvent(_elContext);
        for (ELContextListener listener : getApplication().getELContextListeners())
        {
            listener.contextCreated(event);
        }

        return _elContext;
    }

}
