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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.FactoryFinder;
import javax.faces.application.NavigationCase;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class NavigationHandlerImplTest extends AbstractJsfTestCase
{

    private DigesterFacesConfigUnmarshallerImpl _digesterFacesConfigUnmarshaller;

    public NavigationHandlerImplTest()
    {
        super();
    }
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _digesterFacesConfigUnmarshaller = new DigesterFacesConfigUnmarshallerImpl(
                externalContext);
    }
    
    @Override
    protected void setFactories() throws Exception
    {
        super.setFactories();
        FactoryFinder.setFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY,
            "org.apache.myfaces.context.PartialViewContextFactoryImpl");
    }


    @Override
    public void tearDown() throws Exception
    {
        _digesterFacesConfigUnmarshaller = null;
        super.tearDown();
    }

    private void loadTextFacesConfig(String file) throws SAXException,
            IOException
    {
        RuntimeConfig runtimeConfig = RuntimeConfig
                .getCurrentInstance(externalContext);

        org.apache.myfaces.config.impl.digester.elements.FacesConfig config = _digesterFacesConfigUnmarshaller
                .getFacesConfig(getClass().getResourceAsStream(file), file);

        for (NavigationRule rule : config.getNavigationRules())
        {
            runtimeConfig.addNavigationRule(rule);
        }
    }

    @Test
    public void testGetSimpleExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimpleExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assert.assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }

    @Test
    public void testGetSimpleGlobalExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimpleGlobalExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assert.assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testGetSimpleMixExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-mix-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/c.jsp", nc.getToViewId(facesContext));
        
        facesContext.getViewRoot().setViewId("/z.jsp");

        nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimpleMixExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-mix-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assert.assertEquals("/c.jsp", facesContext.getViewRoot().getViewId());
        
        facesContext.getViewRoot().setViewId("/z.jsp");
        
        nh.handleNavigation(facesContext, null, "go");
        
        Assert.assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testGetSimplePartialExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-partial-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/cars/b.jsp", nc.getToViewId(facesContext));
        
        facesContext.getViewRoot().setViewId("/cars/z.jsp");

        nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/cars/c.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimplePartialExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-partial-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assert.assertEquals("/cars/b.jsp", facesContext.getViewRoot().getViewId());
        
        facesContext.getViewRoot().setViewId("/cars/z.jsp");
        
        nh.handleNavigation(facesContext, null, "go");
        
        Assert.assertEquals("/cars/c.jsp", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testGetSimpleELExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");

        Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleELExactMatchRuleFailNullOutcome() throws Exception
    {
        loadTextFacesConfig("simple-el-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", null);

        Assert.assertNull(nc);
    }
    
    public static class TestBean
    {
        public boolean isTrue()
        {
            return true;
        }
        
        public boolean isFalse()
        {
            return false;
        }
    }
    
    @Test
    public void testGetSimpleIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleNotIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/b.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/d.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimplePreemptiveIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/x.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/go.jsp", nc.getToViewId(facesContext));
    }    
    
    @Test
    public void testGetSimpleGlobalPreemptiveMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-preemptive-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/x.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assert.assertEquals("/a.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleELNoCondMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-nocond-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");

        Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleELNoCondNullOutcomeMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-nocond-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", null);

        Assert.assertNull(nc);
    }
    
    @Test
    public void testActionOutcomePrecendeceMachRule() throws Exception
    {
        loadTextFacesConfig("simple-action-outcome-precedence-config.xml");

        for (int i = 0; i < 2; i++)
        {
            final int j = i;
            facesContext.getViewRoot().setViewId("/a.jsp");
            
            NavigationHandlerImpl nh = new NavigationHandlerImpl(){
    
                @Override
                public Map<String, Set<NavigationCase>> getNavigationCases()
                {
                    // We have two conditions, this code is for rotate the ordering so we
                    // test all possible outputs
                    Map<String, Set<NavigationCase>> map = super.getNavigationCases();
                    Map<String, Set<NavigationCase>> map2 = new HashMap<String, Set<NavigationCase>>();
                    
                    for (Map.Entry<String, Set<NavigationCase>> entry : map.entrySet())
                    {
                        LinkedHashSet<NavigationCase> set = new LinkedHashSet<NavigationCase>();
                        List<NavigationCase> list = new ArrayList<NavigationCase>();
                        list.addAll(entry.getValue());
                        Collections.rotate(list, j);
                        set.addAll(list);
                        map2.put(entry.getKey(), set);
                    }
                    return map2; 
                }
            };
    
            NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");
    
            Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
        }
    }
    
    @Test
    public void testActionOutcomePrecendeceAlternateOutcomeMachRule() throws Exception
    {
        loadTextFacesConfig("simple-action-outcome-precedence-config.xml");

        for (int i = 0; i < 2; i++)
        {
            final int j = i;
            facesContext.getViewRoot().setViewId("/a.jsp");
            
            NavigationHandlerImpl nh = new NavigationHandlerImpl(){
    
                @Override
                public Map<String, Set<NavigationCase>> getNavigationCases()
                {
                    // We have two conditions, this code is for rotate the ordering so we
                    // test all possible outputs
                    Map<String, Set<NavigationCase>> map = super.getNavigationCases();
                    Map<String, Set<NavigationCase>> map2 = new HashMap<String, Set<NavigationCase>>();
                    
                    for (Map.Entry<String, Set<NavigationCase>> entry : map.entrySet())
                    {
                        LinkedHashSet<NavigationCase> set = new LinkedHashSet<NavigationCase>();
                        List<NavigationCase> list = new ArrayList<NavigationCase>();
                        list.addAll(entry.getValue());
                        Collections.rotate(list, j);
                        set.addAll(list);
                        map2.put(entry.getKey(), set);
                    }
                    return map2; 
                }
            };
    
            NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "nogo");
    
            Assert.assertEquals("/c.jsp", nc.getToViewId(facesContext));
        }
    }

    @Test
    public void testActionOutcomePrecendeceNoOutcomeMachRule() throws Exception
    {
        loadTextFacesConfig("simple-action-outcome-precedence-config.xml");

        for (int i = 0; i < 2; i++)
        {
            final int j = i;
            facesContext.getViewRoot().setViewId("/a.jsp");
            
            NavigationHandlerImpl nh = new NavigationHandlerImpl(){
    
                @Override
                public Map<String, Set<NavigationCase>> getNavigationCases()
                {
                    // We have two conditions, this code is for rotate the ordering so we
                    // test all possible outputs
                    Map<String, Set<NavigationCase>> map = super.getNavigationCases();
                    Map<String, Set<NavigationCase>> map2 = new HashMap<String, Set<NavigationCase>>();
                    
                    for (Map.Entry<String, Set<NavigationCase>> entry : map.entrySet())
                    {
                        LinkedHashSet<NavigationCase> set = new LinkedHashSet<NavigationCase>();
                        List<NavigationCase> list = new ArrayList<NavigationCase>();
                        list.addAll(entry.getValue());
                        Collections.rotate(list, j);
                        set.addAll(list);
                        map2.put(entry.getKey(), set);
                    }
                    return map2; 
                }
            };
    
            NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", null);
    
            //If the <if> element is absent, only match a non-null outcome
            Assert.assertNull(nc);
        }
    }
    
    @Test
    public void testActionOutcomePrecendece2MachRule() throws Exception
    {
        loadTextFacesConfig("simple-action-outcome-precedence-2-config.xml");

        for (int i = 0; i < 2; i++)
        {
            final int j = i;
            facesContext.getViewRoot().setViewId("/a.jsp");
            
            NavigationHandlerImpl nh = new NavigationHandlerImpl(){
    
                @Override
                public Map<String, Set<NavigationCase>> getNavigationCases()
                {
                    // We have two conditions, this code is for rotate the ordering so we
                    // test all possible outputs
                    Map<String, Set<NavigationCase>> map = super.getNavigationCases();
                    Map<String, Set<NavigationCase>> map2 = new HashMap<String, Set<NavigationCase>>();
                    
                    for (Map.Entry<String, Set<NavigationCase>> entry : map.entrySet())
                    {
                        LinkedHashSet<NavigationCase> set = new LinkedHashSet<NavigationCase>();
                        List<NavigationCase> list = new ArrayList<NavigationCase>();
                        list.addAll(entry.getValue());
                        Collections.rotate(list, j);
                        set.addAll(list);
                        map2.put(entry.getKey(), set);
                    }
                    return map2; 
                }
            };
    
            NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");
    
            Assert.assertEquals("/b.jsp", nc.getToViewId(facesContext));
        }
    }
    
    /**
     * Tests if the URL parameters of an outcome are correctly
     * added to the NavigationCase.
     */
    @Test
    public void testFacesRedirectAddsUrlParameters()
    {
        NavigationHandlerImpl nh = new NavigationHandlerImpl();
        
        // get the NavigationCase
        // note that the URL parameters can be separated via & or &amp;
        NavigationCase navigationCase = nh.getNavigationCase(facesContext, null, 
                "test.xhtml?faces-redirect=true&a=b&amp;includeViewParams=true&amp;c=d&e=f");
        
        // created the expected parameter map
        Map<String, List<String>> expected = new HashMap<String, List<String>>();
        expected.put("a", Arrays.asList("b"));
        expected.put("c", Arrays.asList("d"));
        expected.put("e", Arrays.asList("f"));
        // note that faces-redirect and includeViewParams
        // should not be added as a parameter
        
        Assert.assertEquals(expected, navigationCase.getParameters());
    }
}
