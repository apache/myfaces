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

import java.util.Iterator;

import javax.faces.component.UINamingContainer.PropertyKeys;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFComponent(type = "javax.faces.Form", family = "javax.faces.Form")
public class UIForm extends UIComponentBase implements NamingContainer, UniqueIdVendor
{
    // private static final Log log = LogFactory.getLog(UIForm.class);

    private boolean _submitted;

    /**
     * 
     * {@inheritDoc}
     * 
     * @since 2.0
     */
    @Override
    public String createUniqueId(FacesContext context, String seed)
    {
        ExternalContext extCtx = context.getExternalContext();
        StringBuilder bld = __getSharedStringBuilder();

        Long uniqueIdCounter = (Long) getStateHelper().get(PropertyKeys.uniqueIdCounter);
        uniqueIdCounter = (uniqueIdCounter == null) ? 0 : uniqueIdCounter;
        getStateHelper().put(PropertyKeys.uniqueIdCounter, (uniqueIdCounter+1L));
        // Generate an identifier for a component. The identifier will be prefixed with UNIQUE_ID_PREFIX, and will be unique within this UIViewRoot. 
        if(seed==null)
        {
            return extCtx.encodeNamespace(bld.append(UIViewRoot.UNIQUE_ID_PREFIX).append(uniqueIdCounter).toString());    
        }
        // Optionally, a unique seed value can be supplied by component creators which should be included in the generated unique id.
        else
        {
            return extCtx.encodeNamespace(bld.append(UIViewRoot.UNIQUE_ID_PREFIX).append(seed).toString());
        }
    }

    public boolean isSubmitted()
    {
        return _submitted;
    }

    public void setSubmitted(boolean submitted)
    {
        _submitted = submitted;
    }

    @Override
    public void processDecodes(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");
        decode(context);
        if (!isSubmitted())
            return;
        for (Iterator<UIComponent> it = getFacetsAndChildren(); it.hasNext();)
        {
            it.next().processDecodes(context);
        }
    }

    @Override
    public void processValidators(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");
        // SF issue #1050022: a form used within a datatable will loose it's submitted state
        // as UIForm is no EditableValueHolder and therefore it's state is not saved/restored by UIData
        // to restore the submitted state we call decode here again
        if (!isSubmitted())
        {
            decode(context);
        }
        if (!isSubmitted())
            return;
        for (Iterator<UIComponent> it = getFacetsAndChildren(); it.hasNext();)
        {
            it.next().processValidators(context);
        }
    }

    @Override
    public void processUpdates(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");
        // SF issue #1050022: a form used within a datatable will loose it's submitted state
        // as UIForm is no EditableValueHolder and therefore it's state is not saved/restored by UIData
        // to restore the submitted state we call decode here again
        if (!isSubmitted())
        {
            decode(context);
        }
        if (!isSubmitted())
            return;
        for (Iterator<UIComponent> it = getFacetsAndChildren(); it.hasNext();)
        {
            it.next().processUpdates(context);
        }
    }

    enum PropertyKeys
    {
         prependId,
         uniqueIdCounter
    }
    
    @Override
    public Object saveState(FacesContext context)
    {
        // The saveState() method of UIForm must call setSubmitted(false) before calling super.saveState() as an 
        // extra precaution to ensure the submitted state is not persisted across requests.
        setSubmitted(false);

        return super.saveState(context);
    }

    // ------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Form";
    public static final String COMPONENT_FAMILY = "javax.faces.Form";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Form";

    public UIForm()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    // ------------------ GENERATED CODE END ---------------------------------------

    @Override
    public String getContainerClientId(FacesContext ctx)
    {
        if (isPrependId())
        {
            return super.getContainerClientId(ctx);
        }
        UIComponent parentNamingContainer = _ComponentUtils.findParentNamingContainer(this, false);
        if (parentNamingContainer != null)
        {
            return parentNamingContainer.getContainerClientId(ctx);
        }
        return null;
    }

    @JSFProperty(defaultValue = "true")
    public boolean isPrependId()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.prependId, true);
    }

    public void setPrependId(boolean prependId)
    {
        getStateHelper().put(PropertyKeys.prependId, prependId ); 
    }

}
