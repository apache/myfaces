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
package org.apache.myfaces.util;

import javax.faces.webapp.UIComponentTag;

/**
 * Utilities for determining the current container and for the unified
 * expression language.
 * 
 */
public class ContainerUtils
{
    /**
     * Determines whether we're running in a Servlet 2.5/JSP 2.1 environment.
     * 
     * @return <code>true</code> if we're running in a JSP 2.1 environment,
     *         <code>false</code> otherwise
     */
    public static boolean isJsp21()
    {
        try 
        {
            // simply check if the class JspApplicationContext is available
            Class.forName("javax.servlet.jsp.JspApplicationContext");
            return true;
        } 
        catch (ClassNotFoundException ex) 
        {
            ; // expected exception in a JSP 2.0 (or less) environment
        }
        
        return false;
    }
    
    /**
     * Return true if the specified string contains an EL expression.
     * 
     * <p>
     * <strong>NOTICE</strong> This method is just a copy of
     * {@link UIComponentTag#isValueReference(String)}, but it's required
     * because the class UIComponentTag depends on a JSP 2.1 container 
     * (for example, it indirectly implements the interface JspIdConsumer)
     * and therefore internal classes shouldn't access this class. That's
     * also the reason why this method is inside the class ContainerUtils,
     * because it allows MyFaces to be independent of a JSP 2.1 container.
     * </p>
     */
    public static boolean isValueReference(String value) 
    {
        if (value == null) {
            throw new NullPointerException("value");
        }

        int start = value.indexOf("#{");
        if (start < 0) {
            return false;
        }

        int end = value.lastIndexOf('}');
        return (end >=0 && start < end);
    }
}
