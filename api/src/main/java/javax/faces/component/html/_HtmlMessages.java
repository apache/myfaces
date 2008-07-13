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

import javax.faces.component.UIMessages;

/**
 * Renders all or some FacesMessages depending on the "for" and
 * "globalOnly" attributes.
 * <p>
 * The behaviour of this component is:
 * <ul>
 * <li>If globalOnly = true, only global messages, that have no associated
 * clientId, will be displayed.
 * <li>else if there is a "for" attribute, only messages that are
 * assigned to the component referenced by the "for" attribute
 * are displayed.
 * <li>else all messages are displayed.
 * </ul>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:messages"
 *   class = "javax.faces.component.html.HtmlMessages"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlMessagesTag"
 *   template = "true"
 *   desc = "h:messages"
 *   
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlMessages extends UIMessages implements _StyleProperties, 
    _MessageProperties
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.HtmlMessages";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Messages";

    /**
     * The layout: "table" or "list". Default: list
     * 
     * @JSFProperty
     *   defaultValue = "list"
     */
    public abstract String getLayout();

}
