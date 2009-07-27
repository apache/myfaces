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
package javax.faces.validator;

import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class BeanValidator implements PartialStateHolder, Validator
{    
    public static final String DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME = "javax.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR";
    public static final String EMPTY_VALIDATION_GROUPS_PATTERN = "^[\\W,]*$";
    public static final String MESSAGE_ID = "javax.faces.validator.BeanValidator.MESSAGE";
    public static final String VALIDATION_GROUPS_DELIMITER = ",";
    public static final String VALIDATOR_FACTORY_KEY = "javax.faces.validator.beanValidator.ValidatorFactory";
    public static final String VALIDATOR_ID = "javax.faces.Bean";
    
    private boolean _initialStateMarked = false;
    private boolean _transient;

    @Override
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    @Override
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    @Override
    public void markInitialState()
    {
        _initialStateMarked = true;
    }

    @Override
    public boolean isTransient()
    {
        return _transient;
    }
    
    @Override
    public void setTransient(boolean newTransientValue)
    {
        _transient = newTransientValue;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        // TODO implement here
    }

    @Override
    public Object saveState(FacesContext context)
    {
        // TODO implement here
        return null;
    }

    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) throws ValidatorException
    {
        // TODO implement here
    }
    
    public String getValidationGroups()
    {
        //TODO implement here
        return null;
    }
    
    public void setValidationGroups(String validationGroups)
    {
        //TODO implement here
    }

}
