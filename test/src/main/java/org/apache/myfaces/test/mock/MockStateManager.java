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

import java.io.IOException;

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * <p>Mock implementation of <code>StateManager</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */
public class MockStateManager extends StateManager
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockStateManager()
    {
    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- StateManager Methods

    /** {@inheritDoc} */
    public SerializedView saveSerializedView(FacesContext context)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public Object getTreeStructureToSave(FacesContext context)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public Object getComponentStateToSave(FacesContext context)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public void writeState(FacesContext context, SerializedView view)
            throws IOException
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public UIViewRoot restoreView(FacesContext context, String viewId,
            String renderKitId)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public UIViewRoot restoreTreeStructure(FacesContext context, String viewId,
            String renderKitId)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public void restoreComponentState(FacesContext context, UIViewRoot view,
            String renderKitId)
    {

        throw new UnsupportedOperationException();

    }

}
