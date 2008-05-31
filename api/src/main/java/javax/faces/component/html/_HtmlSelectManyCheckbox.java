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

import javax.faces.component.UISelectMany;

/**
 * Allow the user to select zero or more items from a set of
 * available options.
 * <p>
 * This is presented as a table with one cell per available option; each
 * cell contains a checkbox and the option's label. The "layout" attribute
 * determines whether the checkboxes are laid out horizontally or vertically.
 * <p>
 * The set of available options is defined by adding child
 * f:selectItem or f:selectItems components to this component.
 * <p>
 * The value attribute must be a value-binding expression to a
 * property of type List, Object array or primitive array. That
 * "collection" is expected to contain objects of the same type as
 * SelectItem.getValue() returns for the child SelectItem objects.
 * On rendering, any child whose value is in the list will be
 * selected initially. During the update phase, the property setter
 * is called to replace the original collection with a completely
 * new collection object of the appropriate type. The new collection
 * object contains the value of each child SelectItem object that
 * is currently selected.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:selectManyCheckbox"
 *   class = "javax.faces.component.html.HtmlSelectManyCheckbox"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlSelectManyCheckboxTag"
 *   desc = "h:selectManyCheckbox"
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlSelectManyCheckbox extends UISelectMany implements 
    _AccesskeyProperty, _UniversalProperties, _Focus_BlurProperties,
    _Change_SelectProperties, _EventProperties, _StyleProperties,
    _TabindexProperty, _Disabled_ReadonlyProperties, 
    _DisabledClass_EnabledClassProperties
{

    public static final String COMPONENT_TYPE = "javax.faces.HtmlSelectManyCheckbox";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Checkbox";

    /**
     * HTML: Specifies the width of the border of this element, in pixels.  Deprecated in HTML 4.01.
     * 
     * @JSFProperty
     *   defaultValue="Integer.MIN_VALUE"
     */
    public abstract int getBorder();
    
    /**
     * Controls the layout direction of the child elements.  Values include:  
     * lineDirection (vertical) and pageDirection (horzontal).
     * 
     * @JSFProperty
     */
    public abstract String getLayout();

}
