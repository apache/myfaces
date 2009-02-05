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
package com.sun.facelets.util;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;

public final class FacesAPI
{

    private static final int version = specifyVersion();
    private static final Class[] UIC_SIG = new Class[] { String.class };

    private FacesAPI()
    {
        super();
    }

    private final static int specifyVersion()
    {
        try
        {
            Application.class.getMethod("getExpressionFactory", null);
        }
        catch (NoSuchMethodException e)
        {
            return 11;
        }
        return 12;
    }

    public final static int getVersion()
    {
        return version;
    }

    public final static int getComponentVersion(UIComponent c)
    {
        return version;
    }

    public final static int getComponentVersion(Class c)
    {
        return version;
    }
}
