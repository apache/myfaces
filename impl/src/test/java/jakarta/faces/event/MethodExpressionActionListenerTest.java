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

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.easymock.classextension.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MethodExpressionActionListener}
 */
public class MethodExpressionActionListenerTest extends AbstractJsfTestCase 
{

    private MethodExpression methodExpressionOneArg;
    private MethodExpressionActionListener methodExpressionActionListener;
    private MethodExpression methodExpressionZeroArg;
    private UICommand uiComponent;
    private ActionEvent actionEvent;
    private Object[] paramsWithActionEvent;

    @Override
    @BeforeEach
    public void setUp() throws Exception 
    {
        super.setUp();
        uiComponent = new UICommand();
        actionEvent = new ActionEvent(uiComponent);
        paramsWithActionEvent = new Object[] {actionEvent};
        
        methodExpressionOneArg = EasyMock.createNiceMock(MethodExpression.class);
        methodExpressionOneArg.getExpressionString();
        EasyMock.expectLastCall().andReturn("#{aActionListener.processAction}").anyTimes();
        
        methodExpressionZeroArg = EasyMock.createNiceMock(MethodExpression.class);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception 
    {
        uiComponent = null;
        actionEvent = null;
        paramsWithActionEvent = null;
        methodExpressionOneArg = null;
        methodExpressionZeroArg = null;
        super.tearDown();
    }

    @Test
    public void testMethodExpressionActionListener() 
    {
        methodExpressionActionListener = new MethodExpressionActionListener();
    }

    @Test
    public void testMethodExpressionActionListenerMethodExpression() 
    {
        EasyMock.replay(methodExpressionOneArg);
        methodExpressionActionListener = new MethodExpressionActionListener(methodExpressionOneArg);
    }

    @Test
    public void testMethodExpressionActionListenerMethodExpressionMethodExpression() 
    {
        EasyMock.replay(methodExpressionOneArg);
        EasyMock.replay(methodExpressionZeroArg);
        methodExpressionActionListener 
                = new MethodExpressionActionListener(methodExpressionOneArg, methodExpressionZeroArg);
    }

    /**
     * Test for case: method with ActionEvent param exists (pre-Faces 2.0 case)
     */
    @Test
    public void testProcessAction() 
    {
        // First, try to invoke the MethodExpression passed to the constructor of this instance,
        // passing the argument ActionEvent as the argument:
        methodExpressionOneArg.invoke(EasyMock.eq(facesContext.getELContext()), EasyMock.aryEq(paramsWithActionEvent));
        EasyMock.expectLastCall().andReturn(null).times(1);
        EasyMock.replay(methodExpressionOneArg);
        
        methodExpressionActionListener = new MethodExpressionActionListener(methodExpressionOneArg);
        methodExpressionActionListener.processAction(actionEvent);
        
        EasyMock.verify(methodExpressionOneArg);
    }

    /**
     * Test for case: method exists but has no ActionEvent param (new possibility in Faces 2.0)
     */
    @Test
    public void testProcessAction2() throws Exception 
    {
        // First, try to invoke the MethodExpression passed to the constructor of this instance,
        // passing the argument ActionEvent as the argument
        methodExpressionOneArg.invoke(EasyMock.eq(facesContext.getELContext()), EasyMock.aryEq(paramsWithActionEvent));
        EasyMock.expectLastCall().andThrow(new MethodNotFoundException()).times(1);
        // If a MethodNotFoundException  is thrown, 
        // call to the zero argument MethodExpression derived from the MethodExpression passed
        // to the constructor of this instance
        methodExpressionZeroArg.invoke(EasyMock.eq(facesContext.getELContext()), EasyMock.aryEq(new Object[0]));
        EasyMock.expectLastCall().andReturn(null).times(1);
        
        EasyMock.replay(methodExpressionOneArg);
        EasyMock.replay(methodExpressionZeroArg);
        
        methodExpressionActionListener = new MethodExpressionActionListener(methodExpressionOneArg, methodExpressionZeroArg);
        methodExpressionActionListener.processAction(actionEvent);
        
        EasyMock.verify(methodExpressionOneArg);
        EasyMock.verify(methodExpressionZeroArg);
    }

    @Test
    public void testSaveState() 
    {
        methodExpressionActionListener = new MethodExpressionActionListener(methodExpressionOneArg, methodExpressionZeroArg);
        Object[] expectedState = new Object [] {methodExpressionOneArg, methodExpressionZeroArg};
        Assertions.assertTrue(Arrays.deepEquals(expectedState, (Object[]) methodExpressionActionListener.saveState(facesContext)),
                "Both MethodExpression instances described in the constructor must be saved.");
    }

    @Test
    public void testRestoreState() throws IllegalAccessException, NoSuchFieldException
    {
        // State saving always call JavaBean constructor:
        methodExpressionActionListener = new MethodExpressionActionListener();
        // Both MethodExpression instances described in the constructor must be restored.
        methodExpressionActionListener.restoreState(facesContext, 
                new Object[] {methodExpressionOneArg, methodExpressionZeroArg});
        
        // Test if the instance variables are set to the right values via reflection
        Field oneArgField = MethodExpressionActionListener.class.getDeclaredField("methodExpressionOneArg");
        oneArgField.setAccessible(true);
        Assertions.assertEquals(methodExpressionOneArg, oneArgField.get(methodExpressionActionListener));
        
        Field zeroArgField = MethodExpressionActionListener.class.getDeclaredField("methodExpressionZeroArg");
        zeroArgField.setAccessible(true);
        Assertions.assertEquals(methodExpressionZeroArg, zeroArgField.get(methodExpressionActionListener));
    }

}
