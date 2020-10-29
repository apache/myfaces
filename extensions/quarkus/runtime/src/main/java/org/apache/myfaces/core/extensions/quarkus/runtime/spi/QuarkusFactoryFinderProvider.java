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
package org.apache.myfaces.core.extensions.quarkus.runtime.spi;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.FacesException;

import org.apache.myfaces.spi.FactoryFinderProvider;
import org.apache.myfaces.util.lang.ClassUtils;

/**
 * Custom FactoryFinder, as the original FactoryFinder cares to much about ClassLoaders,
 * which doesn't work with Quarkus native.
 * As Quarkus == 1 webapp, we can simple handle everything as static.
 */
public class QuarkusFactoryFinderProvider implements FactoryFinderProvider
{
    private final HashMap<String, List<String>> classes = new HashMap<>();
    private final HashMap<String, Object> instances = new HashMap<>();

    @Override
    public Object getFactory(String factoryName) throws FacesException
    {
        Object instance = instances.get(factoryName);

        if (instance == null)
        {
            try
            {
                List<String> impls = classes.computeIfAbsent(factoryName, k -> new ArrayList<>(3));

                Iterator<String> implsIterator = impls.iterator();
                while (implsIterator.hasNext())
                {
                    Class implClass = ClassUtils.classForName(implsIterator.next());

                    for (Constructor<?> constructor : implClass.getConstructors())
                    {
                        if (constructor.getParameterTypes().length == 1)
                        {
                            instance = constructor.newInstance(new Object[] { instance });
                        }
                    }

                    if (instance == null)
                    {
                        for (Constructor<?> constructor : implClass.getConstructors())
                        {
                            if (constructor.getParameterTypes().length == 0)
                            {
                                instance = constructor.newInstance();
                            }
                        }
                    }
                }

                instances.put(factoryName, instance);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return instance;
    }

    @Override
    public void releaseFactories() throws FacesException
    {
        instances.clear();
    }

    @Override
    public void setFactory(String factoryName, String implName)
    {
        List<String> impls = classes.computeIfAbsent(factoryName, k -> new ArrayList<>(3));
        impls.add(implName);

        instances.remove(factoryName);
    }
}
