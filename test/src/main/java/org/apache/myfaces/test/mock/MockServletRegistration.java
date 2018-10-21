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

package org.apache.myfaces.test.mock;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRegistration;

/**
 *
 */
public class MockServletRegistration implements ServletRegistration
{
    public static String[] facesServletMappings = {"/faces/*", "*.jsf", "*.xhtml"};
    
    private Set<String> mappings = new LinkedHashSet<String>();
    private String name;
    private String servletClassName;

    public MockServletRegistration(String name, String servletClassName, String ... mappings)
    {
        this.name = name;
        this.servletClassName = servletClassName;
    }

    public MockServletRegistration()
    {
    }
        
    public Set<String> addMapping(String... mappingArray)
    {
        for (String s : mappingArray)
        {
            mappings.add(s);
        }
        return mappings;
    }

    public Collection<String> getMappings()
    {
        return mappings;
    }

    public String getRunAsRole()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getClassName()
    {
        return servletClassName;
    }

    public String getInitParameter(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<String, String> getInitParameters()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName()
    {
        return name;
    }

    public boolean setInitParameter(String string, String string1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<String> setInitParameters(Map<String, String> map)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setClassName(String className)
    {
        this.servletClassName = className;
    }
    
}
