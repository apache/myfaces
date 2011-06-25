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
import java.util.List;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

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
        //System.out.println(fw);
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
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }
}
