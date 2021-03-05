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
package org.apache.myfaces.cdi.view;

import java.io.Serializable;
import java.util.Random;
import jakarta.enterprise.context.SessionScoped;
import org.apache.myfaces.cdi.util.AbstractContextualStorageHolder;

/**
 *
 * @author Leonardo Uribe
 */
@SessionScoped
public class ViewScopeContextualStorageHolder
        extends AbstractContextualStorageHolder<ViewScopeContextualStorage>
        implements Serializable
{    
    private static final Random RANDOM_GENERATOR = new Random();

    public ViewScopeContextualStorageHolder()
    {
    }

    public String generateUniqueViewScopeId()
    {
        // To ensure uniqueness we just use a random generator and we check
        // if the key is already used.
        String key;
        do 
        {
            key = Integer.toString(RANDOM_GENERATOR.nextInt());
        } while (storageMap.containsKey(key));
        return key;
    }

    @Override
    protected ViewScopeContextualStorage newContextualStorage(String slotId)
    {
        return new ViewScopeContextualStorage(beanManager);
    }

}
