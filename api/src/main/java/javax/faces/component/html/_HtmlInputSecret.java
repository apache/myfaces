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

/**
 * Renders as an HTML input tag with its type set to "password".
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * 
 * @JSFComponent
 *   name = "h:inputSecret"
 *   class = "javax.faces.component.html.HtmlInputSecret"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlInputSecretTag"
 *   template = "true"
 *   desc = "h:inputSecret"
 *   
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlInputSecret extends UIInput implements _AccesskeyProperty,
    _AltProperty, _UniversalProperties, _Focus_BlurProperties, _EventProperties,
    _StyleProperties, _TabindexProperty, _Change_SelectProperties, 
    _Disabled_ReadonlyProperties
{
    public static final String COMPONENT_TYPE = "javax.faces.HtmlInputSecret";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Secret";

    /**
     * HTML: The maximum number of characters allowed to be entered.
     * 
     * @JSFProperty
     *   defaultValue = "Integer.MIN_VALUE"
     */
    public abstract int getMaxlength();
    
    /**
     * If true, the value will be re-sent (in plaintext) when the form
     * is rerendered (see JSF.7.4.4). Default is false.
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
    
}
