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

import javax.faces.component.UIOutput;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 *
 * Renders a HTML a element.  Child f:param elements are added to the href
 * attribute as query parameters.  Other children are rendered as the link text or image.
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 */
@JSFComponent
(name = "h:outputLink",
clazz = "javax.faces.component.html.HtmlOutputLink",template=true,
tagClass = "org.apache.myfaces.taglib.html.HtmlOutputLinkTag",
defaultRendererType = "javax.faces.Link"
)
abstract class _HtmlOutputLink extends UIOutput implements _AccesskeyProperty,
_UniversalProperties, _Focus_BlurProperties, _EventProperties, _StyleProperties,
_TabindexProperty, _LinkProperties
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.Output";
  static public final String COMPONENT_TYPE =
    "javax.faces.HtmlOutputLink";

}
