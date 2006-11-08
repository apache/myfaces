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
package org.apache.myfaces.lifecycle;

import javax.faces.FacesException;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class LifecycleFactoryImpl
        extends LifecycleFactory
{
    private final Map _lifecycles = new HashMap();

    public LifecycleFactoryImpl()
    {
        addLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE, new LifecycleImpl());
    }

    public void addLifecycle(String id, Lifecycle lifecycle)
    {
        synchronized (_lifecycles)
        {
            if (_lifecycles.get(id) != null)
            {
                throw new IllegalArgumentException("Lifecycle with id '" + id + "' already exists.");
            }
            _lifecycles.put(id, lifecycle);
        }
    }

    public Lifecycle getLifecycle(String id)
            throws FacesException
    {
        synchronized (_lifecycles)
        {
            Lifecycle lifecycle = (Lifecycle)_lifecycles.get(id);
            if (lifecycle == null)
            {
                throw new IllegalArgumentException("Unknown lifecycle '" + id + "'.");
            }
            return lifecycle;
        }
    }

    public Iterator getLifecycleIds()
    {
        return _lifecycles.keySet().iterator();
    }
}
