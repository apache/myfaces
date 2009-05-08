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

import org.apache.myfaces.shared_impl.util.StringUtils;

public class PartialViewContextImpl extends PartialViewContext
{
    private static final String METHOD_GETEXECUTEIDS = "";
    private static final String METHOD_GETPARTIALRESPONSEWRITER = "";
    private static final String METHOD_GETRENDERIDS = "";
    private static final String METHOD_ISAJAXREQUEST = "";
    private static final String METHOD_ISEXECUTEALL = "";
    private static final String METHOD_ISPARTIALREQUEST = "";
    private static final String METHOD_ISRENDERALL = "";
    private static final String METHOD_PROCESSPARTIAL = "";
    private static final String METHOD_RELEASE = "";
    private static final String METHOD_SETPARTIALREQUEST = "";
    private static final String METHOD_SETRENDERALL = "";
    
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
    

    public PartialViewContextImpl(FacesContext context)
    {
        _facesContext = context;
    }

    @Override
    public boolean isAjaxRequest()
    {
        assertNotReleased(METHOD_ISAJAXREQUEST);
        
        if (_ajaxRequest == null)
        {
            String requestType = _facesContext.getExternalContext().
                getRequestHeaderMap().get(FACES_REQUEST);
            _ajaxRequest = (requestType != null && PARTIAL_AJAX.equals(requestType));
        }
        return _ajaxRequest;
    }

    @Override
    public boolean isExecuteAll()
    {
        assertNotReleased(METHOD_ISEXECUTEALL);
        
        if (isAjaxRequest())
        {
            String executeMode = _facesContext.getExternalContext().
                getRequestParameterMap().get(
                        PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME);
            if (PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode))
            {
                return true;
            }
        }        
        return false;
    }

    @Override
    public boolean isPartialRequest()
    {
        assertNotReleased(METHOD_ISPARTIALREQUEST);

        if (_partialRequest == null)
        {
            String requestType = _facesContext.getExternalContext().
                getRequestHeaderMap().get(FACES_REQUEST);
            _partialRequest = (requestType != null && PARTIAL_PROCESS.equals(requestType));
        }
        return isAjaxRequest() || _partialRequest;
    }

    @Override
    public boolean isRenderAll()
    {
        assertNotReleased(METHOD_ISRENDERALL);
        
        if (_renderAll == null)
        {
            if (isAjaxRequest())
            {
                String executeMode = _facesContext.getExternalContext().
                    getRequestParameterMap().get(
                            PartialViewContext.PARTIAL_RENDER_PARAM_NAME);
                if (PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode))
                {
                    _renderAll = true;
                }
            }
            if (_renderAll == null)
            {
                _renderAll = false;
            }
        }
        return _renderAll;
    }
    

    @Override
    public void setPartialRequest(boolean isPartialRequest)
    {
        assertNotReleased(METHOD_SETPARTIALREQUEST);

        _partialRequest = isPartialRequest;
        
    }

    @Override
    public void setRenderAll(boolean renderAll)
    {
        assertNotReleased(METHOD_SETRENDERALL);

        _renderAll = renderAll;
    }
    
    @Override
    public Collection<String> getExecuteIds()
    {
        assertNotReleased(METHOD_GETEXECUTEIDS);
        
        if (_executeClientIds == null)
        {
            String executeMode = _facesContext.getExternalContext().
            getRequestParameterMap().get(
                    PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME);
            
            if (executeMode != null && !"".equals(executeMode) &&
                !PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode) &&
                !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode))
            {
                String[] clientIds = StringUtils.splitShortString(executeMode.replaceAll("[ \\t\\n]*",""), ',');

                //The collection must be mutable
                List<String> tempList = new ArrayList<String>();
                for (int i = 0; i < clientIds.length ; i++)
                {
                    tempList.add(clientIds[i]);
                }
                _executeClientIds = tempList;
            }
            else
            {
                _executeClientIds = new ArrayList<String>();                
            }
        }        
        return _executeClientIds;
    }
    

    @Override
    public Collection<String> getRenderIds()
    {
        assertNotReleased(METHOD_GETRENDERIDS);
        
        if (_renderClientIds == null)
        {
            String renderMode = _facesContext.getExternalContext().
            getRequestParameterMap().get(
                    PartialViewContext.PARTIAL_RENDER_PARAM_NAME);
            
            if (renderMode != null && !"".equals(renderMode) && 
                !PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode) &&
                !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode))
            {
                String[] clientIds = StringUtils.splitShortString(renderMode.replaceAll("[ \\t\\n]*",""), ',');

                //The collection must be mutable
                List<String> tempList = new ArrayList<String>();
                for (int i = 0; i < clientIds.length ; i++)
                {
                    tempList.add(clientIds[i]);
                }
                _renderClientIds = tempList;
            }
            else
            {
                _renderClientIds = new ArrayList<String>();                
            }
        }        
        return _renderClientIds;
    }    

    @Override
    public PartialResponseWriter getPartialResponseWriter()
    {
        assertNotReleased(METHOD_GETPARTIALRESPONSEWRITER);
        //TODO: JSF 2.0, add impl
        
        return new PartialResponseWriter(_facesContext.getResponseWriter());
    }
    
    @Override
    public void processPartial(PhaseId phaseId)
    {
        assertNotReleased(METHOD_PROCESSPARTIAL);

        UIComponent viewRoot = _facesContext.getViewRoot();

        if (phaseId == PhaseId.APPLY_REQUEST_VALUES
                || phaseId == PhaseId.PROCESS_VALIDATIONS
                || phaseId == PhaseId.UPDATE_MODEL_VALUES)
        {
            Set<VisitHint> hints = new HashSet<VisitHint>();
            hints.add(VisitHint.EXECUTE_LIFECYCLE);
            hints.add(VisitHint.SKIP_UNRENDERED);
            VisitContext visitCtx = VisitContext.createVisitContext(_facesContext, getRenderIds(), hints);
            viewRoot.visitTree(visitCtx, new PhaseAwareVisitCallback(_facesContext, phaseId));
        }
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

    @Override
    public void release()
    {
        assertNotReleased(METHOD_RELEASE);
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

        @Override
        public VisitResult visit(VisitContext context, UIComponent target) {
            if (_phaseId == PhaseId.APPLY_REQUEST_VALUES)
            {
                target.processDecodes(_facesContext);
            }
            else if (_phaseId == PhaseId.PROCESS_VALIDATIONS)
            {
                target.processValidators(_facesContext);
            }
            else if (_phaseId == PhaseId.UPDATE_MODEL_VALUES)
            {
                target.processUpdates(_facesContext);
            }

            // Return VisitResult.REJECT as processDecodes/Validators/Updates already traverse sub tree
            return VisitResult.REJECT;
        }
    }
}
