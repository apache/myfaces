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
package javax.faces.application;

import javax.faces.context.FacesContext;
import java.io.IOException;


/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Stan Silvert
 * @version $Revision$ $Date$
 */
public abstract class StateManager
{
    public static final String STATE_SAVING_METHOD_PARAM_NAME = "javax.faces.STATE_SAVING_METHOD";
    public static final String STATE_SAVING_METHOD_CLIENT = "client";
    public static final String STATE_SAVING_METHOD_SERVER = "server";
    private Boolean _savingStateInClient = null;
    
    /**
     * @deprecated
     */
    public StateManager.SerializedView saveSerializedView(javax.faces.context.FacesContext context) {
        return null;
    }
    
    /**
     * @since 1.2
     */
    public Object saveView(FacesContext context) {
        StateManager.SerializedView serializedView = saveSerializedView(context);
        if (serializedView == null) return null;
        
        Object[] structureAndState = new Object[2];
        structureAndState[0] = serializedView.getStructure();
        structureAndState[1] = serializedView.getState();
        
        return structureAndState;
    }
    
    /**
     * @deprecated
     */
    protected Object getTreeStructureToSave(javax.faces.context.FacesContext context) {
        return null;
    }

    /**
     * @deprecated
     */
    protected Object getComponentStateToSave(javax.faces.context.FacesContext context) {
        return null;
    }

    /**
     * @deprecated
     */
    public void writeState(javax.faces.context.FacesContext context,
                                    StateManager.SerializedView state)
            throws java.io.IOException {
        // default impl does nothing as per JSF 1.2 javadoc
    }
    
    /**
     * @since 1.2
     */
    public void writeState(FacesContext context, Object state) throws IOException {
        if (!(state instanceof Object[])) return;
        Object[] structureAndState = (Object[])state;
        if (structureAndState.length < 2) return;
        
        writeState(context, new StateManager.SerializedView(structureAndState[0], structureAndState[1]));
    }

    public abstract javax.faces.component.UIViewRoot restoreView(javax.faces.context.FacesContext context,
                                                                 String viewId,
                                                                 String renderKitId);

    /**
     * @deprecated
     */
    protected javax.faces.component.UIViewRoot restoreTreeStructure(javax.faces.context.FacesContext context,
                                                                    String viewId,
                                                                    String renderKitId) {
        return null;
    }

    /**
     * @deprecated
     */
    protected void restoreComponentState(javax.faces.context.FacesContext context,
                                         javax.faces.component.UIViewRoot viewRoot,
                                         String renderKitId) {
        // default impl does nothing as per JSF 1.2 javadoc
    }

    public boolean isSavingStateInClient(javax.faces.context.FacesContext context)
    {
        if(context == null) throw new NullPointerException("context");
        if (_savingStateInClient != null) return _savingStateInClient.booleanValue();
        String stateSavingMethod = context.getExternalContext().getInitParameter(STATE_SAVING_METHOD_PARAM_NAME);
        if (stateSavingMethod == null)
        {
            _savingStateInClient = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("No state saving method defined, assuming default server state saving");
        }
        else if (stateSavingMethod.equals(STATE_SAVING_METHOD_CLIENT))
        {
            _savingStateInClient = Boolean.TRUE;
        }
        else if (stateSavingMethod.equals(STATE_SAVING_METHOD_SERVER))
        {
            _savingStateInClient = Boolean.FALSE;
        }
        else
        {
            _savingStateInClient = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("Illegal state saving method '" + stateSavingMethod + "', default server state saving will be used");
        }
        return _savingStateInClient.booleanValue();
    }


    /**
     * @deprecated
     */
    public class SerializedView 
    {
        private Object _structure;
        private Object _state;

        /**
         * @deprecated
         */
        public SerializedView(Object structure, Object state)
        {
            _structure = structure;
            _state = state;
        }

        /**
         * @deprecated
         */
        public Object getStructure()
        {
            return _structure;
        }

        /**
         * @deprecated
         */
        public Object getState()
        {
            return _state;
        }
    }
}
