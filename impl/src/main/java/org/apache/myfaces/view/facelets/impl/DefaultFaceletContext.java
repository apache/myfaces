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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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

import org.apache.myfaces.view.facelets.AbstractFacelet;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.TemplateContext;
import org.apache.myfaces.view.facelets.TemplateManager;
import org.apache.myfaces.view.facelets.el.DefaultVariableMapper;
import org.apache.myfaces.view.facelets.tag.jsf.core.AjaxHandler;

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

    private final AbstractFacelet _facelet;
    private final List<AbstractFacelet> _faceletHierarchy;

    private VariableMapper _varMapper;

    private FunctionMapper _fnMapper;

    private final Map<String, Integer> _ids;
    private final Map<Integer, Integer> _prefixes;
    private String _prefix;

    private final StringBuilder _uniqueIdBuilder = new StringBuilder(30);

    //private final LinkedList<TemplateManager> _clients;
    
    private final FaceletCompositionContext _mctx;
    
    private LinkedList<AjaxHandler> _ajaxHandlerStack;
    
    private final List<TemplateContext> _isolatedTemplateContext;
    
    private int _currentTemplateContext;

    public DefaultFaceletContext(DefaultFaceletContext ctx,
            AbstractFacelet facelet, boolean ccWrap)
    {
        _ctx = ctx._ctx;
        _ids = ctx._ids;
        _prefixes = ctx._prefixes;
        //_clients = ctx._clients;
        _faces = ctx._faces;
        _fnMapper = ctx._fnMapper;
        _varMapper = ctx._varMapper;
        _faceletHierarchy = new ArrayList<AbstractFacelet>(ctx._faceletHierarchy
                .size() + 1);
        _faceletHierarchy.addAll(ctx._faceletHierarchy);
        _faceletHierarchy.add(facelet);
        _facelet = facelet;
        _mctx = ctx._mctx;
        
        if (ccWrap)
        {
            // Each time a composite component is being applied, a new
            // ajax stack should be created, and f:ajax tags outside the
            // composite component should be ignored.
            _ajaxHandlerStack = null;
        }
        else
        {
            // It is a template include, the current ajax stack should be
            // preserved.
            _ajaxHandlerStack = ctx._ajaxHandlerStack;
        }
        
        _isolatedTemplateContext = new ArrayList<TemplateContext>(ctx._isolatedTemplateContext.size()+1);
        for (int i = 0; i <= ctx._currentTemplateContext; i++)
        {
            _isolatedTemplateContext.add(ctx._isolatedTemplateContext.get(i));
        }
        _currentTemplateContext = ctx._currentTemplateContext;

        //Update FACELET_CONTEXT_KEY on FacesContext attribute map, to 
        //reflect the current facelet context instance
        ctx.getFacesContext().getAttributes().put(
                FaceletContext.FACELET_CONTEXT_KEY, this);
    }

    public DefaultFaceletContext(FacesContext faces, AbstractFacelet facelet, FaceletCompositionContext mctx)
    {
        _ctx = faces.getELContext();
        _ids = new HashMap<String, Integer>();
        _prefixes = new HashMap<Integer, Integer>();
        //_clients = new LinkedList<TemplateManager>();
        _faces = faces;
        _fnMapper = _ctx.getFunctionMapper();
        _varMapper = _ctx.getVariableMapper();
        if (_varMapper == null)
        {
            _varMapper = new DefaultVariableMapper();
        }        
        _faceletHierarchy = new ArrayList<AbstractFacelet>(1);
        _faceletHierarchy.add(facelet);
        _facelet = facelet;
        _mctx = mctx;
        
        _isolatedTemplateContext = new ArrayList<TemplateContext>(1);
        _isolatedTemplateContext.add(new TemplateContextImpl());
        _currentTemplateContext = 0;

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
    public void includeFacelet(UIComponent parent, String relativePath)
            throws IOException
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
            StringBuilder builder = new StringBuilder(
                    _faceletHierarchy.size() * 30);
            for (int i = 0; i < _faceletHierarchy.size(); i++)
            {
                AbstractFacelet facelet = _faceletHierarchy.get(i);
                builder.append(facelet.getAlias());
            }

            // Integer prefixInt = new Integer(builder.toString().hashCode());
            // -= Leonardo Uribe =- if the previous formula is used, it is possible that
            // negative values are introduced. The presence of '-' char causes problems
            // with htmlunit 2.4 or lower, so in order to prevent it it is better to use
            // only positive values instead.
            // Take into account CompilationManager.nextTagId() uses Math.abs too.
            Integer prefixInt = new Integer(Math.abs(builder.toString().hashCode()));

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
                _varMapper.setVariable(name, _facelet.getExpressionFactory()
                        .createValueExpression(value, Object.class));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void includeFacelet(UIComponent parent, URL absolutePath)
            throws IOException, FacesException, ELException
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
    public TemplateManager popClient(TemplateClient client)
    {
        //if (!this._clients.isEmpty())
        //{
        //    Iterator<TemplateManager> itr = this._clients.iterator();
        //    while (itr.hasNext())
        //    {
        //        if (itr.next().equals(client))
        //        {
        //            itr.remove();
        //            return;
        //        }
        //    }
        //}
        //throw new IllegalStateException(client + " not found");
        //return _clients.removeFirst();
        return _isolatedTemplateContext.get(_currentTemplateContext).popClient();
    }

    @Override
    public void pushClient(final TemplateClient client)
    {
        //this._clients.add(0, new TemplateManager(this._facelet, client, true));
        //_clients.addFirst(new TemplateManagerImpl(this._facelet, client, true));
        _isolatedTemplateContext.get(_currentTemplateContext).pushClient(this._facelet, client);
    }

    public TemplateManager popExtendedClient(TemplateClient client)
    {
        //return _clients.removeLast();
        return _isolatedTemplateContext.get(_currentTemplateContext).popExtendedClient();
    }
    
    @Override
    public void extendClient(final TemplateClient client)
    {
        //this._clients.add(new TemplateManager(this._facelet, client, false));
        //_clients.addLast(new TemplateManagerImpl(this._facelet, client, false));
        _isolatedTemplateContext.get(_currentTemplateContext).extendClient(this._facelet, client);
    }

    @Override
    public boolean includeDefinition(UIComponent parent, String name)
            throws IOException, FaceletException, FacesException, ELException
    {
        //boolean found = false;
        //TemplateManager client;
        //for (int i = 0, size = this._clients.size(); i < size && !found; i++)
        //{
        //    client = ((TemplateManager) this._clients.get(i));
        //    if (client.equals(this._facelet))
        //        continue;
        //    found = client.apply(this, parent, name);
        //}
        //return found;
        return _isolatedTemplateContext.get(_currentTemplateContext).includeDefinition(this, this._facelet, parent, name);
    }

    /*
    private final static class TemplateManagerImpl extends TemplateManager implements TemplateClient
    {
        private final DefaultFacelet _owner;

        private final TemplateClient _target;

        private final boolean _root;

        private final Set<String> _names = new HashSet<String>();

        public TemplateManagerImpl(DefaultFacelet owner, TemplateClient target,
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
                found = this._target
                        .apply(new DefaultFaceletContext(
                                (DefaultFaceletContext) ctx, this._owner, false),
                                parent, name);
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
    }*/

    /*
    @Override
    public TemplateManager popCompositeComponentClient(boolean cleanClientStack)
    {
        //if (!this._compositeComponentClients.isEmpty())
        //{
            //if (cleanClientStack)
            //{
            //    _clientsStack.get(_currentClientStack).clear();
            //}
            //_currentClientStack--;
            //return this._compositeComponentClients.remove(0);
        //}
        if (_currentTemplateContext > 0)
        {
            TemplateManager tm = _isolatedTemplateContext.get(_currentTemplateContext).getCompositeComponentClient();
            if (cleanClientStack)
            {
                _isolatedTemplateContext.get(_currentTemplateContext).clear();
            }
            _currentTemplateContext--;
            return tm;
        }
        return null;
    }
    

    @Override
    public void pushCompositeComponentClient(final TemplateClient client)
    {
        //this._compositeComponentClients.add(0, new CompositeComponentTemplateManager(this._facelet, client));
        //if (_currentClientStack + 1 <= _clientsStack.size())
        //{
        //    _clientsStack.add(new LinkedList<TemplateManager>());
        //}
        //_currentClientStack++;
        if (_currentTemplateContext + 1 <= _isolatedTemplateContext.size())
        {
            _isolatedTemplateContext.add(new IsolatedTemplateContextImpl());
        }
        _currentTemplateContext++;
        _isolatedTemplateContext.get(_currentTemplateContext).setCompositeComponentClient( new CompositeComponentTemplateManager(this._facelet, client));
    }
    
    @Override
    public void pushCompositeComponentClient(final TemplateManager client)
    {
        //this._compositeComponentClients.add(0, client);
        //if (_currentClientStack + 1 < _clientsStack.size())
        //{
        //    _clientsStack.add(new LinkedList<TemplateManager>());
        //}
        //_currentClientStack++;
        if (_currentTemplateContext + 1 < _isolatedTemplateContext.size())
        {
            _isolatedTemplateContext.add(new IsolatedTemplateContextImpl());
        }
        _currentTemplateContext++;
        _isolatedTemplateContext.get(_currentTemplateContext).setCompositeComponentClient(client);
    }*/
    
    @Override
    public void pushCompositeComponentClient(final TemplateClient client)
    {
        TemplateContext itc = new TemplateContextImpl();
        itc.setCompositeComponentClient(new CompositeComponentTemplateManager(this._facelet, client));
        _isolatedTemplateContext.add(itc);
        _currentTemplateContext++;
    }
    
    @Override
    public void popCompositeComponentClient()
    {
        if (_currentTemplateContext > 0)
        {
            _isolatedTemplateContext.remove(_currentTemplateContext);
            _currentTemplateContext--;
        }
    }
    
    @Override
    public void pushTemplateContext(TemplateContext client)
    {
        _isolatedTemplateContext.add(client);
        _currentTemplateContext++;
    }    

    
    @Override
    public TemplateContext popTemplateContext()
    {
        if (_currentTemplateContext > 0)
        {
            TemplateContext itc = _isolatedTemplateContext.get(_currentTemplateContext);
            _isolatedTemplateContext.remove(_currentTemplateContext);
            _currentTemplateContext--;
            return itc;
        }
        return null;
    }

    @Override
    public boolean includeCompositeComponentDefinition(UIComponent parent, String name)
            throws IOException, FaceletException, FacesException, ELException
    {
        //boolean found = false;
        //TemplateManager client;

        //for (int i = 0, size = this._compositeComponentClients.size(); i < size && !found; i++)
        //{
        //    client = ((TemplateManager) this._compositeComponentClients.get(i));
        //    if (client.equals(this._facelet))
        //        continue;
        //    found = client.apply(this, parent, name);
        //}

        //return found;
        TemplateClient ccClient = _isolatedTemplateContext.get(_currentTemplateContext).getCompositeComponentClient();
        if (ccClient != null)
        {
            return ccClient.apply(this, parent, name);
        }
        return false;
    }
    
    private final static class CompositeComponentTemplateManager extends TemplateManager implements TemplateClient
    {
        private final AbstractFacelet _owner;

        protected final TemplateClient _target;

        private final Set<String> _names = new HashSet<String>();

        public CompositeComponentTemplateManager(AbstractFacelet owner, TemplateClient target)
        {
            this._owner = owner;
            this._target = target;
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
                found = this._target
                        .apply(new DefaultFaceletContext(
                                (DefaultFaceletContext) ctx, this._owner, false),
                                parent, name);
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
            throws IOException, FaceletException, FacesException, ELException
    {
        _facelet.applyCompositeComponent(this, parent, resource);
    }

    @Override
    public Iterator<AjaxHandler> getAjaxHandlers()
    {
        if (_ajaxHandlerStack != null && !_ajaxHandlerStack.isEmpty())
        {
            return _ajaxHandlerStack.iterator();
        }
        return null;
    }

    @Override
    public void popAjaxHandlerToStack()
    {
        if (_ajaxHandlerStack != null && !_ajaxHandlerStack.isEmpty())
        {
            _ajaxHandlerStack.removeFirst();
        }
    }

    @Override
    public void pushAjaxHandlerToStack(
            AjaxHandler parent)
    {
        if (_ajaxHandlerStack == null)
        {
            _ajaxHandlerStack = new LinkedList<AjaxHandler>();
        }

        _ajaxHandlerStack.addFirst(parent);
    }

    @Override
    public boolean isBuildingCompositeComponentMetadata()
    {
        return _facelet.isBuildingCompositeComponentMetadata();
    }
    
    public FaceletCompositionContext getFaceletCompositionContext()
    {
        return _mctx;
    }
}
