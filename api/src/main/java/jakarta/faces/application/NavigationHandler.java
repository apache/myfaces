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
package jakarta.faces.application;

import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
public abstract class NavigationHandler
{
    public abstract void handleNavigation(FacesContext context,
                                          String fromAction,
                                          String outcome);
    
    /**
     * @since 2.2
     * @param context
     * @param fromAction
     * @param outcome
     * @param toFlowDocumentId 
     */
    public void handleNavigation(FacesContext context,
                                 String fromAction,
                                 String outcome,
                                 String toFlowDocumentId)
    {
        this.handleNavigation(context, fromAction, outcome);
    }

    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome)
    {
        return null;
    }

    public Map<String, Set<NavigationCase>> getNavigationCases()
    {
        return Collections.emptyMap();
    }

    public void performNavigation(String outcome)
    {
        handleNavigation(FacesContext.getCurrentInstance(), null, outcome);
    }

    /**
     * @since 2.2
     * @param context
     * @param flow
     */
    public void inspectFlow(FacesContext context, Flow flow)
    {
    }

    /**
     * @since 2.2
     * @param context
     * @param fromAction
     * @param outcome
     * @param toFlowDocumentId
     * @return
     */
    public NavigationCase getNavigationCase(FacesContext context,
                                            java.lang.String fromAction,
                                            java.lang.String outcome,
                                            java.lang.String toFlowDocumentId)
    {
        return getNavigationCase(context, fromAction, outcome);
    }
}
