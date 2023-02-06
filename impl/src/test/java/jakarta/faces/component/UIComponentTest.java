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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import org.apache.myfaces.test.MyFacesAsserts;
import org.apache.myfaces.test.TestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UIComponentTest extends UIComponentTestBase
{
    /**
     * Test method for {@link jakarta.faces.component.UIComponent#getFacetCount()}.
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
        Assertions.assertEquals(3, component.getFacetCount());
        _mocksControl.verify();

        _mocksControl.reset();
        expect(component.getFacets()).andReturn(null);
        _mocksControl.replay();
        Assertions.assertEquals(0, component.getFacetCount());
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIComponent#getContainerClientId(jakarta.faces.context.FacesContext)}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetContainerClientId() throws Exception
    {
        Collection<Method> mockedMethods = new ArrayList<Method>();
        Class<UIComponent> clazz = UIComponent.class;
        mockedMethods.add(clazz.getDeclaredMethod("getClientId", FacesContext.class));
        final UIComponent testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods
                .size()]));
        _mocksControl.checkOrder(true);

        MyFacesAsserts.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                testimpl.getContainerClientId(null);
            }
        });

        expect(testimpl.getClientId(same(_facesContext))).andReturn("xyz");
        _mocksControl.replay();
        Assertions.assertEquals("xyz", testimpl.getContainerClientId(_facesContext));
        _mocksControl.verify();
    }
}
