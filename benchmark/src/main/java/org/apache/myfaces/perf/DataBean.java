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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;

/** Shared read-only row data for the table/repeat/foreach/nested readonly scenarios. */
@Named
@ApplicationScoped
public class DataBean
{
    public static final int READONLY_ROWS = 200;
    public static final int FOREACH_ROWS = 100;
    public static final int GROUPS = 20;
    public static final int GROUP_ROWS = 10;

    private List<Row> rows;
    private List<Row> foreachRows;
    private List<Group> groups;

    @PostConstruct
    public void init()
    {
        rows = buildRows(READONLY_ROWS);
        foreachRows = buildRows(FOREACH_ROWS);

        List<Group> gs = new ArrayList<>(GROUPS);
        for (int g = 0; g < GROUPS; g++)
        {
            gs.add(new Group(g, buildRows(GROUP_ROWS)));
        }
        groups = gs;
    }

    private static List<Row> buildRows(int n)
    {
        List<Row> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
        {
            list.add(new Row(i));
        }
        return list;
    }

    public List<Row> getRows()
    {
        return rows;
    }

    public List<Row> getForeachRows()
    {
        return foreachRows;
    }

    public List<Group> getGroups()
    {
        return groups;
    }
}
