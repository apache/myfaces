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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("forEachInputSessionBean")
@SessionScoped
public class ForEachInputSessionBean implements Serializable
{
    private final List<ForEachInputItem> items = new ArrayList<>();
    private final List<ForEachInputItem> identityItems = new ArrayList<>();
    private final Set<ForEachInputItem> itemSet = new LinkedHashSet<>();
    private final List<List<ForEachInputItem>> groups = new ArrayList<>();

    public ForEachInputSessionBean()
    {
        items.add(new ForEachInputKeyedItem("a", "original a"));
        items.add(new ForEachInputKeyedItem("b", "original b"));

        identityItems.add(new ForEachInputItem("a", "original a"));
        identityItems.add(new ForEachInputItem("b", "original b"));

        itemSet.add(new ForEachInputItem("s1", "original s1"));
        itemSet.add(new ForEachInputItem("s2", "original s2"));

        groups.add(new ArrayList<>(List.of(new ForEachInputItem("g1", "original g1"))));
        groups.add(new ArrayList<>(List.of(new ForEachInputItem("g2", "original g2"))));
    }

    public List<ForEachInputItem> getItems()
    {
        return items;
    }

    public List<ForEachInputItem> getIdentityItems()
    {
        return identityItems;
    }

    public Set<ForEachInputItem> getItemSet()
    {
        return itemSet;
    }

    public List<List<ForEachInputItem>> getGroups()
    {
        return groups;
    }
}
