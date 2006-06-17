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
package javax.faces.render;

import javax.faces.application.StateManager;
import javax.faces.context.FacesContext;
import java.io.IOException;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Stan Silvert
 * @version $Revision$ $Date$
 */
public abstract class ResponseStateManager
{
    public static final String RENDER_KIT_ID_PARAM = "javax.faces.RenderKitId";
    public static final String VIEW_STATE_PARAM = "javax.faces.ViewState";
    
    public void writeState(FacesContext context, Object state) throws IOException{
        
        throw new UnsupportedOperationException("1.2");
        
    }
    
    /**
     * @deprecated
     */
    public void writeState(FacesContext context,
                           StateManager.SerializedView state)
            throws IOException {
        // does nothing as per JSF 1.2 javadoc
    }

    /**
     * @since 1.2
     */
    public Object getState(FacesContext context, String viewId) {
        Object[] structureAndState = new Object[2];
        structureAndState[0] = getTreeStructureToRestore(context, viewId);
        structureAndState[1] = getComponentStateToRestore(context);
        return structureAndState;
    }
    
    
    /**
     * @deprecated
     */
    public Object getTreeStructureToRestore(FacesContext context,
                                             String viewId) {
        return null;
    }
    

    /**
     * @deprecated
     */
    public Object getComponentStateToRestore(FacesContext context) {
        return null;
    }
    
    /**
     * @since 1.2
     */
    public boolean isPostback(FacesContext context) {
        return context.getExternalContext().getRequestParameterMap().size() > 0;
    }

}
