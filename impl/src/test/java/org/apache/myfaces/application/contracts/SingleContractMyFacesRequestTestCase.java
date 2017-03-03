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
package org.apache.myfaces.application.contracts;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.faces.application.ProjectStage;
import javax.faces.application.ResourceVisitOption;
import javax.faces.application.StateManager;
import javax.faces.component.UICommand;


import org.apache.myfaces.config.RuntimeConfig;

import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.junit.Assert;
import org.junit.Test;

public class SingleContractMyFacesRequestTestCase extends AbstractMyFacesRequestTestCase
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
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.application.contracts");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("javax.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("javax.faces.CONFIG_FILES", "/no-contract-faces-config.xml");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.toString());
    }
    
    @Test
    public void testDefaultConfiguration() throws Exception
    {
        startViewRequest("/index.xhtml");
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        
        Set<String> allContracts = runtimeConfig.getResourceLibraryContracts();
        Set<String> externalContextContracts = runtimeConfig.getExternalContextResourceLibraryContracts();
        Set<String> classloaderContracts = runtimeConfig.getClassLoaderResourceLibraryContracts();

        Assert.assertTrue(allContracts.contains("yellow"));
        Assert.assertTrue(allContracts.contains("blue"));
        Assert.assertTrue(allContracts.contains("red"));

        Assert.assertTrue(classloaderContracts.contains("yellow"));
        Assert.assertTrue(classloaderContracts.contains("blue"));
        Assert.assertTrue(externalContextContracts.contains("red"));
        
        processLifecycleExecute();
        executeBuildViewCycle(facesContext);
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList == null || contractsList.isEmpty());
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        endRequest();
    }

    @Test
    public void testView1() throws Exception
    {
        startViewRequest("/view_1.xhtml");
        
        processLifecycleExecute();
        executeBuildViewCycle(facesContext);
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList.contains("yellow"));
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        List<String> contractsList2 = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList2.contains("yellow"));
    }
    
    @Test
    public void testView2() throws Exception
    {
        startViewRequest("/view_2.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle(facesContext);
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList.contains("blue"));
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        List<String> contractsList2 = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList2.contains("blue"));
    }

    @Test
    public void testView3() throws Exception
    {
        startViewRequest("/view_3.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle(facesContext);
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList.contains("red"));
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        List<String> contractsList2 = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList2.contains("red"));
    }
    
    @Test
    public void testView1_3() throws Exception
    {
        startViewRequest("/view_1.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle(facesContext);
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList.contains("yellow"));
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();        
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("header_yellow"));
        
        endRequest();
        
        startViewRequest("/view_3.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle(facesContext);
        
        contractsList = facesContext.getResourceLibraryContracts();
        Assert.assertTrue(contractsList.contains("red"));
        
        executeViewHandlerRender(facesContext);

        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("header_red"));
        
        executeAfterRender(facesContext);
        
        endRequest();
    }    

    @Test
    public void testGetViewResourcesYellow()
    {
        startViewRequest("/view_1.xhtml");
        processLifecycleExecute();        
        
        Stream<String> stream = facesContext.getApplication().getResourceHandler().getViewResources(
                facesContext, "/", Integer.MAX_VALUE);
        boolean hasYellowPage = false;
        boolean hasBluePage = false;
        boolean hasRedPage = false;
        for (Iterator<String> it = stream.iterator(); it.hasNext() ;)
        {
            String s = it.next();
            if (s.contains("yellowPage.xhtml"))
            {
                hasYellowPage = true;
            }
            if (s.contains("bluePage.xhtml"))
            {
                hasBluePage = true;
            }
            if (s.contains("redPage.xhtml"))
            {
                hasRedPage = true;
            }
        }
        Assert.assertTrue(hasYellowPage);
        Assert.assertFalse(hasBluePage);
        Assert.assertFalse(hasRedPage);
    }

    
    @Test
    public void testGetViewResourcesBlue()
    {
        startViewRequest("/view_2.xhtml");
        processLifecycleExecute();        
        
        Stream<String> stream = facesContext.getApplication().getResourceHandler().getViewResources(
                facesContext, "/", Integer.MAX_VALUE);
        boolean hasYellowPage = false;
        boolean hasBluePage = false;
        boolean hasRedPage = false;
        for (Iterator<String> it = stream.iterator(); it.hasNext() ;)
        {
            String s = it.next();
            if (s.contains("yellowPage.xhtml"))
            {
                hasYellowPage = true;
            }
            if (s.contains("bluePage.xhtml"))
            {
                hasBluePage = true;
            }
            if (s.contains("redPage.xhtml"))
            {
                hasRedPage = true;
            }
        }
        Assert.assertFalse(hasYellowPage);
        Assert.assertTrue(hasBluePage);
        Assert.assertFalse(hasRedPage);
    }

    @Test
    public void testGetTopLevelViewResourcesBlue()
    {
        startViewRequest("/view_2.xhtml");
        processLifecycleExecute();        
        
        Stream<String> stream = facesContext.getApplication().getResourceHandler().getViewResources(
                facesContext, "/", Integer.MAX_VALUE, ResourceVisitOption.TOP_LEVEL_VIEWS_ONLY);
        boolean hasYellowPage = false;
        boolean hasBluePage = false;
        boolean hasRedPage = false;
        for (Iterator<String> it = stream.iterator(); it.hasNext() ;)
        {
            String s = it.next();
            if (s.contains("yellowPage.xhtml"))
            {
                hasYellowPage = true;
            }
            if (s.contains("bluePage.xhtml"))
            {
                hasBluePage = true;
            }
            if (s.contains("redPage.xhtml"))
            {
                hasRedPage = true;
            }
            System.out.println(s);
        }
        Assert.assertFalse(hasYellowPage);
        Assert.assertTrue(hasBluePage);
        Assert.assertFalse(hasRedPage);
    }

}
