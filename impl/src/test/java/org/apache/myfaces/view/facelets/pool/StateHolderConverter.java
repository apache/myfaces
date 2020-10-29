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
package org.apache.myfaces.view.facelets.pool;

import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

/**
 *
 */
@FacesConverter(value="oam.test.StateHolderConverter")
public class StateHolderConverter implements Converter, StateHolder
{
    private String param;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException
    {
        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException
    {
        return value != null ? value.toString() : null;
    }

    @Override
    public Object saveState(FacesContext context)
    {
        return param;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        param = (String) state;
    }

    @Override
    public boolean isTransient()
    {
        return false;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
    }

    /**
     * @return the param
     */
    public String getParam()
    {
        return param;
    }

    /**
     * @param param the param to set
     */
    public void setParam(String param)
    {
        this.param = param;
    }
    
}
