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
