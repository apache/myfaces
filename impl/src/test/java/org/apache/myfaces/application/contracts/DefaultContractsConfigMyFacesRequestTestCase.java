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
import jakarta.faces.application.StateManager;

import org.junit.jupiter.api.Assertions;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.junit.jupiter.api.Test;

public class DefaultContractsConfigMyFacesRequestTestCase extends AbstractMyFacesRequestTestCase
{

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.application.contracts");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
    }
    
    @Test
    public void testDefaultConfiguration() throws Exception
    {
        startViewRequest("/index.xhtml");
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        
        Set<String> allContracts = runtimeConfig.getResourceLibraryContracts();
        Set<String> externalContextContracts = runtimeConfig.getExternalContextResourceLibraryContracts();
        Set<String> classloaderContracts = runtimeConfig.getClassLoaderResourceLibraryContracts();

        Assertions.assertTrue(allContracts.contains("yellow"));
        Assertions.assertTrue(allContracts.contains("blue"));
        Assertions.assertTrue(allContracts.contains("red"));

        Assertions.assertTrue(classloaderContracts.contains("yellow"));
        Assertions.assertTrue(classloaderContracts.contains("blue"));
        Assertions.assertTrue(externalContextContracts.contains("red"));
        
        List<String> defaultContracts = runtimeConfig.getContractMappings().get("*");
        
        Assertions.assertTrue(defaultContracts.contains("yellow"));
        Assertions.assertTrue(defaultContracts.contains("blue"));
        Assertions.assertTrue(defaultContracts.contains("red"));
        
        processLifecycleExecute();
        executeBuildViewCycle();
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        Assertions.assertTrue(contractsList.contains("yellow"));
        Assertions.assertTrue(contractsList.contains("blue"));
        Assertions.assertTrue(contractsList.contains("red"));
        
        endRequest();
    }

}
