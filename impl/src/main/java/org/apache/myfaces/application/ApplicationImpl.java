/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.application;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.jsp.JspStateManagerImpl;
import org.apache.myfaces.application.jsp.JspViewHandlerImpl;
import org.apache.myfaces.config.impl.digester.elements.Property;
import org.apache.myfaces.el.NullPropertyResolver;
import org.apache.myfaces.el.NullVariableResolver;
import org.apache.myfaces.el.convert.ELResolverToPropertyResolver;
import org.apache.myfaces.el.convert.ELResolverToVariableResolver;
import org.apache.myfaces.el.convert.MethodExpressionToMethodBinding;
import org.apache.myfaces.el.convert.ValueBindingToValueExpression;
import org.apache.myfaces.el.convert.ValueExpressionToValueBinding;
import org.apache.myfaces.el.unified.resolver.ResolverForFaces;
import org.apache.myfaces.el.unified.resolver.ResolverForJSP;
import org.apache.myfaces.shared_impl.util.ClassUtils;

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
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * DOCUMENT ME!
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @author Thomas Spiegl
 * @author Stan Silvert
 * @version $Revision$ $Date$
 */
public class ApplicationImpl
        extends Application {
    private static final Log log = LogFactory.getLog(ApplicationImpl.class);
    
    //~ Instance fields ----------------------------------------------------------------------------
    
    private Collection           _supportedLocales = Collections.EMPTY_SET;
    private Locale               _defaultLocale;
    private String               _messageBundle;
    
    private ViewHandler          _viewHandler;
    private NavigationHandler    _navigationHandler;
    private VariableResolver     _variableResolver;
    private PropertyResolver     _propertyResolver;
    private ActionListener       _actionListener;
    private String               _defaultRenderKitId;
    private StateManager         _stateManager;
    
    private ServletContext       _servletContext;
    
    private ResolverForFaces     _resolverForFaces;
    private ResolverForJSP       _resolverForJSP;
    
    private ExpressionFactory    _expressionFactory;
    
    private ArrayList<ELContextListener> _elContextListeners;
    
    // components, converters, and validators can be added at runtime--must synchronize
    private final Map _converterIdToClassMap = Collections.synchronizedMap(new HashMap());
    private final Map _converterClassNameToClassMap = Collections.synchronizedMap(new HashMap());
    private final Map _converterClassNameToConfigurationMap = Collections.synchronizedMap(new HashMap());
    private final Map _componentClassMap = Collections.synchronizedMap(new HashMap());
    private final Map _validatorClassMap = Collections.synchronizedMap(new HashMap());
    
    
    //~ Constructors -------------------------------------------------------------------------------
    
    public ApplicationImpl() {
        // set default implementation in constructor
        // pragmatic approach, no syncronizing will be needed in get methods
        _viewHandler = new JspViewHandlerImpl();
        _navigationHandler = new NavigationHandlerImpl();
        _variableResolver = new NullVariableResolver();
        _propertyResolver = new NullPropertyResolver();
        _actionListener = new ActionListenerImpl();
        _defaultRenderKitId = "HTML_BASIC";
        _stateManager = new JspStateManagerImpl();
        _elContextListeners = new ArrayList();
        _resolverForFaces =  new ResolverForFaces();
        _resolverForJSP = null;
        
        if (log.isTraceEnabled()) log.trace("New Application instance created");
    }
    
    //~ Methods ------------------------------------------------------------------------------------
    
    // note: this method is not part of the javax.faces.application.Application interface
    // it must be called by FacesConfigurator or other init mechanism
    public void setServletContext(ServletContext servletContext) {
        
        // this Class.forName will be removed when Tomcat fixes a bug
        // also, we should then be able to remove jasper.jar from the deployment
        try {
            Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        _servletContext = servletContext;
        ResolverForJSP _resolverForJSP = new ResolverForJSP();
        
        log.debug("factory = " + JspFactory.getDefaultFactory());
        JspApplicationContext appCtx = JspFactory.getDefaultFactory().getJspApplicationContext(_servletContext);
        
        appCtx.addELResolver(_resolverForJSP);
        
        _expressionFactory = appCtx.getExpressionFactory();
    }
    

    public void addELResolver(ELResolver resolver) {
        _resolverForFaces.addResolverFromApplicationAddResolver(resolver);
        _resolverForJSP.addResolverFromApplicationAddResolver(resolver);
    }

    public ELResolver getELResolver() {
        return _resolverForFaces;
    }
    
    public ResourceBundle getResourceBundle(FacesContext facesContext, String name)
        throws FacesException, NullPointerException {
        
        checkNull(facesContext, "facesContext");
        checkNull(name, "name");
        
        //TODO: implement the rest of this
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    public UIComponent createComponent(ValueExpression componentExpression,
            FacesContext facesContext,
            String componentType)
            throws FacesException, NullPointerException {
        
        checkNull(componentExpression, "componentExpression");
        checkNull(facesContext, "facesContext");
        checkNull(componentType, "componentType");

        ELContext elContext = facesContext.getELContext();
        
        Object retVal = componentExpression.getValue(elContext);

        UIComponent createdComponent;

        try
        {
            if (retVal instanceof UIComponent)
            {
                createdComponent = (UIComponent)retVal;
            }
            else
            {
                createdComponent = createComponent(componentType);
                componentExpression.setValue(elContext, retVal);
            }
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

        return createdComponent;
    }
    
    public ExpressionFactory getExpressionFactory() {
        return _expressionFactory;
    }
    
    public Object evaluateExpressionGet(FacesContext context, String expression, Class expectedType) throws ELException {
        ELContext elContext = context.getELContext();
        return getExpressionFactory()
               .createValueExpression(elContext, expression, expectedType)
               .getValue(elContext);
    }
    
    public void addELContextListener(ELContextListener listener) {
        
        synchronized (_elContextListeners) {
            _elContextListeners.add(listener);
        }
    }
    
    public void removeELContextListener(ELContextListener listener) {
        synchronized (_elContextListeners) {
            _elContextListeners.remove(listener);
        }
    }
    
    public ELContextListener[] getELContextListeners() {
        // this gets called on every request, so I can't afford to synchronize
        // I just have to trust that toArray() with do the right thing if the
        // list is changing (not likely)
        return _elContextListeners.toArray(new ELContextListener[0]);
    }
    
    public void setActionListener(ActionListener actionListener) {
        checkNull(actionListener, "actionListener");
        
        _actionListener = actionListener;
        if (log.isTraceEnabled()) log.trace("set actionListener = " + actionListener.getClass().getName());
    }
    
    public ActionListener getActionListener() {
        return _actionListener;
    }
    
    public Iterator getComponentTypes() {
        return _componentClassMap.keySet().iterator();
    }
    
    public Iterator getConverterIds() {
        return _converterIdToClassMap.keySet().iterator();
    }
    
    public Iterator getConverterTypes() {
        return _converterClassNameToClassMap.keySet().iterator();
    }
    
    public void setDefaultLocale(Locale locale) {
        checkNull(locale, "locale");
        
        _defaultLocale = locale;
        if (log.isTraceEnabled()) log.trace("set defaultLocale = " + locale.getCountry() + " " + locale.getLanguage());
    }
    
    public Locale getDefaultLocale() {
        return _defaultLocale;
    }
    
    public void setMessageBundle(String messageBundle) {
        checkNull(messageBundle, "messageBundle");
        
        _messageBundle = messageBundle;
        if (log.isTraceEnabled()) log.trace("set MessageBundle = " + messageBundle);
    }
    
    public String getMessageBundle() {
        return _messageBundle;
    }
    
    public void setNavigationHandler(NavigationHandler navigationHandler) {
        checkNull(navigationHandler, "navigationHandler");
        
        _navigationHandler = navigationHandler;
        if (log.isTraceEnabled()) log.trace("set NavigationHandler = " + navigationHandler.getClass().getName());
    }
    
    public NavigationHandler getNavigationHandler() {
        return _navigationHandler;
    }
    
    /**
     * @deprecated
     */
    public void setPropertyResolver(PropertyResolver propertyResolver) {
        checkNull(propertyResolver, "propertyResolver");
        
        _resolverForFaces.addResolverFromLegacyPropertyResolver(propertyResolver);
        
        // TODO: fix FacesConfigurator so this won't happen
        if (_resolverForJSP != null) {
            _resolverForJSP.addResolverFromLegacyPropertyResolver(propertyResolver);
        }
        
        _propertyResolver = new ELResolverToPropertyResolver(getELResolver());
        
        if (log.isTraceEnabled()) log.trace("set PropertyResolver = " + propertyResolver.getClass().getName());
    }
    
    /**
     * @deprecated
     */
    public PropertyResolver getPropertyResolver() {
        return _propertyResolver;
    }
    
    public void setSupportedLocales(Collection locales) {
        checkNull(locales, "locales");
        
        _supportedLocales = locales;
        if (log.isTraceEnabled()) log.trace("set SupportedLocales");
    }
    
    public Iterator getSupportedLocales() {
        return _supportedLocales.iterator();
    }
    
    public Iterator getValidatorIds() {
        return _validatorClassMap.keySet().iterator();
    }
    
    /**
     * @deprecated
     */
    public void setVariableResolver(VariableResolver variableResolver) {
        _resolverForFaces.addResolverFromLegacyVariableResolver(variableResolver);
        
        // TODO: fix FacesConfigurator so this won't happen
        if (_resolverForJSP != null) {
            _resolverForJSP.addResolverFromLegacyVariableResolver(variableResolver);
        }
        
        _variableResolver = new ELResolverToVariableResolver(getELResolver());
    }
    
    /**
     * @deprecated
     */
    public VariableResolver getVariableResolver() {
        return _variableResolver;
    }
    
    public void setViewHandler(ViewHandler viewHandler) {
        checkNull(viewHandler, "viewHandler");
        
        _viewHandler = viewHandler;
        if (log.isTraceEnabled()) log.trace("set ViewHandler = " + viewHandler.getClass().getName());
    }
    
    public ViewHandler getViewHandler() {
        return _viewHandler;
    }
    
    public void addComponent(String componentType, String componentClassName) {
        checkNull(componentType, "componentType");
        checkEmpty(componentType, "componentType");
        checkNull(componentClassName, "componentClassName");
        checkEmpty(componentClassName, "componentClassName");
        
        try {
            _componentClassMap.put(componentType, ClassUtils.simpleClassForName(componentClassName));
            if (log.isTraceEnabled()) log.trace("add Component class = " + componentClassName +
                    " for type = " + componentType);
        } catch (Exception e) {
            log.error("Component class " + componentClassName + " not found", e);
        }
    }
    
    public void addConverter(String converterId, String converterClass) {
        checkNull(converterId, "converterId");
        checkEmpty(converterId, "converterId");
        checkNull(converterClass, "converterClass");
        checkEmpty(converterClass, "converterClass");
        
        try {
            _converterIdToClassMap.put(converterId, ClassUtils.simpleClassForName(converterClass));
            if (log.isTraceEnabled()) log.trace("add Converter id = " + converterId +
                    " converterClass = " + converterClass);
        } catch (Exception e) {
            log.error("Converter class " + converterClass + " not found", e);
        }
    }
    
    public void addConverter(Class targetClass, String converterClass) {
        checkNull(targetClass, "targetClass");
        checkNull(converterClass, "converterClass");
        checkEmpty(converterClass, "converterClass");
        
        try {
            _converterClassNameToClassMap.put(targetClass, converterClass);
            if (log.isTraceEnabled()) log.trace("add Converter for class = " + targetClass +
                    " converterClass = " + converterClass);
        } catch (Exception e) {
            log.error("Converter class " + converterClass + " not found", e);
        }
    }
    
    public void addConverterConfiguration(String converterClassName,
            org.apache.myfaces.config.impl.digester.elements.Converter configuration) {
        checkNull(converterClassName, "converterClassName");
        checkEmpty(converterClassName, "converterClassName");
        checkNull(configuration, "configuration");
        
        _converterClassNameToConfigurationMap.put(converterClassName, configuration);
    }
    
    public void addValidator(String validatorId, String validatorClass) {
        checkNull(validatorId, "validatorId");
        checkEmpty(validatorId, "validatorId");
        checkNull(validatorClass, "validatorClass");
        checkEmpty(validatorClass, "validatorClass");
        
        try {
            _validatorClassMap.put(validatorId, ClassUtils.simpleClassForName(validatorClass));
            if (log.isTraceEnabled()) log.trace("add Validator id = " + validatorId +
                    " class = " + validatorClass);
        } catch (Exception e) {
            log.error("Validator class " + validatorClass + " not found", e);
        }
    }
    
    public UIComponent createComponent(String componentType)
    throws FacesException {
        checkNull(componentType, "componentType");
        checkEmpty(componentType, "componentType");
        
        Class componentClass;
        synchronized (_componentClassMap) {
            componentClass = (Class) _componentClassMap.get(componentType);
        }
        if (componentClass == null) {
            log.error("Undefined component type " + componentType);
            throw new FacesException("Undefined component type " + componentType);
        }
        
        try {
            return (UIComponent) componentClass.newInstance();
        } catch (Exception e) {
            log.error("Could not instantiate component componentType = " + componentType, e);
            throw new FacesException("Could not instantiate component componentType = " + componentType, e);
        }
    }
    
    /**
     * @deprecated Use createComponent(ValueExpression, FacesContext, String) instead.
     */
    public UIComponent createComponent(ValueBinding valueBinding,
                                       FacesContext facesContext,
                                       String componentType)
            throws FacesException {
        
        checkNull(valueBinding, "valueBinding");
        checkNull(facesContext, "facesContext");
        checkNull(componentType, "componentType");
        checkEmpty(componentType, "componentType");
        
        ValueExpression valExpression = new ValueBindingToValueExpression(valueBinding);
        
        return createComponent(valExpression, facesContext, componentType);
    }
    
    public Converter createConverter(String converterId) {
        checkNull(converterId, "converterId");
        checkEmpty(converterId, "converterId");
        
        Class converterClass = (Class) _converterIdToClassMap.get(converterId);
        
        try {
            Converter converter= (Converter) converterClass.newInstance();
            
            setConverterProperties(converterClass, converter);
            
            return converter;
        } catch (Exception e) {
            log.error("Could not instantiate converter " + converterClass, e);
            throw new FacesException("Could not instantiate converter: " + converterClass, e);
        }
    }
    
    
    public Converter createConverter(Class targetClass) {
        checkNull(targetClass, "targetClass");
        
        return internalCreateConverter(targetClass);
    }
    
    
    private Converter internalCreateConverter(Class targetClass) {
        // Locate a Converter registered for the target class itself.
        String converterClassName = (String)_converterClassNameToClassMap.get(targetClass);
        
        //Locate a Converter registered for interfaces that are
        // implemented by the target class (directly or indirectly).
        if (converterClassName == null) {
            Class interfaces[] = targetClass.getInterfaces();
            if (interfaces != null) {
                for (int i = 0, len = interfaces.length; i < len; i++) {
                    // search all superinterfaces for a matching converter, create it
                    Converter converter = internalCreateConverter(interfaces[i]);
                    if (converter != null) {
                        return converter;
                    }
                }
            }
        }
        
        if (converterClassName != null) {
            try {
                Class converterClass = ClassUtils.simpleClassForName(converterClassName);
                
                Converter converter = null;
                try {
                    // look for a constructor that takes a single Class object
                    // See JSF 1.2 javadoc for Converter
                    Constructor constructor = converterClass.getConstructor(new Class[]{Class.class});
                    converter = (Converter)constructor.newInstance(new Object[]{targetClass});
                } catch (Exception e) {
                    // if there is no matching constructor use no-arg constructor
                    converter = (Converter) converterClass.newInstance();
                }
                
                setConverterProperties(converterClass, converter);
                
                return converter;
            } catch (Exception e) {
                log.error("Could not instantiate converter " + converterClassName, e);
                throw new FacesException("Could not instantiate converter: " + converterClassName, e);
            }
        }
        
        //   locate converter for primitive types
        if (targetClass == Long.TYPE) {
            return internalCreateConverter(Long.class);
        } else if (targetClass == Boolean.TYPE) {
            return internalCreateConverter(Boolean.class);
        } else if (targetClass == Double.TYPE) {
            return internalCreateConverter(Double.class);
        } else if (targetClass == Byte.TYPE) {
            return internalCreateConverter(Byte.class);
        } else if (targetClass == Short.TYPE) {
            return internalCreateConverter(Short.class);
        } else if (targetClass == Integer.TYPE) {
            return internalCreateConverter(Integer.class);
        } else if (targetClass == Float.TYPE) {
            return internalCreateConverter(Float.class);
        } else if (targetClass == Character.TYPE) {
            return internalCreateConverter(Character.class);
        }
        
        
        //Locate a Converter registered for the superclass (if any) of the target class,
        // recursively working up the inheritance hierarchy.
        Class superClazz = targetClass.getSuperclass();
        if (superClazz != null) {
            return internalCreateConverter(superClazz);
        } else {
            return null;
        }
        
    }
    
    private void setConverterProperties(Class converterClass, Converter converter) {
        org.apache.myfaces.config.impl.digester.elements.Converter converterConfig =
                (org.apache.myfaces.config.impl.digester.elements.Converter)
                _converterClassNameToConfigurationMap.get(converterClass.getName());
        
        if(converterConfig != null) {
            
            Iterator it = converterConfig.getProperties();
            
            while (it.hasNext()) {
                Property property = (Property) it.next();
                
                try {
                    BeanUtils.setProperty(converter,property.getPropertyName(),property.getDefaultValue());
                } catch(Throwable th) {
                    log.error("Initializing converter : "+converterClass.getName()+" with property : "+
                            property.getPropertyName()+" and value : "+property.getDefaultValue()+" failed.");
                }
            }
        }
    }
    
    
    // Note: this method used to be synchronized in the JSF 1.1 version.  Why?
    /**
     * @deprecated
     */
    public MethodBinding createMethodBinding(String reference, Class[] params)
    throws ReferenceSyntaxException {
        checkNull(reference, "reference");
        checkEmpty(reference, "reference");
        
        if (params == null) params = new Class[0];
        
        MethodExpression methodExpression;
        
        try {
            methodExpression = getExpressionFactory()
            .createMethodExpression(threadELContext(), reference, Object.class, params);
        } catch (ELException e) {
            throw new ReferenceSyntaxException(e);
        }
        
        return new MethodExpressionToMethodBinding(methodExpression);
    }
    
    public Validator createValidator(String validatorId) throws FacesException {
        checkNull(validatorId, "validatorId");
        checkEmpty(validatorId, "validatorId");
        
        Class validatorClass = (Class) _validatorClassMap.get(validatorId);
        if (validatorClass == null) {
            String message = "Unknown validator id '" + validatorId + "'.";
            log.error(message);
            throw new FacesException(message);
        }
        
        try {
            return (Validator) validatorClass.newInstance();
        } catch (Exception e) {
            log.error("Could not instantiate validator " + validatorClass, e);
            throw new FacesException("Could not instantiate validator: " + validatorClass, e);
        }
    }
    
    /**
     * @deprecated
     */
    public ValueBinding createValueBinding(String reference) throws ReferenceSyntaxException {
        checkNull(reference, "reference");
        checkEmpty(reference, "reference");
        
        ValueExpression valueExpression;
        
        try {
            valueExpression = getExpressionFactory()
            .createValueExpression(threadELContext(), reference, Object.class);
        } catch (ELException e) {
            throw new ReferenceSyntaxException(e);
        }
        
        return new ValueExpressionToValueBinding(valueExpression);
    }
    
    // gets the elContext from the current FacesContext()
    private ELContext threadELContext() {
        return FacesContext.getCurrentInstance().getELContext();
    }
    
    
    public String getDefaultRenderKitId() {
        return _defaultRenderKitId;
    }
    
    public void setDefaultRenderKitId(String defaultRenderKitId) {
        _defaultRenderKitId = defaultRenderKitId;
    }
    
    public StateManager getStateManager() {
        return _stateManager;
    }
    
    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
    
    private void checkNull(Object param, String paramName) {
        if (param == null) {
            throw new NullPointerException(paramName + " can not be null.");
        }
    }
    
    private void checkEmpty(String param, String paramName) {
        if (param.length() == 0) {
            throw new NullPointerException("String " + paramName + " can not be empty.");
        }
    }
}
