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

package org.apache.myfaces.view.facelets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ProjectStage;
import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKitFactory;

import org.apache.myfaces.application.ApplicationFactoryImpl;
import org.apache.myfaces.application.ViewHandlerImpl;
import org.apache.myfaces.config.FacesConfigDispenser;
import org.apache.myfaces.config.FacesConfigUnmarshaller;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.Behavior;
import org.apache.myfaces.config.element.ClientBehaviorRenderer;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.element.facelets.FaceletTagLibrary;
import org.apache.myfaces.config.impl.FacesConfigDispenserImpl;
import org.apache.myfaces.config.impl.FacesConfigUnmarshallerImpl;
import org.apache.myfaces.context.PartialViewContextFactoryImpl;
import org.apache.myfaces.application.ViewIdSupport;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.spi.FacesConfigurationProviderFactory;
import org.apache.myfaces.test.base.junit4.AbstractJsfConfigurableMultipleRequestsTestCase;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.apache.myfaces.test.mock.MockExternalContext;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.apache.myfaces.test.mock.MockFacesContextFactory;
import org.apache.myfaces.test.mock.visit.MockVisitContextFactory;
import org.apache.myfaces.view.facelets.impl.FaceletCacheFactoryImpl;
import org.apache.myfaces.view.facelets.mock.MockViewDeclarationLanguageFactory;
import org.apache.myfaces.view.facelets.tag.jsf.TagHandlerDelegateFactoryImpl;

public abstract class FaceletMultipleRequestsTestCase extends AbstractJsfConfigurableMultipleRequestsTestCase
{
    private final String filePath = this.getDirectory();
    protected FacesConfigDispenser dispenser = null;
    protected MockFaceletViewDeclarationLanguage vdl;


    @Override
    protected void setUpServletContextAndSession() throws Exception
    {
        super.setUpServletContextAndSession();
        URI context = this.getContext();
        servletContext.setDocumentRoot(new File(context));
        
        //This params are optional
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME,
                StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("org.apache.myfaces.RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON","false");
        servletContext.addInitParameter("org.apache.myfaces.RENDER_VIEWSTATE_ID","true");
        servletContext.addInitParameter("org.apache.myfaces.STRICT_XHTML_LINKS","true");
        servletContext.addInitParameter("org.apache.myfaces.CONFIG_REFRESH_PERIOD","0");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, "UnitTest");
    }
    
    @Override
    protected void setUpServletRequestAndResponse() throws Exception
    {
        super.setUpServletRequestAndResponse();
        URI context = this.getContext();
        request.setPathElements(context.getPath(), null, context.getPath(), context.getQuery());
    }

    protected URI getContext()
    {
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL url = cl.getResource(this.filePath);
            if (url == null)
            {
                throw new FileNotFoundException(cl.getResource("").getFile()
                        + this.filePath + " was not found");
            }
            else
            {
                return new URI(url.toString());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error Initializing Context", e);
        }
    }

    protected URL getLocalFile(String name) throws FileNotFoundException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(this.filePath + "/" + name);
        if (url == null)
        {
            throw new FileNotFoundException(cl.getResource("").getFile() + name
                    + " was not found");
        }
        return url;
    }

    private String getDirectory()
    {
        return this.getClass().getName().substring(0,
                this.getClass().getName().lastIndexOf('.')).replace('.', '/')
                + "/";
    }

    @Override
    protected void setFactories() throws Exception
    {
        super.setFactories();

        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                ApplicationFactoryImpl.class.getName());
        FactoryFinder.setFactory(FactoryFinder.FACES_CONTEXT_FACTORY,
                "org.apache.myfaces.test.mock.MockFacesContextFactory");
        FactoryFinder.setFactory(FactoryFinder.LIFECYCLE_FACTORY,
                "org.apache.myfaces.test.mock.lifecycle.MockLifecycleFactory");
        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY,
                "org.apache.myfaces.test.mock.MockRenderKitFactory");
        FactoryFinder.setFactory(
                FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                MockViewDeclarationLanguageFactory.class.getName());
        FactoryFinder.setFactory(FactoryFinder.TAG_HANDLER_DELEGATE_FACTORY,
                TagHandlerDelegateFactoryImpl.class.getName());
        FactoryFinder.setFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY,
                PartialViewContextFactoryImpl.class.getName());
        FactoryFinder.setFactory(FactoryFinder.VISIT_CONTEXT_FACTORY, 
                MockVisitContextFactory.class.getName());
        FactoryFinder.setFactory(FactoryFinder.FACELET_CACHE_FACTORY,
                FaceletCacheFactoryImpl.class.getName());
    }
    
    @Override
    protected void setUpExternalContext() throws Exception
    {
        super.setUpExternalContext();

        RuntimeConfig.getCurrentInstance(externalContext).setExpressionFactory(
                createExpressionFactory());
    }
    
    protected ExpressionFactory createExpressionFactory()
    {
        return new MockExpressionFactory();
    }
    
    @Override
    protected void setUpFacesContext() throws Exception
    {
        super.setUpFacesContext();

        //Finally set the ResponseWriter
        ResponseWriter rw = renderKit.createResponseWriter(
                new StringWriter(), null, null);
        facesContext.setResponseWriter(rw);
    }

    @Override
    protected void setUpRenderKit() throws Exception
    {
        super.setUpRenderKit();
        setupComponents();
        setupConvertersAndValidators();
        setupBehaviors();
        setupRenderers();
    }

    @Override
    protected void setUpView() throws Exception
    {
        UIViewRoot root = new UIViewRoot();
        root.setViewId("/test");
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(root);
    }

    @Override
    protected void setUpApplication() throws Exception
    {
        FactoryFinder.setFactory(FactoryFinder.FACES_CONTEXT_FACTORY,
            "org.apache.myfaces.test.mock.MockFacesContextFactory");
        facesContextFactory = (MockFacesContextFactory) FactoryFinder
            .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        facesContext = (MockFacesContext) facesContextFactory.getFacesContext(
                servletContext, request, response, lifecycle);
        if (facesContext.getExternalContext() != null)
        {
            externalContext = (MockExternalContext) facesContext.getExternalContext();
        }

        super.setUpApplication();
        
        ((MockFacesContext) facesContext).setApplication(application);
        
        ViewHandlerImpl viewHandler = (ViewHandlerImpl) application.getViewHandler();
        viewHandler.setViewIdSupport(new ViewIdSupport(facesContext){

            @Override
            public String calculateActionURL(FacesContext facesContext,
                    String viewId)
            {
                return viewId;
            }

            @Override
            public String deriveLogicalViewId(FacesContext context, String viewId)
            {
                return viewId;
            }
            
            @Override
            public String deriveViewId(FacesContext context, String viewId)
            {
                return viewId;
            }
            
        }); 
        
        // Redirect resource request to the directory where the test class is,
        // to make easier test composite components.
        //((ResourceHandlerImpl)application.getResourceHandler()).
        //    setResourceHandlerSupport(new MockResourceHandlerSupport(this.getClass()));
    }
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        //facesContext.setViewRoot(facesContext.getApplication().getViewHandler()
        //        .createView(facesContext, "/test"));
        
        FacesConfigurationProviderFactory factory = FacesConfigurationProviderFactory.
            getFacesConfigurationProviderFactory(externalContext);
        List<FacesConfig> list = factory.getFacesConfigurationProvider(externalContext)
            .getFaceletTaglibFacesConfig(externalContext);
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        for (FacesConfig fc : list)
        {
            for (FaceletTagLibrary lib : fc.getFaceletTagLibraryList())
            {
                runtimeConfig.addFaceletTagLibrary(lib);
            }
        }
        
        vdl = (MockFaceletViewDeclarationLanguage) application.getViewHandler().
            getViewDeclarationLanguage(facesContext,"/test");
        
        externalContext = null;
        facesContext = null;
    }
    
    /*@Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }*/

    protected void loadStandardFacesConfig() throws Exception
    {
        if (dispenser == null)
        {
            InputStream stream = ClassUtils
            .getResourceAsStream("META-INF/standard-faces-config.xml");
            FacesConfigUnmarshaller<? extends FacesConfig> unmarshaller = new FacesConfigUnmarshallerImpl(
                    externalContext);
            dispenser = new FacesConfigDispenserImpl();
            dispenser.feed(unmarshaller.getFacesConfig(stream,
                    "META-INF/standard-faces-config.xml"));
        }
    }
    
    /**
     * Override this methods and add just what it is necessary
     * reduce execution time.
     */
    protected void setupComponents() throws Exception
    {
        loadStandardFacesConfig();
        for (String componentType : dispenser.getComponentTypes())
        {
            application.addComponent(componentType, dispenser
                    .getComponentClass(componentType));
        }
    }
    
    protected void setupBehaviors() throws Exception
    {
        loadStandardFacesConfig();
        for (Behavior behavior : dispenser.getBehaviors())
        {
            application.addBehavior(behavior.getBehaviorId(), behavior.getBehaviorClass());
        }
    }
    
    /**
     * Override this methods and add just what it is necessary
     * reduce execution time.
     */
    protected void setupRenderers() throws Exception
    {
        loadStandardFacesConfig();
        for (Renderer element : dispenser
                .getRenderers(RenderKitFactory.HTML_BASIC_RENDER_KIT))
        {
            javax.faces.render.Renderer renderer;
            try
            {
                renderer = (javax.faces.render.Renderer) ClassUtils
                        .newInstance(element.getRendererClass());
            }
            catch (Throwable e)
            {
                // ignore the failure so that the render kit is configured
                continue;
            }

            renderKit.addRenderer(element.getComponentFamily(), element
                    .getRendererType(), renderer);
        }
        
        for (ClientBehaviorRenderer element : dispenser.getClientBehaviorRenderers(RenderKitFactory.HTML_BASIC_RENDER_KIT))
        {
            javax.faces.render.ClientBehaviorRenderer renderer;
            
            try
            {
                renderer = (javax.faces.render.ClientBehaviorRenderer) ClassUtils
                        .newInstance(element.getRendererClass());
            }
            catch (Throwable e)
            {
                // ignore the failure so that the render kit is configured
                continue;
            }

            renderKit.addClientBehaviorRenderer(element.getRendererType(), renderer);
        }
    }
    
    /**
     * Override this methods and add just what it is necessary
     * reduce execution time.
     */
    protected void setupConvertersAndValidators() throws Exception
    {
        loadStandardFacesConfig();
        for (String validatorId : dispenser.getValidatorIds())
        {
            application.addValidator(validatorId, dispenser
                    .getValidatorClass(validatorId));
        }
        for (String converterId : dispenser.getConverterIds())
        {
            application.addConverter(converterId, dispenser
                    .getConverterClassById(converterId));
        }
        for (String validatorId : dispenser.getValidatorIds())
        {
            application.addValidator(validatorId, dispenser
                    .getValidatorClass(validatorId));
        }
    }

    public URL resolveUrl(String path)
    {
        try
        {
            return new URL(this.getContext().toURL(), path.substring(1));
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
    }
    
    /**
     * Sets the ProjectStage for the test case.
     * @param stage
     * @throws IllegalStateException
     */
    public void setProjectStage(ProjectStage stage) throws IllegalStateException
    {
        try
        {
            Field projectStageField = application.getClass().getDeclaredField("_projectStage");
            projectStageField.setAccessible(true);
            projectStageField.set(application, stage);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not configure ProjectStage for test case", e);
        }
    }

}
