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

package org.apache.myfaces.el;

import java.util.HashMap;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

import org.apache.cactus.ServletTestCase;

public class ValueBindingImplCactus extends ServletTestCase {
    private FacesContext facesContext;
    private UIViewRoot viewRoot;
    private Application application;
    
    protected void setUp() throws Exception {
        super.setUp();
        FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder
                .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
                .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory
                .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        facesContext = facesContextFactory.getFacesContext(this.config
                .getServletContext(), request, response, lifecycle);
        assertNotNull(facesContext);
        application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        String viewId = "/index.jsp";
        viewRoot = viewHandler.createView(facesContext, viewId);
        viewRoot.setViewId(viewId);
        facesContext.setViewRoot(viewRoot);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.getExpressionString()'
     */
    public void testGetExpressionString() {

    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.getType(FacesContext)'
     */
    public void testGetType() {

    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.getValue(FacesContext)'
     */
    public void testGetValue() {
    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.isReadOnly(FacesContext)'
     */
    public void testIsReadOnly() {

    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.setValue(FacesContext, Object)'
     */
    public void testSetValueSimpleMap() {
        facesContext.getExternalContext().getRequestMap().put("foo", new HashMap());
        ValueBinding binding = application.createValueBinding("#{foo['baz']}");
        Integer value = new Integer(14);
        binding.setValue(facesContext, value);
        assertEquals(14, ((Integer)binding.getValue(facesContext)).intValue());
    }

    public void testSetValueSimpleBeanInRequestMapNoInitialValue() {
        Map map = new HashMap();
        DummyBean bean = new DummyBean(map);
        facesContext.getExternalContext().getRequestMap().put("bean", bean);
        ValueBinding binding = application.createValueBinding("#{bean.map['baz']}");
        Integer value = new Integer(14);
        binding.setValue(facesContext, value);
        assertEquals(14, ((Integer)binding.getValue(facesContext)).intValue());
    }

    public void testSetValueSimpleBeanInRequestMapWithInitialValue() {
        Map map = new HashMap();
        String initialValue = "hello world";
        map.put("baz", initialValue);
        DummyBean bean = new DummyBean(map);
        facesContext.getExternalContext().getRequestMap().put("bean", bean);
        ValueBinding binding = application.createValueBinding("#{bean.map['baz']}");
        assertEquals(initialValue, binding.getValue(facesContext));
        Integer value = new Integer(14);
        binding.setValue(facesContext, value);
        assertEquals(14, ((Integer)binding.getValue(facesContext)).intValue());
    }

    public void testSetValueSimpleBeanInRequestMapWithConverter() {
        Map map = new HashMap();
        DummyBean bean = new DummyBean(map);
        facesContext.getExternalContext().getRequestMap().put("bean", bean);
        ValueBinding binding = application.createValueBinding("#{bean.map['baz']}");
        binding.setValue(facesContext, new Integer(14));
        assertEquals(14, ((Integer)binding.getValue(facesContext)).intValue());
    }

    public void testSetValueSimpleBeanInSessionMap() {
        DummyBean bean = new DummyBean(new HashMap());
        facesContext.getExternalContext().getSessionMap().put("bean", bean);
        ValueBinding binding = application.createValueBinding("#{bean.map['baz']}");
        Integer value = new Integer(14);
        binding.setValue(facesContext, value);
        assertEquals(14, ((Integer)binding.getValue(facesContext)).intValue());
    }
    
    public void setSetIntegerPrimitive() {
        DummyBean bean = new DummyBean(new HashMap());
        facesContext.getExternalContext().getSessionMap().put("bean", bean);
        ValueBinding binding = application.createValueBinding("#{bean.integerPrimitive}");
        Integer value = new Integer(14);
        binding.setValue(facesContext, value);
        assertEquals(14, bean.getIntegerPrimitive());
    }
    
    public void testUnaryNot() {
        facesContext.getExternalContext().getRequestMap().put("trueBean", Boolean.TRUE);
        ValueBinding binding;
        
        // First test #{trueBean} is working well
        binding = application.createValueBinding("#{trueBean}");
        assertTrue( ((Boolean)binding.getValue(facesContext)).booleanValue() );
        
        // Then test #{! trueBean} is false
        binding = application.createValueBinding("#{! trueBean}");
        assertFalse( ((Boolean)binding.getValue(facesContext)).booleanValue() );
    }
    
    public void testNotEmpty() {
        facesContext.getExternalContext().getRequestMap().put("dummyString", "dummy");
        ValueBinding binding;
        
        binding = application.createValueBinding("#{! empty dummyString}");
        assertTrue( ((Boolean)binding.getValue(facesContext)).booleanValue() );
        
        binding = application.createValueBinding("#{! empty undefString}");
        assertFalse( ((Boolean)binding.getValue(facesContext)).booleanValue() );
    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.ValueBindingImpl(Application,
     * String)'
     */
    public void testValueBindingImplApplicationString() {

    }

    /*
     * Test method for 'org.apache.myfaces.el.ValueBindingImpl.toString()'
     */
    public void testToString() {

    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.ValueBindingImpl()'
     */
    public void testValueBindingImpl() {

    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.saveState(FacesContext)'
     */
    public void testSaveState() {

    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.restoreState(FacesContext,
     * Object)'
     */
    public void testRestoreState() {

    }

    /*
     * Test method for 'org.apache.myfaces.el.ValueBindingImpl.isTransient()'
     */
    public void testIsTransient() {

    }

    /*
     * Test method for
     * 'org.apache.myfaces.el.ValueBindingImpl.setTransient(boolean)'
     */
    public void testSetTransient() {

    }

}
