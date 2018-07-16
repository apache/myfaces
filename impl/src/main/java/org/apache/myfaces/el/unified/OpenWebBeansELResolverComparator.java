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
package org.apache.myfaces.el.unified;

import java.util.Comparator;

import javax.el.ELResolver;

/**
 * Comparator for ELResolvers that shifts the Resolver from OWB to the last place.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @since 1.2.10, 2.0.2
 */
public class OpenWebBeansELResolverComparator implements Comparator<ELResolver>
{
    
    public static final String OWB_RESOLVER_OLD = "org.apache.webbeans.el.WebBeansELResolver";
    public static final String OWB_RESOLVER     = "org.apache.webbeans.el22.WebBeansELResolver";

    @Override
    public int compare(ELResolver r1, ELResolver r2)
    {
        if (r1.getClass().getName().equals(OWB_RESOLVER_OLD) || r1.getClass().getName().equals(OWB_RESOLVER))
        {
            return 1;
        }
        else if (r2.getClass().getName().equals(OWB_RESOLVER_OLD) || r2.getClass().getName().equals(OWB_RESOLVER))
        {
            return -1;
        }
        return 0; // keep order
    }
    
}
