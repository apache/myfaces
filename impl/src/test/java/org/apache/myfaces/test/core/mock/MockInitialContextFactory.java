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
package org.apache.myfaces.test.core.mock;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Mock per-thread implementation of InitialContextFactory
 *
 */
public class MockInitialContextFactory implements InitialContextFactory
{

    private static ThreadLocal<Context> currentInstance = new ThreadLocal<Context>();

    public Context getInitialContext(Hashtable<?, ?> environment)
        throws NamingException
    {
        return currentInstance.get();
    }
    
    public static void setCurrentContext(Context context)
    {
        currentInstance.set(context);
    }

    public static void clearCurrentContext()
    {
        currentInstance.remove();
    }    

    public static void bind(String name, Object obj)
    {
        try
        {
            currentInstance.get().bind(name, obj);
        }
        catch (NamingException e)
        { // can't happen.
            throw new RuntimeException(e);
        }
    }
    
}
