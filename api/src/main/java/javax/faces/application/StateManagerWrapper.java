/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.io.IOException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Stan Silvert
 */
public abstract class StateManagerWrapper extends StateManager {
    
    protected abstract StateManager getWrapped();
    
    public StateManager.SerializedView saveSerializedView(FacesContext context) {
        return getWrapped().saveSerializedView(context);
    }
    
    public Object saveView(FacesContext context) {
        return getWrapped().saveView(context);
    }

    public boolean isSavingStateInClient(FacesContext context) {
        return getWrapped().isSavingStateInClient(context);
    }

    protected Object getTreeStructureToSave(FacesContext context) {
        return getWrapped().getTreeStructureToSave(context);
    }

    protected Object getComponentStateToSave(FacesContext context) {
        return getWrapped().getComponentStateToSave(context);
    }

    public void writeState(FacesContext context, StateManager.SerializedView state) throws IOException {
        getWrapped().writeState(context, state);
    }
    
    public void writeState(FacesContext context, Object state) throws IOException {
        getWrapped().writeState(context, state);
    }

    public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
        return getWrapped().restoreView(context, viewId, renderKitId);
    }

    protected UIViewRoot restoreTreeStructure(FacesContext context, String viewId, String renderKitId) {
        return getWrapped().restoreTreeStructure(context, viewId, renderKitId);
    }

    protected void restoreComponentState(FacesContext context, UIViewRoot viewRoot, String renderKitId) {
        getWrapped().restoreComponentState(context, viewRoot, renderKitId);
    }
    
}
