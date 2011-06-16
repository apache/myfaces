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
public final class DefaultVariableMapper extends VariableMapperBase
{
    private Map<String, ValueExpression> _vars;
    
    private PageContext _pageContext;
    
    private TemplateContext _templateContext;
    
    private VariableMapper _delegate;
    
    public boolean _trackResolveVariables;
    
    public boolean _variableResolved;

    public DefaultVariableMapper()
    {
        super();
        _trackResolveVariables = false;
        _variableResolved = false;
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

        //If the variable is not on the VariableMapper
        if (returnValue == null)
        {
            //Check on page and template context
            if (_pageContext != null && _pageContext.getAttributeCount() > 0)
            {
                if (_pageContext.getAttributes().containsKey(name))
                {
                    returnValue = _pageContext.getAttributes().get(name);
                    if (_trackResolveVariables)
                    {
                        _variableResolved = true;
                    }
                    return returnValue;
                }
            }
            
            if (_templateContext != null && !_templateContext.isParameterEmpty())
            {
                if (_templateContext.getParameterMap().containsKey(name))
                {
                    returnValue = _templateContext.getParameter(name);
                    if (_trackResolveVariables)
                    {
                        _variableResolved = true;
                    }
                    return returnValue;
                }
            }
            
            if (_delegate != null)
            {
                returnValue = _delegate.resolveVariable(name);
            }
        }
        else if (_trackResolveVariables)
        {
            // Is this code in a block that wants to cache 
            // the resulting expression(s) and variable has been resolved?
            _variableResolved = true;
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

    @Override
    public boolean isAnyFaceletsVariableResolved()
    {
        if (_trackResolveVariables)
        {
            return _variableResolved;
        }
        else
        {
            //Force expression creation
            return true;
        }
    }

    @Override
    public void beforeConstructELExpression()
    {
        _trackResolveVariables = true;
        _variableResolved = false;
    }

    @Override
    public void afterConstructELExpression()
    {
        _trackResolveVariables = false;
        _variableResolved = false;
    }
}
