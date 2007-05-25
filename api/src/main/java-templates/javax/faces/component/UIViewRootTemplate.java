/*
* Copyright 2004-2006 The Apache Software Foundation.
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

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.*;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas Berger (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.2
 */
public class UIViewRootTemplate
        extends UIComponentBase
{
    private static final int ANY_PHASE_ORDINAL = PhaseId.ANY_PHASE.getOrdinal();
    public static final String UNIQUE_ID_PREFIX = "j_id";

    private final Logger logger = Logger.getLogger(UIViewRootTemplate.class.getName());

    // todo: is it right to save the state of _events and _phaseListeners?
     /**/ // removes the generated methods so only state saving stays
     /**/// getEvents
     /**/// setEvents
     /**/// getUniqueIdCounter
     /**/// setUniqueIdCounter
     /**/// getPhaseListeners
     /**/// getLocale
     /**/ private List<FacesEvent> _events = null;
     /**/ private long _uniqueIdCounter = 0;
     /**/ private Locale _locale;
     /**/ private Collection<PhaseListener> _phaseListeners;
     /**/ private MethodExpression getBeforePhaseListener() { return null; }
     /**/ private MethodExpression getAfterPhaseListener() { return null; }
     /**/ public String getFamily() { return null; }

    private transient Lifecycle _lifecycle = null;

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
                UIViewRootTemplate.super.processDecodes(context);
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
                UIViewRootTemplate.super.processValidators(context);
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
                UIViewRootTemplate.super.processUpdates(context);
            }
        }, true);
    }

    public void processApplication(final FacesContext context)
    {
        checkNull(context, "context");
        process(context, PhaseId.INVOKE_APPLICATION, null, true);
    }

    public void encodeBegin(FacesContext context)
            throws java.io.IOException
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

        if(!skipPhase)
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
            notifyListeners(context, PhaseId.RENDER_RESPONSE, getAfterPhaseListener(), false);
        }
        catch (Exception e)
        {
            // following the spec we have to swallow the exception
            logger.log(Level.SEVERE, "Exception while processing phase listener: " + e.getMessage(), e);
        }
    }

    /*
     * Provides a unique id for this component instance.
     */
    public String createUniqueId()
    {
        ExternalContext extCtx = FacesContext.getCurrentInstance().getExternalContext();
        return extCtx.encodeNamespace(UNIQUE_ID_PREFIX + _uniqueIdCounter++);
    }

    /**
     * Gets The locale for this ViewRoot.
     *
     * @return the new locale value
     */
    public Locale getLocale()
    {
        if (_locale != null)
        {
            return _locale;
        }
        ValueExpression expression = getValueExpression("locale");
        if (expression != null)
        {
            return (Locale) expression.getValue(getFacesContext().getELContext());
        }
        else
        {
            Object locale = getFacesContext().getApplication().getViewHandler().calculateLocale(getFacesContext());

            if (locale instanceof Locale)
            {
                return (Locale) locale;
            }
            else if (locale instanceof String)
            {
                return stringToLocale((String)locale);
            }
        }

        return getFacesContext().getApplication().getViewHandler().calculateLocale(getFacesContext());
    }

    private boolean process(FacesContext context, PhaseId phaseId, Processor processor, boolean broadcast)
    {
        if (!notifyListeners(context, phaseId, getBeforePhaseListener(), true))
        {
            if (processor != null)
                processor.process();

            if (broadcast)
            {
                _broadcastForPhase(phaseId);
                if (context.getRenderResponse() || context.getResponseComplete())
                {
                    clearEvents();
                }
            }
        }
        return notifyListeners(context, phaseId, getAfterPhaseListener(), false);
    }

    private boolean notifyListeners(FacesContext context, PhaseId phaseId, MethodExpression listener, boolean beforePhase)
    {
        boolean skipPhase = false;

        if (listener != null || (_phaseListeners != null && !_phaseListeners.isEmpty()))
        {
            PhaseEvent event = createEvent(context, phaseId);

            if (listener != null)
            {
                listener.invoke(context.getELContext(), new Object[]{event});
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
            LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
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
            if (ordinal == ANY_PHASE_ORDINAL ||
                    ordinal == phaseIdOrdinal)
            {
                UIComponent source = event.getComponent();
                try
                {
                    source.broadcast(event);
                }
                catch (AbortProcessingException e)
                {
                    // abort event processing
                    // Page 3-30 of JSF 1.1 spec: "Throw an AbortProcessingException, to tell the JSF implementation
                    // that no further broadcast of this event, or any further events, should take place."
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
            // TODO: abort processing of any event of any phase or just of any event of the current phase???
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

    private interface Processor
    {
        void process();
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
                String lang = localeStr.substring(0,1);
                String country = localeStr.substring(3,4);
                return new Locale(lang,country);
            }
        }

        return Locale.getDefault();
    }

}
