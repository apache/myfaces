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
package javax.faces.component;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;

/**
 * Base class for components that provide a new "namespace" for the ids of their
 * child components.
 * <p>
 * See the javadocs for interface NamingContainer for further details.
 */
@JSFComponent(
        name="f:subview",
        bodyContent="JSP",
        tagClass="org.apache.myfaces.taglib.core.SubviewTag")
@JSFJspProperty(name="id",required=true)
public class UINamingContainer extends UIComponentBase implements NamingContainer, UniqueIdVendor
{
    public static final String COMPONENT_TYPE = "javax.faces.NamingContainer";
    public static final String COMPONENT_FAMILY = "javax.faces.NamingContainer";
    public static final String SEPARATOR_CHAR_PARAM_NAME = "javax.faces.SEPARATOR_CHAR";

    /**
     * Construct an instance of the UINamingContainer.
     */
    public UINamingContainer()
    {
        setRendererType(null);
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    /**
     * 
     * {@inheritDoc}
     * 
     * @since 2.0
     */
    @Override
    public String createUniqueId(FacesContext context, String seed)
    {
        // TODO: IMPLEMENT HERE
        return null;
    }
    
    /**
     * 
     * @param context
     * @return
     * 
     * @since 2.0
     */
    @SuppressWarnings("deprecation")
    public static char getSeparatorChar(FacesContext context)
    {
        ExternalContext eContext = context.getExternalContext();
        
        // The implementation must determine if there is a <context-param> with the value given by the 
        // value of the symbolic constant SEPARATOR_CHAR_PARAM_NAME
        String param = eContext.getInitParameter(SEPARATOR_CHAR_PARAM_NAME);
        if (param == null || param.length() == 0)
        {
            // Otherwise, the value of the symbolic constant NamingContainer.SEPARATOR_CHAR must be returned.
            return NamingContainer.SEPARATOR_CHAR;
        }
        else
        {
            // If there is a value for this param, the first character of the value must be returned from 
            // this method
            return param.charAt(0);
        }
    }
}
