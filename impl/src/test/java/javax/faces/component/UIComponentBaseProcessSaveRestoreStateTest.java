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
package javax.faces.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:14:31
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentBaseProcessSaveRestoreStateTest extends AbstractUIComponentBaseTest
{
    private static final String CHILD_STATE = "childState";
    private static final String TESTIMPL_STATE = "testimplState";
    private static final String FACET_STATE = "facetState";
    private UIComponent _facet;
    private UIComponent _child;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _facet = _mocksControl.createMock(UIComponent.class);
        _child = _mocksControl.createMock(UIComponent.class);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        _facet = null;
        _child = null;
    }

    @Override
    protected Collection<Method> getMockedMethods() throws Exception
    {
        Collection<Method> methods = super.getMockedMethods();
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacets", (Class<?>[])null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getChildren", (Class<?>[])null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacetCount", (Class<?>[])null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getChildCount", (Class<?>[])null));
        methods.add(UIComponentBase.class.getDeclaredMethod("saveState", new Class[]{FacesContext.class}));
        methods.add(UIComponentBase.class.getDeclaredMethod("restoreState", new Class[]{FacesContext.class,
                Object.class}));
        return methods;
    }

    @Test(expected = NullPointerException.class)
    public void testSaveStateExpections() throws Exception
    {
        _testImpl.processSaveState(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRestoreStateExpections() throws Exception
    {
        _testImpl.processRestoreState(null, null);
    }

// FIXME: Need to add some expectation for FacesContext.getAttributes. I'll have to read a bit more about 
//        easy mock to fix the test error.
//    @Test
//    public void testSaveRestoreStateWithTransientChilds() throws Exception
//    {
//        _testImpl.setTransient(true);
//        Assert.assertNull(_testImpl.processSaveState(_facesContext));
//
//        _testImpl.setTransient(false);
//        setUpChilds(true, true, true);
//        _mocksControl.replay();
//        Object state = _testImpl.processSaveState(_facesContext);
//        Assert.assertNotNull(state);
//        _mocksControl.verify();
//
//        _mocksControl.reset();
//        _testImpl.restoreState(EasyMock.same(_facesContext), EasyMock.eq(TESTIMPL_STATE));
//        _mocksControl.replay();
//        _testImpl.processRestoreState(_facesContext, state);
//        _mocksControl.verify();
//    }
//
//    @Test
//    public void testSaveRestoreState() throws Exception
//    {
//        _testImpl.setTransient(true);
//        Assert.assertNull(_testImpl.processSaveState(_facesContext));
//
//        _testImpl.setTransient(false);
//        setUpChilds(true, false, false);
//        _mocksControl.replay();
//        Object state = _testImpl.processSaveState(_facesContext);
//        Assert.assertNotNull(state);
//        _mocksControl.verify();
//
//        _mocksControl.reset();
//        _facesContext.getAttributes();
//        setUpChilds(false, false, false);
//        _mocksControl.replay();
//        _testImpl.processRestoreState(_facesContext, state);
//        _mocksControl.verify();
//    }

    private void setUpChilds(boolean saveState, boolean facetTransient, boolean childTransient)
    {
        if (saveState || !facetTransient)
        {
            Map<String, UIComponent> facetMap = new HashMap<String, UIComponent>();
            facetMap.put("testFacet", _facet);
            expect(_testImpl.getFacetCount()).andReturn(1).anyTimes();
            expect(_testImpl.getFacets()).andReturn(facetMap).anyTimes();
            expect(_facet.isTransient()).andReturn(facetTransient).anyTimes();
        }
        if (!facetTransient)
        {
            if (saveState)
            {
                expect(_facet.processSaveState(EasyMock.same(_facesContext))).andReturn(FACET_STATE);
            }
            else
            {
                _facet.processRestoreState(EasyMock.same(_facesContext), EasyMock.eq(FACET_STATE));
            }
        }
        if (saveState || !childTransient)
        {
            List<UIComponent> childs = new ArrayList<UIComponent>();
            childs.add(_child);
            expect(_testImpl.getChildCount()).andReturn(1).anyTimes();
            expect(_testImpl.getChildren()).andReturn(childs).anyTimes();
            expect(_child.isTransient()).andReturn(childTransient).anyTimes();
        }
        if (!childTransient)
        {
            if (saveState)
            {
                expect(_child.processSaveState(EasyMock.same(_facesContext))).andReturn(CHILD_STATE);
            }
            else
            {
                _child.processRestoreState(EasyMock.same(_facesContext), EasyMock.eq(CHILD_STATE));
            }
        }
        if (saveState)
        {
            expect(_testImpl.saveState(EasyMock.same(_facesContext))).andReturn(TESTIMPL_STATE);
        }
        else
        {
            _testImpl.restoreState(EasyMock.same(_facesContext), EasyMock.eq(TESTIMPL_STATE));
        }
    }
}
