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

import java.util.Map;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This gets a single FacesContext-local shared stringbuilder instance, each
 * time you call _getSharedStringBuilder it sets the length of the stringBuilder
 * instance to 0.
 * </p><p>
 * This allows you to use the same StringBuilder instance over and over. You
 * must call toString on the instance before calling _getSharedStringBuilder
 * again.
 * </p>
 * Example that works
 * <pre><code>
 * StringBuilder sb1 = _getSharedStringBuilder();
 * sb1.append(a).append(b);
 * String c = sb1.toString();
 *
 * StringBuilder sb2 = _getSharedStringBuilder();
 * sb2.append(b).append(a);
 * String d = sb2.toString();
 * </code></pre>
 * <br><br>
 * Example that doesn't work, you must call toString on sb1 before calling
 * _getSharedStringBuilder again.
 * <pre><code>
 * StringBuilder sb1 = _getSharedStringBuilder();
 * StringBuilder sb2 = _getSharedStringBuilder();
 *
 * sb1.append(a).append(b);
 * String c = sb1.toString();
 *
 * sb2.append(b).append(a);
 * String d = sb2.toString();
 * </code></pre>
 *
 */
public class SharedStringBuilder
{
    public static StringBuilder get(String stringBuilderKey)
    {
        return get(FacesContext.getCurrentInstance(), stringBuilderKey);
    }

    public static StringBuilder get(FacesContext facesContext, String stringBuilderKey)
    {
        Map<Object, Object> attributes = facesContext.getAttributes();

        StringBuilder sb = (StringBuilder) attributes.get(stringBuilderKey);

        if (sb == null)
        {
            sb = new StringBuilder();
            attributes.put(stringBuilderKey, sb);
        }
        else
        {
            // clear out the stringBuilder by setting the length to 0
            sb.setLength(0);
        }

        return sb;
    }
    
    public static StringBuilder get(FacesContext facesContext, String stringBuilderKey, int initialSize)
    {
        Map<Object, Object> attributes = facesContext.getAttributes();

        StringBuilder sb = (StringBuilder) attributes.get(stringBuilderKey);

        if (sb == null)
        {
            sb = new StringBuilder(initialSize);
            attributes.put(stringBuilderKey, sb);
        }
        else
        {
            // clear out the stringBuilder by setting the length to 0
            sb.setLength(0);
        }

        return sb;
    }
}
