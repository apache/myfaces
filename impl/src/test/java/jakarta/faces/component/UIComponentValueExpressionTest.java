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
import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;

import org.easymock.EasyMock;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link UIComponent#getValueExpression(String)}. and
 * {@link UIComponent#setValueExpression(String, jakarta.el.ValueExpression)}.
 */
class UIComponentValueExpressionTest extends UIComponentTestBase
{
    private UIComponent _testimpl;
    private ValueExpression _expression;
    private ELContext _elContext;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        Collection<Method> mockedMethods = new ArrayList<Method>();
        Class<UIComponent> clazz = UIComponent.class;
        mockedMethods.add(clazz.getDeclaredMethod("getAttributes", (Class<?>[])null));
        mockedMethods.add(clazz.getDeclaredMethod("getFacesContext", (Class<?>[])null));

        _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
        _expression = _mocksControl.createMock(ValueExpression.class);
        _elContext = _mocksControl.createMock(ELContext.class);
        _mocksControl.checkOrder(true);
    }

    @Test
    void valueExpressionArgumentNPE() throws Exception
    {
        assertThrows(NullPointerException.class, () ->
            _testimpl.setValueExpression(null, _expression));
    }

    @Test
    void valueExpressionArgumentId() throws Exception
    {
        assertThrows(IllegalArgumentException.class, () ->
            _testimpl.setValueExpression("id", _expression));
    }

    @Test
    void valueExpressionArgumentsParent() throws Exception
    {
        assertThrows(IllegalArgumentException.class, () ->
            _testimpl.setValueExpression("parent", _expression));
    }

    @Test
    void valueExpression() throws Exception
    {
        expect(_expression.isLiteralText()).andReturn(false);
        _mocksControl.replay();
        _testimpl.setValueExpression("xxx", _expression);
        _mocksControl.verify();
        assertEquals(_expression, _testimpl.getValueExpression("xxx"));
        _testimpl.setValueExpression("xxx", null);
        _mocksControl.verify();

        assertNull(_testimpl.getValueExpression("xxx"));
    }

    @Test
    void valueExpressionWithExceptionOnGetValue() throws Exception
    {
        expect(_expression.isLiteralText()).andReturn(true);
        expect(_testimpl.getFacesContext()).andReturn(_facesContext);
        expect(_facesContext.getELContext()).andReturn(_elContext);
        expect(_expression.getValue(EasyMock.eq(_elContext))).andThrow(new ELException());
        Map<String, Object> map = new HashMap<String, Object>();
        expect(_testimpl.getAttributes()).andReturn(map);
        _mocksControl.replay();
        assertThrows(FacesException.class, () ->
            _testimpl.setValueExpression("xxx", _expression));
    }

    @Test
    void valueExpressionWithLiteralText() throws Exception
    {
        expect(_expression.isLiteralText()).andReturn(true);
        expect(_testimpl.getFacesContext()).andReturn(_facesContext);
        expect(_facesContext.getELContext()).andReturn(_elContext);
        expect(_expression.getValue(EasyMock.eq(_elContext))).andReturn("abc");
        Map<String, Object> map = new HashMap<String, Object>();
        expect(_testimpl.getAttributes()).andReturn(map);
        _mocksControl.replay();
        _testimpl.setValueExpression("xxx", _expression);
        assertEquals("abc", map.get("xxx"));
        _mocksControl.verify();
        assertNull(_testimpl.getValueExpression("xxx"));
    }

}
