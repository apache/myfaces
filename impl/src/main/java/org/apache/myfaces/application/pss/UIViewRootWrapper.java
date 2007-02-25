package org.apache.myfaces.application.pss;

import javax.faces.component.UIViewRoot;
import javax.faces.component.UIComponentBase;
import javax.faces.event.FacesEvent;
import javax.faces.context.FacesContext;
import java.util.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: retro
 * Date: Feb 23, 2007
 * Time: 1:12:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIViewRootWrapper extends UIViewRoot
{

    private UIViewRoot _originalViewRoot;

    public UIViewRootWrapper( UIViewRoot orUiViewRoot) {
        _originalViewRoot = orUiViewRoot;
    }

    public boolean getRendersChildren() {
        return true;
    }



    public String getViewId()
    {
        return _originalViewRoot.getViewId();
    }

    public void setViewId(String viewId)
    {
        _originalViewRoot.setViewId(viewId);
    }

    public void queueEvent(FacesEvent event)
    {
        _originalViewRoot.queueEvent(event);
    }

    public void processDecodes(FacesContext context)
    {
      _originalViewRoot.processDecodes(context);
    }

    public void processValidators(FacesContext context)
    {
       _originalViewRoot.processValidators(context);
    }

    public void processUpdates(FacesContext context)
    {
        _originalViewRoot.processUpdates(context);
    }

    public void processApplication(FacesContext context)
    {
        _originalViewRoot.processApplication(context);
    }

    public void encodeBegin(FacesContext context)
            throws java.io.IOException
    {
       _originalViewRoot.encodeBegin(context);
    }

    /* Provides a unique id for this component instance.
    */
    public String createUniqueId()
    {
       return _originalViewRoot.createUniqueId();
    }

    public Locale getLocale()
    {
       return _originalViewRoot.getLocale();
    }


    public void setLocale(Locale locale)
    {
        _originalViewRoot.setLocale(locale);
    }

    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.ViewRoot";
    public static final String COMPONENT_FAMILY = "javax.faces.ViewRoot";
    //private static final String DEFAULT_RENDERKITID = RenderKitFactory.HTML_BASIC_RENDER_KIT;

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    public void setRenderKitId(String renderKitId)
    {
        _originalViewRoot.setRenderKitId(renderKitId);
    }

    public String getRenderKitId()
    {
        return _originalViewRoot.getRenderKitId();
    }



    public Object saveState(FacesContext context)
    {
       return _originalViewRoot.saveState(context);
    }

    public void restoreState(FacesContext context, Object state)
    {
        _originalViewRoot.restoreState(context,state);
    }
    //------------------ GENERATED CODE END ---------------------------------------


}
