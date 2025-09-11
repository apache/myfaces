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

import java.beans.FeatureDescriptor;
import jakarta.el.ELResolver;

/**
 * <p>Convenience base class for EL resolvers.</p>
 * 
 * @since 1.0.0
 */
abstract class AbstractELResolver extends ELResolver
{

    /*
     * These two were removed in Expression Langauge 6.0, but we want to keep backwards
     * compatability with 5.0.
     */
    public static final String TYPE = "type";

    public static final String RESOLVABLE_AT_DESIGN_TIME = "resolvableAtDesignTime";

    // ------------------------------------------------------- Protected Methods

    /**
     * <p>Create and return a <code>FeatureDescriptor</code> configured with
     * the specified arguments.</p>
     *
     * @param name Feature name
     * @param displayName Display name
     * @param description Short description
     * @param expert Flag indicating this feature is for experts
     * @param hidden Flag indicating this feature should be hidden
     * @param preferred Flag indicating this feature is the preferred one
     *  among features of the same type
     * @param type Runtime type of this feature
     * @param designTime Flag indicating feature is resolvable at design time
     */
    protected FeatureDescriptor descriptor(String name, String displayName,
            String description, boolean expert, boolean hidden,
            boolean preferred, Object type, boolean designTime)
    {

        FeatureDescriptor descriptor = new FeatureDescriptor();

        descriptor.setName(name);
        descriptor.setDisplayName(displayName);
        descriptor.setShortDescription(description);
        descriptor.setExpert(expert);
        descriptor.setHidden(hidden);
        descriptor.setPreferred(preferred);
        descriptor.setValue(AbstractELResolver.TYPE, type);
        if (designTime)
        {
            descriptor.setValue(AbstractELResolver.RESOLVABLE_AT_DESIGN_TIME,
                    Boolean.TRUE);
        }
        else
        {
            descriptor.setValue(AbstractELResolver.RESOLVABLE_AT_DESIGN_TIME,
                    Boolean.FALSE);
        }

        return descriptor;

    }

}
