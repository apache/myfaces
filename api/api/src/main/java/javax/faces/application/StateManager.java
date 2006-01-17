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




/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class StateManager
{
    public static final String STATE_SAVING_METHOD_PARAM_NAME = "javax.faces.STATE_SAVING_METHOD";
    public static final String STATE_SAVING_METHOD_CLIENT = "client";
    public static final String STATE_SAVING_METHOD_SERVER = "server";
    private Boolean _savingStateInClient = null;

    public abstract StateManager.SerializedView saveSerializedView(javax.faces.context.FacesContext context);

    protected abstract Object getTreeStructureToSave(javax.faces.context.FacesContext context);

    protected abstract Object getComponentStateToSave(javax.faces.context.FacesContext context);

    public abstract void writeState(javax.faces.context.FacesContext context,
                                    StateManager.SerializedView state)
            throws java.io.IOException;

    public abstract javax.faces.component.UIViewRoot restoreView(javax.faces.context.FacesContext context,
                                                                 String viewId,
                                                                 String renderKitId);

    protected abstract javax.faces.component.UIViewRoot restoreTreeStructure(javax.faces.context.FacesContext context,
                                                                             String viewId,
                                                                             String renderKitId);

    protected abstract void restoreComponentState(javax.faces.context.FacesContext context,
                                                  javax.faces.component.UIViewRoot viewRoot,
                                                  String renderKitId);

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


    public class SerializedView
    {
        private Object _structure;
        private Object _state;

        public SerializedView(Object structure, Object state)
        {
            _structure = structure;
            _state = state;
        }

        public Object getStructure()
        {
            return _structure;
        }

        public Object getState()
        {
            return _state;
        }
    }
}
