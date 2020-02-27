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
package org.apache.myfaces.view.facelets.tag.composite;

import javax.el.MethodExpression;
import jakarta.faces.component.UINamingContainer;

public class SimpleComponent extends UINamingContainer
{

    public SimpleComponent()
    {
        super();
    }

    public String getJavaProperty()
    {
        return (String) getStateHelper().eval(PropertyKeys.javaProperty);
    }

    public void setJavaProperty(String javaProperty)
    {
        getStateHelper().put(PropertyKeys.javaProperty, javaProperty);
    }
    
    public MethodExpression getCustomMethod()
    {
        return (MethodExpression) getStateHelper().eval(PropertyKeys.customMethod);
    }

    public void setCustomMethod(MethodExpression customMethodExpression)
    {
        getStateHelper().put(PropertyKeys.customMethod, customMethodExpression);
    }
    
    protected enum PropertyKeys
    {
        javaProperty,
        customMethod
    }
}
