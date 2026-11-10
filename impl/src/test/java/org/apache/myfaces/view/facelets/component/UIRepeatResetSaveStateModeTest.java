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
package org.apache.myfaces.view.facelets.component;

import java.util.Map;
import jakarta.faces.event.PhaseId;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.view.facelets.ViewPoolProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The view pool resets the state of a view when it saves it: a soft reset clears only the transient state and keeps
 * the delta state, a hard reset clears all state (see {@link ViewPoolProcessor#RESET_MODE_SOFT} and
 * {@link ViewPoolProcessor#RESET_MODE_HARD}). UIRepeat must do the same as UIData with its per-row state.
 */
public class UIRepeatResetSaveStateModeTest extends AbstractFacesTestCase
{
    private UIRepeat repeat;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        repeat = new UIRepeat();
        repeat.setId("repeat");
        facesContext.getViewRoot().getChildren().add(repeat);
        repeat.markInitialState();
        repeat._rowDeltaStates.put("repeat:0", Map.of("repeat:0:in", "delta"));
        repeat._rowTransientStates.put("repeat:0", Map.of("repeat:0:in", "transient"));
    }

    @Test
    public void testSoftResetKeepsDeltaState()
    {
        facesContext.getViewRoot().getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY,
                ViewPoolProcessor.RESET_MODE_SOFT);
        repeat.saveState(facesContext);

        Assertions.assertEquals(Map.of("repeat:0:in", "delta"), repeat._rowDeltaStates.get("repeat:0"));
        Assertions.assertTrue(repeat._rowTransientStates.isEmpty());
    }

    @Test
    public void testHardResetClearsAllState()
    {
        facesContext.getViewRoot().getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY,
                ViewPoolProcessor.RESET_MODE_HARD);
        repeat.saveState(facesContext);

        Assertions.assertTrue(repeat._rowDeltaStates.isEmpty());
        Assertions.assertTrue(repeat._rowTransientStates.isEmpty());
    }
}
