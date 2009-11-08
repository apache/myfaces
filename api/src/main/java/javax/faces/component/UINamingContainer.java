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

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;

/**
 * Base class for components that provide a new "namespace" for the ids of their
 * child components.
 * <p>
 * See the javadocs for interface NamingContainer for further details.
 * </p>
 */
@JSFComponent(
        name="f:subview",
        bodyContent="JSP",
        tagClass="org.apache.myfaces.taglib.core.SubviewTag")
@JSFJspProperty(name="id",required=true)
public class UINamingContainer extends UIComponentBase implements NamingContainer
{
    public static final String COMPONENT_TYPE = "javax.faces.NamingContainer";
    public static final String COMPONENT_FAMILY = "javax.faces.NamingContainer";

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
}
