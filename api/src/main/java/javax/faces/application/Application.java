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
package javax.faces.application;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.el.ELContextListener;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.event.SystemEventListenerHolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds webapp-wide resources for a JSF application. There is a single one of these for a web application, accessable
 * via
 * 
 * <pre>
 * FacesContext.getCurrentInstance().getApplication()
 * </pre>
 * 
 * In particular, this provides a factory for UIComponent objects. It also provides convenience methods for creating
 * ValueBinding objects.
 * 
 * See Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Stan Silvert
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public abstract class Application
{
    // The following concrete methods were added for JSF 1.2. They supply default
    // implementations that throw UnsupportedOperationException.
    // This allows old Application implementations to still work.
    public void addELResolver(ELResolver resolver)
    {
        throw new UnsupportedOperationException();
    }

    public ELResolver getELResolver()
    {
        throw new UnsupportedOperationException();
    }

    public ResourceBundle getResourceBundle(FacesContext ctx, String name) throws FacesException, NullPointerException
    {
        throw new UnsupportedOperationException();
    }
    
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType)
    {
        // TODO: JSF 2.0 #1
        // VALIDATE: Shouldn't this be abstract or throw UnsupportedOperationException?
        return null;
    }

    public UIComponent createComponent(ValueExpression componentExpression, FacesContext facesContext,
                                       String componentType) throws FacesException, NullPointerException
    {
        throw new UnsupportedOperationException();
    }
    
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context,
                                       String componentType, String rendererType)
    {
        // TODO: JSF 2.0 #2
        // VALIDATE: Shouldn't this be abstract or throw UnsupportedOperationException?
        return null;
    }

    public ExpressionFactory getExpressionFactory()
    {
        throw new UnsupportedOperationException();
    }
    
    public ResourceHandler getResourceHandler()
    {
        throw new UnsupportedOperationException();
    }

    public void addELContextListener(ELContextListener listener)
    {
        throw new UnsupportedOperationException();
    }
    
    public void publishEvent(Class<? extends SystemEvent> systemEventClass, Object source)
    {
        throw new UnsupportedOperationException();
    }

    public void removeELContextListener(ELContextListener listener)
    {
        throw new UnsupportedOperationException();
    }

    public ELContextListener[] getELContextListeners()
    {
        throw new UnsupportedOperationException();
    }

    public Object evaluateExpressionGet(FacesContext context, String expression, Class expectedType) throws ELException
    {
        throw new UnsupportedOperationException();
    }

    // -------- abstract methods -------------------
    public abstract javax.faces.event.ActionListener getActionListener();

    public abstract void setActionListener(javax.faces.event.ActionListener listener);

    public abstract Locale getDefaultLocale();

    public abstract void setDefaultLocale(Locale locale);

    public abstract String getDefaultRenderKitId();

    public abstract void setDefaultRenderKitId(String renderKitId);

    public abstract String getMessageBundle();

    public abstract void setMessageBundle(String bundle);

    /**
     * Return the NavigationHandler object which is responsible for mapping from a logical (viewid, fromAction, outcome)
     * to the URL of a view to be rendered.
     */
    public abstract javax.faces.application.NavigationHandler getNavigationHandler();

    public abstract void setNavigationHandler(javax.faces.application.NavigationHandler handler);

    public ProjectStage getProjectStage()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the object used by the VariableResolver to read and write named properties on java beans, Arrays, Lists and
     * Maps. This object is used by the ValueBinding implementation, and during the process of configuring
     * "managed bean" properties.
     * 
     * @deprecated
     */
    public abstract javax.faces.el.PropertyResolver getPropertyResolver();

    /**
     * @deprecated
     */
    public abstract void setPropertyResolver(javax.faces.el.PropertyResolver resolver);

    /**
     * Get the object used to resolve expressions of form "#{...}".
     * 
     * @deprecated
     */
    public abstract javax.faces.el.VariableResolver getVariableResolver();

    /**
     * @deprecated
     */
    public abstract void setVariableResolver(javax.faces.el.VariableResolver resolver);

    public abstract javax.faces.application.ViewHandler getViewHandler();

    public abstract void setViewHandler(javax.faces.application.ViewHandler handler);
    
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, 
                                 Class sourceClass, SystemEventListener listener)
    {
        throw new UnsupportedOperationException();
    }
    
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener)
    {
        subscribeToEvent(systemEventClass, null, listener);
    }
    
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, 
                                     Class sourceClass, SystemEventListener listener)
    {
        throw new UnsupportedOperationException();
    }
    
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener)
    {
        throw new UnsupportedOperationException();
    }

    public abstract javax.faces.application.StateManager getStateManager();
    
    public void setResourceHandler(ResourceHandler resourceHandler)
    {
        throw new UnsupportedOperationException();
    }

    public abstract void setStateManager(javax.faces.application.StateManager manager);

    /**
     * Define a new mapping from a logical "component type" to an actual java class name. This controls what type is
     * created when method createComponent of this class is called.
     * <p>
     * Param componentClass must be the fully-qualified class name of some class extending the UIComponent class. The
     * class must have a default constructor, as instances of it will be created using Class.newInstance.
     * <p>
     * It is permitted to override a previously defined mapping, ie to call this method multiple times with the same
     * componentType string. The createComponent method will simply use the last defined mapping.
     */
    public abstract void addComponent(String componentType, String componentClass);

    /**
     * Create a new UIComponent subclass, using the mappings defined by previous calls to the addComponent method of
     * this class.
     * <p>
     * 
     * @throws FacesException
     *             if there is no mapping defined for the specified componentType, or if an instance of the specified
     *             type could not be created for any reason.
     */
    public abstract javax.faces.component.UIComponent createComponent(String componentType) throws FacesException;

    /**
     * Create an object which has an associating "binding" expression tying the component to a user property.
     * <p>
     * First the specified value-binding is evaluated; if it returns a non-null value then the component
     * "already exists" and so the resulting value is simply returned.
     * <p>
     * Otherwise a new UIComponent instance is created using the specified componentType, and the new object stored via
     * the provided value-binding before being returned.
     * 
     * @deprecated
     */
    public abstract javax.faces.component.UIComponent createComponent(javax.faces.el.ValueBinding componentBinding,
                                                                      javax.faces.context.FacesContext context,
                                                                      String componentType) throws FacesException;

    public abstract Iterator<String> getComponentTypes();

    public abstract void addConverter(String converterId, String converterClass);

    public abstract void addConverter(Class targetClass, String converterClass);

    public abstract javax.faces.convert.Converter createConverter(String converterId);

    public abstract javax.faces.convert.Converter createConverter(Class targetClass);

    public abstract Iterator<String> getConverterIds();

    public abstract Iterator<Class> getConverterTypes();

    /**
     * Create an object which can be used to invoke an arbitrary method via an EL expression at a later time. This is
     * similar to createValueBinding except that it can invoke an arbitrary method (with parameters) rather than just
     * get/set a javabean property.
     * <p>
     * This is used to invoke ActionListener method, and ValueChangeListener methods.
     * 
     * @deprecated
     */
    public abstract javax.faces.el.MethodBinding createMethodBinding(String ref, Class[] params)
            throws ReferenceSyntaxException;

    public abstract Iterator<Locale> getSupportedLocales();

    public abstract void setSupportedLocales(Collection<Locale> locales);

    public abstract void addValidator(String validatorId, String validatorClass);

    public abstract javax.faces.validator.Validator createValidator(String validatorId) throws FacesException;

    public abstract Iterator<String> getValidatorIds();

    /**
     * Create an object which can be used to invoke an arbitrary method via an EL expression at a later time. This is
     * similar to createValueBinding except that it can invoke an arbitrary method (with parameters) rather than just
     * get/set a javabean property.
     * <p>
     * This is used to invoke ActionListener method, and ValueChangeListener methods.
     * 
     * @deprecated
     */
    public abstract javax.faces.el.ValueBinding createValueBinding(String ref) throws ReferenceSyntaxException;
}
