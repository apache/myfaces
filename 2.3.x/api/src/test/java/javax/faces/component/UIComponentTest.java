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
package javax.faces.component;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import org.apache.myfaces.Assert;
import org.apache.myfaces.TestRunner;
import org.junit.Test;

public class UIComponentTest extends UIComponentTestBase
{
    /**
     * Test method for {@link javax.faces.component.UIComponent#getFacetCount()}.
     */
    @Test
    public void testGetFacetCount() throws Exception
    {
        Method[] methods = {UIComponent.class.getDeclaredMethod("getFacets", (Class<?>[])null)};
        UIComponent component = _mocksControl.createMock(UIComponent.class, methods);
        Map<String, UIComponent> map = new HashMap<String, UIComponent>();
        map.put("xxx1", new UIInput());
        map.put("xxx2", new UIInput());
        map.put("xxx3", new UIInput());
        expect(component.getFacets()).andReturn(map);
        _mocksControl.replay();
        org.junit.Assert.assertEquals(3, component.getFacetCount());
        _mocksControl.verify();

        _mocksControl.reset();
        expect(component.getFacets()).andReturn(null);
        _mocksControl.replay();
        org.junit.Assert.assertEquals(0, component.getFacetCount());
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link javax.faces.component.UIComponent#getContainerClientId(javax.faces.context.FacesContext)}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetContainerClientId() throws Exception
    {
        Collection<Method> mockedMethods = new ArrayList<Method>();
        Class<UIComponent> clazz = UIComponent.class;
        mockedMethods.add(clazz.getDeclaredMethod("getClientId", new Class[] { FacesContext.class }));
        final UIComponent testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods
                .size()]));
        _mocksControl.checkOrder(true);

        Assert.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                testimpl.getContainerClientId(null);
            }
        });

        expect(testimpl.getClientId(same(_facesContext))).andReturn("xyz");
        _mocksControl.replay();
        org.junit.Assert.assertEquals("xyz", testimpl.getContainerClientId(_facesContext));
        _mocksControl.verify();
    }
}
