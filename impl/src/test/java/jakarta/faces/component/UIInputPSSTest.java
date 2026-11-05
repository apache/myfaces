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

import jakarta.el.ValueExpression;
import jakarta.faces.event.PhaseId;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.el.MockValueExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The partial state of a {@link UIInput}: the {@code valid} and {@code localValueSet} flags qualify the local value,
 * so they are saved together with it, while the submitted value is per-request state and is not saved.
 * <p>
 * The state is saved during Render Response, as it is for the view state, because outside that phase
 * {@link UIComponentBase#saveState} also saves the transient state.
 */
public class UIInputPSSTest extends AbstractFacesTestCase
{

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
    }

    /**
     * A local value saved with the view must still be pushed to the model and cleared by {@link UIInput#updateModel}
     * of a later request, so that the model value is rendered afterwards.
     */
    @Test
    public void testLocalValueSetIsSavedWithTheLocalValue()
    {
        UIInput input = createInput();
        input.setValue("stale");

        UIInput restored = restore(input.saveState(facesContext));
        Assertions.assertTrue(restored.isLocalValueSet());

        restored.updateModel(facesContext);
        Assertions.assertFalse(restored.isLocalValueSet());
        Assertions.assertEquals("stale", facesContext.getExternalContext().getRequestMap().get("value"));
        Assertions.assertEquals("stale", restored.getValue());
        Assertions.assertNull(restored.saveState(facesContext));
    }

    @Test
    public void testInvalidIsSaved()
    {
        UIInput input = createInput();
        input.setValid(false);

        UIInput restored = restore(input.saveState(facesContext));
        Assertions.assertFalse(restored.isValid());
    }

    @Test
    public void testFlagsBackAtTheirDefaultsAddNothingToTheSavedState()
    {
        UIInput input = createInput();
        input.setValid(false);
        input.setValid(true);
        input.setLocalValueSet(true);
        input.setLocalValueSet(false);

        Assertions.assertNull(input.saveState(facesContext));
    }

    @Test
    public void testSubmittedValueIsNotSaved()
    {
        UIInput input = createInput();
        input.setSubmittedValue("submitted");

        UIInput restored = restore(input.saveState(facesContext));
        Assertions.assertNull(restored.getSubmittedValue());
    }

    private UIInput createInput()
    {
        UIInput input = new UIInput();
        input.setId("input");
        ValueExpression expression = new MockValueExpression("#{requestScope.value}", String.class);
        input.setValueExpression("value", expression);
        input.markInitialState();
        return input;
    }

    private UIInput restore(Object state)
    {
        UIInput restored = createInput();
        restored.restoreState(facesContext, state);
        return restored;
    }
}
