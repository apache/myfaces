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
package org.apache.myfaces.core.api.shared;

import java.text.MessageFormat;
import java.util.Locale;

import javax.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/** 
 * This class encapsulates a FacesMessage to evaluate the label
 * expression on render response, where f:loadBundle is available
 */
public class ParametrizableFacesMessage extends FacesMessage
{
    private static final long serialVersionUID = 7792947730961657948L;

    private final Object args[];
    private String evaluatedDetail;
    private String evaluatedSummary;
    private transient Object evaluatedArgs[];
    private Locale locale;

    public ParametrizableFacesMessage(String summary, String detail, Object[] args, Locale locale)
    {
        super(summary, detail);
        if (locale == null)
        {
            throw new NullPointerException("locale");
        }
        this.locale = locale;
        this.args = args;
    }

    public ParametrizableFacesMessage(FacesMessage.Severity severity, String summary, String detail, Object[] args,
            Locale locale)
    {
        super(severity, summary, detail);
        if (locale == null)
        {
            throw new NullPointerException("locale");
        }
        this.locale = locale;
        this.args = args;
    }

    @Override
    public String getDetail()
    {
        if (evaluatedArgs == null && args != null)
        {
            evaluateArgs();
        }
        if (evaluatedDetail == null)
        {
            MessageFormat format = new MessageFormat(super.getDetail(), locale);
            evaluatedDetail = format.format(evaluatedArgs);
        }
        return evaluatedDetail;
    }

    @Override
    public void setDetail(String detail)
    {
        super.setDetail(detail);
        evaluatedDetail = null;
    }
    
    public String getUnformattedDetail()
    {
        return super.getDetail();
    }

    @Override
    public String getSummary()
    {
        if (evaluatedArgs == null && args != null)
        {
            evaluateArgs();
        }
        if (evaluatedSummary == null)
        {
            MessageFormat format = new MessageFormat(super.getSummary(), locale);
            evaluatedSummary = format.format(evaluatedArgs);
        }
        return evaluatedSummary;
    }

    @Override
    public void setSummary(String summary)
    {
        super.setSummary(summary);
        evaluatedSummary = null;
    }
    
    public String getUnformattedSummary()
    {
        return super.getSummary();
    }

    private void evaluateArgs()
    {
        FacesContext facesContext = null;
        
        evaluatedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++)
        {
            if (args[i] == null)
            {
                continue;
            }

            if (args[i] instanceof ValueExpression)
            {
                if (facesContext == null)
                {
                    facesContext = FacesContext.getCurrentInstance();
                }
                evaluatedArgs[i] = ((ValueExpression) args[i]).getValue(facesContext.getELContext());
            }
            else 
            {
                evaluatedArgs[i] = args[i];
            }
        }
    }
}
