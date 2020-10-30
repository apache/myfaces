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
package org.apache.myfaces.webapp;

import jakarta.el.ELContextEvent;
import jakarta.el.ELContextListener;
import jakarta.faces.context.FacesContext;

/**
 * {@link ELContextListener} which installs the {@link FacesContext} (if present),
 * into the {@link jakarta.el.ELContext} and dispatches ELContext events to
 * the Faces application #{@link ELContextListener}.
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesELContextListener implements ELContextListener
{
    @Override
    public void contextCreated(ELContextEvent ece)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null)
        {
            ece.getELContext().putContext(FacesContext.class, facesContext);

            for (ELContextListener listener : facesContext.getApplication().getELContextListeners())
            {
                listener.contextCreated(ece);
            }
        }
    }

}
