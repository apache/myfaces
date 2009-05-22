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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;

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
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;

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

    // ~ Instance fields
    // ----------------------------------------------------------------------------

    private Collection<Locale> _supportedLocales = Collections.emptySet();
    private Locale _defaultLocale;
    private String _messageBundle;

    private ViewHandler _viewHandler;
    private NavigationHandler _navigationHandler;
    private ActionListener _actionListener;
    private String _defaultRenderKitId;
    private StateManager _stateManager;

    private ArrayList<ELContextListener> _elContextListeners;

    // components, converters, and validators can be added at runtime--must
    // synchronize, uses ConcurrentHashMap to allow concurrent read of map
    private final Map<String, Class> _converterIdToClassMap = new ConcurrentHashMap<String, Class>();
    private final Map<Class, String> _converterClassNameToClassMap = new ConcurrentHashMap<Class, String>();
    private final Map<String, org.apache.myfaces.config.impl.digester.elements.Converter> _converterClassNameToConfigurationMap = new ConcurrentHashMap<String, org.apache.myfaces.config.impl.digester.elements.Converter>();
    private final Map<String, Class> _componentClassMap = new ConcurrentHashMap<String, Class>();
    private final Map<String, Class> _validatorClassMap = new ConcurrentHashMap<String, Class>();

    private final RuntimeConfig _runtimeConfig;

    private ELResolver elResolver;

    private ELResolverBuilder resolverBuilderForFaces;

    // ~ Constructors
    // -------------------------------------------------------------------------------

    public ApplicationImpl()
    {
        this(internalGetRuntimeConfig());
    }

    private static RuntimeConfig internalGetRuntimeConfig()
    {
        if (initializingRuntimeConfig.get() == null)
        {
            //It may happen that the current thread value
            //for initializingRuntimeConfig is not set 
            //(note that this value is final, so it just 
            //allow set only once per thread).
            //So the better for this case is try to get
            //the value using RuntimeConfig.getCurrentInstance()
            //instead throw an IllegalStateException (only fails if
            //the constructor is called before setInitializingRuntimeConfig).
            //From other point of view, AbstractFacesInitializer do 
            //the same as below, so there is not problem if
            //we do this here and this is the best place to do
            //this.
            //log.info("initializingRuntimeConfig.get() == null, so loading from ExternalContext");
            ApplicationImpl.setInitializingRuntimeConfig(
                    RuntimeConfig.getCurrentInstance(
                            FacesContext.getCurrentInstance()
                            .getExternalContext()));

            //throw new IllegalStateException("The runtime config instance which is created while initialize myfaces "
            //        + "must be set through ApplicationImpl.setInitializingRuntimeConfig");
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
        _viewHandler = new JspViewHandlerImpl();
        _navigationHandler = new NavigationHandlerImpl();
        _actionListener = new ActionListenerImpl();
        _defaultRenderKitId = "HTML_BASIC";
        _stateManager = new JspStateManagerImpl();
        _elContextListeners = new ArrayList<ELContextListener>();
        _runtimeConfig = runtimeConfig;

        if (log.isTraceEnabled())
            log.trace("New Application instance created");
    }

    public static void setInitializingRuntimeConfig(RuntimeConfig config)
    {
        initializingRuntimeConfig.set(config);
    }

    // ~ Methods
    // ------------------------------------------------------------------------------------

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
    public final ELResolver getELResolver()
    {
        // we don't need synchronization here since it is ok to have multiple instances of the elresolver
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
    public final java.util.ResourceBundle getResourceBundle(final FacesContext facesContext, final String name) throws FacesException,
            NullPointerException
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
    public final UIComponent createComponent(final ValueExpression componentExpression, final FacesContext facesContext,
            final String componentType) throws FacesException, NullPointerException
    {

        checkNull(componentExpression, "componentExpression");
        checkNull(facesContext, "facesContext");
        checkNull(componentType, "componentType");

        final ELContext elContext = facesContext.getELContext();

        try
        {
            final Object retVal = componentExpression.getValue(elContext);

            UIComponent createdComponent;

            if (retVal instanceof UIComponent)
            {
                createdComponent = (UIComponent) retVal;
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
    public final ExpressionFactory getExpressionFactory()
    {
        return _runtimeConfig.getExpressionFactory();
    }

    @Override
    public final Object evaluateExpressionGet(final FacesContext context, final String expression, final Class expectedType) throws ELException
    {
        ELContext elContext = context.getELContext();
        return getExpressionFactory().createValueExpression(elContext, expression, expectedType).getValue(elContext);
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
    public final Iterator<Class> getConverterTypes()
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
    public final ViewHandler getViewHandler()
    {
        return _viewHandler;
    }

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
    public final void addConverter(final Class targetClass, final String converterClass)
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

    public final void addConverterConfiguration(final String converterClassName,
            final org.apache.myfaces.config.impl.digester.elements.Converter configuration)
    {
        checkNull(converterClassName, "converterClassName");
        checkEmpty(converterClassName, "converterClassName");
        checkNull(configuration, "configuration");

        _converterClassNameToConfigurationMap.put(converterClassName, configuration);
    }

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
    public final UIComponent createComponent(final String componentType) throws FacesException
    {
        checkNull(componentType, "componentType");
        checkEmpty(componentType, "componentType");

        final Class componentClass = _componentClassMap.get(componentType);
        if (componentClass == null)
        {
            log.error("Undefined component type " + componentType);
            throw new FacesException("Undefined component type " + componentType);
        }

        try
        {
            return (UIComponent) componentClass.newInstance();
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
     * Return an instance of the converter class that has been registered under
     * the specified id.
     * <p>
     * Converters are registered via faces-config.xml files, and can also be registered
     * via the addConverter(String id, Class converterClass) method on this class. Here
     * the the appropriate Class definition is found, then an instance is created and
     * returned.
     * <p>
     * A converter registered via a config file can have any number of nested attribute or
     * property tags. The JSF specification is very vague about what effect these nested
     * tags have. This method ignores nested attribute definitions, but for each nested
     * property tag the corresponding setter is invoked on the new Converter instance
     * passing the property's defaultValuer. Basic typeconversion is done so the target
     * properties on the Converter instance can be String, int, boolean, etc. Note that:
     * <ol>
     * <li>the Sun Mojarra JSF implemenation ignores nested property tags completely, so
     * this behaviour cannot be relied on across implementations.
     * <li>there is no equivalent functionality for converter classes registered via
     * the Application.addConverter api method.
     * </ol>
     * <p>
     * Note that this method is most commonly called from the standard f:attribute tag.
     * As an alternative, most components provide a "converter" attribute which uses an
     * EL expression to create a Converter instance, in which case this method is not
     * invoked at all. The converter attribute allows the returned Converter instance to
     * be configured via normal dependency-injection, and is generally a better choice
     * than using this method.
     */
    @Override
    public final Converter createConverter(final String converterId)
    {
        checkNull(converterId, "converterId");
        checkEmpty(converterId, "converterId");

        final Class converterClass = _converterIdToClassMap.get(converterId);
        if(converterClass == null)
        {
            throw new FacesException("Could not find any registered converter-class by converterId : "+converterId);
        }

        try
        {
            final Converter converter = (Converter) converterClass.newInstance();

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
    public final Converter createConverter(final Class targetClass)
    {
        checkNull(targetClass, "targetClass");

        return internalCreateConverter(targetClass);
    }

    private Converter internalCreateConverter(final Class targetClass)
    {
        // Locate a Converter registered for the target class itself.
        String converterClassName = _converterClassNameToClassMap.get(targetClass);

        // Get EnumConverter for enum classes with no special converter, check
        // here as recursive call with java.lang.Enum will not work
        if (converterClassName == null && targetClass.isEnum()) {
            converterClassName = _converterClassNameToClassMap.get(Enum.class);
        }

        // Locate a Converter registered for interfaces that are
        // implemented by the target class (directly or indirectly).
        if (converterClassName == null)
        {
            final Class interfaces[] = targetClass.getInterfaces();
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

        if (converterClassName != null)
        {
            try
            {
                final Class converterClass = ClassUtils.simpleClassForName(converterClassName);

                Converter converter = null;
                try
                {
                    // look for a constructor that takes a single Class object
                    // See JSF 1.2 javadoc for Converter
                    final Constructor constructor = converterClass.getConstructor(new Class[] { Class.class });
                    converter = (Converter) constructor.newInstance(new Object[] { targetClass });
                }
                catch (Exception e)
                {
                    // if there is no matching constructor use no-arg
                    // constructor
                    converter = (Converter) converterClass.newInstance();
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
        Class superClazz = targetClass.getSuperclass();

        return superClazz != null ? internalCreateConverter(superClazz) : null;

    }

    private void setConverterProperties(final Class converterClass, final Converter converter)
    {
        final org.apache.myfaces.config.impl.digester.elements.Converter converterConfig = _converterClassNameToConfigurationMap
                .get(converterClass.getName());

        if (converterConfig != null)
        {

            final Iterator it = converterConfig.getProperties();

            while (it.hasNext())
            {
                final Property property = (Property) it.next();

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
    public final MethodBinding createMethodBinding(final String reference, Class[] params) throws ReferenceSyntaxException
    {
        checkNull(reference, "reference");
        checkEmpty(reference, "reference");

        // TODO: this check should be performed by the expression factory. It is a requirement of the TCK
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

        Class validatorClass = _validatorClassMap.get(validatorId);
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
        _stateManager = stateManager;
    }

    private void checkNull(final Object param, final String paramName)
    {
        if (param == null)
        {
            throw new NullPointerException(paramName + " can not be null.");
        }
    }

    private void checkEmpty(final String param, final String paramName)
    {
        if (param.length() == 0)
        {
            throw new NullPointerException("String " + paramName + " can not be empty.");
        }
    }
}
