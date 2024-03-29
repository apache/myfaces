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
package org.apache.myfaces.flow.builder;

import jakarta.faces.flow.builder.ViewBuilder;
import org.apache.myfaces.flow.FlowImpl;
import org.apache.myfaces.flow.ViewNodeImpl;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class ViewBuilderImpl extends ViewBuilder
{
    private FlowImpl facesFlow;
    private ViewNodeImpl viewNode;
    
    public ViewBuilderImpl(FlowImpl facesFlow, String viewNodeId, String vdlDocumentId)
    {
        this.facesFlow = facesFlow;
        this.viewNode = new ViewNodeImpl(viewNodeId, vdlDocumentId);
        facesFlow.addView(viewNode);
    }

    @Override
    public ViewBuilder markAsStartNode()
    {
        facesFlow.setStartNodeId(viewNode.getId());
        return this;
    }
    
}
