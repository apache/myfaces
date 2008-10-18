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
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AfterAddToParentEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.event.ViewMapCreatedEvent;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * Creates a JSF View, which is a container that holds all of the components that are part of the view.
 * <p>
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * </p>
 * <p>
 * See the javadoc for this class in the <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF
 * Specification</a> for further details.
 * </p>
 */
@JSFComponent(name = "f:view", bodyContent = "JSP", tagClass = "org.apache.myfaces.taglib.core.ViewTag")
@JSFJspProperty(name = "binding", returnType = "java.lang.String", tagExcluded = true)
public class UIViewRoot extends UIComponentBase implements ComponentSystemEventListener
{
    public static final String COMPONENT_TYPE = "javax.faces.ViewRoot";
    public static final String COMPONENT_FAMILY = "javax.faces.ViewRoot";

    public static final String UNIQUE_ID_PREFIX = "j_id";
    private static final int ANY_PHASE_ORDINAL = PhaseId.ANY_PHASE.getOrdinal();

    private final Logger logger = Logger.getLogger(UIViewRoot.class.getName());
    
    private static final PhaseProcessor APPLY_REQUEST_VALUES_PROCESSOR = new ApplyRequestValuesPhaseProcessor();
    private static final PhaseProcessor PROCESS_VALIDATORS_PROCESSOR = new ProcessValidatorPhaseProcessor();
    private static final PhaseProcessor UPDATE_MODEL_PROCESSOR = new UpdateModelPhaseProcessor();
    
    private static final ContextCallback AJAX_ENCODE_ALL_CALLBACK = new AjaxEncodeAllCallback();
    private static final ContextCallback APPLY_REQUEST_VALUES_CALLBACK = new ApplyRequestValuesPhaseCallback();
    private static final ContextCallback PROCESS_VALIDATORS_CALLBACK = new ProcessValidatorPhaseCallback();
    private static final ContextCallback UPDATE_MODEL_CALLBACK = new UpdateModelPhaseCallback();
    
    
    private static final String AJAX_RESPONSE_COMPONENTS = "components";
    private static final String AJAX_RESPONSE_ROOT = "partial-response";
    
    private static final String AJAX_RESPONSE_MARKUP = "markup";
    private static final String AJAX_RESPONSE_RENDER = "render";
    private static final String AJAX_RESPONSE_STATE = "state";
    private static final String AJAX_RESPONSE_CHARACTER_DATA_END = "]]>";
    private static final String AJAX_RESPONSE_CHARACTER_DATA_START = "<![CDATA[";
    
    private static final String XML_CONTENT_TYPE = "text/xml";
    
    // TODO: Add the encoding as well? If so a constant might not be perfect
    private static final String XML_DOCUMENT_HEADER = "<?xml version=\"1.0\" ?>";
    
    /**
     * The counter which will ensure a unique component id for every component instance in the tree that doesn't have an
     * id attribute set.
     */
    private long _uniqueIdCounter = 0;

    private Locale _locale;
    private String _renderKitId;
    private String _viewId;

    // todo: is it right to save the state of _events and _phaseListeners?
    private List<FacesEvent> _events;
    private List<PhaseListener> _phaseListeners;

    private MethodExpression _beforePhaseListener;
    private MethodExpression _afterPhaseListener;

    private Map<String, Object> _viewScope;

    private transient Lifecycle _lifecycle = null;

    /**
     * Construct an instance of the UIViewRoot.
     */
    public UIViewRoot()
    {
        setRendererType(null);
    }

    public void processEvent(ComponentSystemEvent event)
    {
        if (event != null && event.getClass().equals(AfterAddToParentEvent.class))
        {
            notifyListeners(getFacesContext(), PhaseId.RESTORE_VIEW, _afterPhaseListener, false);
        }
    }

    @Override
    public void queueEvent(FacesEvent event)
    {
        checkNull(event, "event");
        if (_events == null)
        {
            _events = new ArrayList<FacesEvent>();
        }
        _events.add(event);
    }

    @Override
    public void processDecodes(FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.APPLY_REQUEST_VALUES, APPLY_REQUEST_VALUES_PROCESSOR, true);
    }

    @Override
    public void processValidators(FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.PROCESS_VALIDATIONS, PROCESS_VALIDATORS_PROCESSOR, true);
    }

    @Override
    public void processUpdates(FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.UPDATE_MODEL_VALUES, UPDATE_MODEL_PROCESSOR, true);
    }

    public void processApplication(final FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.INVOKE_APPLICATION, null, true);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException
    {
        checkNull(context, "context");

        boolean skipPhase = false;

        try
        {
            skipPhase = notifyListeners(context, PhaseId.RENDER_RESPONSE, getBeforePhaseListener(), true);
        }
        catch (Exception e)
        {
            // following the spec we have to swallow the exception
            logger.log(Level.SEVERE, "Exception while processing phase listener: " + e.getMessage(), e);
        }

        if (!skipPhase)
        {
            if (context.isAjaxRequest())
            {
                // If FacesContext.isAjaxRequest() returns true.
                _encodeBeginAjax(context);
            }
            else
            {
                // If FacesContext.isAjaxRequest() returns false, perform the parent class encodeBegin processing.
                super.encodeBegin(context);
            }
        }
    }
    
    /**
     * @since 2.0
     */
    @Override
    public void encodeChildren(FacesContext context) throws IOException
    {
        // If FacesContext.isAjaxRequest() returns true and FacesContext.isRenderAll() returns false. 
        if (context.isAjaxRequest() && !context.isRenderAll())
        {
            // Call FacesContext.getRenderPhaseClientIds(). This returns a list of client ids that must be processed during 
            // the render portion of the request processing lifecycle.
            List<String> clientIds = context.getRenderPhaseClientIds();
            if (clientIds == null || clientIds.isEmpty())
            {
                /* 
                 * If partial rendering was not performed, delegate to the parent 
                 * UIComponentBase.encodeChildren(javax.faces.context.FacesContext) method.
                 */
                super.encodeChildren(context);
            }
            else
            {
                try
                {
                    // For each client id in the list, 
                    for (String clientId : clientIds)
                    {
                        // using invokeOnComponent, all the encodeAll method on the component with that client id. 
                        invokeOnComponent(context, clientId, AJAX_ENCODE_ALL_CALLBACK);
                    }
                }
                catch (CallbackIOException e)
                {
                    throw e.getIOException();
                }
            }
        }
        else
        {
            /* 
             * If FacesContext.isAjaxRequest() returns false, or FacesContext.isRenderAll(), delegate to the parent 
             * UIComponentBase.encodeChildren(javax.faces.context.FacesContext) method.
             */
            super.encodeChildren(context);
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException
    {
        checkNull(context, "context");

        if (context.isAjaxRequest())
        {
            // If FacesContext.isAjaxRequest() returns true, write the ending elements for the partial response.
            _encodeEndAjax(context);
        }
        else
        {
            // If FacesContext.isAjaxRequest() returns flase, invoke the default 
            // UIComponentBase.encodeEnd(javax.faces.context.FacesContext) behavior.
            super.encodeEnd(context);
        }
        
        try
        {
            notifyListeners(context, PhaseId.RENDER_RESPONSE, getAfterPhaseListener(), false);
        }
        catch (Exception e)
        {
            // following the spec we have to swallow the exception
            logger.log(Level.SEVERE, "Exception while processing phase listener: " + e.getMessage(), e);
        }
    }

    /**
     * Provides a unique id for this component instance.
     */
    public String createUniqueId()
    {
        ExternalContext extCtx = FacesContext.getCurrentInstance().getExternalContext();
        StringBuilder bld = __getSharedStringBuilder();
        return extCtx.encodeNamespace(bld.append(UNIQUE_ID_PREFIX).append(_uniqueIdCounter++).toString());
    }

    /**
     * The locale for this view.
     * <p>
     * Defaults to the default locale specified in the faces configuration file.
     * </p>
     */
    @JSFProperty
    public Locale getLocale()
    {
        if (_locale != null)
        {
            return _locale;
        }
        ValueExpression expression = getValueExpression("locale");
        if (expression != null)
        {
            return (Locale)expression.getValue(getFacesContext().getELContext());
        }
        else
        {
            Object locale = getFacesContext().getApplication().getViewHandler().calculateLocale(getFacesContext());

            if (locale instanceof Locale)
            {
                return (Locale)locale;
            }
            else if (locale instanceof String)
            {
                return stringToLocale((String)locale);
            }
        }

        return getFacesContext().getApplication().getViewHandler().calculateLocale(getFacesContext());
    }

    public void setLocale(Locale locale)
    {
        this._locale = locale;
    }

    private boolean process(FacesContext context, PhaseId phaseId, PhaseProcessor processor, boolean broadcast)
    {
        if (!notifyListeners(context, phaseId, getBeforePhaseListener(), true))
        {
            if (processor != null)
            {
                processor.process(context, this);
            }

            if (broadcast)
            {
                _broadcastForPhase(phaseId);
            }
        }
        
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
        
        return notifyListeners(context, phaseId, getAfterPhaseListener(), false);
    }

    /**
     * Invoke view-specific phase listeners, plus an optional EL MethodExpression.
     * <p>
     * JSF1.2 adds the ability for PhaseListener objects to be added to a UIViewRoot instance, and for
     * "beforePhaseListener" and "afterPhaseListener" EL expressions to be defined on the viewroot. This method is
     * expected to be called at appropriate times, and will then execute the relevant listener callbacks.
     * <p>
     * Parameter "listener" may be null. If not null, then it is an EL expression pointing to a user method that will be
     * invoked.
     * <p>
     * Note that the global PhaseListeners are invoked via the Lifecycle implementation, not from this method here.
     */
    private boolean notifyListeners(FacesContext context, PhaseId phaseId, MethodExpression listener,
                                    boolean beforePhase)
    {
        /*
         * Initialize a state flag to false.
         * 
         * If getBeforePhaseListener() returns non-null, invoke the listener, passing in the correct corresponding 
         * PhaseId for this phase.
         * 
         * Upon return from the listener, call FacesContext.getResponseComplete() and FacesContext.getRenderResponse(). 
         * If either return true set the internal state flag to true.
         * 
         * If or one or more listeners have been added by a call to addPhaseListener(javax.faces.event.PhaseListener), 
         * invoke the beforePhase method on each one whose PhaseListener.getPhaseId() matches the current phaseId, 
         * passing in the same PhaseId as in the previous step.
         * 
         * Upon return from each listener, call FacesContext.getResponseComplete() and FacesContext.getRenderResponse().
         * If either return true set the internal state flag to true.
         * 
         * Execute any processing for this phase if the internal state flag was not set.
         * 
         * If getAfterPhaseListener() returns non-null, invoke the listener, passing in the correct corresponding 
         * PhaseId for this phase.
         * 
         * If or one or more listeners have been added by a call to addPhaseListener(javax.faces.event.PhaseListener), 
         * invoke the afterPhase method on each one whose PhaseListener.getPhaseId() matches the current phaseId, 
         * passing in the same PhaseId as in the previous step.
         */

        boolean skipPhase = false;

        if (listener != null || (_phaseListeners != null && !_phaseListeners.isEmpty()))
        {
            PhaseEvent event = createEvent(context, phaseId);

            if (listener != null)
            {
                listener.invoke(context.getELContext(), new Object[] { event });
                skipPhase = context.getResponseComplete() || context.getRenderResponse();
            }

            if (_phaseListeners != null && !_phaseListeners.isEmpty())
            {
                for (PhaseListener phaseListener : _phaseListeners)
                {
                    PhaseId listenerPhaseId = phaseListener.getPhaseId();
                    if (phaseId.equals(listenerPhaseId) || PhaseId.ANY_PHASE.equals(listenerPhaseId))
                    {
                        if (beforePhase)
                        {
                            phaseListener.beforePhase(event);
                        }
                        else
                        {
                            phaseListener.afterPhase(event);
                        }
                        skipPhase = context.getResponseComplete() || context.getRenderResponse();
                    }
                }
            }
        }

        return skipPhase;
    }

    private PhaseEvent createEvent(FacesContext context, PhaseId phaseId)
    {
        if (_lifecycle == null)
        {
            LifecycleFactory factory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            String id = context.getExternalContext().getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
            if (id == null)
            {
                id = LifecycleFactory.DEFAULT_LIFECYCLE;
            }
            _lifecycle = factory.getLifecycle(id);
        }
        return new PhaseEvent(context, phaseId, _lifecycle);
    }

    private void _broadcastForPhase(PhaseId phaseId)
    {
        if (_events == null)
        {
            return;
        }

        boolean abort = false;

        int phaseIdOrdinal = phaseId.getOrdinal();
        for (ListIterator<FacesEvent> listiterator = _events.listIterator(); listiterator.hasNext();)
        {
            FacesEvent event = listiterator.next();
            int ordinal = event.getPhaseId().getOrdinal();
            if (ordinal == ANY_PHASE_ORDINAL || ordinal == phaseIdOrdinal)
            {
                UIComponent source = event.getComponent();
                try
                {
                    source.broadcast(event);
                }
                catch (AbortProcessingException e)
                {
                    // abort event processing
                    // Page 3-30 of JSF 1.1 spec: "Throw an
                    // AbortProcessingException, to tell the JSF implementation
                    // that no further broadcast of this event, or any further
                    // events, should take place."
                    abort = true;
                    break;
                }
                finally
                {
                    try
                    {
                        listiterator.remove();
                    }
                    catch (ConcurrentModificationException cme)
                    {
                        int eventIndex = listiterator.previousIndex();
                        _events.remove(eventIndex);
                        listiterator = _events.listIterator();
                    }
                }
            }
        }

        if (abort)
        {
            // TODO: abort processing of any event of any phase or just of any
            // event of the current phase???
            clearEvents();
        }
    }

    private void clearEvents()
    {
        _events = null;
    }

    private void checkNull(Object value, String valueLabel)
    {
        if (value == null)
        {
            throw new NullPointerException(valueLabel + " is null");
        }
    }

    private Locale stringToLocale(String localeStr)
    {
        // locale expr: \[a-z]{2}((-|_)[A-Z]{2})?

        if (localeStr.contains("_") || localeStr.contains("-"))
        {
            if (localeStr.length() == 2)
            {
                // localeStr is the lang
                return new Locale(localeStr);
            }
        }
        else
        {
            if (localeStr.length() == 5)
            {
                String lang = localeStr.substring(0, 1);
                String country = localeStr.substring(3, 4);
                return new Locale(lang, country);
            }
        }

        return Locale.getDefault();
    }

    public List<PhaseListener> getPhaseListeners()
    {
        // TODO: JSF 2.0 #57

        return null;
    }

    /**
     * @since 2.0
     */
    @Override
    public boolean getRendersChildren()
    {
        // If FacesContext.isAjaxRequest() returns true and it is a partial render request 
        // (FacesContext.isRenderAll() returns false), return true.
        FacesContext context = FacesContext.getCurrentInstance();

        return (context.isAjaxRequest() && context.isRenderAll()) ? true : super.getRendersChildren();
    }
    
    /**
     * Defines what renderkit should be used to render this view.
     */
    @JSFProperty
    public String getRenderKitId()
    {
        if (_renderKitId != null)
        {
            return _renderKitId;
        }
        ValueExpression expression = getValueExpression("renderKitId");
        if (expression != null)
        {
            return (String)expression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    public void setRenderKitId(String renderKitId)
    {
        this._renderKitId = renderKitId;
    }

    /**
     * DO NOT USE.
     * <p>
     * This inherited property is disabled. Although this class extends a base-class that defines a read/write rendered
     * property, this particular subclass does not support setting it. Yes, this is broken OO design: direct all
     * complaints to the JSF spec group.
     */
    @Override
    @JSFProperty(tagExcluded = true)
    public void setRendered(boolean state)
    {
        // Call parent method due to TCK problems
        super.setRendered(state);
        // throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRendered()
    {
        // Call parent method due to TCK problems
        return super.isRendered();
    }

    /**
     * DO NOT USE.
     * <p>
     * Although this class extends a base-class that defines a read/write id property, it makes no sense for this
     * particular subclass to support it. The tag library does not export this property for use, but there is no way to
     * "undeclare" a java method. Yes, this is broken OO design: direct all complaints to the JSF spec group.
     * <p>
     * This property should be disabled (ie throw an exception if invoked). However there are currently several places
     * that call this method (eg during restoreState) so it just does the normal thing for the moment. TODO: fix callers
     * then make this throw an exception.
     * 
     * @JSFProperty tagExcluded="true"
     */
    @Override
    public void setId(String id)
    {
        // throw new UnsupportedOperationException();

        // Leave enabled for now. Things like the TreeStructureManager call this,
        // even though they probably should not.
        super.setId(id);
    }

    @Override
    public String getId()
    {
        // Should just return null. But as setId passes the method on, do same here.
        return super.getId();
    }

    /**
     * DO NOT USE.
     * <p>
     * As this component has no "id" property, it has no clientId property either.
     */
    @Override
    public String getClientId(FacesContext context)
    {
        return super.getClientId(context);
        // Call parent method due to TCK problems
        // return null;
    }

    public void addComponentResource(FacesContext context, UIComponent componentResource)
    {
        addComponentResource(context, componentResource, null);
    }

    public void addComponentResource(FacesContext context, UIComponent componentResource, String target)
    {
        // If the target argument is null
        if (target == null)
        {
            // Look for a target attribute on the component
            target = (String)componentResource.getAttributes().get("target");

            // If there is no target attribute, set target to be the default value head
            if (target == null)
            {
                target = "head";
            }
        }

        // Call getComponentResources to obtain the child list for the given target
        List<UIComponent> componentResources = getComponentResources(context, target);

        // Add the component resource to the list
        // TODO: Validate if we should check for duplicates, spec don't say anything about it
        componentResources.add(componentResource);
    }

    public List<UIComponent> getComponentResources(FacesContext context, String target)
    {
        // Locate the facet for the component by calling getFacet() using target as the argument
        UIComponent facet = getFacet(target);

        // If the facet is not found
        if (facet == null)
        {
            facet = context.getApplication().createComponent("javax.faces.Panel");
            facet.setId(target);

            // Add the facet to the facets Map using target as the key
            getFacets().put(target, facet);
        }

        // Return the children of the facet
        return facet.getChildren();
    }

    public Map<String, Object> getViewMap()
    {
        return this.getViewMap(true);
    }

    public Map<String, Object> getViewMap(boolean create)
    {
        if (_viewScope == null && create)
        {
            _viewScope = new ViewScope();
        }

        return _viewScope;
    }

    public void removeComponentResource(FacesContext context, UIComponent componentResource)
    {
        removeComponentResource(context, componentResource, null);
    }

    public void removeComponentResource(FacesContext context, UIComponent componentResource, String target)
    {
        // If the target argument is null
        if (target == null)
        {
            // Look for a target attribute on the component
            target = (String)componentResource.getAttributes().get("target");

            // If there is no target attribute
            if (target == null)
            {
                // Set target to be the default value head
                target = "head";
            }
        }

        // Call getComponentResources to obtain the child list for the given target.
        List<UIComponent> componentResources = getComponentResources(context, target);

        // Remove the component resource from the child list
        componentResources.remove(componentResource);
    }

    /**
     * A unique identifier for the "template" from which this view was generated.
     * <p>
     * Typically this is the filesystem path to the template file, but the exact details are the responsibility of the
     * current ViewHandler implementation.
     */
    @JSFProperty(tagExcluded = true)
    public String getViewId()
    {
        return _viewId;
    }

    public void setViewId(String viewId)
    {
        // It really doesn't make much sense to allow null here.
        // However the TCK does not check for it, and sun's implementation
        // allows it so here we allow it too.
        this._viewId = viewId;
    }

    /**
     * Adds a The phaseListeners attached to ViewRoot.
     */
    public void addPhaseListener(PhaseListener phaseListener)
    {
        if (phaseListener == null)
            throw new NullPointerException("phaseListener");
        if (_phaseListeners == null)
            _phaseListeners = new ArrayList<PhaseListener>();

        _phaseListeners.add(phaseListener);
    }

    /**
     * Removes a The phaseListeners attached to ViewRoot.
     */
    public void removePhaseListener(PhaseListener phaseListener)
    {
        if (phaseListener == null || _phaseListeners == null)
            return;

        _phaseListeners.remove(phaseListener);
    }

    /**
     * MethodBinding pointing to a method that takes a javax.faces.event.PhaseEvent and returns void, called before
     * every phase except for restore view.
     * 
     * @return the new beforePhaseListener value
     */
    @JSFProperty(stateHolder = true, returnSignature = "void", methodSignature = "javax.faces.event.PhaseEvent", jspName = "beforePhase")
    public MethodExpression getBeforePhaseListener()
    {
        if (_beforePhaseListener != null)
        {
            return _beforePhaseListener;
        }
        ValueExpression expression = getValueExpression("beforePhaseListener");
        if (expression != null)
        {
            return (MethodExpression)expression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets
     * 
     * @param beforePhaseListener
     *            the new beforePhaseListener value
     */
    public void setBeforePhaseListener(MethodExpression beforePhaseListener)
    {
        this._beforePhaseListener = beforePhaseListener;
    }

    /**
     * MethodBinding pointing to a method that takes a javax.faces.event.PhaseEvent and returns void, called after every
     * phase except for restore view.
     * 
     * @return the new afterPhaseListener value
     */
    @JSFProperty(stateHolder = true, returnSignature = "void", methodSignature = "javax.faces.event.PhaseEvent", jspName = "afterPhase")
    public MethodExpression getAfterPhaseListener()
    {
        if (_afterPhaseListener != null)
        {
            return _afterPhaseListener;
        }
        ValueExpression expression = getValueExpression("afterPhaseListener");
        if (expression != null)
        {
            return (MethodExpression)expression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets
     * 
     * @param afterPhaseListener
     *            the new afterPhaseListener value
     */
    public void setAfterPhaseListener(MethodExpression afterPhaseListener)
    {
        this._afterPhaseListener = afterPhaseListener;
    }

    @Override
    public Object saveState(FacesContext facesContext)
    {
        Object[] values = new Object[8];
        values[0] = super.saveState(facesContext);
        values[1] = _locale;
        values[2] = _renderKitId;
        values[3] = _viewId;
        values[4] = _uniqueIdCounter;
        values[5] = saveAttachedState(facesContext, _phaseListeners);
        values[6] = saveAttachedState(facesContext, _beforePhaseListener);
        values[7] = saveAttachedState(facesContext, _afterPhaseListener);

        return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restoreState(FacesContext facesContext, Object state)
    {
        Object[] values = (Object[])state;
        super.restoreState(facesContext, values[0]);
        _locale = (Locale)values[1];
        _renderKitId = (String)values[2];
        _viewId = (String)values[3];
        _uniqueIdCounter = (Long)values[4];
        _phaseListeners = (List<PhaseListener>)restoreAttachedState(facesContext, values[5]);
        _beforePhaseListener = (MethodExpression)restoreAttachedState(facesContext, values[6]);
        _afterPhaseListener = (MethodExpression)restoreAttachedState(facesContext, values[7]);
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
    
    private void _encodeBeginAjax(FacesContext context) throws IOException
    {
        // replace the ResponseWriter in the FacesContext with the writer used to render partial responses.
        ResponseWriter writer = context.getPartialResponseWriter();
        
        context.setResponseWriter(writer);
        
        if (!context.isRenderNone())
        {
            // If FacesContext.isRenderNone() returns false, set the response content-type and headers 
            // appropriately for XML.
            context.getExternalContext().setResponseContentType(XML_CONTENT_TYPE);
            
            writer.write(XML_DOCUMENT_HEADER);
            
            // The  method must write the beginning elements for the partial response:
            // <partial-response>
            //   <components>
            writer.startElement(AJAX_RESPONSE_ROOT, null);
            writer.startElement(AJAX_RESPONSE_COMPONENTS, null);
            
            // If FacesContext.isRenderAll() returns true write:
            if (context.isRenderAll())
            {
                // <render id="javax.faces.ViewRoot"/>
                //   <markup><![CDATA[
                // to indicate the client JavaScript must use the entire response.  
                writer.startElement(AJAX_RESPONSE_RENDER, this);
                writer.writeAttribute("id", getFamily(), "clientId");
                writer.startElement(AJAX_RESPONSE_MARKUP, this);
                writer.write(AJAX_RESPONSE_CHARACTER_DATA_START);
            }
        }
    }
    
    private void _encodeEndAjax(FacesContext context) throws IOException
    {
        ResponseWriter writer = context.getResponseWriter();
        
        if (context.isRenderAll())
        {
            // If FacesContext.isRenderAll() returns true write:
            
            // ]]>
            writer.write(AJAX_RESPONSE_CHARACTER_DATA_END);
            
            // </markup>
            writer.endElement(AJAX_RESPONSE_MARKUP);
            
            // </render>
            writer.endElement(AJAX_RESPONSE_RENDER);
            
            if (!context.isRenderNone())
            {
                // component markup was rendered (FacesContext.isRenderNone() returns false), write:
                // </components>
                writer.endElement(AJAX_RESPONSE_COMPONENTS);
            }
        }
        
        // <state>
        writer.startElement(AJAX_RESPONSE_STATE, null);
        
        // <![CDATA[
        writer.write(AJAX_RESPONSE_CHARACTER_DATA_START);
        
        // state information for this view
        writer.write(context.getApplication().getStateManager().getViewState(context));
        
        // ]]>
        writer.write(AJAX_RESPONSE_CHARACTER_DATA_END);
        
        // </state>
        writer.endElement(AJAX_RESPONSE_STATE);
        
        // Write the ending partial-response element:
        // </partial-response>
        writer.endElement(AJAX_RESPONSE_ROOT);
    }
    
    private List<String> _getAjaxClientIds(FacesContext context)
    {
        // Call FacesContext.getExecutePhaseClientIds()
        List<String> clientIds = context.getExecutePhaseClientIds();
        if (clientIds == null || clientIds.isEmpty())
        {
            // If there were no client ids specified, refer to the List of client ids by calling 
            // FacesContext.getRenderPhaseClientIds()
            clientIds = context.getRenderPhaseClientIds();
        }
        
        return clientIds;
    }
    
    private void _processDecodesAjax(FacesContext context)
    {
        List<String> clientIds = _getAjaxClientIds(context);
        if (clientIds == null || clientIds.isEmpty())
        {
            // If partial processing was not perfomed on any components, perform processDecodes on all components 
            // in the view.
            _processDecodesDefault(context);
        }
        else
        {
            for (String clientId : clientIds)
            {
                // For each client id in the list, using invokeOnComponent, call the respective processDecodes method 
                // on the component with that client id.
                invokeOnComponent(context, clientId, APPLY_REQUEST_VALUES_CALLBACK);
            }
            
            /*
             * Obtain an instance of a response writer that uses content type text/xml by calling 
             * FacesContext.getPartialResponseWriter(). Install the writer by calling 
             * FacesContext.setResponseWriter(javax.faces.context.ResponseWriter). 
             */
            context.setResponseWriter(context.getPartialResponseWriter());
        }
    }
    
    private void _processDecodesDefault(FacesContext context)
    {
        // If FacesContext.isAjaxRequest() returned false, or partial processing was not perfomed on any components, 
        // perform processDecodes on all components in the view.
        super.processDecodes(context);
    }
    
    private void _processUpdatesAjax(FacesContext context)
    {
        List<String> clientIds = _getAjaxClientIds(context);
        if (clientIds == null || clientIds.isEmpty())
        {
            // If partial processing was not perfomed on any components, perform processDecodes on all components 
            // in the view.
            _processUpdatesDefault(context);
        }
        else
        {
            for (String clientId : clientIds)
            {
                // For each client id in the list, using invokeOnComponent, call the respective processUpdates method 
                // on the component with that client id.
                invokeOnComponent(context, clientId, UPDATE_MODEL_CALLBACK);
            }
        }
    }
    
    private void _processUpdatesDefault(FacesContext context)
    {
        // If FacesContext.isAjaxRequest() returned false, or partial processing was not perfomed on any components, 
        // perform processUpdates on all components in the view.
        super.processUpdates(context);
    }
    
    private void _processValidatorsAjax(FacesContext context)
    {
        List<String> clientIds = _getAjaxClientIds(context);
        if (clientIds == null || clientIds.isEmpty())
        {
            // If partial processing was not perfomed on any components, perform processDecodes on all components 
            // in the view.
            _processValidatorsDefault(context);
        }
        else
        {
            for (String clientId : clientIds)
            {
                // For each client id in the list, using invokeOnComponent, call the respective processValidators 
                // method on the component with that client id.
                invokeOnComponent(context, clientId, PROCESS_VALIDATORS_CALLBACK);
            }
        }
    }
    
    private void _processValidatorsDefault(FacesContext context)
    {
        // If FacesContext.isAjaxRequest()  returned false, or partial processing was not perfomed on any components, 
        // perform processValidators on all components in the view.
        super.processValidators(context);
    }

    private static interface PhaseProcessor
    {
        public void process(FacesContext context, UIViewRoot root);
    }

    private static class ApplyRequestValuesPhaseProcessor implements PhaseProcessor
    {
        public void process(FacesContext context, UIViewRoot root)
        {
            if (context.isAjaxRequest())
            {
                root._processDecodesAjax(context);
            }
            else
            {
                root._processDecodesDefault(context);
            }
        }
    }
    
    private static class ProcessValidatorPhaseProcessor implements PhaseProcessor
    {
        public void process(FacesContext context, UIViewRoot root)
        {
            if (context.isAjaxRequest())
            {
                root._processValidatorsAjax(context);
            }
            else
            {
                root._processValidatorsDefault(context);
            }
        }
    }
    
    private static class UpdateModelPhaseProcessor implements PhaseProcessor
    {
        public void process(FacesContext context, UIViewRoot root)
        {
            if (context.isAjaxRequest())
            {
                root._processUpdatesAjax(context);
            }
            else
            {
                root._processUpdatesDefault(context);
            }
        }
    }
    
    private static class AjaxEncodeAllCallback implements ContextCallback
    {
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            // Each component's rendered markup must be wrapped as follows:
            ResponseWriter writer = context.getResponseWriter();
            
            try
            {
                // <render id="form:table"/>
                writer.startElement(AJAX_RESPONSE_RENDER, target);
                writer.writeAttribute("id", target.getClientId(context), "clientId");
                
                //  <markup>
                writer.startElement(AJAX_RESPONSE_MARKUP, target);
                
                // <![CDATA[
                writer.write(AJAX_RESPONSE_CHARACTER_DATA_START);

                // component rendered markup **
                target.encodeAll(context);
                
                // ]]>
                writer.write(AJAX_RESPONSE_CHARACTER_DATA_END);
                
                // </markup>
                writer.endElement(AJAX_RESPONSE_MARKUP);
                
                // </render>
                writer.endElement(AJAX_RESPONSE_RENDER);
            }
            catch (IOException e)
            {
                throw new CallbackIOException(e);
            }
        }
    }

    private static class ApplyRequestValuesPhaseCallback implements ContextCallback
    {
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            target.processDecodes(context);
        }
    }
    
    private static class ProcessValidatorPhaseCallback implements ContextCallback
    {
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            target.processValidators(context);
        }
    }
    
    private static class UpdateModelPhaseCallback implements ContextCallback
    {
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            target.processUpdates(context);
        }
    }
    
    private static class CallbackIOException extends RuntimeException
    {
        private IOException exception;
        
        public CallbackIOException(IOException exception)
        {
            super(exception);
        }
        
        public IOException getIOException()
        {
            return exception;
        }
    }

    private class ViewScope extends HashMap<String, Object>
    {
        @Override
        public void clear()
        {
            super.clear();

            /*
             * The returned Map must be implemented such that calling clear() on the Map causes
             * Application.publishEvent(java.lang.Class, java.lang.Object) to be called, passing
             * ViewMapDestroyedEvent.class as the first argument and this UIViewRoot instance as the second argument.
             */
            getFacesContext().getApplication().publishEvent(ViewMapCreatedEvent.class, UIViewRoot.this);
        }
    }
}
