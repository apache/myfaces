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
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import org.apache.myfaces.view.facelets.component.rowstate.RowStateBean.Item;

/**
 * Bean of rowStatePreservedUpdateModel.xhtml and rowStateSharedSavedState.xhtml, see
 * {@link RowStatePreservedUpdateModelTest} and {@link RowStateSharedSavedStateTest}.
 */
@Named("rowStateStaleBean")
@SessionScoped
public class RowStateStaleBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<Item> preserved = List.of(new Item("original a"), new Item("original b"));
    private final List<Item> repeated = List.of(new Item("original e"), new Item("original f"));
    private final List<Item> plain = List.of(new Item("original c"), new Item("original d"));

    private final List<Item> tableRows = List.of(new Item("zero"), new Item("one"));
    private final List<Item> repeatRows = List.of(new Item("zero"), new Item("one"));
    private String required;

    public void change(AjaxBehaviorEvent event)
    {
        preserved.forEach(item -> item.setName("changed"));
        repeated.forEach(item -> item.setName("changed"));
        plain.forEach(item -> item.setName("changed"));
    }

    public List<Item> getPreserved()
    {
        return preserved;
    }

    public List<Item> getRepeated()
    {
        return repeated;
    }

    public List<Item> getPlain()
    {
        return plain;
    }

    public List<Item> getTableRows()
    {
        return tableRows;
    }

    public List<Item> getRepeatRows()
    {
        return repeatRows;
    }

    public String getRequired()
    {
        return required;
    }

    public void setRequired(String required)
    {
        this.required = required;
    }
}
