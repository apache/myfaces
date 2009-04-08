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
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
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
public class FacesContextImpl extends FacesContext
{

    public static final String METHOD_ISAJAXREQUEST = "isAjaxRequest";
    private static final String METHOD_ADDMESSAGE = "addMessage";
    private static final String METHOD_GETAPPLICATION = "getApplication";
    private static final String METHOD_GETATTRIBUTES = "getAttributes";
    private static final String METHOD_GETCLIENTIDSWITHMESSAGES = "getClientIdsWithMessages";
    private static final String METHOD_GETCURRENTPHASEID = "getCurrentPhaseId";
    private static final String METHOD_GETELCONTEXT = "getELContext";
    private static final String METHOD_GETEXTERNALCONTEXT = "getExternalContext";
    private static final String METHOD_GETMAXIMUMSEVERITY = "getMaximumSeverity";
    private static final String METHOD_GETMESSAGES = "getMessages";
    private static final String METHOD_GETRENDERKIT = "getRenderKit";
    private static final String METHOD_GETRESPONSECOMPLETE = "getResponseComplete";
    private static final String METHOD_GETRESPONSESTREAM = "getResponseStream";
    private static final String METHOD_GETRESPONSEWRITER = "getResponseWriter";
    private static final String METHOD_RELEASE = "release";
    private static final String METHOD_RENDERRESPONSE = "renderResponse";
    private static final String METHOD_RESPONSECOMPLETE = "responseComplete";
    private static final String METHOD_RESPONSEWRITER = "responseWriter";
    private static final String METHOD_SETCURRENTPHASEID = "setCurrentPhaseId";
    private static final String METHOD_SETEXTERNALCONTEXT = "setExternalContext";
    private static final String METHOD_SETRESPONSESTREAM = "setResponseStream";
    private static final String METHOD_SETRESPONSEWRITER = "setResponseWriter";
    private static final String METHOD_SETVIEWROOT = "setViewRoot";
    private static final String METHOD_GETVIEWROOT = "getViewRoot";
    static final String RE_SPLITTER = "[\\s\\t\\r\\n]*\\,[\\s\\t\\r\\n]*";
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

    // ~ Constructors -------------------------------------------------------------------------------
    public FacesContextImpl(final ServletContext servletContext, final ServletRequest servletRequest,
                            final ServletResponse servletResponse)
    {
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
        }
    }

    private void init(final ReleaseableExternalContext externalContext)
    {
        _application = ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY))
                .getApplication();
        _renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        _externalContext = externalContext;
        FacesContext.setCurrentInstance(this); // protected method, therefore must be called from here
    }

    // ~ Methods ------------------------------------------------------------------------------------
    
    @Override
    public ExceptionHandler getExceptionHandler()
    {
        // TODO: IMPLEMENT HERE
        return super.getExceptionHandler();
    }
    
    @Override
    public final ExternalContext getExternalContext()
    {
        assertNotReleased(METHOD_GETEXTERNALCONTEXT);

        return (ExternalContext) _externalContext;
    }

    @Override
    public final FacesMessage.Severity getMaximumSeverity()
    {
        assertNotReleased(METHOD_GETMAXIMUMSEVERITY);

        return _maximumSeverity;
    }

    @Override
    public List<FacesMessage> getMessageList()
    {
        // TODO: IMPLEMENT HERE
        return super.getMessageList();
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId)
    {
        // TODO: IMPLEMENT HERE
        return super.getMessageList(clientId);
    }

    @Override
    public final Iterator<FacesMessage> getMessages()
    {
        assertNotReleased(METHOD_GETMESSAGES);

        if (_messages == null)
        {
            return NullIterator.instance();
        }

        return _messages.iterator();
    }

    @Override
    public final Application getApplication()
    {
        assertNotReleased(METHOD_GETAPPLICATION);

        return _application;
    }

    @Override
    public final Iterator<String> getClientIdsWithMessages()
    {
        assertNotReleased(METHOD_GETCLIENTIDSWITHMESSAGES);

        if (_messages == null || _messages.isEmpty())
        {
            return NullIterator.instance();
        }

        final Set<String> uniqueClientIds = new LinkedHashSet<String>(_messageClientIds);

        return uniqueClientIds.iterator();
    }

    @Override
    public PhaseId getCurrentPhaseId()
    {
        assertNotReleased(METHOD_GETCURRENTPHASEID);

        return _currentPhaseId;
    }

    @Override
    public final Iterator<FacesMessage> getMessages(final String clientId)
    {

        assertNotReleased(METHOD_GETMESSAGES);

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
    
    @Override
    public PartialViewContext getPartialViewContext()
    {
        // TODO: JSF 2.0
        return null;
    }

    @Override
    public final RenderKit getRenderKit()
    {
        assertNotReleased(METHOD_GETRENDERKIT);

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
        assertNotReleased(METHOD_GETRESPONSECOMPLETE);

        return _renderResponse;
    }

    @Override
    public final boolean getResponseComplete()
    {
        assertNotReleased(METHOD_GETRESPONSECOMPLETE);

        return _responseComplete;
    }

    @Override
    public final void setResponseStream(final ResponseStream responseStream)
    {
        assertNotReleased(METHOD_SETRESPONSESTREAM);

        if (responseStream == null)
        {
            throw new NullPointerException("responseStream");
        }
        _responseStream = responseStream;
    }

    @Override
    public final ResponseStream getResponseStream()
    {
        assertNotReleased(METHOD_GETRESPONSESTREAM);

        return _responseStream;
    }

    @Override
    public final void setResponseWriter(final ResponseWriter responseWriter)
    {
        assertNotReleased(METHOD_SETRESPONSEWRITER);

        if (responseWriter == null)
        {
            throw new NullPointerException(METHOD_RESPONSEWRITER);
        }
        _responseWriter = responseWriter;
    }

    @Override
    public final ResponseWriter getResponseWriter()
    {
        assertNotReleased(METHOD_GETRESPONSEWRITER);

        return _responseWriter;
    }

    @Override
    public final void setViewRoot(final UIViewRoot viewRoot)
    {
        assertNotReleased(METHOD_SETVIEWROOT);

        if (viewRoot == null)
        {
            throw new NullPointerException("viewRoot");
        }
        _viewRoot = viewRoot;
    }

    @Override
    public final UIViewRoot getViewRoot()
    {
        assertNotReleased(METHOD_GETVIEWROOT);

        return _viewRoot;
    }

    @Override
    public final void addMessage(final String clientId, final FacesMessage message)
    {
        assertNotReleased(METHOD_ADDMESSAGE);

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

    @Override
    public final void release()
    {
        assertNotReleased(METHOD_RELEASE);

        if (_externalContext != null)
        {
            _externalContext.release();
            _externalContext = null;
        }

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

        _messageClientIds = null;
        _messages = null;
        _application = null;
        _responseStream = null;
        _responseWriter = null;
        _viewRoot = null;

        _released = true;
        FacesContext.setCurrentInstance(null);
    }

    @Override
    public boolean isPostback()
    {
        assertNotReleased(METHOD_RENDERRESPONSE);

        return getRenderKit().getResponseStateManager().isPostback(this);
    }

    @Override
    public final void renderResponse()
    {
        assertNotReleased(METHOD_RENDERRESPONSE);

        _renderResponse = true;
    }

    @Override
    public final void responseComplete()
    {
        assertNotReleased(METHOD_RESPONSECOMPLETE);

        _responseComplete = true;
    }

    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        assertNotReleased(METHOD_SETCURRENTPHASEID);

        _currentPhaseId = currentPhaseId;
    }

    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        // TODO: JSF 2.0, add impl
        super.setExceptionHandler(exceptionHandler);
    }

    // Portlet need to do this to change from ActionRequest/Response to
    // RenderRequest/Response
    public final void setExternalContext(ReleaseableExternalContext extContext)
    {
        assertNotReleased(METHOD_SETEXTERNALCONTEXT);

        _externalContext = extContext;
        FacesContext.setCurrentInstance(this); // TODO: figure out if I really need to do this
    }

    @Override
    public final ELContext getELContext()
    {
        assertNotReleased(METHOD_GETELCONTEXT);

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
        assertNotReleased(METHOD_GETATTRIBUTES);

        if (_attributes == null)
        {
            _attributes = new HashMap<Object, Object>();
        }
        return _attributes;
    }

    /**
     * private helper method to split incoming request parameter lists according to the JSF2 specs the JSF2 spec usually
     * sees empty lists as either not being set (null), or empty == "" or with an optional special value being set
     * marking it as empty!
     * 
     * @param key
     *            the request parameter key holding the list
     * @param emptyValue
     *            the special empty value
     * @return a list of strings or an empty list if nothing was found
     */
    private final List<String> getRequestParameterList(String key, String emptyValue)
    {
        // FIXME: This method assume that getParameterMap returns a Map<String, String>, but the spec
        //        says it's supposed to be Map<String, String[]>. -= Simon Lessard =-
        Map paramMap = ((ServletRequest) getExternalContext().getRequest()).getParameterMap();
        
        String clientIds = (String) paramMap.get(key);
        if (clientIds == null)
        {// no value given
            return Collections.<String>emptyList();
        }
        
        clientIds = clientIds.trim();
        if (clientIds.equals("") || (emptyValue != null && clientIds.equals(emptyValue)))
        {// empty String!
            return Collections.<String>emptyList();
        }

        /**
         * we have to process the params list we now split the params as fast as possible
         */
        String[] splitted = clientIds.split(RE_SPLITTER);
        
        /*
         * we have to retrim the first and last entry we could have pending blanks!
         */
        splitted[0] = splitted[0].trim();
        
        int trimLast = splitted.length - 1;
        if (trimLast > 0)
        {// all others trimmed by the re
            splitted[trimLast] = splitted[trimLast].trim();
        }
        
        return Arrays.asList(splitted);
    }

    /**
     * has to be thrown in many of the methods if the method is called after the instance has been released!
     */
    private final void assertNotReleased(String string)
    {
        if (_released)
        {
            StringBuilder errorMessage = new StringBuilder(128);
            errorMessage.append("Error in method call on javax.faces.context.FacesContext.");
            errorMessage.append(string);
            errorMessage.append(", the facesContext is already released!");
            throw new IllegalStateException(errorMessage.toString());
        }
    }

// TODO: MOVE - PartialViewContext is no longer created by FacesContext in the latest spec, see 
//              PartialViewContextFactory
//    private class PartialViewContextImpl extends PartialViewContext
//    {
//
//        /**
//         * if set to false the response writing is suppressed this construct has been added to deal with subview lifecycles
//         * in the ajax cycle
//         * 
//         * @param enable
//         *            if set to true the response is routed through if set to false the response is suppressed!
//         * 
//         * @throws IllegalStateException
//         *             if the current context already is released!
//         */
//        @Override
//        public void enableResponseWriting(boolean enable)
//        {
//            assertNotReleased(METHOD_ENABLERESPONSEWRITING);
//
//            _responseWrapper.setEnabled(enable);
//        }
//
//        @Override
//        public Map<Object, Object> getAttributes()
//        {
//            return FacesContextImpl.this.getAttributes();
//        }
//
//        /**
//         * @return the list of client ids to be processed in the execute phase null if all have to be processed The client
//         *         ids either must be set via the setter or being present by having a PARTIAL_EXECUTE_PARAM_NAME with a
//         *         value set non existent or NO_PARTIAL_PHASE_CLIENT_IDS values in the request map and a non set local list
//         *         result in an empty list as return value!
//         * 
//         * @since 2.0
//         * @throws IllegalStateException
//         *             if the current context already is released!
//         */
//        @Override
//        public List<String> getExecutePhaseClientIds()
//        {
//            assertNotReleased(METHOD_GETEXECUTEPHASECLIENTIDS);
//
//            if (_executePhaseClientIds != null)
//            {
//                return _executePhaseClientIds;
//            }
//
//            _executePhaseClientIds = getRequestParameterList(PARTIAL_EXECUTE_PARAM_NAME, NO_PARTIAL_PHASE_CLIENT_IDS);
//
//            return _executePhaseClientIds;
//        }
//
//        @Override
//        public ResponseWriter getPartialResponseWriter()
//        {
//            // TODO: JSF 2.0, missing impl
//            return null;
//        }
//
//        /**
//         * 
//         * @return a list of client ids which are fetched from the request <b>parameter</b> map. The key for the map entries
//         *         is {@link javax.faces.context.FacesContext.PARTIAL_RENDER_PARAM_NAME}. The list is a comma separated list
//         *         of client ids in the request map! if the value
//         *         {@link javax.faces.context.FacesContext.NO_PARTIAL_PHASE_CLIENT_IDS} is set or null or empty then an
//         *         empty list is returned!
//         * 
//         *         The client ids are the ones which have to be processed during the render phase
//         * 
//         * @since 2.0
//         * @throws IllegalStateException
//         *             if the current context already is released!
//         */
//        @Override
//        public List<String> getRenderPhaseClientIds()
//        {
//            assertNotReleased(METHOD_GETRENDERPHASECLIENTIDS);
//
//            /* already processed or set from the outside */
//            if (null != _renderPhaseClientIds)
//            {
//                return _renderPhaseClientIds;
//            }
//
//            _renderPhaseClientIds = getRequestParameterList(PARTIAL_RENDER_PARAM_NAME, NO_PARTIAL_PHASE_CLIENT_IDS);
//
//            return _renderPhaseClientIds;
//        }
//
//        /**
//         * TODO #51 in progress is ajax request implementation according to the spec, javax.faces.partial must be present
//         * and then it will return true if none is present it will return false since we only have to check it once a lazy
//         * init cannot be done because theoretically someone can push in a request wrapper!
//         * 
//         */
//        @Override
//        public boolean isAjaxRequest()
//        {
//
//            assertNotReleased(METHOD_ISAJAXREQUEST);
//
//            /*
//             * A speed shortcut here is feasable but it has to be discussed we have those in several parts of the
//             * facesContext and even most of them implicetly enforced by the spec itself. Hence i set it here.
//             * 
//             * 
//             * The problem is that request parameter maps can be delivered by RequestWrappers hence, you cannot rely on the
//             * map being entirely immutable. But leaving it out probably is also a no option since this method probably will
//             * be called by every component renderer and a O(log(n)) lookup is not a serious performance impact but serious
//             * enough!
//             * 
//             * This has to be cleared up with the spec people! This should not cause a problem under normal circumstances
//             * however!
//             */
//            if (_ajaxRequest == null)
//            {
//                Map<String, String> requestParamMap = getExternalContext().getRequestParameterMap();
//                _ajaxRequest = requestParamMap.containsKey(AJAX_REQ_KEY);
//            }
//            
//            return _ajaxRequest;
//        }
//
//        /**
//         * @return is render none return true if {@link #PARTIAL_EXECUTE_PARAM_NAME} is set in the current request map! and
//         *         the value is set to {@link #NO_PARTIAL_PHASE_CLIENT_IDS}. Otherwise return false!
//         */
//        @Override
//        public boolean isExecuteNone()
//        {
//            assertNotReleased(METHOD_ISEXECUTENONE);
//
//            Map<String, String> requestMap = getExternalContext().getRequestParameterMap();
//            
//            String param = (String) requestMap.get(PARTIAL_EXECUTE_PARAM_NAME);
//            
//            return NO_PARTIAL_PHASE_CLIENT_IDS.equals(param);
//        }
//
//        /**
//         * @return true in case of PARTIAL_RENDER_PARAM_NAME being set and its value is NO_PARTIAL_PHASE_CLIENT_IDS.
//         *         Otherwise return false
//         */
//        @Override
//        public boolean isRenderNone()
//        {
//            assertNotReleased(METHOD_ISRENDERNONE);
//
//            Map<String, String> requestMap = getExternalContext().getRequestParameterMap();
//            
//            String param = (String) requestMap.get(PARTIAL_RENDER_PARAM_NAME);
//            
//            return NO_PARTIAL_PHASE_CLIENT_IDS.equals(param);
//        }
//
//        /**
//         * @return true in case of {@link javax.faces.context.FacesContext.isAjaxRequest()} returns true, {@link
//         *         javax.faces.context.FacesContext.isRenderNone()} returns false and {@link
//         *         javax.faces.context.FacesContext.getRenderPhaseClientIds()} returns also an empty list
//         */
//        @Override
//        public boolean isRenderAll()
//        {
//            assertNotReleased(METHOD_ISRENDERALL);
//
//            if (_renderAll != null)
//            {
//                return _renderAll;
//            }
//            // I assume doing the check once per request is correct
//            // there is no way to determine if there was an override
//            // of the renderAll according to the spec!
//            List<String> renderClientIds = getRenderPhaseClientIds();
//            
//            _renderAll = renderClientIds.isEmpty() && isAjaxRequest() && !isRenderNone();
//            
//            return _renderAll;
//        }
//
//        @Override
//        public void release()
//        {
//            FacesContextImpl.this.release();
//        }
//
//        /**
//         * @param executePhaseClientIds
//         *            the list of client ids to be processed by the execute phase
//         * 
//         * @since 2.0
//         * @throws IllegalStateException
//         *             if the current context already is released!
//         */
//        @Override
//        public void setExecutePhaseClientIds(List<String> executePhaseClientIds)
//        {
//            assertNotReleased(METHOD_SETEXECUTEPHASECLIENTIDS);
//
//            _executePhaseClientIds = executePhaseClientIds;
//        }
//
//        /**
//         * override for the isRenderall determination mechanism if set to true the isRenderAll() must! return true! If
//         * nothing is set the isRenderall() does a fallback into its renderall determination algorithm!
//         * 
//         * @param renderAll
//         *            if set to true isRenderAll() will return true on the subsequent calls in the request!
//         */
//        @Override
//        public void setRenderAll(boolean renderAll)
//        {
//            assertNotReleased(METHOD_SETRENDERALL);
//
//            _renderAll = renderAll;// autoboxing does the conversation here, no need to do casting
//        }
//
//        /**
//         * @param the
//         *            list of client ids to be processed by the render phase!
//         * @since 2.0
//         * @throws IllegalStateException
//         *             if the current context already is released!
//         */
//        @Override
//        public void setRenderPhaseClientIds(List<String> renderPhaseClientIds)
//        {
//            assertNotReleased(METHOD_SETEXECUTEPHASECLIENTIDS);
//
//            _renderPhaseClientIds = renderPhaseClientIds;
//        }
//    }
}
