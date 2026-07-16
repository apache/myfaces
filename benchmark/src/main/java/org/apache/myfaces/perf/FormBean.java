package org.apache.myfaces.perf;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Backing bean for the form-inputs postback scenario. */
@Named
@SessionScoped
public class FormBean implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int FIELD_COUNT = 35;

    private String name;
    private int quantity;
    private double price;
    private List<String> fields;

    @PostConstruct
    public void init()
    {
        name = "sample";
        quantity = 1;
        price = 9.99;
        fields = new ArrayList<>(FIELD_COUNT);
        for (int i = 0; i < FIELD_COUNT; i++)
        {
            fields.add("value-" + i);
        }
    }

    public String submit()
    {
        // no navigation; stays on the same view (postback)
        return null;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public List<String> getFields()
    {
        return fields;
    }

    public void setFields(List<String> fields)
    {
        this.fields = fields;
    }
}
