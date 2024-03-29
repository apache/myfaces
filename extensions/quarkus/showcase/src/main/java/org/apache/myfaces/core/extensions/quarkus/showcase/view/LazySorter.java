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
package org.apache.myfaces.core.extensions.quarkus.showcase.view;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

public class LazySorter implements Comparator<Car>
{

    private String sortField;
    private SortOrder sortOrder;

    public LazySorter(String sortField, SortOrder sortOrder)
    {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(Car car1, Car car2)
    {
        try
        {
            Object value1 = Car.class.getMethod("get" + capitalize(this.sortField)).invoke(car1);
            Object value2 = Car.class.getMethod("get" + capitalize(this.sortField)).invoke(car2);

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public String capitalize(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
