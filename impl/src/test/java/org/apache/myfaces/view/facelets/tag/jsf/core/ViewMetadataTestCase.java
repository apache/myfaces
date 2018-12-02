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
package org.apache.myfaces.view.facelets.tag.jsf.core;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.view.ViewMetadata;

import org.apache.myfaces.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.HelloWorld;
import org.junit.Assert;
import org.junit.Test;

public class ViewMetadataTestCase extends FaceletTestCase
{
    
    protected ConfigurableNavigationHandler navigationHandler;
    
    public static class MockViewNavigationHandlerNavigationHandler
        extends ConfigurableNavigationHandler
    {
        
        Map<String, Set<NavigationCase>> cases = new HashMap<String, Set<NavigationCase>>(); 

        @Override
        public NavigationCase getNavigationCase(FacesContext context,
                String fromAction, String outcome)
        {
            Set<NavigationCase> casesSet = cases.get(outcome);
            if (casesSet == null)
            {
                return null;
            }

            for (NavigationCase navCase : casesSet)
            {
                if (fromAction == null)
                {
                    return navCase;
                }
                else if (fromAction.equals(navCase.getFromAction()))
                {
                    return navCase;
                }
            }
            return null;
        }

        @Override
        public Map<String, Set<NavigationCase>> getNavigationCases()
        {
            // TODO Auto-generated method stub
            return cases;
        }

        @Override
        public void handleNavigation(FacesContext context, String fromAction,
                String outcome)
        {
            
        }
    }
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();        
        navigationHandler = new MockViewNavigationHandlerNavigationHandler();
        application.setNavigationHandler(navigationHandler);
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        navigationHandler = null;
    }
    
    @Test
    public void testSimpleViewMetadata() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        Set<NavigationCase> cases = new HashSet<NavigationCase>();
        NavigationCase navCase = new NavigationCase("viewMetadata.xhtml",null,
                "somePage.xhtml",null, "somePage.xhtml", null,false,false);
        cases.add(navCase);
        navigationHandler.getNavigationCases().put("somePage.xhtml", cases);
        navigationHandler.getNavigationCase(facesContext, null, "somePage.xhtml");
        
        ViewMetadata metadata = vdl.getViewMetadata(facesContext, "viewMetadata.xhtml");
        UIViewRoot root = metadata.createMetadataView(facesContext);
        
        Collection<UIViewParameter> viewParameters = metadata.getViewParameters(root);
        
        Assert.assertEquals(1, viewParameters.size());
        
        //root.setViewId("viewMetadata.xhtml");
        vdl.buildView(facesContext, root, "viewMetadata.xhtml");
        facesContext.setViewRoot(root);
        
        StringWriter sw = new StringWriter();
        ResponseWriter mrw = new HtmlResponseWriterImpl(sw,"text/html","UTF-8");
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        System.out.print(sw.toString());
    }
}
