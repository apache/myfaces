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

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:42:55
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentBaseGetClientIdTest extends AbstractComponentTest
{
    public UIComponentBaseGetClientIdTest(String arg0)
    {
        super(arg0);
    }

    protected UIComponentBase _testImpl;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _testImpl = new UIOutput();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

      public void testNullFacesContext() throws Exception
    {
        try
        {
            _testImpl.getClientId(null);
            fail();
        }
        catch(NullPointerException e)
        {
            
        }
        catch(Exception e)
        {
            fail();
        }
    }

    public void testWithoutParentAndNoRenderer() throws Exception
    {
        String expectedClientId = "testId";
        _testImpl.setId(expectedClientId);
        
        assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
        assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
    }

    public void testWithRenderer() throws Exception
    {
        String id = "testId";
        final String expectedClientId = "convertedClientId";
        _testImpl = new UIOutput()
        {
            protected Renderer getRenderer(FacesContext facesContext)
            {
                return new Renderer()
                {
                    public String convertClientId(FacesContext context, String clientId)
                    {
                        return expectedClientId;
                    }
                };
            }
        };
        _testImpl.setId(id);
        assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
        assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
    }

    public void testWithParentNamingContainer() throws Exception
    {
        String id = "testId";
        String containerClientId = "containerClientId";
        String expectedClientId = containerClientId + NamingContainer.SEPARATOR_CHAR + id;
        
        UIComponent namingContainer = new UINamingContainer();
        UIComponent parent = new UIPanel();
        
        namingContainer.setId(containerClientId);
        namingContainer.getChildren().add(parent);
        parent.setId("parent");
        parent.getChildren().add(_testImpl);
        _testImpl.setId(id);

        assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
        assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
    }

    public void testWithParentNamingContainerChanging() throws Exception
    {
        String id = "testId";
        String containerClientId = "containerClientId";
        
        for (int i = 0; i < 10; i++)
        {
            final int j = i;
            UIComponent namingContainer = new UINamingContainer()
            {
                @Override
                public String getContainerClientId(FacesContext ctx)
                {
                    return super.getContainerClientId(ctx) + j;
                }
                
            };
            UIComponent parent = new UIPanel();
            _testImpl.setId(id);
            String expectedClientId = containerClientId + i + NamingContainer.SEPARATOR_CHAR + id;
            
            namingContainer.setId(containerClientId);
            namingContainer.getChildren().add(parent);
            parent.setId("parent");
            parent.getChildren().add(_testImpl);

            assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
            assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
            parent.getChildren().remove(_testImpl);
        }
    }

    public void testWithoutId() throws Exception
    {
        UIViewRoot viewRoot = facesContext.getViewRoot();
        assertNotNull(viewRoot.createUniqueId());
        assertNotNull(_testImpl.getClientId());
        assertNotNull(_testImpl.getId());
    }

    public void testWithoutIdAndNoUIViewRoot() throws Exception
    {
        facesContext.setViewRoot(null);
        try
        {
            _testImpl.getClientId(facesContext);
            fail();
        }
        catch(FacesException e)
        {
            
        }
        catch(Exception e)
        {
            fail();
        }
    }
}
