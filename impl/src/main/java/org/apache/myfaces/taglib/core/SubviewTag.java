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
package org.apache.myfaces.taglib.core;

import javax.faces.webapp.UIComponentTag;
import javax.faces.component.UINamingContainer;

/**
 * This tag associates a set of UIComponents with the nearest parent
 * UIComponent.  It acts as a naming container to make the IDs of its
 * component elements unique.
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFJspTag
 *   name="f:subview"
 *   bodyContent="JSP" 
 * @JSFJspAttribute
 *   name="id"
 *   className="java.lang.String"
 *   required="true"
 *   longDescription="The developer-assigned ID of this component."
 * @JSFJspAttribute
 *   name="binding"
 *   className="java.lang.String"
 *   longDescription="Identifies a backing bean property to bind to this component instance."
 * @JSFJspAttribute
 *   name="rendered"
 *   className="java.lang.String"
 *   longDescription="A boolean value that indicates whether this component should be rendered."
 *   
 *   
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class SubviewTag
    extends UIComponentTag
{
    public String getComponentType()
    {
        return UINamingContainer.COMPONENT_TYPE;
    }

    public String getRendererType()
    {
        return null;
    }
}
