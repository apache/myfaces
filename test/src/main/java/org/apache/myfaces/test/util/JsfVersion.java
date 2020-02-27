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
package org.apache.myfaces.test.util;

import jakarta.faces.application.Application;

public class JsfVersion
{

    private static boolean supports12;
    private static boolean supports20;

    static
    {
        try
        {
            Application.class.getMethod("getExpressionFactory");
            supports12 = true;

            try
            {
                Application.class.getMethod("getExceptionHandler");
                supports20 = true;

            }
            catch (NoSuchMethodException e)
            {
                // ignore
            }

        }
        catch (NoSuchMethodException e)
        {
            // ignore
        }
    }

    private JsfVersion()
    {
        // avoid instantiation
    }

    /**
     * Does the JSF is version 1.2 or higher
     *
     * @return Supports 1.2 or higher
     */
    public static boolean supports12()
    {
        return supports12;
    }

    /**
     * Does the JSF is version 2.0 or higher
     *
     * @return Supports 2.0 or higher
     */
    public static boolean supports20()
    {
        return supports20;
    }
}
