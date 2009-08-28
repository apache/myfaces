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

import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextFactory;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.context.ReleaseableFacesContextFactory;
import org.apache.myfaces.el.unified.FacesELContext;
import org.apache.myfaces.shared_impl.util.NullIterator;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class FacesContextImpl extends FacesContext
{

    private static final String METHOD_RESPONSEWRITER = "responseWriter";
    static final String RE_SPLITTER = "[\\s\\t\\r\\n]*\\,[\\s\\t\\r\\n]*";
    // ~ Instance fields ----------------------------------------------------------------------------

    private Map<String, List<FacesMessage>> _messages = null;
    private Application _application;
    private PhaseId _currentPhaseId;
    private ExternalContext _externalContext;
    private ReleaseableExternalContext _defaultExternalContext;
    private ResponseStream _responseStream = null;
    private ResponseWriter _responseWriter = null;
    private FacesMessage.Severity _maximumSeverity = null;
    private UIViewRoot _viewRoot;
    private boolean _renderResponse = false;
    private boolean _responseComplete = false;
    private RenderKitFactory _renderKitFactory;
    private boolean _released = false;
    private ELContext _elContext;
    private Map<Object, Object> _attributes = null;
    //private ResponseSwitch _responseWrapper = null;
    private boolean _validationFailed = false;
    private boolean _processingEvents = true;
    private ExceptionHandler _exceptionHandler = null;
    private PartialViewContext _partialViewContext = null;
    private ReleaseableFacesContextFactory _facesContextFactory = null;

    // ~ Constructors -------------------------------------------------------------------------------
    public FacesContextImpl(final ServletContext servletContext, final ServletRequest servletRequest,
                            final ServletResponse servletResponse)
    {
        init(new ServletExternalContextImpl(servletContext, servletRequest, servletResponse));
        /*
        try
        {
            // we wrap the servlet response to get our switching behavior!
            _responseWrapper = new ResponseSwitch(servletResponse);
            init(new ServletExternalContextImpl(servletContext, servletRequest, _responseWrapper));
        }
        catch (IOException ex)
        {
            Log log = LogFactory.getLog(this.getClass());
            log.fatal("Could not obtain the response writers! Detail:" + ex.toString());
        }*/
    }
    
    public FacesContextImpl(final ExternalContext externalContext,
            final ReleaseableExternalContext defaultExternalContext , 
            final ReleaseableFacesContextFactory facesContextFactory)
    {
        _facesContextFactory = facesContextFactory;
        init(externalContext, defaultExternalContext);
    }

    private void init(final ReleaseableExternalContext externalContext)
    {
        init((ExternalContext) externalContext, externalContext);
    }

    private void init(final ExternalContext externalContext, final ReleaseableExternalContext defaultExternalContext)
    {       
        _externalContext = externalContext;
        FacesContext.setCurrentInstance(this);  //protected method, therefore must be called from here
        _application = ((ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY))
                .getApplication();
        _renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
    }

    // ~ Methods ------------------------------------------------------------------------------------
    
    @Override
    public ExceptionHandler getExceptionHandler()
    {
        return _exceptionHandler;
    }
    
    @Override
    public final ExternalContext getExternalContext()
    {
        assertNotReleased();

        return (ExternalContext) _externalContext;
    }

    @Override
    public final FacesMessage.Severity getMaximumSeverity()
    {
        assertNotReleased();

        return _maximumSeverity;
    }

    @Override
    public List<FacesMessage> getMessageList()
    {
        assertNotReleased();
        
        if (_messages == null)
        {
            return Collections.unmodifiableList(Collections.<FacesMessage>emptyList());
        }
        
        List<FacesMessage> lst = new ArrayList<FacesMessage>();
        for(List<FacesMessage> curLst : _messages.values())
        {
            lst.addAll(curLst);       
        }
        
        return Collections.unmodifiableList(lst);
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId)
    {
        assertNotReleased();
        
        if (_messages == null || !_messages.containsKey(clientId))
        {
            return Collections.unmodifiableList(Collections.<FacesMessage>emptyList());
        }
        
        return _messages.get(clientId);
    }

    @Override
    public final Iterator<FacesMessage> getMessages()
    {
        assertNotReleased();

        if (_messages == null)
        {
            return NullIterator.instance();
        }

        List<FacesMessage> lst = new ArrayList<FacesMessage>();
        for(List<FacesMessage> curLst : _messages.values())
        {
            lst.addAll(curLst);       
        }
        return lst.iterator();
    }

    @Override
    public final Application getApplication()
    {
        assertNotReleased();

        return _application;
    }

    @Override
    public final Iterator<String> getClientIdsWithMessages()
    {
        assertNotReleased();

        if (_messages == null || _messages.isEmpty())
        {
            return NullIterator.instance();
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
    public final Iterator<FacesMessage> getMessages(final String clientId)
    {

        assertNotReleased();

        if (_messages == null || !_messages.containsKey(clientId))
        {
            return NullIterator.instance();
        }
        
        return _messages.get(clientId).iterator();        
    }
    
    @Override
    public PartialViewContext getPartialViewContext()
    {
        assertNotReleased();

        if (_partialViewContext == null)
        {
            //Get through factory finder
            PartialViewContextFactory factory = (PartialViewContextFactory)
                FactoryFinder.getFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
            _partialViewContext = factory.getPartialViewContext(this);
        }
        return _partialViewContext;
    }

    @Override
    public final RenderKit getRenderKit()
    {
        assertNotReleased();

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

    @Override
    public final boolean getRenderResponse()
    {
        assertNotReleased();

        return _renderResponse;
    }

    @Override
    public final boolean getResponseComplete()
    {
        assertNotReleased();

        return _responseComplete;
    }

    @Override
    public final void setResponseStream(final ResponseStream responseStream)
    {
        assertNotReleased();

        if (responseStream == null)
        {
            throw new NullPointerException("responseStream");
        }
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

        if (responseWriter == null)
        {
            throw new NullPointerException(METHOD_RESPONSEWRITER);
        }
        _responseWriter = responseWriter;
    }

    @Override
    public final ResponseWriter getResponseWriter()
    {
        assertNotReleased();

        return _responseWriter;
    }

    @Override
    public final void setViewRoot(final UIViewRoot viewRoot)
    {
        assertNotReleased();

        if (viewRoot == null)
        {
            throw new NullPointerException("viewRoot");
        }
        // If the current UIViewRoot is non-null, and calling equals() on the argument root, passing the current UIViewRoot returns false
        // the clear method must be called on the Map returned from UIViewRoot.getViewMap().
        if (_viewRoot != null && !_viewRoot.equals(viewRoot))
        {
            //call getViewMap(false) to prevent unnecessary map creation
            Map<String, Object> viewMap = _viewRoot.getViewMap(false);
            if (viewMap != null)
            {
                viewMap.clear();
            }
        }
        _viewRoot = viewRoot;
    }

    @Override
    public final UIViewRoot getViewRoot()
    {
        assertNotReleased();

        return _viewRoot;
    }

    @Override
    public final void addMessage(final String clientId, final FacesMessage message)
    {
        assertNotReleased();

        if (message == null)
        {
            throw new NullPointerException("message");
        }

        if (_messages == null)
        {
            _messages = new HashMap<String, List<FacesMessage>>();
        }
        
        List<FacesMessage> lst = _messages.get(clientId); 
        if (lst == null)
        {
            lst = new ArrayList<FacesMessage>();
            _messages.put(clientId, lst);
        }
        
        lst.add(message);
        
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
    public final void release()
    {
        assertNotReleased();

        if (_facesContextFactory != null)
        {
            _facesContextFactory.release();
            _facesContextFactory = null;
        }
        if (_defaultExternalContext != null)
        {
            _defaultExternalContext.release();
            _defaultExternalContext = null;
        }
        _externalContext = null;

        /*
         * Spec JSF 2 section getAttributes when release is called the attributes map must!!! be cleared!
         * 
         * (probably to trigger some clearance methods on possible added entries before nullifying everything)
         */
        if (_attributes != null)
        {
            _attributes.clear();
            _attributes = null;
        }

        _messages = null;
        _application = null;
        _responseStream = null;
        _responseWriter = null;
        _viewRoot = null;
        _partialViewContext = null;

        _released = true;
        FacesContext.setCurrentInstance(null);
    }

    @Override
    public boolean isPostback()
    {
        assertNotReleased();

        RenderKit renderKit = getRenderKit();
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
            // javax.faces.ViewState, so there is no problem after all.
            String renderKitId = this.getApplication().getViewHandler().calculateRenderKitId(this);
            RenderKitFactory factory = (RenderKitFactory) 
                FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            renderKit = factory.getRenderKit(this, renderKitId);            
        }
        return renderKit.getResponseStateManager().isPostback(this);            
    }

    @Override
    public final void renderResponse()
    {
        assertNotReleased();

        _renderResponse = true;
    }

    @Override
    public final void responseComplete()
    {
        assertNotReleased();

        _responseComplete = true;
    }

    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        assertNotReleased();

        _currentPhaseId = currentPhaseId;
    }

    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        _exceptionHandler = exceptionHandler;
    }

    // Portlet need to do this to change from ActionRequest/Response to
    // RenderRequest/Response
    /* This code comes from jsf 1.1 and is not valid anymore
    public final void setExternalContext(ReleaseableExternalContext extContext)
    {
        assertNotReleased();

        _externalContext = extContext;
        FacesContext.setCurrentInstance(this); // TODO: figure out if I really need to do this
    }*/

    @Override
    public final ELContext getELContext()
    {
        assertNotReleased();

        if (_elContext != null)
        {
            return _elContext;
        }

        _elContext = new FacesELContext(getApplication().getELResolver(), this);

        ELContextEvent event = new ELContextEvent(_elContext);
        for (ELContextListener listener : getApplication().getELContextListeners())
        {
            listener.contextCreated(event);
        }

        return _elContext;
    }

    /**
     * Returns a mutable map of attributes associated with this faces context when
     * {@link javax.faces.context.FacesContext.release} is called the map must be cleared!
     * 
     * Note this map is not associated with the request map the request map still is accessible via the
     * {@link javax.faces.context.FacesContext.getExternalContext.getRequestMap} method!
     * 
     * Also the scope is different to the request map, this map has the scope of the context, and is cleared once the
     * release method on the context is called!
     * 
     * Also the map does not cause any events according to the spec!
     * 
     * @since JSF 2.0
     * 
     * @throws IllegalStateException
     *             if the current context already is released!
     */
    @Override
    public Map<Object, Object> getAttributes()
    {
        assertNotReleased();

        if (_attributes == null)
        {
            _attributes = new HashMap<Object, Object>();
        }
        return _attributes;
    }
    
    @Override
    public void validationFailed()
    {
        assertNotReleased();
        
        _validationFailed=true;
    }

    @Override
    public boolean isValidationFailed()
    {
        assertNotReleased();
        
        return _validationFailed;
    }
    
    @Override
    public boolean isProcessingEvents()
    {
        assertNotReleased();
        
        return _processingEvents;
    }
    
    @Override
    public void setProcessingEvents(boolean processingEvents)
    {
        assertNotReleased();
        
        _processingEvents = processingEvents;
    }
    
    /**
     * has to be thrown in many of the methods if the method is called after the instance has been released!
     */
    private final void assertNotReleased()
    {
        if (_released)
        {
            throw new IllegalStateException("Error the FacesContext is already released!");
        }
    }
}
