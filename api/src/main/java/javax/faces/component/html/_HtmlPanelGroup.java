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

import javax.faces.component.UIPanel;

/**
 * This element is used to group other components where the
 * specification requires one child element. If any of the HTML or
 * CSS attributes are set, its content is rendered within a span element.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:panelGroup"
 *   class = "javax.faces.component.html.HtmlPanelGroup"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlPanelGroupTag"
 *   tagSuperclass = "javax.faces.webapp.UIComponentBodyTag"
 *   template = "true"
 *   desc = "h:panelGroup
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlPanelGroup extends UIPanel implements _StyleProperties
{

    public static final String COMPONENT_TYPE = "javax.faces.HtmlPanelGroup";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Group";

}
