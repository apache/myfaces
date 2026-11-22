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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UICommand;


import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.junit.jupiter.api.Test;

class SingleContractMyFacesRequestTestCase extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.application.contracts");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("jakarta.faces.CONFIG_FILES", "/no-contract-faces-config.xml");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.toString());
    }

    @Test
    void defaultConfiguration() throws Exception
    {
        startViewRequest("/index.xhtml");
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        
        Set<String> allContracts = runtimeConfig.getResourceLibraryContracts();
        Set<String> externalContextContracts = runtimeConfig.getExternalContextResourceLibraryContracts();
        Set<String> classloaderContracts = runtimeConfig.getClassLoaderResourceLibraryContracts();

        assertTrue(allContracts.contains("yellow"));
        assertTrue(allContracts.contains("blue"));
        assertTrue(allContracts.contains("red"));

        assertTrue(classloaderContracts.contains("yellow"));
        assertTrue(classloaderContracts.contains("blue"));
        assertTrue(externalContextContracts.contains("red"));
        
        processLifecycleExecute();
        executeBuildViewCycle();
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList == null || contractsList.isEmpty());
        
        executeViewHandlerRender();
        executeAfterRender();
        
        endRequest();
    }

    @Test
    void view1() throws Exception
    {
        startViewRequest("/view_1.xhtml");
        
        processLifecycleExecute();
        executeBuildViewCycle();
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList.contains("yellow"));
        
        executeViewHandlerRender();
        executeAfterRender();
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        List<String> contractsList2 = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList2.contains("yellow"));
    }

    @Test
    void view2() throws Exception
    {
        startViewRequest("/view_2.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle();
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList.contains("blue"));
        
        executeViewHandlerRender();
        executeAfterRender();
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        List<String> contractsList2 = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList2.contains("blue"));
    }

    @Test
    void view3() throws Exception
    {
        startViewRequest("/view_3.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle();
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList.contains("red"));
        
        executeViewHandlerRender();
        executeAfterRender();
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        List<String> contractsList2 = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList2.contains("red"));
    }

    @Test
    void view13() throws Exception
    {
        startViewRequest("/view_1.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle();
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList.contains("yellow"));
        
        executeViewHandlerRender();
        executeAfterRender();
        
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();        
        String content1 = new String(writer1.content());
        assertTrue(content1.contains("header_yellow"));
        
        endRequest();
        
        startViewRequest("/view_3.xhtml");
        processLifecycleExecute();
        executeBuildViewCycle();
        
        contractsList = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList.contains("red"));
        
        executeViewHandlerRender();

        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        assertTrue(content2.contains("header_red"));
        
        executeAfterRender();
        
        endRequest();
    }

    @Test
    void getViewResourcesYellow()
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
        assertTrue(hasYellowPage);
        assertFalse(hasBluePage);
        assertFalse(hasRedPage);
    }


    @Test
    void getViewResourcesBlue()
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
        assertFalse(hasYellowPage);
        assertTrue(hasBluePage);
        assertFalse(hasRedPage);
    }

    @Test
    void getTopLevelViewResourcesBlue()
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
            //System.out.println(s);
        }
        assertFalse(hasYellowPage);
        assertTrue(hasBluePage);
        assertFalse(hasRedPage);
    }

}
