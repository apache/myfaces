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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.el.ValueExpression;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * @author Andreas Berger (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.2
 */
public class UIViewRootTemplate
        extends UIComponentBase
{
    private static final int ANY_PHASE_ORDINAL = PhaseId.ANY_PHASE.getOrdinal();
    public static final String UNIQUE_ID_PREFIX = "_id";

    // todo: is it right to save the state of _events and _phaseListeners?
    /**/ // removes the generated methods so only state saving stays
    /**///getEvents
    /**///setEvents
    /**///getUniqueIdCounter
    /**///setUniqueIdCounter
    /**/private List<FacesEvent> _events = null;
    /**/private long _uniqueIdCounter = 0;
    /**/private Locale _locale;

    public void queueEvent(FacesEvent event)
    {
        if (event == null)
        {
            throw new NullPointerException("event");
        }
        if (_events == null)
        {
            _events = new ArrayList<FacesEvent>();
        }
        _events.add(event);
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
                    //  that no further broadcast of this event, or any further events, should take place."
                    abort = true;
                    break;
                } finally
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


    public void processDecodes(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        super.processDecodes(context);
        _broadcastForPhase(PhaseId.APPLY_REQUEST_VALUES);
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
    }

    public void processValidators(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        super.processValidators(context);
        _broadcastForPhase(PhaseId.PROCESS_VALIDATIONS);
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
    }

    public void processUpdates(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        super.processUpdates(context);
        _broadcastForPhase(PhaseId.UPDATE_MODEL_VALUES);
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
    }

    public void processApplication(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        _broadcastForPhase(PhaseId.INVOKE_APPLICATION);
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
    }

    public void encodeBegin(FacesContext context)
            throws java.io.IOException
    {
        clearEvents();
        super.encodeBegin(context);
    }

    /* Provides a unique id for this component instance.
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
    /**///getLocale
    public Locale getLocale()
    {
        if (_locale != null)
        {
            return _locale;
        }
        ValueExpression expression = getValueExpression("locale");
        FacesContext facesContext = getFacesContext();
        if (expression == null)
        {
            return facesContext.getApplication().getViewHandler().calculateLocale(facesContext);
        }
        Object locale = expression.getValue(getFacesContext().getELContext());
        if (locale == null)
        {
            return facesContext.getApplication().getViewHandler().calculateLocale(facesContext);
        }
        if (locale instanceof Locale)
        {
            return (Locale) locale;
        } else if (locale instanceof String)
        {
            return getLocale((String) locale);
        } else
        {
            //TODO: not specified!?
            throw new IllegalArgumentException("locale binding");
        }
    }


    /**
     * Create Locale from String representation.
     * <p/>
     * http://java.sun.com/j2se/1.4.2/docs/api/java/util/Locale.html
     *
     * @param locale locale representation in String.
     * @return Locale instance
     */
    private static Locale getLocale(String locale)
    {
        int cnt = 0;
        int pos = 0;
        int prev = 0;

        // store locale variation.
        // ex. "ja_JP_POSIX"
        //  lv[0] : language(ja)
        //  lv[1] : country(JP)
        //  lv[2] : variant(POSIX)
        String[] lv = new String[3];
        Locale l = null;

        while ((pos = locale.indexOf('_', prev)) != -1)
        {
            lv[cnt++] = locale.substring(prev, pos);
            prev = pos + 1;
        }

        lv[cnt++] = locale.substring(prev, locale.length());

        switch (cnt)
        {
            case 1:
                // create Locale from language.
                l = new Locale(lv[0]);
                break;
            case 2:
                // create Locale from language and country.
                l = new Locale(lv[0], lv[1]);
                break;
            case 3:
                // create Locale from language, country and variant.
                l = new Locale(lv[0], lv[1], lv[2]);
                break;
        }
        return l;
    }   
}
