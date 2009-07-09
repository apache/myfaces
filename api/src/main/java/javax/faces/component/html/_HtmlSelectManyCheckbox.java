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
package javax.faces.component.html;

import javax.faces.component.UISelectMany;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * Allow the user to select zero or more items from a set of available options.
 * 
 * This is presented as a table with one cell per available option; each cell contains a
 * checkbox and the option's label. The "layout" attribute determines whether the checkboxes
 * are laid out horizontally or vertically.
 * <p>
 * The set of available options is defined by adding child
 * f:selectItem or f:selectItems components to this component.
 * <p>
 * The value attribute must be a value-binding expression to a
 * property of type List, Object array or primitive array. That
 * "collection" is expected to contain objects of the same type as
 * SelectItem.getValue() returns for the child SelectItem objects.
 * On rendering, any child whose value is in the list will be
 * selected initially. During the update phase, the property setter
 * is called to replace the original collection with a completely
 * new collection object of the appropriate type. The new collection
 * object contains the value of each child SelectItem object that
 * is currently selected.
 *
 * <h4>Events:</h4>
 * <table border="1" width="100%" cellpadding="3" summary="">
 * <tr bgcolor="#CCCCFF" class="TableHeadingColor">
 * <th align="left">Type</th>
 * <th align="left">Phases</th>
 * <th align="left">Description</th>
 * </tr>
 * <tr class="TableRowColor">
 * <td valign="top"><code>javax.faces.event.ValueChangeEvent</code></td>
 * <td valign="top" nowrap></td>
 * <td valign="top">The valueChange event is delivered when the value
                attribute is changed.</td>
 * </tr>
 * </table>
 */
@JSFComponent
(name = "h:selectManyCheckbox",
clazz = "javax.faces.component.html.HtmlSelectManyCheckbox",template=true,
tagClass = "org.apache.myfaces.taglib.html.HtmlSelectManyCheckboxTag",
defaultRendererType = "javax.faces.Checkbox",
implementz = "javax.faces.component.behavior.ClientBehaviorHolder"
)
abstract class _HtmlSelectManyCheckbox extends UISelectMany implements 
    _AccesskeyProperty, _UniversalProperties, _FocusBlurProperties,
    _ChangeSelectProperties, _EventProperties, _StyleProperties,
    _TabindexProperty, _DisabledReadonlyProperties, 
    _DisabledClassEnabledClassProperties, _LabelProperty
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.SelectMany";
  static public final String COMPONENT_TYPE =
    "javax.faces.HtmlSelectManyCheckbox";

  /**
   * HTML: Specifies the width of the border of this element, in pixels.  Deprecated in HTML 4.01.
   * 
   * @JSFProperty
   *   defaultValue="Integer.MIN_VALUE"
   */
  public abstract int getBorder();
  
  /**
   * Controls the layout direction of the child elements.  Values include:  
   * lineDirection (vertical) and pageDirection (horzontal).
   * 
   * @JSFProperty
   */
  public abstract String getLayout();

}
