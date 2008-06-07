// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.

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
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperties;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 *
 * The root element.
 */
@JSFComponent
@JSFJspProperties(properties={
@JSFJspProperty(name = "rendered",returnType = "boolean",tagExcluded = true),
@JSFJspProperty(name = "binding",returnType = "java.lang.String",tagExcluded = true),
@JSFJspProperty(name = "id",returnType = "java.lang.String",tagExcluded = true)})
public class UIViewRoot extends UIComponentBase
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.ViewRoot";
  static public final String COMPONENT_TYPE =
    "javax.faces.ViewRoot";

  /**
   * Construct an instance of the UIViewRoot.
   */
  public UIViewRoot()
  {
    setRendererType(null);
  }
      private static final int ANY_PHASE_ORDINAL = PhaseId.ANY_PHASE.getOrdinal();
    public static final String UNIQUE_ID_PREFIX = "j_id";

    private final Logger logger = Logger.getLogger(UIViewRoot.class.getName());

    // todo: is it right to save the state of _events and _phaseListeners?

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
        StringBuilder bld = __getSharedStringBuilder();
        return extCtx.encodeNamespace(bld.append(UNIQUE_ID_PREFIX).append(_uniqueIdCounter++).toString());
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


  // Property: locale
  private Locale _locale;

  /**
   * Sets The locale for this ViewRoot.
   * 
   * @param locale  the new locale value
   */
  public void setLocale(Locale locale)
  {
    this._locale = locale;
  }

  // Property: renderKitId
  private String _renderKitId;

  /**
   * Gets The initial value of this component.
   *
   * @return  the new renderKitId value
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

  /**
   * Sets The initial value of this component.
   * 
   * @param renderKitId  the new renderKitId value
   */
  public void setRenderKitId(String renderKitId)
  {
    this._renderKitId = renderKitId;
  }

  // Property: viewId
  private String _viewId;

  /**
   * Gets The viewId.
   *
   * @return  the new viewId value
   */
  @JSFProperty
  (tagExcluded = true)
  public String getViewId()
  {
    return _viewId;
  }

  /**
   * Sets The viewId.
   * 
   * @param viewId  the new viewId value
   */
  public void setViewId(String viewId)
  {
    this._viewId = viewId;
  }

  // Property: events
  private List<FacesEvent> _events;

  // Property: uniqueIdCounter
  private long _uniqueIdCounter = 0;

  // Property: phaseListeners
  private List<PhaseListener> _phaseListeners;

  /**
   * Adds a The phaseListeners attached to ViewRoot.
   */
  public void addPhaseListener( PhaseListener phaseListener)
  {
    if (phaseListener == null) throw new NullPointerException("phaseListener");
    if (_phaseListeners == null)
      _phaseListeners = new ArrayList<PhaseListener>();

    _phaseListeners.add(phaseListener);
  }

  /**
   * Removes a The phaseListeners attached to ViewRoot.
   */
  public void removePhaseListener( PhaseListener phaseListener)
  {
    if (phaseListener == null || _phaseListeners == null)
      return;

    _phaseListeners.remove(phaseListener);
  }

  // Property: beforePhaseListener
  private MethodExpression _beforePhaseListener;

  /**
   * Gets 
   *
   * @return  the new beforePhaseListener value
   */
  @JSFProperty
  (stateHolder = true,
  returnSignature = "void",
  methodSignature = "javax.faces.event.PhaseEvent",
  jspName = "beforePhase")
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
   * @param beforePhaseListener  the new beforePhaseListener value
   */
  public void setBeforePhaseListener(MethodExpression beforePhaseListener)
  {
    this._beforePhaseListener = beforePhaseListener;
  }

  // Property: afterPhaseListener
  private MethodExpression _afterPhaseListener;

  /**
   * Gets 
   *
   * @return  the new afterPhaseListener value
   */
  @JSFProperty
  (stateHolder = true,
  returnSignature = "void",
  methodSignature = "javax.faces.event.PhaseEvent",
  jspName = "afterPhase")
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
   * @param afterPhaseListener  the new afterPhaseListener value
   */
  public void setAfterPhaseListener(MethodExpression afterPhaseListener)
  {
    this._afterPhaseListener = afterPhaseListener;
  }

  @Override
  public Object saveState(FacesContext facesContext)
  {
    Object[] values = new Object[10];
    values[0] = super.saveState(facesContext);
    values[1] = _locale;
    values[2] = _renderKitId;
    values[3] = _viewId;
    values[4] = saveAttachedState(facesContext, _events);
    values[5] = _uniqueIdCounter;
    values[7] = saveAttachedState(facesContext, _phaseListeners);
    values[8] = saveAttachedState(facesContext, _beforePhaseListener);
    values[9] = saveAttachedState(facesContext, _afterPhaseListener);

    return values;
  }

  @Override
  public void restoreState(FacesContext facesContext, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(facesContext,values[0]);
    _locale = (Locale)values[1];
    _renderKitId = (String)values[2];
    _viewId = (String)values[3];
    _events = (List)restoreAttachedState(facesContext, values[4]);
    _uniqueIdCounter = (Long)values[5];
    _phaseListeners = (List) restoreAttachedState(facesContext, values[7]);
    _beforePhaseListener = (MethodExpression)restoreAttachedState(facesContext, values[8]);
    _afterPhaseListener = (MethodExpression)restoreAttachedState(facesContext, values[9]);
  }

  @Override
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }
}
