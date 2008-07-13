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

import javax.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;


/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFComponent(type = "javax.faces.Form",
    family = "javax.faces.Form")
public class UIForm
        extends UIComponentBase
        implements NamingContainer
{
    //private static final Log log = LogFactory.getLog(UIForm.class);

    private boolean _submitted;
    
    private Boolean _prependId;
    
    public boolean isSubmitted()
    {
        return _submitted;
    }

    public void setSubmitted(boolean submitted)
    {
        _submitted = submitted;
    }

    public void processDecodes(javax.faces.context.FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        decode(context);
        if (!isSubmitted()) return;
        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processDecodes(context);
        }
    }

    public void processValidators(javax.faces.context.FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        // SF issue #1050022: a form used within a datatable will loose it's submitted state
        // as UIForm is no EditableValueHolder and therefore it's state is not saved/restored by UIData
        // to restore the submitted state we call decode here again
        if (!isSubmitted()) {
            decode(context);
        }
        if (!isSubmitted()) return;
        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processValidators(context);
        }
    }

    public void processUpdates(javax.faces.context.FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        // SF issue #1050022: a form used within a datatable will loose it's submitted state
        // as UIForm is no EditableValueHolder and therefore it's state is not saved/restored by UIData
        // to restore the submitted state we call decode here again
        if (!isSubmitted()) {
            decode(context);
        }
        if (!isSubmitted()) return;
        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processUpdates(context);
        }
    }

    public Object saveState(javax.faces.context.FacesContext context)
    {
        Object[] state = new Object[2];
        state[0] = super.saveState(context);
        state[1] = _prependId;
        return state;
    }
    
    @Override
    public void restoreState(FacesContext context, Object state)
    {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        _prependId = (Boolean) values[1];
    }

    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Form";
    public static final String COMPONENT_FAMILY = "javax.faces.Form";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Form";


    public UIForm()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    //------------------ GENERATED CODE END ---------------------------------------
    
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
        return getExpressionValue("prependId", _prependId, true);
    }

    public void setPrependId(boolean prependId)
    {
        _prependId = prependId;
    }
    
}
