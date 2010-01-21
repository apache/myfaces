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
import javax.faces.component.UniqueIdVendor;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.TemplateClient;
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
    public final static String COMPOSITE_COMPONENT_STACK = "org.apache.myfaces.view.facelets.COMPOSITE_COMPONENT_STACK";

    public final static String UNIQUEID_VENDOR_STACK = "org.apache.myfaces.view.facelets.UNIQUEID_VENDOR_STACK";

    public final static String AJAX_HANDLER_STACK = "org.apache.myfaces.view.facelets.AJAX_HANDLER_STACK";
    
    public final static String VALIDATION_GROUPS_STACK = "org.apache.myfaces.view.facelets.VALIDATION_GROUPS_STACK";
    
    public final static String EXCLUDED_VALIDATOR_IDS_STACK = "org.apache.myfaces.view.facelets.EXCLUDED_VALIDATOR_IDS_STACK";

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

    public DefaultFaceletContext(DefaultFaceletContext ctx,
            DefaultFacelet facelet)
    {
        _ctx = ctx._ctx;
        _clients = ctx._clients;
        _faces = ctx._faces;
        _fnMapper = ctx._fnMapper;
        _ids = ctx._ids;
        _prefixes = ctx._prefixes;
        _varMapper = ctx._varMapper;
        _faceletHierarchy = new ArrayList<DefaultFacelet>(ctx._faceletHierarchy
                .size() + 1);
        _faceletHierarchy.addAll(ctx._faceletHierarchy);
        _faceletHierarchy.add(facelet);
        _facelet = facelet;

        //Update FACELET_CONTEXT_KEY on FacesContext attribute map, to 
        //reflect the current facelet context instance
        ctx.getFacesContext().getAttributes().put(
                FaceletContext.FACELET_CONTEXT_KEY, this);
    }

    public DefaultFaceletContext(FacesContext faces, DefaultFacelet facelet)
    {
        _ctx = faces.getELContext();
        _ids = new HashMap<String, Integer>();
        _prefixes = new HashMap<Integer, Integer>();
        _clients = new ArrayList<TemplateManager>(5);
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
                DefaultFacelet facelet = _faceletHierarchy.get(i);
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
                found = this._target
                        .apply(new DefaultFaceletContext(
                                (DefaultFaceletContext) ctx, this._owner),
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
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        //
        LinkedList<AjaxHandler> componentStack = (LinkedList<AjaxHandler>) attributes
                .remove(AJAX_HANDLER_STACK);
        _facelet.applyCompositeComponent(this, parent, resource);
        attributes.put(AJAX_HANDLER_STACK, componentStack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public UIComponent getCompositeComponentFromStack()
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<UIComponent> componentStack = (LinkedList<UIComponent>) attributes
                .get(COMPOSITE_COMPONENT_STACK);
        if (componentStack != null && !componentStack.isEmpty())
        {
            return componentStack.peek();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pushCompositeComponentToStack(UIComponent parent)
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<UIComponent> componentStack = (LinkedList<UIComponent>) attributes
                .get(COMPOSITE_COMPONENT_STACK);
        if (componentStack == null)
        {
            componentStack = new LinkedList<UIComponent>();
            attributes.put(COMPOSITE_COMPONENT_STACK, componentStack);
        }

        componentStack.addFirst(parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void popCompositeComponentToStack()
    {
        Map<Object, Object> contextAttributes = getFacesContext()
                .getAttributes();

        LinkedList<UIComponent> componentStack = (LinkedList<UIComponent>) contextAttributes
                .get(COMPOSITE_COMPONENT_STACK);
        if (componentStack != null && !componentStack.isEmpty())
        {
            componentStack.removeFirst();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public UniqueIdVendor getUniqueIdVendorFromStack()
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<UniqueIdVendor> componentStack = (LinkedList<UniqueIdVendor>) attributes
                .get(UNIQUEID_VENDOR_STACK);
        if (componentStack != null && !componentStack.isEmpty())
        {
            return componentStack.peek();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void popUniqueIdVendorToStack()
    {
        Map<Object, Object> contextAttributes = getFacesContext()
                .getAttributes();

        LinkedList<UniqueIdVendor> uniqueIdVendorStack = (LinkedList<UniqueIdVendor>) contextAttributes
                .get(UNIQUEID_VENDOR_STACK);
        if (uniqueIdVendorStack != null && !uniqueIdVendorStack.isEmpty())
        {
            uniqueIdVendorStack.removeFirst();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pushUniqueIdVendorToStack(UniqueIdVendor parent)
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<UniqueIdVendor> componentStack = (LinkedList<UniqueIdVendor>) attributes
                .get(UNIQUEID_VENDOR_STACK);
        if (componentStack == null)
        {
            componentStack = new LinkedList<UniqueIdVendor>();
            attributes.put(UNIQUEID_VENDOR_STACK, componentStack);
        }

        componentStack.addFirst(parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<AjaxHandler> getAjaxHandlers()
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<AjaxHandler> componentStack = (LinkedList<AjaxHandler>) attributes
                .get(AJAX_HANDLER_STACK);
        if (componentStack != null && !componentStack.isEmpty())
        {
            return componentStack.iterator();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void popAjaxHandlerToStack()
    {
        Map<Object, Object> contextAttributes = getFacesContext()
                .getAttributes();

        LinkedList<AjaxHandler> uniqueIdVendorStack = (LinkedList<AjaxHandler>) contextAttributes
                .get(AJAX_HANDLER_STACK);
        if (uniqueIdVendorStack != null && !uniqueIdVendorStack.isEmpty())
        {
            uniqueIdVendorStack.removeFirst();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pushAjaxHandlerToStack(
            AjaxHandler parent)
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<AjaxHandler> componentStack = (LinkedList<AjaxHandler>) attributes
                .get(AJAX_HANDLER_STACK);
        if (componentStack == null)
        {
            componentStack = new LinkedList<AjaxHandler>();
            attributes.put(AJAX_HANDLER_STACK, componentStack);
        }

        componentStack.addFirst(parent);
    }
    
    /**
     * Gets the top of the validationGroups stack.
     * @return
     * @since 2.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public String getFirstValidationGroupFromStack()
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();
        
        LinkedList<String> validationGroupsStack 
                = (LinkedList<String>) attributes.get(VALIDATION_GROUPS_STACK);
        if (validationGroupsStack != null && !validationGroupsStack.isEmpty())
        {
            return validationGroupsStack.getFirst(); // top-of-stack
        }
        return null;
    }
    
    /**
     * Removes top of stack.
     * @since 2.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public void popValidationGroupsToStack()
    {
        Map<Object, Object> contextAttributes = getFacesContext().getAttributes();
        
        LinkedList<String> validationGroupsStack 
                = (LinkedList<String>) contextAttributes.get(VALIDATION_GROUPS_STACK);
        if (validationGroupsStack != null && !validationGroupsStack.isEmpty())
        {
            validationGroupsStack.removeFirst();
        }
    }
    
    /**
     * Pushes validationGroups to the stack.
     * @param validationGroups
     * @since 2.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public void pushValidationGroupsToStack(String validationGroups)
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<String> validationGroupsStack 
                = (LinkedList<String>) attributes.get(VALIDATION_GROUPS_STACK);
        if (validationGroupsStack == null)
        {
            validationGroupsStack = new LinkedList<String>();
            attributes.put(VALIDATION_GROUPS_STACK, validationGroupsStack);
        }

        validationGroupsStack.addFirst(validationGroups);
    }
    
    /**
     * Gets all validationIds on the stack.
     * @return
     * @since 2.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<String> getExcludedValidatorIds()
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();
        
        LinkedList<String> excludedValidatorIdsStack 
                = (LinkedList<String>) attributes.get(EXCLUDED_VALIDATOR_IDS_STACK);
        if (excludedValidatorIdsStack != null && !excludedValidatorIdsStack.isEmpty())
        {
            return excludedValidatorIdsStack.iterator();
        }
        return null;
    }
    
    /**
     * Removes top of stack.
     * @since 2.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public void popExcludedValidatorIdToStack()
    {
        Map<Object, Object> contextAttributes = getFacesContext().getAttributes();
        
        LinkedList<String> excludedValidatorIdsStack 
                = (LinkedList<String>) contextAttributes.get(EXCLUDED_VALIDATOR_IDS_STACK);
        if (excludedValidatorIdsStack != null && !excludedValidatorIdsStack.isEmpty())
        {
            excludedValidatorIdsStack.removeFirst();
        }
    }
    
    /**
     * Pushes validatorId to the stack of excluded validatorIds.
     * @param validatorId
     * @since 2.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public void pushExcludedValidatorIdToStack(String validatorId)
    {
        Map<Object, Object> attributes = getFacesContext().getAttributes();

        LinkedList<String> excludedValidatorIdsStack 
                = (LinkedList<String>) attributes.get(EXCLUDED_VALIDATOR_IDS_STACK);
        if (excludedValidatorIdsStack == null)
        {
            excludedValidatorIdsStack = new LinkedList<String>();
            attributes.put(EXCLUDED_VALIDATOR_IDS_STACK, excludedValidatorIdsStack);
        }

        excludedValidatorIdsStack.addFirst(validatorId);
    }
}
