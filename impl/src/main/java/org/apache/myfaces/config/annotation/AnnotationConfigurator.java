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
package org.apache.myfaces.config.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.NoneScoped;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.context.ExternalContext;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.render.RenderKitFactory;
import javax.faces.validator.FacesValidator;

import org.apache.myfaces.config.impl.digester.elements.Application;
import org.apache.myfaces.config.impl.digester.elements.Behavior;
import org.apache.myfaces.config.impl.digester.elements.Converter;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.myfaces.spi.AnnotationProviderFactory;

/**
 * Configure all annotations that needs to be defined at startup.
 * 
 * <ul>
 * <li>{@link javax.faces.component.FacesComponent}</li>
 * <li>{@link javax.faces.convert.FacesConverter}</li>
 * <li>{@link javax.faces.validator.FacesValidator}</li>
 * <li>{@link javax.faces.render.FacesRenderer}</li>
 * <li>{@link javax.faces.bean.ManagedBean}</li>
 * <li>{@link javax.faces.bean.ManagedProperty}</li>
 * <li>{@link javax.faces.render.FacesBehaviorRenderer}</li>
 * </ul>
 * <p>
 * Some parts copied from org.apache.shale.tiger.view.faces.LifecycleListener2
 * </p>
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class AnnotationConfigurator
{
    //private static final Log log = LogFactory.getLog(AnnotationConfigurator.class);
    private static final Logger log = Logger.getLogger(AnnotationConfigurator.class.getName());

    /**
     * <p>The render kit factory for this application.</p>
     */
    private RenderKitFactory rkFactory = null;

    public AnnotationConfigurator()
    {
    }

    public FacesConfig createFacesConfig(ExternalContext _externalContext, boolean metadataComplete)
    {
        if (!metadataComplete)
        {
            AnnotationProvider provider = AnnotationProviderFactory.getAnnotationProviderFactory(_externalContext).getAnnotationProvider(_externalContext);
            Map<Class<? extends Annotation>,Set<Class<?>>> map = provider.getAnnotatedClasses(_externalContext);
            return createFacesConfig(map);
        }
        return null;
    }
    /*
    public void configure(final Application application, 
            final FacesConfigDispenser<FacesConfig> dispenser,
            boolean metadataComplete) throws FacesException
    {
        AnnotationProvider provider = AnnotationProviderFactory.getAnnotationProviderFactory(_externalContext).createAnnotationProvider(_externalContext);
        Map<Class<? extends Annotation>,Set<Class<?>>> map = provider.getAnnotatedClasses(_externalContext);
        configureClasses(application, dispenser, map);
    }*/
    
    protected FacesConfig createFacesConfig(Map< Class<? extends Annotation>, Set<Class<?>> > map)
    {
        FacesConfig facesConfig = new FacesConfig();
        
        Set<Class<?>> classes = map.get(FacesComponent.class);
        
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesComponent comp = (FacesComponent) clazz
                        .getAnnotation(FacesComponent.class);
                if (comp != null)
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addComponent(" + comp.value() + ","
                                + clazz.getName() + ")");
                    }
                    
                    facesConfig.addComponent(comp.value(), clazz.getName());
                }
            }
        }
        
        classes = map.get(FacesConverter.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesConverter conv = (FacesConverter) clazz
                        .getAnnotation(FacesConverter.class);
                if (conv != null)
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addConverter(" + conv.value() + ","
                                + clazz.getName() + ")");
                    }
                    //If there is a previous entry on Application Configuration Resources,
                    //the entry there takes precedence
                    boolean hasForClass = !Object.class.equals(conv.forClass());
                    boolean hasValue = conv.value().length() > 0;
                    if (hasForClass || hasValue)
                    {
                        Converter converter = new Converter();
                        if (hasForClass)
                        {
                            converter.setForClass(conv.forClass().getName());
                        }
                        if (hasValue) {
                            converter.setConverterId(conv.value());
                        }
                        converter.setConverterClass(clazz.getName());
                        facesConfig.addConverter(converter);
                    }
                    else
                    {   
                        // TODO MartinKoci MYFACES-3053
                        throw new FacesException("@FacesConverter must have value, forClass or both. Check annotation @FacesConverter on class: "  + clazz.getName());
                    }
                }                
            }
        }
        
        classes = map.get(FacesValidator.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesValidator val = (FacesValidator) clazz
                .getAnnotation(FacesValidator.class);
                if (val != null)
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addValidator(" + val.value() + "," + clazz.getName()
                                + ")");
                    }
                    facesConfig.addValidator(val.value(), clazz.getName());
                    if (val.isDefault())
                    {
                        Application app = null;
                        if(facesConfig.getApplications().isEmpty())
                        {
                            app = new Application();
                        }
                        else
                        {
                            app = (Application) facesConfig.getApplications().get(0);
                        }
                        app.addDefaultValidatorId(val.value());
                    }
                }
            }
        }

        classes = map.get(FacesRenderer.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesRenderer rend = (FacesRenderer) clazz
                .getAnnotation(FacesRenderer.class);
                if (rend != null)
                {
                    String renderKitId = rend.renderKitId();
                    if (renderKitId == null)
                    {
                        renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
                    }
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addRenderer(" + renderKitId + ", "
                                + rend.componentFamily() + ", " + rend.rendererType()
                                + ", " + clazz.getName() + ")");
                    }
                    
                    org.apache.myfaces.config.impl.digester.elements.RenderKit renderKit =
                       (org.apache.myfaces.config.impl.digester.elements.RenderKit) facesConfig.getRenderKit(renderKitId);
                    if (renderKit == null)
                    {
                        renderKit = new org.apache.myfaces.config.impl.digester.elements.RenderKit();
                        facesConfig.addRenderKit(renderKit);
                    }
                    
                    org.apache.myfaces.config.impl.digester.elements.Renderer renderer =
                        new org.apache.myfaces.config.impl.digester.elements.Renderer();
                    renderer.setComponentFamily(rend.componentFamily());
                    renderer.setRendererClass(clazz.getName());
                    renderer.setRendererType(rend.rendererType());
                    renderKit.addRenderer(renderer);
                }        
            }
        }

        classes = map.get(ManagedBean.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                javax.faces.bean.ManagedBean bean = 
                    (javax.faces.bean.ManagedBean) clazz.getAnnotation(javax.faces.bean.ManagedBean.class);
        
                if (bean != null)
                {
                    if (log.isLoggable(Level.FINE))
                    {
                        log.fine("Class '" + clazz.getName() + "' has an @ManagedBean annotation");
                    }
                    
                    org.apache.myfaces.config.impl.digester.elements.ManagedBean mbc =
                        new org.apache.myfaces.config.impl.digester.elements.ManagedBean();
                    String beanName = bean.name();
                    
                    if ((beanName == null) || beanName.equals ("")) {
                        int index;
                        
                        // Missing name attribute algorithm: take the unqualified name and make the
                        // first character lowercase.
                        
                        beanName = clazz.getName();
                        index = beanName.lastIndexOf (".");
                        
                        if (index != -1) {
                            beanName = beanName.substring (index + 1);
                        }
                        
                        beanName = Character.toLowerCase (beanName.charAt (0)) +
                            beanName.substring (1);
                    }
                    
                    mbc.setName(beanName);
                    mbc.setEager(Boolean.toString(bean.eager()));
                    mbc.setBeanClass(clazz.getName());
                    
                    ApplicationScoped appScoped = (ApplicationScoped) clazz.getAnnotation(ApplicationScoped.class);
                    if (appScoped != null)
                    {
                        mbc.setScope("application");
                    }
                    
                    else
                    {
                        NoneScoped noneScoped = (NoneScoped) clazz.getAnnotation(NoneScoped.class);
                        if (noneScoped != null)
                        {
                            mbc.setScope("none");
                        }
                        
                        else
                        {
                            RequestScoped requestScoped = (RequestScoped) clazz.getAnnotation(RequestScoped.class);
                            if (requestScoped != null)
                            {
                                mbc.setScope("request");
                            }
                            
                            else
                            {
                                SessionScoped sessionScoped = (SessionScoped) clazz.getAnnotation(SessionScoped.class);
                                if (sessionScoped != null)
                                {
                                    mbc.setScope("session");
                                }
                                
                                else
                                {
                                    ViewScoped viewScoped = (ViewScoped) clazz.getAnnotation(ViewScoped.class);
                                    if (viewScoped != null)
                                    {
                                        mbc.setScope("view");
                                    }
                                    
                                    else {
                                        CustomScoped customScoped = (CustomScoped) clazz.getAnnotation(CustomScoped.class);
                                        if (customScoped != null)
                                        {
                                            mbc.setScope(customScoped.value());
                                        }
                                        
                                        else
                                        {
                                            // No scope annotation means default of "request".
                                            
                                            mbc.setScope ("request");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Field[] fields = fields(clazz);
                    for (Field field : fields)
                    {
                        if (log.isLoggable(Level.FINEST))
                        {
                            log.finest("  Scanning field '" + field.getName() + "'");
                        }
                        javax.faces.bean.ManagedProperty property = (javax.faces.bean.ManagedProperty) field
                                .getAnnotation(javax.faces.bean.ManagedProperty.class);
                        if (property != null)
                        {
                            if (log.isLoggable(Level.FINE))
                            {
                                log.fine("  Field '" + field.getName()
                                        + "' has a @ManagedProperty annotation");
                            }
                            org.apache.myfaces.config.impl.digester.elements.ManagedProperty mpc = 
                                new org.apache.myfaces.config.impl.digester.elements.ManagedProperty();
                            String name = property.name();
                            if ((name == null) || "".equals(name))
                            {
                                name = field.getName();
                            }
                            mpc.setPropertyName(name);
                            mpc.setPropertyClass(field.getType().getName()); // FIXME - primitives, arrays, etc.
                            mpc.setValue(property.value());
                            mbc.addProperty(mpc);
                            continue;
                        }
                    }
                    facesConfig.addManagedBean(mbc);
                }        
            }
        }

        classes = map.get(NamedEvent.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                NamedEvent namedEvent = (NamedEvent) clazz.getAnnotation (NamedEvent.class);
        
                if (namedEvent != null) {
                    // Can only apply @NamedEvent to ComponentSystemEvent subclasses.
                    
                    if (!ComponentSystemEvent.class.isAssignableFrom (clazz)) {
                        // Just log this.  We'll catch it later in the runtime.
                        
                        if (log.isLoggable(Level.WARNING)) {
                            log.warning (clazz.getName() + " is annotated with @javax.faces.event.NamedEvent, but does " +
                                "not extend javax.faces.event.ComponentSystemEvent");
                        }
                    }
                    // Have to register @NamedEvent annotations with the NamedEventManager class since
                    // we need to get access to this info later and can't from the dispenser (it's not a
                    // singleton).
                    org.apache.myfaces.config.impl.digester.elements.NamedEvent namedEventConfig = 
                        new org.apache.myfaces.config.impl.digester.elements.NamedEvent();
                    namedEventConfig.setEventClass(clazz.getName());
                    namedEventConfig.setShortName(namedEvent.shortName());
                    facesConfig.addNamedEvent(namedEventConfig);
                }
            }
        }
    
        classes = map.get(FacesBehavior.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesBehavior facesBehavior = (FacesBehavior) clazz.getAnnotation (FacesBehavior.class);
        
                if (facesBehavior != null) {
                    // Can only apply @FacesBehavior to Behavior implementors.
                    
                    if (!javax.faces.component.behavior.Behavior.class.isAssignableFrom (clazz)) {
                        // Just log this.  We'll catch it later in the runtime.
                        
                        if (log.isLoggable(Level.WARNING)) {
                            log.warning (clazz.getName() + " is annotated with @javax.faces.component.behavior.FacesBehavior, " +
                                    "but does not implement javax.faces.component.behavior.Behavior");
                        }
                    }
                    
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest ("addBehavior(" + facesBehavior.value() + ", " + clazz.getName() + ")");
                    }
                    
                    Behavior behavior = new Behavior();
                    behavior.setBehaviorId(facesBehavior.value());
                    behavior.setBehaviorClass(clazz.getName());
                    facesConfig.addBehavior(behavior);
                }
                
            }
        }
    
        classes = map.get(FacesBehaviorRenderer.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesBehaviorRenderer facesBehaviorRenderer = (FacesBehaviorRenderer) clazz.getAnnotation (FacesBehaviorRenderer.class);
        
                if (facesBehaviorRenderer != null) {
                    String renderKitId = facesBehaviorRenderer.renderKitId();
                    //RenderKit renderKit;
                    
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest ("addClientBehaviorRenderer(" + renderKitId + ", " + facesBehaviorRenderer.rendererType() + ", " +
                             clazz.getName() + ")");
                    }
                    
                    org.apache.myfaces.config.impl.digester.elements.RenderKit renderKit =
                        (org.apache.myfaces.config.impl.digester.elements.RenderKit) facesConfig.getRenderKit(renderKitId);
                    if (renderKit == null)
                    {
                        renderKit = new org.apache.myfaces.config.impl.digester.elements.RenderKit();
                        facesConfig.addRenderKit(renderKit);
                    }
                    
                    org.apache.myfaces.config.impl.digester.elements.ClientBehaviorRenderer cbr = 
                        new org.apache.myfaces.config.impl.digester.elements.ClientBehaviorRenderer();
                    cbr.setRendererType(facesBehaviorRenderer.rendererType());
                    cbr.setRendererClass(clazz.getName());
                    renderKit.addClientBehaviorRenderer(cbr);
                }
            }
        }
        return facesConfig;
    }
    
    /*
    protected void configureClasses( Application application, 
            FacesConfigDispenser<FacesConfig> dispenser, 
            Map< Class<? extends Annotation>, Set<Class<?>> > map)
    {
        Set<Class<?>> classes = map.get(FacesComponent.class);
        
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesComponent comp = (FacesComponent) clazz
                        .getAnnotation(FacesComponent.class);
                if (comp != null)
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addComponent(" + comp.value() + ","
                                + clazz.getName() + ")");
                    }
                    
                    //If there is a previous entry on Application Configuration Resources,
                    //the entry there takes precedence
                    if (dispenser.getComponentClass(comp.value()) == null)
                    {
                        application.addComponent(comp.value(), clazz.getName());                
                    }
                }
            }
        }
        
        classes = map.get(FacesConverter.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesConverter conv = (FacesConverter) clazz
                        .getAnnotation(FacesConverter.class);
                if (conv != null)
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addConverter(" + conv.value() + ","
                                + clazz.getName() + ")");
                    }
                    //If there is a previous entry on Application Configuration Resources,
                    //the entry there takes precedence
                    if (!Object.class.equals(conv.forClass()))
                    {
                        application.addConverter(conv.forClass(), clazz.getName());
                    }
                    if (dispenser.getConverterClassById(conv.value()) == null &&
                            conv.value() != null && !"".equals(conv.value()))
                    {
                        application.addConverter(conv.value(), clazz.getName());
                    }
                }                
            }
        }
        
        classes = map.get(FacesValidator.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesValidator val = (FacesValidator) clazz
                .getAnnotation(FacesValidator.class);
                if (val != null)
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addValidator(" + val.value() + "," + clazz.getName()
                                + ")");
                    }
                    //If there is a previous entry on Application Configuration Resources,
                    //the entry there takes precedence
                    if (dispenser.getValidatorClass(val.value()) == null)
                    {
                        application.addValidator(val.value(), clazz.getName());
                        if (val.isDefault())
                        {
                            application.addDefaultValidatorId(val.value());
                        }
                    }
                }
            }
        }

        classes = map.get(FacesRenderer.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesRenderer rend = (FacesRenderer) clazz
                .getAnnotation(FacesRenderer.class);
                if (rend != null)
                {
                    String renderKitId = rend.renderKitId();
                    if (renderKitId == null)
                    {
                        renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
                    }
                    if (log.isLoggable(Level.FINEST))
                    {
                        log.finest("addRenderer(" + renderKitId + ", "
                                + rend.componentFamily() + ", " + rend.rendererType()
                                + ", " + clazz.getName() + ")");
                    }
                    try
                    {
                        RenderKit rk = renderKitFactory().getRenderKit(null,
                                renderKitId);
                        if (rk == null)
                        {
                            if (log.isLoggable(Level.SEVERE))
                            {
                                log.severe("RenderKit "+renderKitId+" not found when adding @FacesRenderer annotation");
                            }
                            throw new FacesException("RenderKit "+renderKitId+" not found when adding @FacesRenderer annotation");
                        }
                        rk.addRenderer(rend.componentFamily(), rend.rendererType(),
                                (Renderer) clazz.newInstance());
                    }
                    catch (Exception e)
                    {
                        throw new FacesException(e);
                    }
                }        
            }
        }

        classes = map.get(ManagedBean.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                javax.faces.bean.ManagedBean bean = 
                    (javax.faces.bean.ManagedBean) clazz.getAnnotation(javax.faces.bean.ManagedBean.class);
        
                if (bean != null)
                {
                    if (log.isLoggable(Level.FINE))
                    {
                        log.fine("Class '" + clazz.getName() + "' has an @ManagedBean annotation");
                    }
                    RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(_externalContext);
                    org.apache.myfaces.config.impl.digester.elements.ManagedBean mbc =
                        new org.apache.myfaces.config.impl.digester.elements.ManagedBean();
                    String beanName = bean.name();
                    
                    if ((beanName == null) || beanName.equals ("")) {
                        int index;
                        
                        // Missing name attribute algorithm: take the unqualified name and make the
                        // first character lowercase.
                        
                        beanName = clazz.getName();
                        index = beanName.lastIndexOf (".");
                        
                        if (index != -1) {
                            beanName = beanName.substring (index + 1);
                        }
                        
                        beanName = Character.toLowerCase (beanName.charAt (0)) +
                            beanName.substring (1);
                    }
                    
                    mbc.setName(beanName);
                    mbc.setEager(Boolean.toString(bean.eager()));
                    mbc.setBeanClass(clazz.getName());
                    
                    ApplicationScoped appScoped = (ApplicationScoped) clazz.getAnnotation(ApplicationScoped.class);
                    if (appScoped != null)
                    {
                        mbc.setScope("application");
                    }
                    
                    else
                    {
                        NoneScoped noneScoped = (NoneScoped) clazz.getAnnotation(NoneScoped.class);
                        if (noneScoped != null)
                        {
                            mbc.setScope("none");
                        }
                        
                        else
                        {
                            RequestScoped requestScoped = (RequestScoped) clazz.getAnnotation(RequestScoped.class);
                            if (requestScoped != null)
                            {
                                mbc.setScope("request");
                            }
                            
                            else
                            {
                                SessionScoped sessionScoped = (SessionScoped) clazz.getAnnotation(SessionScoped.class);
                                if (sessionScoped != null)
                                {
                                    mbc.setScope("session");
                                }
                                
                                else
                                {
                                    ViewScoped viewScoped = (ViewScoped) clazz.getAnnotation(ViewScoped.class);
                                    if (viewScoped != null)
                                    {
                                        mbc.setScope("view");
                                    }
                                    
                                    else {
                                        CustomScoped customScoped = (CustomScoped) clazz.getAnnotation(CustomScoped.class);
                                        if (customScoped != null)
                                        {
                                            mbc.setScope(customScoped.value());
                                        }
                                        
                                        else
                                        {
                                            // No scope annotation means default of "request".
                                            
                                            mbc.setScope ("request");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Field[] fields = fields(clazz);
                    for (Field field : fields)
                    {
                        if (log.isLoggable(Level.FINEST))
                        {
                            log.finest("  Scanning field '" + field.getName() + "'");
                        }
                        javax.faces.bean.ManagedProperty property = (javax.faces.bean.ManagedProperty) field
                                .getAnnotation(javax.faces.bean.ManagedProperty.class);
                        if (property != null)
                        {
                            if (log.isLoggable(Level.FINE))
                            {
                                log.fine("  Field '" + field.getName()
                                        + "' has a @ManagedProperty annotation");
                            }
                            org.apache.myfaces.config.impl.digester.elements.ManagedProperty mpc = 
                                new org.apache.myfaces.config.impl.digester.elements.ManagedProperty();
                            String name = property.name();
                            if ((name == null) || "".equals(name))
                            {
                                name = field.getName();
                            }
                            mpc.setPropertyName(name);
                            mpc.setPropertyClass(field.getType().getName()); // FIXME - primitives, arrays, etc.
                            mpc.setValue(property.value());
                            mbc.addProperty(mpc);
                            continue;
                        }
                    }
                    runtimeConfig.addManagedBean(mbc.getManagedBeanName(), mbc);
                }        
            }
        }

        classes = map.get(NamedEvent.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                NamedEvent namedEvent = (NamedEvent) clazz.getAnnotation (NamedEvent.class);
        
                if (namedEvent != null) {
                    // Can only apply @NamedEvent to ComponentSystemEvent subclasses.
                    
                    if (!ComponentSystemEvent.class.isAssignableFrom (clazz)) {
                        // Just log this.  We'll catch it later in the runtime.
                        
                        if (log.isLoggable(Level.WARNING)) {
                            log.warning (clazz.getName() + " is annotated with @javax.faces.event.NamedEvent, but does " +
                                "not extend javax.faces.event.ComponentSystemEvent");
                        }
                        
                        return;
                    }
                    
                    // Have to register @NamedEvent annotations with the NamedEventManager class since
                    // we need to get access to this info later and can't from the dispenser (it's not a
                    // singleton).
                    
                    NamedEventManager.getInstance().addNamedEvent (namedEvent.shortName(),
                        (Class<? extends ComponentSystemEvent>) clazz);
                }
            }
        }
    
        classes = map.get(FacesBehavior.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesBehavior facesBehavior = (FacesBehavior) clazz.getAnnotation (FacesBehavior.class);
        
                if (facesBehavior != null) {
                    // Can only apply @FacesBehavior to Behavior implementors.
                    
                    if (!Behavior.class.isAssignableFrom (clazz)) {
                        // Just log this.  We'll catch it later in the runtime.
                        
                        if (log.isLoggable(Level.WARNING)) {
                            log.warning (clazz.getName() + " is annotated with @javax.faces.component.behavior.FacesBehavior, " +
                                    "but does not implement javax.faces.component.behavior.Behavior");
                        }
                    }
                    
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest ("addBehavior(" + facesBehavior.value() + ", " + clazz.getName() + ")");
                    }
                    
                    application.addBehavior (facesBehavior.value(), clazz.getName());
                }
                
            }
        }
    
        classes = map.get(FacesBehaviorRenderer.class);
        if (classes != null && !classes.isEmpty())
        {
            for (Class<?> clazz : classes)
            {
                FacesBehaviorRenderer facesBehaviorRenderer = (FacesBehaviorRenderer) clazz.getAnnotation (FacesBehaviorRenderer.class);
        
                if (facesBehaviorRenderer != null) {
                    String renderKitId = facesBehaviorRenderer.renderKitId();
                    RenderKit renderKit;
                    
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest ("addClientBehaviorRenderer(" + renderKitId + ", " + facesBehaviorRenderer.rendererType() + ", " +
                             clazz.getName() + ")");
                    }
                    
                    try {
                        ClientBehaviorRenderer clientBehaviorRenderer;
                        
                        renderKit = renderKitFactory().getRenderKit (null, renderKitId);
                        
                        clientBehaviorRenderer = (ClientBehaviorRenderer) clazz.newInstance();
                        
                        renderKit.addClientBehaviorRenderer(facesBehaviorRenderer.rendererType(), clientBehaviorRenderer);
                    }
                    
                    catch (Throwable e) {
                        throw new FacesException (e);
                    }
                }
            }
        }
        */
        /*
        ListenerFor listenerFor = (ListenerFor) clazz.getAnnotation(ListenerFor.class);
        if (listenerFor != null)
        {
            processListenerFor(application, clazz, listenerFor);
        }
        
        ListenersFor listenersFor = (ListenersFor) clazz.getAnnotation(ListenersFor.class);
        if (listenersFor != null)
        {
            for (ListenerFor item : listenersFor.value())
            {
                processListenerFor(application, clazz, item);
            }
        }*/
        
        // TODO: All annotations scanned at startup must be configured here!
    /*
    }
    */

    /**
     * <p>Return an array of all <code>Field</code>s reflecting declared
     * fields in this class, or in any superclass other than
     * <code>java.lang.Object</code>.</p>
     *
     * @param clazz Class to be analyzed
     */
    private Field[] fields(Class<?> clazz) {

        Map<String,Field> fields = new HashMap<String,Field>();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
        } while ((clazz = clazz.getSuperclass()) != Object.class);
        return (Field[]) fields.values().toArray(new Field[fields.size()]);

    }

    /**
     * <p>Return the <code>RenderKitFactory</code> for this application.</p>
     */
    /*
    private RenderKitFactory renderKitFactory()
    {

        if (rkFactory == null)
        {
            rkFactory = (RenderKitFactory) FactoryFinder
                    .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return rkFactory;

    }*/
}
