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
package org.apache.myfaces.mc.test.core.mock;

import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 */
public class MockInitialContext extends InitialContext
{

    private Map<String, Object> bindings = new HashMap<String, Object>();

    public MockInitialContext() throws NamingException
    {
    }

    @Override
    public void bind(String name, Object obj)
        throws NamingException
    {
        bindings.put(name, obj);
    }

    @Override
    public Object lookup(String name) throws NamingException
    {
        return bindings.get(name);
    }
}
