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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.Parameter;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class FlowCallNodeImpl extends FlowCallNode implements Freezable
{
    private String id;
    private String calledFlowId;
    private ValueExpression calledFlowIdEL;
    private String calledFlowDocumentId;
    private ValueExpression calledFlowDocumentIdEL;
    
    private Map<String, Parameter> outboundParametersMap;
    private Map<String, Parameter> unmodifiableOutboundParametersMap;
    
    private boolean initialized;

    public FlowCallNodeImpl(String id)
    {
        this.id = id;
        outboundParametersMap = new HashMap<>();
        unmodifiableOutboundParametersMap = Collections.unmodifiableMap(outboundParametersMap);
    }
    
    @Override
    public Map<String, Parameter> getOutboundParameters()
    {
        return unmodifiableOutboundParametersMap;
    }
    
    public void putOutboundParameter(String key, Parameter value)
    {
        checkInitialized();
        outboundParametersMap.put(key, value);
    }

    @Override
    public String getCalledFlowDocumentId(FacesContext context)
    {
        if (calledFlowDocumentIdEL != null)
        {
            return calledFlowDocumentIdEL.getValue(context.getELContext());
        }
        return calledFlowDocumentId;
    }

    @Override
    public String getCalledFlowId(FacesContext context)
    {
        if (calledFlowIdEL != null)
        {
            return calledFlowIdEL.getValue(context.getELContext());
        }
        return calledFlowId;
    }

    @Override
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        checkInitialized();
        this.id = id;
    }

    public void setCalledFlowId(String calledFlowId)
    {
        checkInitialized();
        this.calledFlowId = calledFlowId;
        this.calledFlowIdEL = null;
    }

    public void setCalledFlowDocumentId(String calledFlowDocumentId)
    {
        checkInitialized();
        this.calledFlowDocumentId = calledFlowDocumentId;
        this.calledFlowDocumentIdEL = null;
    }
    
    @Override
    public void freeze()
    {
        initialized = true;
        
        for (Map.Entry<String, Parameter> entry : outboundParametersMap.entrySet())
        {
            if (entry.getValue() instanceof Freezable)
            {
                ((Freezable)entry.getValue()).freeze();
            }
        }
    }
    
    private void checkInitialized() throws IllegalStateException
    {
        if (initialized)
        {
            throw new IllegalStateException("Flow is inmutable once initialized");
        }
    }

    public void setCalledFlowId(ValueExpression calledFlowIdEL)
    {
        this.calledFlowIdEL = calledFlowIdEL;
        this.calledFlowId = null;
    }

    public void setCalledFlowDocumentId(ValueExpression calledFlowDocumentIdEL)
    {
        this.calledFlowDocumentIdEL = calledFlowDocumentIdEL;
        this.calledFlowDocumentId = null;
    }
}
