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
package org.apache.myfaces.view.facelets.el;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 * This MethodExpression contains a ValueExpression which resolves to 
 * the "real" MethodExpression that should be invoked. This is needed 
 * when the MethodExpression is on the parent composite component attribute map.
 * See FaceletViewDeclarationLanguage.retargetMethodExpressions() for details.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ValueExpressionMethodExpression extends MethodExpression
{
    
    private static final long serialVersionUID = -2847633717581167765L;
    
    private ValueExpression valueExpression;
    
    public ValueExpressionMethodExpression(ValueExpression valueExpression)
    {
        this.valueExpression = valueExpression;   
    }

    @Override
    public MethodInfo getMethodInfo(ELContext context)
    {
        return getMethodExpression(context).getMethodInfo(context);
    }

    @Override
    public Object invoke(ELContext context, Object[] params)
    {
        return getMethodExpression(context).invoke(context, params);
    }

    @Override
    public boolean equals(Object obj)
    {
        return getMethodExpression().equals(obj);
    }

    @Override
    public String getExpressionString()
    {
        return getMethodExpression().getExpressionString();
    }

    @Override
    public int hashCode()
    {
        return getMethodExpression().hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return getMethodExpression().isLiteralText();
    }
    
    private MethodExpression getMethodExpression()
    {
        return getMethodExpression(FacesContext.getCurrentInstance().getELContext());
    }
    
    private MethodExpression getMethodExpression(ELContext context)
    {
        return (MethodExpression) valueExpression.getValue(context);
    }

}
