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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.TemplateClient;
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
final class DefaultFaceletContext extends AbstractFaceletContext
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
    
    private final List<TemplateManager> _clients;

    public DefaultFaceletContext(DefaultFaceletContext ctx, DefaultFacelet facelet)
    {
        _ctx = ctx._ctx;
        _clients = ctx._clients;
        _faces = ctx._faces;
        _fnMapper = ctx._fnMapper;
        _ids = ctx._ids;
        _prefixes = ctx._prefixes;
        _varMapper = ctx._varMapper;
        _faceletHierarchy = new ArrayList<DefaultFacelet>(ctx._faceletHierarchy.size() + 1);
        _faceletHierarchy.addAll(ctx._faceletHierarchy);
        _faceletHierarchy.add(facelet);
        _facelet = facelet;
        
        //Update FACELET_CONTEXT_KEY on FacesContext attribute map, to 
        //reflect the current facelet context instance
        ctx.getFacesContext().getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, this);
    }

    public DefaultFaceletContext(FacesContext faces, DefaultFacelet facelet)
    {
        _ctx = faces.getELContext();
        _ids = new HashMap<String, Integer>();
        _prefixes = new HashMap<Integer, Integer>();
        _clients = new ArrayList(5);
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
        
        //Set FACELET_CONTEXT_KEY on FacesContext attribute map, to 
        //reflect the current facelet context instance
        faces.getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, this);
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
    
    //Begin methods from AbstractFaceletContext

    @Override
    public void popClient(TemplateClient client)
    {
        if (!this._clients.isEmpty())
        {
            Iterator<TemplateManager> itr = this._clients.iterator();
            while (itr.hasNext())
            {
                if (itr.next().equals(client))
                {
                    itr.remove();
                    return;
                }
            }
        }
        throw new IllegalStateException(client + " not found");
    }

    @Override
    public void pushClient(final TemplateClient client)
    {
        this._clients.add(0, new TemplateManager(this._facelet, client, true));
    }

    @Override
    public void extendClient(final TemplateClient client)
    {
        this._clients.add(new TemplateManager(this._facelet, client, false));
    }

    @Override
    public boolean includeDefinition(UIComponent parent, String name)
            throws IOException, FaceletException, FacesException, ELException
    {
        boolean found = false;
        TemplateManager client;

        for (int i = 0, size = this._clients.size(); i < size && !found; i++)
        {
            client = ((TemplateManager) this._clients.get(i));
            if (client.equals(this._facelet))
                continue;
            found = client.apply(this, parent, name);
        }

        return found;
    }

    private final static class TemplateManager implements TemplateClient
    {
        private final DefaultFacelet _owner;

        private final TemplateClient _target;

        private final boolean _root;

        private final Set<String> _names = new HashSet<String>();

        public TemplateManager(DefaultFacelet owner, TemplateClient target,
                boolean root)
        {
            this._owner = owner;
            this._target = target;
            this._root = root;
        }

        public boolean apply(FaceletContext ctx, UIComponent parent, String name)
                throws IOException, FacesException, FaceletException,
                ELException
        {
            String testName = (name != null) ? name : "facelets._NULL_DEF_";
            if (this._names.contains(testName))
            {
                return false;
            }
            else
            {
                this._names.add(testName);
                boolean found = false;
                found = this._target.apply(new DefaultFaceletContext(
                        (DefaultFaceletContext) ctx, this._owner), parent, name);
                this._names.remove(testName);
                return found;
            }
        }

        public boolean equals(Object o)
        {
            // System.out.println(this.owner.getAlias() + " == " +
            // ((DefaultFacelet) o).getAlias());
            return this._owner == o || this._target == o;
        }

        public boolean isRoot()
        {
            return this._root;
        }
    }
    
    //End methods from AbstractFaceletContext
    
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

    @Override
    public void applyCompositeComponent(UIComponent parent, Resource resource)
            throws  IOException, FaceletException, FacesException, ELException
    {
        _facelet.applyCompositeComponent(this, parent, resource);
    }
    
    @Override
    public UIComponent getCompositeComponentFromStack()
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();   
        
        Deque<UIComponent> componentStack = (Deque<UIComponent>) attributes.get(AbstractFaceletContext.COMPOSITE_COMPONENT_STACK);
        if(componentStack != null && !componentStack.isEmpty())
        {
            return componentStack.peek();
        }
        return null;
    }

    @Override
    public void pushCompositeComponentToStack(UIComponent parent)    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();   
        
        Deque<UIComponent> componentStack = (Deque<UIComponent>) attributes.get(AbstractFaceletContext.COMPOSITE_COMPONENT_STACK);
        if(componentStack == null)
        {
            componentStack = new ArrayDeque<UIComponent>();
            attributes.put(AbstractFaceletContext.COMPOSITE_COMPONENT_STACK, componentStack);
        }
        
        componentStack.push(parent);
    }

    @Override
    public void popCompositeComponentToStack()
    {
        Map<Object, Object> contextAttributes = getFacesContext().getAttributes();   
        
        Deque<UIComponent> componentStack = (Deque<UIComponent>) contextAttributes.get(AbstractFaceletContext.COMPOSITE_COMPONENT_STACK);
        if(componentStack != null && !componentStack.isEmpty())
        {
            componentStack.pop();
        }
    }    

}
