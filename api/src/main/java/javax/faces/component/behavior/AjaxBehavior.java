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
package javax.faces.component.behavior;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorListener;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 16:04:27 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public abstract class AjaxBehavior extends ClientBehaviorBase
{
    public static final String BEHAVIOR_ID = "javax.faces.behavior.Ajax";
    
    private static final Set<ClientBehaviorHint> HINTS = Collections.singleton(ClientBehaviorHint.SUBMITTING);

    private static final String ATTR_DISABLED = "disabled";
    private static final String ATTR_EXECUTE = "execute";
    private static final String ATTR_IMMEDIATE = "immediate";
    private static final String ATTR_ON_ERROR = "onError";
    private static final String ATTR_ON_EVENT = "onEvent";
    private static final String ATTR_RENDER = "render";
    
    private Boolean _disabled;
    private Collection<String> _execute;
    private Boolean _immediate;
    private String _onError;
    private String _onEvent;
    private Set<String> _render;
    
    private Map<String, ValueExpression> _expressions;
    

    /**
     * 
     */
    public AjaxBehavior()
    {
        // Default capacity (10) should be decent with the default load factor (0.75)
        // So there's a total of 7 slots in the map with 7 default attributes
        _expressions = new HashMap<String, ValueExpression>();
    }
    
    public void addAjaxBehaviorListener(AjaxBehaviorListener listener)
    {
        addBehaviorListener(listener);
    }
    
    public Collection<String> getExecute(FacesContext context)
    {
        return _getIds(ATTR_EXECUTE, _execute);
    }
    
    public Set<ClientBehaviorHint> getHints()
    {
        // FIXME: Useless, notify the EG... Mojarra returns a static Set containing SUBMITTING...
        // Copying Mojarra behavior for now by assuming they simply forgot to add JavaDoc in the class
        return HINTS;
    }
    
    public String getOnError(FacesContext context)
    {
        return _resolve(ATTR_ON_ERROR, _onError);
    }
    
    public String getOnEvent(FacesContext context)
    {
        return _resolve(ATTR_ON_EVENT, _onEvent);
    }
    
    public Collection<String> getRender(FacesContext context)
    {
        return _getIds(ATTR_RENDER, _render);
    }

    public String getRendererType()
    {
        return BEHAVIOR_ID;
    }
    
    public ValueExpression getValueExpression(String name)
    {
        _checkNull(name);
        
        return _expressions.get(name);
    }
    
    public boolean isDisabled(FacesContext context)
    {
        return Boolean.TRUE.equals(_resolve(ATTR_DISABLED, _disabled));
    }
    
    public boolean isImmediate(FacesContext context)
    {
        return Boolean.TRUE.equals(_resolve(ATTR_IMMEDIATE, _immediate));
    }
    
    public boolean isImmediateSet(FacesContext context)
    {
        return _immediate != null || _expressions.containsKey(ATTR_IMMEDIATE);
    }
    
    public void removeAjaxBehaviorListener(AjaxBehaviorListener listener)
    {
        removeBehaviorListener(listener);
    }
    
    @Override
    public void restoreState(FacesContext context, Object state)
    {
        // TODO: IMPLEMENT HERE - Full + delta support
        super.restoreState(context, state);
    }

    @Override
    public Object saveState(FacesContext context)
    {
        // TODO: IMPLEMENT HERE - Full + delta support
        return super.saveState(context);
    }
    
    public void setDisabled(Boolean disabled)
    {
        this._disabled = disabled;
    }

    public void setExecute(Collection<String> execute)
    {
        this._execute = execute;
    }

    public void setImmediate(Boolean immediate)
    {
        this._immediate = immediate;
    }

    public void setOnError(String error)
    {
        _onError = error;
    }

    public void setOnEvent(String event)
    {
        _onEvent = event;
    }

    public void setRender(Set<String> render)
    {
        this._render = render;
    }
    
    public void setValueExpression(String name, ValueExpression expression)
    {
        _checkNull(name);
        
        if (expression == null)
        {
            _expressions.remove(name);
        }
        else
        {
            _expressions.put(name, expression);
        }
    }

    private void _checkNull(String name)
    {
        if (name == null)
        {
            throw new NullPointerException();
        }
    }

    private Collection<String> _getIds(String name, Collection<String> explicitValue)
    {
        // FIXME: I don't see how I can make it non empty when there's nothing that I can put in it
        //        by default
        Collection<String> ids = _resolve(name, explicitValue);
        
        return ids == null ? Collections.<String>emptyList() : ids;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T _resolve(String name, T explicitValue)
    {
        assert name != null;
        assert name.length() > 0;
        
        if (explicitValue == null)
        {
            ValueExpression expression = _expressions.get(name);
            if (expression != null)
            {
                explicitValue = (T)expression.getValue(FacesContext.getCurrentInstance().getELContext());
            }
        }
        
        return explicitValue;
    }
}
