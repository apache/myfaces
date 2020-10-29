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
package org.apache.myfaces.core.extensions.quarkus.runtime.producer;

import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;

import io.quarkus.arc.BeanCreator;

public class ManagedPropertyBeanCreator implements BeanCreator<Object>
{
    public static final String EXPRESSION = "expression";

    @Override
    public Object create(CreationalContext<Object> cc, Map<String, Object> map)
    {
        String expression = (String) map.get(EXPRESSION);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null)
        {
            throw new FacesException("@ManagedProperty(\"" + expression
                    + "\") can only be resolved in a JSF request!");
        }

        return facesContext.getApplication().evaluateExpressionGet(facesContext, expression, Object.class);
    }

}
