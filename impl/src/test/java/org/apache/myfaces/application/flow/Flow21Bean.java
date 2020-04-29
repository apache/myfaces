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
package org.apache.myfaces.application.flow;

import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 */
@Named
@FlowScoped(value="flow2", definingDocumentId="flow_def_1")
public class Flow21Bean implements Serializable
{
    private String postConstructCalled;
    
    private String name;
    
    @Inject
    private Flow1Bean flow1Bean;
    
    @PostConstruct
    public void init()
    {
        postConstructCalled = "true";
    }

    public String getPostConstructCalled()
    {
        return postConstructCalled;
    }

    public void setPostConstructCalled(String postConstructCalled)
    {
        this.postConstructCalled = postConstructCalled;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Flow1Bean getFlow1Bean()
    {
        return flow1Bean;
    }

    public void setFlow1Bean(Flow1Bean flow1Bean)
    {
        this.flow1Bean = flow1Bean;
    }
}
