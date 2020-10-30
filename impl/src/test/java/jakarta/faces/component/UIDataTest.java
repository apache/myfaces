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

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIData;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIInput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.application.StateManager;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.test.MyFacesAsserts;
import org.apache.myfaces.test.TestRunner;
import org.apache.myfaces.test.mock.MockRenderedValueExpression;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.visit.MockVisitContext;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;

public class UIDataTest extends AbstractJsfTestCase
{
    private IMocksControl _mocksControl;
    private UIData _testImpl;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _mocksControl = EasyMock.createControl();
        _testImpl = new UIData();
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIData#setValueExpression(java.lang.String, javax.el.ValueExpression)}.
     */
    public void testValueExpression()
    {
        assertSetValueExpressionException(IllegalArgumentException.class, "rowIndex");
        assertSetValueExpressionException(NullPointerException.class, null);
    }

    private void assertSetValueExpressionException(Class<? extends Throwable> expected, final String name)
    {
        MyFacesAsserts.assertException(expected, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.setValueExpression(name, null);
            }
        });
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getClientId(jakarta.faces.context.FacesContext)}.
     */
    public void testGetClientId()
    {
        _testImpl.setId("xxx");
        Renderer renderer = _mocksControl.createMock(Renderer.class);
        renderKit.addRenderer(UIData.COMPONENT_FAMILY, UIData.COMPONENT_TYPE, renderer);
        Assert.assertEquals("xxx", _testImpl.getClientId(facesContext));
        _testImpl.setRowIndex(99);
        //MYFACES-2744 UIData.getClientId() should not append rowIndex, instead use UIData.getContainerClientId()
        Assert.assertEquals("xxx", _testImpl.getClientId(facesContext)); 
        Assert.assertEquals("xxx:99", _testImpl.getContainerClientId(facesContext));
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIData#invokeOnComponent(jakarta.faces.context.FacesContext, java.lang.String, jakarta.faces.component.ContextCallback)}
     * .
     * Tests, if invokeOnComponent also checks the facets of the h:column children (MYFACES-2370)
     */
    public void testInvokeOnComponentFacesContextStringContextCallback()
    {
        /**
         * Concrete class of ContextCallback needed to test invokeOnComponent. 
         */
        final class MyContextCallback implements ContextCallback
        {
            
            private boolean invoked = false;

            public void invokeContextCallback(FacesContext context,
                    UIComponent target)
            {
                this.invoked = true;
            }
            
        }
        
        UIComponent facet = new UIOutput();
        facet.setId("facet");
        UIColumn column = new UIColumn();
        column.setId("column");
        column.getFacets().put("header", facet);
        _testImpl.setId("uidata");
        _testImpl.getChildren().add(column);
        
        MyContextCallback callback = new MyContextCallback();
        _testImpl.invokeOnComponent(facesContext, facet.getClientId(facesContext), callback);
        // the callback should have been invoked
        Assert.assertTrue(callback.invoked);
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#broadcast(jakarta.faces.event.FacesEvent)}.
     */
    public void testBroadcastFacesEvent()
    {
        // create event mock
        final FacesEvent originalEvent = _mocksControl.createMock(FacesEvent.class);
        
        // create the component for the event
        UIComponent eventComponent = new UICommand()
        {

            @Override
            public void broadcast(FacesEvent event)
                    throws AbortProcessingException
            {
                // the event must be the originalEvent
                Assert.assertEquals(originalEvent, event);
                
                // the current row index must be the row index from the time the event was queued
                Assert.assertEquals(5, _testImpl.getRowIndex());
                
                // the current component must be this (pushComponentToEL() must have happened)
                Assert.assertEquals(this, UIComponent.getCurrentComponent(facesContext));
                
                // to be able to verify that broadcast() really has been called
                getAttributes().put("broadcastCalled", Boolean.TRUE);
            }
            
        };
        
        // set component on event
        EasyMock.expect(originalEvent.getComponent()).andReturn(eventComponent).anyTimes();
        // set phase on event
        EasyMock.expect(originalEvent.getPhaseId()).andReturn(PhaseId.INVOKE_APPLICATION).anyTimes();
        _mocksControl.replay();
        
        // set PhaseId for event processing
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        // set row index for event
        _testImpl.setRowIndex(5);
        // UIData must be a child of UIViewRoot to queue and event
        facesContext.getViewRoot().getChildren().add(_testImpl);
        // queue event (this will create a FacesEventWrapper with the current row index)
        _testImpl.queueEvent(originalEvent);
        // change the current row index
        _testImpl.setRowIndex(0);
        // now broadcast the event (this will call UIData.broadcast())
        facesContext.getViewRoot().broadcastEvents(facesContext, PhaseId.INVOKE_APPLICATION);
        
        // -= Assertions =-
        
        // the current component must be null (popComponentFromEL() must have happened)
        Assert.assertNull(UIComponent.getCurrentComponent(facesContext));
        
        // the row index must now be 0 (at broadcast() it must be 5)
        Assert.assertEquals(0, _testImpl.getRowIndex());
        
        // verify mock behavior
        _mocksControl.verify();
        
        // verify that broadcast() really has been called
        Assert.assertEquals(Boolean.TRUE, eventComponent.getAttributes().get("broadcastCalled"));
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#encodeBegin(jakarta.faces.context.FacesContext)}.
     */
    public void testEncodeBeginFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#encodeEnd(jakarta.faces.context.FacesContext)}.
     */
    public void testEncodeEndFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#queueEvent(jakarta.faces.event.FacesEvent)}.
     */
    public void testQueueEventFacesEvent()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#processDecodes(jakarta.faces.context.FacesContext)}.
     */
    public void testProcessDecodesFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#processValidators(jakarta.faces.context.FacesContext)}.
     */
    public void testProcessValidatorsFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#processUpdates(jakarta.faces.context.FacesContext)}.
     */
    public void testProcessUpdatesFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#saveState(jakarta.faces.context.FacesContext)}.
     */
    public void testSaveState()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIData#restoreState(jakarta.faces.context.FacesContext, java.lang.Object)}.
     */
    public void testRestoreState()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#UIData()}.
     */
    public void testUIData()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setFooter(jakarta.faces.component.UIComponent)}.
     */
    public void testSetFooter()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getFooter()}.
     */
    public void testGetFooter()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setHeader(jakarta.faces.component.UIComponent)}.
     */
    public void testSetHeader()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getHeader()}.
     */
    public void testGetHeader()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#isRowAvailable()}.
     */
    public void testIsRowAvailable()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getRowCount()}.
     */
    public void testGetRowCount()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getRowData()}.
     */
    public void testGetRowData()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getRowIndex()}.
     */
    public void testGetRowIndex()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setRowIndex(int)}.
     */
    public void testSetRowIndex()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getDataModel()}.
     */
    public void testGetDataModel()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setDataModel(jakarta.faces.model.DataModel)}.
     */
    public void testSetDataModel()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setValue(java.lang.Object)}.
     */
    public void testSetValue()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setRows(int)}.
     */
    public void testSetRows()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setFirst(int)}.
     */
    public void testSetFirst()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getValue()}.
     */
    public void testGetValue()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getVar()}.
     */
    public void testGetVar()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#setVar(java.lang.String)}.
     */
    public void testSetVar()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getRows()}.
     */
    public void testGetRows()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getFirst()}.
     */
    public void testGetFirst()
    {
        // TODO
    }

    /**
     * Test method for {@link jakarta.faces.component.UIData#getFamily()}.
     */
    public void testGetFamily()
    {
        // TODO
    }
    
    /**
     * Test method for 
     * {@link jakarta.faces.component.UIData#visitTree(jakarta.faces.component.visit.VisitContext, jakarta.faces.component.visit.VisitCallback)}.
     */
    public void testVisitTree() {
        UIData uidata = new UIData();
        // value
        Collection<String> value = new ArrayList<String>();
        value.add("value#1");
        value.add("value#2");
        uidata.setValue(value);
        // header facet
        UIComponent headerFacet = new HtmlPanelGroup();
        headerFacet.setId("headerFacet");
        uidata.setHeader(headerFacet);
        // footer facet
        UIComponent footerFacet = new HtmlPanelGroup();
        footerFacet.setId("footerFacet");
        uidata.setFooter(footerFacet);
        // first child
        UIComponent child1 = new UIColumn();
        // facet of first child
        UIComponent facetOfChild1 = new HtmlPanelGroup();
        child1.getFacets().put("someFacet", facetOfChild1);
        // child of first child
        UIOutput childOfChild1 = new UIOutput();
        childOfChild1.setId("childOfColumn");
        child1.getChildren().add(childOfChild1);
        uidata.getChildren().add(child1);
        // second child (should not be processed --> != UIColumn)
        UIComponent child2 = new HtmlPanelGroup(); 
        uidata.getChildren().add(child2);
        VisitCallback callback = null;
        
        IMocksControl control = EasyMock.createControl();
        VisitContext visitContextMock = control.createMock(VisitContext.class);
        EasyMock.expect(visitContextMock.getFacesContext()).andReturn(facesContext).anyTimes();
        EasyMock.expect(visitContextMock.getHints()).andReturn(Collections.<VisitHint>emptySet()).anyTimes();
        Collection<String> subtreeIdsToVisit = new ArrayList<String>();
        subtreeIdsToVisit.add("1");
        EasyMock.expect(visitContextMock.getSubtreeIdsToVisit(uidata)).andReturn(subtreeIdsToVisit);
        EasyMock.expect(visitContextMock.invokeVisitCallback(uidata, callback)).andReturn(VisitResult.ACCEPT);
        EasyMock.expect(visitContextMock.invokeVisitCallback(headerFacet, callback)).andReturn(VisitResult.ACCEPT);
        EasyMock.expect(visitContextMock.invokeVisitCallback(footerFacet, callback)).andReturn(VisitResult.ACCEPT);
        EasyMock.expect(visitContextMock.invokeVisitCallback(facetOfChild1, callback)).andReturn(VisitResult.ACCEPT);
        EasyMock.expect(visitContextMock.invokeVisitCallback(child1, callback)).andReturn(VisitResult.ACCEPT);
        EasyMock.expect(visitContextMock.invokeVisitCallback(childOfChild1, callback)).andReturn(VisitResult.ACCEPT).times(2);
        control.replay();
        
        uidata.visitTree(visitContextMock, callback);
        
        control.verify();
        
        // VisitHint.SKIP_ITERATION test:
        
        // (1) uiData with two rows - should iterate over row twice
        MockVisitContext mockVisitContext = new MockVisitContext(facesContext, null);
        CountingVisitCallback countingVisitCallback = new CountingVisitCallback(2);
        uidata.visitTree(mockVisitContext, countingVisitCallback);
        countingVisitCallback.verify();
        
        // (2) uiData with two values - should iterate over row ones - SKIP_ITERATION is used
        mockVisitContext = new MockVisitContext(facesContext, EnumSet.of(VisitHint.SKIP_ITERATION));
        countingVisitCallback = new CountingVisitCallback(1);
        uidata.visitTree(mockVisitContext, countingVisitCallback);
        countingVisitCallback.verify();
        
        // (3) uiData with five values - should iterate over row five times
        value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        value.add("3");
        value.add("4");
        value.add("5");
        uidata.setValue(value);
        mockVisitContext = new MockVisitContext(facesContext, null);
        countingVisitCallback = new CountingVisitCallback(5);
        uidata.visitTree(mockVisitContext, countingVisitCallback);
        countingVisitCallback.verify();
        
        // (4) uiData with five values - should iterate over child ones - SKIP_ITERATION is used
        mockVisitContext = new MockVisitContext(facesContext, EnumSet.of(VisitHint.SKIP_ITERATION));
        countingVisitCallback = new CountingVisitCallback(1);
        uidata.visitTree(mockVisitContext, countingVisitCallback);
        countingVisitCallback.verify();
    }

    
    public static class RowData
    {
        private String text;

        public RowData(String text, String style)
        {
           super();
            this.text = text;
            this.style = style;
        }

        private String style;
        
        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }

        public String getStyle()
        {
            return style;
        }

        public void setStyle(String style)
        {
            this.style = style;
        }
    }
    
    public void testPreserveRowComponentState1() throws Exception
    {
        List<RowData> model = new ArrayList<RowData>();
        model.add(new RowData("text1","style1"));
        model.add(new RowData("text1","style2"));
        model.add(new RowData("text1","style3"));
        model.add(new RowData("text1","style4"));
        
        //Put on request map to be resolved later
        request.setAttribute("list", model);
        
        UIViewRoot root = facesContext.getViewRoot();
        UIData table = new UIData();
        UIColumn column = new UIColumn();
        UIOutput text = new UIOutput();
        
        //This is only required if markInitiaState fix is not used 
        root.setId(root.createUniqueId());
        table.setId(root.createUniqueId());
        column.setId(root.createUniqueId());
        text.setId(root.createUniqueId());
        
        table.setVar("row");
        table.setRowStatePreserved(true);
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
            Assert.assertEquals(rowData.getText(), text.getValue());
            text.getAttributes().put("style", rowData.getStyle());
        }
        
        //Reset row index
        table.setRowIndex(-1);

        //Check the values were not lost
        for (int i = 0; i < model.size(); i++)
        {
            table.setRowIndex(i);
            Assert.assertEquals(model.get(i).getStyle(), text.getAttributes().get("style"));
        }
        
    }
        
    public void testCollectionDataModel() throws Exception
    {
        SimpleCollection<RowData> model = new SimpleCollection<RowData>();
        model.add(new RowData("text1","style1"));
        model.add(new RowData("text1","style2"));
        model.add(new RowData("text1","style3"));
        model.add(new RowData("text1","style4"));
        
        //Put on request map to be resolved later
        request.setAttribute("list", model);
        
        UIViewRoot root = facesContext.getViewRoot();
        UIData table = new UIData();
        UIColumn column = new UIColumn();
        UIOutput text = new UIOutput();
        
        //This is only required if markInitiaState fix is not used 
        root.setId(root.createUniqueId());
        table.setId(root.createUniqueId());
        column.setId(root.createUniqueId());
        text.setId(root.createUniqueId());
        
        table.setVar("row");
        table.setRowStatePreserved(true);
        table.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{list}", Collection.class));
        
        text.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{row.text}",String.class));
        
        root.getChildren().add(table);
        table.getChildren().add(column);
        column.getChildren().add(text);
        
        //Check the value expressions are working and change the component state
        int i = 0;
        for (Iterator<RowData> it = model.iterator(); it.hasNext();)
        {
            RowData rowData = it.next(); 
            table.setRowIndex(i);
            Assert.assertEquals(rowData.getText(), text.getValue());
            i++;
        }
        
        //Reset row index
        table.setRowIndex(-1);
    }

    
    private final class SimpleCollection<T> extends AbstractCollection<T>
    {
        private List<T> delegate = new ArrayList();

        public boolean add(T e)
        {
            return delegate.add(e);
        }
            
        @Override
        public Iterator<T> iterator()
        {
            return delegate.iterator();
        }

        @Override
        public int size()
        {
            return delegate.size();
        }
    }

    private final class CountingVisitCallback implements VisitCallback {
        
        public final int expectedVisits;
        
        public CountingVisitCallback(int expectedRowVisits) {
            super();
            this.expectedVisits = expectedRowVisits;
        }

        public int headerFacetVisits = 0;
        public int footerFacetVisits = 0;
        public int rowVisits = 0;
        
        public VisitResult visit(VisitContext context, UIComponent target) {
            
            if ("headerFacet".equals(target.getId())) {
                headerFacetVisits++;
            } else if ("footerFacet".equals(target.getId())) {
                footerFacetVisits++;
            } else if ("childOfColumn".equals(target.getId())) {
                rowVisits++;
            }
            return VisitResult.ACCEPT;
        }
        
        public void verify() {
                Assert.assertEquals("header facet must be visited only ones", 1, headerFacetVisits);
                Assert.assertEquals("footer facet must be visited only ones", 1, footerFacetVisits);
                Assert.assertEquals("Expected row visit does not match", expectedVisits, rowVisits);
        }
    }
    
    public void testProcessDecodesRenderedFalse() throws Exception {
        UIData uiData = new VerifyNoLifecycleMethodComponent();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext,  uiData, false);
        
        uiData.processDecodes(facesContext);
        
        Assert.assertEquals("processDecodes must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
        
    }
    
    public void testProcessDecodesRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, _testImpl, true);
        _addColumn();
        
        _testImpl.processDecodes(facesContext);
        
        Assert.assertEquals("processDecodes must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
    }
    
    
    public void testProcessValidatorsRenderedFalse() throws Exception {
        UIData uiData = new VerifyNoLifecycleMethodComponent();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext,  uiData, false);
        
        uiData.processValidators(facesContext);
        
        Assert.assertEquals("processDecodes must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
        
    }
    
    public void testProcessValidatorsRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, _testImpl, true);
        _addColumn();
        
        _testImpl.processValidators(facesContext);
        
        Assert.assertEquals("processValidators must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
    }
    
    public void testProcessUpdatesRenderedFalse() throws Exception {
        UIData uiData = new VerifyNoLifecycleMethodComponent();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext,  uiData, false);
        
        uiData.processUpdates(facesContext);
        
        Assert.assertEquals("processUpdates must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
        
    }
    
    public void testProcessUpdatesRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, _testImpl, true);
        _addColumn();
        
        _testImpl.processUpdates(facesContext);
        
        Assert.assertEquals("processUpdates must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
    }

    private void _addColumn() {
        UIColumn uiColumn = new UIColumn();
        uiColumn.setId("testId");
        _testImpl.getChildren().add(uiColumn);
        MockRenderedValueExpression ve = new MockRenderedValueExpression("#{component.is eq 'testId'}", Boolean.class, uiColumn, true);
        uiColumn.setValueExpression("rendered", ve);
    }
    

    /** Verifies no call to encode* and process* methods */
    public class VerifyNoLifecycleMethodComponent extends UIData
    {
        public void setRowIndex(int rowIndex) {
            Assert.fail();
        }
        public void decode(FacesContext context) {
            Assert.fail();
        }
        public void validate(FacesContext context) {
            Assert.fail();
        }
        public void updateModel(FacesContext context) {
            Assert.fail();
        }
        public void encodeBegin(FacesContext context) throws IOException {
            Assert.fail();
        }
        public void encodeChildren(FacesContext context) throws IOException {
            Assert.fail();
        }
        public void encodeEnd(FacesContext context) throws IOException {
            Assert.fail();
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
        UIData table = (UIData) root.getChildren().get(0);
        UIColumn column = (UIColumn) table.getChildren().get(0);
        UIInput text = (UIInput) column.getChildren().get(0);
        
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        
        //Check the value expressions are working and change the component state 
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals(rowData.getText(), text.getValue());
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
        Object restoredState = (Object) ois.readObject();
        oos.close();
        ois.close();
        
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        facesContext.setViewRoot(new UIViewRoot());
        root = facesContext.getViewRoot();
        createSimpleTable(root);
        table = (UIData) root.getChildren().get(0);
        column = (UIColumn) table.getChildren().get(0);
        text = (UIInput) column.getChildren().get(0);
        
        table.restoreState(facesContext, restoredState);

        //Check the values were not lost
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals("value"+(i+1), text.getSubmittedValue());
            //assertEquals(model.get(i).getStyle(), text.getAttributes().get("style"));
        }
    }
    
    private void createSimpleTable(UIViewRoot root)
    {
        createSimpleTable(root, false);
    }
    
    private void createSimpleTable(UIViewRoot root, boolean rowStatePreserved)
    {
        UIData table = new UIData();
        UIColumn column = new UIColumn();
        UIInput text = new UIInput();
        
        //This is only required if markInitiaState fix is not used 
        root.setId(root.createUniqueId());
        table.setId(root.createUniqueId());
        column.setId(root.createUniqueId());
        text.setId(root.createUniqueId());
        
        table.setVar("row");
        if (rowStatePreserved)
        {
            table.setRowStatePreserved(true);
        }
        table.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{list}",List.class));
        
        text.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{row.text}",String.class));
        
        root.getChildren().add(table);
        table.getChildren().add(column);
        column.getChildren().add(text);
    }
    
    /**
     * Test state save and restore cycle taking in consideration portlet case.
     * 
     * In portlets, saveState() could be called on INVOKE_APPLICATION phase and
     * restoreState() could be called in RENDER_RESPONSE phase.
     * 
     * This test is active when PSS is enabled.
     * 
     * @throws Exception 
     */
    public void testSaveAndRestorePortletLifecycleWithPss1() throws Exception
    {
        facesContext.getRenderKit().addRenderer("jakarta.faces.Data", "jakarta.faces.Table",new Renderer(){});
        
        List<RowData> model = new ArrayList<RowData>();
        model.add(new RowData("text1","style1"));
        model.add(new RowData("text2","style2"));
        model.add(new RowData("text3","style3"));
        model.add(new RowData("text4","style4"));
        
        //Put on request map to be resolved later
        request.setAttribute("list", model);
        
        UIViewRoot root = facesContext.getViewRoot();
        createSimpleTable(root);
        UIData table = (UIData) root.getChildren().get(0);
        UIColumn column = (UIColumn) table.getChildren().get(0);
        UIInput text = (UIInput) column.getChildren().get(0);
        
        //Simulate markInitialState call.
        facesContext.getAttributes().put(StateManager.IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
        root.markInitialState();
        table.markInitialState();
        column.markInitialState();
        text.markInitialState();
        facesContext.getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);        
        
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        
        //Check the value expressions are working and change the component state 
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals(rowData.getText(), text.getValue());
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
        Object restoredState = (Object) ois.readObject();
        oos.close();
        ois.close();
        
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        facesContext.setViewRoot(new UIViewRoot());
        root = facesContext.getViewRoot();
        root.setRenderKitId("HTML_BASIC");
        
        createSimpleTable(root);
        table = (UIData) root.getChildren().get(0);
        column = (UIColumn) table.getChildren().get(0);
        text = (UIInput) column.getChildren().get(0);
        
        //Simulate markInitialState call.
        facesContext.getAttributes().put(StateManager.IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
        root.markInitialState();
        table.markInitialState();
        column.markInitialState();
        text.markInitialState();
        facesContext.getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);        
        
        table.restoreState(facesContext, restoredState);

        //Check the values were not lost
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals("value"+(i+1), text.getSubmittedValue());
            //assertEquals(model.get(i).getStyle(), text.getAttributes().get("style"));
        }
        
    }
    
    /**
     * Test state save and restore cycle taking in consideration portlet case.
     * 
     * In portlets, saveState() could be called on INVOKE_APPLICATION phase and
     * restoreState() could be called in RENDER_RESPONSE phase.
     * 
     * This test is active when PSS is enabled.
     * 
     * @throws Exception 
     */
    public void testSaveAndRestorePortletLifecycleWithPss2() throws Exception
    {
        facesContext.getRenderKit().addRenderer("jakarta.faces.Data", "jakarta.faces.Table",new Renderer(){});
        
        List<RowData> model = new ArrayList<RowData>();
        model.add(new RowData("text1","style1"));
        model.add(new RowData("text2","style2"));
        model.add(new RowData("text3","style3"));
        model.add(new RowData("text4","style4"));
        
        //Put on request map to be resolved later
        request.setAttribute("list", model);
        
        UIViewRoot root = facesContext.getViewRoot();
        createSimpleTable(root, true);
        UIData table = (UIData) root.getChildren().get(0);
        UIColumn column = (UIColumn) table.getChildren().get(0);
        UIInput text = (UIInput) column.getChildren().get(0);
        
        //Simulate markInitialState call.
        facesContext.getAttributes().put(StateManager.IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
        root.markInitialState();
        table.markInitialState();
        column.markInitialState();
        text.markInitialState();
        facesContext.getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);        
        
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        
        //Check the value expressions are working and change the component state 
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals(rowData.getText(), text.getValue());
            text.setSubmittedValue("value"+(i+1));
            text.getTransientStateHelper().putTransient("key", "value"+(i+1));
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
        Object restoredState = (Object) ois.readObject();
        oos.close();
        ois.close();
        
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        facesContext.setViewRoot(new UIViewRoot());
        root = facesContext.getViewRoot();
        root.setRenderKitId("HTML_BASIC");
        
        createSimpleTable(root, true);
        table = (UIData) root.getChildren().get(0);
        column = (UIColumn) table.getChildren().get(0);
        text = (UIInput) column.getChildren().get(0);
        
        //Simulate markInitialState call.
        facesContext.getAttributes().put(StateManager.IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
        root.markInitialState();
        table.markInitialState();
        column.markInitialState();
        text.markInitialState();
        facesContext.getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);        
        
        table.restoreState(facesContext, restoredState);

        //Check the values were not lost
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals("value"+(i+1), text.getSubmittedValue());
            Assert.assertEquals("value"+(i+1), text.getTransientStateHelper().getTransient("key"));
            //assertEquals(model.get(i).getStyle(), text.getAttributes().get("style"));
        }
        
    }

}
