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

import javax.el.ELContextListener;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MockApplication extends org.apache.shale.test.mock.MockApplication
{

    private Collection<ELContextListener> _elContextListener = new ArrayList<ELContextListener>();
    private Collection<ELResolver> _elResolvers = new ArrayList<ELResolver>();
    private ELResolver _elResolver;
    private ExpressionFactory _expressionFactory;

    @Override
    public void addELContextListener(ELContextListener listener)
    {
        _elContextListener.add(listener);
    }

    @Override
    public void addELResolver(ELResolver resolver)
    {
        _elResolvers.add(resolver);
    }
    
    /**
     * @return the elResolvers
     */
    public Collection<ELResolver> getElResolvers()
    {
        return _elResolvers;
    }

    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext facesContext,
            String componentType) throws FacesException, NullPointerException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evaluateExpressionGet(FacesContext context, String expression, Class expectedType) throws ELException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ELContextListener[] getELContextListeners()
    {
        return _elContextListener.toArray(new ELContextListener[_elContextListener.size()]);
    }

    /**
     * @param elResolver
     *            the elResolver to set
     */
    public void setElResolver(ELResolver elResolver)
    {
        _elResolver = elResolver;
    }

    @Override
    public ELResolver getELResolver()
    {
        return _elResolver;
    }

    @Override
    public ExpressionFactory getExpressionFactory()
    {
        return _expressionFactory;
    }

    /**
     * @param expressionFactory
     *            the expressionFactory to set
     */
    public void setExpressionFactory(ExpressionFactory expressionFactory)
    {
        _expressionFactory = expressionFactory;
    }

    @Override
    public ResourceBundle getResourceBundle(FacesContext ctx, String name) throws FacesException, NullPointerException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeELContextListener(ELContextListener listener)
    {
        _elContextListener.remove(listener);
    }
}
