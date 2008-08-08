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
import javax.faces.context.ExternalContext;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

/**
 * Creates a JSF View, which is a container that holds all of the
 * components that are part of the view.
 * <p> 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "f:view"
 *   bodyContent = "JSP"
 *   tagClass = "org.apache.myfaces.taglib.core.ViewTag"
 *   desc = "UIViewRoot"
 *
 * @JSFJspProperty name = "binding" returnType = "java.lang.String" tagExcluded = "true"
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIViewRoot
        extends UIComponentBase
{
    public static final String UNIQUE_ID_PREFIX = "_id";

    private static final int ANY_PHASE_ORDINAL = PhaseId.ANY_PHASE.getOrdinal();

    /**
     * The counter which will ensure a unique component id
     * for every component instance in the tree that doesn't have
     * an id attribute set.
     */
    private long _uniqueIdCounter = 0;

    private String _viewId = null;
    private Locale _locale = null;
    private List _events = null;

    public String getViewId()
    {
        return _viewId;
    }

    public void setViewId(String viewId)
    {
//      The spec does not require to check this, so we don't check it      
//        if (viewId == null) throw new NullPointerException("viewId");
        _viewId = viewId;
    }

    public void queueEvent(FacesEvent event)
    {
        if (event == null) throw new NullPointerException("event");
        if (_events == null)
        {
            _events = new ArrayList();
        }
        _events.add(event);
    }

    private void _broadcastForPhase(PhaseId phaseId)
    {
        if (_events == null) return;

        boolean abort = false;

        int phaseIdOrdinal = phaseId.getOrdinal();
        for (ListIterator listiterator = _events.listIterator(); listiterator.hasNext();)
        {
            FacesEvent event = (FacesEvent) listiterator.next();
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
                } finally {

                    try
                    {
                        listiterator.remove();
                    }
                    catch(ConcurrentModificationException cme)
                    {
                        int eventIndex = listiterator.previousIndex();
                        _events.remove(eventIndex);
                        listiterator = _events.listIterator();
                    }
                }
            }
        }

        if (abort) {
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
        if (context == null) throw new NullPointerException("context");
        super.processDecodes(context);
        _broadcastForPhase(PhaseId.APPLY_REQUEST_VALUES);
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
    }

    public void processValidators(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        super.processValidators(context);
        _broadcastForPhase(PhaseId.PROCESS_VALIDATIONS);
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
    }

    public void processUpdates(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        super.processUpdates(context);
        _broadcastForPhase(PhaseId.UPDATE_MODEL_VALUES);
        if (context.getRenderResponse() || context.getResponseComplete())
        {
            clearEvents();
        }
    }

    public void processApplication(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
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
     * The locale of this view.  Default: the default locale from the configuration file.
     * 
     * @JSFProperty
     */    
    public Locale getLocale()
    {
        if (_locale != null) return _locale;
        ValueBinding vb = getValueBinding("locale");
        FacesContext facesContext = getFacesContext();
        if (vb == null)
        {
            return facesContext.getApplication().getViewHandler().calculateLocale(facesContext);
        }
        Object locale = vb.getValue(facesContext);
        if (locale == null)
        {
            return facesContext.getApplication().getViewHandler().calculateLocale(facesContext);
        }
        if (locale instanceof Locale)
        {
            return (Locale)locale;
        }
        else if (locale instanceof String)
        {
            return getLocale((String)locale);
        }
        else
        {
            throw new IllegalArgumentException("locale binding"); //TODO: not specified!?
        }
    }

    /**
     * Create Locale from String representation.
     *
     * http://java.sun.com/j2se/1.4.2/docs/api/java/util/Locale.html
     *
     * @param locale locale representation in String.
     * @return Locale instance
     */
    private static Locale getLocale(String locale){
        int cnt = 0;
        int pos = 0;
        int prev = 0;

        // store locale variation.
        // ex. "ja_JP_POSIX"
        //  lv[0] : language(ja)
        //  lv[1] : country(JP)
        //  lv[2] : variant(POSIX)
        String[] lv = new String[3];
        Locale l=null;

        while((pos=locale.indexOf('_',prev))!=-1){
             lv[cnt++] = locale.substring(prev,pos);
             prev = pos + 1;
        }

        lv[cnt++] = locale.substring(prev,locale.length());

        switch(cnt){
            case 1:
                // create Locale from language.
                l = new Locale(lv[0]);
                break;
            case 2:
                // create Locale from language and country.
                l = new Locale(lv[0],lv[1]);
                break;
            case 3:
                // create Locale from language, country and variant.
                l = new Locale(lv[0], lv[1], lv[2]);
                break;
        }
        return l;
    }


    public void setLocale(Locale locale)
    {
        _locale = locale;
    }

    public static final String COMPONENT_TYPE = "javax.faces.ViewRoot";
    public static final String COMPONENT_FAMILY = "javax.faces.ViewRoot";
    //private static final String DEFAULT_RENDERKITID = RenderKitFactory.HTML_BASIC_RENDER_KIT;

    private String _renderKitId = null;

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    public void setRenderKitId(String renderKitId)
    {
        _renderKitId = renderKitId;
    }

    public String getRenderKitId()
    {
        if (_renderKitId != null) return _renderKitId;
        ValueBinding vb = getValueBinding("renderKitId");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null; //DEFAULT_RENDERKITID
    }

    /**
     * Disable this property; although this class extends a base-class that
     * defines a read/write rendered property, this particular subclass
     * does not support setting it. Yes, this is broken OO design: direct
     * all complaints to the JSF spec group.
     *
     * @JSFProperty tagExcluded="true"
     */
    public void setRendered(boolean state)
    {
       //It should throw UnsupportedOperationException
       //throw new UnsupportedOperationException();
       //Restored due to compatibility with TCK tests.
       super.setRendered(state);
    }

    public boolean isRendered()
    {
        //return true;
        //Restored due to compatibility with TCK tests.
        return super.isRendered();
    }

    /**
     * DO NOT USE.
     * <p>
     * Although this class extends a base-class that defines a read/write
     * id property, it makes no sense for this particular subclass to support
     * it. The tag library does not export this property for use, but there 
     * is no way to "undeclare" a java method. Yes, this is broken OO design:
     * direct all complaints to the JSF spec group.
     * <p>
     * This property should be disabled (ie throw an exception if invoked).
     * However there are currently several places that call this method
     * (eg during restoreState) so it just does the normal thing for the
     * moment. TODO: fix callers then make this throw an exception.
     *
     * @JSFProperty tagExcluded="true"
     */
    public void setId(String id)
    {
        //  throw new UnsupportedOperationException();

        // Re-enable for now. Things like the TreeStructureManager call this,
        // even though they probably should not.
        super.setId(id);
    }

    public String getId()
    {
        // return null;

        // Re-enable for now.
        return super.getId();
    }

    /**
     * As this component has no "id" property, it has no clientId property either.
     */
    public String getClientId(FacesContext context)
    {
        //return null;
        //Restored due to compatibility with TCK tests.
        return super.getClientId(context);
    }

    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = _locale;
        values[2] = _renderKitId;
        values[3] = _viewId;
        values[4] = new Long(_uniqueIdCounter);
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _locale = (Locale)values[1];
        _renderKitId = (String)values[2];
        _viewId = (String)values[3];
        _uniqueIdCounter = values[4]==null?0:((Long)values[4]).longValue();
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
