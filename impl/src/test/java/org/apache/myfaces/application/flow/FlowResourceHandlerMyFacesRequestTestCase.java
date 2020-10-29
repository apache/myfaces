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
package org.apache.myfaces.application.flow;

import java.util.Iterator;
import java.util.stream.Stream;
import jakarta.faces.application.StateManager;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.resource.ClassLoaderResourceLoader;
import org.apache.myfaces.resource.ExternalContextResourceLoader;
import org.junit.Test;

/**
 *
 * @author lu4242
 */
public class FlowResourceHandlerMyFacesRequestTestCase extends AbstractMyFacesRequestTestCase
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
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.application.flow");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("javax.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("javax.faces.CONFIG_FILES", "/WEB-INF/flow1-flow.xml");
        servletContext.addInitParameter("javax.faces.CLIENT_WINDOW_MODE", "url");
    }
 
    @Test
    public void testClassLoaderResourceLoader()
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ClassLoaderResourceLoader loader = new ClassLoaderResourceLoader("org/apache/myfaces/application/flow");
        
        for (Iterator<String> it = loader.iterator(facesContext, "", 1); it.hasNext();)
        {
            String path = it.next();
            System.out.println(path);
        }
    }
    
    @Test
    public void testExternalContextResourceLoader()
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();        
        
        ExternalContextResourceLoader loader = new ExternalContextResourceLoader("");
        
        for (Iterator<String> it = loader.iterator(facesContext, "", 1); it.hasNext();)
        {
            String path = it.next();
            System.out.println(path);
        }
    }
    
    @Test
    public void testGetViewResources()
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();        
        
        Stream<String> stream = facesContext.getApplication().getResourceHandler().getViewResources(
                facesContext, "/", 2);
        stream.forEach(s -> {
            System.out.println(s);
        });
        
    }
}
