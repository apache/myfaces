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

package org.apache.myfaces.test.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.event.ActionListener;
import javax.faces.render.RenderKitFactory;
import javax.faces.validator.Validator;

/**
 * <p>Mock implementation of <code>Application</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public abstract class MockApplication10 extends Application
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockApplication10()
    {

        setActionListener(new MockActionListener());
        components = new HashMap();
        converters = new HashMap();
        converters1 = new HashMap();
        setDefaultLocale(Locale.getDefault());
        setDefaultRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        setNavigationHandler(new MockNavigationHandler());
        setStateManager(new MockStateManager());
        setSupportedLocales(new ArrayList());
        validators = new HashMap();
        setViewHandler(new MockViewHandler());

        // Register the standard by-id converters
        addConverter("javax.faces.BigDecimal",
                "javax.faces.convert.BigDecimalConverter");
        addConverter("javax.faces.BigInteger",
                "javax.faces.convert.BigIntegerConverter");
        addConverter("javax.faces.Boolean",
                "javax.faces.convert.BooleanConverter");
        addConverter("javax.faces.Byte", "javax.faces.convert.ByteConverter");
        addConverter("javax.faces.Character",
                "javax.faces.convert.CharacterConverter");
        addConverter("javax.faces.DateTime",
                "javax.faces.convert.DateTimeConverter");
        addConverter("javax.faces.Double",
                "javax.faces.convert.DoubleConverter");
        addConverter("javax.faces.Float", "javax.faces.convert.FloatConverter");
        addConverter("javax.faces.Integer",
                "javax.faces.Convert.IntegerConverter");
        addConverter("javax.faces.Long", "javax.faces.convert.LongConverter");
        addConverter("javax.faces.Number",
                "javax.faces.convert.NumberConverter");
        addConverter("javax.faces.Short", "javax.faces.convert.ShortConverter");

        // Register the standard by-type converters
        addConverter(Boolean.class, "javax.faces.convert.BooleanConverter");
        addConverter(Boolean.TYPE, "javax.faces.convert.BooleanConverter");
        addConverter(Byte.class, "javax.faces.convert.ByteConverter");
        addConverter(Byte.TYPE, "javax.faces.convert.ByteConverter");
        addConverter(Character.class, "javax.faces.convert.CharacterConverter");
        addConverter(Character.TYPE, "javax.faces.convert.CharacterConverter");
        addConverter(Double.class, "javax.faces.convert.DoubleConverter");
        addConverter(Double.TYPE, "javax.faces.convert.DoubleConverter");
        addConverter(Float.class, "javax.faces.convert.FloatConverter");
        addConverter(Float.TYPE, "javax.faces.convert.FloatConverter");
        addConverter(Integer.class, "javax.faces.convert.IntegerConverter");
        addConverter(Integer.TYPE, "javax.faces.convert.IntegerConverter");
        addConverter(Long.class, "javax.faces.convert.LongConverter");
        addConverter(Long.TYPE, "javax.faces.convert.LongConverter");
        addConverter(Short.class, "javax.faces.convert.ShortConverter");
        addConverter(Short.TYPE, "javax.faces.convert.ShortConverter");

    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    private ActionListener actionListener = null;
    private Map components = null;
    private Map converters = null; // By id
    private Map converters1 = null; // By type
    private Locale defaultLocale = null;
    private String defaultRenderKitId = null;
    private String messageBundle = null;
    private NavigationHandler navigationHandler = null;
    private StateManager stateManager = null;
    private Collection supportedLocales = null;
    private Map validators = null;
    private ViewHandler viewHandler = null;

    // ----------------------------------------------------- Application Methods

    /** {@inheritDoc} */
    public ActionListener getActionListener()
    {

        return this.actionListener;

    }

    /** {@inheritDoc} */
    public void setActionListener(ActionListener actionListener)
    {
        this.actionListener = actionListener;
    }

    /** {@inheritDoc} */
    public Locale getDefaultLocale()
    {

        return this.defaultLocale;

    }

    /** {@inheritDoc} */
    public void setDefaultLocale(Locale defaultLocale)
    {

        this.defaultLocale = defaultLocale;

    }

    /** {@inheritDoc} */
    public String getDefaultRenderKitId()
    {

        return this.defaultRenderKitId;

    }

    /** {@inheritDoc} */
    public void setDefaultRenderKitId(String defaultRenderKitId)
    {

        this.defaultRenderKitId = defaultRenderKitId;

    }

    /** {@inheritDoc} */
    public String getMessageBundle()
    {

        return this.messageBundle;

    }

    /** {@inheritDoc} */
    public void setMessageBundle(String messageBundle)
    {

        this.messageBundle = messageBundle;

    }

    /** {@inheritDoc} */
    public NavigationHandler getNavigationHandler()
    {

        return this.navigationHandler;

    }

    /** {@inheritDoc} */
    public void setNavigationHandler(NavigationHandler navigationHandler)
    {

        this.navigationHandler = navigationHandler;

    }

    /** {@inheritDoc} */
    public StateManager getStateManager()
    {

        return this.stateManager;

    }

    /** {@inheritDoc} */
    public void setStateManager(StateManager stateManager)
    {

        this.stateManager = stateManager;

    }

    /** {@inheritDoc} */
    public Iterator getSupportedLocales()
    {

        return this.supportedLocales.iterator();

    }

    /** {@inheritDoc} */
    public void setSupportedLocales(Collection supportedLocales)
    {

        this.supportedLocales = supportedLocales;

    }

    /** {@inheritDoc} */
    public ViewHandler getViewHandler()
    {

        return this.viewHandler;

    }

    /** {@inheritDoc} */
    public void setViewHandler(ViewHandler viewHandler)
    {

        this.viewHandler = viewHandler;

    }

    /** {@inheritDoc} */
    public void addComponent(String componentType, String componentClass)
    {

        components.put(componentType, componentClass);

    }

    /** {@inheritDoc} */
    public UIComponent createComponent(String componentType)
    {

        if (componentType == null)
        {
            throw new NullPointerException("Requested component type is null");
        }
        String componentClass = (String) components.get(componentType);
        if (componentClass == null)
        {
            throw new FacesException(
                    "No component class registered for component type '"
                            + componentType + '\'');
        }
        try
        {
            Class clazz = Class.forName(componentClass);
            return ((UIComponent) clazz.newInstance());
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

    }


    /** {@inheritDoc} */
    public Iterator getComponentTypes()
    {

        return (components.keySet().iterator());

    }

    /** {@inheritDoc} */
    public void addConverter(String converterId, String converterClass)
    {

        converters.put(converterId, converterClass);

    }

    /** {@inheritDoc} */
    public void addConverter(Class targetClass, String converterClass)
    {

        converters1.put(targetClass, converterClass);

    }

    /** {@inheritDoc} */
    public Converter createConverter(String converterId)
    {

        String converterClass = (String) converters.get(converterId);
        if (converterClass == null)
        {
            return null;
        }
        try
        {
            Class clazz = Class.forName(converterClass);
            return ((Converter) clazz.newInstance());
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

    }

    /** {@inheritDoc} */
    public Converter createConverter(Class targetClass)
    {

        String converterClass = (String) converters1.get(targetClass);
        if (converterClass == null)
        {
            return null;
        }
        try
        {
            Class clazz = Class.forName(converterClass);
            return ((Converter) clazz.newInstance());
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

    }

    /** {@inheritDoc} */
    public Iterator getConverterIds()
    {

        return (converters.keySet().iterator());

    }

    /** {@inheritDoc} */
    public Iterator getConverterTypes()
    {

        return (converters1.keySet().iterator());

    }


    /** {@inheritDoc} */
    public void addValidator(String validatorId, String validatorClass)
    {

        validators.put(validatorId, validatorClass);

    }

    /** {@inheritDoc} */
    public Validator createValidator(String validatorId)
    {

        String validatorClass = (String) validators.get(validatorId);
        try
        {
            Class clazz = Class.forName(validatorClass);
            return ((Validator) clazz.newInstance());
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

    }

    /** {@inheritDoc} */
    public Iterator getValidatorIds()
    {
        return (validators.keySet().iterator());
    }

}
