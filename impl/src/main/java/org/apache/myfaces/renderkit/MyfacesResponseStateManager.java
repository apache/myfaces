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
package org.apache.myfaces.renderkit;

import javax.faces.application.StateManager;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import java.io.IOException;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class MyfacesResponseStateManager
        extends ResponseStateManager
{
    /**
     * Writes hidden form inputs with the state info to be saved.
     * {@link StateManager} delegates calls to {@link javax.faces.application.StateManager#writeState}
     * to this method.
     */
    public abstract void writeState(FacesContext facescontext,
                                    StateManager.SerializedView serializedview) throws IOException;

    /**
     * Writes url parameters with the state info to be saved.
     * {@link org.apache.myfaces.application.MyfacesStateManager} delegates calls to
     * {@link org.apache.myfaces.application.MyfacesStateManager#writeState} to this method.
     */
    public abstract void writeStateAsUrlParams(FacesContext facescontext,
                                               StateManager.SerializedView serializedview) throws IOException;
}
