package org.apache.myfaces.config;

/**
 * http://issues.apache.org/jira/browse/MYFACES-889?page=all
 * 
 * @author Dennis C. Byrne
 */

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Myfaces889TestCase extends AbstractManagedBeanBuilderTestCase
{

    public Myfaces889TestCase(String name)
    {
        super(name);
    }

    //private static Log log = LogFactory.getLog(Myfaces889TestCase.class);
    private static Logger log = Logger.getLogger(Myfaces889TestCase.class.getName());

    public void testWriteOnlyMap()
    {
        assertTrue(example != null);
        log.fine("managed bean successfully created");

        Map<String, String> writeOnlyMap = example.getHiddenWriteOnlyMap();

        assertTrue(writeOnlyMap != null);
        log.fine("managed map is not null");

        scrutinizeMap(writeOnlyMap);
    }

    public void testManagedMap()
    {
        assertTrue(example != null);
        log.fine("managed bean successfully created");

        Map<String, String> managedMap = example.getManagedMap();

        assertTrue(managedMap != null);
        log.fine("managed map is not null");

        scrutinizeMap(managedMap);
    }

    private void scrutinizeMap(Map<String, String> map)
    {
        assertTrue(map.size() == 3);
        log.fine("managed map has the correct size " + map.size());
        
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            String config = (String) MANAGED_MAP.get(key);
            log.fine("looking @ " + config + " and " + value);
            assertTrue(config.equals(value));
        }
    }

    public void testManagedList()
    {
        assertTrue(example != null);
        log.fine("managed bean successfully created");

        List<String> managedList = example.getManagedList();

        scrutinizeList(managedList);
    }

    public void testWriteOnlyList()
    {
        assertTrue(example != null);
        log.fine("managed bean successfully created");

        List<String> writeOnlyList = example.getHiddenWriteOnlyList();

        scrutinizeList(writeOnlyList);
    }

    private void scrutinizeList(List<String> list)
    {
        assertTrue(list != null);
        log.fine("managed list is not null " + list.size());
        assertTrue(list.size() == 3);
        log.fine("managed list has the correct size " + list.size());

        for (int i = 0; i < list.size(); i++)
        {
            String entry = list.get(i);
            String config = MANAGED_LIST.get(i);
            log.fine("looking @ " + config + " and " + entry);
            assertTrue(config.equals(entry));
        }
    }

    public void testManagedProperty()
    {
        assertTrue(example != null);
        log.fine("managed bean successfully created");

        String managedPropertyValue = example.getManagedProperty();

        assertTrue(INJECTED_VALUE.equals(managedPropertyValue));
        log.fine("managed property String has the correct value ");
    }

}
