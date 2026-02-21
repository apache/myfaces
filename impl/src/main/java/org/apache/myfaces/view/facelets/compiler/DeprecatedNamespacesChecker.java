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
package org.apache.myfaces.view.facelets.compiler;

import org.apache.myfaces.view.facelets.tag.composite.CompositeLibrary;
import org.apache.myfaces.view.facelets.tag.faces.JsfLibrary;
import org.apache.myfaces.view.facelets.tag.faces.PassThroughLibrary;
import org.apache.myfaces.view.facelets.tag.faces.core.CoreLibrary;
import org.apache.myfaces.view.facelets.tag.faces.html.HtmlLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.core.JstlCoreLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.fn.JstlFnLibrary;
import org.apache.myfaces.view.facelets.tag.ui.UILibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class DeprecatedNamespacesChecker
{
    private static final Map<String, String> DEPRECATED_NAMESPACE_REPLACEMENTS;
    private static final Set<String> FOUND_NAMESPACES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static
    {
        Map<String, String> map = new HashMap<>();
        mapDeprecatedNamespaces(map, CoreLibrary.NAMESPACES, CoreLibrary.NAMESPACE);
        mapDeprecatedNamespaces(map, HtmlLibrary.NAMESPACES, HtmlLibrary.NAMESPACE);
        mapDeprecatedNamespaces(map, UILibrary.NAMESPACES, UILibrary.NAMESPACE);
        mapDeprecatedNamespaces(map, CompositeLibrary.NAMESPACES, CompositeLibrary.NAMESPACE);
        mapDeprecatedNamespaces(map, JsfLibrary.NAMESPACES_SET, JsfLibrary.NAMESPACE);
        mapDeprecatedNamespaces(map, PassThroughLibrary.NAMESPACES_SET, PassThroughLibrary.NAMESPACE);
        mapDeprecatedNamespaces(map, JstlCoreLibrary.NAMESPACES, JstlCoreLibrary.NAMESPACE);
        mapDeprecatedNamespaces(map, JstlFnLibrary.NAMESPACES, JstlFnLibrary.NAMESPACE);
        DEPRECATED_NAMESPACE_REPLACEMENTS = Collections.unmodifiableMap(map);
    }

    private static void mapDeprecatedNamespaces(Map<String, String> map,
                                                Set<String> namespaces,
                                                String defaultNamespace)
    {
        for (String namespace : namespaces)
        {
            if (!namespace.equals(defaultNamespace))
            {
                map.put(namespace, defaultNamespace);
            }
        }
    }

    /**
     * If given namespace is null, return null.
     * If given namespace was already previously checked, return null.
     * If given namespace is not deprecated, return null.
     * If given namespace is deprecated, return replacement namespace.
     */
    static String shouldWarnAboutForDeprecatedNamespace(String namespace)
    {
        if (namespace == null || !FOUND_NAMESPACES.add(namespace))
        {
            return null;
        }
        return DEPRECATED_NAMESPACE_REPLACEMENTS.get(namespace);
    }
}