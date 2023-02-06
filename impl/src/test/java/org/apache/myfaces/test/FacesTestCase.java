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
package org.apache.myfaces.test;

import jakarta.el.ELContext;
import jakarta.faces.application.Application;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import junit.framework.TestCase;

import org.apache.myfaces.test.mock.MockFacesContext12;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class FacesTestCase
{
    protected FacesContext _facesContext;
    protected ExternalContext _externalContext;
    protected Application _application;
    protected ELContext _elContext;

    @Before
    public void setUp() throws Exception
    {
        _externalContext = Mockito.mock(ExternalContext.class);
        _facesContext = Mockito.mock(FacesContext.class);
        MockFacesContext12.setCurrentInstance(_facesContext);
        _application = Mockito.mock(Application.class);
        _elContext = Mockito.mock(ELContext.class);        
    }
    
    @After
    public void tearDown() throws Exception
    {
        MockFacesContext12.setCurrentInstance(null);
    }
}
