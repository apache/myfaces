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

package org.apache.myfaces.test.mock;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.application.ResourceHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.ListenersFor;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.event.SystemEventListenerHolder;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.test.mock.resource.MockResourceHandler;

/**
 * <p>Mock implementation of <code>Application</code> that includes the semantics
 * added by JavaServer Faces 2.0.</p>
 * 
 * @author Leonardo Uribe
 * @since 1.0.0
 *
 */
public abstract class MockApplication20 extends MockApplication12
{

    // ------------------------------------------------------------ Constructors

    public MockApplication20()
    {
        super();

        // install the 2.0-ViewHandler-Mock
        this.setViewHandler(new MockViewHandler20());
        this.setResourceHandler(new MockResourceHandler());
    }

    private static class SystemListenerEntry
    {
        private List<SystemEventListener> _lstSystemEventListener;
        private Map<Class<?>, List<SystemEventListener>> _sourceClassMap;

        public SystemListenerEntry()
        {
        }

        public void addListener(SystemEventListener listener)
        {
            assert listener != null;

            addListenerNoDuplicate(getAnySourceListenersNotNull(), listener);
        }

        public void addListener(SystemEventListener listener, Class<?> source)
        {
            assert listener != null;

            if (source == null)
            {
                addListener(listener);
            }
            else
            {
                addListenerNoDuplicate(
                        getSpecificSourceListenersNotNull(source), listener);
            }
        }

        public void removeListener(SystemEventListener listener)
        {
            assert listener != null;

            if (_lstSystemEventListener != null)
            {
                _lstSystemEventListener.remove(listener);
            }
        }

        public void removeListener(SystemEventListener listener,
                Class<?> sourceClass)
        {
            assert listener != null;

            if (sourceClass == null)
            {
                removeListener(listener);
            }
            else
            {
                if (_sourceClassMap != null)
                {
                    List<SystemEventListener> listeners = _sourceClassMap
                            .get(sourceClass);
                    if (listeners != null)
                    {
                        listeners.remove(listener);
                    }
                }
            }
        }

        public void publish(Class<? extends SystemEvent> systemEventClass,
                Class<?> classSource, Object source, SystemEvent event)
        {
            if (source != null && _sourceClassMap != null)
            {
                event = _traverseListenerList(_sourceClassMap.get(classSource),
                        systemEventClass, source, event);
            }

            _traverseListenerList(_lstSystemEventListener, systemEventClass,
                    source, event);
        }

        private void addListenerNoDuplicate(
                List<SystemEventListener> listeners,
                SystemEventListener listener)
        {
            if (!listeners.contains(listener))
            {
                listeners.add(listener);
            }
        }

        private synchronized List<SystemEventListener> getAnySourceListenersNotNull()
        {
            if (_lstSystemEventListener == null)
            {
                /*
                 * TODO: Check if modification occurs often or not, might have to use a synchronized list instead.
                 * 
                 * Registrations found:
                 */
                _lstSystemEventListener = new CopyOnWriteArrayList<SystemEventListener>();
            }

            return _lstSystemEventListener;
        }

        private synchronized List<SystemEventListener> getSpecificSourceListenersNotNull(
                Class<?> sourceClass)
        {
            if (_sourceClassMap == null)
            {
                _sourceClassMap = new ConcurrentHashMap<Class<?>, List<SystemEventListener>>();
            }

            List<SystemEventListener> list = _sourceClassMap.get(sourceClass);
            if (list == null)
            {
                /*
                 * TODO: Check if modification occurs often or not, might have to use a synchronized list instead.
                 * 
                 * Registrations found:
                 */
                list = new CopyOnWriteArrayList<SystemEventListener>();
                _sourceClassMap.put(sourceClass, list);
            }

            return list;
        }
    }

    // ------------------------------------------------------ Instance Variables

    private static final Logger log = Logger.getLogger(MockApplication20.class.getName());

    private final Map<Class<? extends SystemEvent>, SystemListenerEntry> _systemEventListenerClassMap = 
        new ConcurrentHashMap<Class<? extends SystemEvent>, SystemListenerEntry>();

    private Map<String, String> _defaultValidatorsIds = new HashMap<String, String>();

    private ProjectStage _projectStage;

    private final Map<String, Class<?>> _behaviorClassMap = new ConcurrentHashMap<String, Class<?>>();

    private final Map<String, Class<?>> _validatorClassMap = new ConcurrentHashMap<String, Class<?>>();

    private ResourceHandler _resourceHandler;

    // ----------------------------------------------------- Mock Object Methods

    public Map<String, String> getDefaultValidatorInfo()
    {
        return Collections.unmodifiableMap(_defaultValidatorsIds);
    }

    private void _handleAnnotations(FacesContext context, Object inspected, UIComponent component)
    {   
        // determine the ProjectStage setting via the given FacesContext
        // note that a local getProjectStage() could cause problems in wrapped environments
        boolean isProduction = context.isProjectStage(ProjectStage.Production);
        
        Class<?> inspectedClass = inspected.getClass();
        _handleListenerForAnnotations(context, inspected, inspectedClass, component, isProduction);

        _handleResourceDependencyAnnotations(context, inspectedClass, component, isProduction);
    }
    
    private void _handleListenerForAnnotations(FacesContext context, Object inspected, Class<?> inspectedClass, 
        UIComponent component, boolean isProduction)
    {
        List<ListenerFor> listenerForList = null;
        
        if(listenerForList == null) //not in production or the class hasn't been inspected yet
        {
            ListenerFor listener = inspectedClass.getAnnotation(ListenerFor.class);
            ListenersFor listeners = inspectedClass.getAnnotation(ListenersFor.class);
            if(listener != null || listeners != null)
            {
                //listeners were found using one or both annotations, create and build a new list
                listenerForList = new ArrayList<ListenerFor>();
                
                if(listener != null)
                {
                    listenerForList.add(listener);
                }
                
                if(listeners != null)
                {
                    listenerForList.addAll(Arrays.asList(listeners.value()));
                }
            }
        }        
 
        if (listenerForList != null) //listeners were found through inspection or from cache, handle them
        {
            for (ListenerFor listenerFor : listenerForList)
            {
                _handleListenerFor(context, inspected, component, listenerFor);
            }
        }
    }

    private void _handleListenerFor(FacesContext context, Object inspected, UIComponent component,
                                    ListenerFor annotation)
    {
        // If this annotation is not present on the class in question, no action must be taken.
        if (annotation != null)
        {
            // Determine the "target" on which to call subscribeToEvent.
            // If the class to which this annotation is attached implements ComponentSystemEventListener
            if (inspected instanceof ComponentSystemEventListener)
            {
                // If the class to which this annotation is attached is a UIComponent instance, "target" is the
                // UIComponent instance.

                // If the class to which this annotation is attached is a Renderer instance, "target" is the
                // UIComponent instance.

                /*
                 * If "target" is a UIComponent call UIComponent.subscribeToEvent(Class, ComponentSystemEventListener)
                 * passing the systemEventClass() of the annotation as the first argument and the instance of the class
                 * to which this annotation is attached (which must implement ComponentSystemEventListener) as the
                 * second argument.
                 */
                component.subscribeToEvent(annotation.systemEventClass(), (ComponentSystemEventListener) inspected);
            }
            // If the class to which this annotation is attached implements SystemEventListener and does not implement
            // ComponentSystemEventListener, "target" is the Application instance.
            else if (component instanceof SystemEventListener)
            {
                // use the Application object from the FacesContext (note that a
                // direct use of subscribeToEvent() could cause problems if the
                // Application is wrapped)
                Application application = context.getApplication();
                
                // If "target" is the Application instance, inspect the value of the sourceClass() annotation attribute
                // value.
                if (Void.class.equals(annotation.sourceClass()))
                {
                    /*
                     * If the value is Void.class, call Application.subscribeToEvent(Class, SystemEventListener),
                     * passing the value of systemEventClass() as the first argument and the instance of the class to
                     * which this annotation is attached (which must implement SystemEventListener) as the second
                     * argument.
                     */
                    application.subscribeToEvent(annotation.systemEventClass(), (SystemEventListener) inspected);
                }
                else
                {
                    /*
                     * Otherwise, call Application.subscribeToEvent(Class, Class, SystemEventListener), passing the
                     * value of systemEventClass() as the first argument, the value of sourceClass() as the second
                     * argument, and the instance of the class to which this annotation is attached (which must
                     * implement SystemEventListener) as the third argument.
                     */
                    application.subscribeToEvent(annotation.systemEventClass(), annotation.sourceClass(),
                                     (SystemEventListener) inspected);
                }
            }

            /*
             * If the class to which this annotation is attached implements ComponentSystemEventListener and is neither
             * an instance of Renderer nor UIComponent, the action taken is unspecified. This case must not trigger any
             * kind of error.
             */
        }
    }

    private void _handleResourceDependencyAnnotations(FacesContext context, Class<?> inspectedClass, 
        UIComponent component, boolean isProduction)
    {
        List<ResourceDependency> dependencyList = null;
        
        if(dependencyList == null)  //not in production or the class hasn't been inspected yet
        {   
            ResourceDependency dependency = inspectedClass.getAnnotation(ResourceDependency.class);
            ResourceDependencies dependencies = inspectedClass.getAnnotation(ResourceDependencies.class);
            if(dependency != null || dependencies != null)
            {
                //resource dependencies were found using one or both annotations, create and build a new list
                dependencyList = new ArrayList<ResourceDependency>();
                
                if(dependency != null)
                {
                    dependencyList.add(dependency);
                }
                
                if(dependencies != null)
                {
                    dependencyList.addAll(Arrays.asList(dependencies.value()));
                }
            }
        }        
 
        if (dependencyList != null) //resource dependencies were found through inspection or from cache, handle them
        {
            for (ResourceDependency dependency : dependencyList)
            {
                _handleResourceDependency(context, component, dependency);
            }
        }
    }
    
    private void _handleResourceDependency(FacesContext context, UIComponent component, ResourceDependency annotation)
    {
        // If this annotation is not present on the class in question, no action must be taken.
        if (annotation != null)
        {
            // Create a UIOutput instance by passing javax.faces.Output. to
            // Application.createComponent(java.lang.String).
            UIOutput output = (UIOutput) createComponent(UIOutput.COMPONENT_TYPE);

            // Get the annotation instance from the class and obtain the values of the name, library, and
            // target attributes.
            String name = annotation.name();
            if (name != null && name.length() > 0)
            {
                name = _ELText.parse(getExpressionFactory(), context.getELContext(), name).toString(
                    context.getELContext());
            }

            // Obtain the renderer-type for the resource name by passing name to
            // ResourceHandler.getRendererTypeForResourceName(java.lang.String).
            // (note that we can not use this.getResourceHandler(), because the Application might be wrapped)
            String rendererType = context.getApplication().getResourceHandler().getRendererTypeForResourceName(name);

            // Call setRendererType on the UIOutput instance, passing the renderer-type.
            output.setRendererType(rendererType);

            // Obtain the Map of attributes from the UIOutput component by calling UIComponent.getAttributes().
            Map<String, Object> attributes = output.getAttributes();

            // Store the name into the attributes Map under the key "name".
            attributes.put("name", name);

            // If library is the empty string, let library be null.
            String library = annotation.library();
            if (library != null && library.length() > 0)
            {
                library = _ELText.parse(getExpressionFactory(), context.getELContext(), library).toString(
                    context.getELContext());
                // If library is non-null, store it under the key "library".
                if ("this".equals(library))
                {
                    // Special "this" behavior
                    Resource resource = (Resource)component.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                    if (resource != null)
                    {
                        attributes.put("library", resource.getLibraryName());
                    }
                }
                else
                {
                    attributes.put("library", library);
                }
            }

            // If target is the empty string, let target be null.
            String target = annotation.target();
            if (target != null && target.length() > 0)
            {
                target = _ELText.parse(getExpressionFactory(), context.getELContext(), target).toString(
                    context.getELContext());
                // If target is non-null, store it under the key "target".
                attributes.put("target", target);
                context.getViewRoot().addComponentResource(context, output, target);
            }
            else
            {
                // Otherwise, if target is null, call UIViewRoot.addComponentResource(javax.faces.context.FacesContext,
                // javax.faces.component.UIComponent), passing the UIOutput instance as the second argument.
                context.getViewRoot().addComponentResource(context, output);
            }
        }
    }
    
    private void _inspectRenderer(FacesContext context, UIComponent component, String componentType,
        String rendererType)
    {
        /*
         * The Renderer instance to inspect must be obtained by calling FacesContext.getRenderKit() and calling
         * RenderKit.getRenderer(java.lang.String, java.lang.String) on the result, passing the argument componentFamily
         * of the newly created component as the first argument and the argument rendererType as the second argument.
         */
        Renderer renderer = context.getRenderKit().getRenderer(component.getFamily(), rendererType);
        if (renderer == null)
        {
            // If no such Renderer can be found, a message must be logged with a helpful error message.
            log.log(Level.SEVERE, "renderer cannot be found for component type " + componentType + " and renderer type "
                    + rendererType);
        }
        else
        {
            // Otherwise, UIComponent.setRendererType(java.lang.String) must be called on the newly created
            // UIComponent instance, passing the argument rendererType as the argument.
            component.setRendererType(rendererType);

            /*
             * except the Renderer for the component to be returned must be inspected for the annotations mentioned in
             * createComponent(ValueExpression, FacesContext, String) as specified in the documentation for that method.
             */
            _handleAnnotations(context, renderer, component);
        }
    }
    
    private static SystemEvent _traverseListenerList(
            List<? extends SystemEventListener> listeners,
            Class<? extends SystemEvent> systemEventClass, Object source,
            SystemEvent event)
    {
        if (listeners != null && !listeners.isEmpty())
        {
            for (SystemEventListener listener : listeners)
            {
                // Call SystemEventListener.isListenerForSource(java.lang.Object), passing the source argument.
                // If this returns false, take no action on the listener.
                if (listener.isListenerForSource(source))
                {
                    // Otherwise, if the event to be passed to the listener instances has not yet been constructed,
                    // construct the event, passing source as the argument to the one-argument constructor that takes
                    // an Object. This same event instance must be passed to all listener instances.
                    event = _createEvent(systemEventClass, source, event);

                    // Call SystemEvent.isAppropriateListener(javax.faces.event.FacesListener), passing the listener
                    // instance as the argument. If this returns false, take no action on the listener.
                    if (event.isAppropriateListener(listener))
                    {
                        // Call SystemEvent.processListener(javax.faces.event.FacesListener), passing the listener
                        // instance.
                        event.processListener(listener);
                    }
                }
            }
        }

        return event;
    }

    private static SystemEvent _createEvent(
            Class<? extends SystemEvent> systemEventClass, Object source,
            SystemEvent event)
    {
        if (event == null)
        {
            try
            {
                Constructor<?>[] constructors = systemEventClass.getConstructors();
                Constructor<? extends SystemEvent> constructor = null;
                for (Constructor<?> c : constructors)
                {
                    if (c.getParameterTypes().length == 1)
                    {
                        // Safe cast, since the constructor belongs
                        // to a class of type SystemEvent
                        constructor = (Constructor<? extends SystemEvent>) c;
                        break;
                    }
                }
                if (constructor != null)
                {
                    event = constructor.newInstance(source);
                }

            }
            catch (Exception e)
            {
                throw new FacesException("Couldn't instanciate system event of type " + systemEventClass.getName(), e);
            }
        }

        return event;
    }

    private void checkNull(final Object param, final String paramName)
    {
        if (param == null)
        {
            throw new NullPointerException(paramName + " cannot be null.");
        }
    }

    private void checkEmpty(final String param, final String paramName)
    {
        if (param.length() == 0)
        {
            throw new NullPointerException("String " + paramName
                    + " cannot be empty.");
        }
    }

    public void publishEvent(FacesContext facesContext,
            Class<? extends SystemEvent> systemEventClass,
            Class<?> sourceBaseType, Object source)
    {
        checkNull(systemEventClass, "systemEventClass");
        checkNull(source, "source");
        
        //Call events only if event processing is enabled.
        if (!facesContext.isProcessingEvents())
        {
            return;
        }

        try
        {
            SystemEvent event = null;
            if (source instanceof SystemEventListenerHolder)
            {
                SystemEventListenerHolder holder = (SystemEventListenerHolder) source;

                // If the source argument implements SystemEventListenerHolder, call 
                // SystemEventListenerHolder.getListenersForEventClass(java.lang.Class) on it, 
                // passing the systemEventClass 
                // argument. If the list is not empty, perform algorithm traverseListenerList on the list.
                event = _traverseListenerList(holder
                        .getListenersForEventClass(systemEventClass),
                        systemEventClass, source, event);
            }
            
            UIViewRoot uiViewRoot = facesContext.getViewRoot();
            if (uiViewRoot != null)
            {
                //Call listeners on view level
                event = _traverseListenerList(uiViewRoot.getViewListenersForEventClass(systemEventClass), 
                        systemEventClass, source, event);
            }

            SystemListenerEntry systemListenerEntry = _systemEventListenerClassMap
                    .get(systemEventClass);
            if (systemListenerEntry != null)
            {
                systemListenerEntry.publish(systemEventClass, sourceBaseType,
                        source, event);
            }
        }
        catch (AbortProcessingException e)
        {
            // If the act of invoking the processListener method causes an AbortProcessingException to be thrown, 
            // processing of the listeners must be aborted, no further processing of the listeners for this event must 
            // take place, and the exception must be logged with Level.SEVERE.
            log.log(Level.SEVERE, "Event processing was aborted", e);
        }
    }

    @Override
    public void publishEvent(FacesContext facesContext,
            Class<? extends SystemEvent> systemEventClass, Object source)
    {
        publishEvent(facesContext, systemEventClass, source.getClass(), source);
    }

    @Override
    public ProjectStage getProjectStage()
    {
        // If the value has already been determined by a previous call to this
        // method, simply return that value.
        if (_projectStage == null)
        {

            FacesContext context = FacesContext.getCurrentInstance();
            String stageName = context.getExternalContext().getInitParameter(
                    ProjectStage.PROJECT_STAGE_PARAM_NAME);

            // If a value is found found
            if (stageName != null)
            {
                /*
                 * see if an enum constant can be obtained by calling ProjectStage.valueOf(), passing the value from the
                 * initParamMap. If this succeeds without exception, save the value and return it.
                 */
                try
                {
                    _projectStage = ProjectStage.valueOf(stageName);
                    return _projectStage;
                }
                catch (IllegalArgumentException e)
                {
                    //log.log(Level.SEVERE, "Couldn't discover the current project stage", e);
                }
            }

            _projectStage = ProjectStage.Production;
        }

        return _projectStage;
    }

    public void addBehavior(String behaviorId, String behaviorClass)
    {
        checkNull(behaviorId, "behaviorId");
        checkEmpty(behaviorId, "behaviorId");
        checkNull(behaviorClass, "behaviorClass");
        checkEmpty(behaviorClass, "behaviorClass");

        try
        {
            _behaviorClassMap.put(behaviorId, Class.forName(behaviorClass));
        }
        catch (ClassNotFoundException ignore)
        {

        }

    }

    public Iterator<String> getBehaviorIds()
    {
        return _behaviorClassMap.keySet().iterator();
    }

    public Behavior createBehavior(String behaviorId) throws FacesException
    {
        checkNull(behaviorId, "behaviorId");
        checkEmpty(behaviorId, "behaviorId");

        final Class<?> behaviorClass = this._behaviorClassMap.get(behaviorId);
        if (behaviorClass == null)
        {
            throw new FacesException(
                    "Could not find any registered behavior-class for behaviorId : "
                            + behaviorId);
        }

        try
        {
            final Behavior behavior = (Behavior) behaviorClass.newInstance();
            _handleAttachedResourceDependencyAnnotations(FacesContext.getCurrentInstance(), behaviorClass);            
            return behavior;
        }
        catch (Exception e)
        {
            throw new FacesException("Could not instantiate behavior: "
                    + behaviorClass, e);
        }
    }

    @Override
    public void addValidator(String validatorId, String validatorClass)
    {
        super.addValidator(validatorId, validatorClass);

        try
        {
            _validatorClassMap.put(validatorId, Class.forName(validatorClass));
        }
        catch (ClassNotFoundException ex)
        {
            throw new FacesException(ex.getMessage());
        }

    }

    public void addDefaultValidatorId(String validatorId)
    {
        if (_validatorClassMap.containsKey(validatorId))
        {
            _defaultValidatorsIds.put(validatorId, _validatorClassMap.get(
                    validatorId).getName());
        }
    }

    public final ResourceHandler getResourceHandler()
    {
        return _resourceHandler;
    }

    public final void setResourceHandler(ResourceHandler resourceHandler)
    {
        checkNull(resourceHandler, "resourceHandler");

        _resourceHandler = resourceHandler;
    }

    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass,
            SystemEventListener listener)
    {
        subscribeToEvent(systemEventClass, null, listener);
    }

    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass,
            Class<?> sourceClass, SystemEventListener listener)
    {
        checkNull(systemEventClass, "systemEventClass");
        checkNull(listener, "listener");

        SystemListenerEntry systemListenerEntry;
        synchronized (_systemEventListenerClassMap)
        {
            systemListenerEntry = _systemEventListenerClassMap
                    .get(systemEventClass);
            if (systemListenerEntry == null)
            {
                systemListenerEntry = new SystemListenerEntry();
                _systemEventListenerClassMap.put(systemEventClass,
                        systemListenerEntry);
            }
        }

        systemListenerEntry.addListener(listener, sourceClass);
    }

    public void unsubscribeFromEvent(
            Class<? extends SystemEvent> systemEventClass,
            SystemEventListener listener)
    {
        unsubscribeFromEvent(systemEventClass, null, listener);
    }

    public void unsubscribeFromEvent(
            Class<? extends SystemEvent> systemEventClass,
            Class<?> sourceClass, SystemEventListener listener)
    {
        checkNull(systemEventClass, "systemEventClass");
        checkNull(listener, "listener");

        SystemListenerEntry systemListenerEntry = _systemEventListenerClassMap
                .get(systemEventClass);
        if (systemListenerEntry != null)
        {
            systemListenerEntry.removeListener(listener, sourceClass);
        }
    }

    @Override
    public UIComponent createComponent(String componentType)
    {
        UIComponent component = super.createComponent(componentType);
        _handleAnnotations(FacesContext.getCurrentInstance(), component, component);
        return component;
    }
    

    @Override
    public UIComponent createComponent(ValueExpression componentExpression,
                                       FacesContext facesContext, String componentType)
            throws FacesException, NullPointerException
    {

        /*
         * Before the component instance is returned, it must be inspected for the presence of a ListenerFor (or
         * ListenersFor) or ResourceDependency (or ResourceDependencies) annotation. If any of these annotations are
         * present, the action listed in ListenerFor or ResourceDependency must be taken on the component, before it is
         * returned from this method. This variant of createComponent must not inspect the Renderer for the component to
         * be returned for any of the afore mentioned annotations. Such inspection is the province of
         */

        checkNull(componentExpression, "componentExpression");
        checkNull(facesContext, "facesContext");
        checkNull(componentType, "componentType");

        ELContext elContext = facesContext.getELContext();

        try
        {
            Object retVal = componentExpression.getValue(elContext);

            UIComponent createdComponent;

            if (retVal instanceof UIComponent)
            {
                createdComponent = (UIComponent) retVal;
                _handleAnnotations(facesContext, createdComponent, createdComponent);
            }
            else
            {
                createdComponent = createComponent(componentType);
                componentExpression.setValue(elContext, createdComponent);
            }

            return createdComponent;
        }
        catch (FacesException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
    }

    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType,
                                       String rendererType)
    {
        // Like createComponent(ValueExpression, FacesContext, String)
        UIComponent component = createComponent(componentExpression, context, componentType);

        _inspectRenderer(context, component, componentType, rendererType);

        return component;
    }
    

    @Override
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType)
    {
        checkNull(context, "context");
        checkNull(componentType, "componentType");

        // Like createComponent(String)
        UIComponent component = createComponent(componentType);

        _inspectRenderer(context, component, componentType, rendererType);

        return component;
    }

    @Override
    public UIComponent createComponent(FacesContext context, Resource componentResource)
    {
        checkNull(context, "context");
        checkNull(componentResource, "componentResource");
        
        UIComponent component = null;
        Resource resource;
        String fqcn;
        Class<? extends UIComponent> componentClass = null;

        /*
         * Obtain a reference to the ViewDeclarationLanguage for this Application instance by calling
         * ViewHandler.getViewDeclarationLanguage(javax.faces.context.FacesContext, java.lang.String), passing the
         * viewId found by calling UIViewRoot.getViewId() on the UIViewRoot in the argument FacesContext.
         */
        UIViewRoot view = context.getViewRoot();
        Application application = context.getApplication();
        ViewDeclarationLanguage vdl = application.getViewHandler().getViewDeclarationLanguage(
            context, view.getViewId());

        /*
         * Obtain a reference to the composite component metadata for this composite component by calling
         * ViewDeclarationLanguage.getComponentMetadata(javax.faces.context.FacesContext,
         * javax.faces.application.Resource), passing the facesContext and componentResource arguments to this method.
         * This version of JSF specification uses JavaBeans as the API to the component metadata.
         */
        BeanInfo metadata = vdl.getComponentMetadata(context, componentResource);
        if (metadata == null)
        {
            throw new FacesException("Could not get component metadata for " 
                    + componentResource.getResourceName()
                    + ". Did you forget to specify <composite:interface>?");
        }

        /*
         * Determine if the component author declared a component-type for this component instance by obtaining the
         * BeanDescriptor from the component metadata and calling its getValue() method, passing
         * UIComponent.COMPOSITE_COMPONENT_TYPE_KEY as the argument. If non-null, the result must be a ValueExpression
         * whose value is the component-type of the UIComponent to be created for this Resource component. Call through
         * to createComponent(java.lang.String) to create the component.
         */
        BeanDescriptor descriptor = metadata.getBeanDescriptor();
        ValueExpression componentType = (ValueExpression) descriptor.getValue(UIComponent.COMPOSITE_COMPONENT_TYPE_KEY);
        boolean annotationsApplied = false;
        if (componentType != null)
        {
            component = application.createComponent((String) componentType.getValue(context.getELContext()));
            annotationsApplied = true;
        }
        else
        {
            /*
             * Otherwise, determine if a script based component for this Resource can be found by calling
             * ViewDeclarationLanguage.getScriptComponentResource(javax.faces.context.FacesContext,
             * javax.faces.application.Resource). If the result is non-null, and is a script written in one of the
             * languages listed in JSF 4.3 of the specification prose document, create a UIComponent instance from the
             * script resource.
             */
            resource = vdl.getScriptComponentResource(context, componentResource);
            if (resource != null)
            {
                String name = resource.getResourceName();
                String className = name.substring(0, name.lastIndexOf('.'));
                
                Class clazz;
                try
                {
                    clazz = Class.forName(className);
                    component = (UIComponent)clazz.newInstance();
                }
                catch (Exception e)
                {
                    throw new FacesException(e);
                }
            }
            else
            {
                /*
                 * Otherwise, let library-name be the return from calling Resource.getLibraryName() on the argument
                 * componentResource and resource-name be the return from calling Resource.getResourceName() on the
                 * argument componentResource. Create a fully qualified Java class name by removing any file extension
                 * from resource-name and let fqcn be library-name + "." + resource-name. If a class with the name of
                 * fqcn cannot be found, take no action and continue to the next step. If any of InstantiationException,
                 * IllegalAccessException, or ClassCastException are thrown, wrap the exception in a FacesException and
                 * re-throw it. If any other exception is thrown, log the exception and continue to the next step.
                 */

                String name = componentResource.getResourceName();
                String className = name.substring(0, name.lastIndexOf('.'));
                fqcn = componentResource.getLibraryName() + '.' + className;
                
                try
                {
                    componentClass = classForName(fqcn);
                }
                catch (ClassNotFoundException e)
                {
                }

                if (componentClass != null)
                {
                    try
                    {
                        component = componentClass.newInstance();
                    }
                    catch (InstantiationException e)
                    {
                        log.log(Level.SEVERE, "Could not instantiate component class name = " + fqcn, e);
                        throw new FacesException("Could not instantiate component class name = " + fqcn, e);
                    }
                    catch (IllegalAccessException e)
                    {
                        log.log(Level.SEVERE, "Could not instantiate component class name = " + fqcn, e);
                        throw new FacesException("Could not instantiate component class name = " + fqcn, e);
                    }
                    catch (Exception e)
                    {
                        log.log(Level.SEVERE, "Could not instantiate component class name = " + fqcn, e);
                    }
                }

                /*
                 * If none of the previous steps have yielded a UIComponent instance, call
                 * createComponent(java.lang.String) passing "javax.faces.NamingContainer" as the argument.
                 */
                if (component == null)
                {
                    component = application.createComponent(UINamingContainer.COMPONENT_TYPE);
                    annotationsApplied = true;
                }
            }
        }

        /*
         * Call UIComponent.setRendererType(java.lang.String) on the UIComponent instance, passing
         * "javax.faces.Composite" as the argument.
         */
        component.setRendererType("javax.faces.Composite");

        /*
         * Store the argument Resource in the attributes Map of the UIComponent under the key,
         * Resource.COMPONENT_RESOURCE_KEY.
         */
        component.getAttributes().put(Resource.COMPONENT_RESOURCE_KEY, componentResource);

        /*
         * Store composite component metadata in the attributes Map of the UIComponent under the key,
         * UIComponent.BEANINFO_KEY.
         */
        component.getAttributes().put(UIComponent.BEANINFO_KEY, metadata);

        /*
         * Before the component instance is returned, it must be inspected for the presence of a ListenerFor annotation.
         * If this annotation is present, the action listed in ListenerFor must be taken on the component, before it is
         * returned from this method.
         */
        if (!annotationsApplied)
        {
            _handleAnnotations(context, component, component);
        }

        return component;
    }
    
    private static Class classForName(String type)
        throws ClassNotFoundException
    {
        if (type == null)
        {
            throw new NullPointerException("type");
        }
        try
        {
            // Try WebApp ClassLoader first
            return Class.forName(type,
                                 false, // do not initialize for faster startup
                                 Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException ignore)
        {
            // fallback: Try ClassLoader for ClassUtils (i.e. the myfaces.jar lib)
            return Class.forName(type,
                                 false, // do not initialize for faster startup
                                 MockApplication20.class.getClassLoader());
        }
    }
    
    private void _handleAttachedResourceDependencyAnnotations(FacesContext context, Object inspected)
    {
        if (inspected == null)
        {
            return;
        }
        
        ResourceDependency annotation = inspected.getClass().getAnnotation(ResourceDependency.class);
        
        if (annotation == null)
        {
            // If the ResourceDependency annotation is not present, the argument must be inspected for the presence 
            // of the ResourceDependencies annotation. 
            ResourceDependencies dependencies = inspected.getClass().getAnnotation(ResourceDependencies.class);
            if (dependencies != null)
            {
                // If the ResourceDependencies annotation is present, the action described in ResourceDependencies 
                // must be taken.
                for (ResourceDependency dependency : dependencies.value())
                {
                    _handleAttachedResourceDependency(context, dependency);
                }
            }
        }
        else
        {
            // If the ResourceDependency annotation is present, the action described in ResourceDependency must be 
            // taken. 
            _handleAttachedResourceDependency(context, annotation);
        }
    }
    
    private void _handleAttachedResourceDependency(FacesContext context, ResourceDependency annotation)
    {
        // If this annotation is not present on the class in question, no action must be taken. 
        if (annotation != null)
        {
            Application application = context.getApplication();
            
            // Create a UIOutput instance by passing javax.faces.Output. to 
            // Application.createComponent(java.lang.String).
            UIOutput output = (UIOutput) application.createComponent(UIOutput.COMPONENT_TYPE);
            
            // Get the annotation instance from the class and obtain the values of the name, library, and 
            // target attributes.
            String name = annotation.name();
            if (name != null && name.length() > 0)
            {
                name = _ELText.parse(getExpressionFactory(), context.getELContext(), name).toString(
                    context.getELContext());
            }
            
            // Obtain the renderer-type for the resource name by passing name to 
            // ResourceHandler.getRendererTypeForResourceName(java.lang.String).
            String rendererType = application.getResourceHandler().getRendererTypeForResourceName(name);
            
            // Call setRendererType on the UIOutput instance, passing the renderer-type.
            output.setRendererType(rendererType);
            
            // Obtain the Map of attributes from the UIOutput component by calling UIComponent.getAttributes().
            Map<String, Object> attributes = output.getAttributes();
            
            // Store the name into the attributes Map under the key "name".
            attributes.put("name", name);
            
            // If library is the empty string, let library be null.
            String library = annotation.library();
            if (library != null && library.length() > 0)
            {
                library = _ELText.parse(getExpressionFactory(), context.getELContext(), library).toString(
                    context.getELContext());
                // If library is non-null, store it under the key "library".
                attributes.put("library", library);
            }
            
            // If target is the empty string, let target be null.
            String target = annotation.target();
            if (target != null && target.length() > 0)
            {
                target = _ELText.parse(getExpressionFactory(), context.getELContext(), target).toString(
                    context.getELContext());
                // If target is non-null, store it under the key "target".
                attributes.put("target", target);
                context.getViewRoot().addComponentResource(context, output, target);
            }
            else
            {
                // Otherwise, if target is null, call UIViewRoot.addComponentResource(javax.faces.context.FacesContext, 
                // javax.faces.component.UIComponent), passing the UIOutput instance as the second argument.
                context.getViewRoot().addComponentResource(context, output);
            }
        }
    }

    @Override
    public Converter createConverter(String converterId)
    {
        Converter converter = super.createConverter(converterId);
        _handleAttachedResourceDependencyAnnotations(FacesContext.getCurrentInstance(), converter);
        return converter;
    }

    @Override
    public Validator createValidator(String validatorId)
    {
        Validator validator = super.createValidator(validatorId);
        _handleAttachedResourceDependencyAnnotations(FacesContext.getCurrentInstance(), validator);
        return validator;
    }
    
    
}
