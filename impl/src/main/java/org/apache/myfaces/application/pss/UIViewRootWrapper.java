package org.apache.myfaces.application.pss;

import javax.faces.component.UIViewRoot;
import javax.faces.component.UIComponentBase;
import javax.faces.event.FacesEvent;
import javax.faces.context.FacesContext;
import java.util.*;
import java.io.IOException;

/*
 * Copyright 2004 The Apache Software Foundation.
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

/**
 * @author Martin Haimberger
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
