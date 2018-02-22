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
package org.apache.myfaces.config;

/**
 * Creates an environment to easily test the creation and initialization of
 * managed beans.
 * 
 * @author Dennis C. Byrne
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.myfaces.config.impl.digester.elements.ListEntriesImpl;
import org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl;
import org.apache.myfaces.config.impl.digester.elements.ManagedPropertyImpl;
import org.apache.myfaces.config.impl.digester.elements.MapEntriesImpl;
import org.apache.myfaces.config.impl.digester.elements.ListEntriesImpl.Entry;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

public abstract class AbstractManagedBeanBuilderTestCase extends AbstractJsfTestCase {

    public AbstractManagedBeanBuilderTestCase(String name) {
        super(name);
    }

    protected MangedBeanExample example;
    
    // managed property values
    protected static final List<String> MANAGED_LIST = new ArrayList<String>();
    protected static final Map<String, String> MANAGED_MAP = new HashMap<String, String>();
    protected static final String INJECTED_VALUE = "tatiana";
    
    /**
     * Skips digester and manually builds and configures a managed bean.
     */
    
    protected void setUp() throws Exception
  {
        super.setUp();
        ManagedBeanBuilder managedBeanBuilder = new ManagedBeanBuilder();
        ManagedBeanImpl managedBean = new ManagedBeanImpl();
        
        managedBean.setBeanClass(MangedBeanExample.class.getName());
        managedBean.setName("managed");
        managedBean.setScope("request");
        
        // test methods of children will want to make sure these values come 
        // out on the other end of this.
        MANAGED_LIST.add("0");
        MANAGED_LIST.add("1");
        MANAGED_LIST.add("2");
        MANAGED_MAP.put("0", "0");
        MANAGED_MAP.put("1", "1");
        MANAGED_MAP.put("2", "2");
        
        ManagedPropertyImpl managedProperty = new ManagedPropertyImpl();
        managedProperty.setPropertyName("managedProperty");
        managedProperty.setValue(INJECTED_VALUE);
        
        ManagedPropertyImpl managedList = new ManagedPropertyImpl();
        managedList.setPropertyName("managedList");
        ListEntriesImpl listEntries = makeListEntries();
        managedList.setListEntries(listEntries);
        
        ManagedPropertyImpl writeOnlyList = new ManagedPropertyImpl();
        writeOnlyList.setPropertyName("writeOnlyList");
        ListEntriesImpl writeOnlyListEntries = makeListEntries();
        writeOnlyList.setListEntries(writeOnlyListEntries);
        
        ManagedPropertyImpl managedMap = new ManagedPropertyImpl();
        managedMap.setPropertyName("managedMap");
        MapEntriesImpl mapEntries = makeMapEntries();
        managedMap.setMapEntries(mapEntries);
        
        ManagedPropertyImpl writeOnlyMap = new ManagedPropertyImpl();
        writeOnlyMap.setPropertyName("writeOnlyMap");
        MapEntriesImpl writeOnlyMapEntries = makeMapEntries();
        writeOnlyMap.setMapEntries(writeOnlyMapEntries);        
        
        managedBean.addProperty(managedProperty);
        managedBean.addProperty(managedList);
        managedBean.addProperty(writeOnlyList);
        managedBean.addProperty(managedMap);
        managedBean.addProperty(writeOnlyMap);

        // simulate a managed bean creation
        example = (MangedBeanExample) managedBeanBuilder
            .buildManagedBean(facesContext, managedBean);
    }
    
    public void tearDown() throws Exception{
        super.tearDown();
        example = null;
        MANAGED_LIST.clear();
        MANAGED_MAP.clear();
    }
    
    private ListEntriesImpl makeListEntries(){
        ListEntriesImpl listEntries = new ListEntriesImpl();
        
        for(int i = 0; i < MANAGED_LIST.size(); i++){
            Entry entry = new Entry();
            entry.setValue((String) MANAGED_LIST.get(i));
            listEntries.addEntry(entry);
        }
        return listEntries;
    }
    
    private MapEntriesImpl makeMapEntries(){
        MapEntriesImpl mapEntries = new MapEntriesImpl();
        
        for(int i = 0 ; i < MANAGED_MAP.size(); i++){
            MapEntriesImpl.Entry mapEntry = new MapEntriesImpl.Entry();
            mapEntry.setKey((String) MANAGED_MAP.get(i + ""));
            mapEntry.setValue((String) MANAGED_MAP.get(i + ""));
            mapEntries.addEntry(mapEntry);
        }
        return mapEntries;
    }
    
}
