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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;

import org.junit.Assert;

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockValueExpression;
import org.apache.myfaces.view.ViewDeclarationLanguageFactoryImpl;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.impl.FaceletCacheFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test class for ViewHandlerImpl
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@RunWith(JUnit4.class)
public class ViewHandlerImplTest extends AbstractJsfTestCase
{
    
    private ViewHandlerImpl _viewHandler;
    
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        // configure VDL factory
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                ViewDeclarationLanguageFactoryImpl.class.getName());
        
        FactoryFinder.setFactory(FactoryFinder.FACELET_CACHE_FACTORY,
                FaceletCacheFactoryImpl.class.getName());
        
        // configure the ViewHandler
        _viewHandler = new TestViewHandlerImpl();
        facesContext.getApplication().setViewHandler(_viewHandler);
        
        // add UIViewRoot as component
        facesContext.getApplication().addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
    }

    @After
    @Override
    public void tearDown() throws Exception
    {
        _viewHandler = null;
        
        super.tearDown();
    }

    /**
     *  Checks if ViewHandler.getBookmarkableURL() wants to change the parameter Map,
     *  which is not allowed, because this Map comes from the NavigationCase and
     *  any changes on it will change the NavigationCase itself.
     *  Normally this would mean that a <f:viewParam> is added as a static value
     *  to the NavigationCase (which is, of course, not allowed).
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetBookmarkableURLDoesNotChangeParametersMap()
    {
        // set the required path elements
        request.setPathElements("/", null, "/newview.jsf", null);
        
        // set the value for the ValueExpression #{paramvalue}
        externalContext.getApplicationMap().put("paramvalue", "paramvalue");
        
        // create a parameter map and make everything unmodifiable (Map and Lists)
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("key1", Collections.unmodifiableList(Arrays.asList("value11", "value12")));
        parameters.put("key2", Collections.unmodifiableList(Arrays.asList("value2")));
        parameters = Collections.unmodifiableMap(parameters);
        
        String url = null;
        try
        {
            url = _viewHandler.getBookmarkableURL(facesContext, "/newview.xhtml", parameters, true);
        }
        catch (UnsupportedOperationException uoe)
        {
            // an UnsupportedOperationException occured, which means getBookmarkableURL()
            // wanted to change the Map or any of the Lists and this is not allowed!
            Assert.fail("ViewHandler.getBookmarkableURL() must not change the parameter Map!");
        }
        
        // additional checks:
        // the URL must contain all params from the parameters map
        // and from the <f:viewParam> components of the target view
        Assert.assertTrue(url.contains("key1=value11"));
        Assert.assertTrue(url.contains("key1=value12"));
        Assert.assertTrue(url.contains("key2=value2"));
        Assert.assertTrue(url.contains("myparam=paramvalue"));
    }
    
    /**
     *  Checks if ViewHandler.getRedirectURL() wants to change the parameter Map,
     *  which is not allowed, because this Map comes from the NavigationCase and
     *  any changes on it will change the NavigationCase itself.
     *  Normally this would mean that a <f:viewParam> is added as a static value
     *  to the NavigationCase (which is, of course, not allowed).
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRedirectURLDoesNotChangeParametersMap()
    {
        // set the required path elements
        request.setPathElements("/", null, "/newview.jsf", null);
        
        // set the value for the ValueExpression #{paramvalue}
        externalContext.getApplicationMap().put("paramvalue", "paramvalue");
        
        // create a parameter map and make everything unmodifiable (Map and Lists)
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("key1", Collections.unmodifiableList(Arrays.asList("value11", "value12")));
        parameters.put("key2", Collections.unmodifiableList(Arrays.asList("value2")));
        parameters = Collections.unmodifiableMap(parameters);
        
        String url = null;
        try
        {
            url = _viewHandler.getRedirectURL(facesContext, "/newview.xhtml", parameters, true);
        }
        catch (UnsupportedOperationException uoe)
        {
            // an UnsupportedOperationException occured, which means getRedirectURL()
            // wanted to change the Map or any of the Lists and this is not allowed!
            Assert.fail("ViewHandler.getRedirectURL() must not change the parameter Map!");
        }
        
        // additional checks:
        // the URL must contain all params from the parameters map
        // and from the <f:viewParam> components of the target view
        Assert.assertTrue(url.contains("key1=value11"));
        Assert.assertTrue(url.contains("key1=value12"));
        Assert.assertTrue(url.contains("key2=value2"));
        Assert.assertTrue(url.contains("myparam=paramvalue"));
    }
    
    /**
     * A ViewHandler implementation that extends the default implementation
     * and returns a TestFaceletViewDeclarationLanguage in getVDL() for test purposes.
     * @author Jakob Korherr
     */
    private class TestViewHandlerImpl extends ViewHandlerImpl
    {

        @Override
        public ViewDeclarationLanguage getViewDeclarationLanguage(
                FacesContext context, String viewId)
        {
            return new TestFaceletViewDeclarationLanguage(context);
        }
        
    }
    
    /**
     * A VDL implementation which extends FaceletViewDeclarationLanguage, but
     * returns a TestViewMetadata instance on getViewMetadata().
     * @author Jakob Korherr
     */
    private class TestFaceletViewDeclarationLanguage extends FaceletViewDeclarationLanguage
    {

        public TestFaceletViewDeclarationLanguage(FacesContext context)
        {
            super(context);
        }

        @Override
        public ViewMetadata getViewMetadata(FacesContext context, String viewId)
        {
            return new TestViewMetadata(viewId);
        }
        
    }
    
    /**
     * A custom ViewMetadata implementation for the test, which returns
     * one UIViewParameter with a name of myparam and a value of #{paramvalue}.
     * @author Jakob Korherr
     */
    private class TestViewMetadata extends ViewMetadata
    {
        
        private String _viewId;
        
        public TestViewMetadata(String viewId)
        {
            _viewId = viewId;
        }

        @Override
        public UIViewRoot createMetadataView(FacesContext context)
        {
            UIViewRoot root = new UIViewRoot();
            root.setViewId(_viewId);
            UIComponent metadataFacet = new UIPanel();
            root.getFacets().put(UIViewRoot.METADATA_FACET_NAME, metadataFacet);
            UIViewParameter viewparam = new UIViewParameter();
            viewparam.setName("myparam");
            viewparam.setValueExpression("value", new MockValueExpression("#{paramvalue}", String.class));
            metadataFacet.getChildren().add(viewparam);
            return root;
        }

        @Override
        public String getViewId()
        {
            return _viewId;
        }
        
    }
    
}
