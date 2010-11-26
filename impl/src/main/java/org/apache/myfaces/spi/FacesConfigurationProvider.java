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
package org.apache.myfaces.spi;

import javax.faces.context.ExternalContext;

import org.apache.myfaces.config.element.FacesConfigData;

/**
 * This interface provide a way to merge and store all JSF config information retrieved
 * from faces-config files, META-INF/service files and annotations that works as base
 * point to initialize MyFaces. The objective is allow server containers to "store" or
 * this information, preventing calculate it over and over each time the web application
 * is started.
 * 
 * @author Leonardo Uribe
 * @since 2.0.3
 *
 */
public abstract class FacesConfigurationProvider
{
    
    /**
     * Returns an object that collect all config information used by MyFaces
     * to initialize the web application.
     * 
     * @param ectx
     * @return
     */
    public abstract FacesConfigData getFacesConfigData(ExternalContext ectx);
    
}
