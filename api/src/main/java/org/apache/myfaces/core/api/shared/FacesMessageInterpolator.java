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
package org.apache.myfaces.core.api.shared;

import java.util.Locale;
import jakarta.faces.context.FacesContext;
import javax.validation.MessageInterpolator;

/**
 * Note: Before 2.1.5/2.0.11 there was another strategy for this point to minimize
 * the instances used, but after checking this with a profiler, it is more expensive to
 * call FacesContext.getCurrentInstance() than create this object for bean validation.
 * 
 * Standard MessageInterpolator, as described in the JSR-314 spec.
 */
public class FacesMessageInterpolator implements MessageInterpolator
{
    private final FacesContext facesContext;
    private final MessageInterpolator interpolator;

    public FacesMessageInterpolator(final MessageInterpolator interpolator, final FacesContext facesContext)
    {
        this.interpolator = interpolator;
        this.facesContext = facesContext;
    }

    @Override
    public String interpolate(final String s, final MessageInterpolator.Context context)
    {
        Locale locale = facesContext.getViewRoot().getLocale();
        return interpolator.interpolate(s, context, locale);
    }

    @Override
    public String interpolate(final String s, final MessageInterpolator.Context context, final Locale locale)
    {
        return interpolator.interpolate(s, context, locale);
    }
}
