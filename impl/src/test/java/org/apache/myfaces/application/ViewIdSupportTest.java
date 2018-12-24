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
package org.apache.myfaces.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;

public class ViewIdSupportTest extends AbstractJsfTestCase
{
    private final String filePath = this.getDirectory();
    private ViewIdSupport viewHandlerSupport;
    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        URI context = this.getContext();
        super.setUpServletObjects();
        
        //setup document root to correctly resolve viewExists
        servletContext.setDocumentRoot(new File(context));
    }
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        viewHandlerSupport = ViewIdSupport.getInstance(facesContext);
    }
    
    private String getDirectory()
    {
        return this.getClass().getName().substring(0,
                this.getClass().getName().lastIndexOf('.')).replace('.', '/')
                + "/";
    }
    
    protected URI getContext()
    {
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL url = cl.getResource(this.filePath);
            if (url == null)
            {
                throw new FileNotFoundException(cl.getResource("").getFile()
                        + this.filePath + " was not found");
            }
            else
            {
                return new URI(url.toString());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error Initializing Context", e);
        }
    }
    
    @Test
    public void testDeriveViewId1() throws Exception
    {
        request.setPathElements("/testwebapp", "/view1.jsf", null , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "/view1.jsf");
        
        Assert.assertNotNull(derivedViewId);
    }
    
    @Test
    public void testDeriveViewId2() throws Exception
    {        
        request.setPathElements("/testwebapp", "/faces", "/view1.jsp" , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "/view1.jsp");
        
        Assert.assertNotNull(derivedViewId);
    }
    
    @Test
    public void testDeriveViewId21() throws Exception
    {        
        request.setPathElements("/testwebapp", "/faces", "/faces/view1.jsp" , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "/faces/view1.jsp");
        
        Assert.assertNotNull(derivedViewId);
    }    
    
    @Test
    public void testDeriveViewId3() throws Exception
    {
        request.setPathElements("/testwebapp", "/view2.jsf", null , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "/view2.jsf");
        
        Assert.assertNotNull(derivedViewId);
    }
    
    @Test
    public void testDeriveViewId4() throws Exception
    {        
        request.setPathElements("/testwebapp", "/faces", "/view2.xhtml" , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "/view2.xhtml");
        
        Assert.assertNotNull(derivedViewId);
    }


    @Test
    public void testDeriveViewId5() throws Exception
    {
        request.setPathElements("/testwebapp", "/noview1.jsf", null , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "/noview1.jsf");
        
        Assert.assertNull(derivedViewId);
    }
    
    /**
     * An empty prefix mapping can occur in Spring environments.
     * @throws Exception
     */
    @Test
    public void testEmptyPrefixMapping() throws Exception
    {        
        request.setPathElements("/testwebapp", "", "/view2.xhtml" , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "/view2.xhtml");
        
        Assert.assertEquals("/view2.xhtml", derivedViewId);
    }
    
    /**
     * An empty prefix mapping can occur in Spring environments.
     * However this should not only return the viewId, but also remove any
     * leading double slashes.
     * @throws Exception
     */
    @Test
    public void testEmptyPrefixMappingDoubleSlash() throws Exception
    {        
        request.setPathElements("/testwebapp", "", "//view2.xhtml" , null);
        
        String derivedViewId = viewHandlerSupport.deriveViewId(facesContext, "//view2.xhtml");
        
        Assert.assertEquals("/view2.xhtml", derivedViewId);
    }

}
