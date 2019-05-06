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
package org.apache.myfaces.renderkit.html.base;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.component.html.HtmlSelectManyMenu;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.renderkit.html.util.HTML;

/**
 * X-CHECKED: tlddoc of h:selectManyListbox
 */
public class HtmlMenuRendererBase
        extends HtmlSelectableRendererBase
{

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, null);
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (component instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(
                        facesContext, facesContext.getResponseWriter());
            }
        }

        if (component instanceof UISelectMany)
        {
            renderMenu(facesContext,
                                         (UISelectMany)component,
                                         isDisabled(facesContext, component),
                                         getConverter(facesContext, component));
        }
        else if (component instanceof UISelectOne)
        {
            renderMenu(facesContext,
                                         (UISelectOne)component,
                                         isDisabled(facesContext, component),
                                         getConverter(facesContext, component));
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + component.getClass().getName());
        }
    }
    
    protected void renderMenu(FacesContext facesContext,
            UISelectOne selectOne, boolean disabled, Converter converter)
            throws IOException
    {
        internalRenderSelect(facesContext, selectOne, disabled, 1, false,
                converter);
    }

    protected void renderMenu(FacesContext facesContext,
            UISelectMany selectMany, boolean disabled, Converter converter)
            throws IOException
    {
        internalRenderSelect(facesContext, selectMany, disabled, 1, true, converter);
    }

    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        if (uiComponent instanceof HtmlSelectManyMenu)
        {
            return ((HtmlSelectManyMenu)uiComponent).isDisabled();
        }
        else if (uiComponent instanceof HtmlSelectOneMenu)
        {
            return ((HtmlSelectOneMenu)uiComponent).isDisabled();
        }

        return RendererUtils.getBooleanAttribute(uiComponent, HTML.DISABLED_ATTR, false);
    }

    @Override
    public void decode(FacesContext facesContext, UIComponent uiComponent)
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, null);

        if (uiComponent instanceof UISelectMany)
        {
            HtmlRendererUtils.decodeUISelectMany(facesContext, uiComponent);
        }
        else if (uiComponent instanceof UISelectOne)
        {
            HtmlRendererUtils.decodeUISelectOne(facesContext, uiComponent);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + uiComponent.getClass().getName());
        }
        if (uiComponent instanceof ClientBehaviorHolder &&
                !HtmlRendererUtils.isDisabled(uiComponent))
        {
            HtmlRendererUtils.decodeClientBehaviors(facesContext, uiComponent);
        }
    }

    @Override
    public Object getConvertedValue(FacesContext facesContext, UIComponent uiComponent, Object submittedValue)
         throws ConverterException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, null);

        if (uiComponent instanceof UISelectMany)
        {
            return RendererUtils.getConvertedUISelectManyValue(facesContext,
                                                               (UISelectMany)uiComponent,
                                                               submittedValue);
        }
        else if (uiComponent instanceof UISelectOne)
        {
            return RendererUtils.getConvertedUISelectOneValue(facesContext,
                                                           (UISelectOne)uiComponent,
                                                           submittedValue);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + uiComponent.getClass().getName());
        }
    }
    
    /**
     * Gets the converter for the given component rendered by this renderer.
     * @param facesContext
     * @param component
     * @return
     */
    protected Converter getConverter(FacesContext facesContext,
            UIComponent component)
    {
        if (component instanceof UISelectMany)
        {
            return HtmlRendererUtils.findUISelectManyConverterFailsafe(facesContext, 
                    (UISelectMany) component);
        }
        else if (component instanceof UISelectOne)
        {
            return HtmlRendererUtils.findUIOutputConverterFailSafe(facesContext, component);
        }
        return null;
    }

}
