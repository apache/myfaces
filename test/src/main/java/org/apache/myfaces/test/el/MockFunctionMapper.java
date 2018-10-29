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

package org.apache.myfaces.test.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.el.FunctionMapper;

/**
 * <p>Mock implementation of <code>FunctionMapper</code>.</p>
 *
 * @since 1.0.0
 */

public class MockFunctionMapper extends FunctionMapper
{

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of MockFunctionMapper */
    public MockFunctionMapper()
    {
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>Map of <code>Method</code> descriptors for static methods, keyed by
     * a string composed of the prefix (or "" if none), a ":", and the local name.</p>
     */
    private Map functions = new HashMap();

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Store a mapping of the specified prefix and localName to the
     * specified method, which must be static.</p>
     */
    public void mapFunction(String prefix, String localName, Method method)
    {

        functions.put(prefix + ':' + localName, method);

    }

    // -------------------------------------------------- FunctionMapper Methods

    /** {@inheritDoc} */
    public Method resolveFunction(String prefix, String localName)
    {

        return (Method) functions.get(prefix + ':' + localName);

    }

}
