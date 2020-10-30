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
package org.apache.myfaces.view.facelets.pss.acid.managed;

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 *
 * @author lu4242
 */
@Named("acidTestBean")
@RequestScoped
public class AcidTestBean
{

    private List<ValueHolder> values = new ArrayList<ValueHolder>();
    
    private String param2;

    public AcidTestBean()
    {
        param2 = "value2";
    }
    
    @PostConstruct
    public void init()
    {
        values.add(new ValueHolder("A-"+System.currentTimeMillis()));
        values.add(new ValueHolder("B-"+System.currentTimeMillis()));
        values.add(new ValueHolder("C-"+System.currentTimeMillis()));
    }
    
    /**
     * @return the values
     */
    public List<ValueHolder> getValues()
    {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(List<ValueHolder> values)
    {
        this.values = values;
    }

    /**
     * @return the param2
     */
    public String getParam2()
    {
        return param2;
    }

    /**
     * @param param2 the param2 to set
     */
    public void setParam2(String param2)
    {
        this.param2 = param2;
    }

    
}
