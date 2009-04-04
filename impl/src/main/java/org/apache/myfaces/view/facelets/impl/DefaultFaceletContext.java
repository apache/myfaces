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
package org.apache.myfaces.view.facelets.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import org.apache.myfaces.view.facelets.el.DefaultVariableMapper;

/**
 * Default FaceletContext implementation.
 * 
 * A single FaceletContext is used for all Facelets involved in an invocation of
 * {@link org.apache.myfaces.view.facelets.Facelet#apply(FacesContext, UIComponent) Facelet#apply(FacesContext, UIComponent)}. This
 * means that included Facelets are treated the same as the JSP include directive.
 * 
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletContext.java,v 1.4.4.3 2006/03/25 01:01:53 jhook Exp $
 */
final class DefaultFaceletContext extends FaceletContext
{

    private final FacesContext _faces;

    private final ELContext _ctx;

    private final DefaultFacelet _facelet;
    private final List<DefaultFacelet> _faceletHierarchy;

    private VariableMapper _varMapper;

    private FunctionMapper _fnMapper;

    private final Map<String, Integer> _ids;
    private final Map<Integer, Integer> _prefixes;
    private String _prefix;
    
    private final StringBuilder _uniqueIdBuilder = new StringBuilder(30);

    public DefaultFaceletContext(DefaultFaceletContext ctx, DefaultFacelet facelet)
    {
        _ctx = ctx._ctx;
        _faces = ctx._faces;
        _fnMapper = ctx._fnMapper;
        _ids = ctx._ids;
        _prefixes = ctx._prefixes;
        _varMapper = ctx._varMapper;
        _faceletHierarchy = new ArrayList<DefaultFacelet>(ctx._faceletHierarchy.size() + 1);
        _faceletHierarchy.addAll(ctx._faceletHierarchy);
        _faceletHierarchy.add(facelet);
        _facelet = facelet;
    }

    public DefaultFaceletContext(FacesContext faces, DefaultFacelet facelet)
    {
        _ctx = faces.getELContext();
        _ids = new HashMap<String, Integer>();
        _prefixes = new HashMap<Integer, Integer>();
        _faces = faces;
        _faceletHierarchy = new ArrayList<DefaultFacelet>(1);
        _faceletHierarchy.add(facelet);
        _facelet = facelet;
        _varMapper = _ctx.getVariableMapper();
        if (_varMapper == null)
        {
            _varMapper = new DefaultVariableMapper();
        }
        _fnMapper = _ctx.getFunctionMapper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FacesContext getFacesContext()
    {
        return _faces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpressionFactory getExpressionFactory()
    {
        return _facelet.getExpressionFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVariableMapper(VariableMapper varMapper)
    {
        // Assert.param("varMapper", varMapper);
        _varMapper = varMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFunctionMapper(FunctionMapper fnMapper)
    {
        // Assert.param("fnMapper", fnMapper);
        _fnMapper = fnMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void includeFacelet(UIComponent parent, String relativePath) throws IOException
    {
        _facelet.include(this, parent, relativePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FunctionMapper getFunctionMapper()
    {
        return _fnMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableMapper getVariableMapper()
    {
        return _varMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getContext(Class key)
    {
        return _ctx.getContext(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void putContext(Class key, Object contextObject)
    {
        _ctx.putContext(key, contextObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateUniqueId(String base)
    {

        if (_prefix == null)
        {
            // TODO: change to StringBuilder when JDK1.5 support is available
            StringBuffer builder = new StringBuffer(_faceletHierarchy.size() * 30);
            for (int i = 0; i < _faceletHierarchy.size(); i++)
            {
                DefaultFacelet facelet = _faceletHierarchy.get(i);
                builder.append(facelet.getAlias());
            }
            Integer prefixInt = new Integer(builder.toString().hashCode());

            Integer cnt = _prefixes.get(prefixInt);
            if (cnt == null)
            {
                _prefixes.put(prefixInt, new Integer(0));
                _prefix = prefixInt.toString();
            }
            else
            {
                int i = cnt.intValue() + 1;
                _prefixes.put(prefixInt, new Integer(i));
                _prefix = prefixInt + "_" + i;
            }
        }

        Integer cnt = _ids.get(base);
        if (cnt == null)
        {
            _ids.put(base, new Integer(0));
            _uniqueIdBuilder.delete(0, _uniqueIdBuilder.length());
            _uniqueIdBuilder.append(_prefix);
            _uniqueIdBuilder.append("_");
            _uniqueIdBuilder.append(base);
            return _uniqueIdBuilder.toString();
        }
        else
        {
            int i = cnt.intValue() + 1;
            _ids.put(base, new Integer(i));
            _uniqueIdBuilder.delete(0, _uniqueIdBuilder.length());
            _uniqueIdBuilder.append(_prefix);
            _uniqueIdBuilder.append("_");
            _uniqueIdBuilder.append(base);
            _uniqueIdBuilder.append("_");
            _uniqueIdBuilder.append(i);
            return _uniqueIdBuilder.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String name)
    {
        if (_varMapper != null)
        {
            ValueExpression ve = _varMapper.resolveVariable(name);
            if (ve != null)
            {
                return ve.getValue(this);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String name, Object value)
    {
        if (_varMapper != null)
        {
            if (value == null)
            {
                _varMapper.setVariable(name, null);
            }
            else
            {
                _varMapper.setVariable(name, 
                                       _facelet.getExpressionFactory().createValueExpression(value, Object.class));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void includeFacelet(UIComponent parent, URL absolutePath) throws IOException, FacesException, ELException
    {
        _facelet.include(this, parent, absolutePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ELResolver getELResolver()
    {
        return _ctx.getELResolver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPropertyResolved()
    {
        return _ctx.isPropertyResolved();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPropertyResolved(boolean resolved)
    {
        _ctx.setPropertyResolved(resolved);
    }
}
