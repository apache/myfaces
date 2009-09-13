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
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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
import java.util.ResourceBundle;
import java.util.Set;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.event.SystemEventListenerHolder;
import javax.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * TODO: PLUGINIZE - All HTML components now have enums for the properties, we have to enhance the Maven 
 *                   plugin to handle that
 * 
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">J
 * SF Specification</a>
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFComponent(type = "javax.faces.Component", family = "javax.faces.Component", desc = "abstract base component", configExcluded = true)
public abstract class UIComponent implements PartialStateHolder, SystemEventListenerHolder, ComponentSystemEventListener {
    // TODO: Reorder methods, this class is a mess

    public static final String ADDED_BY_PDL_KEY = "javax.faces.component.ADDED_BY_PDL_KEY";
    public static final String BEANINFO_KEY = "javax.faces.component.BEANINFO_KEY";
    public static final String COMPOSITE_COMPONENT_TYPE_KEY = "javax.faces.component.COMPOSITE_COMPONENT_TYPE";
    public static final String COMPOSITE_FACET_NAME = "javax.faces.component.COMPOSITE_FACET_NAME";
    public static final String CURRENT_COMPONENT = "javax.faces.component.CURRENT_COMPONENT";
    public static final String CURRENT_COMPOSITE_COMPONENT = "javax.faces.component.CURRENT_COMPOSITE_COMPONENT";
    public static final String FACETS_KEY = "javax.faces.component.FACETS_KEY";
    public static final String VIEW_LOCATION_KEY = "javax.faces.component.VIEW_LOCATION_KEY";
    private static final String _COMPONENT_STACK = "componentStack:" + UIComponent.class.getName();
    Map<Class<? extends SystemEvent>, List<SystemEventListener>> _systemEventListenerClassMap;
    
    @Deprecated
    protected Map<String, ValueExpression> bindings;
    /**
     * Used to cache the map created using getResourceBundleMap() method, since this method could be called several
     * times when rendering the composite component. This attribute may not be serialized, so transient is used (There
     * are some very few special cases when UIComponent instances are serializable like t:schedule, so it is better if
     * transient is used).
     */
    private transient Map<String, String> _resourceBundleMap = null;
    private boolean _inView = false;
    private StateHelper _stateHelper = null;
    
    /**
     * In JSF 2.0 bindings map was deprecated, and replaced with a map
     * inside stateHelper. We need this one here because stateHelper needs
     * to be implemented from here and internally it depends from this property.
     */
    private boolean _initialStateMarked = false;

    public UIComponent() {
    }

    public abstract Map<String, Object> getAttributes();

    /**
     * 
     * {@inheritDoc}
     * 
     * @since 2.0
     */
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
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
            throws FacesException {
        // java.lang.NullPointerException - if any of the arguments are null
        if (context == null || clientId == null || callback == null) {
            throw new NullPointerException();
        }

        // searching for this component?
        boolean found = clientId.equals(this.getClientId(context));
        if (found) {
            try {
                callback.invokeContextCallback(context, this);
            } catch (Exception e) {
                throw new FacesException(e);
            }
            return found;
        }
        // Searching for this component's children/facets
        for (Iterator<UIComponent> it = this.getFacetsAndChildren(); !found && it.hasNext();) {
            found = it.next().invokeOnComponent(context, clientId, callback);
        }

        return found;
    }

    /**
     * 
     * @param component
     * @return true if the component is a composite component otherwise false is returned
     * 
     *
     * @throws NullPointerException if the component is null
     * @since 2.0
     */
    public static boolean isCompositeComponent(UIComponent component) {

        //since _isCompositeComponent does it the same way we do it here also although I
        //would prefer following method

        //return component.getRendererType().equals("javax.faces.Composite");

        return component.getAttributes().containsKey(Resource.COMPONENT_RESOURCE_KEY);
    }

    /**
     * Indicate if this component is inside a view,
     * or in other words is contained by an UIViewRoot
     * instance (which represents the view). If this component
     * is a UIViewRoot instance, the components "always"
     * is on the view.
     * 
     * By default it is false but for UIViewRoot instances is
     * true. 
     * 
     * @return
     * 
     * @since 2.0
     */
    public boolean isInView() {
        return _inView;
    }

    public abstract boolean isRendered();

    public void markInitialState()
    {
        _initialStateMarked = true;
    }

    /**
     *
     * This method indicates if a component is visitable
     * according to the hints passed by the VisitContext parameter!
     *
     * This method internally is used by visitTree and if it returns false
     * it short circuits the visitTree execution.
     *
     *
     *
     * @param context
     * @return
     * 
     * @since 2.0
     */
    protected boolean isVisitable(VisitContext context) {

        Set <VisitHint> visitHints = context.getHints();
        boolean retVal = !((visitHints.contains(VisitHint.SKIP_UNRENDERED)  && !this.isRendered()) ||
           (visitHints.contains(VisitHint.SKIP_TRANSIENT) && this.isTransient()));
        //executable cannot be handled here because we do not have any method to determine
        //whether a component is executable or not, this seems to be a hole in the spec!
        //but we can resolve it on ppr context level, where it is needed!
        //maybe in the long run we can move it down here, if it makes sense
        return retVal;
    }

    /**
     * @deprecated Replaced by setValueExpression
     */
    @Deprecated
    public abstract void setValueBinding(String name, ValueBinding binding);

    public void setValueExpression(String name, ValueExpression expression) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.equals("id")) {
            throw new IllegalArgumentException("Can't set a ValueExpression for the 'id' property.");
        }
        if (name.equals("parent")) {
            throw new IllegalArgumentException("Can't set a ValueExpression for the 'parent' property.");
        }

        if (expression == null) {
            //if (bindings != null) {
            //    bindings.remove(name);
            //    if (bindings.isEmpty()) {
            //        bindings = null;
            //    }
            //}
            getStateHelper().remove(PropertyKeys.bindings, name);
        } else {
            if (expression.isLiteralText()) {
                try {
                    Object value = expression.getValue(getFacesContext().getELContext());
                    getAttributes().put(name, value);
                    return;
                } catch (ELException e) {
                    throw new FacesException(e);
                }
            }

            //if (bindings == null) {
            //    bindings = new HashMap<String, ValueExpression>();
            //}
            //
            //bindings.put(name, expression);
            getStateHelper().put(PropertyKeys.bindings, name, expression);
        }
    }

    public String getClientId() {
        return getClientId(getFacesContext());
    }

    public abstract String getClientId(FacesContext context);

    /**
     * search for the nearest parent composite component, if no parent is found
     * it has to return null!
     *
     * if the component itself is null we have to return null as well!
     *
     * @param component the component to start from
     * @return the parent composite component if found otherwise null
     * 
     * @since 2.0
     */
    public static UIComponent getCompositeComponentParent(UIComponent component) {

        if(component == null) {
            return null;
        }
        UIComponent parent = null;

        do {
            parent = component.getParent();
            if(parent != null && UIComponent.isCompositeComponent(parent)) {
                return parent;
            }
        } while(parent != null);
        return null;
    }

    /**
     * @since 1.2
     */
    public String getContainerClientId(FacesContext ctx) {
        if (ctx == null) {
            throw new NullPointerException("FacesContext ctx");
        }

        return getClientId(ctx);
    }

    /**
     * 
     * @param context
     * @return
     * 
     * @since 2.0
     */
    public static UIComponent getCurrentComponent(FacesContext context) {
        return (UIComponent) context.getAttributes().get(UIComponent.CURRENT_COMPONENT);
    }

    /**
     * 
     * @param context
     * @return
     * 
     * @since 2.0
     */
    public static UIComponent getCurrentCompositeComponent(FacesContext context) {
        return (UIComponent) context.getAttributes().get(UIComponent.CURRENT_COMPOSITE_COMPONENT);
    }

    public abstract String getFamily();

    public abstract String getId();

    public List<SystemEventListener> getListenersForEventClass(Class<? extends SystemEvent> eventClass) {
        List<SystemEventListener> listeners;
        if (_systemEventListenerClassMap == null) {
            listeners = Collections.emptyList();
        } else {
            listeners = _systemEventListenerClassMap.get(eventClass);
            if (listeners == null) {
                listeners = Collections.emptyList();
            } else {
                listeners = Collections.unmodifiableList(listeners);
            }
        }

        return listeners;
    }

    /**
     * 
     * @return
     * 
     * @since 2.0
     */
    public UIComponent getNamingContainer() {
        // Starting with "this", return the closest component in the ancestry that is a NamingContainer 
        // or null if none can be found.
        UIComponent component = this;
        do {
            if (component instanceof NamingContainer) {
                return component;
            }

            component = component.getParent();
        } while (component != null);

        return null;
    }

    public abstract void setId(String id);

    /**
     * Define if the component is on the view or not.
     * <p>
     * This value is set in the following conditions:
     * </p>
     * <ul>
     * <li>Component / Facet added: if the parent isInView = true, 
     *     set it to true and all their children or facets,
     *     otherwise take no action</li>
     * <li>Component / Facet removed: if the parent isInView = false,
     *     set it to false and all their children or facets,
     *     otherwise take no action</li>
     * <ul>
     * @param isInView
     * 
     * @since 2.0
     */
    public void setInView(boolean isInView) {
        _inView = isInView;
    }

    /**
     * For JSF-framework internal use only. Don't call this method to add components to the component tree. Use
     * <code>parent.getChildren().add(child)</code> instead.
     */
    public abstract void setParent(UIComponent parent);

    /**
     * Returns the parent of the component. Children can be added to or removed from a component even if this method
     * returns null for the child.
     */
    public abstract UIComponent getParent();

    public abstract void setRendered(boolean rendered);

    public abstract String getRendererType();

    public abstract void setRendererType(String rendererType);

    public abstract boolean getRendersChildren();

    public Map<String, String> getResourceBundleMap() {
        if (_resourceBundleMap == null) {
            FacesContext context = getFacesContext();
            Locale locale = context.getViewRoot().getLocale();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            try {
                // looks for a ResourceBundle with a base name equal to the fully qualified class
                // name of the current UIComponent this and Locale equal to the Locale of the current UIViewRoot.
                _resourceBundleMap = new BundleMap(ResourceBundle.getBundle(getClass().getName(), locale, loader));
            } catch (MissingResourceException e) {
                // If no such bundle is found, and the component is a composite component
                if (this._isCompositeComponent()) {
                    // No need to check componentResource (the resource used to build the composite
                    // component instance) to null since it is already done on this._isCompositeComponent()
                    Resource componentResource = (Resource) getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                    // Let resourceName be the resourceName of the Resource for this composite component,
                    // replacing the file extension with ".properties"
                    int extensionIndex = componentResource.getResourceName().lastIndexOf('.');
                    String resourceName = (extensionIndex < 0 ? componentResource.getResourceName() : componentResource.getResourceName().substring(0, extensionIndex)) + ".properties";

                    // Let libraryName be the libraryName of the the Resource for this composite component.
                    // Call ResourceHandler.createResource(java.lang.String,java.lang.String), passing the derived
                    // resourceName and
                    // libraryName.
                    Resource bundleResource = context.getApplication().getResourceHandler().createResource(resourceName, componentResource.getLibraryName());

                    if (bundleResource != null) {
                        // If the resultant Resource exists and can be found, the InputStream for the resource
                        // is used to create a ResourceBundle. If either of the two previous steps for obtaining the
                        // ResourceBundle
                        // for this component is successful, the ResourceBundle is wrapped in a Map<String, String> and
                        // returned.
                        try {
                            _resourceBundleMap = new BundleMap(new PropertyResourceBundle(bundleResource.getInputStream()));
                        } catch (IOException e1) {
                            // Nothing happens, then resourceBundleMap is set as empty map
                        }
                    }
                }
                // Otherwise Collections.EMPTY_MAP is returned.
                if (_resourceBundleMap == null) {
                    _resourceBundleMap = Collections.emptyMap();
                }
            }
        }

        return _resourceBundleMap;
    }

    /**
     * @deprecated Replaced by getValueExpression
     */
    @Deprecated
    public abstract ValueBinding getValueBinding(String name);

    public ValueExpression getValueExpression(String name) {
        if (name == null) {
            throw new NullPointerException("name can not be null");
        }
        
        Map<String,Object> bindings = (Map<String,Object>) getStateHelper().
            get(PropertyKeys.bindings); 

        if (bindings == null) {
            if (!(this instanceof UIComponentBase)) {
                // if the component does not inherit from UIComponentBase and don't implements JSF 1.2 or later
                ValueBinding vb = getValueBinding(name);
                if (vb != null) {
                    //bindings = new HashMap<String, ValueExpression>();
                    ValueExpression ve = new _ValueBindingToValueExpression(vb);
                    getStateHelper().put(PropertyKeys.bindings , name,  ve);
                    return ve;
                }
            }
        } else {
            //return bindings.get(name);
            return (ValueExpression) bindings.get(name);
        }
        return null;
    }

    public abstract List<UIComponent> getChildren();

    public abstract int getChildCount();

    public abstract UIComponent findComponent(String expr);

    public abstract Map<String, UIComponent> getFacets();

    public abstract UIComponent getFacet(String name);

    public abstract Iterator<UIComponent> getFacetsAndChildren();

    public abstract void broadcast(FacesEvent event) throws AbortProcessingException;

    /**
     * {@inheritDoc}
     * 
     * @since 2.0
     */
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    public abstract void decode(FacesContext context);

    public abstract void encodeBegin(FacesContext context) throws IOException;

    public abstract void encodeChildren(FacesContext context) throws IOException;

    public abstract void encodeEnd(FacesContext context) throws IOException;

    public void encodeAll(FacesContext context) throws IOException {
        if (context == null) {
            throw new NullPointerException();
        }

        if (isRendered()) {
            this.encodeBegin(context);

            // rendering children
            if (this.getRendersChildren()) {
                this.encodeChildren(context);
            } // let children render itself
            else {
                if (this.getChildCount() > 0) {
                    for (UIComponent comp : this.getChildren()) {
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

    public abstract void queueEvent(FacesEvent event);

    public abstract void processRestoreState(FacesContext context, Object state);

    public abstract void processDecodes(FacesContext context);

    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        // The default implementation performs the following action. If the argument event is an instance of
        // AfterRestoreStateEvent,
        if (event instanceof PostRestoreStateEvent) {
            // call this.getValueExpression(java.lang.String) passing the literal string "binding"
            ValueExpression expression = getValueExpression("binding");

            // If the result is non-null, set the value of the ValueExpression to be this.
            if (expression != null) {
                expression.setValue(getFacesContext().getELContext(), this);
            }
        }
    }

    public abstract void processValidators(FacesContext context);

    public abstract void processUpdates(FacesContext context);

    public abstract java.lang.Object processSaveState(FacesContext context);

    public void subscribeToEvent(Class<? extends SystemEvent> eventClass, ComponentSystemEventListener componentListener) {
        // The default implementation creates an inner SystemEventListener instance that wraps argument
        // componentListener as the listener argument.
        SystemEventListener listener = new EventListenerWrapper(this, componentListener);

        // Make sure the map exists
        if (_systemEventListenerClassMap == null) {
            _systemEventListenerClassMap = new HashMap<Class<? extends SystemEvent>, List<SystemEventListener>>();
        }

        List<SystemEventListener> listeners = _systemEventListenerClassMap.get(eventClass);
        // Make sure the list for class exists
        if (listeners == null) {
            listeners = new _DeltaList<SystemEventListener>(new ArrayList<SystemEventListener>(2));
            _systemEventListenerClassMap.put(eventClass, listeners);
        }

        // Deal with contains? Spec is silent
        listeners.add(listener);
    }

    public void unsubscribeFromEvent(Class<? extends SystemEvent> eventClass,
            ComponentSystemEventListener componentListener) {
        /*
         * When doing the comparison to determine if an existing listener is equal to the argument componentListener
         * (and thus must be removed), the equals() method on the existing listener must be invoked, passing the
         * argument componentListener, rather than the other way around.
         * 
         * What is that supposed to mean? Are we supposed to keep an internal map of created listener wrappers? TODO:
         * Check with the EG what's the meaning of this, equals should be commutative -= Simon Lessard =-
         */
        SystemEventListener listener = new EventListenerWrapper(this, componentListener);

        getFacesContext().getApplication().unsubscribeFromEvent(eventClass, listener);
    }

    /**
     * The visit tree method, visit tree walks over a subtree and processes
     * the callback object to perform some operation on the subtree
     * <p>
     * there are some details in the implementation which according to the spec have
     * to be in place:
     * a) before calling the callback and traversing into the subtree  pushComponentToEL
     * has to be called
     * b) after the processing popComponentFromEL has to be performed to remove the component
     * from the el
     * </p>
     * <p>
     * The tree traversal optimizations are located in the visit context and can be replaced
     * via the VisitContextFactory in the faces-config factory section
     * </p>
     *
     * @param context the visit context which handles the processing details
     * @param callback the callback to be performed
     * @return false if the processing is not done true if we can shortcut
     * the visiting because we are done with everything
     * 
     * @since 2.0
     */
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        if (!isVisitable(context)) {
            return false;
        }

        pushComponentToEL(context.getFacesContext(), this);
        try {
            VisitResult res = context.invokeVisitCallback(this, callback);
            switch (res) {
                //we are done nothing has to be processed anymore
                case COMPLETE:
                    return true;

                case REJECT:
                    return false;

                //accept
                default:
                    boolean visitResult = false;
                    List <UIComponent> children = getChildren();
                    if(children == null || children.isEmpty()) {
                        return visitResult;
                    }
                    for (UIComponent child : children) {
                        visitResult = child.visitTree(context, callback);
                        if (visitResult) {
                            return visitResult;
                        }
                    }
                    return visitResult;
            }
        } finally {
            //all components must call popComponentFromEl after visiting is finished
            popComponentFromEL(context.getFacesContext());
        }
    }

    protected abstract FacesContext getFacesContext();

    protected abstract Renderer getRenderer(FacesContext context);

    /**
     * Note that id, clientId properties
     * never change its value after the component is populated,
     * so we don't need to store it on StateHelper or restore it when
     * initialStateMarked == true
     * (Note that rendererType is suspicious, in theory this field is
     * initialized on constructor, but on 1.1 and 1.2 is saved and restored,
     * so to keep backward behavior we put it on StateHelper )
     *  
     * Also, facesListeners can't be wrapped on StateHelper because it
     * needs to handle PartialStateHolder instances when it is saved and
     * restored and this interface does not implement PartialStateHolder,
     * so we can't propagate calls to markInitialState and clearInitialState,
     * in other words, the List wrapped by StateHelper does not handle
     * PartialStateHolder items.
     * 
     * "bindings" map does not need to deal with PartialStateHolder instances,
     *  so we can use StateHelper feature (handle delta for this map or in
     *  other words track add/removal from bindings map as delta).
     */
    enum PropertyKeys
    {
        rendered,
        rendererType,
        attributesMap,
        bindings,
        facesListeners
    }

    protected StateHelper getStateHelper() {
        return getStateHelper(true);
    }

    /**
     * returns a delta state saving enabled state helper
     * for the current component
     * @param create if true a state helper is created if not already existing
     * @return an implementation of the StateHelper interface or null if none exists and create is set to false
     */
    protected StateHelper getStateHelper(boolean create) {
        if(_stateHelper != null) {
            return _stateHelper;
        }
        if(create) {
            _stateHelper = new _DeltaStateHelper(this);
        }
        return _stateHelper;
    }

    @SuppressWarnings("unchecked")
    public void popComponentFromEL(FacesContext context) {
        Map<Object, Object> contextAttributes = context.getAttributes();        
        
        // Pop the current UIComponent from the FacesContext attributes map so that the previous 
        // UIComponent, if any, becomes the current component.
        Deque<UIComponent> componentStack = (Deque<UIComponent>) contextAttributes.get(UIComponent._COMPONENT_STACK);
        
        UIComponent newCurrent = null;
        if (componentStack != null && !componentStack.isEmpty())
        {
            newCurrent = componentStack.pop();
        }
        UIComponent oldCurrent = (UIComponent)contextAttributes.put(UIComponent.CURRENT_COMPONENT, newCurrent);
        
        if (oldCurrent != null && oldCurrent._isCompositeComponent())
        {
            // Recalculate the current composite component
            if (newCurrent != null)
            {
                if (newCurrent._isCompositeComponent())
                {
                    contextAttributes.put(UIComponent.CURRENT_COMPOSITE_COMPONENT, newCurrent);
                }
                else
                {
                    for (UIComponent component : componentStack)
                    {
                        if (component._isCompositeComponent())
                        {
                            contextAttributes.put(UIComponent.CURRENT_COMPOSITE_COMPONENT, component);
                            break;
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void pushComponentToEL(FacesContext context, UIComponent component) {
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
 
        if (component._isCompositeComponent())
        {
            contextAttributes.put(UIComponent.CURRENT_COMPOSITE_COMPONENT, component);
        }
    }

    /**
     * @since 1.2
     */
    public int getFacetCount() {
        // not sure why the RI has this method in both
        // UIComponent and UIComponentBase
        Map<String, UIComponent> facets = getFacets();
        return facets == null ? 0 : facets.size();
    }

    private boolean _isCompositeComponent() {
        //moved to the static method
        return UIComponent.isCompositeComponent(this);
    }
    
    private static class BundleMap implements Map<String, String> {

        private ResourceBundle _bundle;
        private List<String> _values;

        public BundleMap(ResourceBundle bundle) {
            _bundle = bundle;
        }

        // Optimized methods
        public String get(Object key) {
            try {
                return (String) _bundle.getObject(key.toString());
            } catch (Exception e) {
                return "???" + key + "???";
            }
        }

        public boolean isEmpty() {
            return !_bundle.getKeys().hasMoreElements();
        }

        public boolean containsKey(Object key) {
            try {
                return _bundle.getObject(key.toString()) != null;
            } catch (MissingResourceException e) {
                return false;
            }
        }

        // Unoptimized methods
        public Collection<String> values() {
            if (_values == null) {
                _values = new ArrayList<String>();
                for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements();) {
                    String v = _bundle.getString(enumer.nextElement());
                    _values.add(v);
                }
            }
            return _values;
        }

        public int size() {
            return values().size();
        }

        public boolean containsValue(Object value) {
            return values().contains(value);
        }

        public Set<Map.Entry<String, String>> entrySet() {
            Set<Entry<String, String>> set = new HashSet<Entry<String, String>>();
            for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements();) {
                final String k = enumer.nextElement();
                set.add(new Map.Entry<String, String>() {

                    public String getKey() {
                        return k;
                    }

                    public String getValue() {
                        return (String) _bundle.getObject(k);
                    }

                    public String setValue(String value) {
                        throw new UnsupportedOperationException();
                    }
                });
            }

            return set;
        }

        public Set<String> keySet() {
            Set<String> set = new HashSet<String>();
            for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements();) {
                set.add(enumer.nextElement());
            }
            return set;
        }

        // Unsupported methods
        public String remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map<? extends String, ? extends String> t) {
            throw new UnsupportedOperationException();
        }

        public String put(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    static class EventListenerWrapper implements SystemEventListener, PartialStateHolder {

        private Class<?> componentClass;
        private ComponentSystemEventListener listener;
        
        public EventListenerWrapper()
        {
            //need a no-arg constructor for state saving purposes
            super();
        }
        
        /**
         * Note we have two cases:
         * 
         * 1. listener is an instance of UIComponent. In this case we cannot save and restore
         *    it because we need to point to the real component, but we can assume the instance
         *    is the same because UIComponent.subscribeToEvent says so. Also take into account
         *    this case is the reason why we need a wrapper for UIComponent.subscribeToEvent
         * 2. listener is an instance of ComponentSystemEventListener but not from UIComponent.
         *    In this case, the instance could implement StateHolder, PartialStateHolder or do
         *    implement anything, so we have to deal with that case as usual.
         * 
         * @param component
         * @param listener
         */
        public EventListenerWrapper(UIComponent component, ComponentSystemEventListener listener) {
            assert component != null;
            assert listener != null;

            this.componentClass = component.getClass();
            this.listener = listener;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
            {
                return true;
            }
            else if (o instanceof EventListenerWrapper)
            {
                EventListenerWrapper other = (EventListenerWrapper) o;
                return componentClass.equals(other.componentClass) && listener.equals(other.listener);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return componentClass.hashCode() + listener.hashCode();
        }

        @Override
        public boolean isListenerForSource(Object source)
        {
            // and its implementation of SystemEventListener.isListenerForSource(java.lang.Object) must return true
            // if the instance class of this UIComponent is assignable from the argument to isListenerForSource.

            return source.getClass().isAssignableFrom(componentClass);
        }

        @Override
        public void processEvent(SystemEvent event)
        {
            // This inner class must call through to the argument componentListener in its implementation of
            // SystemEventListener.processEvent(javax.faces.event.SystemEvent)

            assert event instanceof ComponentSystemEvent;

            listener.processEvent((ComponentSystemEvent) event);
        }

        @Override
        public void clearInitialState()
        {
            if (!(listener instanceof UIComponent) && listener instanceof PartialStateHolder)
            {
                ((PartialStateHolder)listener).clearInitialState();
            }
        }

        @Override
        public boolean initialStateMarked()
        {
            if (!(listener instanceof UIComponent) && listener instanceof PartialStateHolder)
            {
                ((PartialStateHolder)listener).initialStateMarked();
            }
            return false;
        }

        @Override
        public void markInitialState()
        {
            if (!(listener instanceof UIComponent) && listener instanceof PartialStateHolder)
            {
                ((PartialStateHolder)listener).markInitialState();
            }
        }

        @Override
        public boolean isTransient()
        {
            if (listener instanceof StateHolder)
            {
                return ((StateHolder)listener).isTransient();
            }            
            return false;
        }

        @Override
        public void restoreState(FacesContext context, Object state)
        {
            //TODO: Delta
            Object[] values = (Object[]) state;
            componentClass = (Class) values[0];
            listener = values[1] == null ? 
                    UIComponent.getCurrentComponent(context) : 
                        (ComponentSystemEventListener) UIComponentBase.restoreAttachedState(context, values[1]);
        }

        @Override
        public Object saveState(FacesContext context)
        {
            //TODO: Delta
            Object[] state = new Object[2];
            state[0] = componentClass;
            if (!(listener instanceof UIComponent))
            {
                state[1] = UIComponentBase.saveAttachedState(context, listener);
            }
            return state;                
        }

        @Override
        public void setTransient(boolean newTransientValue)
        {
            if (listener instanceof StateHolder)
            {
                ((StateHolder)listener).setTransient(newTransientValue);
            }            
        }
    }
}