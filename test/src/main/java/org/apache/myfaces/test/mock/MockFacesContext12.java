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

import jakarta.el.ELContext;
import jakarta.el.ELContextEvent;
import jakarta.el.ELContextListener;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.Lifecycle;

import org.apache.myfaces.test.el.MockELContext;

/**
 * <p>Mock implementation of <code>FacesContext</code> that includes the semantics
 * added by JavaServer Faces 1.2.</p>
 *
 * $Id$
 *
 * @since 1.0.0
 */
public abstract class MockFacesContext12 extends MockFacesContext10
{

    // ------------------------------------------------------------ Constructors

    public MockFacesContext12()
    {
        super();
        setCurrentInstance(this);
    }

    public MockFacesContext12(ExternalContext externalContext)
    {
        super(externalContext);
    }

    public MockFacesContext12(ExternalContext externalContext,
            Lifecycle lifecycle)
    {
        super(externalContext, lifecycle);
    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Set the <code>ELContext</code> instance for this instance.</p>
     *
     * @param elContext The new ELContext
     */
    public void setELContext(ELContext elContext)
    {

        this.elContext = elContext;

    }

    // ------------------------------------------------------ Instance Variables

    private ELContext elContext = null;

    // ---------------------------------------------------- FacesContext Methods

    /** {@inheritDoc} */
    public ELContext getELContext()
    {

        if (this.elContext == null)
        {

            // Initialize a new ELContext
            this.elContext = new MockELContext();
            this.elContext.putContext(FacesContext.class, this);

            // Notify interested listeners that this ELContext was created
            ELContextListener[] listeners = getApplication()
                    .getELContextListeners();
            if ((listeners != null) && (listeners.length > 0))
            {
                ELContextEvent event = new ELContextEvent(this.elContext);
                for (int i = 0; i < listeners.length; i++)
                {
                    listeners[i].contextCreated(event);
                }
            }

        }
        return this.elContext;

    }

    /** {@inheritDoc} */
    public void release()
    {
        super.release();
        this.elContext = null;
    }

}
