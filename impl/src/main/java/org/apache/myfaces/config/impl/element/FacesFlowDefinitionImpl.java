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
package org.apache.myfaces.config.impl.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.myfaces.config.element.FacesFlowCall;
import org.apache.myfaces.config.element.FacesFlowMethodCall;
import org.apache.myfaces.config.element.FacesFlowParameter;
import org.apache.myfaces.config.element.FacesFlowReturn;
import org.apache.myfaces.config.element.FacesFlowSwitch;
import org.apache.myfaces.config.element.FacesFlowView;
import org.apache.myfaces.config.element.NavigationRule;

/**
 *
 * @author Leonardo Uribe
 */
public class FacesFlowDefinitionImpl extends org.apache.myfaces.config.element.FacesFlowDefinition
{
    private String definingDocumentId;
    private String id;
    private String startNode;
    private String initializer;
    private String finalizer;
    
    private List<FacesFlowView> viewList;
    private List<FacesFlowSwitch> switchList;
    private List<FacesFlowReturn> returnList;
    private List<NavigationRule> navigationRuleList;
    private List<FacesFlowCall> flowCallList;
    private List<FacesFlowMethodCall> methodCallList;
    private List<FacesFlowParameter> inboundParameterList;

    @Override
    public String getStartNode()
    {
        return startNode;
    }

    @Override
    public List<FacesFlowView> getViewList()
    {
        if (viewList == null)
        {
            return Collections.emptyList();
        }
        return viewList;
    }
    
    public void addView(FacesFlowView view)
    {
        if (viewList == null)
        {
            viewList = new ArrayList<>();
        }
        viewList.add(view);
    }

    @Override
    public List<FacesFlowSwitch> getSwitchList()
    {
        if (switchList == null)
        {
            return Collections.emptyList();
        }
        return switchList;
    }
    
    public void addSwitch(FacesFlowSwitch switchItem)
    {
        if (switchList == null)
        {
            switchList = new ArrayList<>();
        }
        switchList.add(switchItem);
    }

    @Override
    public List<FacesFlowReturn> getReturnList()
    {
        if (returnList == null)
        {
            return Collections.emptyList();
        }
        return returnList;
    }

    public void addReturn(FacesFlowReturn value)
    {
        if (returnList == null)
        {
            returnList = new ArrayList<>();
        }
        returnList.add(value);
    }
    
    @Override
    public List<NavigationRule> getNavigationRuleList()
    {
        if (navigationRuleList == null)
        {
            return Collections.emptyList();
        }
        return navigationRuleList;
    }

    public void addNavigationRule(NavigationRule value)
    {
        if (navigationRuleList == null)
        {
            navigationRuleList = new ArrayList<>();
        }
        navigationRuleList.add(value);
    }
    
    @Override
    public List<FacesFlowCall> getFlowCallList()
    {
        if (flowCallList == null)
        {
            return Collections.emptyList();
        }
        return flowCallList;
    }

    public void addFlowCall(FacesFlowCall value)
    {
        if (flowCallList == null)
        {
            flowCallList = new ArrayList<>();
        }
        flowCallList.add(value);
    }

    @Override
    public List<FacesFlowMethodCall> getMethodCallList()
    {
        if (methodCallList == null)
        {
            return Collections.emptyList();
        }
        return methodCallList;
    }

    public void addMethodCall(FacesFlowMethodCall value)
    {
        if (methodCallList == null)
        {
            methodCallList = new ArrayList<>();
        }
        methodCallList.add(value);
    }
    
    @Override
    public String getInitializer()
    {
        return initializer;
    }

    @Override
    public String getFinalizer()
    {
        return finalizer;
    }

    @Override
    public List<FacesFlowParameter> getInboundParameterList()
    {
        if (inboundParameterList == null)
        {
            return Collections.emptyList();
        }
        return inboundParameterList;
    }

    public void addInboundParameter(FacesFlowParameter value)
    {
        if (inboundParameterList == null)
        {
            inboundParameterList = new ArrayList<>();
        }
        inboundParameterList.add(value);
    }

    public void setStartNode(String startNode)
    {
        this.startNode = startNode;
    }

    public void setInitializer(String initializer)
    {
        this.initializer = initializer;
    }

    public void setFinalizer(String finalizer)
    {
        this.finalizer = finalizer;
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
    public String getDefiningDocumentId()
    {
        return definingDocumentId;
    }

    public void setDefiningDocumentId(String definingDocumentId)
    {
        this.definingDocumentId = definingDocumentId;
    }
}
