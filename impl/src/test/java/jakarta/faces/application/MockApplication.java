/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package jakarta.faces.application;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.ActionListener;
import jakarta.faces.validator.Validator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

/**
 * Mock version of {@link Application}
 */
public class MockApplication extends Application
{
    @Override
    public void addComponent(String componentType, String componentClass)
    {
    }

    @Override
    public void addConverter(Class<?> targetClass, String converterClass)
    {
    }

    @Override
    public void addConverter(String converterId, String converterClass)
    {
    }

    @Override
    public void addValidator(String validatorId, String validatorClass)
    {
    }

    @Override
    public UIComponent createComponent(String componentType) throws FacesException
    {
        return null;
    }

    @Override
    public <T> Converter<T> createConverter(Class<T> targetClass)
    {
        return null;
    }

    @Override
    public Converter createConverter(String converterId)
    {
        return null;
    }

    @Override
    public Validator createValidator(String validatorId) throws FacesException
    {
        return null;
    }

    @Override
    public ActionListener getActionListener()
    {
        return null;
    }

    @Override
    public Iterator<String> getComponentTypes()
    {
        return null;
    }

    @Override
    public Iterator<String> getConverterIds()
    {
        return null;
    }

    @Override
    public Iterator<Class<?>> getConverterTypes()
    {
        return null;
    }

    @Override
    public Locale getDefaultLocale()
    {
        return null;
    }

    @Override
    public String getDefaultRenderKitId()
    {
        return null;
    }

    @Override
    public String getMessageBundle()
    {
        return null;
    }

    @Override
    public NavigationHandler getNavigationHandler()
    {
        return null;
    }

    @Override
    public StateManager getStateManager()
    {
        return null;
    }

    @Override
    public Iterator<Locale> getSupportedLocales()
    {
        return null;
    }

    @Override
    public Iterator<String> getValidatorIds()
    {
        return null;
    }

    @Override
    public ViewHandler getViewHandler()
    {
        return null;
    }

    @Override
    public void setActionListener(ActionListener listener)
    {
    }

    @Override
    public void setDefaultLocale(Locale locale)
    {
    }

    @Override
    public void setDefaultRenderKitId(String renderKitId)
    {
    }

    @Override
    public void setMessageBundle(String bundle)
    {
    }

    @Override
    public void setNavigationHandler(NavigationHandler handler)
    {
    }

    @Override
    public void setStateManager(StateManager manager)
    {
    }

    @Override
    public void setSupportedLocales(Collection<Locale> locales)
    {
    }

    @Override
    public void setViewHandler(ViewHandler handler)
    {
    }
}
