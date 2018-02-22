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

import java.util.List;
import java.util.Set;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewResource;

import org.apache.myfaces.config.RuntimeConfig;

import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.junit.Assert;
import org.junit.Test;

public class ContractsCreateResourceMyFacesRequestTestCase extends AbstractMyFacesRequestTestCase
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
        servletContext.addInitParameter("javax.faces.CONFIG_FILES", "/blue-faces-config.xml");
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
        
        List<String> defaultContracts = runtimeConfig.getContractMappings().get("*");
        
        Assert.assertFalse(defaultContracts.contains("yellow"));
        Assert.assertTrue(defaultContracts.contains("blue"));
        Assert.assertFalse(defaultContracts.contains("red"));
        
        processLifecycleExecute();
        executeBuildViewCycle(facesContext);
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        Assert.assertFalse(contractsList.contains("yellow"));
        Assert.assertTrue(contractsList.contains("blue"));
        Assert.assertFalse(contractsList.contains("red"));
        
        ResourceHandler resourceHandler = facesContext.getApplication().getResourceHandler();
        
        ViewResource resource1 = resourceHandler.createViewResource(facesContext, "/panel.xhtml");
        Assert.assertNotNull(resource1);
        Assert.assertTrue(resource1.getURL().toString().contains("panel.xhtml"));
        
        Resource resource2 = resourceHandler.createResource("myjs.js", "mylib");
        Assert.assertNotNull(resource2);
        
        boolean libraryFound = resourceHandler.libraryExists("mylib");
        Assert.assertTrue(libraryFound);
        
        endRequest();
    }

}
