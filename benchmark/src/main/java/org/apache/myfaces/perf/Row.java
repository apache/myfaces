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
