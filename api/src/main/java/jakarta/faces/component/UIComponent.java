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
package jakarta.faces.component;

import org.apache.myfaces.core.api.shared.lang.ClassUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.Resource;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PostRestoreStateEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.event.SystemEventListenerHolder;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 *
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">J
 * SF Specification</a>
 */
@JSFComponent(type = "jakarta.faces.Component", family = "jakarta.faces.Component",
              desc = "abstract base component", configExcluded = true)
public abstract class UIComponent
        implements PartialStateHolder, TransientStateHolder, SystemEventListenerHolder, ComponentSystemEventListener
{
    // TODO: Reorder methods, this class is a mess
    /**
     * Constant used in component attribute map to retrieve the BeanInfo of a composite
     * component.
     *
     * @see jakarta.faces.view.ViewDeclarationLanguage#getComponentMetadata(FacesContext, Resource)
     * @see jakarta.faces.view.ViewDeclarationLanguage#retargetAttachedObjects(FacesContext, UIComponent, List)
     * @see jakarta.faces.view.ViewDeclarationLanguage#retargetMethodExpressions(FacesContext, UIComponent)
     * @see jakarta.faces.application.Application#createComponent(FacesContext, Resource)
     */
    public static final String BEANINFO_KEY = "jakarta.faces.component.BEANINFO_KEY";

    /**
     * Constant used in BeanInfo descriptor as a key for retrieve an alternate component type
     * for create the composite base component. 
     *
     * @see jakarta.faces.application.Application#createComponent(FacesContext, Resource)
     */
    public static final String COMPOSITE_COMPONENT_TYPE_KEY = "jakarta.faces.component.COMPOSITE_COMPONENT_TYPE";

    /**
     * Constant used to define the facet inside this component that store the component hierarchy
     * generated by a composite component implementation, and then rendered. In other words, 
     * note that direct children of a component are not rendered, instead components inside 
     * this face are rendered.
     */
    public static final String COMPOSITE_FACET_NAME = "jakarta.faces.component.COMPOSITE_FACET_NAME";

    /**
     * This constant has two usages. The first one is in component attribute map to identify the 
     * facet name under this component is child of its parent. The second one is on BeanInfo descriptor
     * as a key for a Map&lt;String, PropertyDescriptor&gt; that contains metadata information defined
     * by composite:facet tag and composite:implementation(because this one fills the facet referenced
     * by COMPOSITE_FACET_NAME constant). 
     */
    public static final String FACETS_KEY = "jakarta.faces.component.FACETS_KEY";

    /**
     * Constant used in component attribute map to store the {@link jakarta.faces.view.Location} object
     * where the definition of this component is.
     */
    public static final String VIEW_LOCATION_KEY = "jakarta.faces.component.VIEW_LOCATION_KEY";

    public static final String ATTRS_WITH_DECLARED_DEFAULT_VALUES
            = "jakarta.faces.component.ATTR_NAMES_WITH_DEFAULT_VALUES";

    /**
     * The key under which the component stack is stored in the FacesContext.
     * ATTENTION: this constant is duplicate in CompositeComponentExpressionUtils.
     */
    private static final String _COMPONENT_STACK = "componentStack:" + UIComponent.class.getName();

    private static final String _CURRENT_COMPOSITE_COMPONENT_KEY = "compositeComponent:" + UIComponent.class.getName();

    Map<Class<? extends SystemEvent>, List<SystemEventListener>> _systemEventListenerClassMap;

    /**
     * Used to cache the map created using getResourceBundleMap() method, since this method could be called several
     * times when rendering the composite component. This attribute may not be serialized, so transient is used (There
     * are some very few special cases when UIComponent instances are serializable like t:schedule, so it is better if
     * transient is used).
     */
    private transient Map<String, String> _resourceBundleMap = null;
    private boolean _inView = false;
    private _DeltaStateHelper _stateHelper = null;

    /**
     * In Faces 2.0 bindings map was deprecated, and replaced with a map
     * inside stateHelper. We need this one here because stateHelper needs
     * to be implemented from here and internally it depends from this property.
     */
    private boolean _initialStateMarked = false;

    public UIComponent()
    {
    }

    public abstract Map<String, Object> getAttributes();
    
    /**
     * @since 2.2
     * @return 
     */
    public Map<String,Object> getPassThroughAttributes()
    {
        return getPassThroughAttributes(true);
    }
    
    /**
     * @since 2.2
     * @param create
     * @return A {@code Map} instance, or {@code null}.
     */
    public Map<String,Object> getPassThroughAttributes(boolean create)
    {
        return Collections.emptyMap();
    }

    /**
     *
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
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
     * @throws jakarta.faces.FacesException
     */
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback)
            throws FacesException
    {
        Assert.notNull(context, "context");
        Assert.notNull(clientId, "clientId");
        Assert.notNull(callback, "callback");

        pushComponentToEL(context, this);
        try
        {
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
            // [perf] Use getFacetsAndChildren() is nicer but this one prevents
            // create 1 iterator per component class that does not have
            // facets attached, which is a common case. 
            if (this.getFacetCount() > 0)
            {
                for (Iterator<UIComponent> it = this.getFacets().values().iterator(); !found && it.hasNext(); )
                {
                    found = it.next().invokeOnComponent(context, clientId, callback);
                }                
            }
            if (this.getChildCount() > 0)
            {
                for (int i = 0, childCount = getChildCount(); !found && (i < childCount); i++)
                {
                    UIComponent child = getChildren().get(i);
                    found = child.invokeOnComponent(context, clientId, callback);
                }
            }
            return found;
        }
        finally
        {
            //all components must call popComponentFromEl after visiting is finished
            popComponentFromEL(context);
        }
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
    public static boolean isCompositeComponent(UIComponent component)
    {
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
    public boolean isInView()
    {
        return _inView;
    }

    public abstract boolean isRendered();

    @Override
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
    protected boolean isVisitable(VisitContext context)
    {
        Collection<VisitHint> hints = context.getHints();

        if (hints.contains(VisitHint.SKIP_TRANSIENT) && this.isTransient())
        {
            return false;
        }

        if (hints.contains(VisitHint.SKIP_UNRENDERED) && !this.isRendered())
        {
            return false;
        }

        //executable cannot be handled here because we do not have any method to determine
        //whether a component is executable or not, this seems to be a hole in the spec!
        //but we can resolve it on ppr context level, where it is needed!
        //maybe in the long run we can move it down here, if it makes sense

        return true;
    }

    public void setValueExpression(String name, ValueExpression expression)
    {
        Assert.notNull(name, "name");

        if (name.equals("id"))
        {
            throw new IllegalArgumentException("Can't set a ValueExpression for the 'id' property.");
        }
        if (name.equals("parent"))
        {
            throw new IllegalArgumentException("Can't set a ValueExpression for the 'parent' property.");
        }

        if (expression == null)
        {
            getStateHelper().remove(PropertyKeys.bindings, name);
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

            getStateHelper().put(PropertyKeys.bindings, name, expression);
        }
    }

    public String getClientId()
    {
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
    public static UIComponent getCompositeComponentParent(UIComponent component)
    {
        if (component == null)
        {
            return null;
        }
        UIComponent parent = component;

        do
        {
            parent = parent.getParent();
            if (parent != null && UIComponent.isCompositeComponent(parent))
            {
                return parent;
            }
        } while (parent != null);
        return null;
    }

    /**
     * @since 1.2
     */
    public String getContainerClientId(FacesContext ctx)
    {
        Assert.notNull(ctx, "ctx");

        return getClientId(ctx);
    }

    /**
     *
     * @param context
     * @return
     *
     * @since 2.0
     */
    public static UIComponent getCurrentComponent(FacesContext context)
    {
        List<UIComponent> componentStack
                = (List<UIComponent>) context.getAttributes().get(UIComponent._COMPONENT_STACK);
        if (componentStack != null && !componentStack.isEmpty())
        {
            return componentStack.get(componentStack.size()-1);
        }

        return null;
    }

    /**
     *
     * @param context
     * @return
     *
     * @since 2.0
     */
    public static UIComponent getCurrentCompositeComponent(FacesContext context)
    {
        return (UIComponent) context.getAttributes().get(UIComponent._CURRENT_COMPOSITE_COMPONENT_KEY);
    }

    public abstract String getFamily();

    public abstract String getId();

    @Override
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

    /**
     *
     * @return
     *
     * @since 2.0
     */
    public UIComponent getNamingContainer()
    {
        // Starting with "this", return the closest component in the ancestry that is a NamingContainer 
        // or null if none can be found.
        UIComponent component = this;
        do
        {
            if (component instanceof NamingContainer)
            {
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
     * </ul>
     * @param isInView
     *
     * @since 2.0
     */
    public void setInView(boolean isInView)
    {
        _inView = isInView;
    }

    /**
     * For Faces-framework internal use only. Don't call this method to add components to the component tree. Use
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

    public Map<String, String> getResourceBundleMap()
    {
        if (_resourceBundleMap == null)
        {
            FacesContext context = getFacesContext();
            Locale locale = context.getViewRoot().getLocale();
            ClassLoader loader = ClassUtils.getContextClassLoader();

            try
            {
                ResourceBundle.Control bundleControl = (ResourceBundle.Control) context.getExternalContext()
                        .getApplicationMap().get("org.apache.myfaces.RESOURCE_BUNDLE_CONTROL");

                // looks for a ResourceBundle with a base name equal to the fully qualified class
                // name of the current UIComponent this and Locale equal to the Locale of the current UIViewRoot.
                if (bundleControl == null)
                {
                    _resourceBundleMap = new _BundleMap(
                            ResourceBundle.getBundle(getClass().getName(), locale, loader));
                }
                else
                {
                    _resourceBundleMap = new _BundleMap(
                            ResourceBundle.getBundle(getClass().getName(), locale, loader, bundleControl));
                }
            }
            catch (MissingResourceException e)
            {
                // If no such bundle is found, and the component is a composite component
                if (this._isCompositeComponent())
                {
                    // No need to check componentResource (the resource used to build the composite
                    // component instance) to null since it is already done on this._isCompositeComponent()
                    Resource componentResource = (Resource) getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                    // Let resourceName be the resourceName of the Resource for this composite component,
                    // replacing the file extension with ".properties"
                    int extensionIndex = componentResource.getResourceName().lastIndexOf('.');
                    String resourceName = (extensionIndex < 0
                            ? componentResource.getResourceName()
                            : componentResource.getResourceName().substring(0, extensionIndex)) + ".properties";

                    // Let libraryName be the libraryName of the the Resource for this composite component.
                    // Call ResourceHandler.createResource(java.lang.String,java.lang.String), passing the derived
                    // resourceName and
                    // libraryName.
                    Resource bundleResource = context.getApplication().getResourceHandler()
                            .createResource(resourceName, componentResource.getLibraryName());

                    if (bundleResource != null)
                    {
                        // If the resultant Resource exists and can be found, the InputStream for the resource
                        // is used to create a ResourceBundle. If either of the two previous steps for obtaining the
                        // ResourceBundle
                        // for this component is successful, the ResourceBundle is wrapped in a Map<String, String> and
                        // returned.
                        try
                        {
                            _resourceBundleMap
                                    = new _BundleMap(new PropertyResourceBundle(bundleResource.getInputStream()));
                        }
                        catch (IOException e1)
                        {
                            // Nothing happens, then resourceBundleMap is set as empty map
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

    public ValueExpression getValueExpression(String name)
    {
        Assert.notNull(name, "name");

        Map<String, Object> bindings = (Map<String, Object>) getStateHelper().get(PropertyKeys.bindings);
        if (bindings != null)
        {
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
    @Override
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    public abstract void decode(FacesContext context);

    public abstract void encodeBegin(FacesContext context) throws IOException;

    public abstract void encodeChildren(FacesContext context) throws IOException;

    public abstract void encodeEnd(FacesContext context) throws IOException;

    public void encodeAll(FacesContext context) throws IOException
    {
        Assert.notNull(context, "context");

        pushComponentToEL(context, this);
        try
        {
            if (!isRendered())
            {
                return;
            }
        }
        finally
        {
            popComponentFromEL(context);
        }

        this.encodeBegin(context);

        // rendering children
        if (this.getRendersChildren())
        {
            this.encodeChildren(context);
        } // let children render itself
        else
        {
            if (this.getChildCount() > 0)
            {
                for (int i = 0; i < this.getChildCount(); i++)
                {
                    UIComponent comp = this.getChildren().get(i);
                    comp.encodeAll(context);
                }
            }
        }
        this.encodeEnd(context);
    }

    protected abstract void addFacesListener(FacesListener listener);

    protected abstract FacesListener[] getFacesListeners(Class clazz);

    protected abstract void removeFacesListener(FacesListener listener);

    public abstract void queueEvent(FacesEvent event);

    public abstract void processRestoreState(FacesContext context, Object state);

    public abstract void processDecodes(FacesContext context);

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException
    {
        // The default implementation performs the following action. If the argument event is an instance of
        // AfterRestoreStateEvent,
        if (event instanceof PostRestoreStateEvent)
        {

            // call this.getValueExpression(java.lang.String) passing the literal string "binding"
            ValueExpression expression = getValueExpression("binding");

            // If the result is non-null, set the value of the ValueExpression to be this.
            if (expression != null)
            {
                expression.setValue(getFacesContext().getELContext(), this);
            }

            //we issue a PostRestoreStateEvent, because the spec clearly states what UIComponent is allowed to do
            //the main issue is that the spec does not say anything about a global dispatch on this level
            //but a quick blackbox test against the ri revealed that the event clearly is dispatched
            //at restore level for every component so we either issue it here or in UIViewRoot and/or the facelet
            // and jsp restore state triggers, a central point is preferrble so we do it here
            //TODO ask the EG the spec clearly contradicts blackbox RI behavior here 

            //getFacesContext().getApplication().publishEvent(getFacesContext(),
            // PostRestoreStateEvent.class, UIComponent.class, this);
            
            // Faces 2.2 vdl.createComponent() requires special handling to refresh
            // dynamic parts when refreshing is done. The only way to do it is 
            // attaching a listener to PostRestoreStateEvent, so we need to do this
            // invocation here.
            // Do it inside UIComponent.processEvent() is better because in facelets
            // UILeaf we can skip this part just overriding the method.
            
            List<SystemEventListener> listeners = this.getListenersForEventClass(PostRestoreStateEvent.class);
            if (!listeners.isEmpty())
            {
                for (int i  = 0, size = listeners.size(); i < size; i++)
                {
                    SystemEventListener listener = listeners.get(i);
                    if (listener.isListenerForSource(this))
                    {
                        // Check if the listener points again to the component, to
                        // avoid StackoverflowException
                        boolean shouldProcessEvent = true;
                        if (listener instanceof _EventListenerWrapper && 
                            ((_EventListenerWrapper) listener).getListenerCapability() ==
                                _EventListenerWrapper.LISTENER_TYPE_COMPONENT)
                        {
                            shouldProcessEvent = false;
                        }
                        if (shouldProcessEvent)
                        {
                            listener.processEvent(event);
                        }
                    }
                }
            }
        }
    }

    public abstract void processValidators(FacesContext context);

    public abstract void processUpdates(FacesContext context);

    public abstract java.lang.Object processSaveState(FacesContext context);

    public void subscribeToEvent(Class<? extends SystemEvent> eventClass,
                                 ComponentSystemEventListener componentListener)
    {
        Assert.notNull(eventClass, "eventClass");
        Assert.notNull(componentListener, "componentListener");

        // The default implementation creates an inner SystemEventListener instance that wraps argument
        // componentListener as the listener argument.

        SystemEventListener listener = new _EventListenerWrapper(this, componentListener);

        // Make sure the map exists
        if (_systemEventListenerClassMap == null)
        {
            _systemEventListenerClassMap = new HashMap<>(4, 1f);
        }

        List<SystemEventListener> listeners = _systemEventListenerClassMap.get(eventClass);
        if (listeners == null)
        {
            listeners = new _DeltaList<>(3);
            _systemEventListenerClassMap.put(eventClass, listeners);
        }

        // Deal with contains? Spec is silent
        listeners.add(listener);
    }

    public void unsubscribeFromEvent(Class<? extends SystemEvent> eventClass,
                                     ComponentSystemEventListener componentListener)
    {
        /*
         * When doing the comparison to determine if an existing listener is equal to the argument componentListener
         * (and thus must be removed), the equals() method on the existing listener must be invoked, passing the
         * argument componentListener, rather than the other way around.
         * 
         * -=Simon Lessard=- What is that supposed to mean? Are we supposed to keep
         * an internal map of created listener wrappers?
         * -= Leonardo Uribe=- Yes, it is supposed a wrapper should be used to hold listener references, to prevent
         * serialize component instances on the state.
         */
        Assert.notNull(eventClass, "eventClass");
        Assert.notNull(componentListener, "componentListener");

        if (_systemEventListenerClassMap != null)
        {
            List<SystemEventListener> listeners = _systemEventListenerClassMap.get(eventClass);
            if (listeners != null && !listeners.isEmpty())
            {
                for (Iterator<SystemEventListener> it = listeners.iterator(); it.hasNext(); )
                {
                    ComponentSystemEventListener listener
                            = ((_EventListenerWrapper) it.next()).getComponentSystemEventListener();
                    if (listener != null && listener.equals(componentListener))
                    {
                        it.remove();
                        break;
                    }
                }
            }
        }
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
    public boolean visitTree(VisitContext context, VisitCallback callback)
    {
        try
        {
            pushComponentToEL(context.getFacesContext(), this);

            if (!isVisitable(context))
            {
                return false;
            }

            VisitResult res = context.invokeVisitCallback(this, callback);
            switch (res)
            {
                //we are done nothing has to be processed anymore
                case COMPLETE:
                    return true;

                case REJECT:
                    return false;

                //accept
                default:
                    if (getFacetCount() > 0)
                    {
                        for (UIComponent facet : getFacets().values())
                        {
                            if (facet.visitTree(context, callback))
                            {
                                return true;
                            }
                        }
                    }
                    int childCount = getChildCount();
                    if (childCount > 0)
                    {
                        for (int i = 0; i < childCount; i++)
                        {
                            UIComponent child = getChildren().get(i);
                            if (child.visitTree(context, callback))
                            {
                                return true;
                            }
                        }
                    }
                    return false;
            }
        }
        finally
        {
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
        facesListeners,
        passThroughAttributesMap
    }

    protected StateHelper getStateHelper()
    {
        return getStateHelper(true);
    }

    /**
     * returns a delta state saving enabled state helper
     * for the current component
     * @param create if true a state helper is created if not already existing
     * @return an implementation of the StateHelper interface or null if none exists and create is set to false
     */
    protected StateHelper getStateHelper(boolean create)
    {
        if (_stateHelper != null)
        {
            return _stateHelper;
        }
        if (create)
        {
            _stateHelper = new _DeltaStateHelper(this);
        }
        return _stateHelper;
    }

    public TransientStateHelper getTransientStateHelper()
    {
        return getTransientStateHelper(true);
    }

    public TransientStateHelper getTransientStateHelper(boolean create)
    {
        if (_stateHelper != null)
        {
            return _stateHelper;
        }
        if (create)
        {
            _stateHelper = new _DeltaStateHelper(this);
        }
        return _stateHelper;
    }

    @Override
    public void restoreTransientState(FacesContext context, Object state)
    {
        getTransientStateHelper().restoreTransientState(context, state);
    }

    @Override
    public Object saveTransientState(FacesContext context)
    {
        return getTransientStateHelper().saveTransientState(context);
    }

    @SuppressWarnings("unchecked")
    public void popComponentFromEL(FacesContext context)
    {
        Map<Object, Object> contextAttributes = context.getAttributes();

        // Pop the current UIComponent from the FacesContext attributes map so that the previous 
        // UIComponent, if any, becomes the current component.
        List<UIComponent> componentStack
                = (List<UIComponent>) contextAttributes.get(UIComponent._COMPONENT_STACK);

        UIComponent oldCurrent = null;
        if (componentStack != null && !componentStack.isEmpty())
        {
            int componentIndex = componentStack.lastIndexOf(this);
            if (componentIndex >= 0)
            {
                for (int i = componentStack.size()-1; i >= componentIndex ; i--)
                {
                    oldCurrent = componentStack.remove(componentStack.size()-1);
                }
            }
            else
            {
                return;
            }
        }

        if (oldCurrent != null && oldCurrent._isCompositeComponent())
        {
            // Recalculate the current composite component
            UIComponent previousCompositeComponent = null;
            for (int i = componentStack.size() - 1; i >= 0; i--)
            {
                UIComponent component = componentStack.get(i);
                if (component._isCompositeComponent())
                {
                    previousCompositeComponent = component;
                    break;
                }
            }
            contextAttributes.put(UIComponent._CURRENT_COMPOSITE_COMPONENT_KEY, previousCompositeComponent);
        }
    }

    @SuppressWarnings("unchecked")
    public void pushComponentToEL(FacesContext context, UIComponent component)
    {
        if (component == null)
        {
            component = this;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();

        List<UIComponent> componentStack = (List<UIComponent>) contextAttributes.get(UIComponent._COMPONENT_STACK);
        if (componentStack == null)
        {
            componentStack = new ArrayList<>();
            contextAttributes.put(UIComponent._COMPONENT_STACK, componentStack);
        }

        componentStack.add(component);
        if (component._isCompositeComponent())
        {
            contextAttributes.put(UIComponent._CURRENT_COMPOSITE_COMPONENT_KEY, component);
        }
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

    private boolean _isCompositeComponent()
    {
        //moved to the static method
        return UIComponent.isCompositeComponent(this);
    }
    
    boolean isCachedFacesContext()
    {
        return false;
    }

    // Dummy method to prevent cast for UIComponentBase when caching
    void setCachedFacesContext(FacesContext facesContext)
    {
    }
}
