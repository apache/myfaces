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

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

/**
 * Test cases for ServletExternalContextImpl.
 * 
 * @author Bill Lucy (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@RunWith(JUnit4.class)
public class FacesContextImplBaseTest extends FaceletTestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that the Map returned by UIViewRoot.getViewMap() is cleared when a new view root is set,
     * as required in 4.1.19.2
     */
    @Test
    public void testViewMapCleared() throws Exception
    {
        // don't use MockFacesServletContext here
        FacesContextImpl fci = new  FacesContextImpl(facesContext.getExternalContext(), null, null);
        UIViewRoot firstRoot = new UIViewRoot();
        UIViewRoot secondRoot = new UIViewRoot();
        fci.setViewRoot(firstRoot);
        Map<String, Object> viewMap = firstRoot.getViewMap();
        viewMap.put("entry", Boolean.TRUE);

        // set a new view root, which will cause the old view root's ViewMap to be cleared if BUILDING_VIEW_METADATA is not set
        fci.setViewRoot(secondRoot);
        Assert.assertEquals("The ViewMap was not cleared as expected", 0, viewMap.size());
    }

    /**
     * Make sure the Map returned by UIViewRoot.getViewMap() is NOT cleared when a new view root is set while the 
     * BUILDING_VIEW_METADATA attribute is set.  See MYFACES-4282.
     */
    @Test
    public void testViewMapNotClearedWhileBuildingViewMetadata() throws Exception
    {
        FacesContextImpl fci = new  FacesContextImpl(facesContext.getExternalContext(), null, null);
        UIViewRoot firstRoot = new UIViewRoot();
        UIViewRoot secondRoot = new UIViewRoot();
        fci.getAttributes().put(FaceletViewDeclarationLanguage.BUILDING_VIEW_METADATA, Boolean.TRUE);
        fci.setViewRoot(firstRoot);
        Map<String, Object> viewMap = firstRoot.getViewMap();
        viewMap.put("entry", Boolean.TRUE);

        // set a new view root - the old view root's ViewMap should NOT be cleared
        fci.setViewRoot(secondRoot);
        Assert.assertEquals("The ViewMap was incorrectly cleared while the BUILDING_VIEW_METADATA attribute was set", 1, viewMap.size());
    }
}
