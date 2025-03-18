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

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.FacesWrapper;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.view.facelets.LocationAware;
import org.apache.myfaces.view.facelets.el.ContextAware;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandlerUtils
{
    protected static boolean isLogStacktrace(FacesContext context, Throwable exception)
    {
        if (context.isProjectStage(ProjectStage.Production))
        {
            if (exception instanceof ViewExpiredException)
            {
                return false;
            }
        }

        return true;
    }

    protected static boolean isLogException(FacesContext context, Throwable exception)
    {
        if (context.isProjectStage(ProjectStage.Production))
        {
            if (exception != null)
            {
                MyfacesConfig myfacesConfig = MyfacesConfig.getCurrentInstance(context);
                for (String ignore : myfacesConfig.getExceptionTypesToIgnoreInLogging())
                {
                    if (ignore.trim().equals(exception.getClass().getName()))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static void logException(Throwable exception, UIComponent component, FacesContext context, Logger logger)
    {
        if (exception == null)
        {
            return;
        }

        while (exception instanceof FacesWrapper)
        {
            exception = (Throwable) ((FacesWrapper) exception).getWrapped();
        }

        while ((exception.getClass().equals(FacesException.class) || exception.getClass().equals(ELException.class)))
        {
            if (exception.getCause() != null)
            {
                exception = exception.getCause();
            }
            else
            {
                break;
            }
        }

        if (!isLogException(context, exception))
        {
            return;
        }

        String msg = exception.getClass().getName() + " occurred while processing "
                + context.getCurrentPhaseId().getName();

        String location = buildLocation(exception, component);
        if (location != null)
        {
            msg += " [Location=" + location + "]";
        }

        if (isLogStacktrace(context, exception))
        {
            logger.log(Level.SEVERE, msg, exception);
        }
        else
        {
            logger.log(Level.SEVERE, msg);
        }
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

        // unwrap to strip location aware stacktraces
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

