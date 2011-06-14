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
package org.apache.myfaces.view.facelets.el;

import java.util.HashMap;
import java.util.Map;

import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.myfaces.view.facelets.PageContext;
import org.apache.myfaces.view.facelets.TemplateContext;

/**
 * Default instance of a VariableMapper backed by a Map
 * 
 * @see javax.el.VariableMapper
 * @see javax.el.ValueExpression
 * @see java.util.Map
 * 
 * @author Jacob Hookom
 * @version $Id: DefaultVariableMapper.java,v 1.3 2008/07/13 19:01:43 rlubke Exp $
 */
public final class DefaultVariableMapper extends VariableMapper
{
    private Map<String, ValueExpression> _vars;
    
    private PageContext _pageContext;
    
    private TemplateContext _templateContext;
    
    private VariableMapper _delegate;

    public DefaultVariableMapper()
    {
        super();
    }

    public DefaultVariableMapper(VariableMapper delegate)
    {
        super();
        this._delegate = delegate;
    }
    
    /**
     * @see javax.el.VariableMapper#resolveVariable(java.lang.String)
     */
    public ValueExpression resolveVariable(String name)
    {
        ValueExpression returnValue = null;
        
        if (_vars != null)
        {
            returnValue = _vars.get(name);
        }
        
        if (returnValue == null && _pageContext != null)
        {
            if (returnValue == null && _pageContext.getAttributeCount() > 0)
            {
                returnValue = _pageContext.getAttributes().get(name);
            }
        }
        
        if (returnValue == null && _templateContext != null)
        {
            if (returnValue == null && !_templateContext.isParameterEmpty())
            {
                returnValue = _templateContext.getParameter(name);
            }
        }        
        
        if (returnValue == null && _delegate != null)
        {
            returnValue = _delegate.resolveVariable(name);
        }
        
        return returnValue;
    }

    /**
     * @see javax.el.VariableMapper#setVariable(java.lang.String, javax.el.ValueExpression)
     */
    public ValueExpression setVariable(String name, ValueExpression expression)
    {
        if (_vars == null)
        {
            _vars = new HashMap<String, ValueExpression>();
        }
        
        return _vars.put(name, expression);
    }
    
    /**
     * Set the current page context this variable mapper should resolve against.
     * 
     * @param pageContext
     */
    public void setPageContext(PageContext pageContext)
    {
        this._pageContext = pageContext;
    }
    
    /**
     * Set the current template context this variable mapper should resolve against.
     * 
     * @param templateContext
     */
    public void setTemplateContext(TemplateContext templateContext)
    {
        this._templateContext = templateContext;
    }
}
