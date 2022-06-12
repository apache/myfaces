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

import jakarta.faces.component.UIOutput;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * Renders a HTML label element.
 * 
 * In addition to the Faces specification, MyFaces allows it to directly give an output text via the "value" attribute.
 */
@JSFComponent(name = "h:outputLabel",
        clazz = "jakarta.faces.component.html.HtmlOutputLabel",template=true,
        tagClass = "org.apache.myfaces.taglib.html.HtmlOutputLabelTag",
        defaultRendererType = "jakarta.faces.Label",
        implementz = "jakarta.faces.component.behavior.ClientBehaviorHolder")
abstract class _HtmlOutputLabel extends UIOutput implements _FocusBlurProperties,
_EventProperties, _StyleProperties, _UniversalProperties, _AccesskeyProperty,
_TabindexProperty, _EscapeProperty, _RoleProperty
{

  static public final String COMPONENT_FAMILY =
    "jakarta.faces.Output";
  static public final String COMPONENT_TYPE =
    "jakarta.faces.HtmlOutputLabel";

  /**
   * The client ID of the target input element of this label.
   * 
   * @JSFProperty
   */
  public abstract String getFor();

}
