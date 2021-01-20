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

package org.apache.myfaces.test.mock.lifecycle;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * @author Mathias Broekelmann (latest modification by $Author: mbr $)
 * @version $Revision: 517403 $ $Date: 2007-03-12 22:17:00 +0100 (Mo, 12 Mrz 2007) $
 * @since 1.0.0
 */
public class DefaultRestoreViewSupport implements RestoreViewSupport
{
    private static final String JAKARTA_SERVLET_INCLUDE_SERVLET_PATH = "jakarta.servlet.include.servlet_path";

    private static final String JAKARTA_SERVLET_INCLUDE_PATH_INFO = "jakarta.servlet.include.path_info";

    private static final Logger log = Logger.getLogger(DefaultRestoreViewSupport.class.getName());

    @Override
    public void processComponentBinding(FacesContext facesContext,
            UIComponent component)
    {
        ValueExpression binding = component.getValueExpression("binding");
        if (binding != null)
        {
            binding.setValue(facesContext.getELContext(), component);
        }

        for (Iterator iter = component.getFacetsAndChildren(); iter.hasNext();)
        {
            processComponentBinding(facesContext, (UIComponent) iter.next());
        }
    }

    @Override
    public String calculateViewId(FacesContext facesContext)
    {
        ExternalContext externalContext = facesContext.getExternalContext();
        Map requestMap = externalContext.getRequestMap();

        String viewId = (String) requestMap.get(JAKARTA_SERVLET_INCLUDE_PATH_INFO);
        if (viewId != null)
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "Calculated viewId '" + viewId
                        + "' from request param '"
                        + JAKARTA_SERVLET_INCLUDE_PATH_INFO + '\'');
            }
        }
        else
        {
            viewId = externalContext.getRequestPathInfo();
            if (viewId != null && log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "Calculated viewId '" + viewId
                        + "' from request path info");
            }
        }

        if (viewId == null)
        {
            viewId = (String) requestMap.get(JAKARTA_SERVLET_INCLUDE_SERVLET_PATH);
            if (viewId != null && log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "Calculated viewId '" + viewId
                        + "' from request param '"
                        + JAKARTA_SERVLET_INCLUDE_SERVLET_PATH + '\'');
            }
        }

        if (viewId == null)
        {
            viewId = externalContext.getRequestServletPath();
            if (viewId != null && log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "Calculated viewId '" + viewId
                        + "' from request servlet path");
            }
        }

        if (viewId == null)
        {
            throw new FacesException("Could not determine view id.");
        }

        return viewId;
    }

    @Override
    public boolean isPostback(FacesContext facesContext)
    {
        return true;
    }
}
