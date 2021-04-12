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
package org.apache.myfaces.view.facelets.tag.faces.core.reset;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Named;

/**
 *
 * @author Leonardo Uribe
 */
@Named("bean")
@SessionScoped
public class ResetValuesBean implements Serializable
{
    
    private String field1;
    
    private Integer field2;

    public void overrideField1(ActionEvent evt)
    {
        field1 = "overriden";
    }
    
    /**
     * @return the field1
     */
    public String getField1()
    {
        return field1;
    }

    /**
     * @param field1 the field1 to set
     */
    public void setField1(String field1)
    {
        this.field1 = field1;
    }

    /**
     * @return the field2
     */
    public Integer getField2()
    {
        return field2;
    }

    /**
     * @param field2 the field2 to set
     */
    public void setField2(Integer field2)
    {
        this.field2 = field2;
    }
    
}
