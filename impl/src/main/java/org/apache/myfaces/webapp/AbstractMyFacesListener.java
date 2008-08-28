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

import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.annotation.LifecycleProviderFactory;

/**
 * @author Dennis Byrne
 */

public abstract class AbstractMyFacesListener {
    private static Log log = LogFactory.getLog(AbstractMyFacesListener.class);

    protected void doPreDestroy(ServletRequestAttributeEvent event, String scope) {
        doPreDestroy(event.getValue(), event.getName(), scope);
    }

    protected void doPreDestroy(HttpSessionBindingEvent event, String scope) {
        doPreDestroy(event.getValue(), event.getName(), scope);
    }

    protected void doPreDestroy(ServletContextAttributeEvent event, String scope) {
        doPreDestroy(event.getValue(), event.getName(), scope);
    }
    
    protected void doPreDestroy(Object value, String name, String scope) {
        
        if(value != null)
        {
            //AnnotatedManagedBeanHandler handler =
            //    new AnnotatedManagedBeanHandler(value, scope, name);

            //handler.invokePreDestroy();

            try
            {
                LifecycleProviderFactory.getLifecycleProviderFactory().getLifecycleProvider(null).destroyInstance(value);
            } catch (IllegalAccessException e)
            {
                log.error("", e);
            } catch (InvocationTargetException e)
            {
                log.error("", e);
            }
        }
    }
}
