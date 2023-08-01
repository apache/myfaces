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
package org.apache.myfaces.context;

import jakarta.faces.FacesWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.view.facelets.LocationAware;
import org.apache.myfaces.view.facelets.el.ContextAware;

public class ExceptionHandlerUtils
{

    public static void logException(ExceptionQueuedEventContext event, Logger logger)
    {
        // unwrap to strip location aware stacktraces
        Throwable exception = event.getException();
        while (exception instanceof FacesWrapper)
        {
            exception = (Throwable) ((FacesWrapper) exception).getWrapped();
        }

        String msg = exception.getClass().getName() + " occurred while processing " + event.getPhaseId().getName();
        String location = buildLocation(event.getException(), event.getComponent());
        if (location != null)
        {
            msg += " [Location=" + location + "]";
        }

        logger.log(Level.SEVERE, msg, exception);
    }

    public static String buildLocation(Throwable ex, UIComponent component)
    {
        String location = "";
        if (ex instanceof ContextAware)
        {
            ContextAware caex = (ContextAware) ex;
            location = caex.getLocation().toString() + ", " +
                                   caex.getQName() + "=\"" +
                                   caex.getExpressionString() + '"';
        }
        else if (ex instanceof LocationAware)
        {
            LocationAware laex = (LocationAware) ex;
            if (laex.getLocation() != null)
            {
                location = laex.getLocation().toString();
            }
        }

        while (ex.getCause() != null)
        {
            ex = ex.getCause();
            if (ex instanceof ContextAware)
            {
                ContextAware caex = (ContextAware) ex;
                location = caex.getLocation().toString() + ", " +
                        caex.getQName() + "=\"" +
                        caex.getExpressionString() + '"';
            }
            else if (ex instanceof LocationAware)
            {
                LocationAware laex = (LocationAware) ex;
                if (laex.getLocation() != null)
                {
                    location = laex.getLocation().toString();
                }
            }
        }

        if (component != null)
        {
            if (!location.isBlank())
            {
                location += ", ";
            }
            location += "clientId=\"" + component.getClientId() + "\"";
        }

        return location;
    }
}
