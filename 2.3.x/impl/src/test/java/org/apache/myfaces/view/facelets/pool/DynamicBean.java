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

import java.io.Serializable;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 */
@ManagedBean(name="dynamicBean")
@SessionScoped
public class DynamicBean implements Serializable
{
    
    private boolean panel1;
    
    private Locale locale;
    
    private String contract;
    
    private boolean resource1;
    
    public DynamicBean()
    {
    }
    
    @PostConstruct
    public void init()
    {
        locale = Locale.US;
        contract = "blue";
    }

    /**
     * @return the panel1
     */
    public boolean isPanel1()
    {
        return panel1;
    }

    /**
     * @param panel1 the panel1 to set
     */
    public void setPanel1(boolean panel1)
    {
        this.panel1 = panel1;
    }

    /**
     * @return the locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * @return the contract
     */
    public String getContract()
    {
        return contract;
    }

    /**
     * @param contract the contract to set
     */
    public void setContract(String contract)
    {
        this.contract = contract;
    }

    /**
     * @return the resource1
     */
    public boolean isResource1()
    {
        return resource1;
    }

    /**
     * @param resource1 the resource1 to set
     */
    public void setResource1(boolean resource1)
    {
        this.resource1 = resource1;
    }
    
}
