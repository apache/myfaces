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
package org.apache.myfaces.context.servlet;

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Test cases for ServletExternalContextImpl.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@RunWith(JUnit4.class)
public class ServletExternalContextImplTest extends AbstractJsfTestCase
{

    private ServletExternalContextImpl _testExternalContext;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        _testExternalContext = new ServletExternalContextImpl(servletContext, request, response);
    }

    @After
    @Override
    public void tearDown() throws Exception
    {
        _testExternalContext = null;
        
        super.tearDown();
    }
    
    /**
     * Tests if encodeRedirectURL() and encodeBookmarkableURL() correctly
     * evaluate ValueExpressions as parameters values.
     */
    /* TODO: Invalid test, because EL evaluation should be done before call these methods.
    @Test
    @SuppressWarnings("unchecked")
    public void testEncodeURLHandlesValueExpressionParameters()
    {
        // put EL values in the application map
        Map<String, Object> applicationMap = externalContext.getApplicationMap();
        applicationMap.put("el1", "myvalue1");
        applicationMap.put("el2", "myvalue2");
        
        // create parameters Map
        // note that Arrays.asList() return an UnmodifiableList and thus we
        // indirectly also check for modification here.
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("param1", Arrays.asList("literalvalue", "#{el1}"));
        parameters.put("param2", Arrays.asList("#{el2}"));
        
        // encode the URL with parameters
        final String redirectUrl = _testExternalContext.encodeRedirectURL("someUrl.jsf", parameters);
        final String bookmarkableUrl = _testExternalContext.encodeBookmarkableURL("someUrl.jsf", parameters);
        
        // asserts for redirectUrl
        Assert.assertTrue(redirectUrl.contains("param1=literalvalue"));
        Assert.assertTrue(redirectUrl.contains("param1=myvalue1"));
        Assert.assertTrue(redirectUrl.contains("param2=myvalue2"));
        
        // asserts for bookmarkableUrl
        Assert.assertTrue(bookmarkableUrl.contains("param1=literalvalue"));
        Assert.assertTrue(bookmarkableUrl.contains("param1=myvalue1"));
        Assert.assertTrue(bookmarkableUrl.contains("param2=myvalue2"));
    }*/

    @Test
    public void testEncodeRedirectUrlWithEmptyParamInBaseUrl()
    {
        // query parameter p1 has an empty value
        String baseUrl = "/test?p1=&p2=test";
        
        // encode that URL without adding further parameters
        final String redirectUrl = _testExternalContext.encodeRedirectURL(baseUrl, null);
        
        // the URL should not change
        Assert.assertTrue(redirectUrl.contains("p1="));
        Assert.assertTrue(redirectUrl.contains("p2=test"));

    }

    @Test
    public void testEncodedSpaceInExistingQueryParameter()
    {
        // the base URL with an existing encoded query parameter 
        String baseUrl = "/test?p1=a+b";
        
        // encode that URL without adding further parameters
        final String redirectUrl = _testExternalContext.encodeRedirectURL(baseUrl, null);
        
        // the URL should not change
        Assert.assertEquals(baseUrl, redirectUrl);

    }
    
    @Test
    public void testEncodedAmpersandInExistingQueryParameter()
    {
        // the base URL with an existing encoded query parameter 
        String baseUrl = "/test?p1=a%26b";
        
        // encode that URL without adding further parameters
        final String redirectUrl = _testExternalContext.encodeRedirectURL(baseUrl, null);
        
        // the URL should not change
        Assert.assertEquals(baseUrl, redirectUrl);
        
    }
    
}
