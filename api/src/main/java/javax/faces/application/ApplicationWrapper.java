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

import javax.faces.FacesException;
import javax.faces.FacesWrapper;
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

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-12-02 18:14:34 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public abstract class ApplicationWrapper extends Application implements FacesWrapper<Application>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void addComponent(String componentType, String componentClass)
    {
        getWrapped().addComponent(componentType, componentClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConverter(Class targetClass, String converterClass)
    {
        getWrapped().addConverter(targetClass, converterClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConverter(String converterId, String converterClass)
    {
        getWrapped().addConverter(converterId, converterClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addValidator(String validatorId, String validatorClass)
    {
        getWrapped().addValidator(validatorId, validatorClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UIComponent createComponent(String componentType) throws FacesException
    {
        return getWrapped().createComponent(componentType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UIComponent createComponent(ValueBinding componentBinding, FacesContext context, String componentType)
            throws FacesException
    {
        return getWrapped().createComponent(componentBinding, context, componentType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Converter createConverter(Class targetClass)
    {
        return getWrapped().createConverter(targetClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Converter createConverter(String converterId)
    {
        return getWrapped().createConverter(converterId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodBinding createMethodBinding(String ref, Class[] params) throws ReferenceSyntaxException
    {
        return getWrapped().createMethodBinding(ref, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Validator createValidator(String validatorId) throws FacesException
    {
        return getWrapped().createValidator(validatorId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueBinding createValueBinding(String ref) throws ReferenceSyntaxException
    {
        return getWrapped().createValueBinding(ref);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionListener getActionListener()
    {
        return getWrapped().getActionListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getComponentTypes()
    {
        return getWrapped().getComponentTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getConverterIds()
    {
        return getWrapped().getConverterIds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Class> getConverterTypes()
    {
        return getWrapped().getConverterTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getDefaultLocale()
    {
        return getWrapped().getDefaultLocale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultRenderKitId()
    {
        return getWrapped().getDefaultRenderKitId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessageBundle()
    {
        return getWrapped().getMessageBundle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigationHandler getNavigationHandler()
    {
        return getWrapped().getNavigationHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyResolver getPropertyResolver()
    {
        return getWrapped().getPropertyResolver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StateManager getStateManager()
    {
        return getWrapped().getStateManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Locale> getSupportedLocales()
    {
        return getWrapped().getSupportedLocales();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getValidatorIds()
    {
        return getWrapped().getValidatorIds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableResolver getVariableResolver()
    {
        return getWrapped().getVariableResolver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewHandler getViewHandler()
    {
        return getWrapped().getViewHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActionListener(ActionListener listener)
    {
        getWrapped().setActionListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultLocale(Locale locale)
    {
        getWrapped().setDefaultLocale(locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultRenderKitId(String renderKitId)
    {
        getWrapped().setDefaultRenderKitId(renderKitId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageBundle(String bundle)
    {
        getWrapped().setMessageBundle(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNavigationHandler(NavigationHandler handler)
    {
        getWrapped().setNavigationHandler(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPropertyResolver(PropertyResolver resolver)
    {
        getWrapped().setPropertyResolver(resolver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStateManager(StateManager manager)
    {
        getWrapped().setStateManager(manager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSupportedLocales(Collection<Locale> locales)
    {
        getWrapped().setSupportedLocales(locales);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVariableResolver(VariableResolver resolver)
    {
        getWrapped().setVariableResolver(resolver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewHandler(ViewHandler handler)
    {
        getWrapped().setViewHandler(handler);
    }

    public abstract Application getWrapped();
}
