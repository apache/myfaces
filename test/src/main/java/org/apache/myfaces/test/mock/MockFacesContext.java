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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.lifecycle.Lifecycle;

/**
 * <p>Mock implementation of <code>FacesContext</code> that includes the semantics
 * added by JavaServer Faces 2.0.</p>
 * 
 * @author Leonardo Uribe
 * @since 1.0.0
 *
 */
public class MockFacesContext extends MockFacesContext20
{

    // ------------------------------------------------------------ Constructors

    public MockFacesContext()
    {
        super();
        setCurrentInstance(this);
    }

    public MockFacesContext(ExternalContext externalContext)
    {
        super(externalContext);
    }

    public MockFacesContext(ExternalContext externalContext,
            Lifecycle lifecycle)
    {
        super(externalContext, lifecycle);
    }

    // ------------------------------------------------------ Instance Variables

    private List<String> _resourceLibraryContracts;
    private Character _separatorChar;
    protected boolean _released = false;
    
    // ----------------------------------------------------- Mock Object Methods   

    @Override
    public List<String> getResourceLibraryContracts()
    {
        if (_resourceLibraryContracts == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return _resourceLibraryContracts;
        }
    }
    
    @Override
    public void setResourceLibraryContracts(List<String> contracts)
    {
        if (contracts == null)
        {
            _resourceLibraryContracts = null;
        }
        else if (contracts.isEmpty())
        {
            _resourceLibraryContracts = null;
        }
        else
        {
            _resourceLibraryContracts = new ArrayList<String>(contracts);
        }
    }
    
    @Override
    public char getNamingContainerSeparatorChar()
    {
        if (_separatorChar == null)
        {
            _separatorChar = UINamingContainer.getSeparatorChar(this);
        }
        return _separatorChar;
    }

    // ------------------------------------------------- ExternalContext Methods

    @Override
    public boolean isReleased()
    {
        return _released;
    }

    @Override
    public void release()
    {
        super.release();
        _released = true;
    }

    @Override
    public Lifecycle getLifecycle()
    {
        return null;
    }
}
