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
package org.apache.myfaces.config;

/**
 * @author Dennis C. Byrne
 */

import java.util.List;
import java.util.Map;

public class MangedBeanExample
{

    private String managedProperty;
    private List<String> managedList;
    private List<String> writeOnlyList;
    private Map<String, String> managedMap;
    private Map<String, String> writeOnlyMap;

    public List<String> getManagedList()
    {
        return managedList;
    }

    public void setManagedList(List<String> managedList)
    {
        this.managedList = managedList;
    }

    public String getManagedProperty()
    {
        return managedProperty;
    }

    public void setManagedProperty(String managedProperty)
    {
        this.managedProperty = managedProperty;
    }

    public Map<String, String> getManagedMap()
    {
        return managedMap;
    }

    public void setManagedMap(Map<String, String> managedMap)
    {
        this.managedMap = managedMap;
    }

    public void setWriteOnlyList(List<String> writeOnlyList)
    {
        this.writeOnlyList = writeOnlyList;
    }

    public void setWriteOnlyMap(Map<String, String> writeOnlyMap)
    {
        this.writeOnlyMap = writeOnlyMap;
    }

    public Map<String, String> getHiddenWriteOnlyMap()
    {
        return writeOnlyMap;
    }

    public List<String> getHiddenWriteOnlyList()
    {
        return writeOnlyList;
    }

}
