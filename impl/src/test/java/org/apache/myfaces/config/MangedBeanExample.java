package org.apache.myfaces.config;

/**
 * @author Dennis C. Byrne
 */

import java.util.List;
import java.util.Map;

public class MangedBeanExample
{

    private String managedProperty;
    private List<String> managedList;
    private List<String> writeOnlyList;
    private Map<String, String> managedMap;
    private Map<String, String> writeOnlyMap;

    public List<String> getManagedList()
    {
        return managedList;
    }

    public void setManagedList(List<String> managedList)
    {
        this.managedList = managedList;
    }

    public String getManagedProperty()
    {
        return managedProperty;
    }

    public void setManagedProperty(String managedProperty)
    {
        this.managedProperty = managedProperty;
    }

    public Map<String, String> getManagedMap()
    {
        return managedMap;
    }

    public void setManagedMap(Map<String, String> managedMap)
    {
        this.managedMap = managedMap;
    }

    public void setWriteOnlyList(List<String> writeOnlyList)
    {
        this.writeOnlyList = writeOnlyList;
    }

    public void setWriteOnlyMap(Map<String, String> writeOnlyMap)
    {
        this.writeOnlyMap = writeOnlyMap;
    }

    public Map<String, String> getHiddenWriteOnlyMap()
    {
        return writeOnlyMap;
    }

    public List<String> getHiddenWriteOnlyList()
    {
        return writeOnlyList;
    }

}
