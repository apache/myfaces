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

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.NavigationCase;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.impl.FacesConfigUnmarshallerImpl;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class NavigationHandlerImplTest extends AbstractJsfTestCase
{
    private FacesConfigUnmarshallerImpl _facesConfigUnmarshaller;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        _facesConfigUnmarshaller = new FacesConfigUnmarshallerImpl(
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
    @AfterEach
    public void tearDown() throws Exception
    {
        _facesConfigUnmarshaller = null;
        super.tearDown();
    }

    private void loadTextFacesConfig(String file) throws SAXException,
            IOException
    {
        RuntimeConfig runtimeConfig = RuntimeConfig
                .getCurrentInstance(externalContext);

        org.apache.myfaces.config.impl.element.FacesConfigImpl config = _facesConfigUnmarshaller
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

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimpleExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assertions.assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }

    @Test
    public void testGetSimpleGlobalExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimpleGlobalExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assertions.assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testGetSimpleMixExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-mix-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/c.jsp", nc.getToViewId(facesContext));
        
        facesContext.getViewRoot().setViewId("/z.jsp");

        nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimpleMixExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-mix-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assertions.assertEquals("/c.jsp", facesContext.getViewRoot().getViewId());
        
        facesContext.getViewRoot().setViewId("/z.jsp");
        
        nh.handleNavigation(facesContext, null, "go");
        
        Assertions.assertEquals("/b.jsp", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testGetSimplePartialExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-partial-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/cars/b.jsp", nc.getToViewId(facesContext));
        
        facesContext.getViewRoot().setViewId("/cars/z.jsp");

        nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/cars/c.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testHandleSimplePartialExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-partial-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        nh.handleNavigation(facesContext, null, "go");

        Assertions.assertEquals("/cars/b.jsp", facesContext.getViewRoot().getViewId());
        
        facesContext.getViewRoot().setViewId("/cars/z.jsp");
        
        nh.handleNavigation(facesContext, null, "go");
        
        Assertions.assertEquals("/cars/c.jsp", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testGetSimpleELExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleELExactMatchRuleFailNullOutcome() throws Exception
    {
        loadTextFacesConfig("simple-el-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", null);

        Assertions.assertNull(nc);
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

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleIfPreferMathRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config-2.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleIfPreferMathRule2() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config-3.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "go", null);

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
        
        nc = nh.getNavigationCase(facesContext, "go", "xx");

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }

    @Test
    public void testGetSimpleNotIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/b.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/d.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimplePreemptiveIfExactMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/x.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/go.jsp", nc.getToViewId(facesContext));
    }    
    
    @Test
    public void testGetSimpleGlobalPreemptiveMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-global-preemptive-rules-config.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/x.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/a.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleELNoCondMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-nocond-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", "go");

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
    @Test
    public void testGetSimpleELNoCondNullOutcomeMatchRule() throws Exception
    {
        loadTextFacesConfig("simple-el-nocond-rules-config.xml");

        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, "#{rules.go}", null);

        Assertions.assertNull(nc);
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
    
            Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
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
    
            Assertions.assertEquals("/c.jsp", nc.getToViewId(facesContext));
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
            Assertions.assertNull(nc);
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
    
            Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
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
        
        Assertions.assertEquals(expected, navigationCase.getParameters());
        Assertions.assertTrue(navigationCase.isIncludeViewParams());
        Assertions.assertTrue(navigationCase.isRedirect());
    }
    
    /**
     * Tests if the URL parameters of an outcome are correctly
     * added to the NavigationCase.
     * Identically to testFacesRedirectAddsUrlParameters(), except that
     * it uses faces-include-view-params=true instead of includeViewParams=true.
     */
    @Test
    public void testFacesRedirectAddsUrlParametersFacesIncludeViewParams()
    {
        NavigationHandlerImpl nh = new NavigationHandlerImpl();
        
        // get the NavigationCase
        // note that the URL parameters can be separated via & or &amp;
        NavigationCase navigationCase = nh.getNavigationCase(facesContext, null, 
                "test.xhtml?faces-redirect=true&a=b&amp;faces-include-view-params=true&amp;c=d&e=f");
        
        // created the expected parameter map
        Map<String, List<String>> expected = new HashMap<String, List<String>>();
        expected.put("a", Arrays.asList("b"));
        expected.put("c", Arrays.asList("d"));
        expected.put("e", Arrays.asList("f"));
        // note that faces-redirect and faces-include-view-params
        // should not be added as a parameter
        
        Assertions.assertEquals(expected, navigationCase.getParameters());
        Assertions.assertTrue(navigationCase.isIncludeViewParams());
        Assertions.assertTrue(navigationCase.isRedirect());
    }
    
    /**
     * Test for MYFACES-3101
     */
    @Test
    public void testHandleViewExpiredExpcetion() throws Exception {
        NavigationHandlerImpl underTest = new NavigationHandlerImpl();
        // simulate no available ViewRoot (in case of VEE)
        facesContext.setViewRoot(null);
        
        facesContext.getExternalContext().getRequestMap().put("jakarta.servlet.include.servlet_path", "/faces/home.xhtml");
        // test is based on:
        // http://www.nfjsone.com/blog/ed_burns/2009/09/dealing_gracefully_with_viewexpiredexception_in_jsf2
        underTest.handleNavigation(facesContext, null, "viewExpired");

        Assertions.assertNotNull(facesContext.getViewRoot());
        Assertions.assertEquals("/viewExpired.xhtml", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testHandleViewExpiredExpcetion2() throws Exception {
        NavigationHandlerImpl underTest = new NavigationHandlerImpl();
        // simulate no available ViewRoot (in case of VEE)
        facesContext.setViewRoot(null);
        
        facesContext.getExternalContext().getRequestMap().put("jakarta.servlet.include.servlet_path", "/home.jsf");
        // test is based on:
        // http://www.nfjsone.com/blog/ed_burns/2009/09/dealing_gracefully_with_viewexpiredexception_in_jsf2
        underTest.handleNavigation(facesContext, null, "viewExpired.xhtml");

        Assertions.assertNotNull(facesContext.getViewRoot());
        Assertions.assertEquals("/viewExpired.xhtml", facesContext.getViewRoot().getViewId());
        
    }
    
    @Test
    public void testHandleViewExpiredExpcetion3() throws Exception {
        NavigationHandlerImpl underTest = new NavigationHandlerImpl();
        // simulate no available ViewRoot (in case of VEE)
        facesContext.setViewRoot(null);
        
        facesContext.getExternalContext().getRequestMap().put("jakarta.servlet.include.servlet_path", "/home.jsf");
        // test is based on:
        // http://www.nfjsone.com/blog/ed_burns/2009/09/dealing_gracefully_with_viewexpiredexception_in_jsf2
        underTest.handleNavigation(facesContext, null, "viewExpired");

        Assertions.assertNotNull(facesContext.getViewRoot());
        
        // In this case, we have /viewExpired.jsf, but note the default ViewHandlerImpl converts the viewId
        // from /viewExpired.jsf to /viewExpired.xhtml, when deriveViewId() is called.
        Assertions.assertEquals("/viewExpired.jsf", facesContext.getViewRoot().getViewId());
        
    }

   @Test
    public void testHandleViewExpiredExpcetion4() throws Exception {
        NavigationHandlerImpl underTest = new NavigationHandlerImpl();
        // simulate no available ViewRoot (in case of VEE)
        facesContext.setViewRoot(null);
        // test is based on:
        // http://www.nfjsone.com/blog/ed_burns/2009/09/dealing_gracefully_with_viewexpiredexception_in_jsf2
        underTest.handleNavigation(facesContext, null, "viewExpired.xhtml");

        Assertions.assertNotNull(facesContext.getViewRoot());
        Assertions.assertEquals("/viewExpired.xhtml", facesContext.getViewRoot().getViewId());
        
    } 
    
    /**
     * Test for MYFACES-3101 - partial request (without redirect)
     */
    @Test
    public void testHandleViewExpiredExpcetionForPartial() throws Exception {
        NavigationHandlerImpl underTest = new NavigationHandlerImpl();
        // simulate no available ViewRoot (in case of VEE)
        facesContext.setViewRoot(null);
        facesContext.getPartialViewContext().setPartialRequest(true);
        
        underTest.handleNavigation(facesContext, null, "/viewExpired.xhtml");
        
        Assertions.assertNotNull(facesContext.getViewRoot());
        Assertions.assertEquals("/viewExpired.xhtml", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testIfDoNotMatchWhenOutcomeNull() throws Exception
    {
        loadTextFacesConfig("simple-if-rules-config-4.xml");

        externalContext.getRequestMap().put("test", new TestBean());
        
        facesContext.getViewRoot().setViewId("/a.jsp");
        
        NavigationHandlerImpl nh = new NavigationHandlerImpl();

        NavigationCase nc = nh.getNavigationCase(facesContext, null, null);

        Assertions.assertNull(nc);
        
        nc = nh.getNavigationCase(facesContext, null, "go");

        Assertions.assertEquals("/b.jsp", nc.getToViewId(facesContext));
    }
    
}
