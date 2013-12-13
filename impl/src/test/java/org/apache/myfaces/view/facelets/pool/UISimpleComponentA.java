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
package org.apache.myfaces.view.facelets.pool;

import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

/**
 *
 * @author lu4242
 */
@ResourceDependency(name="test1.js")
@FacesComponent(createTag=true,
    namespace="http://myfaces.apache.org/testComponent", 
    tagName="simpleComponentA", value="org.apache.myfaces.view.facelets.pool.UISimpleComponentA")
public class UISimpleComponentA extends UIComponentBase
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.view.facelets.pool.UISimpleComponentA";
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.view.facelets.pool.UISimpleComponentA";
    //public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.view.facelets.pool.UISimpleComponentA";

    public UISimpleComponentA()
    {
        setRendererType(null);
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
