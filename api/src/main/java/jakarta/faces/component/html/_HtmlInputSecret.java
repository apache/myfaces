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

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * Renders as an HTML input tag with its type set to "password".
 */
@JSFComponent(name = "h:inputSecret",
        clazz = "jakarta.faces.component.html.HtmlInputSecret",template=true,
        tagClass = "org.apache.myfaces.taglib.html.HtmlInputSecretTag",
        defaultRendererType = "jakarta.faces.Secret",
        implementz = "jakarta.faces.component.behavior.ClientBehaviorHolder",
        defaultEventName = "valueChange")
abstract class _HtmlInputSecret extends UIInput implements _AccesskeyProperty,
    _UniversalProperties, _FocusBlurProperties, _EventProperties,
    _StyleProperties, _TabindexProperty, _ChangeProperty, _SelectProperty,
    _DisabledReadonlyProperties, _LabelProperty, _RoleProperty
{

  static public final String COMPONENT_FAMILY =
    "jakarta.faces.Input";
  static public final String COMPONENT_TYPE =
    "jakarta.faces.HtmlInputSecret";

  /**
   * HTML: The maximum number of characters allowed to be entered.
   * 
   * @JSFProperty
   *   defaultValue = "Integer.MIN_VALUE"
   */
  public abstract int getMaxlength();
  
  /**
   * If true, the value will be re-sent (in plaintext) when the form
   * is rerendered (see Faces.7.4.4). Default is false.
   * 
   * @JSFProperty
   *   defaultValue = "false"
   */
  public abstract boolean isRedisplay();
  
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
   * @since 1.2
   */
  @JSFProperty
  public abstract String getAutocomplete();
  
}
