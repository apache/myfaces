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

import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:42:55
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentBaseGetClientIdTest extends AbstractComponentTest
{
    protected UIComponentBase _testImpl;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _testImpl = new UIOutput();
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testNullFacesContext() throws Exception
    {
        try
        {
            _testImpl.getClientId(null);
            Assert.fail();
        }
        catch(NullPointerException e)
        {
            
        }
        catch(Exception e)
        {
            Assert.fail();
        }
    }

    @Test
    public void testWithoutParentAndNoRenderer() throws Exception
    {
        String expectedClientId = "testId";
        _testImpl.setId(expectedClientId);
        
        Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
        Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
    }

    @Test
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
        Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
        Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
    }

    @Test
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

        Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
        Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
    }

    @Test
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

            Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
            Assert.assertEquals(expectedClientId, _testImpl.getClientId(facesContext));
            parent.getChildren().remove(_testImpl);
        }
    }

    @Test
    public void testWithoutId() throws Exception
    {
        UIViewRoot viewRoot = facesContext.getViewRoot();
        Assert.assertNotNull(viewRoot.createUniqueId());
        Assert.assertNotNull(_testImpl.getClientId());
        Assert.assertNotNull(_testImpl.getId());
    }

    @Test
    public void testWithoutIdAndNoUIViewRoot() throws Exception
    {
        facesContext.setViewRoot(null);
        try
        {
            _testImpl.getClientId(facesContext);
            Assert.fail();
        }
        catch(FacesException e)
        {
            
        }
        catch(Exception e)
        {
            Assert.fail();
        }
    }
}
