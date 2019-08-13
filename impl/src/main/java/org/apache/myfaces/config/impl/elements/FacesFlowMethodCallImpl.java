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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.myfaces.config.element.FacesFlowMethodParameter;

/**
 *
 * @author Leonardo Uribe
 */
public class FacesFlowMethodCallImpl extends org.apache.myfaces.config.element.FacesFlowMethodCall
{
    private String id;
    private String method;
    private String defaultOutcome;
    private List<FacesFlowMethodParameter> parameterList;

    @Override
    public String getMethod()
    {
        return method;
    }

    @Override
    public String getDefaultOutcome()
    {
        return defaultOutcome;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public void setDefaultOutcome(String defaultOutcome)
    {
        this.defaultOutcome = defaultOutcome;
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

    @Override
    public List<FacesFlowMethodParameter> getParameterList()
    {
        if (parameterList == null)
        {
            return Collections.emptyList();
        }
        return parameterList;
    }
    
    public void addParameter(FacesFlowMethodParameter parameter)
    {
        if (parameterList == null)
        {
            parameterList = new ArrayList<>();
        }
        parameterList.add(parameter);
    }
}
