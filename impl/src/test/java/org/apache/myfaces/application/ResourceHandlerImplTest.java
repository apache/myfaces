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

import java.io.InputStream;
import java.net.MalformedURLException;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.faces.application.Resource;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.resource.ClassLoaderResourceLoader;
import org.apache.myfaces.resource.ResourceHandlerCache;
import org.apache.myfaces.resource.ResourceHandlerSupport;
import org.apache.myfaces.resource.ResourceLoader;
import org.apache.myfaces.resource.ResourceMeta;
import org.apache.myfaces.resource.ResourceMetaImpl;
import org.junit.Ignore;
import org.mockito.Mockito;

/**
 * Test cases for org.apache.myfaces.application.ResourceHandlerImpl.
 *
 * @author Jakob Korherr
 */
public class ResourceHandlerImplTest extends AbstractJsfTestCase
{

    private ResourceHandlerImpl resourceHandler;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        resourceHandler = new ResourceHandlerImpl();
        request.setPathElements("/xxx", "/yyy", "/test.xhtml", null);
    }

    @Override
    public void tearDown() throws Exception
    {
        resourceHandler = null;

        super.tearDown();    
    }

    @Test
    public void testCreateResource_ResourceNotNull() throws Exception
    {
        Resource resource = resourceHandler.createResource("testResource.xhtml");

        Assert.assertNotNull(resource);
    }

    @Test
    public void testCreateResource_cacheHonorsLocale() throws Exception
    {
        // setup message bundle to use
        application.setMessageBundle("org/apache/myfaces/application/resourcehandler/messages");

        // get english resource
        application.setDefaultLocale(Locale.ENGLISH);
        Resource resourceEn = resourceHandler.createResource("testResource.xhtml");
        URL urlEn = resourceEn.getURL();

        // get german resource
        application.setDefaultLocale(Locale.GERMAN);
        Resource resourceDe = resourceHandler.createResource("testResource.xhtml");
        URL urlDe = resourceDe.getURL();

        // URLs MUST be different, since there is an english and a german version of the resource
        Assert.assertFalse("Resources must be different", urlEn.equals(urlDe));
    }

    @Test
    public void testDeriveResourceMeta1() throws Exception
    {
        application.setMessageBundle("org/apache/myfaces/application/resourcehandler/messages");
        
        ResourceLoader loader = new ResourceLoader("/resources") {

            @Override
            public String getResourceVersion(String path)
            {
                return null;
            }

            @Override
            public String getLibraryVersion(String path)
            {
                return null;
            }

            public URL getResourceURL(String resourceId)
            {
                try
                {
                    return new URL("file://"+resourceId);
                }
                catch (MalformedURLException ex)
                {
                    Logger.getLogger(ResourceHandlerImplTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
            
            @Override
            public URL getResourceURL(ResourceMeta resourceMeta)
            {
                try
                {
                    return new URL("file://"+resourceMeta.getResourceIdentifier());
                }
                catch (MalformedURLException ex)
                {
                    Logger.getLogger(ResourceHandlerImplTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            public InputStream getResourceInputStream(ResourceMeta resourceMeta)
            {
                return null;
            }

            @Override
            public ResourceMeta createResourceMeta(String prefix, String libraryName, 
                String libraryVersion, String resourceName, String resourceVersion)
            {
                return new ResourceMetaImpl(prefix, libraryName, 
                    libraryVersion, resourceName, resourceVersion);
            }

            @Override
            public boolean libraryExists(String libraryName)
            {
                return true;
            }
        };
        
        application.setDefaultLocale(Locale.ENGLISH);
        ResourceMeta resource = resourceHandler.deriveResourceMeta(facesContext, loader, "en/mylib/1_0_2/myres.js/1_3.js");
        Assert.assertNotNull(resource);
        Assert.assertEquals("en", resource.getLocalePrefix());
        Assert.assertEquals("mylib", resource.getLibraryName());
        Assert.assertEquals("1_0_2", resource.getLibraryVersion());
        Assert.assertEquals("myres.js", resource.getResourceName());
        Assert.assertEquals("1_3", resource.getResourceVersion());
        
        resource = resourceHandler.deriveResourceMeta(facesContext, loader, "en/mylib/1_0_2/myres.js");
        Assert.assertNotNull(resource);
        Assert.assertEquals("en", resource.getLocalePrefix());
        Assert.assertEquals("mylib", resource.getLibraryName());
        Assert.assertEquals("1_0_2", resource.getLibraryVersion());
        Assert.assertEquals("myres.js", resource.getResourceName());
        Assert.assertNull(resource.getResourceVersion());        
        
        resource = resourceHandler.deriveResourceMeta(facesContext, loader, "en/mylib/myres.js/1_3.js");
        Assert.assertNotNull(resource);
        Assert.assertEquals("en", resource.getLocalePrefix());
        Assert.assertEquals("mylib", resource.getLibraryName());
        Assert.assertNull(resource.getLibraryVersion());
        Assert.assertEquals("myres.js", resource.getResourceName());
        Assert.assertEquals("1_3", resource.getResourceVersion());

        resource = resourceHandler.deriveResourceMeta(facesContext, loader, "en/mylib/myres.js");
        Assert.assertNotNull(resource);
        Assert.assertEquals("en", resource.getLocalePrefix());
        Assert.assertEquals("mylib", resource.getLibraryName());
        Assert.assertEquals("myres.js", resource.getResourceName());
        Assert.assertNull(resource.getLibraryVersion());
        Assert.assertNull(resource.getResourceVersion());

        resource = resourceHandler.deriveResourceMeta(facesContext, loader, "en/myres.js");
        Assert.assertNotNull(resource);
        Assert.assertNull(resource.getLibraryName());
        Assert.assertNull(resource.getLibraryVersion());
        Assert.assertNull(resource.getResourceVersion());
        Assert.assertEquals("en", resource.getLocalePrefix());
        Assert.assertEquals("myres.js", resource.getResourceName());
        
        resource = resourceHandler.deriveResourceMeta(facesContext, loader, "mylib/myres.js");
        Assert.assertNotNull(resource);
        Assert.assertNull(resource.getLocalePrefix());
        Assert.assertNull(resource.getLibraryVersion());
        Assert.assertNull(resource.getResourceVersion());
        Assert.assertEquals("mylib", resource.getLibraryName());
        Assert.assertEquals("myres.js", resource.getResourceName());
        
        resource = resourceHandler.deriveResourceMeta(facesContext, loader, "myres.js");
        Assert.assertNotNull(resource);
        Assert.assertNull(resource.getLocalePrefix());
        Assert.assertNull(resource.getLibraryName());
        Assert.assertNull(resource.getLibraryVersion());
        Assert.assertNull(resource.getResourceVersion());        
        Assert.assertEquals("myres.js", resource.getResourceName());
    }

    @Test
    public void testCreateResourceNullResourceName() throws Exception
    {
        boolean didNPEOccur = false;
        try 
        {
            resourceHandler.createResource(null);
        } catch (NullPointerException e)
        {
            didNPEOccur = true;
        }
        
        Assert.assertTrue(didNPEOccur);
    }

    @Test
    public void testCache()
    {
        ResourceLoader loader = Mockito.spy(new ClassLoaderResourceLoader(null));
        Mockito.when(loader.resourceExists(Mockito.any())).thenReturn(true);

        ResourceHandlerCache cache = Mockito.spy(new ResourceHandlerCache());   
        
        ResourceHandlerSupport support = Mockito.spy(new DefaultResourceHandlerSupport());
        Mockito.when(support.getResourceLoaders()).thenReturn(new ResourceLoader[] { loader });
        
        resourceHandler = Mockito.spy(resourceHandler);
        Mockito.when(resourceHandler.getResourceHandlerCache()).thenReturn(cache);
        Mockito.when(resourceHandler.getResourceHandlerSupport()).thenReturn(support);
        
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");

        Mockito.verify(cache, Mockito.times(4)).getResource(
                Mockito.eq("test.png"), Mockito.eq("test"), Mockito.eq("test"), Mockito.any());
        Mockito.verify(cache, Mockito.times(1)).putResource(
                Mockito.eq("test.png"), Mockito.eq("test"), Mockito.eq("test"), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(loader, Mockito.times(1)).createResourceMeta(
                Mockito.any(), Mockito.eq("test"), Mockito.any(), Mockito.eq("test.png"), Mockito.any());
    }
    
    @Test
    public void testDisabledCache()
    {
        ResourceLoader loader = Mockito.spy(new ClassLoaderResourceLoader(null));
        Mockito.when(loader.resourceExists(Mockito.any())).thenReturn(true);

        ResourceHandlerCache cache = Mockito.spy(new ResourceHandlerCache());   
        Mockito.when(cache.isResourceHandlerCacheEnabled()).thenReturn(false);
        
        ResourceHandlerSupport support = Mockito.spy(new DefaultResourceHandlerSupport());
        Mockito.when(support.getResourceLoaders()).thenReturn(new ResourceLoader[] { loader });
        
        resourceHandler = Mockito.spy(resourceHandler);
        Mockito.when(resourceHandler.getResourceHandlerCache()).thenReturn(cache);
        Mockito.when(resourceHandler.getResourceHandlerSupport()).thenReturn(support);
        
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");

        Mockito.verify(cache, Mockito.times(4)).getResource(
                Mockito.eq("test.png"), Mockito.eq("test"), Mockito.eq("test"), Mockito.any());
        Mockito.verify(cache, Mockito.times(4)).putResource(
                Mockito.eq("test.png"), Mockito.eq("test"), Mockito.eq("test"), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(loader, Mockito.times(4)).createResourceMeta(
                Mockito.any(), Mockito.eq("test"), Mockito.any(), Mockito.eq("test.png"), Mockito.any());
    }

    @Test
    public void testResourceExistsCache()
    {
        ResourceLoader loader = Mockito.spy(new ClassLoaderResourceLoader(null));

        ResourceHandlerCache cache = Mockito.spy(new ResourceHandlerCache());   
        
        ResourceHandlerSupport support = Mockito.spy(new DefaultResourceHandlerSupport());
        Mockito.when(support.getResourceLoaders()).thenReturn(new ResourceLoader[] { loader });
        
        resourceHandler = Mockito.spy(resourceHandler);
        Mockito.when(resourceHandler.getResourceHandlerCache()).thenReturn(cache);
        Mockito.when(resourceHandler.getResourceHandlerSupport()).thenReturn(support);
        
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");
        resourceHandler.createResource("test.png", "test", "test");

        Mockito.verify(cache, Mockito.times(4)).getResource(
                Mockito.eq("test.png"), Mockito.eq("test"), Mockito.eq("test"), Mockito.any());
        Mockito.verify(loader, Mockito.times(1)).getResourceURL(Mockito.any());
        Mockito.verify(loader, Mockito.never()).getResourceInputStream(Mockito.any());
        
    }
}
