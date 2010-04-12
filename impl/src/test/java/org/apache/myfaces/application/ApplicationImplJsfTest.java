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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.el.VariableMapper;
import javax.faces.FactoryFinder;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockVariableMapper;
import org.apache.myfaces.test.mock.MockServletContext;
import org.apache.myfaces.test.mock.resource.MockResource;
import org.apache.myfaces.view.facelets.MockFaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.mock.MockViewDeclarationLanguageFactory;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

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
     * Helper method to assert the RendererType, the Resource and the BeanInfo.
     * @param component
     * @param resource
     * @param metadata
     */
    private static void assertRendererTypeResourceBeanInfo(UIComponent component, Resource resource, BeanInfo metadata)
    {
        assertEquals(COMPOSITE_RENDERER_MSG, "javax.faces.Composite", component.getRendererType());
        assertEquals(RESOURCE_MSG, resource, component.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY));
        assertEquals(BEANINFO_MSG, metadata, component.getAttributes().get(UIComponent.BEANINFO_KEY));
    }

    private ApplicationImpl _testApplication;
    private IMocksControl _mocksControl;
    private FaceletContext _faceletContext;
    
    public ApplicationImplJsfTest(String name)
    {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _mocksControl = EasyMock.createControl();
        
        // create a FaceletContext Mock and put it in the FacesContext
        _faceletContext = _mocksControl.createMock(FaceletContext.class);
        facesContext.getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, _faceletContext);
        
        // create and configure our ApplicationImpl instance
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                MockViewDeclarationLanguageFactory.class.getName());
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        _testApplication = new ApplicationImpl(runtimeConfig);
    }

    @Override
    protected void tearDown() throws Exception
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
        MockResource resource = new MockResource(null, "testlib", null, "composite.xhtml", 
                null, new File("src/test/resources/org/apache/myfaces/application"));
        
        // get the BeanInfo metadata
        BeanInfo metadata = vdl.getComponentMetadata(facesContext, resource);
        
        // create the component
        UIComponent component = _testApplication.createComponent(facesContext, resource);
        
        // asserts for the first component
        assertTrue("The component has to be an UINamingContainer", component instanceof UINamingContainer);
        assertRendererTypeResourceBeanInfo(component, resource, metadata);
        
        // ---- second component test - from a script ------------------------------------------
        
        MockResource scriptResource = new MockResource(null, "testlib", null, 
                "org.apache.myfaces.application.TestResourceComponent.groovy", 
                null, new File("src/test/resources/org/apache/myfaces/application"));
        
        // install the script resource to the VDL-mock
        vdl.setScriptComponentResource(resource, scriptResource);
        
        // create the component
        component = _testApplication.createComponent(facesContext, resource);
        
        // asserts for the second component
        assertTrue("The component has to be a TestResourceComponent", component instanceof TestResourceComponent);
        assertRendererTypeResourceBeanInfo(component, resource, metadata);
        
        // remove the script resource again
        vdl.setScriptComponentResource(resource, null);
        
        // ---- third component test - from libaryName.resourceName.class -----------------------
        
        MockResource componentResource = new MockResource(null, "org.apache.myfaces.application",
                null, "TestResourceComponent.xhtml", null,
                new File("src/test/resources/org/apache/myfaces/application"))
        {

            /* (non-javadoc)
             * We have to overwrite getURL() here, because it has to deliver a valid URL and
             * we can't get that out of the library and the resource name we set on this resource.
             */
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
        assertTrue("The component has to be a TestResourceComponent", component instanceof TestResourceComponent);
        assertRendererTypeResourceBeanInfo(component, componentResource, metadataComponentResource);
        
        // ---- fourth component test - with a custom componentType ------------------------------
        
        // change the resource
        resource = new MockResource(null, "testlib", null, "compositeWithComponentType.xhtml", 
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
        assertTrue("The component has to be an instance of UIOutput", component instanceof UIOutput);
        assertRendererTypeResourceBeanInfo(component, resource, metadata);
    }
    
}
