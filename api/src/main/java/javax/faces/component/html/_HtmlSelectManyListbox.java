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
 * available options. This is presented as a listbox which allows
 * multiple rows in the list to be selected simultaneously.
 * <p>
 * The set of available options is defined by adding child
 * f:selectItem or f:selectItems components to this component.
 * <p>
 * The list is rendered as an HTML select element. The "multiple"
 * attribute is set on the element and the size attribute is set to
 * the provided value, defaulting to the number of items in the list
 * if no value is provided. If the size is set to 1, then a
 * "drop-down" list (aka "combo-box") is presented, though if this is
 * the intention then a selectManyMenu should be used instead.
 * <p>
 * The value attribute must be a value-binding expression to a
 * property of type List, Object array or primitive array. That
 * "collection" is expected to contain objects of the same type as
 * SelectItem.getValue() returns for the child SelectItem objects.
 * On rendering, any child whose value is in the list will be
 * selected initially. During the update phase, the property is set
 * to contain a "collection" of values for those child SelectItem
 * objects that are currently selected.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "h:selectManyListbox"
 *   class = "javax.faces.component.html.HtmlSelectManyListbox"
 *   tagClass = "org.apache.myfaces.taglib.html.HtmlSelectManyListboxTag"
 *   template = "true"
 *   desc = "h:selectManyListbox"
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
abstract class _HtmlSelectManyListbox extends UISelectMany implements 
    _AccesskeyProperty, _UniversalProperties, _DisabledReadonlyProperties,
    _FocusBlurProperties, _ChangeSelectProperties, _EventProperties,
    _StyleProperties, _TabindexProperty, _DisabledClassEnabledClassProperties
{

    public static final String COMPONENT_TYPE = "javax.faces.HtmlSelectManyListbox";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Listbox";

    /**
     * see JSF Spec.
     * 
     * @JSFProperty
     *   defaultValue="Integer.MIN_VALUE"
     */
    public abstract int getSize();
        
}
