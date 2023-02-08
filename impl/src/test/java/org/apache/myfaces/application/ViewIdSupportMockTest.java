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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.faces.FacesException;

import org.apache.myfaces.test.MyFacesAsserts;
import org.apache.myfaces.test.FacesTestCase;
import org.apache.myfaces.test.TestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ViewIdSupportMockTest extends FacesTestCase
{
    private ViewIdSupport viewIdSupport;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        Mockito.when(_facesContext.getExternalContext()).thenReturn(_externalContext);
        Mockito.when(_externalContext.getApplicationMap()).thenReturn(new ConcurrentHashMap<String, Object>());

        viewIdSupport = ViewIdSupport.getInstance(_facesContext);
    }

    @Test
    public void testCalculateViewIdFromRequestAttributeIncludePathInfo()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String expectedValue = "jakarta.servlet.include.path_info_VIEWID";
        map.put("jakarta.servlet.include.path_info", expectedValue);        
        Mockito.when(_externalContext.getRequestMap()).thenReturn(map);

        Assertions.assertEquals(expectedValue, viewIdSupport.calculateViewId(_facesContext));
    }

    @Test
    public void testCalculateViewIdFromRequestPathInfo()
    {
        Mockito.when(_externalContext.getRequestMap()).thenReturn(Collections.emptyMap());
        
        String expectedValue = "requestPathInfo_VIEWID";
        Mockito.when(_externalContext.getRequestPathInfo()).thenReturn(expectedValue);

        Assertions.assertEquals(expectedValue, viewIdSupport.calculateViewId(_facesContext));
    }
    
    @Test
    public void testCalculateViewIdFromRequestAttributeIncludeServletPath()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String expectedValue = "jakarta.servlet.include.servlet_path_VIEWID";
        map.put("jakarta.servlet.include.servlet_path", expectedValue);
        Mockito.when(_externalContext.getRequestMap()).thenReturn(map);
        
        Mockito.when(_externalContext.getRequestPathInfo()).thenReturn(null);

        Assertions.assertEquals(expectedValue, viewIdSupport.calculateViewId(_facesContext));
    }

    @Test
    public void testCalculateViewIdFromRequestServletPath()
    {
        Mockito.when(_externalContext.getRequestMap()).thenReturn(Collections.emptyMap());
        Mockito.when(_externalContext.getRequestPathInfo()).thenReturn(null);
        String expectedValue = "RequestServletPath_VIEWID";
        Mockito.when(_externalContext.getRequestServletPath()).thenReturn(expectedValue);

        Assertions.assertEquals(expectedValue, viewIdSupport.calculateViewId(_facesContext));
    }

    @Test
    public void testCalculateViewIdFacesException()
    {
        Mockito.when(_externalContext.getRequestMap()).thenReturn(Collections.emptyMap());
        Mockito.when(_externalContext.getRequestPathInfo()).thenReturn(null);
        Mockito.when(_externalContext.getRequestServletPath()).thenReturn(null);

        MyFacesAsserts.assertException(FacesException.class, new TestRunner()
        {
            @Override
            public void run() throws Throwable
            {
                viewIdSupport.calculateViewId(_facesContext);
            }
        });
    }
}
