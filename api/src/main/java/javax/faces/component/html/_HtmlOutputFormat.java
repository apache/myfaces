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

/**
 * Renders as text, applying the child f:param values to the value
 * attribute as a MessageFormat string.  If this element has an ID
 * or CSS style properties, the text is wrapped in a span element.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:outputFormat"
 *   class = "javax.faces.component.html.HtmlOutputFormat"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlOutputFormatTag"
 *   template = "true"
 *   desc = "h:outputFormat"
 *   
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlOutputFormat extends UIOutput implements _StyleProperties, 
    _EscapeProperty, _TitleProperty
{
    public static final String COMPONENT_TYPE = "javax.faces.HtmlOutputFormat";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Format";

}
