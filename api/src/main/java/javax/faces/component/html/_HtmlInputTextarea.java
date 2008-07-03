// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.

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

import javax.faces.component.UIInput;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 *
 * Renders a HTML textarea element.
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
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
(name = "h:inputTextarea",
clazz = "javax.faces.component.html.HtmlInputTextarea",template=true,
tagClass = "org.apache.myfaces.taglib.html.HtmlInputTextareaTag",
defaultRendererType = "javax.faces.Textarea"
)
abstract class _HtmlInputTextarea extends UIInput implements _AccesskeyProperty,
    _UniversalProperties, _Focus_BlurProperties, _Change_SelectProperties,
    _EventProperties, _StyleProperties, _TabindexProperty, 
    _Disabled_ReadonlyProperties, _LabelProperty
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.Input";
  static public final String COMPONENT_TYPE =
    "javax.faces.HtmlInputTextarea";

  /**
   * HTML: The width of this element, in characters.
   * 
   * @JSFProperty
   *   defaultValue = "Integer.MIN_VALUE"
   */
  public abstract int getCols();
  
  /**
   * HTML: The height of this element, in characters.
   * 
   * @JSFProperty
   *   defaultValue = "Integer.MIN_VALUE"
   */
  public abstract int getRows();

}
