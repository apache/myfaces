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

import org.apache.myfaces.spi.InjectionProvider;
import org.apache.myfaces.spi.InjectionProviderException;

/**
 * Needs to be implemented to support injection in JSF artifacts:
 * https://github.com/quarkusio/quarkus/blob/main/extensions/arc/deployment/
 * src/main/java/io/quarkus/arc/deployment/ArcTestResourceProvider.java#L21-L54
 * https://github.com/quarkusio/quarkus/issues/2378
 */
public class QuarkusInjectionProvider extends InjectionProvider
{
    @Override
    public Object inject(Object instance) throws InjectionProviderException
    {
        return instance;
    }

    @Override
    public void postConstruct(Object instance, Object creationMetaData) throws InjectionProviderException
    {

    }

    @Override
    public void preDestroy(Object instance, Object creationMetaData) throws InjectionProviderException
    {

    }

}
