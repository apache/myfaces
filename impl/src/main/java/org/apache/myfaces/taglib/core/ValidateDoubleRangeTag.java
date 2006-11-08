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

import org.apache.myfaces.convert.ConverterUtils;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.Validator;
import javax.faces.webapp.UIComponentTag;
import javax.faces.webapp.ValidatorTag;
import javax.servlet.jsp.JspException;

/**
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Manfred Geiler
 * @version $Revision$ $Date$
 */
public class ValidateDoubleRangeTag
    extends ValidatorTag
{
    private static final long serialVersionUID = 6396116656224588016L;

    private static final String VALIDATOR_ID = "javax.faces.DoubleRange";

    private String _minimum = null;
    private String _maximum = null;

    public void release()
    {
        _minimum = null;
        _maximum = null;
    }

    public void setMinimum(String minimum)
    {
        _minimum = minimum;
    }

    public void setMaximum(String maximum)
    {
        _maximum = maximum;
    }

    protected Validator createValidator()
        throws JspException
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        setValidatorId(VALIDATOR_ID);
        DoubleRangeValidator validator = (DoubleRangeValidator)super.createValidator();
        if (_minimum != null)
        {
            if (UIComponentTag.isValueReference(_minimum))
            {
                ValueBinding vb = facesContext.getApplication().createValueBinding(_minimum);
                validator.setMinimum(ConverterUtils.convertToDouble(vb.getValue(facesContext)));
            }
            else
            {
                validator.setMinimum(ConverterUtils.convertToDouble(_minimum));
            }
        }
        if (_maximum != null)
        {
            if (UIComponentTag.isValueReference(_maximum))
            {
                ValueBinding vb = facesContext.getApplication().createValueBinding(_maximum);
                validator.setMaximum(ConverterUtils.convertToDouble(vb.getValue(facesContext)));
            }
            else
            {
                validator.setMaximum(ConverterUtils.convertToDouble(_maximum));
            }
        }
        return validator;
    }


}
