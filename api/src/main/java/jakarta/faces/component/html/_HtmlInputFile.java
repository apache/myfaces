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
package jakarta.faces.component.html;

import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.core.api.shared.MessageUtils;

/**
 * Renders a HTML input element.
 *
 */
@JSFComponent(name = "h:inputFile",
    clazz = "jakarta.faces.component.html.HtmlInputFile",
    template = true,
    tagClass = "org.apache.myfaces.taglib.html.HtmlInputFileTag",
    defaultRendererType = "jakarta.faces.File",
    implementz = "jakarta.faces.component.behavior.ClientBehaviorHolder",
    defaultEventName = "valueChange"
)
abstract class _HtmlInputFile extends UIInput implements _AccesskeyProperty,
    _AltProperty, _UniversalProperties, _DisabledReadonlyProperties,
    _FocusBlurProperties, _ChangeProperty, _SelectProperty,
    _EventProperties, _StyleProperties, _TabindexProperty, _LabelProperty,
    _RoleProperty
{

    static public final String COMPONENT_FAMILY = "jakarta.faces.Input";
    static public final String COMPONENT_TYPE = "jakarta.faces.HtmlInputFile";

    /**
     * HTML: The maximum number of characters allowed to be entered.
     * 
     * @JSFProperty
     *   defaultValue = "Integer.MIN_VALUE"
     */
    public abstract int getMaxlength();

    /**
     * HTML: The initial width of this control, in characters.
     * 
     * @JSFProperty
     *   defaultValue = "Integer.MIN_VALUE"
     */
    public abstract int getSize();

    /**
     * If the value of this attribute is "off", render "off" as the value of the attribute.
     * This indicates that the browser should disable its autocomplete feature for this component.
     * This is useful for components that perform autocompletion and do not want the browser interfering.
     * If this attribute is not set or the value is "on", render nothing.
     *
     * @return  the new autocomplete value
     */
    @JSFProperty
    public abstract String getAutocomplete();

    /**
     * Comma separated string of mime types of files to filter in client side file browse dialog.
     * NOTE: this is not validated in server side.
     *
     * @return
     */
    @JSFProperty
    public abstract String getAccept();

    /**
     * Flag indicating that this element must allow multiple file selection. A value
     * of false causes no attribute to be rendered, while a value of true causes the attribute to be rendered as
     * multiple="multiple".
     *
     * @return the multiple value
     */
    @JSFProperty(defaultValue="false")
    public abstract boolean isMultiple();

    @Override
    protected void validateValue(FacesContext context, Object convertedValue)
    {
        if (!isValid())
        {
            return;
        }

        // If our value is empty, check the required property
        boolean isEmpty = isEmptyValue(convertedValue); 

        if (isRequired() && isEmpty)
        {
            if (getRequiredMessage() != null)
            {
                String requiredMessage = getRequiredMessage();
                context.addMessage(this.getClientId(context), new jakarta.faces.application.FacesMessage(
                        jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                    requiredMessage, requiredMessage));
            }
            else
            {
                MessageUtils.addErrorMessage(context, this, REQUIRED_MESSAGE_ID,
                    new Object[] { MessageUtils.getLabel(context, this) });
            }
            setValid(false);
            return;
        }

        if (!isEmpty)
        {
            super.validateValue(context, convertedValue);
        }
    }
    
    private static boolean isEmptyValue(Object value)
    {
        if (value == null)
        {
            return true;
        }
        else if (value instanceof String)
        {
            if ( ((String)value).trim().length() <= 0 )
            {
                return true;
            }
        }
        else if (value instanceof java.util.Collection)
        {
            if ( ((java.util.Collection)value).isEmpty())
            {
                return true;
            }
        }
        else if (value.getClass().isArray())
        {
            if (java.lang.reflect.Array.getLength(value) <= 0)
            {
                return true;
            }
        }
        else if (value instanceof java.util.Map)
        {
            if ( ((java.util.Map)value).isEmpty())
            {
                return true;
            }
        }
        else if (value instanceof jakarta.servlet.http.Part) 
        {
            if (((jakarta.servlet.http.Part)value).getSize() <= 0) 
            {
                return true;
            }
        }
        return false;
    }

}
