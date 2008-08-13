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

/**
 * Base class for components that provide a new "namespace" for the ids of their child components.
 * <p>
 * See the documentation on interface NamingContainer for more details.
 * 
 * @JSFComponent type = "javax.faces.NamingContainer" family = "javax.faces.NamingContainer" desc =
 *               "UINamingContainer"
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UINamingContainer extends UIComponentBase implements NamingContainer
{
    public static final String COMPONENT_TYPE = "javax.faces.NamingContainer";
    public static final String COMPONENT_FAMILY = "javax.faces.NamingContainer";

    public UINamingContainer()
    {
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
