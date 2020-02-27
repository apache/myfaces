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
package org.apache.myfaces.view.facelets.bean;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ValueChangeEvent;

public class DummyBean
{
    
    public String action()
    {
        return "success";
    }
    
    public void actionListener(ActionEvent evt)
    {
        System.out.println("actionListener");
    }

    public void validate(FacesContext context, UIComponent component, java.lang.Object value)
    {
        System.out.println("validate");
    }
    
    public void valueChange(ValueChangeEvent evt)
    {
        System.out.println("valueChange");
    }
    
    public String callMe()
    {
        return "success";
    }
}
