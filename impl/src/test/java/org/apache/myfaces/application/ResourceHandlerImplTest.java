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

import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.faces.application.Resource;
import java.net.URL;
import java.util.Locale;

/**
 * Test cases for org.apache.myfaces.application.ResourceHandlerImpl.
 *
 * @author Jakob Korherr
 */
public class ResourceHandlerImplTest extends AbstractJsfTestCase
{

    private ResourceHandlerImpl resourceHandler;

    public ResourceHandlerImplTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        resourceHandler = new ResourceHandlerImpl();
    }

    @Override
    protected void tearDown() throws Exception
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

}
