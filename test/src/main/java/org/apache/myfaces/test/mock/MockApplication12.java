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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContextListener;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.test.config.ResourceBundleVarNames;
import org.apache.myfaces.test.el.FacesImplicitObjectELResolver;
import org.apache.myfaces.test.el.FacesResourceBundleELResolver;
import org.apache.myfaces.test.el.FacesScopedAttributeELResolver;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.apache.myfaces.test.el.ReservedWordsELResolver;

/**
 * <p>Mock implementation of <code>Application</code> that includes the semantics
 * added by JavaServer Faces 1.2.</p>
 *
 * $Id$
 *
 * @since 1.0.0
 */
public abstract class MockApplication12 extends MockApplication10
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockApplication12()
    {

        super();

        // Configure our expression factory and EL resolvers
        expressionFactory = new MockExpressionFactory();

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>A list of resource bundles configured for this application.</p>
     */
    private Map bundles = new HashMap();

    /**
     * <p>The set of configured ELContextListener instances.</p>
     */
    private List elContextListeners = new ArrayList();

    /**
     * <p>Expression factory for this instance.</p>
     */
    private ExpressionFactory expressionFactory = null;

    /**
     * <p>The configured composite resolver to be returned by <code>getELResolver()</code>.
     * This value is lazily instantiated.</p>
     */
    private ELResolver resolver = null;

    /**
     * <p>The set of ELResolver instances configured on this instance.</p>
     */
    private List resolvers = new ArrayList();

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Add the specified resource bundle to those associated with
     * this application.</p>
     *
     * @param name Name under which to add this resource bundle
     * @param bundle ResourceBundle to add
     */
    public void addResourceBundle(String name, ResourceBundle bundle)
    {
        bundles.put(name, bundle);
    }

    /**
     * <p>Return a <code>Map</code> of the resource bundles configured
     * for this application, keyed by name.</p>
     */
    public Map getResourceBundles()
    {
        return bundles;
    }

    /**
     * Set the current ExpressionFactory to be returned by this mock object
     * 
     * @param expressionFactory
     */
    public void setExpressionFactory(ExpressionFactory expressionFactory)
    {
        this.expressionFactory = expressionFactory;
    }
    
    // ----------------------------------------------------- Application Methods

    /** {@inheritDoc} */
    public void addELContextListener(ELContextListener listener)
    {

        elContextListeners.add(listener);

    }

    /** {@inheritDoc} */
    public void addELResolver(ELResolver resolver)
    {

        // Simulate the restriction that you cannot add resolvers after
        // the first request has been processed.
        if (this.resolver != null)
        {
            throw new IllegalStateException("Cannot add resolvers now");
        }

        resolvers.add(resolver);

    }

    /** {@inheritDoc} */
    public UIComponent createComponent(ValueExpression expression,
            FacesContext context, String componentType)
    {

        UIComponent component = null;
        try
        {
            component = (UIComponent) expression.getValue(context
                    .getELContext());
            if (component == null)
            {
                component = createComponent(componentType);
                expression.setValue(context.getELContext(), component);
            }

        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
        return component;

    }

    /** {@inheritDoc} */
    public Object evaluateExpressionGet(FacesContext context,
            String expression, Class expectedType) throws ELException
    {

        ValueExpression ve = getExpressionFactory().createValueExpression(
                context.getELContext(), expression, expectedType);
        return ve.getValue(context.getELContext());

    }

    /** {@inheritDoc} */
    public ELContextListener[] getELContextListeners()
    {

        return (ELContextListener[]) elContextListeners
                .toArray(new ELContextListener[elContextListeners.size()]);

    }

    /** {@inheritDoc} */
    public ELResolver getELResolver()
    {

        if (resolver == null)
        {

            // Configure a default ELResolver per Section 5.6.2 of JSF 1.2
            CompositeELResolver composite = new CompositeELResolver();

            composite.add(new FacesImplicitObjectELResolver());

            CompositeELResolver nested = new CompositeELResolver();
            Iterator items = resolvers.iterator();
            while (items.hasNext())
            {
                nested.add((ELResolver) items.next());
            }
            composite.add(nested);

            // composite.add(new faces.ManagedBeanELResolver()); // FIXME
            composite.add(new ResourceBundleELResolver());
            composite.add(new FacesResourceBundleELResolver());
            composite.add(new MapELResolver());
            composite.add(new ListELResolver());
            composite.add(new ArrayELResolver());
            composite.add(new BeanELResolver());
            composite.add(new FacesScopedAttributeELResolver());
            composite.add(new ReservedWordsELResolver());

            // Make the resolver we have configured the application wide one
            resolver = composite;

        }
        return resolver;

    }

    /** {@inheritDoc} */
    public ExpressionFactory getExpressionFactory()
    {

        return this.expressionFactory;

    }

    /** {@inheritDoc} */
    public ResourceBundle getResourceBundle(FacesContext context, String name)
    {

        if ((context == null) || (name == null))
        {
            throw new NullPointerException();
        }
        Locale locale = null;
        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot != null)
        {
            locale = viewRoot.getLocale();
        }
        if (locale == null)
        {
            locale = Locale.getDefault();
        }
        try
        {
            return ResourceBundle.getBundle(name, locale);
        }
        catch (MissingResourceException e)
        {

            String newName = ResourceBundleVarNames.getVarName(name);
            if (newName == null)
            {
                return null;
            }

            try
            {
                return ResourceBundle.getBundle(newName, locale);
            }
            catch (MissingResourceException exc)
            {
                return null;
            }

        }
    }

    /** {@inheritDoc} */
    public void removeELContextListener(ELContextListener listener)
    {

        elContextListeners.remove(listener);

    }

}
