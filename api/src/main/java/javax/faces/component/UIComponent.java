/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFComponent(
        type="javax.faces.Component",
        family="javax.faces.Component",
        desc = "abstract base component",
        configExcluded = true
        )
public abstract class UIComponent
        implements StateHolder
{
    
    protected Map<String,ValueExpression> bindings;
    
    public UIComponent()
    {
    }

    public abstract java.util.Map<String, Object> getAttributes();

    /**
     * @deprecated Replaced by getValueExpression
     */
    public abstract javax.faces.el.ValueBinding getValueBinding(java.lang.String name);

    public ValueExpression getValueExpression(String name)
    {
        if (name == null)
            throw new NullPointerException("name can not be null");

        if (bindings == null)
        {
            if (!(this instanceof UIComponentBase))
            {
                // if the component does not inherit from UIComponentBase and don't implements JSF 1.2 or later
                ValueBinding vb = getValueBinding(name);
                if (vb != null)
                {
                    bindings = new HashMap<String, ValueExpression>();
                    ValueExpression ve = new _ValueBindingToValueExpression(vb);
                    bindings.put(name, ve);
                    return ve;
                }
            }
        }
        else
        {
            return bindings.get(name);
        }

        return null;
    }
    
    /**
     * @deprecated Replaced by setValueExpression
     */
    public abstract void setValueBinding(java.lang.String name,
                                         javax.faces.el.ValueBinding binding);

    public void setValueExpression(String name, ValueExpression expression)
    {
        if (name == null)
            throw new NullPointerException("name");
        if (name.equals("id"))
            throw new IllegalArgumentException("Can't set a ValueExpression for the 'id' property.");
        if (name.equals("parent"))
            throw new IllegalArgumentException("Can't set a ValueExpression for the 'parent' property.");
        
        if (expression == null)
        {
            if (bindings != null)
            {
                bindings.remove(name);
                if(bindings.isEmpty())
                    bindings = null;
            }
        }
        else
        {
            if (expression.isLiteralText())
            {
                try
                {
                    Object value = expression.getValue(getFacesContext().getELContext());
                    getAttributes().put(name, value);
                    return;
                }
                catch (ELException e)
                {
                    throw new FacesException(e);
                }
            }

            if (bindings == null)
            {
                bindings = new HashMap<String, ValueExpression>();
            }

            bindings.put(name, expression);
        }
    }
    
    /**
     * Invokes the <code>invokeContextCallback</code> method with the component, specified by <code>clientId</code>.
     * @param context <code>FacesContext</code> for the current request
     * @param clientId the id of the desired <code>UIComponent</code> clazz 
     * @param callback Implementation of the <code>ContextCallback</code> to be called
     * @return has component been found ?
     * @throws javax.faces.FacesException
     */
    public boolean invokeOnComponent(javax.faces.context.FacesContext context, String clientId, javax.faces.component.ContextCallback callback) throws javax.faces.FacesException
    {
        //java.lang.NullPointerException - if any of the arguments are null
        if(context == null || clientId == null || callback == null)
        {
            throw new NullPointerException();
        }
        
        //searching for this component?
        boolean found = clientId.equals(this.getClientId(context)); 
        if(found)
        {
            try
            {
                callback.invokeContextCallback(context, this);
            } catch(Exception e)
            {
                throw new FacesException(e);
            }
            return found;
        }
        //Searching for this component's children/facets 
        for (Iterator<UIComponent> it = this.getFacetsAndChildren(); !found && it.hasNext();){
            found = it.next().invokeOnComponent(context, clientId, callback);
        }
            
        return found;
    }

    public abstract java.lang.String getClientId(javax.faces.context.FacesContext context);

    public abstract java.lang.String getFamily();

    public abstract java.lang.String getId();

    public abstract void setId(java.lang.String id);

    /**
     * Returns the parent of the component.
     * Children can be added to or removed from a component even if this method returns null
     * for the child.
     */
    public abstract javax.faces.component.UIComponent getParent();

    /**
     * For JSF-framework internal use only.   Don't call this method to
     * add components to the component tree.
     * Use <code>parent.getChildren().add(child)</code> instead.
     */
    public abstract void setParent(javax.faces.component.UIComponent parent);

    public abstract boolean isRendered();

    public abstract void setRendered(boolean rendered);

    public abstract java.lang.String getRendererType();

    public abstract void setRendererType(java.lang.String rendererType);

    public abstract boolean getRendersChildren();

    public abstract java.util.List<UIComponent> getChildren();

    public abstract int getChildCount();

    public abstract javax.faces.component.UIComponent findComponent(java.lang.String expr);

    public abstract java.util.Map<String, UIComponent> getFacets();

    public abstract javax.faces.component.UIComponent getFacet(java.lang.String name);

    public abstract java.util.Iterator<UIComponent> getFacetsAndChildren();

    public abstract void broadcast(javax.faces.event.FacesEvent event)
            throws AbortProcessingException;

    public abstract void decode(javax.faces.context.FacesContext context);

    public abstract void encodeBegin(javax.faces.context.FacesContext context)
            throws java.io.IOException;

    public abstract void encodeChildren(javax.faces.context.FacesContext context)
            throws java.io.IOException;

    public abstract void encodeEnd(javax.faces.context.FacesContext context)
            throws java.io.IOException;

    public void encodeAll(javax.faces.context.FacesContext context) throws java.io.IOException
    {
        if(context == null)
        {
            throw new NullPointerException();
        }
        
        if(isRendered())
        {
            this.encodeBegin(context);
            
            //rendering children
            if(this.getRendersChildren())
            {
                this.encodeChildren(context);
            }
            //let children render itself
            else
            {
          if(this.getChildCount()>0) {
                  for (UIComponent comp : this.getChildren()) {
                        comp.encodeAll(context);
                    }
          }
            }
            this.encodeEnd(context);
        }
    }



    protected abstract void addFacesListener(javax.faces.event.FacesListener listener);

    protected abstract javax.faces.event.FacesListener[] getFacesListeners(java.lang.Class clazz);

    protected abstract void removeFacesListener(javax.faces.event.FacesListener listener);

    public abstract void queueEvent(javax.faces.event.FacesEvent event);

    public abstract void processRestoreState(javax.faces.context.FacesContext context,
                                             java.lang.Object state);

    public abstract void processDecodes(javax.faces.context.FacesContext context);

    public abstract void processValidators(javax.faces.context.FacesContext context);

    public abstract void processUpdates(javax.faces.context.FacesContext context);

    public abstract java.lang.Object processSaveState(javax.faces.context.FacesContext context);

    protected abstract javax.faces.context.FacesContext getFacesContext();

    protected abstract javax.faces.render.Renderer getRenderer(javax.faces.context.FacesContext context);

    /**
     * @since 1.2
     */
    
    public int getFacetCount()
    {
        // not sure why the RI has this method in both 
        // UIComponent and UIComponentBase
        Map<String, UIComponent> facets = getFacets();
        return facets == null ? 0 : facets.size();
    }
    
    /**
     * @since 1.2
     */
    
    public String getContainerClientId(FacesContext ctx)
    {
        if( ctx == null )
            throw new NullPointerException("FacesContext ctx");
        
        return getClientId(ctx);
    }
}
