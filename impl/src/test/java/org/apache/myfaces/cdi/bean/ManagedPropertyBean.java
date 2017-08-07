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
package org.apache.myfaces.cdi.bean;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.ManagedProperty;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class ManagedPropertyBean {

    @Inject
    @ManagedProperty("#{testBean.number}")
    private int numberManagedProperty;

    @Inject
    @ManagedProperty("#{testBean.text}")
    private String textManagedProperty;

    @Inject
    @ManagedProperty("#{testBean.list}")
    private List<String> listManagedProperty;

    @Inject
    @ManagedProperty("#{testBean.stringArray}")
    private String[] stringArrayManagedProperty;

    @Inject
    @ManagedProperty("#{testBean}")
    private TestBean bean;

    public String test() {
        return "numberManagedProperty = " + numberManagedProperty +
               "    textManagedProperty =  " + textManagedProperty +
               "    listManagedProperty = " + listManagedProperty.get(0) +
               "    stringArrayManagedProperty = " + stringArrayManagedProperty[0] +
               "    bean = " + bean.toString();
    }
}
