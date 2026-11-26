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
package jakarta.faces.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import jakarta.el.MethodExpression;
import jakarta.faces.component.UIInput;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.easymock.classextension.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MethodExpressionValidator}
 */
class MethodExpressionValidatorTest extends AbstractFacesTestCase
{

    private MethodExpression methodExpression;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        methodExpression = EasyMock.createNiceMock(MethodExpression.class);
        EasyMock.replay(methodExpression);
    }

    @Test
    void saveAndRestoreState() throws Exception
    {
        MethodExpressionValidator validator = new MethodExpressionValidator(methodExpression);
        Object state = validator.saveState(facesContext);
        assertNotNull(state);

        MethodExpressionValidator restored = new MethodExpressionValidator();
        restored.restoreState(facesContext, state);
        assertSame(methodExpression, getMethodExpression(restored));
    }

    @Test
    void saveStateWhileInitialStateMarked()
    {
        MethodExpressionValidator validator = new MethodExpressionValidator(methodExpression);
        validator.markInitialState();
        assertTrue(validator.initialStateMarked());
        assertNull(validator.saveState(facesContext),
                "No state must be saved while the initial state is marked.");

        validator.clearInitialState();
        assertFalse(validator.initialStateMarked());
        assertSame(methodExpression, validator.saveState(facesContext),
                "The MethodExpression must be saved after clearInitialState().");
    }

    @Test
    void restoreNullState() throws Exception
    {
        MethodExpressionValidator validator = new MethodExpressionValidator(methodExpression);
        validator.markInitialState();
        validator.restoreState(facesContext, null);
        assertSame(methodExpression, getMethodExpression(validator));
    }

    /**
     * The view state of an unchanged component holds no state for its MethodExpressionValidator.
     */
    @Test
    void unchangedComponentHasNoState()
    {
        UIInput input = new UIInput();
        input.addValidator(new MethodExpressionValidator(methodExpression));
        input.markInitialState();

        assertNull(input.saveState(facesContext));
    }

    /**
     * Another validator of the same component changes after the build. The delta of the validator list then
     * holds a null state for the MethodExpressionValidator, which must keep the MethodExpression of the build.
     */
    @Test
    void deltaRestoreKeepsMethodExpression() throws Exception
    {
        UIInput input = new UIInput();
        LengthValidator lengthValidator = new LengthValidator();
        input.addValidator(lengthValidator);
        input.addValidator(new MethodExpressionValidator(methodExpression));
        input.markInitialState();
        lengthValidator.setMaximum(5);

        Object state = input.saveState(facesContext);
        assertNotNull(state);

        // rebuild the view
        UIInput rebuilt = new UIInput();
        rebuilt.addValidator(new LengthValidator());
        rebuilt.addValidator(new MethodExpressionValidator(methodExpression));
        rebuilt.markInitialState();
        rebuilt.restoreState(facesContext, state);

        Validator<?>[] validators = rebuilt.getValidators();
        assertEquals(2, validators.length);
        assertEquals(5, ((LengthValidator) validators[0]).getMaximum());
        assertSame(methodExpression, getMethodExpression((MethodExpressionValidator) validators[1]));
    }

    private static MethodExpression getMethodExpression(MethodExpressionValidator validator) throws Exception
    {
        Field field = MethodExpressionValidator.class.getDeclaredField("methodExpression");
        field.setAccessible(true);
        return (MethodExpression) field.get(validator);
    }
}
