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
package org.apache.myfaces.taglib.core;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.faces.webapp.UIComponentClassicTagBase;
import javax.faces.webapp.UIComponentELTag;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.el.ValueExpression;
import javax.el.ELContext;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Bruno Aranda (JSR-252)
 * @version $Revision$ $Date$
 */
public class AttributeTag
        extends TagSupport
{
    private static final long serialVersionUID = 31476300171678632L;
    private ValueExpression _nameExpression;
    private ValueExpression _valueExpression;

    /**
     * @param nameExpression
     */
    public void setName(ValueExpression nameExpression)
    {
        _nameExpression = nameExpression;
    }

    /**
     * @param valueExpression
     */
    public void setValue(ValueExpression valueExpression)
    {
        _valueExpression = valueExpression;
    }


    public int doStartTag() throws JspException
    {
        UIComponentClassicTagBase componentTag = UIComponentELTag.getParentUIComponentClassicTagBase(pageContext);
        if (componentTag == null)
        {
            throw new JspException("no parent UIComponentTag found");
        }
        UIComponent component = componentTag.getComponentInstance();
        if (component == null)
        {
            throw new JspException("parent UIComponentTag has no UIComponent");
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();

        String name = null;
        Object value = null;
        boolean isLiteral = false;

        if (_nameExpression != null)
        {
             name = (String) _nameExpression.getValue(elContext);
        }

        if (_valueExpression != null)
        {
            isLiteral = _valueExpression.isLiteralText();
            value = _valueExpression.getValue(elContext);
        }

        if (name != null)
        {
            if (component.getAttributes().get(name) == null)
            {
                if (isLiteral)
                {
                   component.getAttributes().put(name, value);
                }
                else
                {
                    component.setValueExpression(name, _valueExpression);
                }
            }
        }

        return SKIP_BODY;
    }

    /**
     * @deprecated
     */
    public void release()
    {
        super.release();
        _nameExpression = null;
        _valueExpression = null;
    }

}
