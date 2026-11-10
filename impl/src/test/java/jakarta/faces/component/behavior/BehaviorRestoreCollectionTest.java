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
package jakarta.faces.component.behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.faces.event.PhaseId;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behaviors have their own copy of saveAttachedState()/restoreAttachedState() (BehaviorBase and the behavior
 * _DeltaStateHelper), which restores every List as an ArrayList and keeps any other Serializable collection as it is.
 * So collections without a zero-argument constructor, such as List.of() (MYFACES-4776), are restored there too.
 */
public class BehaviorRestoreCollectionTest extends AbstractFacesTestCase
{
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
    }

    @Test
    public void testFullState()
    {
        assertRoundTrip(false, List.of("a", "b"), Arrays.asList("c", "d"));
        assertRoundTrip(false, Collections.unmodifiableList(new ArrayList<>(List.of("a"))), Set.of("c"));
    }

    @Test
    public void testDeltaState()
    {
        assertRoundTrip(true, List.of("a", "b"), Arrays.asList("c", "d"));
        assertRoundTrip(true, Collections.singletonList("a"), Set.of("c"));
    }

    private void assertRoundTrip(boolean delta, Collection<String> execute, Collection<String> render)
    {
        AjaxBehavior behavior = new AjaxBehavior();
        if (delta)
        {
            behavior.markInitialState();
        }
        behavior.setExecute(execute);
        behavior.setRender(render);

        Object state = behavior.saveState(facesContext);

        AjaxBehavior restored = new AjaxBehavior();
        if (delta)
        {
            restored.markInitialState();
        }
        restored.restoreState(facesContext, state);

        Assertions.assertEquals(new ArrayList<>(execute), new ArrayList<>(restored.getExecute()));
        Assertions.assertEquals(new ArrayList<>(render), new ArrayList<>(restored.getRender()));
    }
}
