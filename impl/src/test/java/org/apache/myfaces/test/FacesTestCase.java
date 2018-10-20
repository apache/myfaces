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

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.apache.myfaces.test.mock.MockFacesContext12;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class FacesTestCase extends TestCase
{
    protected FacesContext _facesContext;
    protected IMocksControl _mocksControl;
    protected ExternalContext _externalContext;
    protected Application _application;
    protected ELContext _elContext;

    protected void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _externalContext = _mocksControl.createMock(ExternalContext.class);
        _facesContext = _mocksControl.createMock(FacesContext.class);
        MockFacesContext12.setCurrentInstance(_facesContext);
        _application = _mocksControl.createMock(Application.class);
        _elContext = _mocksControl.createMock(ELContext.class);        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        MockFacesContext12.setCurrentInstance(null);
    }
}
