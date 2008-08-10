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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * Creates a JSF View, which is a container that holds all of the components
 * that are part of the view.
 * <p>
 * Unless otherwise specified, all attributes accept static values or EL
 * expressions.
 * <p>
 * See the javadoc for this class in the <a
 * href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF
 * Specification</a> for further details.
 */
@JSFComponent(name="f:view", bodyContent="JSP", tagClass="org.apache.myfaces.taglib.core.ViewTag")
@JSFJspProperty(name="binding", returnType="java.lang.String", tagExcluded=true)
public class UIViewRoot extends UIComponentBase
{
    public static final String COMPONENT_TYPE = "javax.faces.ViewRoot";
    private static final String COMPONENT_FAMILY = "javax.faces.ViewRoot";

    public static final String UNIQUE_ID_PREFIX = "j_id";
    private static final int ANY_PHASE_ORDINAL = PhaseId.ANY_PHASE.getOrdinal();

    private final Logger logger = Logger.getLogger(UIViewRoot.class.getName());

    /**
     * The counter which will ensure a unique component id for every component instance in the tree that
     * doesn't have an id attribute set.
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

    private transient Lifecycle _lifecycle = null;

    private interface Processor
    {
        void process();
    }

    /**
     * Construct an instance of the UIViewRoot.
     */
    public UIViewRoot()
    {
        setRendererType(null);
    }

    public void queueEvent(FacesEvent event)
    {
        checkNull(event, "event");
        if (_events == null)
        {
            _events = new ArrayList<FacesEvent>();
        }
        _events.add(event);
    }

    public void processDecodes(final FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.APPLY_REQUEST_VALUES, new Processor()
        {
            public void process()
            {
                UIViewRoot.super.processDecodes(context);
            }
        }, true);
    }

    public void processValidators(final FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.PROCESS_VALIDATIONS, new Processor()
        {
            public void process()
            {
                UIViewRoot.super.processValidators(context);
            }
        }, true);
    }

    public void processUpdates(final FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.UPDATE_MODEL_VALUES, new Processor()
        {
            public void process()
            {
                UIViewRoot.super.processUpdates(context);
            }
        }, true);
    }

    public void processApplication(final FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.INVOKE_APPLICATION, null, true);
    }

    public void encodeBegin(FacesContext context) throws java.io.IOException
    {
        checkNull(context, "context");

        boolean skipPhase = false;

        try
        {
            skipPhase = notifyListeners(context, PhaseId.RENDER_RESPONSE,
                    getBeforePhaseListener(), true);
        }
        catch (Exception e)
        {
            // following the spec we have to swallow the exception
            logger.log(Level.SEVERE,
                    "Exception while processing phase listener: "
                            + e.getMessage(), e);
        }

        if (!skipPhase)
        {
            super.encodeBegin(context);
        }
    }

    public void encodeEnd(FacesContext context) throws java.io.IOException
    {
        checkNull(context, "context");
        super.encodeEnd(context);
        try
        {
            notifyListeners(context, PhaseId.RENDER_RESPONSE,
                    getAfterPhaseListener(), false);
        }
        catch (Exception e)
        {
            // following the spec we have to swallow the exception
            logger.log(Level.SEVERE,
                    "Exception while processing phase listener: "
                            + e.getMessage(), e);
        }
    }

    /**
     * Provides a unique id for this component instance.
     */
    public String createUniqueId()
    {
        ExternalContext extCtx = FacesContext.getCurrentInstance()
                .getExternalContext();
        StringBuilder bld = __getSharedStringBuilder();
        return extCtx.encodeNamespace(bld.append(UNIQUE_ID_PREFIX).append(
                _uniqueIdCounter++).toString());
    }

    /**
     * The locale for this view.
     * <p>
     * Defaults to the default locale specified in the faces configuration file.
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
            return (Locale) expression.getValue(getFacesContext()
                    .getELContext());
        }
        else
        {
            Object locale = getFacesContext().getApplication().getViewHandler()
                    .calculateLocale(getFacesContext());

            if (locale instanceof Locale)
            {
                return (Locale) locale;
            }
            else if (locale instanceof String)
            {
                return stringToLocale((String) locale);
            }
        }

        return getFacesContext().getApplication().getViewHandler()
                .calculateLocale(getFacesContext());
    }

    public void setLocale(Locale locale)
    {
        this._locale = locale;
    }

    private boolean process(FacesContext context, PhaseId phaseId,
            Processor processor, boolean broadcast)
    {
        if (!notifyListeners(context, phaseId, getBeforePhaseListener(), true))
        {
            if (processor != null)
                processor.process();

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
     * JSF1.2 adds the ability for PhaseListener objects to be added to a UIViewRoot instance,
     * and for "beforePhaseListener" and "afterPhaseListener" EL expressions to be defined
     * on the viewroot. This method is expected to be called at appropriate times, and will
     * then execute the relevant listener callbacks.
     * <p>
     * Parameter "listener" may be null. If not null, then it is an EL expression pointing
     * to a user method that will be invoked.
     * <p>
     * Note that the global PhaseListeners are invoked via the Lifecycle implementation, not
     * from this method here.
     */
    private boolean notifyListeners(FacesContext context, PhaseId phaseId,
            MethodExpression listener, boolean beforePhase)
    {
        boolean skipPhase = false;

        if (listener != null
                || (_phaseListeners != null && !_phaseListeners.isEmpty()))
        {
            PhaseEvent event = createEvent(context, phaseId);

            if (listener != null)
            {
                listener.invoke(context.getELContext(), new Object[]
                { event });
                skipPhase = context.getResponseComplete()
                        || context.getRenderResponse();
            }

            if (_phaseListeners != null && !_phaseListeners.isEmpty())
            {
                for (PhaseListener phaseListener : _phaseListeners)
                {
                    PhaseId listenerPhaseId = phaseListener.getPhaseId();
                    if (phaseId.equals(listenerPhaseId)
                            || PhaseId.ANY_PHASE.equals(listenerPhaseId))
                    {
                        if (beforePhase)
                        {
                            phaseListener.beforePhase(event);
                        }
                        else
                        {
                            phaseListener.afterPhase(event);
                        }
                        skipPhase = context.getResponseComplete()
                                || context.getRenderResponse();
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
            LifecycleFactory factory = (LifecycleFactory) FactoryFinder
                    .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            String id = context.getExternalContext().getInitParameter(
                    FacesServlet.LIFECYCLE_ID_ATTR);
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
        for (ListIterator<FacesEvent> listiterator = _events.listIterator(); listiterator
                .hasNext();)
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
            return (String) expression.getValue(getFacesContext()
                    .getELContext());
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
     * This inherited property is disabled. Although this class extends a base-class that
     * defines a read/write rendered property, this particular subclass does not
     * support setting it. Yes, this is broken OO design: direct all complaints
     * to the JSF spec group.
     */
    @Override
    @JSFProperty(tagExcluded=true)
    public void setRendered(boolean state)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRendered()
    {
        return true;
    }

    /**
     * DO NOT USE.
     * <p>
     * Although this class extends a base-class that defines a read/write id
     * property, it makes no sense for this particular subclass to support it.
     * The tag library does not export this property for use, but there is no
     * way to "undeclare" a java method. Yes, this is broken OO design: direct
     * all complaints to the JSF spec group.
     * <p>
     * This property should be disabled (ie throw an exception if invoked).
     * However there are currently several places that call this method (eg
     * during restoreState) so it just does the normal thing for the moment.
     * TODO: fix callers then make this throw an exception.
     * 
     * @JSFProperty tagExcluded="true"
     */
    public void setId(String id)
    {
        // throw new UnsupportedOperationException();

        // Leave enabled for now. Things like the TreeStructureManager call this,
        // even though they probably should not.
        super.setId(id);
    }

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
    public String getClientId(FacesContext context)
    {
        return null;
    }

    /**
     * A unique identifier for the "template" from which this view was generated.
     * <p>
     * Typically this is the filesystem path to the template file, but the exact
     * details are the responsibility of the current ViewHandler implementation.
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
     * Gets
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
            return (MethodExpression) expression.getValue(getFacesContext()
                    .getELContext());
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
     * Gets
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
            return (MethodExpression) expression.getValue(getFacesContext()
                    .getELContext());
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

    @Override
    public void restoreState(FacesContext facesContext, Object state)
    {
        Object[] values = (Object[]) state;
        super.restoreState(facesContext, values[0]);
        _locale = (Locale) values[1];
        _renderKitId = (String) values[2];
        _viewId = (String) values[3];
        _uniqueIdCounter = (Long) values[4];
        _phaseListeners = (List) restoreAttachedState(facesContext, values[5]);
        _beforePhaseListener = (MethodExpression) restoreAttachedState(
                facesContext, values[6]);
        _afterPhaseListener = (MethodExpression) restoreAttachedState(
                facesContext, values[7]);
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
