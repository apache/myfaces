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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import jakarta.el.MethodExpression;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.validator.Validator;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Test;

/**
 * {@link MethodExpressionActionListener}, {@link MethodExpressionValueChangeListener} and
 * {@link MethodExpressionValidator} leave no state in a component whose initial state is marked, and keep their full
 * state whenever the component saves its full state.
 */
class MethodExpressionPartialStateHolderTest extends AbstractFacesTestCase
{

    @Test
    void markedCommandSavesNoActionListenerState()
    {
        UICommand command = new UICommand();
        command.addActionListener(new MethodExpressionActionListener(mock(MethodExpression.class),
                mock(MethodExpression.class)));
        command.markInitialState();

        assertNull(command.saveState(facesContext));
    }

    @Test
    void markedInputSavesNoValueChangeListenerNorValidatorState()
    {
        UIInput input = new UIInput();
        input.addValueChangeListener(new MethodExpressionValueChangeListener(mock(MethodExpression.class),
                mock(MethodExpression.class)));
        input.addValidator(new MethodExpressionValidator(mock(MethodExpression.class)));
        input.markInitialState();

        assertNull(input.saveState(facesContext));
    }

    @Test
    void actionListenerAddedAfterMarkingIsRestoredFromFullState()
    {
        MethodExpression oneArg = mock(MethodExpression.class);
        MethodExpression zeroArg = mock(MethodExpression.class);
        UICommand command = new UICommand();
        command.markInitialState();
        command.addActionListener(new MethodExpressionActionListener(oneArg, zeroArg));
        Object state = command.saveState(facesContext);
        assertNotNull(state);

        UICommand restored = new UICommand();
        restored.restoreState(facesContext, state);

        ActionListener[] listeners = restored.getActionListeners();
        assertEquals(1, listeners.length);
        MethodExpressionActionListener listener = assertInstanceOf(MethodExpressionActionListener.class,
                listeners[0]);
        assertArrayEquals(new Object[] { oneArg, zeroArg }, (Object[]) listener.saveState(facesContext));
    }

    @Test
    void valueChangeListenerAddedAfterMarkingIsRestoredFromFullState()
    {
        MethodExpression oneArg = mock(MethodExpression.class);
        MethodExpression zeroArg = mock(MethodExpression.class);
        UIInput input = new UIInput();
        input.markInitialState();
        input.addValueChangeListener(new MethodExpressionValueChangeListener(oneArg, zeroArg));
        Object state = input.saveState(facesContext);
        assertNotNull(state);

        UIInput restored = new UIInput();
        restored.restoreState(facesContext, state);

        ValueChangeListener[] listeners = restored.getValueChangeListeners();
        assertEquals(1, listeners.length);
        MethodExpressionValueChangeListener listener = assertInstanceOf(
                MethodExpressionValueChangeListener.class, listeners[0]);
        assertArrayEquals(new Object[] { oneArg, zeroArg }, (Object[]) listener.saveState(facesContext));
    }

    @Test
    void transientActionListenerIsNotRestored()
    {
        UICommand command = new UICommand();
        MethodExpressionActionListener listener = new MethodExpressionActionListener(mock(MethodExpression.class),
                mock(MethodExpression.class));
        listener.setTransient(true);
        command.addActionListener(listener);
        Object state = command.saveState(facesContext);

        UICommand restored = new UICommand();
        restored.restoreState(facesContext, state);

        assertEquals(0, restored.getActionListeners().length);
    }

    @Test
    void clearedListenerRoundTripsThroughAttachedState()
    {
        MethodExpression oneArg = mock(MethodExpression.class);
        MethodExpression zeroArg = mock(MethodExpression.class);
        MethodExpressionActionListener listener = new MethodExpressionActionListener(oneArg, zeroArg);
        listener.markInitialState();
        listener.clearInitialState();

        Object restored = UIComponentBase.restoreAttachedState(facesContext,
                UIComponentBase.saveAttachedState(facesContext, listener));

        MethodExpressionActionListener restoredListener = assertInstanceOf(
                MethodExpressionActionListener.class, restored);
        assertArrayEquals(new Object[] { oneArg, zeroArg },
                (Object[]) restoredListener.saveState(facesContext));
    }

    @Test
    void markedValidatorKeepsItsExpressionWhenSiblingStateIsRestored()
    {
        MethodExpression expression = mock(MethodExpression.class);
        Object state = buildMarkedInput(expression, "changed").saveState(facesContext);
        assertNotNull(state);

        UIInput rebuilt = buildMarkedInput(expression, "built");
        rebuilt.restoreState(facesContext, state);

        Validator<?>[] validators = rebuilt.getValidators();
        MethodExpressionValidator validator = assertInstanceOf(MethodExpressionValidator.class,
                validators[0]);
        validator.clearInitialState();
        assertSame(expression, validator.saveState(facesContext));
        assertEquals("changed", ((StatefulValidator) validators[1]).config);
    }

    private static UIInput buildMarkedInput(MethodExpression expression, String siblingConfig)
    {
        UIInput input = new UIInput();
        input.addValidator(new MethodExpressionValidator(expression));
        StatefulValidator sibling = new StatefulValidator();
        input.addValidator(sibling);
        input.markInitialState();
        sibling.config = siblingConfig;
        return input;
    }

    /**
     * A validator implementing only {@link StateHolder}, which therefore always saves its state.
     */
    public static class StatefulValidator implements Validator<Object>, StateHolder
    {
        String config = "built";

        @Override
        public void validate(FacesContext context, UIComponent component, Object value)
        {
        }

        @Override
        public Object saveState(FacesContext context)
        {
            return config;
        }

        @Override
        public void restoreState(FacesContext context, Object state)
        {
            config = (String) state;
        }

        @Override
        public boolean isTransient()
        {
            return false;
        }

        @Override
        public void setTransient(boolean newTransientValue)
        {
        }
    }
}
