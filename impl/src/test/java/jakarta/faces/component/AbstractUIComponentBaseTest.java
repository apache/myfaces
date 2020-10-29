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
package jakarta.faces.component;

import jakarta.faces.component.UIComponentBase;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.ArrayList;

import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

import org.easymock.classextension.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;

/**
 * Created by IntelliJ IDEA.
* User: mathias
* Date: 18.03.2007
* Time: 01:22:02
* To change this template use File | Settings | File Templates.
*/
public abstract class AbstractUIComponentBaseTest
{
    protected UIComponentBase _testImpl;
    protected IMocksControl _mocksControl;
    protected FacesContext _facesContext;
    protected Renderer _renderer;

    @Before
    public void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _facesContext = _mocksControl.createMock(FacesContext.class);
        _testImpl = _mocksControl.createMock(UIComponentBase.class, getMockedMethodsArray());
        _renderer = _mocksControl.createMock(Renderer.class);
    }
    
    @After
    public void tearDown() throws Exception
    {
        _mocksControl = null;
        _facesContext = null;
        _testImpl = null;
        _renderer = null;
    }

    protected final Method[] getMockedMethodsArray() throws Exception
    {
        Collection<Method> mockedMethods = getMockedMethods();
        return mockedMethods.toArray(new Method[mockedMethods.size()]);
    }

    protected Collection<Method> getMockedMethods() throws Exception
    {
        Collection<Method> methods = new ArrayList<Method>();
        methods.add(UIComponentBase.class.getDeclaredMethod("getRenderer", new Class[]{FacesContext.class}));
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", (Class<?>[])null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getParent", (Class<?>[])null));

        return methods;
    }
}
