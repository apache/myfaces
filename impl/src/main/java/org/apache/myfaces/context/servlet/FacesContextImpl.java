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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.el.unified.FacesELContext;
import org.apache.myfaces.shared_impl.util.NullIterator;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class FacesContextImpl extends FacesContext {

    public static final String AJAX_REQ_KEY = "javax.faces.partial.ajax";
    // ~ Instance fields ----------------------------------------------------------------------------

    // TODO: I think a Map<String, List<FacesMessage>> would more efficient than those two -= Simon Lessard =-
    private List<FacesMessage> _messages = null;
    private List<String> _messageClientIds = null;
    private Application _application;
    private PhaseId _currentPhaseId;
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
    private Map<Object, Object> _attributes = null;
    private ResponseSwitch _responseWrapper = null;
    private List<String> _renderPhaseClientIds = null;
    private List<String> _executePhaseClientIds = null;

    private Boolean _renderAll = null;

    // ~ Constructors -------------------------------------------------------------------------------
    public FacesContextImpl(final ServletContext servletContext, final ServletRequest servletRequest,
            final ServletResponse servletResponse) {
        try {
            //we wrap the servlet response to get our switching behavior!
            _responseWrapper = new ResponseSwitch(servletResponse);
            init(new ServletExternalContextImpl(servletContext, servletRequest, _responseWrapper));
        } catch (IOException ex) {
            Log log = LogFactory.getLog(this.getClass());
            log.fatal("Could not obtain the response writers! Detail:" + ex.toString());
        }
    }

    private void init(final ReleaseableExternalContext externalContext) {
        _application = ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();
        _renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        _externalContext = externalContext;
        FacesContext.setCurrentInstance(this); // protected method, therefore must be called from here
    }


    // ~ Methods ------------------------------------------------------------------------------------
    @Override
    public final ExternalContext getExternalContext() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return (ExternalContext) _externalContext;
    }

    @Override
    public final FacesMessage.Severity getMaximumSeverity() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }

        return _maximumSeverity;
    }

    @Override
    public final Iterator<FacesMessage> getMessages() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }

        if (_messages == null) {
            return NullIterator.instance();
        }

        return _messages.iterator();
    }

    @Override
    public final Application getApplication() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }

        return _application;
    }

    @Override
    public final Iterator<String> getClientIdsWithMessages() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }

        if (_messages == null || _messages.isEmpty()) {
            return NullIterator.instance();
        }

        final Set<String> uniqueClientIds = new LinkedHashSet<String>(_messageClientIds);

        return uniqueClientIds.iterator();
    }

    @Override
    public PhaseId getCurrentPhaseId() {
        return _currentPhaseId;
    }

    @Override
    public final Iterator<FacesMessage> getMessages(final String clientId) {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }

        if (_messages == null) {
            return NullIterator.instance();
        }

        List<FacesMessage> lst = new ArrayList<FacesMessage>();
        for (int i = 0; i < _messages.size(); i++) {
            Object savedClientId = _messageClientIds.get(i);
            if (clientId == null) {
                if (savedClientId == null) {
                    lst.add(_messages.get(i));
                }
            } else {
                if (clientId.equals(savedClientId)) {
                    lst.add(_messages.get(i));
                }
            }
        }

        return lst.iterator();
    }

    @Override
    public final RenderKit getRenderKit() {
        if (getViewRoot() == null) {
            return null;
        }

        String renderKitId = getViewRoot().getRenderKitId();

        if (renderKitId == null) {
            return null;
        }

        return _renderKitFactory.getRenderKit(this, renderKitId);
    }

    @Override
    public final boolean getRenderResponse() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _renderResponse;
    }

    @Override
    public final boolean getResponseComplete() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseComplete;
    }

    @Override
    public final void setResponseStream(final ResponseStream responseStream) {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (responseStream == null) {
            throw new NullPointerException("responseStream");
        }
        _responseStream = responseStream;
    }

    @Override
    public final ResponseStream getResponseStream() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseStream;
    }

    @Override
    public final void setResponseWriter(final ResponseWriter responseWriter) {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (responseWriter == null) {
            throw new NullPointerException("responseWriter");
        }
        _responseWriter = responseWriter;
    }

    @Override
    public final ResponseWriter getResponseWriter() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _responseWriter;
    }

    @Override
    public final void setViewRoot(final UIViewRoot viewRoot) {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (viewRoot == null) {
            throw new NullPointerException("viewRoot");
        }
        _viewRoot = viewRoot;
    }

    @Override
    public final UIViewRoot getViewRoot() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        return _viewRoot;
    }

    @Override
    public final void addMessage(final String clientId, final FacesMessage message) {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (message == null) {
            throw new NullPointerException("message");
        }

        if (_messages == null) {
            _messages = new ArrayList<FacesMessage>();
            _messageClientIds = new ArrayList<String>();
        }
        _messages.add(message);
        _messageClientIds.add((clientId != null) ? clientId : null);
        FacesMessage.Severity serSeverity = message.getSeverity();
        if (serSeverity != null) {
            if (_maximumSeverity == null) {
                _maximumSeverity = serSeverity;
            } else if (serSeverity.compareTo(_maximumSeverity) > 0) {
                _maximumSeverity = serSeverity;
            }
        }
    }

    @Override
    public final void release() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (_externalContext != null) {
            _externalContext.release();
            _externalContext = null;
        }

        _messageClientIds = null;
        _messages = null;
        _application = null;
        _responseStream = null;
        _responseWriter = null;
        _viewRoot = null;
        _attributes = null;

        _released = true;
        FacesContext.setCurrentInstance(null);
    }

    @Override
    public boolean isPostback() {
        return getRenderKit().getResponseStateManager().isPostback(this);
    }

    @Override
    public final void renderResponse() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        _renderResponse = true;
    }

    @Override
    public final void responseComplete() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        _responseComplete = true;
    }

    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId) {
        _currentPhaseId = currentPhaseId;
    }

    // Portlet need to do this to change from ActionRequest/Response to
    // RenderRequest/Response
    public final void setExternalContext(ReleaseableExternalContext extContext) {
        _externalContext = extContext;
        FacesContext.setCurrentInstance(this); // TODO: figure out if I really need to do this
    }

    @Override
    public final ELContext getELContext() {
        if (_elContext != null) {
            return _elContext;
        }

        _elContext = new FacesELContext(getApplication().getELResolver(), this);

        ELContextEvent event = new ELContextEvent(_elContext);
        for (ELContextListener listener : getApplication().getELContextListeners()) {
            listener.contextCreated(event);
        }

        return _elContext;
    }

    /**
     * @since JSF 2.0 
     */
    @Override
    public Map<Object, Object> getAttributes() {
        if (_released) {
            throw new IllegalStateException("FacesContext already released");
        }
        if (_attributes == null) {
            _attributes = new HashMap<Object, Object>();
        }
        return _attributes;
    }

    /**
     * if set to false the response writing is suppressed
     * this construct has been added to deal with
     * subview lifecycles in the ajax cycle
     *
     * @param enable if set to true the response is routed through if set to false
     * the response is suppressed!
     */
    @Override
    public void enableResponseWriting(boolean enable) {
        _responseWrapper.setEnabled(enable);
        super.enableResponseWriting(enable);
    }

    /**
     * @return the list of client ids to be processed in the execute phase
     * null if all have to be processed
     */
    @Override
    public List<String> getExecutePhaseClientIds() {
        return super.getExecutePhaseClientIds();
    }

    /**
     * @return the list of client ids to be processed in the
     * render phase null if all have to be processed
     */
    @Override
    public List<String> getRenderPhaseClientIds() {
        return super.getRenderPhaseClientIds();
    }

    /**
     * @param executePhaseClientIds the list of client ids
     * to be processed by the execute phase
     */
    @Override
public void setExecutePhaseClientIds(List<String> executePhaseClientIds) {
        super.setExecutePhaseClientIds(executePhaseClientIds);
    }

    /**
     * @param the list of client ids to be processed by the render
     * phase!
     */
    @Override
    public void setRenderPhaseClientIds(List<String> renderPhaseClientIds) {
        super.setRenderPhaseClientIds(renderPhaseClientIds);
    }

    /**
     * TODO #51 in progress
     * is ajax request implementation
     * according to the spec, javax.faces.partial must be present
     * and then it will return true
     * if none is present it will return false
     * since we only have to check it once
     * a lazy init cannot be done because theoretically someone
     * can push in a request wrapper!
     *
     */
  /*  @Override
    public boolean isAjaxRequest() {
        Map requestMap = getExternalContext().getRequestMap();
        return requestMap.containsKey(AJAX_REQ_KEY);
    }*/

   
    /**
     * @return is render none return true if {@link #PARTIAL_EXECUTE_PARAM_NAME} is set in the current request
     * map!
     * and the value is set to {@link #NO_PARTIAL_PHASE_CLIENT_IDS}. Otherwise return false!
     */
    @Override
    public boolean isExecuteNone() {
        Map requestMap = getExternalContext().getRequestParameterMap();
        String param = (String) requestMap.get(PARTIAL_EXECUTE_PARAM_NAME);
        return NO_PARTIAL_PHASE_CLIENT_IDS.equals(param);
    }


    /**
     * @return true in case of PARTIAL_RENDER_PARAM_NAME being set and its value is
     * NO_PARTIAL_PHASE_CLIENT_IDS. Otherwise return false
     */
    @Override
    public boolean isRenderNone() {
        Map requestMap = getExternalContext().getRequestParameterMap();
        String param = (String) requestMap.get(PARTIAL_RENDER_PARAM_NAME);
        return NO_PARTIAL_PHASE_CLIENT_IDS.equals(param);
    }

    /**
     * @return true in case of {@link javax.faces.context.FacesContext.isAjaxRequest()} returns true,
     *  {@link javax.faces.context.FacesContext.isRenderNone()} returns false and
     *  {@link javax.faces.context.FacesContext.getRenderPhaseClientIds()} returns also
     * an empty list
     */
    @Override
    public boolean isRenderAll() {
        if(_renderAll != null) {
            return _renderAll;
        }
        //I assume doing the check once per request is correct
        //there is no way to determine if there was an override
        //of the renderAll according to the spec!
        List renderClientIds = getRenderPhaseClientIds();
        _renderAll = renderClientIds.isEmpty() && isAjaxRequest() && !isRenderNone();
        return _renderAll;
    }

    /**
     * override for the isRenderall determination mechanism
     * if set to true the isRenderAll() must! return
     * true!
     * If nothing is set the isRenderall() does a fallback into
     * its renderall determination algorithm!
     * 
     * @param renderAll if set to true isRenderAll() will return
     * true on the subsequent calls in the request!
     */
    @Override
    public void setRenderAll(boolean renderAll) {
        _renderAll = renderAll;//autoboxing does the conversation here, no need to do casting
    }




}
