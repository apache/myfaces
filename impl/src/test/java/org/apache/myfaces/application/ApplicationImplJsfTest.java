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

import java.beans.BeanInfo;

import javax.el.ExpressionFactory;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.apache.myfaces.view.facelets.impl.FaceletCacheFactoryImpl;
import org.apache.myfaces.view.facelets.mock.MockViewDeclarationLanguageFactory;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;

/**
 * Test class for ApplicationImpl that extends AbstractJsfTestCase
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revisio$ $Date$
 */
public class ApplicationImplJsfTest extends AbstractJsfTestCase
{
    
    private static final String RESOURCE_MSG = "The Resource must be stored in the component's attribute " +
                                               "map under the key " + Resource.COMPONENT_RESOURCE_KEY;
    
    private static final String BEANINFO_MSG = "The BeanInfo metadata has to be store in the component's " + 
                                               "attribute map under the key " + UIComponent.BEANINFO_KEY;
    
    private static final String COMPOSITE_RENDERER_MSG = "The rendererType has to be javax.faces.Composite";
    
    private static final String TEST_COMPONENT_TYPE = "org.apache.myfaces.MyCustomComponentType";
    
    /**
     * Application wrapper to test all the methods from ApplicationImpl,
     * but to be able to override getExpressionFactory() to get the 
     * MockExpressionFactory.
     * 
     * @author Jakob Korherr
     */
    public static class TestApplicationWrapper extends ApplicationWrapper
    {
        
        private ApplicationImpl _applicationImpl;
        private MockExpressionFactory _expressionFactory;
        
        public TestApplicationWrapper(ApplicationImpl applicationImpl)
        {
            _applicationImpl = applicationImpl;
            _expressionFactory = new MockExpressionFactory();
        }

        @Override
        public Application getWrapped()
        {
            return _applicationImpl;
        }
        
        @Override
        public ExpressionFactory getExpressionFactory() 
        {
            return _expressionFactory;
        }
        
    }
    
    /**
     * Helper method to assert the RendererType, the Resource and the BeanInfo.
     * @param component
     * @param resource
     * @param metadata
     */
    private static void assertRendererTypeResourceBeanInfo(UIComponent component, Resource resource, BeanInfo metadata)
    {
        Assert.assertEquals(COMPOSITE_RENDERER_MSG, "javax.faces.Composite", component.getRendererType());
        Assert.assertEquals(RESOURCE_MSG, resource, component.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY));
        Assert.assertEquals(BEANINFO_MSG, metadata, component.getAttributes().get(UIComponent.BEANINFO_KEY));
    }

    private TestApplicationWrapper _testApplication;
    private IMocksControl _mocksControl;
    private FaceletContext _faceletContext;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        _mocksControl = EasyMock.createControl();
        
        // create a FaceletContext Mock and put it in the FacesContext
        _faceletContext = _mocksControl.createMock(FaceletContext.class);
        facesContext.getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, _faceletContext);
        
        // create and configure our ApplicationImpl instance
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                MockViewDeclarationLanguageFactory.class.getName());
        FactoryFinder.setFactory(FactoryFinder.FACELET_CACHE_FACTORY,
                FaceletCacheFactoryImpl.class.getName());
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        _testApplication = new TestApplicationWrapper(new ApplicationImpl(runtimeConfig));
        facesContext.setApplication(_testApplication);
    }

    @Override
    public void tearDown() throws Exception
    {
        _mocksControl = null;
        _faceletContext = null;
        _testApplication = null;
        
        super.tearDown();
    }

    /**
     * Tests the creation of a composite component via 
     * Application.createComponent(FacesContext context, Resource componentResource)
     */
    public void testCreateComponentFromResource()
    {
        /* TODO: Make it work again
        // we need a UINamingContainer for this test
        application.addComponent(UINamingContainer.COMPONENT_TYPE, UINamingContainer.class.getName());
        _testApplication.addComponent(UINamingContainer.COMPONENT_TYPE, UINamingContainer.class.getName());
        
        // configure FaceletContext mock
        MockVariableMapper variableMapper = new MockVariableMapper();
        EasyMock.expect(_faceletContext.getVariableMapper()).andReturn(variableMapper).anyTimes();
        _faceletContext.setVariableMapper((VariableMapper) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        _mocksControl.replay();

        // get the VDL
        UIViewRoot view = facesContext.getViewRoot();
        MockFaceletViewDeclarationLanguage vdl 
                = (MockFaceletViewDeclarationLanguage) _testApplication.getViewHandler()
                    .getViewDeclarationLanguage(facesContext, view.getViewId());
        
        // ---- first component test - without any special settings ----------------------------
        
        // configure the Resource needed for this test
        MockSimpleResource resource = new MockSimpleResource(null, "testlib", null, "composite.xhtml", 
                null, new File("src/test/resources/org/apache/myfaces/application"));
        
        // get the BeanInfo metadata
        BeanInfo metadata = vdl.getComponentMetadata(facesContext, resource);
        
        // create the component
        UIComponent component = _testApplication.createComponent(facesContext, resource);
        
        // asserts for the first component
        Assert.assertTrue("The component has to be an UINamingContainer", component instanceof UINamingContainer);
        assertRendererTypeResourceBeanInfo(component, resource, metadata);
        
        // ---- second component test - from a script ------------------------------------------
        
        MockSimpleResource scriptResource = new MockSimpleResource(null, "testlib", null, 
                "org.apache.myfaces.application.MockResourceComponent.groovy", 
                null, new File("src/test/resources/org/apache/myfaces/application"));
        
        // install the script resource to the VDL-mock
        vdl.setScriptComponentResource(resource, scriptResource);
        
        // create the component
        component = _testApplication.createComponent(facesContext, resource);
        
        // asserts for the second component
        Assert.assertTrue("The component has to be a MockResourceComponent", component instanceof MockResourceComponent);
        assertRendererTypeResourceBeanInfo(component, resource, metadata);
        
        // remove the script resource again
        vdl.setScriptComponentResource(resource, null);
        
        // ---- third component test - from libaryName.resourceName.class -----------------------
        
        MockSimpleResource componentResource = new MockSimpleResource(null, "org.apache.myfaces.application",
                null, "MockResourceComponent.xhtml", null,
                new File("src/test/resources/org/apache/myfaces/application"))
        {

            // (non-javadoc)
            // We have to overwrite getURL() here, because it has to deliver a valid URL and
            // we can't get that out of the library and the resource name we set on this resource.
            @Override
            public URL getURL()
            {
                MockServletContext servletContext 
                        = (MockServletContext) FacesContext.getCurrentInstance()
                                .getExternalContext().getContext();
                servletContext.setDocumentRoot(new File("src/test/resources/org/apache/myfaces/application"));
    
                try 
                {
                    return servletContext.getResource("/testlib/composite.xhtml");
                } 
                catch (MalformedURLException e) 
                {
                    return null;
                }
            }
            
        };
        
        // get the BeanInfo metadata
        BeanInfo metadataComponentResource = vdl.getComponentMetadata(facesContext, componentResource);
        
        // create the component
        component = _testApplication.createComponent(facesContext, componentResource);
        
        // asserts for the third component
        Assert.assertTrue("The component has to be a MockResourceComponent", component instanceof MockResourceComponent);
        assertRendererTypeResourceBeanInfo(component, componentResource, metadataComponentResource);
        
        // ---- fourth component test - with a custom componentType ------------------------------
        
        // change the resource
        resource = new MockSimpleResource(null, "testlib", null, "compositeWithComponentType.xhtml", 
                null, new File("src/test/resources/org/apache/myfaces/application"));
        // FIXME resource.setResourceName(resourceName) did not work
        // this can be changed in the next release of MyFaces test (1.0.0-beta.NEXT)
        
        // register the new component type
        _testApplication.addComponent(TEST_COMPONENT_TYPE, UIOutput.class.getName());
        
        // get the BeanInfo metadata
        metadata = vdl.getComponentMetadata(facesContext, resource);
        
        // create the component
        component = _testApplication.createComponent(facesContext, resource);
        
        // asserts for the fourth component
        Assert.assertTrue("The component has to be an instance of UIOutput", component instanceof UIOutput);
        assertRendererTypeResourceBeanInfo(component, resource, metadata);*/
    }
    
}
