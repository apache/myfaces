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
import java.util.*;


import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContext;
import javax.faces.event.PhaseId;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitResult;
import javax.faces.component.UIComponent;

import javax.faces.context.ExternalContext;
import javax.faces.context.ResponseWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.util.StringUtils;

public class PartialViewContextImpl extends PartialViewContext {

    private static final String FACES_REQUEST = "Faces-Request";
    private static final String PARTIAL_AJAX = "partial/ajax";
    private static final String PARTIAL_PROCESS = "partial/process";
    private FacesContext _facesContext = null;
    private boolean _released = false;
    // Cached values, since their parent methods could be called
    // many times and the result does not change during the life time
    // of this object.
    private Boolean _ajaxRequest = null;
    private Collection<String> _executeClientIds = null;
    private Collection<String> _renderClientIds = null;
    // Values that need to be saved because exists a setXX method 
    private Boolean _partialRequest = null;
    private Boolean _renderAll = null;

    public PartialViewContextImpl(FacesContext context) {
        _facesContext = context;
    }

    @Override
    public boolean isAjaxRequest() {
        assertNotReleased();

        if (_ajaxRequest == null) {
            String requestType = _facesContext.getExternalContext().
                    getRequestHeaderMap().get(FACES_REQUEST);
            _ajaxRequest = (requestType != null && PARTIAL_AJAX.equals(requestType));
        }
        return _ajaxRequest;
    }

    @Override
    public boolean isExecuteAll() {
        assertNotReleased();

        if (isAjaxRequest()) {
            String executeMode = _facesContext.getExternalContext().
                    getRequestParameterMap().get(
                    PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME);
            if (PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPartialRequest() {
        assertNotReleased();

        if (_partialRequest == null) {
            String requestType = _facesContext.getExternalContext().
                    getRequestHeaderMap().get(FACES_REQUEST);
            _partialRequest = (requestType != null && PARTIAL_PROCESS.equals(requestType));
        }
        return isAjaxRequest() || _partialRequest;
    }

    @Override
    public boolean isRenderAll() {
        assertNotReleased();

        if (_renderAll == null) {
            if (isAjaxRequest()) {
                String executeMode = _facesContext.getExternalContext().
                        getRequestParameterMap().get(
                        PartialViewContext.PARTIAL_RENDER_PARAM_NAME);
                if (PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode)) {
                    _renderAll = true;
                }
            }
            if (_renderAll == null) {
                _renderAll = false;
            }
        }
        return _renderAll;
    }

    @Override
    public void setPartialRequest(boolean isPartialRequest) {
        assertNotReleased();

        _partialRequest = isPartialRequest;

    }

    @Override
    public void setRenderAll(boolean renderAll) {
        assertNotReleased();

        _renderAll = renderAll;
    }

    @Override
    public Collection<String> getExecuteIds() {
        assertNotReleased();

        if (_executeClientIds == null) {
            String executeMode = _facesContext.getExternalContext().
                    getRequestParameterMap().get(
                    PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME);

            if (executeMode != null && !"".equals(executeMode) &&
                    !PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode) &&
                    !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode)) {
                String[] clientIds = StringUtils.splitShortString(executeMode.replaceAll("[ \\t\\n]*", ""), ',');

                //The collection must be mutable
                List<String> tempList = new ArrayList<String>();
                Collections.addAll(tempList, clientIds);
                _executeClientIds = tempList;
            } else {
                _executeClientIds = new ArrayList<String>();
            }
        }
        return _executeClientIds;
    }

    @Override
    public Collection<String> getRenderIds() {
        assertNotReleased();

        if (_renderClientIds == null) {
            String renderMode = _facesContext.getExternalContext().
                    getRequestParameterMap().get(
                    PartialViewContext.PARTIAL_RENDER_PARAM_NAME);

            if (renderMode != null && !"".equals(renderMode) &&
                    !PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode) &&
                    !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode)) {
                String[] clientIds = StringUtils.splitShortString(renderMode.replaceAll("[ \\t\\n]*", ""), ',');

                //The collection must be mutable
                List<String> tempList = new ArrayList<String>();
                Collections.addAll(tempList, clientIds);
                _renderClientIds = tempList;
            } else {
                _renderClientIds = new ArrayList<String>();
            }
        }
        return _renderClientIds;
    }

    @Override
    public PartialResponseWriter getPartialResponseWriter() {
        assertNotReleased();
        //TODO: JSF 2.0, add impl

        return new PartialResponseWriter(_facesContext.getResponseWriter());
    }

    /**
     * process the partial response
     * allowed phase ids according to the spec
     *
     *
     */
    @Override
    public void processPartial(PhaseId phaseId) {
        assertNotReleased();

        UIComponent viewRoot = _facesContext.getViewRoot();



        if (phaseId == PhaseId.APPLY_REQUEST_VALUES || phaseId == PhaseId.PROCESS_VALIDATIONS || phaseId == PhaseId.UPDATE_MODEL_VALUES) {
            processPartialExecute(viewRoot, phaseId);
        } else if (phaseId == PhaseId.RENDER_RESPONSE) {

            processPartialRendering(viewRoot, phaseId);
        }


    }

    private void processPartialExecute(UIComponent viewRoot, PhaseId phaseId) {
        Collection<String> executeIds = getExecuteIds();
        if (executeIds == null || executeIds.isEmpty()) {
            return;
        }
        Set<VisitHint> hints = new HashSet<VisitHint>();
        hints.add(VisitHint.EXECUTE_LIFECYCLE);
        hints.add(VisitHint.SKIP_UNRENDERED);
        VisitContext visitCtx = VisitContext.createVisitContext(_facesContext, executeIds, hints);
        viewRoot.visitTree(visitCtx, new PhaseAwareVisitCallback(_facesContext, phaseId));
    }

    private void processPartialRendering(UIComponent viewRoot, PhaseId phaseId) {
        //TODO process partial rendering
        //https://issues.apache.org/jira/browse/MYFACES-2118
        Collection<String> renderIds = getRenderIds();
        if (renderIds == null || renderIds.isEmpty()) {
            return;
        }



        PartialResponseWriter writer = getPartialResponseWriter();
        ResponseWriter oldWriter = _facesContext.getResponseWriter();
        boolean inDocument = false;

        //response type = text/xml
        //no caching and no timeout if possible!
        ExternalContext externalContext = _facesContext.getExternalContext();
        externalContext.setResponseContentType("text/xml");
        externalContext.addResponseHeader("Pragma", "no-cache");
        externalContext.addResponseHeader("Cache-control", "no-cache");
        //under normal circumstances pragma should be enough, IE needs
        //a special treatment!
        //http://support.microsoft.com/kb/234067
        externalContext.addResponseHeader("Expires", "-1");

        try {

            writer.startDocument();
            inDocument = true;
            _facesContext.setResponseWriter(writer);

            Set<VisitHint> hints = new HashSet<VisitHint>();
            /*unrendered have to be skipped, transient definitely must be added to our list!*/
            hints.add(VisitHint.SKIP_UNRENDERED);

            VisitContext visitCtx = VisitContext.createVisitContext(_facesContext, renderIds, hints);
            viewRoot.visitTree(visitCtx, new PhaseAwareVisitCallback(_facesContext, phaseId));

        } catch (IOException ex) {
            Log log = LogFactory.getLog(PartialViewContextImpl.class);
            if (log.isErrorEnabled()) {
                log.error(ex);
            }

        } finally {
            try {
                if (inDocument) {
                    writer.endDocument();
                }
                writer.flush();
            } catch (IOException ex) {
                Log log = LogFactory.getLog(PartialViewContextImpl.class);
                if (log.isErrorEnabled()) {
                    log.error(ex);
                }
            }

            _facesContext.setResponseWriter(oldWriter);
        }

    }

    /**
     * has to be thrown in many of the methods if the method is called after the instance has been released!
     */
    private void assertNotReleased() {
        if (_released) {
            throw new IllegalStateException("Error the FacesContext is already released!");
        }
    }

    @Override
    public void release() {
        assertNotReleased();
        _executeClientIds = null;
        _renderClientIds = null;
        _ajaxRequest = null;
        _partialRequest = null;
        _renderAll = null;
        _facesContext = null;
        _released = true;
    }

    private class PhaseAwareVisitCallback implements VisitCallback {

        private PhaseId _phaseId;
        private FacesContext _facesContext;

        public PhaseAwareVisitCallback(FacesContext facesContext, PhaseId phaseId) {
            this._phaseId = phaseId;
            this._facesContext = facesContext;
        }

        public VisitResult visit(VisitContext context, UIComponent target) {
            if (_phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                target.processDecodes(_facesContext);
            } else if (_phaseId == PhaseId.PROCESS_VALIDATIONS) {
                target.processValidators(_facesContext);
            } else if (_phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                target.processUpdates(_facesContext);
            } else if (_phaseId == PhaseId.RENDER_RESPONSE) {
                processRenderComponent(target);
            } else {
                throw new IllegalStateException("PPR Response, illegale phase called");
            }

            // Return VisitResult.REJECT as processDecodes/Validators/Updates already traverse sub tree
            return VisitResult.REJECT;
        }

        /**
         * the rendering subpart of the tree walker
         * every component id which is passed down via render must be handled
         * here!
         *
         * @param target the target component to be handled!
         */
        private void processRenderComponent(UIComponent target) {
            boolean inUpdate = false;
            PartialResponseWriter writer = (PartialResponseWriter) _facesContext.getResponseWriter();
            try {
                writer.startUpdate(target.getClientId(_facesContext));
                inUpdate = true;
                target.encodeAll(_facesContext);
            } catch (IOException ex) {
                Log log = LogFactory.getLog(PartialViewContextImpl.class);
                if (log.isErrorEnabled()) {
                    log.error("IOException for rendering component", ex);
                }
            } finally {
                if (inUpdate) {
                    try {
                        writer.endUpdate();
                    } catch (IOException ex) {
                        Log log = LogFactory.getLog(PartialViewContextImpl.class);
                        if (log.isErrorEnabled()) {
                            log.error("IOException for rendering component, stopping update rendering", ex);
                        }
                    }
                }
            }
        }
    }
}
