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
package org.apache.myfaces.view.facelets.tag.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.Company;
import org.apache.myfaces.view.facelets.bean.Example;
import org.apache.myfaces.view.facelets.component.UIRepeat;
import org.apache.myfaces.view.facelets.util.FastWriter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for UIRepeat.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RepeatTestCase extends FaceletTestCase 
{

    @Test
    public void testRepeat() throws Exception 
    {
        Company c = Example.createCompany();
        facesContext.getExternalContext().getRequestMap().put("company", c);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "repeat.xml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        String content = fw.toString();
        
        int hrIndex = content.indexOf("<dt>HR</dt>");
        Assert.assertNotSame(-1, hrIndex);
        int rdIndex = content.indexOf("<dt>RD</dt>", hrIndex);
        Assert.assertNotSame(-1, rdIndex);

        int empIndex1 = content.indexOf(
            "<dd class=\"3\">Ellen, Sue</dd><dd class=\"4\">Scooner, Mary</dd>", hrIndex);
        Assert.assertNotSame(-1, empIndex1);
        int empIndex2 = content.indexOf(
            "<dd class=\"6\">Burns, Ed</dd><dd class=\"7\">Lubke, Ryan</dd><dd class=\"8\">Kitain, Roger</dd>",
            rdIndex);
        Assert.assertNotSame(-1, empIndex2);
        
        int hrIndex2 = content.indexOf("<li class=\"HR\">HR</li>");
        Assert.assertNotSame(-1, hrIndex2);
        int rdIndex2 = content.indexOf("<li class=\"RD\">RD</li>", hrIndex2);
        Assert.assertNotSame(-1, rdIndex2);
    }
    
    /**
     * Tests UIRepeat.invokeOnComponent() including var and varStatus properties.
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeOnComponent() throws IOException
    {
        // put the values for ui:repeat on the request map
        final String[] repeatValues = new String[]{ "a", "b", "c" };
        externalContext.getRequestMap().put("repeatValues", repeatValues);
        
        // build testUIRepeat.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testUIRepeat.xhtml");
        
        // get the component instances
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        UIComponent outputText = repeat.getChildren().get(0);
        
        // create the ContextCallback
        TestContextCallback callback = new TestContextCallback(facesContext);
        
        // save some values in #{row} and #{status} and test the
        // automatic saving and restoring of them
        final String var = "row";
        final String varStatus = "status";
        final String varValue = "someVarValue";
        final String statusValue = "someStatusValue";
        externalContext.getRequestMap().put(var, varValue);
        externalContext.getRequestMap().put(varStatus, statusValue);
        
        // invokeOnComponent on UIRepeat itself
        String invokeId = "form:repeat";
        Assert.assertTrue(root.invokeOnComponent(facesContext, invokeId, callback));
        Assert.assertEquals(repeat, callback._lastTarget);
        Assert.assertEquals(varValue, callback._rowValue); // previous set varValue
        Assert.assertEquals(statusValue, callback._repeatStatus); // previous set statusValue
        
        // invokeOnComponent on a child of UIRepeat in the first row
        invokeId = "form:repeat:0:outputText";
        Assert.assertTrue(root.invokeOnComponent(facesContext, invokeId, callback));
        Assert.assertEquals(outputText, callback._lastTarget);
        Assert.assertEquals(repeatValues[0], callback._rowValue);
        Assert.assertEquals(0, callback._index);
        Assert.assertEquals(true, callback._first);
        Assert.assertEquals(false, callback._last);
        Assert.assertEquals(true, callback._even);
        
        // invokeOnComponent on a child of UIRepeat in the second row
        invokeId = "form:repeat:1:outputText";
        Assert.assertTrue(root.invokeOnComponent(facesContext, invokeId, callback));
        Assert.assertEquals(outputText, callback._lastTarget);
        Assert.assertEquals(repeatValues[1], callback._rowValue);
        Assert.assertEquals(1, callback._index);
        Assert.assertEquals(false, callback._first);
        Assert.assertEquals(false, callback._last);
        Assert.assertEquals(false, callback._even);
        
        // invokeOnComponent on a child of UIRepeat in the third row
        invokeId = "form:repeat:2:outputText";
        Assert.assertTrue(root.invokeOnComponent(facesContext, invokeId, callback));
        Assert.assertEquals(outputText, callback._lastTarget);
        Assert.assertEquals(repeatValues[2], callback._rowValue);
        Assert.assertEquals(2, callback._index);
        Assert.assertEquals(false, callback._first);
        Assert.assertEquals(true, callback._last);
        Assert.assertEquals(true, callback._even);
        
        // invokeOnComponent on a child of UIRepeat with invalid row (-1)
        invokeId = "form:repeat:outputText";
        Assert.assertTrue(root.invokeOnComponent(facesContext, invokeId, callback));
        
        // after all these calls to invokeOnComponent, row and status still
        // have to be the same like before
        Assert.assertEquals(varValue, externalContext.getRequestMap().get(var));
        Assert.assertEquals(statusValue, externalContext.getRequestMap().get(varStatus));
        
        // remove the values from the request map
        externalContext.getRequestMap().remove("repeatValues");
        externalContext.getRequestMap().remove(var);
        externalContext.getRequestMap().remove(varStatus);
    }
    
    /**
     * ContextCallback to test invokeOnComponent() including var and varStatus properties.
     * @author Jakob Korherr
     */
    private static class TestContextCallback implements ContextCallback
    {
        
        private UIComponent _lastTarget;
        private Object _rowValue;
        private Object _repeatStatus;
        private Object _index;
        private Object _first, _last, _even;
        private ValueExpression _rowValueExpression;
        private ValueExpression _statusValueExpression;
        private ValueExpression _indexValueExpression;
        private ValueExpression _firstValueExpression;
        private ValueExpression _lastValueExpression;
        private ValueExpression _evenValueExpression;

        public TestContextCallback(FacesContext context)
        {
            _rowValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{row}", Object.class);
            _statusValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{status}", Object.class);
            _indexValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{status.index}", Object.class);
            _firstValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{status.first}", Object.class);
            _lastValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{status.last}", Object.class);
            _evenValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{status.even}", Object.class);
        }
        
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            _lastTarget = target;
            
            // evaluate ValueExpressions
            ELContext elCtx = context.getELContext();
            _rowValue = _rowValueExpression.getValue(elCtx);
            _repeatStatus = _statusValueExpression.getValue(elCtx);
            try
            {
                _index = _indexValueExpression.getValue(elCtx);
                _first = _firstValueExpression.getValue(elCtx);
                _last = _lastValueExpression.getValue(elCtx);
                _even = _evenValueExpression.getValue(elCtx);
            }
            catch (ELException ele)
            {
                // repeatStatus is some other object, so these values are all null
                _index = _first = _last = _even = null;
            }
        }
        
    }
    
    /**
     * Tests UIRepeat.visitTree().
     * @throws IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testVisitTree() throws IOException
    {
     // put the values for ui:repeat on the request map
        final String[] repeatValues = new String[]{ "a", "b", "c" };
        externalContext.getRequestMap().put("repeatValues", repeatValues);
        
        // build testUIRepeat.xhtml
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testUIRepeat.xhtml");
        
        // get the component instances
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        
        // create the VisitCallback
        TestVisitCallback testVisitCallback = new TestVisitCallback(facesContext, repeatValues);
        
        // save some values in #{row} and #{status} and test the
        // automatic saving and restoring of them
        final String var = "row";
        final String varStatus = "status";
        final String varValue = "someVarValue";
        final String statusValue = "someStatusValue";
        externalContext.getRequestMap().put(var, varValue);
        externalContext.getRequestMap().put(varStatus, statusValue);
        
        // perform visit
        repeat.visitTree(VisitContext.createVisitContext(facesContext), testVisitCallback);
        
        // created expected List
        List<String> expectedClientIds = new ArrayList<String>();
        expectedClientIds.add("form:repeat");
        expectedClientIds.add("form:repeat:0:outputText");
        expectedClientIds.add("form:repeat:1:outputText");
        expectedClientIds.add("form:repeat:2:outputText");
        
        // see if we got the expected result
        Assert.assertEquals(expectedClientIds, testVisitCallback._visitedClientIds);
        
        // after the tree visit, row and status still
        // have to be the same like before
        Assert.assertEquals(varValue, externalContext.getRequestMap().get(var));
        Assert.assertEquals(statusValue, externalContext.getRequestMap().get(varStatus));
        
        // remove the values from the request map
        externalContext.getRequestMap().remove("repeatValues");
        externalContext.getRequestMap().remove(var);
        externalContext.getRequestMap().remove(varStatus);
    }

    /**
     * VisitCallback to test visitTree().
     * @author Jakob Korherr
     */
    private static class TestVisitCallback implements VisitCallback
    {
        
        private List<String> _visitedClientIds;
        private ValueExpression _rowValueExpression;
        private ValueExpression _indexValueExpression;
        private String[] _repeatValues;
        
        public TestVisitCallback(FacesContext context, String[] repeatValues)
        {
            _repeatValues = repeatValues;
            _visitedClientIds = new ArrayList<String>();
            _rowValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{row}", Object.class);
            _indexValueExpression = context.getApplication().getExpressionFactory()
                    .createValueExpression(context.getELContext(), "#{status.index}", Object.class);
        }

        public VisitResult visit(VisitContext context, UIComponent target)
        {
            final String clientId = target.getClientId(context.getFacesContext());
            if (_visitedClientIds.contains(clientId))
            {
                Assert.fail("Component with clientId " + clientId + " visited twice!");
            }
            else
            {
                _visitedClientIds.add(clientId);
                
                if (!(target instanceof UIRepeat))
                {
                    // test #{row} and #{status.index}
                    ELContext elCtx = context.getFacesContext().getELContext();
                    
                    Object indexObject = _indexValueExpression.getValue(elCtx);
                    // indexObject has to be an Integer
                    Assert.assertTrue(indexObject instanceof Integer);
                    Integer index = (Integer) indexObject;
                    
                    // the index has to be part of the clientId
                    Assert.assertTrue(clientId.contains("" + index));
                    
                    Object rowValue = _rowValueExpression.getValue(elCtx);
                    // #{row} has to be the repeatValue for the current index
                    Assert.assertEquals(_repeatValues[index], rowValue);
                }
            }
            
            return VisitResult.ACCEPT;
        }
        
    }
    
    @Test
    public void testRepeatOffset() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        String content = fw.toString();
        
        // offset="2" size="1" should render only 1 row
        int itemIndex1 = content.indexOf("B1");
        Assert.assertEquals(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertEquals(-1, itemIndex2);
        
        String item1 = "B3";
        int itemIndex3 = content.indexOf(item1);
        Assert.assertNotSame(-1, itemIndex3);
        String item2 = "B4";
        // the second item should not be there
        Assert.assertEquals(-1, content.indexOf(item2, itemIndex1+2));
    }
    
    @Test
    public void testRepeatOffset_0() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat0");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertNotSame(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3");
        Assert.assertEquals(-1, itemIndex3);
    }
    
    @Test
    public void testRepeatBegin_0() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeatbegin0");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertNotSame(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3");
        Assert.assertNotSame(-1, itemIndex3);
    }

    
    @Test
    public void testRepeatOffset_0_7() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat0_7");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertNotSame(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2", itemIndex1);
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3", itemIndex2);
        Assert.assertNotSame(-1, itemIndex3);
        int itemIndex4 = content.indexOf("B4", itemIndex3);
        Assert.assertNotSame(-1, itemIndex4);
        int itemIndex5 = content.indexOf("B5", itemIndex4);
        Assert.assertNotSame(-1, itemIndex5);
        int itemIndex6 = content.indexOf("B6", itemIndex5);
        Assert.assertNotSame(-1, itemIndex6);
        int itemIndex7 = content.indexOf("B7", itemIndex6);
        Assert.assertNotSame(-1, itemIndex7);
    }
    
    @Test
    public void testRepeatBegin_0_7() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeatbegin0_7");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertNotSame(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2", itemIndex1);
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3", itemIndex2);
        Assert.assertNotSame(-1, itemIndex3);
        int itemIndex4 = content.indexOf("B4", itemIndex3);
        Assert.assertNotSame(-1, itemIndex4);
        int itemIndex5 = content.indexOf("B5", itemIndex4);
        Assert.assertNotSame(-1, itemIndex5);
        int itemIndex6 = content.indexOf("B6", itemIndex5);
        Assert.assertNotSame(-1, itemIndex6);
        int itemIndex7 = content.indexOf("B7", itemIndex6);
        Assert.assertNotSame(-1, itemIndex7);
    }
    
    @Test
    public void testRepeatOffset_0_8() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat0_8");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        try
        {
            repeat.encodeAll(facesContext);
            Assert.fail();
        }
        catch(FacesException e)
        {
            // size cannot be greater than collection size
        }
    }
    
    @Test
    public void testRepeatBegin_0_8() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeatbegin0_8");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        try
        {
            repeat.encodeAll(facesContext);
            Assert.fail();
        }
        catch(FacesException e)
        {
            // size cannot be greater than collection size
        }
    }
    
    @Test
    public void testRepeatOffset_1() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat1");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertEquals(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3", itemIndex2);
        Assert.assertNotSame(-1, itemIndex3);
        int itemIndex4 = content.indexOf("B4", itemIndex3);
        Assert.assertNotSame(-1, itemIndex4);
        int itemIndex5 = content.indexOf("B5", itemIndex4);
        Assert.assertEquals(-1, itemIndex5);
    }
    
    @Test
    public void testRepeatBegin_1() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeatbegin1");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertEquals(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3", itemIndex2);
        Assert.assertNotSame(-1, itemIndex3);
        int itemIndex4 = content.indexOf("B4", itemIndex3);
        Assert.assertNotSame(-1, itemIndex4);
        int itemIndex5 = content.indexOf("B5", itemIndex4);
        Assert.assertEquals(-1, itemIndex5);
    }
    
    @Test
    public void testRepeatOffset_1_7() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat1_7");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertEquals(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3", itemIndex2);
        Assert.assertNotSame(-1, itemIndex3);
        int itemIndex4 = content.indexOf("B4", itemIndex3);
        Assert.assertNotSame(-1, itemIndex4);
        int itemIndex5 = content.indexOf("B5", itemIndex4);
        Assert.assertNotSame(-1, itemIndex5);
        int itemIndex6 = content.indexOf("B6", itemIndex5);
        Assert.assertNotSame(-1, itemIndex6);
        int itemIndex7 = content.indexOf("B7", itemIndex6);
        Assert.assertNotSame(-1, itemIndex7);
    }
    
    @Test
    public void testRepeatBegin_1_7() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeatbegin1_7");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertEquals(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertNotSame(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3", itemIndex2);
        Assert.assertNotSame(-1, itemIndex3);
        int itemIndex4 = content.indexOf("B4", itemIndex3);
        Assert.assertNotSame(-1, itemIndex4);
        int itemIndex5 = content.indexOf("B5", itemIndex4);
        Assert.assertNotSame(-1, itemIndex5);
        int itemIndex6 = content.indexOf("B6", itemIndex5);
        Assert.assertNotSame(-1, itemIndex6);
        int itemIndex7 = content.indexOf("B7", itemIndex6);
        Assert.assertNotSame(-1, itemIndex7);
    }
    
    @Test
    public void testRepeatOffset_1_8() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat1_8");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        try
        {
            repeat.encodeAll(facesContext);
            Assert.fail();
        }
        catch(FacesException e)
        {
            // size cannot be greater than collection size
        }
    }
    
    @Test
    public void testRepeatBegin_1_8() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeatbegin1_8");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        try
        {
            repeat.encodeAll(facesContext);
            Assert.fail();
        }
        catch(FacesException e)
        {
            // size cannot be greater than collection size
        }
    }

    @Test
    public void testRepeatOffset2() throws Exception 
    {
        final String[] repeatValues = new String[] {"B1", "B2", "B3", "B4", "B5", "B6", "B7"};
        facesContext.getExternalContext().getRequestMap().put("repeatValues", repeatValues);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_offset2.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        int itemIndex1 = content.indexOf("B1");
        Assert.assertEquals(-1, itemIndex1);
        int itemIndex2 = content.indexOf("B2");
        Assert.assertEquals(-1, itemIndex2);
        int itemIndex3 = content.indexOf("B3");
        Assert.assertNotSame(-1, itemIndex3);
        int itemIndex4 = content.indexOf("B4", itemIndex3);
        Assert.assertNotSame(-1, itemIndex4);
        int itemIndex5 = content.indexOf("B5", itemIndex4);
        Assert.assertNotSame(-1, itemIndex5);
        int itemIndex6 = content.indexOf("B6", itemIndex5);
        Assert.assertNotSame(-1, itemIndex6);
        int itemIndex7 = content.indexOf("B7", itemIndex6);
        Assert.assertNotSame(-1, itemIndex7);
        
        //System.out.println(fw);
    }
    
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeOnComponentBeginEnd() throws IOException
    {                
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testUIRepeatBeginEnd.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        Assert.assertTrue(content.contains("Hello 1"));
        Assert.assertTrue(content.contains("Hello 2"));
        Assert.assertTrue(content.contains("Hello 3"));
        
        Assert.assertFalse(content.contains("Hello 0"));
        Assert.assertFalse(content.contains("Hello 4"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeOnNullModel() throws IOException
    {                
        final List<String> modelValues = null;
        final List<String> loadedModelValues = Arrays.asList("Claire", "Michael");
        facesContext.getExternalContext().getRequestMap().put("modelValues", modelValues);
        facesContext.getExternalContext().getRequestMap().put("loadedModelValues", loadedModelValues);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testUIRepeatEmpty.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        Assert.assertFalse(content.contains("Hello "));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeOnEmptyList() throws IOException
    {                
        final List<String> modelValues = Collections.emptyList();
        final List<String> loadedModelValues = Arrays.asList("Claire", "Michael");
        facesContext.getExternalContext().getRequestMap().put("modelValues", modelValues);
        facesContext.getExternalContext().getRequestMap().put("loadedModelValues", loadedModelValues);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testUIRepeatEmpty.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        Assert.assertFalse(content.contains("Hello "));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeModelStep_1() throws IOException
    {                
        final List<String> values = Arrays.asList("User #0", "User #1", "User #2", "User #3", "User #4", "User #5", "User #6", "User #7", "User #8", "User #9", "User #10");
        IterationBean iterationBean = new IterationBean(1, 7, 1, values);
        facesContext.getExternalContext().getRequestMap().put("iterationBean", iterationBean);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_model_step.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        Assert.assertTrue(content.contains("User #1"));
        Assert.assertTrue(content.contains("User #2"));
        Assert.assertTrue(content.contains("User #3"));
        Assert.assertTrue(content.contains("User #4"));
        Assert.assertTrue(content.contains("User #5"));
        Assert.assertTrue(content.contains("User #6"));
        Assert.assertTrue(content.contains("User #7"));
        Assert.assertFalse(content.contains("User #0"));
        Assert.assertFalse(content.contains("User #8"));
        Assert.assertFalse(content.contains("User #9"));
        Assert.assertFalse(content.contains("User #10"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeModelStep_2() throws IOException
    {                
        final List<String> values = Arrays.asList("User #0", "User #1", "User #2", "User #3", "User #4", "User #5", "User #6", "User #7", "User #8", "User #9", "User #10");
        IterationBean iterationBean = new IterationBean(1, 7, 2, values);
        facesContext.getExternalContext().getRequestMap().put("iterationBean", iterationBean);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_model_step.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        Assert.assertTrue(content.contains("User #1"));
        Assert.assertTrue(content.contains("User #3"));
        Assert.assertTrue(content.contains("User #5"));
        Assert.assertTrue(content.contains("User #7"));
        Assert.assertFalse(content.contains("User #0"));
        Assert.assertFalse(content.contains("User #2"));
        Assert.assertFalse(content.contains("User #4"));
        Assert.assertFalse(content.contains("User #6"));
        Assert.assertFalse(content.contains("User #8"));
        Assert.assertFalse(content.contains("User #9"));
        Assert.assertFalse(content.contains("User #10"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeModelStep_3() throws IOException
    {                
        final List<String> values = Arrays.asList("User #0", "User #1", "User #2", "User #3", "User #4", "User #5", "User #6", "User #7", "User #8", "User #9", "User #10");
        IterationBean iterationBean = new IterationBean(2, 7, 3, values);
        facesContext.getExternalContext().getRequestMap().put("iterationBean", iterationBean);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_model_step.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        Assert.assertTrue(content.contains("User #2"));
        Assert.assertTrue(content.contains("User #5"));
        Assert.assertFalse(content.contains("User #0"));
        Assert.assertFalse(content.contains("User #1"));
        Assert.assertFalse(content.contains("User #3"));
        Assert.assertFalse(content.contains("User #4"));
        Assert.assertFalse(content.contains("User #6"));
        Assert.assertFalse(content.contains("User #7"));
        Assert.assertFalse(content.contains("User #8"));
        Assert.assertFalse(content.contains("User #9"));
        Assert.assertFalse(content.contains("User #10"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeModelChangeStep_3() throws IOException
    {                
        final List<String> values = Arrays.asList("User #0", "User #1", "User #2", "User #3", "User #4", "User #5", "User #6", "User #7", "User #8", "User #9", "User #10");
        IterationBean iterationBean = new IterationBean(2, 7, 3, values);
        facesContext.getExternalContext().getRequestMap().put("iterationBean", iterationBean);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ui_repeat_model_step.xhtml");
        
        UIRepeat repeat = (UIRepeat) root.findComponent("form:repeat");
        Assert.assertNotNull(repeat);
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        
        String content = fw.toString();

        Assert.assertTrue(content.contains("User #2"));
        Assert.assertTrue(content.contains("User #5"));

        Assert.assertFalse(content.contains("User #0"));
        Assert.assertFalse(content.contains("User #1"));
        Assert.assertFalse(content.contains("User #3"));
        Assert.assertFalse(content.contains("User #4"));
        Assert.assertFalse(content.contains("User #6"));
        Assert.assertFalse(content.contains("User #7"));
        Assert.assertFalse(content.contains("User #8"));
        Assert.assertFalse(content.contains("User #9"));
        Assert.assertFalse(content.contains("User #10"));
        
        iterationBean.setStep(2);
        fw = new FastWriter();
        rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        
        repeat.encodeAll(facesContext);
        content = fw.toString();
        
        Assert.assertTrue(content.contains("User #2"));
        Assert.assertTrue(content.contains("User #4"));
        Assert.assertTrue(content.contains("User #6"));
        
        Assert.assertFalse(content.contains("User #0"));
        Assert.assertFalse(content.contains("User #1"));
        Assert.assertFalse(content.contains("User #3"));
        Assert.assertFalse(content.contains("User #5"));
        Assert.assertFalse(content.contains("User #7"));
        Assert.assertFalse(content.contains("User #8"));
        Assert.assertFalse(content.contains("User #9"));
        Assert.assertFalse(content.contains("User #10"));
    }
    
    public class IterationBean {
        private int begin;
        private int end;
        private int step;
        private List<String> values;
        public IterationBean(int begin, int end, int step, List<String> values) {
            this.begin = begin;
            this.end = end;
            this.step = step;
            this.values = values;
        }
        public int getBegin() {
            return begin;
        }
        public int getEnd() {
            return end;
        }
        public int getStep() {
            return step;
        }
        public void setStep(int step) {
        	this.step = step;
        }
        public List<String> getValues() {
            return values;
        }
    }
}
