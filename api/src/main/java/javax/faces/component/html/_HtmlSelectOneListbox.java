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

import javax.faces.component.UISelectOne;

/**
 * Allow the user to choose one option from a set of options.
 * <p>
 * Rendered as a listbox with the MULTIPLE attribute set to false.
 * <p>
 * The available choices are defined via child f:selectItem or
 * f:selectItems elements. The size of the listbox defaults to the
 * number of available choices; if size is explicitly set to a
 * smaller value, then scrollbars will be rendered. If size is set
 * to 1 then a "drop-down menu" (aka "combo-box") is rendered, though
 * if this is the intent then selectOneMenu should be used instead.
 * <p>
 * The value attribute of this component is read to determine
 * which of the available options is initially selected; its value
 * should match the "value" property of one of the child SelectItem
 * objects.
 * <p>
 * On submit of the enclosing form, the value attribute's bound
 * property is updated to contain the "value" property from the
 * chosen SelectItem.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p> 
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:selectOneListbox"
 *   class = "javax.faces.component.html.HtmlSelectOneListbox"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlSelectOneListboxTag"
 *   desc = "h:selectOneListbox"
 *   
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlSelectOneListbox extends UISelectOne implements
    _AccesskeyProperty, _UniversalProperties, _Disabled_ReadonlyProperties,
    _Focus_BlurProperties, _Change_SelectProperties, _EventProperties,
    _StyleProperties, _TabindexProperty, _DisabledClass_EnabledClassProperties
{

    public static final String COMPONENT_TYPE = "javax.faces.HtmlSelectOneListbox";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Listbox";

    /**
     * see JSF Spec.
     * 
     * @JSFProperty
     *   defaultValue="0"
     */
    public abstract int getSize();
    
}