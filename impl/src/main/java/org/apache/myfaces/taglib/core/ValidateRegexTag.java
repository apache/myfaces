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

import javax.faces.validator.Validator;
import javax.faces.validator.RegexValidator;
import javax.faces.context.FacesContext;
import javax.faces.webapp.ValidatorELTag;
import javax.servlet.jsp.JspException;
import javax.el.ValueExpression;
import javax.el.ExpressionFactory;
import javax.el.ELContext;

/**
 * JSP Tag class for {@link javax.faces.validator.RegexValidator}.
 *
 * @author Jan-Kees van Andel
 * @since 2.0
 */
public class ValidateRegexTag extends ValidatorELTag
{
    private static final long serialVersionUID = 8363913774859484811L;

    private ValueExpression _pattern;

    @Override
    protected Validator createValidator() throws JspException
    {
        RegexValidator validator = new RegexValidator();
        if (null != _pattern)
        {
            FacesContext fc = FacesContext.getCurrentInstance();
            ELContext elc = fc.getELContext();
            String pattern = (String)_pattern.getValue(elc);
            validator.setPattern(pattern);
        }
        else
        {
            throw new AssertionError("pattern may not be null");
        }
        return validator;
    }

    public ValueExpression getPattern() {
        return _pattern;
    }

    public void setPattern(ValueExpression pattern) {
        this._pattern = pattern;
    }

    @Override
    public void release()
    {
        this._pattern = null;
    }
}
