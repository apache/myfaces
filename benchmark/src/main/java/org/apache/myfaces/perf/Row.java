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

package org.apache.myfaces.perf;

/** A realistic-ish table row (typed fields, a description with HTML-meta chars to exercise escaping). */
public class Row
{
    private final int id;
    private final String name;
    private final String description;
    private final double price;
    private final int quantity;
    private final boolean active;

    public Row(int id)
    {
        this.id = id;
        this.name = "Item " + id;
        this.description = "Row <" + id + "> & \"details\" — line " + id + " with meta chars";
        this.price = 9.99 + id;
        this.quantity = id % 100;
        this.active = (id % 2) == 0;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public double getPrice()
    {
        return price;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public boolean isActive()
    {
        return active;
    }
}
