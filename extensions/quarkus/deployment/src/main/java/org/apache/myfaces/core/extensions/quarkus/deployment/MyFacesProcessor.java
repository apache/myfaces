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
package org.apache.myfaces.core.extensions.quarkus.deployment;

import java.io.IOException;
import java.util.Optional;

import javax.faces.application.ProjectStage;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.flow.FlowScoped;
import javax.faces.flow.builder.FlowDefinition;
import javax.faces.model.FacesDataModel;
import javax.faces.push.PushContext;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.faces.view.ViewScoped;
import javax.faces.view.facelets.FaceletsResourceResolver;
import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.cdi.FacesScoped;
import org.apache.myfaces.cdi.JsfApplicationArtifactHolder;
import org.apache.myfaces.cdi.JsfArtifactProducer;
import org.apache.myfaces.cdi.config.FacesConfigBeanHolder;
import org.apache.myfaces.cdi.model.FacesDataModelManager;
import org.apache.myfaces.cdi.view.ViewScopeBeanHolder;
import org.apache.myfaces.cdi.view.ViewTransientScoped;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.config.annotation.CdiAnnotationProviderExtension;
import org.apache.myfaces.config.element.NamedEvent;
import org.apache.myfaces.core.extensions.quarkus.runtime.exception.QuarkusExceptionHandlerFactory;
import org.apache.myfaces.flow.cdi.FlowBuilderFactoryBean;
import org.apache.myfaces.flow.cdi.FlowScopeBeanHolder;
import org.apache.myfaces.push.cdi.PushContextFactoryBean;
import org.apache.myfaces.push.cdi.WebsocketApplicationBean;
import org.apache.myfaces.push.cdi.WebsocketChannelTokenBuilderBean;
import org.apache.myfaces.push.cdi.WebsocketSessionBean;
import org.apache.myfaces.push.cdi.WebsocketViewBean;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.BeanRegistrarBuildItem;
import io.quarkus.arc.deployment.BeanRegistrationPhaseBuildItem;
import io.quarkus.arc.deployment.ContextRegistrarBuildItem;
import io.quarkus.arc.processor.ContextRegistrar;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.AdditionalApplicationArchiveMarkerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.apache.myfaces.core.extensions.quarkus.runtime.MyFacesRecorder;
import org.apache.myfaces.core.extensions.quarkus.runtime.QuarkusFacesInitilializer;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusFacesScopeContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusFlowScopedContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusViewScopeContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusViewTransientScopeContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.spi.QuarkusInjectionProvider;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ProfileManager;
import io.quarkus.undertow.deployment.ListenerBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;
import io.quarkus.undertow.deployment.ServletInitParamBuildItem;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.el.ELResolver;
import javax.enterprise.inject.Produces;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.SystemEvent;
import javax.faces.render.ClientBehaviorRenderer;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.ConverterHandler;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagHandler;
import javax.faces.view.facelets.ValidatorHandler;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.el.ExpressionFactoryImpl;
import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.cdi.util.BeanEntry;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.core.extensions.quarkus.runtime.spi.QuarkusFactoryFinderProvider;
import org.apache.myfaces.el.ELResolverBuilderForFaces;
import org.apache.myfaces.renderkit.ErrorPageWriter;
import org.apache.myfaces.spi.FactoryFinderProviderFactory;
import org.apache.myfaces.spi.impl.DefaultWebConfigProviderFactory;
import org.apache.myfaces.util.ExternalContextUtils;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.view.ViewScopeProxyMap;
import org.apache.myfaces.view.facelets.compiler.SAXCompiler;
import org.apache.myfaces.view.facelets.compiler.TagLibraryConfig;
import org.apache.myfaces.view.facelets.tag.MethodRule;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.webapp.AbstractFacesInitializer;
import org.apache.myfaces.webapp.FaceletsInitilializer;
import org.apache.myfaces.webapp.MyFacesContainerInitializer;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

class MyFacesProcessor
{

    private static final Class[] BEAN_CLASSES =
    {
            JsfApplicationArtifactHolder.class,
            JsfArtifactProducer.class,
            FacesConfigBeanHolder.class,
            FacesDataModelManager.class,
            ViewScopeBeanHolder.class,
            CdiAnnotationProviderExtension.class,
            PushContextFactoryBean.class,
            WebsocketChannelTokenBuilderBean.class,
            WebsocketSessionBean.class,
            WebsocketViewBean.class,
            WebsocketApplicationBean.class,
            FlowBuilderFactoryBean.class,
            FlowScopeBeanHolder.class
    };

    private static final String[] BEAN_DEFINING_ANNOTATION_CLASSES =
    {
            FacesComponent.class.getName(),
            FacesBehavior.class.getName(),
            FacesConverter.class.getName(),
            FacesValidator.class.getName(),
            FacesRenderer.class.getName(),
            NamedEvent.class.getName(),
            FacesBehaviorRenderer.class.getName(),
            FaceletsResourceResolver.class.getName(),
            FlowDefinition.class.getName()
    };
    
    private static final String[] FACTORIES =
    {
        FactoryFinder.APPLICATION_FACTORY,
        FactoryFinder.EXCEPTION_HANDLER_FACTORY,
        FactoryFinder.EXTERNAL_CONTEXT_FACTORY,
        FactoryFinder.FACES_CONTEXT_FACTORY,
        FactoryFinder.LIFECYCLE_FACTORY,
        FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY,
        FactoryFinder.RENDER_KIT_FACTORY,
        FactoryFinder.TAG_HANDLER_DELEGATE_FACTORY,
        FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
        FactoryFinder.VISIT_CONTEXT_FACTORY,
        FactoryFinder.FACELET_CACHE_FACTORY,
        FactoryFinder.FLASH_FACTORY,
        FactoryFinder.FLOW_HANDLER_FACTORY,
        FactoryFinder.CLIENT_WINDOW_FACTORY,
        FactoryFinder.SEARCH_EXPRESSION_CONTEXT_FACTORY,
        QuarkusExceptionHandlerFactory.class.getName()
    };

    @BuildStep
    void buildFeature(BuildProducer<FeatureBuildItem> feature) throws IOException
    {
        feature.produce(new FeatureBuildItem("myfaces"));
    }

    @BuildStep
    void buildServlet(BuildProducer<FeatureBuildItem> feature,
            BuildProducer<ServletBuildItem> servlet,
            BuildProducer<ListenerBuildItem> listener) throws IOException
    {
        servlet.produce(ServletBuildItem.builder("Faces Servlet", FacesServlet.class.getName())
                .addMapping("*.xhtml")
                .build());

        // sometimes Quarkus doesn't scan web-fragments?! lets add it manually
        listener.produce(new ListenerBuildItem(StartupServletContextListener.class.getName()));
    }

    @BuildStep
    void buildCdiBeans(BuildProducer<AdditionalBeanBuildItem> additionalBean,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefiningAnnotation) throws IOException
    {
        for (Class<?> clazz : BEAN_CLASSES)
        {
            additionalBean.produce(AdditionalBeanBuildItem.unremovableOf(clazz));
        }

        for (String clazz : BEAN_DEFINING_ANNOTATION_CLASSES)
        {
            beanDefiningAnnotation.produce(new BeanDefiningAnnotationBuildItem(DotName.createSimple(clazz)));
        }

    }

    @BuildStep
    void buildCdiScopes(BuildProducer<ContextRegistrarBuildItem> contextRegistrar) throws IOException
    {
        contextRegistrar.produce(new ContextRegistrarBuildItem(new ContextRegistrar()
        {
            @Override
            public void register(ContextRegistrar.RegistrationContext registrationContext)
            {
                registrationContext.configure(ViewScoped.class)
                        .normal()
                        .contextClass(QuarkusViewScopeContext.class)
                        .done();
                registrationContext.configure(FacesScoped.class)
                        .normal()
                        .contextClass(QuarkusFacesScopeContext.class)
                        .done();
                registrationContext.configure(ViewTransientScoped.class)
                        .normal()
                        .contextClass(QuarkusViewTransientScopeContext.class)
                        .done();
                registrationContext.configure(FlowScoped.class)
                        .normal()
                        .contextClass(QuarkusFlowScopedContext.class)
                        .done();
            }
        }));
    }

    @BuildStep
    void buildInitParams(BuildProducer<ServletInitParamBuildItem> initParam) throws IOException
    {
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.INJECTION_PROVIDER, QuarkusInjectionProvider.class.getName()));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.FACES_INITIALIZER, QuarkusFacesInitilializer.class.getName()));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.SUPPORT_JSP, "false"));
    }

    @BuildStep
    void buildRecommendedInitParams(BuildProducer<ServletInitParamBuildItem> initParam) throws IOException
    {
        // user config
        Config config = ConfigProvider.getConfig();

        Optional<String> projectStage = resolveProjectStage(config);
        initParam.produce(new ServletInitParamBuildItem(ProjectStage.PROJECT_STAGE_PARAM_NAME, projectStage.get()));

        Optional<String> enableWebsocketsEndpoint = config.getOptionalValue(
                PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME,
                String.class);
        if (enableWebsocketsEndpoint.isPresent())
        {
            initParam.produce(new ServletInitParamBuildItem(PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME,
                    enableWebsocketsEndpoint.get()));
        }

        // common
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.LOG_WEB_CONTEXT_PARAMS, "false"));
        initParam.produce(new ServletInitParamBuildItem(
                StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER));
        initParam.produce(new ServletInitParamBuildItem(
                StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, "false"));

        // perf
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.CHECK_ID_PRODUCTION_MODE, "false"));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.EARLY_FLUSH_ENABLED, "true"));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.CACHE_EL_EXPRESSIONS, "alwaysRecompile"));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.COMPRESS_STATE_IN_SESSION, "false"));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION, "15"));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION, "3"));

        // MyFaces uses default 0, which means always recompile
        if (ProjectStage.valueOf(projectStage.get()) == ProjectStage.Development)
        {
            initParam.produce(new ServletInitParamBuildItem(
                    ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME, "1"));
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void buildAnnotationProviderIntegration(MyFacesRecorder recorder, CombinedIndexBuildItem combinedIndex)
            throws IOException
    {
        for (String clazz : BEAN_DEFINING_ANNOTATION_CLASSES)
        {
            combinedIndex.getIndex()
                    .getAnnotations(DotName.createSimple(clazz))
                    .stream()
                    .forEach(annotation -> recorder.registerAnnotatedClass(annotation.name().toString(),
                            annotation.target().asClass().name().toString()));
        }
    }

    private Optional<String> resolveProjectStage(Config config)
    {
        Optional<String> projectStage = config.getOptionalValue(ProjectStage.PROJECT_STAGE_PARAM_NAME,
                String.class);
        if (!projectStage.isPresent())
        {
            projectStage = Optional.of(ProjectStage.Production.name());
            if (LaunchMode.DEVELOPMENT.getDefaultProfile().equals(ProfileManager.getActiveProfile()))
            {
                projectStage = Optional.of(ProjectStage.Development.name());
            }
            else if (LaunchMode.TEST.getDefaultProfile().equals(ProfileManager.getActiveProfile()))
            {
                projectStage = Optional.of(ProjectStage.SystemTest.name());
            }
        }
        return projectStage;
    }

    @BuildStep
    void buildMangedPropertyProducers(BeanRegistrationPhaseBuildItem beanRegistrationPhase,
            BuildProducer<BeanRegistrationPhaseBuildItem.BeanConfiguratorBuildItem> beanConfigurators)
            throws IOException
    {
        ManagedPropertyBuildStep.build(beanRegistrationPhase, beanConfigurators);
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void buildFacesDataModels(MyFacesRecorder recorder,
            BuildProducer<BeanRegistrarBuildItem> beanConfigurators,
            CombinedIndexBuildItem combinedIndex) throws IOException
    {
        for (AnnotationInstance ai : combinedIndex.getIndex()
                .getAnnotations(DotName.createSimple(FacesDataModel.class.getName())))
        {
            AnnotationValue forClass = ai.value("forClass");
            if (forClass != null)
            {
                recorder.registerFacesDataModel(
                        ai.target().asClass().name().toString(),
                        forClass.asClass().name().toString());
            }
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void buildFlowScopedMapping(MyFacesRecorder recorder,
            CombinedIndexBuildItem combinedIndex) throws IOException
    {
        for (AnnotationInstance ai : combinedIndex.getIndex()
                .getAnnotations(DotName.createSimple(FlowScoped.class.getName())))
        {
            AnnotationValue flowId = ai.value("value");
            if (flowId != null && flowId.asString() != null)
            {
                AnnotationValue definingDocumentId = ai.value("definingDocumentId");
                recorder.registerFlowReference(ai.target().asClass().name().toString(),
                        definingDocumentId == null ? "" : definingDocumentId.asString(),
                        flowId.asString());
            }
        }
    }

    @BuildStep
    void produceApplicationArchiveMarker(
            BuildProducer<AdditionalApplicationArchiveMarkerBuildItem> additionalArchiveMarkers)
    {
        additionalArchiveMarkers.produce(new AdditionalApplicationArchiveMarkerBuildItem("javax/faces/component"));
        additionalArchiveMarkers.produce(new AdditionalApplicationArchiveMarkerBuildItem("org/apache/myfaces/view"));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void registerForLimitedReflection(MyFacesRecorder recorder,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex)
    {
        List<String> classNames = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();
       
        classNames.addAll(collectSubclasses(combinedIndex, Renderer.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, ClientBehaviorRenderer.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, javax.el.ValueExpression.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, SystemEvent.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, FacesContext.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, Application.class.getName()));

        // Web.xml parsing
        classNames.addAll(collectSubclasses(combinedIndex, DocumentBuilderFactory.class.getName()));
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncLocalPart");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncNot");
        
        for (String factory : FACTORIES)
        {
            classNames.addAll(collectSubclasses(combinedIndex, factory));
        }

        classNames.addAll(Arrays.asList(
            "javax.faces.component._DeltaStateHelper",
            "javax.faces.component._DeltaStateHelper$InternalMap",
            "org.apache.myfaces.application._ApplicationUtils"));

        classes.addAll(Arrays.asList(
                DefaultWebConfigProviderFactory.class,
                ErrorPageWriter.class,
                MyFacesContainerInitializer.class,
                ExceptionQueuedEventContext.class,
                FacesConfigurator.class,
                FaceletsInitilializer.class,
                TagLibraryConfig.class,
                String.class,
                ViewScopeProxyMap.class,
                SAXCompiler.class,
                StateUtils.class,
                ExpressionFactoryImpl.class));

        reflectiveClass.produce(
                new ReflectiveClassBuildItem(false, false, classNames.toArray(new String[classNames.size()])));
        reflectiveClass.produce(
                new ReflectiveClassBuildItem(false, false, classes.toArray(new Class[classes.size()])));
    }
    
    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void registerForMethodReflection(MyFacesRecorder recorder, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
                               CombinedIndexBuildItem combinedIndex)
    {        
        List<String> classNames = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();
        
        classNames.add("javax.faces._FactoryFinderProviderFactory");
        
        classNames.addAll(collectSubclasses(combinedIndex, TagHandler.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, ConverterHandler.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, ComponentHandler.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, ValidatorHandler.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, UIComponent.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, ELResolver.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, MethodRule.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, MetaRuleset.class.getName()));

        classNames.addAll(collectImplementors(combinedIndex, Converter.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, Validator.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, Behavior.class.getName()));

        classes.addAll(Arrays.asList(
                ClassUtils.class,
                FactoryFinderProviderFactory.class,
                ComponentSupport.class,
                QuarkusFactoryFinderProvider.class,
                ELResolverBuilderForFaces.class,
                AbstractFacesInitializer.class,
                ExternalContextUtils.class,
                BeanEntry.class));
        
        reflectiveClass.produce(
                new ReflectiveClassBuildItem(true, false, classNames.toArray(new String[classNames.size()])));
        reflectiveClass.produce(
                new ReflectiveClassBuildItem(true, false, classes.toArray(new Class[classes.size()])));
    }

    
    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void registerForFieldReflection(MyFacesRecorder recorder, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
                               CombinedIndexBuildItem combinedIndex)
    {     
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, true, 
                "javax.faces.context._MyFacesExternalContextHelper"));
    }

    @BuildStep
    void substrateResourceBuildItems(BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
                                     BuildProducer<NativeImageResourceBundleBuildItem> resourceBundleBuildItem)
    {
        nativeImageResourceProducer.produce(new NativeImageResourceBuildItem(
                "META-INF/rsc/myfaces-dev-error.xml",
                "META-INF/rsc/myfaces-dev-debug.xml", 
                "org/apache/myfaces/resource/default.dtd",
                "org/apache/myfaces/resource/datatypes.dtd", 
                "META-INF/web-fragment.xml",
                "META-INF/resources/org/apache/myfaces/windowId/windowhandler.html",
                "org/apache/myfaces/resource/facelet-taglib_1_0.dtd", 
                "org/apache/myfaces/resource/javaee_5.xsd",
                "org/apache/myfaces/resource/web-facelettaglibrary_2_0.xsd",
                "org/apache/myfaces/resource/XMLSchema.dtd", 
                "org/apache/myfaces/resource/facesconfig_1_0.dtd",
                "org/apache/myfaces/resource/web-facesconfig_1_1.dtd",
                "org/apache/myfaces/resource/web-facesconfig_1_2.dtd", 
                "org/apache/myfaces/resource/web-facesconfig_2_0.dtd",
                "org/apache/myfaces/resource/web-facesconfig_2_1.dtd",
                "org/apache/myfaces/resource/web-facesconfig_2_2.dtd", 
                "org/apache/myfaces/resource/web-facesconfig_2_3.dtd",
                "org/apache/myfaces/resource/web-facesconfig_3_0.dtd",
                "org/apache/myfaces/resource/xml.xsd",
                "META-INF/rsc/myfaces-dev-error-include.xml",
                "META-INF/services/javax.servlet.ServletContainerInitializer"));

        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_ar"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_ca"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_cs"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_de"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_en"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_es"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_fr"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_it"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_ja"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_mt"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_nl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_pl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_pt_PR"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_ru"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_sk"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_zh_CN"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_zh_HK"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.faces.Messages_zh_TW"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.el.PrivateMessages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.servlet.LocalStrings"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("javax.el.LocalStrings"));
    }
    
    public List<String> collectSubclasses(CombinedIndexBuildItem combinedIndex, String className)
    {
        List<String> classes = combinedIndex.getIndex()
                .getAllKnownSubclasses(DotName.createSimple(className))
                .stream()
                .map(ClassInfo::toString)
                .collect(Collectors.toList());
        classes.add(className);
        return classes;
    }

    public List<String> collectImplementors(CombinedIndexBuildItem combinedIndex, String className)
    {
        List<String> classes = combinedIndex.getIndex()
                .getAllKnownImplementors(DotName.createSimple(className))
                .stream()
                .map(ClassInfo::toString)
                .collect(Collectors.toList());
        classes.add(className);
        return classes;
    }


    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void registerWebappClassesForReflection(MyFacesRecorder recorder,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex)
    {
        List<ClassInfo> types = new ArrayList<>();
        List<String> typeNames = new ArrayList<>();

        DotName produces = DotName.createSimple(Produces.class.getName());
        
        // loop all @Named beans and @Produces
        for (AnnotationInstance ai :
                combinedIndex.getIndex().getAnnotations(DotName.createSimple(Named.class.getName())))
        {
            if (ai.target().kind() == AnnotationTarget.Kind.CLASS)
            {
                types.add(ai.target().asClass());
            } 
            else if (ai.target().kind() == AnnotationTarget.Kind.FIELD)
            {
                if (ai.target().asField().annotation(produces) != null)
                {
                    try
                    {
                        types.add(ai.target().asField().asClass());
                    }
                    catch (Exception e)
                    {
                        try
                        {
                            // extract the class name via the toString()... there is no other way?
                            String className = ai.target().asField().toString();
                            className = className.substring(0, className.indexOf("<"));

                            ClassInfo ci = combinedIndex.getIndex().getClassByName(DotName.createSimple(className));
                            if (ci == null)
                            {
                                //try to add the name
                                typeNames.add(className);
                            }
                            else
                            {
                                types.add(ci);
                            }
                        }
                        catch (Exception e1)
                        {
                            // no class, also no generic type -> ignore
                        }
                    }
                }
            }
            else if (ai.target().kind() == AnnotationTarget.Kind.METHOD)
            {
                if (ai.target().asMethod().annotation(produces) != null)
                {
                    if (ai.target().asMethod().returnType().kind() == Type.Kind.CLASS)
                    {
                        ClassInfo ci = combinedIndex.getIndex().getClassByName(
                                ai.target().asMethod().returnType().asClassType().name());
                        if (ci == null)
                        {
                            //try to add the name
                            typeNames.add(ai.target().asMethod().returnType().asClassType().name().toString()); 
                        }
                        else
                        {
                            types.add(ci);
                        }
                    }
                    else if (ai.target().asMethod().returnType().kind() == Type.Kind.PARAMETERIZED_TYPE)
                    {
                        // extract the class name via the toString()...
                        // ai.target().asMethod().returnType().asParameterizedType().owner() returns null?!
                        String className = ai.target().asMethod().returnType().asParameterizedType().toString();
                        className = className.substring(0, className.indexOf("<"));

                        ClassInfo ci = combinedIndex.getIndex().getClassByName(DotName.createSimple(className));
                        if (ci == null)
                        {
                            //try to add the name
                            typeNames.add(className);
                        }
                        else
                        {
                            types.add(ci);
                        }
                    }
                }
            }
        }

        // sort our duplicate
        types = types.stream().distinct().collect(Collectors.toList());
        types.removeIf(ci -> ci == null);

        // collect all public types from getters and fields
        List<ClassInfo> temp = new ArrayList();
        types.stream().forEach(ci -> collectPublicTypes(ci, temp, combinedIndex));
        types.addAll(temp);

        // sort our duplicate
        types = types.stream().distinct().collect(Collectors.toList());
        types.removeIf(ci -> ci == null);

        for (ClassInfo type : types)
        {
            // register type
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, type.name().toString()));
            // and try to register the ClientProxy
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, type.name().toString() + "_ClientProxy"));
        }
        

        // sort our duplicate
        typeNames = typeNames.stream().distinct().collect(Collectors.toList());
        typeNames.removeIf(ci -> ci == null || ci.trim().isEmpty());

        for (String typeName : typeNames)
        {
            // register type
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, typeName));
            // and try to register the ClientProxy
            reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, typeName + "_ClientProxy"));
        }
    }
    
    void collectPublicTypes(ClassInfo type, List<ClassInfo> publicTypes, CombinedIndexBuildItem combinedIndex)
    {
        if (type == null)
        {
            return;
        }
        
        ClassInfo ci = (ClassInfo) type;
        for (MethodInfo mi : ci.methods())
        {
            if (Modifier.isPublic(mi.flags()) && mi.name().startsWith("get"))
            {
                ClassInfo returnType =
                        combinedIndex.getIndex().getClassByName(mi.returnType().name());
                if (returnType == null || publicTypes.contains(returnType))
                {
                    continue;
                }
                publicTypes.add(returnType);
                collectPublicTypes(returnType, publicTypes, combinedIndex);
            }
        }
        for (FieldInfo fi : ci.fields())
        {
            if (Modifier.isPublic(fi.flags()) && !Modifier.isStatic(fi.flags()))
            {
                ClassInfo fieldType =
                        combinedIndex.getIndex().getClassByName(fi.type().name());
                if (fieldType == null || publicTypes.contains(fieldType))
                {
                    continue;
                }
                publicTypes.add(fieldType);
                collectPublicTypes(fieldType, publicTypes, combinedIndex);
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    @BuildStep
    void buildPrimeFacesRecommendedInitParams(BuildProducer<ServletInitParamBuildItem> initParam) throws IOException
    {
        initParam.produce(new ServletInitParamBuildItem(
                "primefaces.SUBMIT", "partial"));
        initParam.produce(new ServletInitParamBuildItem(
                "primefaces.MOVE_SCRIPTS_TO_BOTTOM", "true"));
    }

    @BuildStep
    void producePrimeFacesApplicationArchiveMarker(
            BuildProducer<AdditionalApplicationArchiveMarkerBuildItem> additionalArchiveMarkers)
    {
        additionalArchiveMarkers.produce(new AdditionalApplicationArchiveMarkerBuildItem("org/primefaces/component"));
    }
    
    @BuildStep
    void substratePrimeFacesResourceBuildItems(BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
                                     BuildProducer<NativeImageResourceBundleBuildItem> resourceBundleBuildItem)
    {
        nativeImageResourceProducer
                .produce(new NativeImageResourceBuildItem("META-INF/maven/org.primefaces/primefaces/pom.properties"));
        

        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.primefaces.Messages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.primefaces.Messages_en"));
    }
    
    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void registerPrimeFaces(MyFacesRecorder recorder,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex)
    {
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false,
                "org.primefaces.util.ComponentUtils","org.primefaces.util.ComponentTraversalUtils",
                "org.primefaces.expression.SearchExpressionUtils","org.primefaces.util.EscapeUtils",
                "org.primefaces.util.SecurityUtils",
                "org.primefaces.util.LangUtils"));
        
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, 
                "org.primefaces.config.PrimeEnvironment",
                "org.primefaces.util.MessageFactory",
                "com.lowagie.text.pdf.MappedRandomAccessFile"));
    }
    
    
}
