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

package org.apache.myfaces.test.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialResponseWriter;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseId;

import org.apache.myfaces.test.mock.visit.MockVisitCallback;

/**
 * <p>Mock implementation of <code>PartialViewContext</code>.</p>
 * <p/>
 * $Id$
 *
 * @since 1.0.0
 */
public class MockPartialViewContext extends PartialViewContext
{

    private static final String FACES_REQUEST = "Faces-Request";
    private static final String PARTIAL_AJAX = "partial/ajax";
    private static final String PARTIAL_PROCESS = "partial/process";
    private static final String SOURCE_PARAM_NAME = "jakarta.faces.source";
    private FacesContext _facesContext = null;
    private Boolean _ajaxRequest = null;
    private Collection<String> _executeClientIds = null;
    private Collection<String> _renderClientIds = null;
    private Boolean _partialRequest = null;
    private Boolean _renderAll = null;
    private PartialResponseWriter _partialResponseWriter = null;
    private List<String> _evalScripts = new ArrayList<String>();

    public MockPartialViewContext(FacesContext context)
    {
        _facesContext = context;
    }

    @Override
    public boolean isAjaxRequest()
    {
        if (_ajaxRequest == null)
        {
            String requestType = _facesContext.getExternalContext()
                    .getRequestHeaderMap().get(FACES_REQUEST);
            _ajaxRequest = (requestType != null && PARTIAL_AJAX
                    .equals(requestType));
        }
        return _ajaxRequest;
    }

    @Override
    public boolean isExecuteAll()
    {

        if (isAjaxRequest())
        {
            String executeMode = _facesContext.getExternalContext()
                    .getRequestParameterMap().get(
                            PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME);
            if (PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS
                    .equals(executeMode))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPartialRequest()
    {

        if (_partialRequest == null)
        {
            String requestType = _facesContext.getExternalContext()
                    .getRequestHeaderMap().get(FACES_REQUEST);
            _partialRequest = (requestType != null && PARTIAL_PROCESS
                    .equals(requestType));
        }
        return isAjaxRequest() || _partialRequest;
    }

    @Override
    public boolean isRenderAll()
    {

        if (_renderAll == null)
        {
            if (isAjaxRequest())
            {
                String executeMode = _facesContext.getExternalContext()
                        .getRequestParameterMap().get(
                                PartialViewContext.PARTIAL_RENDER_PARAM_NAME);
                if (PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS
                        .equals(executeMode))
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

        _partialRequest = isPartialRequest;

    }

    @Override
    public void setRenderAll(boolean renderAll)
    {

        _renderAll = renderAll;
    }

    @Override
    public Collection<String> getExecuteIds()
    {

        if (_executeClientIds == null)
        {
            String executeMode = _facesContext.getExternalContext()
                    .getRequestParameterMap().get(
                            PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME);

            if (executeMode != null
                    && !"".equals(executeMode)
                    &&
                    //!PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode) &&
                    !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS
                            .equals(executeMode))
            {

                String[] clientIds = splitShortString(
                        _replaceTabOrEnterCharactersWithSpaces(executeMode),
                        ' ');

                //The collection must be mutable
                List<String> tempList = new ArrayList<String>();
                for (String clientId : clientIds)
                {
                    if (clientId.length() > 0)
                    {
                        tempList.add(clientId);
                    }
                }
                // The "jakarta.faces.source" parameter needs to be added to the list of
                // execute ids if missing (otherwise, we'd never execute an action associated
                // with, e.g., a button).

                String source = _facesContext.getExternalContext()
                        .getRequestParameterMap().get(SOURCE_PARAM_NAME);

                if (source != null)
                {
                    source = source.trim();

                    if (!tempList.contains(source))
                    {
                        tempList.add(source);
                    }
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

        if (_renderClientIds == null)
        {
            String renderMode = _facesContext.getExternalContext()
                    .getRequestParameterMap().get(
                            PartialViewContext.PARTIAL_RENDER_PARAM_NAME);

            if (renderMode != null
                    && !"".equals(renderMode)
                    &&
                    //!PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode) &&
                    !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS
                            .equals(renderMode))
            {
                String[] clientIds = splitShortString(
                        _replaceTabOrEnterCharactersWithSpaces(renderMode), ' ');

                //The collection must be mutable
                List<String> tempList = new ArrayList<String>();
                for (String clientId : clientIds)
                {
                    if (clientId.length() > 0)
                    {
                        tempList.add(clientId);
                    }
                }
                _renderClientIds = tempList;
            }
            else
            {
                _renderClientIds = new ArrayList<String>();

                if (PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS
                        .equals(renderMode))
                {
                    _renderClientIds
                            .add(PartialResponseWriter.RENDER_ALL_MARKER);
                }
            }
        }
        return _renderClientIds;
    }

    @Override
    public PartialResponseWriter getPartialResponseWriter()
    {
        if (_partialResponseWriter == null)
        {
            ResponseWriter responseWriter = _facesContext.getResponseWriter();
            if (responseWriter == null)
            {
                // This case happens when getPartialResponseWriter() is called before
                // render phase, like in ExternalContext.redirect(). We have to create a
                // ResponseWriter from the RenderKit and then wrap if necessary. 
                try
                {
                    responseWriter = _facesContext.getRenderKit()
                            .createResponseWriter(
                                    _facesContext.getExternalContext()
                                            .getResponseOutputWriter(),
                                    "text/xml",
                                    _facesContext.getExternalContext()
                                            .getRequestCharacterEncoding());
                }
                catch (IOException e)
                {
                    throw new IllegalStateException(
                            "Cannot create Partial Response Writer", e);
                }
            }
            // It is possible that the RenderKit return a PartialResponseWriter instance when 
            // createResponseWriter,  so we should cast here for it and prevent double wrapping.
            if (responseWriter instanceof PartialResponseWriter writer)
            {
                _partialResponseWriter = writer;
            }
            else
            {
                _partialResponseWriter = new PartialResponseWriter(
                        responseWriter);
            }
        }
        return _partialResponseWriter;
    }

    @Override
    public void processPartial(PhaseId phaseId)
    {

        UIViewRoot viewRoot = _facesContext.getViewRoot();

        VisitContext visitCtx = VisitContext.createVisitContext(_facesContext,
                null, null);
        viewRoot.visitTree(visitCtx, new MockVisitCallback());
    }

    @Override
    public void release()
    {
        _executeClientIds = null;
        _renderClientIds = null;
        _ajaxRequest = null;
        _partialRequest = null;
        _renderAll = null;
        _facesContext = null;
        _evalScripts = new ArrayList<String>();
    }
    
    public List<String> getEvalScripts()
    {
        return _evalScripts;
    }

    private static String[] splitShortString(String str, char separator)
    {
        int len = str.length();

        int lastTokenIndex = 0;

        // Step 1: how many substrings?
        //      We exchange double scan time for less memory allocation
        for (int pos = str.indexOf(separator); pos >= 0; pos = str.indexOf(
                separator, pos + 1))
        {
            lastTokenIndex++;
        }

        // Step 2: allocate exact size array
        String[] list = new String[lastTokenIndex + 1];

        int oldPos = 0;

        // Step 3: retrieve substrings
        int pos = str.indexOf(separator);
        int i = 0;
        
        while (pos >= 0)
        {
            list[i++] = str.substring(oldPos, pos);
            oldPos = (pos + 1);
            pos = str.indexOf(separator, oldPos);
        }

        list[lastTokenIndex] = str.substring(oldPos, len);

        return list;
    }

    private String _replaceTabOrEnterCharactersWithSpaces(String mode)
    {
        StringBuilder builder = new StringBuilder(mode.length());
        for (int i = 0; i < mode.length(); i++)
        {
            if (mode.charAt(i) == '\t' || mode.charAt(i) == '\n')
            {
                builder.append(' ');
            }
            else
            {
                builder.append(mode.charAt(i));
            }
        }
        return builder.toString();
    }
}
