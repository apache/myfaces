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
 * @version $Revision$ $Date$
 */
public abstract class ResponseStateManager
{
	
    /**
     * @deprecated
     */
    public abstract void writeState(FacesContext context,
                                    StateManager.SerializedView state)
            throws IOException;

    /**
     * @deprecated
     */
    public abstract Object getTreeStructureToRestore(FacesContext context,
                                                     String viewId);

    /**
     * @deprecated
     */
    public abstract Object getComponentStateToRestore(FacesContext context);

    public abstract boolean isPostback(FacesContext context); 
    
    public abstract Object getState(FacesContext context, String viewId);
    
    public abstract void writeState(FacesContext context, Object state) ;
    
}
