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

import jakarta.faces.event.PhaseId;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4777: restoring the full state of a component must reset the component to that state, also when nothing
 * was in its state helper when the full state was saved. This is what UIData and UIRepeat with
 * {@code rowStatePreserved="true"} do when they enter a row without saved state of its own.
 */
public class UIComponentBaseRestoreEmptyFullStateTest extends AbstractFacesTestCase
{
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
    }

    @Test
    public void testFullStateWithEmptyStateHelperClearsLocalValue()
    {
        UIInput input = new UIInput();
        input.setId("in");
        Object fullState = input.saveState(facesContext);
        input.markInitialState();

        // the row before leaves its local value in the component
        input.setValue("typed");
        Assertions.assertTrue(input.isLocalValueSet());

        // same sequence as UIData.restoreFullDescendantComponentStates()
        input.clearInitialState();
        input.restoreState(facesContext, fullState);
        input.markInitialState();

        Assertions.assertNull(input.getLocalValue());
        Assertions.assertFalse(input.isLocalValueSet());
        Assertions.assertEquals("in", input.getId());
    }

    @Test
    public void testFullStateWithEmptyStateHelperClearsAttributes()
    {
        UIOutput output = new UIOutput();
        output.setId("out");
        Object fullState = output.saveState(facesContext);
        output.markInitialState();

        output.getAttributes().put("styleClass", "row-before");
        output.setRendered(false);

        output.clearInitialState();
        output.restoreState(facesContext, fullState);
        output.markInitialState();

        Assertions.assertNull(output.getAttributes().get("styleClass"));
        Assertions.assertTrue(output.isRendered());
    }

    /**
     * Control: a full state with something in its state helper already replaces the state helper.
     */
    @Test
    public void testFullStateWithStateHelperReplacesLocalValue()
    {
        UIInput input = new UIInput();
        input.setId("in");
        input.setRequired(true);
        Object fullState = input.saveState(facesContext);
        input.markInitialState();

        input.setValue("typed");

        input.clearInitialState();
        input.restoreState(facesContext, fullState);
        input.markInitialState();

        Assertions.assertNull(input.getLocalValue());
        Assertions.assertFalse(input.isLocalValueSet());
        Assertions.assertTrue(input.isRequired());
    }

    /**
     * A null delta keeps its meaning: nothing changed, so the current state stays as it is.
     */
    @Test
    public void testNullDeltaKeepsLocalValue()
    {
        UIInput input = new UIInput();
        input.setId("in");
        input.markInitialState();
        input.setValue("typed");

        UIInput other = new UIInput();
        other.setId("in");
        other.markInitialState();
        Object delta = other.saveState(facesContext);
        Assertions.assertNull(delta);

        input.restoreState(facesContext, delta);
        Assertions.assertEquals("typed", input.getLocalValue());
        Assertions.assertTrue(input.isLocalValueSet());
    }
}
