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
package org.apache.myfaces.flow;

import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowCallNode;
import javax.faces.flow.FlowHandler;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class FlowHandlerImpl extends FlowHandler
{

    @Override
    public Map<Object, Object> getCurrentFlowScope()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Flow getFlow(FacesContext context, String definingDocumentId, String id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addFlow(FacesContext context, Flow toAdd)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Flow getCurrentFlow(FacesContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void transition(FacesContext context,
        Flow sourceFlow, Flow targetFlow,
        FlowCallNode outboundCallNode, String toViewId)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isActive(FacesContext context, String definingDocument, String id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clientWindowTransition(FacesContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLastDisplayedViewId(FacesContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pushReturnMode(FacesContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void popReturnMode(FacesContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
