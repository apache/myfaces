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

import java.util.Collections;
import java.util.Map;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.FlowHandler;

/**
 *
 * @author Leonardo Uribe
 */
public class MockFlowHandler extends FlowHandler
{

    @Override
    public Map<Object, Object> getCurrentFlowScope()
    {
        return Collections.emptyMap();
    }

    @Override
    public Flow getFlow(FacesContext context, String definingDocumentId, String id)
    {
        return null;
    }

    @Override
    public void addFlow(FacesContext context, Flow toAdd)
    {
    }

    @Override
    public Flow getCurrentFlow(FacesContext context)
    {
        return null;
    }

    @Override
    public boolean isActive(FacesContext context, String definingDocument, String id)
    {
        return false;
    }

    @Override
    public void transition(FacesContext context, Flow sourceFlow, Flow targetFlow, 
        FlowCallNode outboundCallNode, String toViewId)
    {
    }

    @Override
    public void clientWindowTransition(FacesContext context)
    {
    }

    @Override
    public String getLastDisplayedViewId(FacesContext context)
    {
        return null;
    }

    @Override
    public void pushReturnMode(FacesContext context)
    {
    }

    @Override
    public void popReturnMode(FacesContext context)
    {
    }
}
