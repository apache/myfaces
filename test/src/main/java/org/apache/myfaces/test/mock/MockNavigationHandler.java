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

package org.apache.myfaces.test.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * <p>Mock implementation of <code>NavigationHandler</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockNavigationHandler extends NavigationHandler
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockNavigationHandler()
    {
    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Add a outcome-viewId pair to the destinations map.</p>
     *
     * @param outcome Logical outcome string
     * @param viewId Destination view identifier
     */
    public void addDestination(String outcome, String viewId)
    {

        destinations.put(outcome, viewId);

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>Set of destination view ids, keyed by logical outcome String
     * that will cause navigation to that view id.</p>
     */
    private Map destinations = new HashMap();

    // ----------------------------------------------- NavigationHandler Methods

    /**
     * <p>Process the specified navigation request.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param action Action method being executed
     * @param outcome Logical outcome from this action method
     */
    public void handleNavigation(FacesContext context, String action,
            String outcome)
    {

        // Navigate solely based on outcome, if we get a match
        String viewId = (String) destinations.get(outcome);
        if (viewId != null)
        {
            UIViewRoot view = getViewHandler(context).createView(context,
                    viewId);
            context.setViewRoot(view);
        }

    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome)
    {
        return null;
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases()
    {
        return Map.of();
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>Return the <code>ViewHandler</code> instance for this application.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    private ViewHandler getViewHandler(FacesContext context)
    {

        return context.getApplication().getViewHandler();

    }

}
