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
package org.apache.myfaces.config;

import org.apache.myfaces.config.impl.digester.elements.Converter;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.RenderKit;
import org.apache.xbean.finder.ClassFinder;

import javax.faces.FacesException;
import javax.faces.webapp.FacesServlet;
import javax.faces.component.FacesComponent;
import javax.faces.context.ExternalContext;
import javax.faces.convert.FacesConverter;
import javax.faces.model.*;
import javax.faces.render.FacesRenderKit;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.List;
import java.net.URL;

/**
 * JSR 314 standard Annotation Configurator.
 *
 * @author Jan-Kees van Andel
 * @version $Revision: 743355 $ $Date: 2009-02-11 16:02:41 +0100 (wo, 11 feb 2009) $
 */
public class AnnotationFacesConfiguratorStrategy extends FacesConfiguratorStrategy
{

    /**
     *
     * @param externalContext The ExternalContext used when starting up.
     */
    public AnnotationFacesConfiguratorStrategy(ExternalContext externalContext)
    {
        super(externalContext);
    }

    public void feed() throws FacesException
    {
        feedAnnotatedManagedBeans();
    }

    public void configure() throws FacesException
    {
        // Do nothing, use existing configure method on XML config strategy.
    }

    private void feedAnnotatedManagedBeans()
    {
        ClassFinder cf;
        try
        {
            String path = ((ServletContext) (_externalContext.getContext())).getRealPath("/WEB-INF/classes");
            ClassLoader classLoader = FacesServlet.class.getClassLoader();
            cf = new ClassFinder(classLoader);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize classfinder, annotated managed beans will not be found", e);
        }

        // Configure javax.faces.model.ManagedBean annotated classes
        List<Class> annotatedClasses = cf.findAnnotatedClasses(ManagedBean.class);
        FacesConfig cfg = new FacesConfig();
        for (Class annotatedClass : annotatedClasses)
        {
            ManagedBean beanAnnotation = (ManagedBean) annotatedClass.getAnnotation(ManagedBean.class);
            cfg.addManagedBean(createManagedBean(annotatedClass, beanAnnotation));
        }

        // Configure javax.faces.model.ManagedBeans annotated classes
        annotatedClasses = cf.findAnnotatedClasses(ManagedBeans.class);
        for (Class annotatedClass : annotatedClasses)
        {
            ManagedBeans beansAnnotation = (ManagedBeans) annotatedClass.getAnnotation(ManagedBeans.class);
            ManagedBean[] beanAnnotations = beansAnnotation.value();
            for (ManagedBean beanAnnotation : beanAnnotations)
            {
                cfg.addManagedBean(createManagedBean(annotatedClass, beanAnnotation));
            }
        }

        // Configure javax.faces.convert.FacesConverter annotated classes
        annotatedClasses = cf.findAnnotatedClasses(FacesConverter.class);
        for (Class annotatedClass : annotatedClasses)
        {
            FacesConverter converterAnnotation = (FacesConverter) annotatedClass.getAnnotation(FacesConverter.class);
            cfg.addConverter(createConverter(annotatedClass, converterAnnotation));
        }

        // Configure javax.faces.validate.FacesValidator annotated classes
        annotatedClasses = cf.findAnnotatedClasses(FacesValidator.class);
        for (Class annotatedClass : annotatedClasses)
        {
            FacesValidator validatorAnnotation = (FacesValidator) annotatedClass.getAnnotation(FacesValidator.class);
            cfg.addValidator(createValidatorId(annotatedClass, validatorAnnotation), annotatedClass.getName());
        }

        // Configure javax.faces.component.FacesComponent annotated classes
        annotatedClasses = cf.findAnnotatedClasses(FacesComponent.class);
        for (Class annotatedClass : annotatedClasses)
        {
            FacesComponent componentAnnotation = (FacesComponent) annotatedClass.getAnnotation(FacesComponent.class);
            cfg.addComponent(createComponentType(annotatedClass, componentAnnotation), annotatedClass.getName());
        }

        // Configure javax.faces.render.FacesRenderKit annotated classes
        annotatedClasses = cf.findAnnotatedClasses(FacesRenderKit.class);
        for (Class annotatedClass : annotatedClasses)
        {
            FacesRenderKit renderKitAnnotation = (FacesRenderKit) annotatedClass.getAnnotation(FacesRenderKit.class);
            cfg.addRenderKit(createRenderKit(annotatedClass, renderKitAnnotation));
        }

        // Configure javax.faces.render.FacesRenderer annotated classes
        // This code must be executed AFTER the RenderKit registration code. It assumes it's initialized.
        annotatedClasses = cf.findAnnotatedClasses(FacesRenderer.class);
        for (Class annotatedClass : annotatedClasses)
        {
            FacesRenderer rendererAnnotation = (FacesRenderer) annotatedClass.getAnnotation(FacesRenderer.class);
            createRenderer(annotatedClass, rendererAnnotation, cfg.getRenderKits());
        }

        getDispenser().feed(cfg);
    }

    private RenderKit createRenderKit(Class<?> annotatedClass, FacesRenderKit renderKitAnnotation)
    {
        RenderKit renderKit = new RenderKit();
        renderKit.setId(renderKitAnnotation.value());
        renderKit.setRenderKitClass(annotatedClass.getName());
        return renderKit;
    }

    private void createRenderer(Class annotatedClass, FacesRenderer rendererAnnotation, List<RenderKit> renderKits)
    {
        org.apache.myfaces.config.impl.digester.elements.Renderer renderer = new org.apache.myfaces.config.impl.digester.elements.Renderer();
        renderer.setComponentFamily(rendererAnnotation.componentFamily());
        renderer.setRendererClass(annotatedClass.getName());
        renderer.setRendererType(rendererAnnotation.rendererType());

        String renderKitId = rendererAnnotation.renderKitId();
        // Add method to RenderKit
        for (RenderKit renderKit : renderKits)
        {
            if (renderKit.getId().equals(renderKitId))
            {
                renderKit.addRenderer(renderer);
                break;
            }
        }

        throw new RuntimeException("Failed to register a Renderer on class: " + annotatedClass.getName() + ". The renderKitId attribute is not specified correctly. It does not exist. You specified: \"" + renderKitId + "\"");
    }

    private String createComponentType(Class<?> annotatedClass, FacesComponent componentAnnotation)
    {
        return componentAnnotation.value();
    }

    private String createValidatorId(Class<?> annotatedClass, FacesValidator validatorAnnotation)
    {
        return validatorAnnotation.value();
    }

    private Converter createConverter(Class<?> annotatedClass, FacesConverter converterAnnotation)
    {
        String id = converterAnnotation.value();
        Class<?> forClass = converterAnnotation.forClass();

        Converter converter = new Converter();
        converter.setConverterClass(annotatedClass.getName());
        boolean isset = false;
        if (id != null && !id.equals(""))
        {
            converter.setConverterId(id);
            isset = true;
        }

        if (forClass != null && !forClass.equals(Object.class))
        {
            converter.setForClass(forClass.getName());
            isset = true;
        }

        if (!isset) {
            throw new RuntimeException("Failed to register Converter on class: " + annotatedClass.getName() + ". Either the value or forClass attribute must be set");
        }

        return converter;
    }

    private org.apache.myfaces.config.impl.digester.elements.ManagedBean createManagedBean(Class annotatedClass, ManagedBean beanAnnotation)
    {
        org.apache.myfaces.config.impl.digester.elements.ManagedBean bean = new org.apache.myfaces.config.impl.digester.elements.ManagedBean();
        if (beanAnnotation.name() == null || beanAnnotation.name().equals(""))
        {
            bean.setName(annotatedClass.getSimpleName());
        }
        else
        {
            bean.setName(beanAnnotation.name());
        }
        bean.setBeanClass(annotatedClass.getName());
        bean.setDescription(null); //TODO: check with spec
        RequestScoped reqScopedAnno = (RequestScoped) annotatedClass.getAnnotation(RequestScoped.class);
        if (reqScopedAnno != null)
        {
            bean.setScope(ManagedBeanBuilder.REQUEST);
        }
        else
        {
            SessionScoped sesScopedAnno = (SessionScoped) annotatedClass.getAnnotation(SessionScoped.class);
            if (sesScopedAnno != null)
            {
                bean.setScope(ManagedBeanBuilder.SESSION);
            }
            else
            {
                ApplicationScoped appScopedAnno = (ApplicationScoped) annotatedClass.getAnnotation(ApplicationScoped.class);
                if (appScopedAnno != null)
                {
                    bean.setScope(ManagedBeanBuilder.APPLICATION);
                }
                else
                {
                    NoneScoped nonScopedAnno = (NoneScoped) annotatedClass.getAnnotation(NoneScoped.class);
                    if (nonScopedAnno != null)
                    {
                        bean.setScope(ManagedBeanBuilder.NONE);
                    }
                }
            }
        }

        List<Field> managedPropertyFields = new ClassFinder(annotatedClass).findAnnotatedFields(ManagedProperty.class);
        for (Field annotatedField : managedPropertyFields)
        {
            ManagedProperty mgdPropAnnotation = annotatedField.getAnnotation(ManagedProperty.class);
            org.apache.myfaces.config.impl.digester.elements.ManagedProperty prop =
                    new org.apache.myfaces.config.impl.digester.elements.ManagedProperty();
            String name = mgdPropAnnotation.name() != null && !mgdPropAnnotation.name().equals("")
                        ? mgdPropAnnotation.name()
                        : annotatedField.getName();
            prop.setPropertyName(name);
            prop.setPropertyClass(annotatedField.getType().getName()); //TODO: Check if this assumption is correct
            prop.setValue(mgdPropAnnotation.value());

            bean.addProperty(prop);
        }

        return bean;
    }

}
