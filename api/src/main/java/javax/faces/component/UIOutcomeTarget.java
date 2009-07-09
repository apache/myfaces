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
package javax.faces.component;

import javax.el.ValueExpression;

public class UIOutcomeTarget extends UIOutput
{
    public static final String COMPONENT_TYPE = "javax.faces.OutcomeTarget";
    public static final String COMPONENT_FAMILY = "javax.faces.OutcomeTarget";
    
    private static final boolean DEFAULT_INCLUDEVIEWPARAMS = false;
    
    private String _outcome;
    private boolean _includeViewParams;
    
    public UIOutcomeTarget()
    {
        super();
        setRendererType("javax.faces.Link");
    }
    
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public String getOutcome()
    {
        if (_outcome != null)
        {
            return _outcome;
        }
        
        ValueExpression expression = getValueExpression("Outcome");
        if (expression != null)
        {
            return (String) expression.getValue(getFacesContext().getELContext());
        }
        
        if(isInView())  //default to the view id
        {
            return getFacesContext().getViewRoot().getViewId();
        }
        
        return _outcome;
    }

    public void setOutcome(String outcome)
    {
        _outcome = outcome;
    }

    public boolean isIncludeViewParams()
    {        
        return getExpressionValue("includePageParams", _includeViewParams, DEFAULT_INCLUDEVIEWPARAMS);
    }

    public void setIncludeViewParams(boolean includeViewParams)
    {
        _includeViewParams = includeViewParams;
    }

    
}
