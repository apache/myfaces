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
package org.apache.myfaces.view.facelets.component.rowstate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("rowStateBean")
@SessionScoped
public class RowStateBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static class Item implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private String name;
        private List<Item> items;

        public Item(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public List<Item> getItems()
        {
            return items;
        }
    }

    private final List<Item> top = new ArrayList<>();
    private final List<Item> outer = new ArrayList<>();
    private String required;

    public RowStateBean()
    {
        for (int i = 0; i < 2; i++)
        {
            top.add(new Item("model-top-" + i));
            Item o = new Item("outer-" + i);
            o.items = new ArrayList<>();
            for (int j = 0; j < 2; j++)
            {
                o.items.add(new Item("model-nested-" + i + "-" + j));
            }
            outer.add(o);
        }
    }

    public List<Item> getTop()
    {
        return top;
    }

    public List<Item> getOuter()
    {
        return outer;
    }

    public String getRequired()
    {
        return required;
    }

    public void setRequired(String required)
    {
        this.required = required;
    }

    public void addInfoMessage()
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "info", null));
    }

    public void addFatalMessage()
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_FATAL, "fatal", null));
    }
}
