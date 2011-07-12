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
package org.apache.myfaces.integrationtest;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Managed Bean for FormInputTest.
 *
 * @author Jakob Korherr
 */
@ManagedBean
@RequestScoped
public class FormInputBean implements Serializable
{

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);

    private String name;
    private Date dateOfBirth;
    private int siblings;

    public String submit(String resultPage)
    {
        // this method checks if EL-2.2 parameters work as expected
        return resultPage;
    }

    public String getFormattedDateOfBirth()
    {
        if (dateOfBirth != null)
        {
            return DATE_FORMAT.format(dateOfBirth);
        }
        return "";
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    public int getSiblings()
    {
        return siblings;
    }

    public void setSiblings(int siblings)
    {
        this.siblings = siblings;
    }

}
