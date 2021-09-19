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

import jakarta.faces.component.UISelectOne;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * Allow the user to choose one option from a set of options.
 * <p>
 * Renders as an HTML table element, containing an input element for
 * each child f:selectItem or f:selectItems elements.  The input
 * elements are rendered as type radio.
 * </p>
 * <p>
 * The value attribute of this component is read to determine
 * which of the available options is initially selected; its value should
 * match the "value" property of one of the child SelectItem objects.
 * </p>
 * <p>
 * On submit of the enclosing form, the value attribute's bound property
 * is updated to contain the "value" property from the chosen SelectItem.
 * </p>
 *
 */
@JSFComponent(name = "h:selectOneRadio",
        clazz = "jakarta.faces.component.html.HtmlSelectOneRadio",template=true,
        tagClass = "org.apache.myfaces.taglib.html.HtmlSelectOneRadioTag",
        defaultRendererType = "jakarta.faces.Radio",
        implementz = "jakarta.faces.component.behavior.ClientBehaviorHolder",
        defaultEventName = "valueChange")
abstract class _HtmlSelectOneRadio extends UISelectOne implements
    _AccesskeyProperty, _UniversalProperties, _DisabledReadonlyProperties,
    _FocusBlurProperties, _ChangeProperty, _SelectProperty,
    _EventProperties, _StyleProperties, _TabindexProperty,
    _DisabledClassEnabledClassProperties, _LabelProperty, _RoleProperty
{

  static public final String COMPONENT_FAMILY =
    "jakarta.faces.SelectOne";
  static public final String COMPONENT_TYPE =
    "jakarta.faces.HtmlSelectOneRadio";

  /**
   * Width in pixels of the border to be drawn around the table containing the options list.
   * 
   * @JSFProperty
   *   defaultValue="Integer.MIN_VALUE"
   */
  public abstract int getBorder();

  /**
   * Controls the layout direction of the child elements.  Values include:
   * lineDirection" (vertical), "pageDirection" (horizontal),
   * and "list" (use LIST element instead of TABLE). Default value is "lineDirection".
   * 
   * @JSFProperty
   */
  public abstract String getLayout();

  /**
   * 
   * @since 2.3
   * @return 
   */
  @JSFProperty
  public abstract String getGroup();
}
