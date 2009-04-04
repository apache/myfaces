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
package javax.faces.application;

import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 13:54:45 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public abstract class ConfigurableNavigationHandler extends NavigationHandler
{

    /**
     * 
     */
    public ConfigurableNavigationHandler()
    {
    }

    public abstract NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome);

    public abstract Map<String,Set<NavigationCase>> getNavigationCases();

    public void performNavigation(String outcome)
    {
        handleNavigation(FacesContext.getCurrentInstance(), null, outcome);
    }
}
