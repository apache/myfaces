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
package org.apache.myfaces.core.api.shared;

import jakarta.faces.context.FacesContext;
import java.util.Map;
import java.util.function.Supplier;

public class VarUtils
{
    public static <T> T executeInScope(FacesContext context, String var, Object value, Supplier<T> callback)
    {
        if (var == null || var.isBlank())
        {
            return callback.get();
        }

        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();

        // save the current value of the key listed in var from the request map
        Object oldValue = requestMap.remove(var);
        try
        {
            // write the current item into the request map under the key listed in var, if available
            requestMap.put(var, value);

            return callback.get();
        }
        finally
        {
            // remove the value with the key from var from the request map, if previously written
            requestMap.remove(var);
            if (oldValue != null)
            {
                // If there was a previous value stored with the key from var in the request map, restore it
                requestMap.put(var, oldValue);
            }
        }
    }
}
