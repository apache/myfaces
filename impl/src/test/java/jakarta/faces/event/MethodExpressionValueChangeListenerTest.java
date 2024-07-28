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
package jakarta.faces.event;

import java.lang.reflect.Field;
import java.util.Arrays;

import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.faces.component.UICommand;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.easymock.classextension.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MethodExpressionValueChangeListener}
 */
public class MethodExpressionValueChangeListenerTest extends AbstractFacesTestCase
{

    private MethodExpression methodExpressionOneArg;
    private MethodExpression methodExpressionZeroArg;
    private MethodExpressionValueChangeListener methodExpressionValueChangeListener;
    private UICommand uiComponent;
    private ValueChangeEvent valueChangeEvent;
    private Object[] paramsWithValueChangeEvent;

    @Override
    @BeforeEach
    public void setUp() throws Exception 
    {
        super.setUp();
        uiComponent = new UICommand();
        valueChangeEvent = new ValueChangeEvent(uiComponent, "1", "2");
        paramsWithValueChangeEvent = new Object[] {valueChangeEvent};
        
        methodExpressionOneArg = EasyMock.createNiceMock(MethodExpression.class);
        methodExpressionOneArg.getExpressionString();
        EasyMock.expectLastCall().andReturn("#{aValueChangeListener.processValueChange}").anyTimes();
        
        methodExpressionZeroArg = EasyMock.createNiceMock(MethodExpression.class);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception 
    {
        uiComponent = null;
        valueChangeEvent = null;
        paramsWithValueChangeEvent = null;
        methodExpressionOneArg = null;
        methodExpressionZeroArg = null;
        super.tearDown();
    }

    @Test
    public void testMethodExpressionValueChangeListener() 
    {
        methodExpressionValueChangeListener = new MethodExpressionValueChangeListener();
    }

    @Test
    public void testMethodExpressionValueChangeListenerMethodExpression() 
    {
        EasyMock.replay(methodExpressionOneArg);
        methodExpressionValueChangeListener = new MethodExpressionValueChangeListener(methodExpressionOneArg);
    }

    @Test
    public void testMethodExpressionValueChangeListenerMethodExpressionMethodExpression() 
    {
        EasyMock.replay(methodExpressionOneArg);
        EasyMock.replay(methodExpressionZeroArg);
        methodExpressionValueChangeListener 
                = new MethodExpressionValueChangeListener(methodExpressionOneArg, methodExpressionZeroArg);
    }

    /**
     * Test for case: method with ValueChangeEvent param exists (pre-Faces 2.0 case)
     */
    @Test
    public void testProcessValueChange() 
    {
        // First, try to invoke the MethodExpression passed to the constructor of this instance,
        // passing the argument ValueChangeEvent as the argument:
        methodExpressionOneArg.invoke(EasyMock.eq(facesContext.getELContext()), EasyMock.aryEq(paramsWithValueChangeEvent));
        EasyMock.expectLastCall().andReturn(null).times(1);
        EasyMock.replay(methodExpressionOneArg);
        
        methodExpressionValueChangeListener = new MethodExpressionValueChangeListener(methodExpressionOneArg);
        methodExpressionValueChangeListener.processValueChange(valueChangeEvent);
        
        EasyMock.verify(methodExpressionOneArg);
    }

    /**
     * Test for case: method exists but has no ValueChangeEvent param (new possibility in Faces 2.0)
     */
    @Test
    public void testProcessValueChange2() throws Exception 
    {
        // First, try to invoke the MethodExpression passed to the constructor of this instance,
        // passing the argument ValueChangeEvent as the argument
        methodExpressionOneArg.invoke(EasyMock.eq(facesContext.getELContext()), EasyMock.aryEq(paramsWithValueChangeEvent));
        EasyMock.expectLastCall().andThrow(new MethodNotFoundException()).times(1);
        // If a MethodNotFoundException  is thrown, 
        // call to the zero argument MethodExpression derived from the MethodExpression passed
        // to the constructor of this instance
        methodExpressionZeroArg.invoke(EasyMock.eq(facesContext.getELContext()), EasyMock.aryEq(new Object[0]));
        EasyMock.expectLastCall().andReturn(null).times(1);
        
        EasyMock.replay(methodExpressionOneArg);
        EasyMock.replay(methodExpressionZeroArg);
        
        methodExpressionValueChangeListener = new MethodExpressionValueChangeListener(methodExpressionOneArg, methodExpressionZeroArg);
        methodExpressionValueChangeListener.processValueChange(valueChangeEvent);
        
        EasyMock.verify(methodExpressionOneArg);
        EasyMock.verify(methodExpressionZeroArg);
    }

    @Test
    public void testSaveState() 
    {
        methodExpressionValueChangeListener = new MethodExpressionValueChangeListener(methodExpressionOneArg, methodExpressionZeroArg);
        Object[] expectedState = new Object [] {methodExpressionOneArg, methodExpressionZeroArg};
        Assertions.assertTrue(Arrays.deepEquals(expectedState, (Object[]) methodExpressionValueChangeListener.saveState(facesContext)), 
                "Both MethodExpression instances described in the constructor must be saved.");
    }

    @Test
    public void testRestoreState() throws IllegalAccessException, NoSuchFieldException
    {
        // State saving always call JavaBean constructor:
        methodExpressionValueChangeListener = new MethodExpressionValueChangeListener();
        // Both MethodExpression instances described in the constructor must be restored.
        methodExpressionValueChangeListener.restoreState(facesContext, 
                new Object[] {methodExpressionOneArg, methodExpressionZeroArg});
        
        // Test if the instance variables are set to the right values via reflection
        Field oneArgField = MethodExpressionValueChangeListener.class.getDeclaredField("methodExpressionOneArg");
        oneArgField.setAccessible(true);
        Assertions.assertEquals(methodExpressionOneArg, oneArgField.get(methodExpressionValueChangeListener));
        
        Field zeroArgField = MethodExpressionValueChangeListener.class.getDeclaredField("methodExpressionZeroArg");
        zeroArgField.setAccessible(true);
        Assertions.assertEquals(methodExpressionZeroArg, zeroArgField.get(methodExpressionValueChangeListener));
    }

}
