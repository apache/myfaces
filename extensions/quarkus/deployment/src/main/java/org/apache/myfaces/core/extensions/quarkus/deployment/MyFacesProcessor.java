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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import jakarta.el.ELResolver;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.flow.FlowScoped;
import jakarta.faces.flow.builder.FlowDefinition;
import jakarta.faces.model.FacesDataModel;
import jakarta.faces.render.ClientBehaviorRenderer;
import jakarta.faces.render.FacesBehaviorRenderer;
import jakarta.faces.render.FacesRenderer;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagHandler;
import jakarta.faces.view.facelets.ValidatorHandler;
import jakarta.faces.webapp.FacesServlet;
import jakarta.inject.Named;
import jakarta.servlet.MultipartConfigElement;

import org.apache.el.ExpressionFactoryImpl;
import org.apache.myfaces.application.ApplicationImplEventManager;
import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.cdi.FacesApplicationArtifactHolder;
import org.apache.myfaces.cdi.FacesArtifactProducer;
import org.apache.myfaces.cdi.FacesScoped;
import org.apache.myfaces.cdi.config.FacesConfigBeanHolder;
import org.apache.myfaces.cdi.model.FacesDataModelManager;
import org.apache.myfaces.cdi.util.BeanEntry;
import org.apache.myfaces.cdi.view.ViewScopeContextualStorageHolder;
import org.apache.myfaces.cdi.view.ViewScopeEventListenerBridge;
import org.apache.myfaces.cdi.view.ViewTransientScoped;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.config.annotation.CdiAnnotationProviderExtension;
import org.apache.myfaces.config.element.NamedEvent;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.core.api.shared.lang.PropertyDescriptorUtils;
import org.apache.myfaces.core.extensions.quarkus.runtime.MyFacesRecorder;
import org.apache.myfaces.core.extensions.quarkus.runtime.QuarkusFacesInitializer;
import org.apache.myfaces.core.extensions.quarkus.runtime.exception.QuarkusExceptionHandlerFactory;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusFacesScopeContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusFlowScopedContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusViewScopeContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.scopes.QuarkusViewTransientScopeContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.spi.QuarkusFactoryFinderProvider;
import org.apache.myfaces.core.extensions.quarkus.runtime.spi.QuarkusInjectionProvider;
import org.apache.myfaces.el.DefaultELResolverBuilder;
import org.apache.myfaces.el.resolver.LambdaBeanELResolver;
import org.apache.myfaces.flow.cdi.FlowScopeContextualStorageHolder;
import org.apache.myfaces.push.cdi.WebsocketChannelTokenBuilder;
import org.apache.myfaces.push.cdi.WebsocketScopeManager;
import org.apache.myfaces.push.cdi.WebsocketSessionManager;
import org.apache.myfaces.renderkit.ErrorPageWriter;
import org.apache.myfaces.spi.FactoryFinderProviderFactory;
import org.apache.myfaces.spi.impl.DefaultWebConfigProviderFactory;
import org.apache.myfaces.util.ExternalContextUtils;
import org.apache.myfaces.util.WebXmlParser;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.view.ViewScopeProxyMap;
import org.apache.myfaces.view.facelets.compiler.SAXCompiler;
import org.apache.myfaces.view.facelets.compiler.TagLibraryConfig;
import org.apache.myfaces.view.facelets.tag.LambdaMetadataTargetImpl;
import org.apache.myfaces.view.facelets.tag.MethodRule;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.jstl.fn.JstlFunction;
import org.apache.myfaces.webapp.FacesInitializerImpl;
import org.apache.myfaces.webapp.MyFacesContainerInitializer;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.WebMetaData;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.BeanRegistrationPhaseBuildItem;
import io.quarkus.arc.deployment.ContextRegistrationPhaseBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.AdditionalApplicationArchiveMarkerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.undertow.deployment.ListenerBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;
import io.quarkus.undertow.deployment.ServletInitParamBuildItem;
import io.quarkus.undertow.deployment.WebMetadataBuildItem;

class MyFacesProcessor
{

    private static final Class[] BEAN_CLASSES =
    {
            FacesApplicationArtifactHolder.class,
            FacesArtifactProducer.class,
            FacesConfigBeanHolder.class,
            FacesDataModelManager.class,
            ViewScopeContextualStorageHolder.class,
            ViewScopeEventListenerBridge.class,
            CdiAnnotationProviderExtension.class,
            WebsocketChannelTokenBuilder.class,
            WebsocketSessionManager.class,
            WebsocketScopeManager.class,
            FlowScopeContextualStorageHolder.class
    };

    private static final String[] BEAN_DEFINING_ANNOTATION_CLASSES =
    {
            Named.class.getName(),
            FacesComponent.class.getName(),
            FacesBehavior.class.getName(),
            FacesConverter.class.getName(),
            FacesValidator.class.getName(),
            FacesRenderer.class.getName(),
            NamedEvent.class.getName(),
            FacesBehaviorRenderer.class.getName(),
            FaceletHandler.class.getName(),
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
    void buildServlet(WebMetadataBuildItem  webMetaDataBuildItem,
            BuildProducer<FeatureBuildItem> feature,
            BuildProducer<ServletBuildItem> servlet,
            BuildProducer<ListenerBuildItem> listener) throws IOException
    {
        WebMetaData webMetaData = webMetaDataBuildItem.getWebMetaData();
        ServletMetaData facesServlet = null;
        if (webMetaData.getServlets() != null)
        {
            facesServlet = webMetaData.getServlets().stream()
                .filter(servletMeta -> FacesServlet.class.getName().equals(servletMeta.getServletClass()))
                .findFirst()
                .orElse(null);
        }
        if (facesServlet == null)
        {
            // Only define here if not explictly defined in web.xml
            servlet.produce(ServletBuildItem.builder("Faces Servlet", FacesServlet.class.getName())
                    .setMultipartConfig(new MultipartConfigElement(""))
                    .addMapping("*.xhtml")
                    .build());
        }

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
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerViewScopedContext(
            ContextRegistrationPhaseBuildItem phase)
    {
            return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
                    phase.getContext()
                            .configure(ViewScoped.class)
                            .normal()
                            .contextClass(QuarkusViewScopeContext.class));
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerFacesScopedContext(
            ContextRegistrationPhaseBuildItem phase)
    {
            return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
                    phase.getContext()
                            .configure(FacesScoped.class)
                            .normal()
                            .contextClass(QuarkusFacesScopeContext.class));
    }
 
    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerViewTransientScopedContext(
            ContextRegistrationPhaseBuildItem phase)
    {
            return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
                    phase.getContext()
                            .configure(ViewTransientScoped.class)
                            .normal()
                            .contextClass(QuarkusViewTransientScopeContext.class));
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerFlowScopedContext(
            ContextRegistrationPhaseBuildItem phase)
    {
            return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
                    phase.getContext()
                            .configure(FlowScoped.class)
                            .normal()
                            .contextClass(QuarkusFlowScopedContext.class));
    }

    @BuildStep
    void buildInitParams(BuildProducer<ServletInitParamBuildItem> initParam) throws IOException
    {
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.INJECTION_PROVIDER, QuarkusInjectionProvider.class.getName()));
        initParam.produce(new ServletInitParamBuildItem(
                MyfacesConfig.FACES_INITIALIZER, QuarkusFacesInitializer.class.getName()));
    }

    @BuildStep
    void buildRecommendedInitParams(BuildProducer<ServletInitParamBuildItem> initParam) throws IOException
    {
        // user config
        Config config = ConfigProvider.getConfig();

        Optional<String> projectStage = resolveProjectStage(config);
        initParam.produce(new ServletInitParamBuildItem(ProjectStage.PROJECT_STAGE_PARAM_NAME, projectStage.get()));
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
                    .forEach(annotation ->
                    {
                        if (annotation.target().kind() == AnnotationTarget.Kind.CLASS)
                        {
                            recorder.registerAnnotatedClass(annotation.name().toString(),
                                    annotation.target().asClass().name().toString());
                        }
                    });
        }
    }

    private Optional<String> resolveProjectStage(Config config)
    {
        Optional<String> projectStage = config.getOptionalValue(ProjectStage.PROJECT_STAGE_PARAM_NAME,
                String.class);
        if (!projectStage.isPresent())
        {
            projectStage = Optional.of(ProjectStage.Production.name());
            if (ConfigUtils.getProfiles().contains(LaunchMode.DEVELOPMENT.getDefaultProfile()))
            {
                projectStage = Optional.of(ProjectStage.Development.name());
            }
            else if (ConfigUtils.getProfiles().contains(LaunchMode.TEST.getDefaultProfile()))
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
        additionalArchiveMarkers.produce(new AdditionalApplicationArchiveMarkerBuildItem("jakarta/faces/component"));
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
        classNames.addAll(collectSubclasses(combinedIndex, jakarta.el.ValueExpression.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, SystemEvent.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, FacesContext.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, Application.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, StateHolder.class.getName()));

        // Web.xml parsing
        classNames.addAll(collectSubclasses(combinedIndex, DocumentBuilderFactory.class.getName()));
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncLocalPart");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncNot");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncBoolean");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncCeiling");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncConcat");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncContains");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncCount");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncCurrent");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncDoclocation");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncExtElementAvailable");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncExtFunction");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncFalse");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncFloor");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncGenerateId");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncHere");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncId");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncLang");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncLast");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncLocalPart");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncNamespace");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncNot");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncNumber");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncPosition");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncQname");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncRound");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncStartsWith");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncString");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncStringLength");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncSubstring");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncSubstringAfter");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncSubstringBefore");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncSum");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncSystemProperty");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncTranslate");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncTrue");
        classNames.add("com.sun.org.apache.xpath.internal.functions.FuncUnparsedEntityURI");
        classNames.add("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");

        for (String factory : FACTORIES)
        {
            classNames.addAll(collectSubclasses(combinedIndex, factory));
        }

        classNames.addAll(Arrays.asList(
            "jakarta.faces.component._DeltaStateHelper",
            "jakarta.faces.component._DeltaStateHelper$InternalMap",
            "jakarta.validation.groups.Default",
            "jakarta.validation.Validation"));

        classes.addAll(Arrays.asList(ApplicationImplEventManager.class,
                DefaultWebConfigProviderFactory.class,
                ErrorPageWriter.class,
                MyFacesContainerInitializer.class,
                ExceptionQueuedEventContext.class,
                FacesConfigurator.class,
                FacesInitializerImpl.class,
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

        classNames.add("jakarta.faces._FactoryFinderProviderFactory");
        classNames.addAll(collectImplementors(combinedIndex, java.util.Collection.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, java.time.temporal.TemporalAccessor.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, java.lang.Number.class.getName()));
        classNames.add(java.util.Date.class.getName());
        classNames.add(java.util.Calendar.class.getName());
        classNames.add(java.lang.Iterable.class.getName());
        classNames.add(java.lang.Throwable.class.getName());
        classNames.add(java.lang.String.class.getName());

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

        // Register CDI produced servlet objects for EL #{session} and #{request}
        classes.addAll(Arrays.asList(
                io.undertow.servlet.spec.HttpServletRequestImpl.class,
                io.undertow.servlet.spec.HttpServletResponseImpl.class,
                io.undertow.servlet.spec.HttpSessionImpl.class));

        classes.addAll(Arrays.asList(
                BeanEntry.class,
                ClassUtils.class,
                ComponentSupport.class,
                DefaultELResolverBuilder.class,
                ExternalContextUtils.class,
                FacesInitializerImpl.class,
                FactoryFinderProviderFactory.class,
                JstlFunction.class,
                QuarkusFactoryFinderProvider.class));

        reflectiveClass.produce(
                new ReflectiveClassBuildItem(true, false, classNames.toArray(new String[classNames.size()])));
        reflectiveClass.produce(
                new ReflectiveClassBuildItem(true, false, classes.toArray(new Class[classes.size()])));
    }

    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    void registerErrorPageClassesForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex)
    {
        final Set<String> classNames = new HashSet<>();
        classNames.add(jakarta.faces.application.ViewExpiredException.class.getName());

        Map<String, String> errorPages = WebXmlParser.getErrorPages(null);
        for (String key : errorPages.keySet())
        {
            if (key != null)
            {
                classNames.add(key);
            }
        }

        reflectiveClass.produce(new ReflectiveClassBuildItem(true, true, classNames.toArray(new String[0])));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    NativeImageConfigBuildItem registerForFieldReflection(MyFacesRecorder recorder,
                               BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
                               CombinedIndexBuildItem combinedIndex)
    {
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, true,
                "jakarta.faces.context._MyFacesExternalContextHelper"));

        // Register ViewScopeContextualStorageHolder to be initialized at runtime, it uses a static random
        NativeImageConfigBuildItem.Builder builder = NativeImageConfigBuildItem.builder();
        builder.addRuntimeInitializedClass(ViewScopeContextualStorageHolder.class.getName());

        return builder.build();
    }

    @BuildStep
    void substrateResourceBuildItems(BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
                                     BuildProducer<NativeImageResourceBundleBuildItem> resourceBundleBuildItem)
    {
        nativeImageResourceProducer.produce(new NativeImageResourceBuildItem(
                "META-INF/rsc/myfaces-dev-error.xml",
                "META-INF/rsc/myfaces-dev-debug.xml",
                "META-INF/rsc/myfaces-dev-error-include.xhtml",
                "org/apache/myfaces/resource/default.dtd",
                "org/apache/myfaces/resource/datatypes.dtd",
                "META-INF/web-fragment.xml",
                "META-INF/web.xml",
                "META-INF/standard-faces-config.xml",
                "META-INF/resources/org/apache/myfaces/windowId/windowhandler.html",
                "org/apache/myfaces/resource/facelet-taglib_1_0.dtd",
                "org/apache/myfaces/resource/javaee_5.xsd",
                "org/apache/myfaces/resource/jakartaee_9.xsd",
                "org/apache/myfaces/resource/jakartaee_10.xsd",
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
                "org/apache/myfaces/resource/web-facesconfig_4_0.dtd",
                "org/apache/myfaces/resource/xml.xsd",
                "META-INF/services/jakarta.servlet.ServletContainerInitializer"));

        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_ar"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_ca"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_cs"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_de"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_en"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_es"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_fr"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_it"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_ja"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_mt"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_nl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_pl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_pt"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_pt_BR"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_ru"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_sk"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_uk"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_zh_CN"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_zh_HK"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.faces.Messages_zh_TW"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.el.PrivateMessages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.servlet.LocalStrings"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jakarta.el.LocalStrings"));
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

        ClassInfo ci = type;
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
    void registerRuntimeInitialization(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitClassBuildItem)
    {
        runtimeInitClassBuildItem.produce(
                new RuntimeInitializedClassBuildItem(LambdaBeanELResolver.class.getCanonicalName()));
        runtimeInitClassBuildItem.produce(
                new RuntimeInitializedClassBuildItem(LambdaMetadataTargetImpl.class.getCanonicalName()));
        runtimeInitClassBuildItem.produce(
                new RuntimeInitializedClassBuildItem(PropertyDescriptorUtils.class.getCanonicalName()));
    }


}
