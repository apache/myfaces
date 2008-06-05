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
 * Renders a HTML label element.
 * <p>
 * In addition to the JSF specification, MyFaces allows it to directly
 * give an output text via the "value" attribute.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:outputLabel"
 *   class = "javax.faces.component.html.HtmlOutputLabel"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlOutputLabelTag"
 *   template = "true"
 *   desc = "h:outputLabel"
 * 
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlOutputLabel extends UIOutput implements _Focus_BlurProperties,
    _EventProperties, _StyleProperties, _UniversalProperties, _AccesskeyProperty,
    _TabindexProperty
{
    public static final String COMPONENT_TYPE = "javax.faces.HtmlOutputLabel";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Label";

    /**
     * The client ID of the target input element of this label.
     * 
     * @JSFProperty
     */
    public abstract String getFor();
    
}
