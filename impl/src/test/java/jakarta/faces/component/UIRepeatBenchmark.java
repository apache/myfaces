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

import org.apache.myfaces.core.api.shared.EditableValueHolderState;

import java.util.Arrays;

/**
 * Microbenchmark for the UIRepeat per-row state-save/restore cycle and the full
 * JSF lifecycle phases (APPLY_REQUEST_VALUES, PROCESS_VALIDATIONS, UPDATE_MODEL_VALUES).
 *
 * <p>Run as a plain {@code main()} — no JMH, no test framework needed.
 * To compare before/after the TransientStateHelper change:
 * <ol>
 *   <li>Run with current code (after the change).</li>
 *   <li>{@code git stash} to revert to StateHelper-based code.</li>
 *   <li>Run again and compare.</li>
 * </ol>
 *
 * <p>No FacesContext is required: all benchmarked paths operate purely on
 * in-memory fields and do not call {@code _component.getFacesContext()}.
 *
 * <p>Scenarios exercised:
 * <ul>
 *   <li><b>getter/setter hot path</b> – {@code setValid/isValid},
 *       {@code setSubmittedValue/getSubmittedValue},
 *       {@code setLocalValueSet/isLocalValueSet}.</li>
 *   <li><b>{@code saveTransientState}/{@code restoreTransientState}</b> –
 *       UIRepeat's {@code _rowTransientStates} path.</li>
 *   <li><b>{@code EditableValueHolderState} roundtrip</b> –
 *       UIRepeat's {@code _rowStates} (EVH) path.</li>
 *   <li><b>Full row-switch (render)</b> – EVH save+restore per row, read-only.</li>
 *   <li><b>APPLY_REQUEST_VALUES</b> – restore → decode (setSubmittedValue) → save.</li>
 *   <li><b>PROCESS_VALIDATIONS success</b> – restore → validate OK (setValue + clear sv) → save.</li>
 *   <li><b>PROCESS_VALIDATIONS failure</b> – restore → validate fail (setValid(false)) → save.</li>
 *   <li><b>UPDATE_MODEL_VALUES</b> – restore → updateModel (setValue(null)) → save.</li>
 * </ul>
 */
public class UIRepeatBenchmark
{
    static final int ROWS    =       10;
    static final int WARMUP  =  300_000;
    static final int MEASURE =  3_000_000;
    static final int ROUNDS  =       12;

    /** Pre-allocated submitted-value strings — avoids String allocation inside benchmarked loops. */
    static final String[] SUBMITTED_VALUES = new String[ROWS];
    static
    {
        for (int i = 0; i < ROWS; i++)
        {
            SUBMITTED_VALUES[i] = "field-value-" + i;
        }
    }

    // ── runner ────────────────────────────────────────────────────────────────

    static double run(String label, Runnable bench)
    {
        for (int i = 0; i < WARMUP; i++)
        {
            bench.run();
        }
        long[] times = new long[ROUNDS];
        for (int r = 0; r < ROUNDS; r++)
        {
            long t0 = System.nanoTime();
            for (int i = 0; i < MEASURE; i++)
            {
                bench.run();
            }
            times[r] = System.nanoTime() - t0;
        }
        Arrays.sort(times);
        double ns = (double) times[ROUNDS / 2] / MEASURE;
        System.out.printf("  %-50s %6.2f ns/op%n", label, ns);
        return ns;
    }

    static void header(String title)
    {
        System.out.println("\n=== " + title + " ===");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Reset component back to all-default state between sub-benchmarks. */
    static void resetInput(UIInput input)
    {
        input.setValid(true);
        input.setLocalValueSet(false);
        input.setSubmittedValue(null);
    }

    /**
     * Pre-populate N per-row snapshots that simulate a realistic mix:
     * some rows are clean (all defaults), others have validation errors.
     */
    static Object[] buildTransientSnapshots(UIInput input, int n)
    {
        Object[] snaps = new Object[n];
        for (int i = 0; i < n; i++)
        {
            if (i % 3 == 0)
            {
                // row has a validation error
                input.setValid(false);
                input.setSubmittedValue("bad-value-" + i);
                input.setLocalValueSet(false);
            }
            else
            {
                // row is clean
                input.setValid(true);
                input.setSubmittedValue(null);
                input.setLocalValueSet(false);
            }
            snaps[i] = input.saveTransientState(null);
        }
        resetInput(input);
        return snaps;
    }

    static EditableValueHolderState[] buildEvhSnapshots(UIInput input, int n)
    {
        EditableValueHolderState[] snaps = new EditableValueHolderState[n];
        for (int i = 0; i < n; i++)
        {
            if (i % 3 == 0)
            {
                input.setValid(false);
                input.setSubmittedValue("bad-value-" + i);
            }
            else
            {
                input.setValid(true);
                input.setSubmittedValue(null);
            }
            snaps[i] = EditableValueHolderState.create(input);
        }
        resetInput(input);
        return snaps;
    }

    // ── main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args)
    {
        UIInput input = new UIInput();

        // ── 1. getter / setter hot path ───────────────────────────────────────
        header("getter/setter hot path  (non-default state, matches validate() path)");

        run("setValid(false) + isValid()", () ->
        {
            input.setValid(false);
            boolean v = input.isValid();
        });

        run("setValid(true) + isValid()  [default, no-op path]", () ->
        {
            input.setValid(true);
            boolean v = input.isValid();
        });

        run("setSubmittedValue(str) + getSubmittedValue()", () ->
        {
            input.setSubmittedValue("hello");
            Object sv = input.getSubmittedValue();
        });

        run("setSubmittedValue(null) + getSubmittedValue() [clear path]", () ->
        {
            input.setSubmittedValue(null);
            Object sv = input.getSubmittedValue();
        });

        run("setLocalValueSet(true) + isLocalValueSet()", () ->
        {
            input.setLocalValueSet(true);
            boolean lv = input.isLocalValueSet();
        });

        run("all three setters + getters combined", () ->
        {
            input.setValid(false);
            input.setSubmittedValue("v");
            input.setLocalValueSet(false);
            boolean v = input.isValid();
            Object sv = input.getSubmittedValue();
            boolean lv = input.isLocalValueSet();
        });

        // ── 2. saveTransientState + restoreTransientState ─────────────────────
        // This is UIRepeat's _rowTransientStates path: one save+restore per
        // child component per row switch.
        header("saveTransientState + restoreTransientState  (UIRepeat _rowTransientStates path)");

        // clean state (null snapshot — the common case for valid, unsubmitted rows)
        resetInput(input);
        run("save+restore  [all defaults — null snapshot]", () ->
        {
            Object s = input.saveTransientState(null);
            input.restoreTransientState(null, s);
        });

        // dirty state (validation error — non-null snapshot)
        input.setValid(false);
        input.setSubmittedValue("bad");
        run("save+restore  [valid=false, submittedValue set]", () ->
        {
            Object s = input.saveTransientState(null);
            input.restoreTransientState(null, s);
        });
        resetInput(input);

        // ── 3. EditableValueHolderState roundtrip ─────────────────────────────
        // This is UIRepeat's _rowStates (EVH) path: UIRepeat calls
        // EditableValueHolderState.create(evh) when leaving a row and
        // state.restoreState(evh) when entering the next row.
        header("EditableValueHolderState roundtrip  (UIRepeat _rowStates path)");

        // clean row — create() returns null, EMPTY.restoreState() is used
        resetInput(input);
        run("create [defaults → null] + EMPTY.restoreState", () ->
        {
            EditableValueHolderState s = EditableValueHolderState.create(input);
            // s is null for default state; UIRepeat uses EMPTY.restoreState
            EditableValueHolderState.EMPTY.restoreState(input);
        });

        // dirty row — create() allocates a new object
        input.setValid(false);
        input.setSubmittedValue("bad-value");
        run("create [non-default] + restoreState", () ->
        {
            EditableValueHolderState s = EditableValueHolderState.create(input);
            s.restoreState(input);
        });
        resetInput(input);

        // ── 4a. Full iteration — MODERN path ─────────────────────────────────
        // _setIndexWithoutPreserveComponentState: only EditableValueHolderState,
        // NO saveTransientState/restoreTransientState on children.
        // This is the path taken by virtually all real UIRepeat usage.
        header("Full row-switch — MODERN path  (" + ROWS + " rows, EVH only)");

        EditableValueHolderState[] evhSnaps = buildEvhSnapshots(input, ROWS);

        run("full iteration  (EVH save+restore per row only)", () ->
        {
            for (int row = 0; row < ROWS; row++)
            {
                // --- UIRepeat: entering row (restoreChildStates) ---
                EditableValueHolderState evh = evhSnaps[row];
                if (evh != null)
                {
                    evh.restoreState(input);
                }
                else
                {
                    EditableValueHolderState.EMPTY.restoreState(input);
                }

                // --- (validation / rendering would happen here) ---
                boolean valid = input.isValid();
                Object sv = input.getSubmittedValue();

                // --- UIRepeat: leaving row (saveChildStates) ---
                evhSnaps[row] = EditableValueHolderState.create(input);
            }
        });

        // ── 4b. Full iteration — LEGACY path ──────────────────────────────────
        // _setIndexWithPreserveComponentState: only _rowTransientStates via
        // saveTransientState/restoreTransientState on children.
        // Only active when rowStatePreserved=true.
        header("Full row-switch — LEGACY path  (" + ROWS + " rows, transient only)");

        Object[] transientSnaps = buildTransientSnapshots(input, ROWS);

        run("full iteration  (transient save+restore per row only)", () ->
        {
            for (int row = 0; row < ROWS; row++)
            {
                // --- UIRepeat: entering row (restoreTransientDescendantComponentStates) ---
                input.restoreTransientState(null, transientSnaps[row]);

                // --- (validation / rendering would happen here) ---
                boolean valid = input.isValid();
                Object sv = input.getSubmittedValue();

                // --- UIRepeat: leaving row (saveTransientDescendantComponentStates) ---
                transientSnaps[row] = input.saveTransientState(null);
            }
        });

        // ── 5a. APPLY_REQUEST_VALUES ───────────────────────────────────────────
        // Simulates UIRepeat.processDecodes(): for each row, restore EVH state,
        // let the renderer decode the request (setSubmittedValue), then save EVH state.
        // This is where Mojarra's plain-field submittedValue has a structural advantage:
        // its setSubmittedValue is a direct field write; ours goes through TransientStateHelper.
        header("Full row-switch — APPLY_REQUEST_VALUES  (" + ROWS + " rows)");

        // Start with empty snapshots (fresh request, no prior per-row state).
        EditableValueHolderState[] applySnaps = new EditableValueHolderState[ROWS];
        resetInput(input);

        run("restore → decode (setSubmittedValue) → save", () ->
        {
            for (int row = 0; row < ROWS; row++)
            {
                // UIRepeat: entering row — restore previous EVH state (null on first visit)
                EditableValueHolderState prevEvh = applySnaps[row];
                if (prevEvh != null)
                {
                    prevEvh.restoreState(input);
                }
                else
                {
                    EditableValueHolderState.EMPTY.restoreState(input);
                }

                // Simulate renderer decode: sets the submitted form value on the component
                input.setSubmittedValue(SUBMITTED_VALUES[row]);

                // UIRepeat: leaving row — save updated EVH state
                applySnaps[row] = EditableValueHolderState.create(input);
            }
        });

        // ── 5b. PROCESS_VALIDATIONS — success ─────────────────────────────────
        // Simulates UIInput.validate() succeeding: reads submittedValue, converts it,
        // calls setValue(convertedValue) [which also sets localValueSet=true],
        // then clears submittedValue.
        header("Full row-switch — PROCESS_VALIDATIONS success  (" + ROWS + " rows)");

        // Seed snapshots as if APPLY_REQUEST_VALUES already ran: each row has a submittedValue.
        EditableValueHolderState[] validateOkSnaps = new EditableValueHolderState[ROWS];
        for (int i = 0; i < ROWS; i++)
        {
            input.setSubmittedValue(SUBMITTED_VALUES[i]);
            validateOkSnaps[i] = EditableValueHolderState.create(input);
        }
        resetInput(input);

        run("restore → validate OK (setValue + clear sv) → save", () ->
        {
            for (int row = 0; row < ROWS; row++)
            {
                EditableValueHolderState evh = validateOkSnaps[row];
                if (evh != null)
                {
                    evh.restoreState(input);
                }
                else
                {
                    EditableValueHolderState.EMPTY.restoreState(input);
                }

                // Simulate UIInput.validate() success path:
                //   converted = getConvertedValue(...) → skipped (no FacesContext needed)
                Object sv = input.getSubmittedValue();   // read submitted value
                input.setValue(sv);                       // store as local value (also sets localValueSet=true)
                input.setSubmittedValue(null);            // clear submitted value after successful conversion

                validateOkSnaps[row] = EditableValueHolderState.create(input);
            }
        });

        // ── 5c. PROCESS_VALIDATIONS — failure ─────────────────────────────────
        // Simulates UIInput.validate() failing: reads submittedValue, marks invalid,
        // keeps submittedValue for re-display in the error state.
        header("Full row-switch — PROCESS_VALIDATIONS failure  (" + ROWS + " rows)");

        // Seed snapshots with submitted values.
        EditableValueHolderState[] validateFailSnaps = new EditableValueHolderState[ROWS];
        for (int i = 0; i < ROWS; i++)
        {
            input.setSubmittedValue(SUBMITTED_VALUES[i]);
            validateFailSnaps[i] = EditableValueHolderState.create(input);
        }
        resetInput(input);

        run("restore → validate fail (setValid(false)) → save", () ->
        {
            for (int row = 0; row < ROWS; row++)
            {
                EditableValueHolderState evh = validateFailSnaps[row];
                if (evh != null)
                {
                    evh.restoreState(input);
                }
                else
                {
                    EditableValueHolderState.EMPTY.restoreState(input);
                }

                // Simulate UIInput.validate() failure path:
                //   submittedValue is retained; component is marked invalid
                Object sv = input.getSubmittedValue();   // read (retained for error display)
                input.setValid(false);                    // mark invalid

                validateFailSnaps[row] = EditableValueHolderState.create(input);
            }
        });

        // ── 5d. UPDATE_MODEL_VALUES ────────────────────────────────────────────
        // Simulates UIInput.updateModel() succeeding: checks isValid + isLocalValueSet,
        // writes to EL model (skipped here), then resets the local value.
        // The reset path (setValue(null) + setLocalValueSet(false)) is the hot path
        // because setValue(null) internally also calls setLocalValueSet(true),
        // causing an add-then-remove cycle in TransientStateHelper.
        header("Full row-switch — UPDATE_MODEL_VALUES  (" + ROWS + " rows)");

        // Seed snapshots as if PROCESS_VALIDATIONS success ran: each row has a local value.
        EditableValueHolderState[] updateSnaps = new EditableValueHolderState[ROWS];
        for (int i = 0; i < ROWS; i++)
        {
            input.setValue(SUBMITTED_VALUES[i]);  // also sets localValueSet=true
            updateSnaps[i] = EditableValueHolderState.create(input);
        }
        resetInput(input);

        run("restore → updateModel (setValue(null) + setLocalValueSet(false)) → save", () ->
        {
            for (int row = 0; row < ROWS; row++)
            {
                EditableValueHolderState evh = updateSnaps[row];
                if (evh != null)
                {
                    evh.restoreState(input);
                }
                else
                {
                    EditableValueHolderState.EMPTY.restoreState(input);
                }

                // Simulate UIInput.updateModel() success path:
                if (input.isValid() && input.isLocalValueSet())
                {
                    // expression.setValue(context.getELContext(), getLocalValue()) skipped
                    input.setValue(null);
                    input.setLocalValueSet(false);
                }

                updateSnaps[row] = EditableValueHolderState.create(input);
            }
        });

        System.out.println();
    }
}
