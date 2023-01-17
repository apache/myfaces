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
package org.apache.myfaces.el.resolver;

import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ImportHandler;
import jakarta.el.PropertyNotFoundException;

/**
 * See Faces 2.2 section 5.6.2.8
 */
public class ImportHandlerResolver extends ScopedAttributeResolver
{
    public ImportHandlerResolver()
    {
    }

    /*
     * Handle the EL case for the name of an import class
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property)
        throws NullPointerException, PropertyNotFoundException, ELException
    {
        ImportHandler importHandler = context.getImportHandler();
        if (importHandler != null) 
        {
            Class<?> clazz = importHandler.resolveClass(property.toString());
            if (clazz != null) 
            {
                context.setPropertyResolved(true);
                return new ELClass(clazz);
            }
        }

        return null;
    }
}
