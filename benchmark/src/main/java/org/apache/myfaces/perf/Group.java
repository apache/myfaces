package org.apache.myfaces.perf;

import java.util.List;

/** A group of rows, for the nested (data-in-data) scenario. */
public class Group
{
    private final int id;
    private final String label;
    private final List<Row> rows;

    public Group(int id, List<Row> rows)
    {
        this.id = id;
        this.label = "Group " + id;
        this.rows = rows;
    }

    public int getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }

    public List<Row> getRows()
    {
        return rows;
    }
}
