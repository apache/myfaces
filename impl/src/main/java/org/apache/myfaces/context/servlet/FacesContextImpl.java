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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.PartialViewContextFactory;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseId;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.myfaces.cdi.FacesScopeContext;
import org.apache.myfaces.cdi.view.ViewTransientScopeContext;

import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.context.ReleasableFacesContextFactory;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class FacesContextImpl extends FacesContextImplBase
{
    static final String RE_SPLITTER = "[\\s\\t\\r\\n]*\\,[\\s\\t\\r\\n]*";
    
    // ~ Instance fields ----------------------------------------------------------------------------
    
    private Map<String, List<FacesMessage>> _messages = null;
    private List<FacesMessage> _orderedMessages = null;
    private PhaseId _currentPhaseId;
    private ResponseStream _responseStream = null;
    private ResponseWriter _responseWriter = null;
    private FacesMessage.Severity _maximumSeverity = null;
    private boolean _renderResponse = false;
    private boolean _responseComplete = false;
    private boolean _validationFailed = false;
    private PartialViewContext _partialViewContext = null;
    private ReleasableFacesContextFactory _facesContextFactory = null;
    
    private PartialViewContextFactory _partialViewContextFactory = null;
    private RenderKitFactory _renderKitFactory = null;
    private Lifecycle _lifecycle = null;

    // ~ Constructors -------------------------------------------------------------------------------
    
    /**
     * Creates a FacesContextImpl with a ServletExternalContextImpl.
     */
    public FacesContextImpl(final ServletContext servletContext, final ServletRequest servletRequest,
                            final ServletResponse servletResponse)
    {
        this(new ServletExternalContextImpl(servletContext, servletRequest, servletResponse));
    }
    
    /**
     * Private constructor used in internal construtor chain.
     * @param externalContext
     */
    private FacesContextImpl(ServletExternalContextImpl externalContext)
    {
        this(externalContext, externalContext, null);
    }
    
    /**
     * Creates a FacesContextImpl with the given ExternalContext,
     * ReleaseableExternalContext and ReleaseableFacesContextFactory.
     * @param externalContext
     * @param defaultExternalContext
     * @param facesContextFactory
     */
    public FacesContextImpl(final ExternalContext externalContext,
            final ExternalContext defaultExternalContext , 
            final ReleasableFacesContextFactory facesContextFactory)
    {
        // setCurrentInstance is called in constructor of super class
        super(externalContext, defaultExternalContext);
        
        _facesContextFactory = facesContextFactory;
    }
    
    public FacesContextImpl(final ExternalContext externalContext,
            final ExternalContext defaultExternalContext , 
            final ReleasableFacesContextFactory facesContextFactory,
            final ApplicationFactory applicationFactory,
            final RenderKitFactory renderKitFactory,
            final PartialViewContextFactory partialViewContextFactory,
            final Lifecycle lifecycle)
    {
        // setCurrentInstance is called in constructor of super class
        super(externalContext, defaultExternalContext, applicationFactory, 
                renderKitFactory);
        
        _facesContextFactory = facesContextFactory;
        _renderKitFactory = renderKitFactory;
        _partialViewContextFactory = partialViewContextFactory;
        _lifecycle = lifecycle;
    }

    // ~ Methods ------------------------------------------------------------------------------------
    
    @Override
    public final void release()
    {
        assertNotReleased();
        if (ExternalSpecifications.isCDIAvailable(getExternalContext()))
        {
            ViewTransientScopeContext.destroyAll(this);
            FacesScopeContext.destroyAll(this);
        }

        _messages = null;
        _orderedMessages = null;
        _currentPhaseId = null;
        _responseStream = null;
        _responseWriter = null;
        _maximumSeverity = null;
        _partialViewContext = null;
        _renderKitFactory = null;
        _partialViewContextFactory = null;
        _lifecycle = null;

        if (_facesContextFactory != null)
        {
            _facesContextFactory.release();
            _facesContextFactory = null;
        }
        
        // release FacesContextImplBase (sets current instance to null)
        super.release();
    }
    
    @Override
    public final FacesMessage.Severity getMaximumSeverity()
    {
        assertNotReleased();

        return _maximumSeverity;
    }
    
    @Override
    public final void addMessage(final String clientId, final FacesMessage message)
    {
        assertNotReleased();

        Assert.notNull(message, "message");

        if (_messages == null)
        {
            _messages = new LinkedHashMap<>(5, 1f);
            _orderedMessages = new ArrayList<>();
        }

        List<FacesMessage> lst = _messages.computeIfAbsent(clientId, k -> new ArrayList<>(3));         
        lst.add(message);

        _orderedMessages.add(message);
        
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

    @Override
    public List<FacesMessage> getMessageList()
    {
        assertNotReleased();
        
        if (_messages == null)
        {
            return Collections.unmodifiableList(Collections.emptyList());
        }
        
        return Collections.unmodifiableList(_orderedMessages);
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId)
    {
        assertNotReleased();
        
        if (_messages == null || !_messages.containsKey(clientId))
        {
            return Collections.unmodifiableList(Collections.emptyList());
        }
        
        return _messages.get(clientId);
    }

    @Override
    public final Iterator<FacesMessage> getMessages()
    {
        assertNotReleased();

        if (_messages == null)
        {
            return Collections.emptyIterator();
        }
        
        return _orderedMessages.iterator();
    }

    @Override
    public final Iterator<FacesMessage> getMessages(final String clientId)
    {

        assertNotReleased();

        if (_messages == null || !_messages.containsKey(clientId))
        {
            return Collections.emptyIterator();
        }
        
        return _messages.get(clientId).iterator();        
    }
    
    @Override
    public final Iterator<String> getClientIdsWithMessages()
    {
        assertNotReleased();

        if (_messages == null || _messages.isEmpty())
        {
            return Collections.emptyIterator();
        }
        
        return _messages.keySet().iterator();
    }
    
    @Override
    public PhaseId getCurrentPhaseId()
    {
        assertNotReleased();

        return _currentPhaseId;
    }
    
    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        assertNotReleased();

        _currentPhaseId = currentPhaseId;
    }
    
    @Override
    public PartialViewContext getPartialViewContext()
    {
        assertNotReleased();

        if (_partialViewContext == null)
        {
            //Get through factory finder
            if (_partialViewContextFactory == null)
            {
                _partialViewContextFactory = (PartialViewContextFactory)
                    FactoryFinder.getFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
            }
            // Put actual facesContext as param, not this - this can be wrapped
            _partialViewContext = _partialViewContextFactory.getPartialViewContext(getCurrentFacesContext());
        }
        return _partialViewContext;
    }

    @Override
    public final boolean getRenderResponse()
    {
        assertNotReleased();

        return _renderResponse;
    }
    
    @Override
    public final void renderResponse()
    {
        assertNotReleased();

        _renderResponse = true;
    }

    @Override
    public final boolean getResponseComplete()
    {
        assertNotReleased();

        return _responseComplete;
    }
    
    @Override
    public final void responseComplete()
    {
        assertNotReleased();

        _responseComplete = true;
    }

    @Override
    public final void setResponseStream(final ResponseStream responseStream)
    {
        assertNotReleased();

        Assert.notNull(responseStream, "responseStream");
        
        _responseStream = responseStream;
    }

    @Override
    public final ResponseStream getResponseStream()
    {
        assertNotReleased();

        return _responseStream;
    }

    @Override
    public final void setResponseWriter(final ResponseWriter responseWriter)
    {
        assertNotReleased();

        Assert.notNull(responseWriter, "responseWriter");
        
        _responseWriter = responseWriter;
    }

    @Override
    public final ResponseWriter getResponseWriter()
    {
        assertNotReleased();

        return _responseWriter;
    }

    @Override
    public boolean isPostback()
    {
        assertNotReleased();

        RenderKit renderKit = getRenderKit();
        FacesContext facesContext = getCurrentFacesContext();
        if (renderKit == null)
        {
            // NullPointerException with StateManager, because
            // to restore state it first restore structure,
            // then fill it and in the middle of the two previous
            // process there is many calls from _ComponentChildrenList.childAdded
            // to facesContext.isPostback, and getViewRoot is null.
            // 
            // Setting a "phantom" UIViewRoot calling facesContext.setViewRoot(viewRoot)
            // to avoid it is bad, because this is work of RestoreViewExecutor,
            // and theorically ViewHandler.restoreView must return an UIViewRoot
            // instance.
            //
            // The problem with this is if the user changes the renderkit directly
            // using f:view renderKitId param, the ResponseStateManager returned
            // will be the one tied to faces-config selected RenderKit. But the usual 
            // method to check if a request is a postback, is always detect the param
            // jakarta.faces.ViewState, so there is no problem after all.
            String renderKitId = facesContext.getApplication().getViewHandler().calculateRenderKitId(facesContext);
            if (_renderKitFactory == null)
            {
                _renderKitFactory = (RenderKitFactory) 
                    FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            }
            renderKit = _renderKitFactory.getRenderKit(facesContext, renderKitId);
        }
        return renderKit.getResponseStateManager().isPostback(facesContext);
    }

    @Override
    public void validationFailed()
    {
        assertNotReleased();
        
        _validationFailed = true;
    }

    @Override
    public boolean isValidationFailed()
    {
        assertNotReleased();
        
        return _validationFailed;
    }

    @Override
    public Lifecycle getLifecycle()
    {
        assertNotReleased();
        return _lifecycle;
    }
}
