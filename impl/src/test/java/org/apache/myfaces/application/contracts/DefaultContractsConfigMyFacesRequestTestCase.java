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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import jakarta.faces.application.StateManager;

import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.junit.jupiter.api.Test;

class DefaultContractsConfigMyFacesRequestTestCase extends AbstractMyFacesCDIRequestTestCase
{

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.application.contracts");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
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
        
        List<String> defaultContracts = runtimeConfig.getContractMappings().get("*");
        
        assertTrue(defaultContracts.contains("yellow"));
        assertTrue(defaultContracts.contains("blue"));
        assertTrue(defaultContracts.contains("red"));
        
        processLifecycleExecute();
        executeBuildViewCycle();
        
        List<String> contractsList = facesContext.getResourceLibraryContracts();
        assertTrue(contractsList.contains("yellow"));
        assertTrue(contractsList.contains("blue"));
        assertTrue(contractsList.contains("red"));
        
        endRequest();
    }

}
