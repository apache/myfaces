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

import jakarta.faces.application.StateManager;
import jakarta.faces.component.*;
import jakarta.faces.component.UIDataTest.RowData;
import jakarta.faces.event.PhaseId;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UIRepeatTest extends AbstractFacesTestCase
{

    public UIRepeatTest()
    {
        super();
    }

    private IMocksControl _mocksControl;
    private UIData _testImpl;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        _mocksControl = EasyMock.createControl();
        _testImpl = new UIData();
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIData#setValueExpression(java.lang.String, jakarta.el.ValueExpression)}.
     */
    @Test
    public void testValueExpression()
    {
        assertSetValueExpressionException(IllegalArgumentException.class, "rowIndex");
        assertSetValueExpressionException(NullPointerException.class, null);
    }

    private void assertSetValueExpressionException(Class<? extends Throwable> expected, final String name)
    {
        Assertions.assertThrows(expected,
                () -> _testImpl.setValueExpression(name, null));
    }

    /*
     * Borrowed from UIDataTest#testPreserveRowComponentState1
     * Adapted to use UIRepeat with rowStatePreserved
     */
    @Test
    public void testPreserveRowComponentState() throws Exception
    {
        List<RowData> model = new ArrayList<RowData>();
        model.add(new RowData("text1","style1"));
        model.add(new RowData("text1","style2"));
        model.add(new RowData("text1","style3"));
        model.add(new RowData("text1","style4"));
        
        //Put on request map to be resolved later
        request.setAttribute("list", model);
        
        UIViewRoot root = facesContext.getViewRoot();
        UIRepeat table = new UIRepeat();
        UIColumn column = new UIColumn();
        UIOutput text = new UIOutput();
        
        //This is only required if markInitiaState fix is not used 
        root.setId(root.createUniqueId());
        table.setId(root.createUniqueId());
        column.setId(root.createUniqueId());
        text.setId(root.createUniqueId());
        
        table.setVar("row");
        table.setRowStatePreserved(true); // Key part of test!
        table.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{list}",List.class));
        
        text.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{row.text}",String.class));
        
        root.getChildren().add(table);
        table.getChildren().add(column);
        column.getChildren().add(text);

        //Simulate markInitialState call.
        facesContext.getAttributes().put(StateManager.IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
        root.markInitialState();
        table.markInitialState();
        column.markInitialState();
        text.markInitialState();
        facesContext.getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);
        
        //Check the value expressions are working and change the component state 
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assertions.assertEquals(rowData.getText(), text.getValue());
            text.getAttributes().put("style", rowData.getStyle());
        }
        
        //Reset row index
        table.setRowIndex(-1);

        //Check the values were not lost
        for (int i = 0; i < model.size(); i++)
        {
            table.setRowIndex(i);
            Assertions.assertEquals(model.get(i).getStyle(), text.getAttributes().get("style"));
        }
        
    }


    /**
     * Test state save and restore cycle taking in consideration portlet case.
     * 
     * In portlets, saveState() could be called on INVOKE_APPLICATION phase and
     * restoreState() could be called in RENDER_RESPONSE phase.
     * 
     * This test is active when PSS is disabled.
     * 
     * @throws Exception 
     */
    @Test
    public void testSaveAndRestorePortletLifecycleWithoutPss1() throws Exception
    {
        List<RowData> model = new ArrayList<RowData>();
        model.add(new RowData("text1","style1"));
        model.add(new RowData("text2","style2"));
        model.add(new RowData("text3","style3"));
        model.add(new RowData("text4","style4"));
        
        //Put on request map to be resolved later
        request.setAttribute("list", model);
        
        UIViewRoot root = facesContext.getViewRoot();
        createSimpleTable(root);
        UIRepeat table = (UIRepeat) root.getChildren().get(0);
        //UIColumn column = (UIColumn) table.getChildren().get(0);
        UIInput text = (UIInput) table.getChildren().get(0);
        
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        
        //Check the value expressions are working and change the component state 
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assertions.assertEquals(rowData.getText(), text.getValue());
            text.setSubmittedValue("value"+(i+1));
            //text.getAttributes().put("style", rowData.getStyle());
        }
        
        //Reset row index
        table.setRowIndex(-1);
        
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        Object state = table.saveState(facesContext);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(state);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object restoredState = ois.readObject();
        oos.close();
        ois.close();
        
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        facesContext.setViewRoot(new UIViewRoot());
        root = facesContext.getViewRoot();
        createSimpleTable(root);
        table = (UIRepeat) root.getChildren().get(0);
        //column = (UIColumn) table.getChildren().get(0);
        text = (UIInput) table.getChildren().get(0);
        
        table.restoreState(facesContext, restoredState);

        //Check the values were not lost
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assertions.assertEquals("value"+(i+1), text.getSubmittedValue());
            //assertEquals(model.get(i).getStyle(), text.getAttributes().get("style"));
        }
    }
    
    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UIRepeat createRepeat(UIViewRoot root, List<?> model)
    {
        UIRepeat table = new UIRepeat();
        root.setId(root.createUniqueId());
        table.setId(root.createUniqueId());
        table.setVar("row");
        table.setValue(model);
        root.getChildren().add(table);
        return table;
    }

    private void createSimpleTable(UIViewRoot root)
    {
        UIRepeat table = new UIRepeat();
        UIInput text = new UIInput();

        root.setId(root.createUniqueId());
        table.setId(root.createUniqueId());
        text.setId(root.createUniqueId());

        table.setVar("row");
        table.setValueExpression("value", application
                .getExpressionFactory().createValueExpression(
                        facesContext.getELContext(), "#{list}", List.class));

        text.setValueExpression("value", application
                .getExpressionFactory().createValueExpression(
                        facesContext.getELContext(), "#{row.text}", String.class));

        root.getChildren().add(table);
        table.getChildren().add(text);
    }

    // -------------------------------------------------------------------------
    // Stateful (EVH) flat-list save / restore
    // -------------------------------------------------------------------------

    /**
     * Core correctness test for the flat-list stateful path.
     *
     * <p>Iterates 3 rows setting a submitted value on the UIInput, then revisits
     * each row and verifies the saved submitted value is correctly restored.
     * This exercises {@code saveChildStates} / {@code restoreChildStates}.
     */
    @Test
    public void testStatefulRowStateIsPreservedOnRevisit()
    {
        UIViewRoot root = facesContext.getViewRoot();
        UIRepeat repeat = createRepeat(root, Arrays.asList("a", "b", "c"));

        UIInput input = new UIInput();
        input.setId("inp");
        repeat.getChildren().add(input);

        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);

        // First pass: visit every row and set a per-row submitted value.
        for (int i = 0; i < 3; i++)
        {
            repeat.setRowIndex(i);
            input.setSubmittedValue("submitted_" + i);
        }
        repeat.setRowIndex(-1);

        // Second pass: revisit rows and verify the submitted values were preserved.
        for (int i = 0; i < 3; i++)
        {
            repeat.setRowIndex(i);
            Assertions.assertEquals("submitted_" + i, input.getSubmittedValue(),
                    "Row " + i + ": submitted value should be restored from Integer row-index-keyed _rowStates");
        }
        repeat.setRowIndex(-1);
    }

    /**
     * After visiting rows with state, moving to index -1 must reset all EVH
     * components to their initial (empty) state.
     */
    @Test
    public void testStatefulResetToInitialStateAtIndexMinusOne()
    {
        UIViewRoot root = facesContext.getViewRoot();
        UIRepeat repeat = createRepeat(root, Arrays.asList("a", "b"));

        UIInput input = new UIInput();
        input.setId("inp");
        repeat.getChildren().add(input);

        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);

        repeat.setRowIndex(0);
        input.setSubmittedValue("dirty");
        repeat.setRowIndex(1);
        input.setSubmittedValue("also_dirty");

        // Reset to -1: components must be in their clean template state.
        repeat.setRowIndex(-1);
        Assertions.assertNull(input.getSubmittedValue(),
                "After reset to -1, submitted value must be cleared to initial state");
        Assertions.assertTrue(input.isValid(),
                "After reset to -1, valid flag must be restored to true");
    }

    /**
     * Multiple UIInput siblings must each have independent per-row state.
     */
    @Test
    public void testStatefulMultipleEVHSiblingsHaveIndependentState()
    {
        UIViewRoot root = facesContext.getViewRoot();
        UIRepeat repeat = createRepeat(root, Arrays.asList("x", "y"));

        UIInput inputA = new UIInput();
        inputA.setId("a");
        UIInput inputB = new UIInput();
        inputB.setId("b");
        repeat.getChildren().add(inputA);
        repeat.getChildren().add(inputB);

        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);

        repeat.setRowIndex(0);
        inputA.setSubmittedValue("a0");
        inputB.setSubmittedValue("b0");

        repeat.setRowIndex(1);
        inputA.setSubmittedValue("a1");
        inputB.setSubmittedValue("b1");

        repeat.setRowIndex(-1);

        // Revisit row 0 — both inputs must have their row-0 values.
        repeat.setRowIndex(0);
        Assertions.assertEquals("a0", inputA.getSubmittedValue());
        Assertions.assertEquals("b0", inputB.getSubmittedValue());

        // Revisit row 1 — both inputs must have their row-1 values.
        repeat.setRowIndex(1);
        Assertions.assertEquals("a1", inputA.getSubmittedValue());
        Assertions.assertEquals("b1", inputB.getSubmittedValue());
    }

    /**
     * UIInput nested inside a UIOutput panel must still be found in
     * {@code _iterationEVHList} (depth-first traversal).
     */
    @Test
    public void testStatefulNestedEVHStateIsPreserved()
    {
        UIViewRoot root = facesContext.getViewRoot();
        UIRepeat repeat = createRepeat(root, Arrays.asList("x", "y"));

        UIOutput panel = new UIOutput();
        panel.setId("panel");
        UIInput input = new UIInput();
        input.setId("inp");
        panel.getChildren().add(input);
        repeat.getChildren().add(panel);

        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);

        repeat.setRowIndex(0);
        input.setSubmittedValue("nested_0");

        repeat.setRowIndex(1);
        input.setSubmittedValue("nested_1");

        repeat.setRowIndex(-1);

        repeat.setRowIndex(0);
        Assertions.assertEquals("nested_0", input.getSubmittedValue(),
                "Nested UIInput row-0 state must be restored");

        repeat.setRowIndex(1);
        Assertions.assertEquals("nested_1", input.getSubmittedValue(),
                "Nested UIInput row-1 state must be restored");
    }

    // -------------------------------------------------------------------------
    // Read-only (no EVH) flat-list clientId reset
    // -------------------------------------------------------------------------

    /**
     * For a read-only repeat (UIOutput children only), {@code setRowIndex} must
     * reset clientIds correctly so each row produces unique clientIds.
     */
    @Test
    public void testReadOnlyClientIdResetPerRow()
    {
        UIViewRoot root = facesContext.getViewRoot();
        UIRepeat repeat = createRepeat(root, Arrays.asList("x", "y", "z"));
        repeat.setId("repeat");

        UIOutput out = new UIOutput();
        out.setId("out");
        repeat.getChildren().add(out);

        repeat.setRowIndex(0);
        String clientId0 = out.getClientId(facesContext);

        repeat.setRowIndex(1);
        String clientId1 = out.getClientId(facesContext);

        repeat.setRowIndex(2);
        String clientId2 = out.getClientId(facesContext);

        Assertions.assertNotEquals(clientId0, clientId1, "ClientIds must differ between rows");
        Assertions.assertNotEquals(clientId1, clientId2, "ClientIds must differ between rows");

        // After resetting, the IDs must match the first pass.
        repeat.setRowIndex(-1);
        repeat.setRowIndex(0);
        Assertions.assertEquals(clientId0, out.getClientId(facesContext),
                "ClientId for row 0 must be stable across iteration sequences");
    }

    /**
     * Transient components must also have their clientId reset even though their
     * state is not saved ({@code _iterationResetList} includes them).
     */
    @Test
    public void testReadOnlyTransientComponentClientIdIsReset()
    {
        UIViewRoot root = facesContext.getViewRoot();
        UIRepeat repeat = createRepeat(root, Arrays.asList("x", "y"));
        repeat.setId("repeat");

        UIOutput transientOut = new UIOutput();
        transientOut.setId("tout");
        transientOut.setTransient(true);
        repeat.getChildren().add(transientOut);

        repeat.setRowIndex(0);
        String id0 = transientOut.getClientId(facesContext);

        repeat.setRowIndex(1);
        String id1 = transientOut.getClientId(facesContext);

        Assertions.assertNotEquals(id0, id1,
                "Transient component clientId must be reset per row");
    }

    // -------------------------------------------------------------------------
    // Iteration list lifecycle
    // -------------------------------------------------------------------------

    /**
     * After {@code encodeBegin} the iteration lists are cleared.  A subsequent
     * iteration sequence (simulating RENDER_RESPONSE) must still work correctly.
     */
    @Test
    public void testIterationListsAreRebuiltAfterEncodeBegin() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        root.setRenderKitId("HTML_BASIC");
        UIRepeat repeat = createRepeat(root, Arrays.asList("a", "b"));

        UIInput input = new UIInput();
        input.setId("inp");
        repeat.getChildren().add(input);

        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);

        // First iteration sequence (simulates APPLY_REQUEST_VALUES)
        repeat.setRowIndex(0);
        input.setSubmittedValue("v0");
        repeat.setRowIndex(1);
        input.setSubmittedValue("v1");
        repeat.setRowIndex(-1);

        // encodeBegin clears the iteration lists
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        repeat.encodeBegin(facesContext);

        // Second iteration sequence (simulates RENDER_RESPONSE row walk)
        // must still reset clientIds without NPE or stale state
        repeat.setRowIndex(0);
        Assertions.assertNull(input.getSubmittedValue(),
                "After encodeBegin, _rowStates is cleared and initial state is clean");

        repeat.setRowIndex(1);
        Assertions.assertNull(input.getSubmittedValue());

        repeat.setRowIndex(-1);
    }
}
