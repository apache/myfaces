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
package javax.faces.component;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFComponent(type = "javax.faces.Component", family = "javax.faces.Component", desc = "abstract base component", configExcluded = true)
public abstract class UIComponent implements StateHolder
{
    public static final String BEANINFO_KEY = "javax.faces.component.BEANINFO_KEY";
    public static final String COMPOSITE_COMPONENT_TYPE_KEY = "javax.faces.component.COMPOSITE_COMPONENT_TYPE";
    public static final String COMPOSITE_FACET_NAME = "javax.faces.component.COMPOSITE_FACET_NAME";
    public static final String CURRENT_COMPONENT = "javax.faces.component.CURRENT_COMPONENT";
    public static final String CURRENT_COMPOSITE_COMPONENT = "javax.faces.component.CURRENT_COMPOSITE_COMPONENT";
    public static final String FACETS_KEY = "javax.faces.component.FACETS_KEY";
    private static final String _COMPONENT_STACK = "componentStack:" + UIComponent.class.getName();

    private Map<Class<? extends SystemEvent>, List<SystemEventListener>> _systemEventListenerClassMap;
    
    protected Map<String, ValueExpression> bindings;
    
    /**
     * Used to cache the map created using getResourceBundleMap() method,
     * since this method could be called several times when rendering the
     * composite component. This attribute may not be serialized,
     * so transient is used (There are some very few special cases when 
     * UIComponent instances are serializable like t:schedule, so it 
     * is better if transient is used).
     */
    private transient Map<String,String> _resourceBundleMap = null;

    public UIComponent()
    {
    }

    public static UIComponent getCurrentComponent(FacesContext context)
    {
        /*
         * Return the UIComponent instance that is currently processing. This is equivalent to evaluating the EL
         * expression "#{component}" and doing a getValue operation on the resultant ValueExpression.
         */
        Application application = context.getApplication();

        ELContext elContext = context.getELContext();
        Object result = application.getELResolver().getValue(context.getELContext(), null, "component");

        return elContext.isPropertyResolved() ? (UIComponent) result : null;
    }

    public static UIComponent getCurrentCompositeComponent(FacesContext context)
    {
        // Return the closest ancestor component, relative to the component returned from 
        // getCurrentComponent(javax.faces.context.FacesContext), that is a composite component, 
        // or null if no such component exists.
        UIComponent currentComponent = getCurrentComponent(context);
        while (currentComponent != null && !currentComponent._isCompositeComponent())
        {
            currentComponent = currentComponent.getParent();
        }
        
        return currentComponent;
    }

    public abstract Map<String, Object> getAttributes();

    /**
     * @deprecated Replaced by getValueExpression
     */
    @Deprecated
    public abstract ValueBinding getValueBinding(String name);

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
    @Deprecated
    public abstract void setValueBinding(String name, ValueBinding binding);

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
                if (bindings.isEmpty())
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
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param clientId
     *            the id of the desired <code>UIComponent</code> clazz
     * @param callback
     *            Implementation of the <code>ContextCallback</code> to be called
     * @return has component been found ?
     * @throws javax.faces.FacesException
     */
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback)
        throws FacesException
    {
        // java.lang.NullPointerException - if any of the arguments are null
        if (context == null || clientId == null || callback == null)
        {
            throw new NullPointerException();
        }

        // searching for this component?
        boolean found = clientId.equals(this.getClientId(context));
        if (found)
        {
            try
            {
                callback.invokeContextCallback(context, this);
            }
            catch (Exception e)
            {
                throw new FacesException(e);
            }
            return found;
        }
        // Searching for this component's children/facets
        for (Iterator<UIComponent> it = this.getFacetsAndChildren(); !found && it.hasNext();)
        {
            found = it.next().invokeOnComponent(context, clientId, callback);
        }

        return found;
    }

    public abstract String getClientId(FacesContext context);

    public abstract String getFamily();

    public abstract String getId();

    public List<SystemEventListener> getListenersForEventClass(Class<? extends SystemEvent> eventClass)
    {
        List<SystemEventListener> listeners;
        if (_systemEventListenerClassMap == null)
        {
            listeners = Collections.emptyList();
        }
        else
        {
            listeners = _systemEventListenerClassMap.get(eventClass);
            if (listeners == null)
            {
                listeners = Collections.emptyList();
            }
            else
            {
                listeners = Collections.unmodifiableList(listeners);
            }
        }
        
        return listeners;
    }

    public abstract void setId(String id);

    /**
     * Returns the parent of the component. Children can be added to or removed from a component even if this method
     * returns null for the child.
     */
    public abstract UIComponent getParent();

    /**
     * For JSF-framework internal use only. Don't call this method to add components to the component tree. Use
     * <code>parent.getChildren().add(child)</code> instead.
     */
    public abstract void setParent(UIComponent parent);

    public abstract boolean isRendered();

    public abstract void setRendered(boolean rendered);

    public abstract String getRendererType();

    public abstract void setRendererType(String rendererType);

    public abstract boolean getRendersChildren();

    public Map<String, String> getResourceBundleMap()
    {
        if (_resourceBundleMap == null)
        {
            FacesContext context = getFacesContext();
            Locale locale = context.getViewRoot().getLocale();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
            try
            {
                // looks for a ResourceBundle with a base name equal to the fully qualified class
                // name of the current UIComponent this and Locale equal to the Locale of the current UIViewRoot.
                _resourceBundleMap = new BundleMap(ResourceBundle.getBundle(getClass().getName(), locale, loader));
            }
            catch (MissingResourceException e)
            {
                //If no such bundle is found, and the component is a composite component
                if (this._isCompositeComponent())
                {
                    //No need to check componentResource (the resource used to build the composite
                    //component instance) to null since it is already done on this._isCompositeComponent()
                    Resource componentResource = (Resource) getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                    // Let resourceName be the resourceName of the Resource for this composite component,
                    // replacing the file extension with ".properties"
                    int extensionIndex = componentResource.getResourceName().lastIndexOf('.');                
                    String resourceName =  (extensionIndex < 0 ? componentResource.getResourceName()
                            : componentResource.getResourceName().substring(0,extensionIndex) )+ ".properties" ;
                    
                    // Let libraryName be the libraryName of the the Resource for this composite component.                
                    // Call ResourceHandler.createResource(java.lang.String,java.lang.String), passing the derived resourceName and
                    // libraryName.
                    Resource bundleResource = context.getApplication().getResourceHandler().createResource(resourceName, 
                            componentResource.getLibraryName());
    
                    if (bundleResource != null)
                    {
                        // If the resultant Resource exists and can be found, the InputStream for the resource
                        // is used to create a ResourceBundle. If either of the two previous steps for obtaining the ResourceBundle
                        // for this component is successful, the ResourceBundle is wrapped in a Map<String, String> and returned.
                        try
                        {
                            _resourceBundleMap = new BundleMap(new PropertyResourceBundle(bundleResource.getInputStream())); 
                        }
                        catch (IOException e1)
                        {
                            //Nothing happens, then resourceBundleMap is set as empty map
                        }
                    }
                }
                // Otherwise Collections.EMPTY_MAP is returned.
                if (_resourceBundleMap == null)
                {
                    _resourceBundleMap = Collections.emptyMap();
                }
            }
        }

        return _resourceBundleMap;
    }

    public abstract List<UIComponent> getChildren();

    public abstract int getChildCount();

    public abstract UIComponent findComponent(String expr);

    public abstract Map<String, UIComponent> getFacets();

    public abstract UIComponent getFacet(String name);

    public abstract Iterator<UIComponent> getFacetsAndChildren();

    public abstract void broadcast(FacesEvent event) throws AbortProcessingException;

    public abstract void decode(FacesContext context);

    public abstract void encodeBegin(FacesContext context) throws IOException;

    public abstract void encodeChildren(FacesContext context) throws IOException;

    public abstract void encodeEnd(FacesContext context) throws IOException;

    public void encodeAll(FacesContext context) throws IOException
    {
        if (context == null)
        {
            throw new NullPointerException();
        }

        if (isRendered())
        {
            this.encodeBegin(context);

            // rendering children
            if (this.getRendersChildren())
            {
                this.encodeChildren(context);
            }
            // let children render itself
            else
            {
                if (this.getChildCount() > 0)
                {
                    for (UIComponent comp : this.getChildren())
                    {
                        comp.encodeAll(context);
                    }
                }
            }
            this.encodeEnd(context);
        }
    }

    protected abstract void addFacesListener(FacesListener listener);

    protected abstract FacesListener[] getFacesListeners(Class clazz);

    protected abstract void removeFacesListener(FacesListener listener);

    public abstract void queueEvent(javax.faces.event.FacesEvent event);

    public abstract void processRestoreState(FacesContext context, Object state);

    public abstract void processDecodes(FacesContext context);

    public abstract void processValidators(FacesContext context);

    public abstract void processUpdates(FacesContext context);

    public abstract java.lang.Object processSaveState(FacesContext context);

    public void subscribeToEvent(Class<? extends SystemEvent> eventClass, ComponentSystemEventListener componentListener)
    {
        // The default implementation creates an inner SystemEventListener instance that wraps argument 
        // componentListener as the listener argument.
        SystemEventListener listener = new EventListenerWrapper(this, componentListener);
        
        // Make sure the map exists
        if (_systemEventListenerClassMap == null)
        {
            _systemEventListenerClassMap = new HashMap<Class<? extends SystemEvent>, List<SystemEventListener>>();
        }
        
        List<SystemEventListener> listeners = _systemEventListenerClassMap.get(eventClass);
        // Make sure the list for class exists
        if (listeners == null)
        {
            listeners = new ArrayList<SystemEventListener>(2);
            _systemEventListenerClassMap.put(eventClass, listeners);
        }
        
        // Deal with contains? Spec is silent
        listeners.add(listener);
    }

    public void unsubscribeFromEvent(Class<? extends SystemEvent> eventClass,
                                     ComponentSystemEventListener componentListener)
    {
        /*
         * When doing the comparison to determine if an existing listener is equal to the argument 
         * componentListener (and thus must be removed), the equals() method on the existing listener must be 
         * invoked, passing the argument componentListener, rather than the other way around.
         * 
         * What is that supposed to mean? Are we supposed to keep an internal map of created listener wrappers?
         * TODO: Check with the EG what's the meaning of this, equals should be commutative -= Simon Lessard =-
         */
        SystemEventListener listener = new EventListenerWrapper(this, componentListener);
        
        getFacesContext().getApplication().unsubscribeFromEvent(eventClass, listener);
    }

    protected abstract FacesContext getFacesContext();

    protected abstract Renderer getRenderer(FacesContext context);

    @SuppressWarnings("unchecked")
    protected void popComponentFromEL(FacesContext context)
    {        
        Map<Object, Object> contextAttributes = context.getAttributes();        
        
        // Pop the current UIComponent from the FacesContext attributes map so that the previous 
        // UIComponent, if any, becomes the current component.
        Deque<UIComponent> componentStack = (Deque<UIComponent>) contextAttributes.get(UIComponent._COMPONENT_STACK);            
        contextAttributes.put(UIComponent.CURRENT_COMPONENT, componentStack.pop());
    }

    @SuppressWarnings("unchecked")
    protected void pushComponentToEL(FacesContext context, UIComponent component)
    {
        Map<Object, Object> contextAttributes = context.getAttributes();        
        UIComponent currentComponent = (UIComponent) contextAttributes.get(UIComponent.CURRENT_COMPONENT);
        
        if(currentComponent != null)
        {
            Deque<UIComponent> componentStack = (Deque<UIComponent>) contextAttributes.get(UIComponent._COMPONENT_STACK);
            if(componentStack == null)
            {
                componentStack = new ArrayDeque<UIComponent>();
                contextAttributes.put(UIComponent._COMPONENT_STACK, componentStack);
            }
            
            componentStack.push(currentComponent);
        }
        
        // Push the current UIComponent this to the FacesContext  attribute map using the key CURRENT_COMPONENT 
        // saving the previous UIComponent associated with CURRENT_COMPONENT for a subsequent call to 
        // popComponentFromEL(javax.faces.context.FacesContext).
        contextAttributes.put(UIComponent.CURRENT_COMPONENT, component);
 
    }

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
        if (ctx == null)
            throw new NullPointerException("FacesContext ctx");

        return getClientId(ctx);
    }
    
    private boolean _isCompositeComponent()
    {
        return getAttributes().get(Resource.COMPONENT_RESOURCE_KEY) != null;
    }

    private static class BundleMap implements Map<String, String>
    {
        private ResourceBundle _bundle;
        private List<String> _values;

        public BundleMap(ResourceBundle bundle)
        {
            _bundle = bundle;
        }

        // Optimized methods

        public String get(Object key)
        {
            try
            {
                return (String) _bundle.getObject(key.toString());
            }
            catch (Exception e)
            {
                return "???" + key + "???";
            }
        }

        public boolean isEmpty()
        {
            return !_bundle.getKeys().hasMoreElements();
        }

        public boolean containsKey(Object key)
        {
            try
            {
                return _bundle.getObject(key.toString()) != null;
            }
            catch (MissingResourceException e)
            {
                return false;
            }
        }

        // Unoptimized methods

        public Collection<String> values()
        {
            if (_values == null)
            {
                _values = new ArrayList<String>();
                for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements();)
                {
                    String v = _bundle.getString(enumer.nextElement());
                    _values.add(v);
                }
            }
            return _values;
        }

        public int size()
        {
            return values().size();
        }

        public boolean containsValue(Object value)
        {
            return values().contains(value);
        }

        public Set<Map.Entry<String, String>> entrySet()
        {
            Set<Entry<String, String>> set = new HashSet<Entry<String, String>>();
            for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements();)
            {
                final String k = enumer.nextElement();
                set.add(new Map.Entry<String, String>()
                {
                    public String getKey()
                    {
                        return k;
                    }

                    public String getValue()
                    {
                        return (String) _bundle.getObject(k);
                    }

                    public String setValue(String value)
                    {
                        throw new UnsupportedOperationException();
                    }
                });
            }

            return set;
        }

        public Set<String> keySet()
        {
            Set<String> set = new HashSet<String>();
            for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements();)
            {
                set.add(enumer.nextElement());
            }
            return set;
        }

        // Unsupported methods

        public String remove(Object key)
        {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map<? extends String, ? extends String> t)
        {
            throw new UnsupportedOperationException();
        }

        public String put(String key, String value)
        {
            throw new UnsupportedOperationException();
        }

        public void clear()
        {
            throw new UnsupportedOperationException();
        }

    }
    
    private class EventListenerWrapper implements SystemEventListener
    {
        private UIComponent component;
        private ComponentSystemEventListener listener;
        
        public EventListenerWrapper(UIComponent component, ComponentSystemEventListener listener)
        {
            assert component != null;
            assert listener != null;
            
            this.component = component;
            this.listener = listener;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            else if (o instanceof EventListenerWrapper)
            {
                EventListenerWrapper other = (EventListenerWrapper)o;
                return component.equals(other.component) && listener.equals(other.listener);
            }
            else
            {
                return false;
            }
        }
        
        @Override
        public int hashCode()
        {
            return component.hashCode() + listener.hashCode();
        }
        
        public boolean isListenerForSource(Object source)
        {
            // and its implementation of SystemEventListener.isListenerForSource(java.lang.Object) must return true 
            // if the instance class of this UIComponent is assignable from the argument to isListenerForSource.
            
            return source.getClass().isAssignableFrom(component.getClass());
        }

        public void processEvent(SystemEvent event)
        {
            // This inner class must call through to the argument componentListener in its implementation of 
            // SystemEventListener.processEvent(javax.faces.event.SystemEvent)
            
            assert event instanceof ComponentSystemEvent;
            
            listener.processEvent((ComponentSystemEvent)event);
        }
    }
}
