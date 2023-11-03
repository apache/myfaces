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


import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchKeywordResolver;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.PropertyResolver;
import jakarta.faces.el.ReferenceSyntaxException;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.el.VariableResolver;

/**
 * <p>Mock implementation of <code>Application</code> that includes the semantics
 * added by JavaServer Faces 3.0.</p>
 * 
 * @author Leonardo Uribe
 * @since 1.0.0
 *
 */
public class MockApplication extends MockApplication22
{
    private VariableResolver variableResolver;
    private PropertyResolver propertyResolver;
    private SearchExpressionHandler searchExpressionHandler;
    private SearchKeywordResolver searchKeywordResolver;

    public MockApplication()
    {
        setVariableResolver(new MockVariableResolver());
        setPropertyResolver(new MockPropertyResolver());
    }

    @Override
    public SearchExpressionHandler getSearchExpressionHandler()
    {
        return searchExpressionHandler;
    }

    @Override
    public void setSearchExpressionHandler(SearchExpressionHandler searchExpressionHandler)
    {
        this.searchExpressionHandler = searchExpressionHandler;
    }

    @Override
    public SearchKeywordResolver getSearchKeywordResolver()
    {
        return searchKeywordResolver;
    }

    public void setSearchKeywordResolver(SearchKeywordResolver searchKeywordResolver)
    {
        this.searchKeywordResolver = searchKeywordResolver;
    }


    @Override
    public UIComponent createComponent(ValueBinding componentBinding, FacesContext context, String componentType)
            throws FacesException
    {
        return null;
    }

    @Override
    public MethodBinding createMethodBinding(String ref, Class<?>[] params) throws ReferenceSyntaxException
    {
        if (ref == null)
        {
            throw new NullPointerException();
        }
        else
        {
            return (new MockMethodBinding(this, ref, params));
        }
    }

    @Override
    public ValueBinding createValueBinding(String ref) throws ReferenceSyntaxException
    {
        if (ref == null)
        {
            throw new NullPointerException();
        }
        else
        {
            return (new MockValueBinding(this, ref));
        }
    }

    @Override
    public PropertyResolver getPropertyResolver()
    {
        return this.propertyResolver;
    }

    @Override
    public VariableResolver getVariableResolver()
    {

        return this.variableResolver;
    }

    @Override
    public void setVariableResolver(VariableResolver variableResolver)
    {
        this.variableResolver = variableResolver;
    }

    @Override
    public void setPropertyResolver(PropertyResolver resolver)
    {
        this.propertyResolver = resolver;
    }

}
