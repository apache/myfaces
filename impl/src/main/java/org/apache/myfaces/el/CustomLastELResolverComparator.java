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
package org.apache.myfaces.el;

import java.util.Comparator;

import jakarta.el.ELResolver;

/**
 * Comparator for ELResolvers that shifts the Resolvers from
 * the faces-config to the back.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @since 1.2.10, 2.0.2
 */
public class CustomLastELResolverComparator implements Comparator<ELResolver>
{

    private CustomFirstELResolverComparator _inverseResolver
            = new CustomFirstELResolverComparator();
    
    @Override
    public int compare(ELResolver r1, ELResolver r2)
    {
        return _inverseResolver.compare(r2, r1);
    }

}
