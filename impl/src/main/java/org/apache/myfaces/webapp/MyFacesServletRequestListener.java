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

import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @author Dennis Byrne
 */

public class MyFacesServletRequestListener extends AbstractMyFacesListener implements ServletRequestListener {

    public void requestDestroyed(ServletRequestEvent event) {

        ServletRequest request = event.getServletRequest();
        Enumeration<String> attributes = request.getAttributeNames();
        
        while(attributes.hasMoreElements()) 
        {
            String name = attributes.nextElement();
            Object attribute = request.getAttribute(name);
            doPreDestroy(attribute, name, ManagedBeanBuilder.REQUEST);
        }
        
    }
        
    public void requestInitialized(ServletRequestEvent event) { // noop
    }

}
