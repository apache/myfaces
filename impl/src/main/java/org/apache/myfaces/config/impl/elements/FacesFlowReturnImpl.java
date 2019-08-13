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
package org.apache.myfaces.config.impl.elements;

import org.apache.myfaces.config.element.NavigationCase;

/**
 *
 * @author Leonardo Uribe
 */
public class FacesFlowReturnImpl extends org.apache.myfaces.config.element.FacesFlowReturn
{
    private String id;
    private NavigationCase navigationCase;

    @Override
    public NavigationCase getNavigationCase()
    {
        return navigationCase;
    }

    public void setNavigationCase(NavigationCase navigationCase)
    {
        this.navigationCase = navigationCase;
    }
    
    @Override
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

}
