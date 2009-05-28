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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextListener;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.application.ResourceHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionListener;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.ListenersFor;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.event.SystemEventListenerHolder;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.faces.view.ViewDeclarationLanguage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.jsp.JspStateManagerImpl;
import org.apache.myfaces.application.jsp.JspViewHandlerImpl;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.impl.digester.elements.Property;
import org.apache.myfaces.config.impl.digester.elements.ResourceBundle;
import org.apache.myfaces.el.PropertyResolverImpl;
import org.apache.myfaces.el.VariableResolverToApplicationELResolverAdapter;
import org.apache.myfaces.el.convert.MethodExpressionToMethodBinding;
import org.apache.myfaces.el.convert.ValueBindingToValueExpression;
import org.apache.myfaces.el.convert.ValueExpressionToValueBinding;
import org.apache.myfaces.el.unified.ELResolverBuilder;
import org.apache.myfaces.el.unified.ResolverBuilderForFaces;
import org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver;
import org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver.Scope;
import org.apache.myfaces.shared_impl.util.ClassUtils;

/**
 * DOCUMENT ME!
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @author Thomas Spiegl
 * @author Stan Silvert
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ApplicationImpl extends Application
{
    private static final Log log = LogFactory.getLog(ApplicationImpl.class);

    private final static VariableResolver VARIABLERESOLVER = new VariableResolverToApplicationELResolverAdapter();

    private final static PropertyResolver PROPERTYRESOLVER = new PropertyResolverImpl();

    // recives the runtime config instance during initializing
    private final static ThreadLocal<RuntimeConfig> initializingRuntimeConfig = new ThreadLocal<RuntimeConfig>();

    // MyFaces specific System Property to set the ProjectStage, if not present via the standard way
    public final static String MYFACES_PROJECT_STAGE_SYSTEM_PROPERTY_NAME = "org.apache.myfaces.PROJECT_STAGE";

    // ~ Instance fields
    // --------------------------------------------------------------------------
    // --

    private Collection<Locale> _supportedLocales = Collections.emptySet();
    private Locale _defaultLocale;
    private String _messageBundle;

    private ViewHandler _viewHandler;
    private NavigationHandler _navigationHandler;
    private ActionListener _actionListener;
    private String _defaultRenderKitId;
    private ResourceHandler _resourceHandler;
    private StateManager _stateManager;

    private ArrayList<ELContextListener> _elContextListeners;

    // components, converters, and validators can be added at runtime--must
    // synchronize, uses ConcurrentHashMap to allow concurrent read of map
    private final Map<String, Class<? extends Converter>> _converterIdToClassMap = new ConcurrentHashMap<String, Class<? extends Converter>>();

    private final Map<Class<?>, String> _converterClassNameToClassMap = new ConcurrentHashMap<Class<?>, String>();

    private final Map<String, org.apache.myfaces.config.impl.digester.elements.Converter> _converterClassNameToConfigurationMap = new ConcurrentHashMap<String, org.apache.myfaces.config.impl.digester.elements.Converter>();

    private final Map<String, Class<? extends UIComponent>> _componentClassMap = new ConcurrentHashMap<String, Class<? extends UIComponent>>();

    private final Map<String, Class<? extends Validator>> _validatorClassMap = new ConcurrentHashMap<String, Class<? extends Validator>>();

    private final Map<Class<? extends SystemEvent>, SystemListenerEntry> _systemEventListenerClassMap = new ConcurrentHashMap<Class<? extends SystemEvent>, SystemListenerEntry>();

    private Map<String, String> _defaultValidatorsIds = new ConcurrentHashMap<String, String>();
    
    private final Map<String, Class<? extends Behavior>> _behaviorClassMap = new ConcurrentHashMap<String, Class<? extends Behavior>>();

    private final RuntimeConfig _runtimeConfig;

    private ELResolver elResolver;

    private ELResolverBuilder resolverBuilderForFaces;

    private ProjectStage _projectStage;

    // ~ Constructors
    // --------------------------------------------------------------------------
    // -----

    public ApplicationImpl()
    {
        this(internalGetRuntimeConfig());
    }

    private static RuntimeConfig internalGetRuntimeConfig()
    {
        if (initializingRuntimeConfig.get() == null)
        {
            // It may happen that the current thread value
            // for initializingRuntimeConfig is not set
            // (note that this value is final, so it just
            // allow set only once per thread).
            // So the better for this case is try to get
            // the value using RuntimeConfig.getCurrentInstance()
            // instead throw an IllegalStateException (only fails if
            // the constructor is called before setInitializingRuntimeConfig).
            // From other point of view, AbstractFacesInitializer do
            // the same as below, so there is not problem if
            // we do this here and this is the best place to do
            // this.
            // log.info(
            // "initializingRuntimeConfig.get() == null, so loading from ExternalContext"
            // );
            ApplicationImpl.setInitializingRuntimeConfig(RuntimeConfig.getCurrentInstance(FacesContext
                    .getCurrentInstance().getExternalContext()));

            // throw new IllegalStateException(
            // "The runtime config instance which is created while initialize myfaces "
            // +
            // "must be set through ApplicationImpl.setInitializingRuntimeConfig"
            // );
        }
        return initializingRuntimeConfig.get();
    }

    ApplicationImpl(final RuntimeConfig runtimeConfig)
    {
        if (runtimeConfig == null)
        {
            throw new IllegalArgumentException("runtimeConfig must mot be null");
        }
        // set default implementation in constructor
        // pragmatic approach, no syncronizing will be needed in get methods
        _viewHandler = new JspViewHandlerImpl(); //TODO: replace with new ViewHandlerImpl class when VDL methods are fully implemented
        _navigationHandler = new NavigationHandlerImpl();
        _actionListener = new ActionListenerImpl();
        _defaultRenderKitId = "HTML_BASIC";
        _stateManager = new JspStateManagerImpl();
        _elContextListeners = new ArrayList<ELContextListener>();
        _resourceHandler = new ResourceHandlerImpl();
        _runtimeConfig = runtimeConfig;

        if (log.isTraceEnabled())
            log.trace("New Application instance created");
    }

    public static void setInitializingRuntimeConfig(RuntimeConfig config)
    {
        initializingRuntimeConfig.set(config);
    }

    // ~ Methods
    // --------------------------------------------------------------------------
    // ----------

    @Override
    public final void addELResolver(final ELResolver resolver)
    {
        if (FacesContext.getCurrentInstance() != null)
        {
            throw new IllegalStateException("It is illegal to add a resolver after the first request is processed");
        }
        if (resolver != null)
        {
            _runtimeConfig.addApplicationElResolver(resolver);
        }
    }

    @Override
    public void addDefaultValidatorId(String validatorId)
    {
        if (_validatorClassMap.containsKey(validatorId))
        {
            _defaultValidatorsIds.put(validatorId, _validatorClassMap.get(validatorId).getName());
        }
    }

    @Override
    public Map<String, String> getDefaultValidatorInfo()
    {
        return Collections.unmodifiableMap(_defaultValidatorsIds);
    }

    @Override
    public final ELResolver getELResolver()
    {
        // we don't need synchronization here since it is ok to have multiple
        // instances of the elresolver
        if (elResolver == null)
        {
            elResolver = createFacesResolver();
        }
        return elResolver;
    }

    private ELResolver createFacesResolver()
    {
        final CompositeELResolver resolver = new FacesCompositeELResolver(Scope.Faces);
        getResolverBuilderForFaces().build(resolver);
        return resolver;
    }

    protected final ELResolverBuilder getResolverBuilderForFaces()
    {
        if (resolverBuilderForFaces == null)
        {
            resolverBuilderForFaces = new ResolverBuilderForFaces(_runtimeConfig);
        }
        return resolverBuilderForFaces;
    }

    public final void setResolverBuilderForFaces(final ELResolverBuilder factory)
    {
        resolverBuilderForFaces = factory;
    }

    @Override
    public final java.util.ResourceBundle getResourceBundle(final FacesContext facesContext, final String name)
            throws FacesException, NullPointerException
    {

        checkNull(facesContext, "facesContext");
        checkNull(name, "name");

        final String bundleName = getBundleName(facesContext, name);

        if (bundleName == null)
        {
            return null;
        }

        Locale locale = Locale.getDefault();

        final UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot != null && viewRoot.getLocale() != null)
        {
            locale = viewRoot.getLocale();
        }

        try
        {
            return getResourceBundle(bundleName, locale, getClassLoader());
        }
        catch (MissingResourceException e)
        {
            throw new FacesException("Could not load resource bundle for name '" + name + "': " + e.getMessage(), e);
        }
    }

    private ClassLoader getClassLoader()
    {
        return ClassUtils.getContextClassLoader();
    }

    String getBundleName(final FacesContext facesContext, final String name)
    {
        ResourceBundle bundle = getRuntimeConfig(facesContext).getResourceBundle(name);
        return bundle != null ? bundle.getBaseName() : null;
    }

    java.util.ResourceBundle getResourceBundle(final String name, final Locale locale, final ClassLoader loader)
            throws MissingResourceException
    {
        return java.util.ResourceBundle.getBundle(name, locale, loader);
    }

    final RuntimeConfig getRuntimeConfig(final FacesContext facesContext)
    {
        return RuntimeConfig.getCurrentInstance(facesContext.getExternalContext());
    }

    final FacesContext getFaceContext()
    {
        return FacesContext.getCurrentInstance();
    }

    @Override
    public final UIComponent createComponent(final ValueExpression componentExpression,
                                             final FacesContext facesContext, final String componentType)
            throws FacesException, NullPointerException
    {

        /*
         * Before the component instance is returned, it must be inspected for the presence of a ListenerFor (or
         * ListenersFor) or ResourceDependency (or ResourceDependencies) annotation. If any of these annotations are
         * present, the action listed in ListenerFor or ResourceDependency must be taken on the component, before it is
         * returned from this method. This variant of createComponent must not inspect the Renderer for the component to
         * be returned for any of the afore mentioned annotations. Such inspection is the province of
         */

        checkNull(componentExpression, "componentExpression");
        checkNull(facesContext, "facesContext");
        checkNull(componentType, "componentType");

        ELContext elContext = facesContext.getELContext();

        try
        {
            Object retVal = componentExpression.getValue(elContext);

            UIComponent createdComponent;

            if (retVal instanceof UIComponent)
            {
                createdComponent = (UIComponent) retVal;
                _handleAnnotations(facesContext, createdComponent, createdComponent);
            }
            else
            {
                createdComponent = createComponent(componentType);
                componentExpression.setValue(elContext, createdComponent);
            }

            return createdComponent;
        }
        catch (FacesException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
    }

    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType,
                                       String rendererType)
    {
        // Like createComponent(ValueExpression, FacesContext, String)
        UIComponent component = createComponent(componentExpression, context, componentType);

        _inspectRenderer(context, component, componentType, rendererType);

        return component;
    }

    @Override
    public final ExpressionFactory getExpressionFactory()
    {
        return _runtimeConfig.getExpressionFactory();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T evaluateExpressionGet(final FacesContext context, final String expression,
                                             final Class<? extends T> expectedType) throws ELException
    {
        ELContext elContext = context.getELContext();

        ExpressionFactory factory = getExpressionFactory();

        return (T) factory.createValueExpression(elContext, expression, expectedType).getValue(elContext);
    }

    @Override
    public final void addELContextListener(final ELContextListener listener)
    {
        synchronized (_elContextListeners)
        {
            _elContextListeners.add(listener);
        }
    }

    @Override
    public void publishEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceBaseType, Object source)
    {
        checkNull(systemEventClass, "systemEventClass");
        checkNull(source, "source");

        try
        {
            SystemEvent event = null;
            if (source instanceof SystemEventListenerHolder)
            {
                SystemEventListenerHolder holder = (SystemEventListenerHolder) source;

                // If the source argument implements SystemEventListenerHolder, call
                // SystemEventListenerHolder.getListenersForEventClass(java.lang.Class) on it, passing the
                // systemEventClass
                // argument. If the list is not empty, perform algorithm traverseListenerList on the list.
                event = _traverseListenerList(holder.getListenersForEventClass(systemEventClass), systemEventClass,
                                              source, event);
            }

            SystemListenerEntry systemListenerEntry = _systemEventListenerClassMap.get(systemEventClass);
            if (systemListenerEntry != null)
            {
                systemListenerEntry.publish(systemEventClass, sourceBaseType, source, event);
            }
        }
        catch (AbortProcessingException e)
        {
            // If the act of invoking the processListener method causes an AbortProcessingException to be thrown,
            // processing of the listeners must be aborted, no further processing of the listeners for this event must
            // take place, and the exception must be logged with Level.SEVERE.
            log.error("Event processing was aborted", e);
        }
    }

    @Override
    public void publishEvent(Class<? extends SystemEvent> systemEventClass, Object source)
    {
        publishEvent(systemEventClass, source.getClass(), source);
    }

    @Override
    public final void removeELContextListener(final ELContextListener listener)
    {
        synchronized (_elContextListeners)
        {
            _elContextListeners.remove(listener);
        }
    }

    @Override
    public final ELContextListener[] getELContextListeners()
    {
        // this gets called on every request, so I can't afford to synchronize
        // I just have to trust that toArray() with do the right thing if the
        // list is changing (not likely)
        return _elContextListeners.toArray(new ELContextListener[_elContextListeners.size()]);
    }

    @Override
    public final void setActionListener(final ActionListener actionListener)
    {
        checkNull(actionListener, "actionListener");

        _actionListener = actionListener;
        if (log.isTraceEnabled())
            log.trace("set actionListener = " + actionListener.getClass().getName());
    }

    @Override
    public final ActionListener getActionListener()
    {
        return _actionListener;
    }

    @Override
    public Iterator<String> getBehaviorIds()
    {
        return _behaviorClassMap.keySet().iterator();
    }

    @Override
    public final Iterator<String> getComponentTypes()
    {
        return _componentClassMap.keySet().iterator();
    }

    @Override
    public final Iterator<String> getConverterIds()
    {
        return _converterIdToClassMap.keySet().iterator();
    }

    @Override
    public final Iterator<Class<?>> getConverterTypes()
    {
        return _converterClassNameToClassMap.keySet().iterator();
    }

    @Override
    public final void setDefaultLocale(final Locale locale)
    {
        checkNull(locale, "locale");

        _defaultLocale = locale;
        if (log.isTraceEnabled())
            log.trace("set defaultLocale = " + locale.getCountry() + " " + locale.getLanguage());
    }

    @Override
    public final Locale getDefaultLocale()
    {
        return _defaultLocale;
    }

    @Override
    public final void setMessageBundle(final String messageBundle)
    {
        checkNull(messageBundle, "messageBundle");

        _messageBundle = messageBundle;
        if (log.isTraceEnabled())
            log.trace("set MessageBundle = " + messageBundle);
    }

    @Override
    public final String getMessageBundle()
    {
        return _messageBundle;
    }

    @Override
    public final void setNavigationHandler(final NavigationHandler navigationHandler)
    {
        checkNull(navigationHandler, "navigationHandler");

        _navigationHandler = navigationHandler;
        if (log.isTraceEnabled())
            log.trace("set NavigationHandler = " + navigationHandler.getClass().getName());
    }

    @Override
    public final NavigationHandler getNavigationHandler()
    {
        return _navigationHandler;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public final void setPropertyResolver(final PropertyResolver propertyResolver)
    {
        checkNull(propertyResolver, "propertyResolver");

        if (getFaceContext() != null)
        {
            throw new IllegalStateException("propertyResolver must be defined before request processing");
        }

        _runtimeConfig.setPropertyResolver(propertyResolver);

        if (log.isTraceEnabled())
            log.trace("set PropertyResolver = " + propertyResolver.getClass().getName());
    }

    @Override
    public ProjectStage getProjectStage()
    {
        // If the value has already been determined by a previous call to this
        // method, simply return that value.
        if (_projectStage == null)
        {
            String stageName = null;
            // Look for a JNDI environment entry under the key given by the
            // value of
            // ProjectStage.PROJECT_STAGE_JNDI_NAME (return type of
            // java.lang.String).
            try
            {
                Context ctx = new InitialContext();
                Object temp = ctx.lookup(ProjectStage.PROJECT_STAGE_JNDI_NAME);
                if (temp != null)
                {
                    if (temp instanceof String)
                    {
                        stageName = (String) temp;
                    }
                    else
                    {
                        log.error("JNDI lookup for key " + ProjectStage.PROJECT_STAGE_JNDI_NAME
                                + " should return a java.lang.String value");
                    }
                }
            }
            catch (NamingException e)
            {
                // no-op
            }

            /*
             * If found, continue with the algorithm below, otherwise, look for an entry in the initParamMap of the
             * ExternalContext from the current FacesContext with the key ProjectStage.PROJECT_STAGE_PARAM_NAME
             */
            if (stageName == null)
            {
                FacesContext context = FacesContext.getCurrentInstance();
                stageName = context.getExternalContext().getInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME);
            }

            /*
             * If not found so far, let's try the Apache MyFaces extension (see MYFACES-2235)
             */
            if (stageName == null)
            {
                stageName = System.getProperty(MYFACES_PROJECT_STAGE_SYSTEM_PROPERTY_NAME);
            }

            // If a value is found found
            if (stageName != null)
            {
                /*
                 * see if an enum constant can be obtained by calling ProjectStage.valueOf(), passing the value from the
                 * initParamMap. If this succeeds without exception, save the value and return it.
                 */
                try
                {
                    _projectStage = ProjectStage.valueOf(stageName);
                    return _projectStage;
                }
                catch (IllegalArgumentException e)
                {
                    log.error("Couldn't discover the current project stage", e);
                }
            }
            else
            {
                if (log.isInfoEnabled())
                {
                    log.info("Couldn't discover the current project stage, using " + ProjectStage.Production);
                }
            }

            /*
             * If not found, or any of the previous attempts to discover the enum constant value have failed, log a
             * descriptive error message, assign the value as ProjectStage.Production and return it.
             */

            _projectStage = ProjectStage.Production;
        }

        return _projectStage;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public final PropertyResolver getPropertyResolver()
    {
        return PROPERTYRESOLVER;
    }

    @Override
    public final void setResourceHandler(ResourceHandler resourceHandler)
    {
        checkNull(resourceHandler, "resourceHandler");

        _resourceHandler = resourceHandler;
    }

    @Override
    public final ResourceHandler getResourceHandler()
    {
        return _resourceHandler;
    }

    @Override
    public final void setSupportedLocales(final Collection<Locale> locales)
    {
        checkNull(locales, "locales");

        _supportedLocales = locales;
        if (log.isTraceEnabled())
            log.trace("set SupportedLocales");
    }

    @Override
    public final Iterator<Locale> getSupportedLocales()
    {
        return _supportedLocales.iterator();
    }

    @Override
    public final Iterator<String> getValidatorIds()
    {
        return _validatorClassMap.keySet().iterator();
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public final void setVariableResolver(final VariableResolver variableResolver)
    {
        checkNull(variableResolver, "variableResolver");

        if (getFaceContext() != null)
        {
            throw new IllegalStateException("variableResolver must be defined before request processing");
        }

        _runtimeConfig.setVariableResolver(variableResolver);

        if (log.isTraceEnabled())
            log.trace("set VariableResolver = " + variableResolver.getClass().getName());
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public final VariableResolver getVariableResolver()
    {
        return VARIABLERESOLVER;
    }

    @Override
    public final void setViewHandler(final ViewHandler viewHandler)
    {
        checkNull(viewHandler, "viewHandler");

        _viewHandler = viewHandler;
        if (log.isTraceEnabled())
            log.trace("set ViewHandler = " + viewHandler.getClass().getName());
    }

    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass,
                                 SystemEventListener listener)
    {
        checkNull(systemEventClass, "systemEventClass");
        checkNull(listener, "listener");

        SystemListenerEntry systemListenerEntry;
        synchronized (_systemEventListenerClassMap)
        {
            systemListenerEntry = _systemEventListenerClassMap.get(systemEventClass);
            if (systemListenerEntry == null)
            {
                systemListenerEntry = new SystemListenerEntry();
                _systemEventListenerClassMap.put(systemEventClass, systemListenerEntry);
            }
        }

        systemListenerEntry.addListener(listener, sourceClass);
    }

    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass,
                                     SystemEventListener listener)
    {
        checkNull(systemEventClass, "systemEventClass");
        checkNull(listener, "listener");

        SystemListenerEntry systemListenerEntry = _systemEventListenerClassMap.get(systemEventClass);
        if (systemListenerEntry != null)
        {
            systemListenerEntry.removeListener(listener, sourceClass);
        }
    }

    @Override
    public final ViewHandler getViewHandler()
    {
        return _viewHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addBehavior(String behaviorId, String behaviorClass)
    {
        checkNull(behaviorId, "behaviorId");
        checkEmpty(behaviorId, "behaviorId");
        checkNull(behaviorClass, "behaviorClass");
        checkEmpty(behaviorClass, "behaviorClass");

        try
        {
            _behaviorClassMap.put(behaviorId, ClassUtils.simpleClassForName(behaviorClass));
            if (log.isTraceEnabled())
                log.trace("add Behavior class = " + behaviorClass + " for id = " + behaviorId);
        }
        catch (Exception e)
        {
            log.error("Behavior class " + behaviorClass + " not found", e);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public final void addComponent(final String componentType, final String componentClassName)
    {
        checkNull(componentType, "componentType");
        checkEmpty(componentType, "componentType");
        checkNull(componentClassName, "componentClassName");
        checkEmpty(componentClassName, "componentClassName");

        try
        {
            _componentClassMap.put(componentType, ClassUtils.simpleClassForName(componentClassName));
            if (log.isTraceEnabled())
                log.trace("add Component class = " + componentClassName + " for type = " + componentType);
        }
        catch (Exception e)
        {
            log.error("Component class " + componentClassName + " not found", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void addConverter(final String converterId, final String converterClass)
    {
        checkNull(converterId, "converterId");
        checkEmpty(converterId, "converterId");
        checkNull(converterClass, "converterClass");
        checkEmpty(converterClass, "converterClass");

        try
        {
            _converterIdToClassMap.put(converterId, ClassUtils.simpleClassForName(converterClass));
            if (log.isTraceEnabled())
                log.trace("add Converter id = " + converterId + " converterClass = " + converterClass);
        }
        catch (Exception e)
        {
            log.error("Converter class " + converterClass + " not found", e);
        }
    }

    @Override
    public final void addConverter(final Class<?> targetClass, final String converterClass)
    {
        checkNull(targetClass, "targetClass");
        checkNull(converterClass, "converterClass");
        checkEmpty(converterClass, "converterClass");

        try
        {
            _converterClassNameToClassMap.put(targetClass, converterClass);
            if (log.isTraceEnabled())
                log.trace("add Converter for class = " + targetClass + " converterClass = " + converterClass);
        }
        catch (Exception e)
        {
            log.error("Converter class " + converterClass + " not found", e);
        }
    }

    public final void addConverterConfiguration(
                                                final String converterClassName,
                                                final org.apache.myfaces.config.impl.digester.elements.Converter configuration)
    {
        checkNull(converterClassName, "converterClassName");
        checkEmpty(converterClassName, "converterClassName");
        checkNull(configuration, "configuration");

        _converterClassNameToConfigurationMap.put(converterClassName, configuration);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void addValidator(final String validatorId, final String validatorClass)
    {
        checkNull(validatorId, "validatorId");
        checkEmpty(validatorId, "validatorId");
        checkNull(validatorClass, "validatorClass");
        checkEmpty(validatorClass, "validatorClass");

        try
        {
            _validatorClassMap.put(validatorId, ClassUtils.simpleClassForName(validatorClass));
            if (log.isTraceEnabled())
                log.trace("add Validator id = " + validatorId + " class = " + validatorClass);
        }
        catch (Exception e)
        {
            log.error("Validator class " + validatorClass + " not found", e);
        }
    }

    @Override
    public Behavior createBehavior(String behaviorId) throws FacesException
    {
        checkNull(behaviorId, "behaviorId");
        checkEmpty(behaviorId, "behaviorId");

        final Class<? extends Behavior> behaviorClass = this._behaviorClassMap.get(behaviorId);
        if (behaviorClass == null)
        {
            throw new FacesException("Could not find any registered behavior-class for behaviorId : " + behaviorId);
        }

        try
        {
            final Behavior behavior = behaviorClass.newInstance();
            return behavior;
        }
        catch (Exception e)
        {
            log.error("Could not instantiate behavior " + behaviorClass, e);
            throw new FacesException("Could not instantiate behavior: " + behaviorClass, e);
        }
    }

    @Override
    public UIComponent createComponent(FacesContext context, Resource componentResource)
    {
        UIComponent component = null;
        Resource resource;
        String fqcn;
        Class<? extends UIComponent> componentClass = null;

        /*
         * Obtain a reference to the ViewDeclarationLanguage for this Application instance by calling
         * ViewHandler.getViewDeclarationLanguage(javax.faces.context.FacesContext, java.lang.String), passing the
         * viewId found by calling UIViewRoot.getViewId() on the UIViewRoot in the argument FacesContext.
         */
        UIViewRoot view = context.getViewRoot();
        ViewDeclarationLanguage vdl = getViewHandler().getViewDeclarationLanguage(context, view.getViewId());

        /*
         * Obtain a reference to the composite component metadata for this composite component by calling
         * ViewDeclarationLanguage.getComponentMetadata(javax.faces.context.FacesContext,
         * javax.faces.application.Resource), passing the facesContext and componentResource arguments to this method.
         * This version of JSF specification uses JavaBeans as the API to the component metadata.
         */
        BeanInfo metadata = vdl.getComponentMetadata(context, componentResource);

        /*
         * Determine if the component author declared a component-type for this component instance by obtaining the
         * BeanDescriptor from the component metadata and calling its getValue() method, passing
         * UIComponent.COMPOSITE_COMPONENT_TYPE_KEY as the argument. If non-null, the result must be a ValueExpression
         * whose value is the component-type of the UIComponent to be created for this Resource component. Call through
         * to createComponent(java.lang.String) to create the component.
         */
        BeanDescriptor descriptor = metadata.getBeanDescriptor();
        ValueExpression componentType = (ValueExpression) descriptor.getValue(UIComponent.COMPOSITE_COMPONENT_TYPE_KEY);

        if (componentType != null)
        {
            component = createComponent((String) componentType.getValue(context.getELContext()));
        }
        else
        {
            /*
             * Otherwise, determine if a script based component for this Resource can be found by calling
             * ViewDeclarationLanguage.getScriptComponentResource(javax.faces.context.FacesContext,
             * javax.faces.application.Resource). If the result is non-null, and is a script written in one of the
             * languages listed in JSF 4.3 of the specification prose document, create a UIComponent instance from the
             * script resource.
             */
            resource = vdl.getScriptComponentResource(context, componentResource);
            if (resource != null)
            {
                String name = resource.getResourceName();
                String className = name.substring(0, name.lastIndexOf('.'));

                component = (UIComponent)ClassUtils.newInstance(className);
                component.getAttributes().put(Resource.COMPONENT_RESOURCE_KEY, componentResource);
            }
            else
            {
                /*
                 * Otherwise, let library-name be the return from calling Resource.getLibraryName() on the argument
                 * componentResource and resource-name be the return from calling Resource.getResourceName() on the
                 * argument componentResource. Create a fully qualified Java class name by removing any file extension
                 * from resource-name and let fqcn be library-name + "." + resource-name. If a class with the name of
                 * fqcn cannot be found, take no action and continue to the next step. If any of InstantiationException,
                 * IllegalAccessException, or ClassCastException are thrown, wrap the exception in a FacesException and
                 * re-throw it. If any other exception is thrown, log the exception and continue to the next step.
                 */

                fqcn = componentResource.getLibraryName() + "." + componentResource.getResourceName();

                try
                {
                    componentClass = ClassUtils.classForName(fqcn);
                }
                catch (ClassNotFoundException e)
                {
                }

                if (componentClass != null)
                {
                    try
                    {
                        component = componentClass.newInstance();
                    }
                    catch (InstantiationException e)
                    {
                        log.error("Could not instantiate component class name = " + fqcn, e);
                        throw new FacesException("Could not instantiate component class name = " + fqcn, e);
                    }
                    catch (IllegalAccessException e)
                    {
                        log.error("Could not instantiate component class name = " + fqcn, e);
                        throw new FacesException("Could not instantiate component class name = " + fqcn, e);
                    }
                    catch (Exception e)
                    {
                        log.error("Could not instantiate component class name = " + fqcn, e);
                    }
                }

                /*
                 * If none of the previous steps have yielded a UIComponent instance, call
                 * createComponent(java.lang.String) passing "javax.faces.NamingContainer" as the argument.
                 */
                if (component == null)
                {
                    createComponent(UINamingContainer.COMPONENT_TYPE);
                }
            }
        }

        /*
         * Call UIComponent.setRendererType(java.lang.String) on the UIComponent instance, passing
         * "javax.faces.Composite" as the argument.
         */
        component.setRendererType("javax.faces.Composite");

        /*
         * Store the argument Resource in the attributes Map of the UIComponent under the key,
         * Resource.COMPONENT_RESOURCE_KEY.
         */
        component.getAttributes().put(Resource.COMPONENT_RESOURCE_KEY, componentResource);

        /*
         * Store composite component metadata in the attributes Map of the UIComponent under the key,
         * UIComponent.BEANINFO_KEY.
         */
        component.getAttributes().put(UIComponent.BEANINFO_KEY, metadata);

        /*
         * Before the component instance is returned, it must be inspected for the presence of a ListenerFor annotation.
         * If this annotation is present, the action listed in ListenerFor must be taken on the component, before it is
         * returned from this method.
         */
        _handleAnnotations(context, component, component);

        return component;
    }

    @Override
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType)
    {
        checkNull(context, "context");
        checkNull(componentType, "componentType");

        // Like createComponent(String)
        UIComponent component = createComponent(componentType);

        _inspectRenderer(context, component, componentType, rendererType);

        return component;
    }

    @Override
    public final UIComponent createComponent(final String componentType) throws FacesException
    {
        checkNull(componentType, "componentType");
        checkEmpty(componentType, "componentType");

        final Class<? extends UIComponent> componentClass = _componentClassMap.get(componentType);
        if (componentClass == null)
        {
            log.error("Undefined component type " + componentType);
            throw new FacesException("Undefined component type " + componentType);
        }

        try
        {
            UIComponent component = componentClass.newInstance();            
            _handleAnnotations(FacesContext.getCurrentInstance(), component, component);
            return component;
        }
        catch (Exception e)
        {
            log.error("Could not instantiate component componentType = " + componentType, e);
            throw new FacesException("Could not instantiate component componentType = " + componentType, e);
        }
    }

    /**
     * @deprecated Use createComponent(ValueExpression, FacesContext, String) instead.
     */
    @Deprecated
    @Override
    public final UIComponent createComponent(final ValueBinding valueBinding, final FacesContext facesContext,
                                             final String componentType) throws FacesException
    {

        checkNull(valueBinding, "valueBinding");
        checkNull(facesContext, "facesContext");
        checkNull(componentType, "componentType");
        checkEmpty(componentType, "componentType");

        final ValueExpression valExpression = new ValueBindingToValueExpression(valueBinding);

        return createComponent(valExpression, facesContext, componentType);
    }

    /**
     * Return an instance of the converter class that has been registered under the specified id.
     * <p>
     * Converters are registered via faces-config.xml files, and can also be registered via the addConverter(String id,
     * Class converterClass) method on this class. Here the the appropriate Class definition is found, then an instance
     * is created and returned.
     * <p>
     * A converter registered via a config file can have any number of nested attribute or property tags. The JSF
     * specification is very vague about what effect these nested tags have. This method ignores nested attribute
     * definitions, but for each nested property tag the corresponding setter is invoked on the new Converter instance
     * passing the property's defaultValuer. Basic typeconversion is done so the target properties on the Converter
     * instance can be String, int, boolean, etc. Note that:
     * <ol>
     * <li>the Sun Mojarra JSF implemenation ignores nested property tags completely, so this behaviour cannot be relied
     * on across implementations.
     * <li>there is no equivalent functionality for converter classes registered via the Application.addConverter api
     * method.
     * </ol>
     * <p>
     * Note that this method is most commonly called from the standard f:attribute tag. As an alternative, most
     * components provide a "converter" attribute which uses an EL expression to create a Converter instance, in which
     * case this method is not invoked at all. The converter attribute allows the returned Converter instance to be
     * configured via normal dependency-injection, and is generally a better choice than using this method.
     */
    @Override
    public final Converter createConverter(final String converterId)
    {
        checkNull(converterId, "converterId");
        checkEmpty(converterId, "converterId");

        final Class<? extends Converter> converterClass = _converterIdToClassMap.get(converterId);
        if (converterClass == null)
        {
            throw new FacesException("Could not find any registered converter-class by converterId : " + converterId);
        }

        try
        {
            final Converter converter = converterClass.newInstance();

            setConverterProperties(converterClass, converter);

            return converter;
        }
        catch (Exception e)
        {
            log.error("Could not instantiate converter " + converterClass, e);
            throw new FacesException("Could not instantiate converter: " + converterClass, e);
        }
    }

    @Override
    public final Converter createConverter(final Class<?> targetClass)
    {
        checkNull(targetClass, "targetClass");

        return internalCreateConverter(targetClass);
    }

    private Converter internalCreateConverter(final Class<?> targetClass)
    {
        // Locate a Converter registered for the target class itself.
        String converterClassName = _converterClassNameToClassMap.get(targetClass);

        // Locate a Converter registered for interfaces that are
        // implemented by the target class (directly or indirectly).
        if (converterClassName == null)
        {
            final Class<?> interfaces[] = targetClass.getInterfaces();
            if (interfaces != null)
            {
                for (int i = 0, len = interfaces.length; i < len; i++)
                {
                    // search all superinterfaces for a matching converter,
                    // create it
                    final Converter converter = internalCreateConverter(interfaces[i]);
                    if (converter != null)
                    {
                        return converter;
                    }
                }
            }
        }

        // Get EnumConverter for enum classes with no special converter, check
        // here as recursive call with java.lang.Enum will not work
        if (converterClassName == null && targetClass.isEnum()) {
            converterClassName = _converterClassNameToClassMap.get(Enum.class);
        }

        if (converterClassName != null)
        {
            try
            {
                Class<? extends Converter> converterClass = ClassUtils.simpleClassForName(converterClassName);

                Converter converter = null;
                try
                {
                    // look for a constructor that takes a single Class object
                    // See JSF 1.2 javadoc for Converter
                    Constructor<? extends Converter> constructor = converterClass
                            .getConstructor(new Class[] { Class.class });

                    converter = constructor.newInstance(new Object[] { targetClass });
                }
                catch (Exception e)
                {
                    // if there is no matching constructor use no-arg
                    // constructor
                    converter = converterClass.newInstance();
                }

                setConverterProperties(converterClass, converter);

                return converter;
            }
            catch (Exception e)
            {
                log.error("Could not instantiate converter " + converterClassName, e);
                throw new FacesException("Could not instantiate converter: " + converterClassName, e);
            }
        }

        // locate converter for primitive types
        if (targetClass == Long.TYPE)
        {
            return internalCreateConverter(Long.class);
        }
        else if (targetClass == Boolean.TYPE)
        {
            return internalCreateConverter(Boolean.class);
        }
        else if (targetClass == Double.TYPE)
        {
            return internalCreateConverter(Double.class);
        }
        else if (targetClass == Byte.TYPE)
        {
            return internalCreateConverter(Byte.class);
        }
        else if (targetClass == Short.TYPE)
        {
            return internalCreateConverter(Short.class);
        }
        else if (targetClass == Integer.TYPE)
        {
            return internalCreateConverter(Integer.class);
        }
        else if (targetClass == Float.TYPE)
        {
            return internalCreateConverter(Float.class);
        }
        else if (targetClass == Character.TYPE)
        {
            return internalCreateConverter(Character.class);
        }

        // Locate a Converter registered for the superclass (if any) of the
        // target class,
        // recursively working up the inheritance hierarchy.
        Class<?> superClazz = targetClass.getSuperclass();

        return superClazz != null ? internalCreateConverter(superClazz) : null;

    }

    private void setConverterProperties(final Class<?> converterClass, final Converter converter)
    {
        final org.apache.myfaces.config.impl.digester.elements.Converter converterConfig = _converterClassNameToConfigurationMap
                .get(converterClass.getName());

        if (converterConfig != null)
        {
            for (Property property : converterConfig.getProperties())
            {
                try
                {
                    BeanUtils.setProperty(converter, property.getPropertyName(), property.getDefaultValue());
                }
                catch (Throwable th)
                {
                    log.error("Initializing converter : " + converterClass.getName() + " with property : "
                            + property.getPropertyName() + " and value : " + property.getDefaultValue() + " failed.");
                }
            }
        }
    }

    // Note: this method used to be synchronized in the JSF 1.1 version. Why?
    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public final MethodBinding createMethodBinding(final String reference, Class<?>[] params)
            throws ReferenceSyntaxException
    {
        checkNull(reference, "reference");
        checkEmpty(reference, "reference");

        // TODO: this check should be performed by the expression factory. It is
        // a requirement of the TCK
        if (!(reference.startsWith("#{") && reference.endsWith("}")))
        {
            throw new ReferenceSyntaxException("Invalid method reference: '" + reference + "'");
        }

        if (params == null)
            params = new Class[0];

        MethodExpression methodExpression;

        try
        {
            methodExpression = getExpressionFactory().createMethodExpression(threadELContext(), reference,
                                                                             Object.class, params);
        }
        catch (ELException e)
        {
            throw new ReferenceSyntaxException(e);
        }

        return new MethodExpressionToMethodBinding(methodExpression);
    }

    @Override
    public final Validator createValidator(final String validatorId) throws FacesException
    {
        checkNull(validatorId, "validatorId");
        checkEmpty(validatorId, "validatorId");

        Class<?> validatorClass = _validatorClassMap.get(validatorId);
        if (validatorClass == null)
        {
            String message = "Unknown validator id '" + validatorId + "'.";
            log.error(message);
            throw new FacesException(message);
        }

        try
        {
            return (Validator) validatorClass.newInstance();
        }
        catch (Exception e)
        {
            log.error("Could not instantiate validator " + validatorClass, e);
            throw new FacesException("Could not instantiate validator: " + validatorClass, e);
        }
    }

    /**
     * @deprecated
     */
    @Override
    public final ValueBinding createValueBinding(final String reference) throws ReferenceSyntaxException
    {
        checkNull(reference, "reference");
        checkEmpty(reference, "reference");

        ValueExpression valueExpression;

        try
        {
            valueExpression = getExpressionFactory().createValueExpression(threadELContext(), reference, Object.class);
        }
        catch (ELException e)
        {
            throw new ReferenceSyntaxException(e);
        }

        return new ValueExpressionToValueBinding(valueExpression);
    }

    // gets the elContext from the current FacesContext()
    private final ELContext threadELContext()
    {
        return getFaceContext().getELContext();
    }

    @Override
    public final String getDefaultRenderKitId()
    {
        return _defaultRenderKitId;
    }

    @Override
    public final void setDefaultRenderKitId(final String defaultRenderKitId)
    {
        _defaultRenderKitId = defaultRenderKitId;
    }

    @Override
    public final StateManager getStateManager()
    {
        return _stateManager;
    }

    @Override
    public final void setStateManager(final StateManager stateManager)
    {
        checkNull(stateManager, "stateManager");

        _stateManager = stateManager;
    }

    private void checkNull(final Object param, final String paramName)
    {
        if (param == null)
        {
            throw new NullPointerException(paramName + " cannot be null.");
        }
    }

    private void checkEmpty(final String param, final String paramName)
    {
        if (param.length() == 0)
        {
            throw new NullPointerException("String " + paramName + " cannot be empty.");
        }
    }

    private static SystemEvent _createEvent(Class<? extends SystemEvent> systemEventClass, Object source,
                                            SystemEvent event)
    {
        if (event == null)
        {
            try
            {
                Constructor<?>[] constructors = systemEventClass.getConstructors();
                Constructor<? extends SystemEvent> constructor = null;
                for (Constructor<?> c : constructors)
                {
                    if (c.getParameterTypes().length == 1)
                    {
                        // Safe cast, since the constructor belongs
                        // to a class of type SystemEvent
                        constructor = (Constructor<? extends SystemEvent>) c;
                        break;
                    }
                }
                if (constructor != null)
                {
                    event = constructor.newInstance(source);
                }

            }
            catch (Exception e)
            {
                throw new FacesException("Couldn't instanciate system event of type " + systemEventClass.getName(), e);
            }
        }

        return event;
    }

    private void _handleAnnotations(FacesContext context, Object inspected, UIComponent component)
    {
        _handleListenerFor(context, inspected, component, inspected.getClass().getAnnotation(ListenerFor.class));

        ListenersFor listeners = component.getClass().getAnnotation(ListenersFor.class);
        if (listeners != null)
        {
            for (ListenerFor listenerFor : listeners.value())
            {
                _handleListenerFor(context, inspected, component, listenerFor);
            }
        }

        _handleResourceDependency(context, component, inspected.getClass().getAnnotation(ResourceDependency.class));
        ResourceDependencies dependencies = inspected.getClass().getAnnotation(ResourceDependencies.class);
        if (dependencies != null)
        {
            for (ResourceDependency dependency : dependencies.value())
            {
                _handleResourceDependency(context, component, dependency);
            }
        }
    }

    private void _handleListenerFor(FacesContext context, Object inspected, UIComponent component,
                                    ListenerFor annotation)
    {
        // If this annotation is not present on the class in question, no action must be taken.
        if (annotation != null)
        {
            // Determine the "target" on which to call subscribeToEvent.
            // If the class to which this annotation is attached implements ComponentSystemEventListener
            if (inspected instanceof ComponentSystemEventListener)
            {
                // If the class to which this annotation is attached is a UIComponent instance, "target" is the
                // UIComponent instance.

                // If the class to which this annotation is attached is a Renderer instance, "target" is the
                // UIComponent instance.

                /*
                 * If "target" is a UIComponent call UIComponent.subscribeToEvent(Class, ComponentSystemEventListener)
                 * passing the systemEventClass() of the annotation as the first argument and the instance of the class
                 * to which this annotation is attached (which must implement ComponentSystemEventListener) as the
                 * second argument.
                 */
                component.subscribeToEvent(annotation.systemEventClass(), (ComponentSystemEventListener) inspected);
            }
            // If the class to which this annotation is attached implements SystemEventListener and does not implement
            // ComponentSystemEventListener, "target" is the Application instance.
            else if (component instanceof SystemEventListener)
            {
                // If "target" is the Application instance, inspect the value of the sourceClass() annotation attribute
                // value.
                if (Void.class.equals(annotation.sourceClass()))
                {
                    /*
                     * If the value is Void.class, call Application.subscribeToEvent(Class, SystemEventListener),
                     * passing the value of systemEventClass() as the first argument and the instance of the class to
                     * which this annotation is attached (which must implement SystemEventListener) as the second
                     * argument.
                     */
                    subscribeToEvent(annotation.systemEventClass(), (SystemEventListener) inspected);
                }
                else
                {
                    /*
                     * Otherwise, call Application.subscribeToEvent(Class, Class, SystemEventListener), passing the
                     * value of systemEventClass() as the first argument, the value of sourceClass() as the second
                     * argument, and the instance of the class to which this annotation is attached (which must
                     * implement SystemEventListener) as the third argument.
                     */
                    subscribeToEvent(annotation.systemEventClass(), annotation.sourceClass(),
                                     (SystemEventListener) inspected);
                }
            }

            /*
             * If the class to which this annotation is attached implements ComponentSystemEventListener and is neither
             * an instance of Renderer nor UIComponent, the action taken is unspecified. This case must not trigger any
             * kind of error.
             */
        }
    }

    private void _handleResourceDependency(FacesContext context, UIComponent component, ResourceDependency annotation)
    {
        // If this annotation is not present on the class in question, no action must be taken.
        if (annotation != null)
        {
            // Create a UIOutput instance by passing javax.faces.Output. to
            // Application.createComponent(java.lang.String).
            UIOutput output = (UIOutput) createComponent(UIOutput.COMPONENT_TYPE);

            // Get the annotation instance from the class and obtain the values of the name, library, and
            // target attributes.
            String name = annotation.name();

            // Obtain the renderer-type for the resource name by passing name to
            // ResourceHandler.getRendererTypeForResourceName(java.lang.String).
            String rendererType = getResourceHandler().getRendererTypeForResourceName(name);

            // Call setRendererType on the UIOutput instance, passing the renderer-type.
            output.setRendererType(rendererType);

            // Obtain the Map of attributes from the UIOutput component by calling UIComponent.getAttributes().
            Map<String, Object> attributes = output.getAttributes();

            // Store the name into the attributes Map under the key "name".
            attributes.put("name", name);

            // If library is the empty string, let library be null.
            String library = annotation.library();
            if (library != null && library.length() > 0)
            {
                // If library is non-null, store it under the key "library".
                if ("this".equals(library))
                {
                    // Special "this" behavior
                    Resource resource = (Resource)component.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                    if (resource != null)
                    {
                        attributes.put("library", resource.getLibraryName());
                    }
                }
                else
                {
                    attributes.put("library", library);
                }
            }

            // If target is the empty string, let target be null.
            String target = annotation.target();
            if (target != null && target.length() > 0)
            {
                // If target is non-null, store it under the key "target".
                attributes.put("target", target);
            }
            else
            {
                // Otherwise, if target is null, call UIViewRoot.addComponentResource(javax.faces.context.FacesContext,
                // javax.faces.component.UIComponent), passing the UIOutput instance as the second argument.
                context.getViewRoot().addComponentResource(context, output);
            }
        }
    }
    
    private void _inspectRenderer(FacesContext context, UIComponent component, String componentType, String rendererType)
    {
        /*
         * The Renderer instance to inspect must be obtained by calling FacesContext.getRenderKit() and calling
         * RenderKit.getRenderer(java.lang.String, java.lang.String) on the result, passing the argument componentFamily
         * of the newly created component as the first argument and the argument rendererType as the second argument.
         */
        Renderer renderer = context.getRenderKit().getRenderer(component.getFamily(), rendererType);
        if (renderer == null)
        {
            // If no such Renderer can be found, a message must be logged with a helpful error message.
            log.error("renderer cannot be found for component type " + componentType + " and renderer type "
                    + rendererType);
        }
        else
        {
            // Otherwise, UIComponent.setRendererType(java.lang.String) must be called on the newly created
            // UIComponent instance, passing the argument rendererType as the argument.
            component.setRendererType(rendererType);

            /*
             * except the Renderer for the component to be returned must be inspected for the annotations mentioned in
             * createComponent(ValueExpression, FacesContext, String) as specified in the documentation for that method.
             */
            _handleAnnotations(context, renderer, component);
        }
    }

    private static SystemEvent _traverseListenerList(List<? extends SystemEventListener> listeners,
                                                     Class<? extends SystemEvent> systemEventClass, Object source,
                                                     SystemEvent event)
    {
        if (listeners != null && !listeners.isEmpty())
        {
            for (SystemEventListener listener : listeners)
            {
                // Call SystemEventListener.isListenerForSource(java.lang.Object), passing the source argument.
                // If this returns false, take no action on the listener.
                if (listener.isListenerForSource(source))
                {
                    // Otherwise, if the event to be passed to the listener instances has not yet been constructed,
                    // construct the event, passing source as the argument to the one-argument constructor that takes
                    // an Object. This same event instance must be passed to all listener instances.
                    event = _createEvent(systemEventClass, source, event);

                    // Call SystemEvent.isAppropriateListener(javax.faces.event.FacesListener), passing the listener
                    // instance as the argument. If this returns false, take no action on the listener.
                    if (event.isAppropriateListener(listener))
                    {
                        // Call SystemEvent.processListener(javax.faces.event.FacesListener), passing the listener
                        // instance.
                        event.processListener(listener);
                    }
                }
            }
        }

        return event;
    }

    private static class SystemListenerEntry
    {
        private List<SystemEventListener> _lstSystemEventListener;
        private Map<Class<?>, List<SystemEventListener>> _sourceClassMap;

        public SystemListenerEntry()
        {
        }

        public void addListener(SystemEventListener listener)
        {
            assert listener != null;

            addListenerNoDuplicate(getAnySourceListenersNotNull(), listener);
        }

        public void addListener(SystemEventListener listener, Class<?> source)
        {
            assert listener != null;

            if (source == null)
            {
                addListener(listener);
            }
            else
            {
                addListenerNoDuplicate(getSpecificSourceListenersNotNull(source), listener);
            }
        }

        public void removeListener(SystemEventListener listener)
        {
            assert listener != null;

            if (_lstSystemEventListener != null)
            {
                _lstSystemEventListener.remove(listener);
            }
        }

        public void removeListener(SystemEventListener listener, Class<?> sourceClass)
        {
            assert listener != null;

            if (sourceClass == null)
            {
                removeListener(listener);
            }
            else
            {
                if (_sourceClassMap != null)
                {
                    List<SystemEventListener> listeners = _sourceClassMap.get(sourceClass);
                    if (listeners != null)
                    {
                        listeners.remove(listener);
                    }
                }
            }
        }

        public void publish(Class<? extends SystemEvent> systemEventClass, Class<?> classSource, Object source,
                            SystemEvent event)
        {
            if (source != null && _sourceClassMap != null)
            {
                event = _traverseListenerList(_sourceClassMap.get(classSource), systemEventClass, source, event);
            }

            _traverseListenerList(_lstSystemEventListener, systemEventClass, source, event);
        }

        private void addListenerNoDuplicate(List<SystemEventListener> listeners, SystemEventListener listener)
        {
            if (!listeners.contains(listener))
            {
                listeners.add(listener);
            }
        }

        private synchronized List<SystemEventListener> getAnySourceListenersNotNull()
        {
            if (_lstSystemEventListener == null)
            {
                /*
                 * TODO: Check if modification occurs often or not, might have to use a synchronized list instead.
                 * 
                 * Registrations found:
                 */
                _lstSystemEventListener = new CopyOnWriteArrayList<SystemEventListener>();
            }

            return _lstSystemEventListener;
        }

        private synchronized List<SystemEventListener> getSpecificSourceListenersNotNull(Class<?> sourceClass)
        {
            if (_sourceClassMap == null)
            {
                _sourceClassMap = new ConcurrentHashMap<Class<?>, List<SystemEventListener>>();
            }

            List<SystemEventListener> list = _sourceClassMap.get(sourceClass);
            if (list == null)
            {
                /*
                 * TODO: Check if modification occurs often or not, might have to use a synchronized list instead.
                 * 
                 * Registrations found:
                 */
                list = new CopyOnWriteArrayList<SystemEventListener>();
                _sourceClassMap.put(sourceClass, list);
            }

            return list;
        }
    }
}
