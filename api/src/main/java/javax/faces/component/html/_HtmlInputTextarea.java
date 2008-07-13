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
 * Renders a HTML textarea element.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:inputTextarea"
 *   class = "javax.faces.component.html.HtmlInputTextarea"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlInputTextareaTag"
 *   template = "true"
 *   desc = "h:inputTextarea"
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlInputTextarea extends UIInput implements _AccesskeyProperty,
    _UniversalProperties, _FocusBlurProperties, _ChangeSelectProperties,
    _EventProperties, _StyleProperties, _TabindexProperty, 
    _DisabledReadonlyProperties
{

    public static final String COMPONENT_TYPE = "javax.faces.HtmlInputTextarea";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Textarea";

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
