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
package org.apache.myfaces.view.facelets;

import javax.el.ExpressionFactory;

import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for issue MYFACES-4175.
 *
 * @author Jay Sartoris
 */

public class TemplateInResourcesDirTestCase extends AbstractMyFacesRequestTestCase
{

    @Override
    protected boolean isScanAnnotations()
    {
        return true;
    }

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets");
    }
    
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }    
    
    @Test
    public void testFaceletWithTemplateInResourcesDir() throws Exception
    {
        startViewRequest("/templateInResourcesDir.xhtml");
        processLifecycleExecuteAndRender();
        
        String text = getRenderedContent(facesContext);
        Assert.assertTrue(text.contains("This template is working as expected!"));
        
        endRequest();
    }
}
