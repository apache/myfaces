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

package org.apache.myfaces.cdi.faces;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.faces.annotation.ApplicationMap;
import javax.faces.annotation.InitParameterMap;
import javax.faces.application.Application;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;

/**
 *
 */
public class ApplicationScopeObjectProducer
{
    
   @Produces
   @ApplicationScoped
   public Application getApplication()
   {
      return FacesContext.getCurrentInstance().getApplication();
   }
   
   @Produces
   @ApplicationMap
   @ApplicationScoped
   public Map<String, Object> getApplicationMap()
   {
       return FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
   }

   @Produces
   @InitParameterMap
   @ApplicationScoped
   public Map<String, Object> getInitParameterMap()
   {
       return FacesContext.getCurrentInstance().getExternalContext().getInitParameterMap();
   }
   
   @Produces
   @ApplicationScoped
   public ResourceHandler getResourceHandler()
   {
      return FacesContext.getCurrentInstance().getApplication().getResourceHandler();
   }
}
