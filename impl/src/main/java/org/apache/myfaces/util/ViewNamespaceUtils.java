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
package org.apache.myfaces.util;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

public class ViewNamespaceUtils
{
    /**
     * Gets the view namespace of the current viewroot.
     * This is a porlet feature, where multiple faces view exists on one portlet view.
     *
     * @param context
     * @return 
     */
    public static String getViewNamespace(FacesContext context)
    {
        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot == null)
        {
            viewRoot = (UIViewRoot) context.getApplication().createComponent(UIViewRoot.COMPONENT_TYPE);
        }
        if (viewRoot instanceof NamingContainer)
        {
            return viewRoot.getContainerClientId(context) + UINamingContainer.getSeparatorChar(context);
        }
        return "";
    }
}
