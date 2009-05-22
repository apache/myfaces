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
package org.apache.myfaces.renderkit.html;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.MyfacesResponseStateManager;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.apache.myfaces.shared_impl.util.StateUtils;
import org.apache.myfaces.shared_impl.config.MyfacesConfig;

import javax.faces.application.StateManager;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlResponseStateManager
    extends MyfacesResponseStateManager {
    private static final Log log = LogFactory.getLog(HtmlResponseStateManager.class);

    private static final int TREE_PARAM = 0;
    private static final int STATE_PARAM = 1;
    private static final int VIEWID_PARAM = 2;

    public static final String STANDARD_STATE_SAVING_PARAM = "javax.faces.ViewState";

    public void writeState(FacesContext facescontext,
                           StateManager.SerializedView serializedview) throws IOException {
        ResponseWriter responseWriter = facescontext.getResponseWriter();

        Object[] savedState = new Object[3];

        if (facescontext.getApplication().getStateManager().isSavingStateInClient(facescontext)) {
            Object treeStruct = serializedview.getStructure();
            Object compStates = serializedview.getState();

            if(treeStruct != null)
            {
                savedState[TREE_PARAM]=treeStruct;
            }
            else {
                log.error("No tree structure to be saved in client response!");
            }

            if (compStates != null) {
                savedState[STATE_PARAM] = compStates;
            }
            else {
                log.error("No component states to be saved in client response!");
            }
        }
        else {
            // write viewSequence
            Object treeStruct = serializedview.getStructure();
            if (treeStruct != null) {
                if (treeStruct instanceof String) {
                    savedState[TREE_PARAM] = treeStruct;
                }
            }
        }

        savedState[VIEWID_PARAM] = facescontext.getViewRoot().getViewId();

        responseWriter.startElement(HTML.INPUT_ELEM, null);
        responseWriter.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
        responseWriter.writeAttribute(HTML.NAME_ATTR, STANDARD_STATE_SAVING_PARAM, null);

        MyfacesConfig myfacesConfig = MyfacesConfig.getCurrentInstance(facescontext.getExternalContext());
        if (myfacesConfig.isRenderViewStateId()) {
            responseWriter.writeAttribute(HTML.ID_ATTR, STANDARD_STATE_SAVING_PARAM, null);
        }
        responseWriter.writeAttribute(HTML.VALUE_ATTR, StateUtils.construct(savedState,
                facescontext.getExternalContext()), null);
        responseWriter.endElement(HTML.INPUT_ELEM);
    }

    /**
     * MyFaces extension
     *
     * @param facescontext
     * @param serializedview
     * @throws IOException
     */
    public void writeStateAsUrlParams(FacesContext facescontext,
                                      StateManager.SerializedView serializedview) throws IOException {

        throw new UnsupportedOperationException("long been deprecated...");
/*        ResponseWriter responseWriter = facescontext.getResponseWriter();
        Object treeStruct = serializedview.getStructure();
        Object compStates = serializedview.getState();

        if (treeStruct != null) {
            if (treeStruct instanceof String) {
                if (StateUtils.isSecure(facescontext.getExternalContext()))
                    treeStruct = StateUtils.construct(treeStruct, facescontext.getExternalContext());
                writeStateParam(responseWriter, TREE_PARAM, (String) treeStruct);
            }
            else {
                writeStateParam(responseWriter, BASE64_TREE_PARAM,
                                StateUtils.construct(treeStruct, facescontext.getExternalContext()));
            }
        }
        else {
            log.error("No tree structure to be saved in client response!");
        }

        if (compStates != null) {
            if (treeStruct != null) {
                responseWriter.write('&');
            }
            if (compStates instanceof String) {
                if (StateUtils.isSecure(facescontext.getExternalContext()))
                    compStates = StateUtils.construct(compStates, facescontext.getExternalContext());
                writeStateParam(responseWriter, STATE_PARAM, (String) compStates);
            }
            else {
                writeStateParam(responseWriter, BASE64_STATE_PARAM, StateUtils.construct(compStates, facescontext.getExternalContext()));
            }
        }
        else {
            log.error("No component states to be saved in client response!");
        }

        if (treeStruct != null || compStates != null) {
            responseWriter.write('&');
        }
        writeStateParam(responseWriter, VIEWID_PARAM, facescontext.getViewRoot().getViewId());


        private void writeStateParam(ResponseWriter writer, String name, String value)
            throws IOException {
            writer.write(name);
            writer.write('=');
            writer.write(URLEncoder.encode(value, writer.getCharacterEncoding()));
        }

        */
    }

    public Object getTreeStructureToRestore(FacesContext facesContext, String viewId) {
        Map reqParamMap = facesContext.getExternalContext().getRequestParameterMap();

        Object encodedState = reqParamMap.get(STANDARD_STATE_SAVING_PARAM);

        if(encodedState==null  || (((String) encodedState).length() == 0))
            return null;        

        Object[] savedState = (Object[]) StateUtils.reconstruct((String) encodedState, facesContext.getExternalContext());

        String restoredViewId = (String) savedState[VIEWID_PARAM];

        if (restoredViewId == null || !restoredViewId.equals(viewId)) {
            //no saved state or state of different viewId
            return null;
        }

        return savedState[TREE_PARAM];
    }

    public Object getComponentStateToRestore(FacesContext facesContext) {
        Map reqParamMap = facesContext.getExternalContext().getRequestParameterMap();

        Object encodedState = reqParamMap.get(STANDARD_STATE_SAVING_PARAM);

        if(encodedState==null  || (((String) encodedState).length() == 0))
            return null;

        Object[] savedState = (Object[]) StateUtils.reconstruct((String) encodedState, facesContext.getExternalContext());

        String restoredViewId = (String) savedState[VIEWID_PARAM];

        if (restoredViewId == null) {
            //no saved state or state of different viewId
            return null;
        }

        return savedState[STATE_PARAM];
    }

}

