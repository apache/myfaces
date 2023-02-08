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
package org.apache.myfaces.view.facelets.impl;

import java.util.Set;
import java.util.TreeSet;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SectionUniqueIdCounterTest extends AbstractJsfTestCase
{
    @Test
    public void testSimpleCounter()
    {
        Set<String> idSet = new TreeSet<String>();
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.startUniqueIdSection()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.startUniqueIdSection()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        counter.endUniqueIdSection();
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        counter.endUniqueIdSection();
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        Assertions.assertTrue(idSet.add(counter.generateUniqueId()));
        
        /*
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter("_");
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.startUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        counter.endUniqueIdSection();
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        System.out.println(UIViewRoot.UNIQUE_ID_PREFIX+counter.generateUniqueId());
        */
    }
    
    @Test
    public void testCachedCounter()
    {
        String[] cache = SectionUniqueIdCounter.generateUniqueIdCache("_", 10);
        
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter("_", cache);
        SectionUniqueIdCounter counterOrig = new SectionUniqueIdCounter("_");
        
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        
        counterOrig.startUniqueIdSection();
        counter.startUniqueIdSection();
        
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());

        counterOrig.endUniqueIdSection();
        counter.endUniqueIdSection();
        
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assertions.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
    }
    
    @Test
    public void testCounterExpansion()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assertions.assertEquals("1", counter.generateUniqueId());
        Assertions.assertEquals("2", counter.generateUniqueId());
        Assertions.assertEquals("2_X", counter.startUniqueIdSection("X"));
        Assertions.assertEquals("2_X_1", counter.generateUniqueId());
        Assertions.assertEquals("2_X_2",counter.generateUniqueId());
        Assertions.assertEquals("2_X_3",counter.startUniqueIdSection());
        Assertions.assertEquals("2_X_3_1",counter.generateUniqueId());
        Assertions.assertEquals("2_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assertions.assertEquals("2_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        Assertions.assertEquals("2_Y", counter.startUniqueIdSection("Y"));
        Assertions.assertEquals("2_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        Assertions.assertEquals("3", counter.generateUniqueId());
        Assertions.assertEquals("4", counter.generateUniqueId());
    }
    
    @Test
    public void testCounterExpansion2()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assertions.assertEquals("1", counter.generateUniqueId());
        Assertions.assertEquals("2",counter.startUniqueIdSection());
        Assertions.assertEquals("2_1",counter.startUniqueIdSection());
        Assertions.assertEquals("2_1_1", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assertions.assertEquals("2_2", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assertions.assertEquals("3", counter.generateUniqueId());
        
        Assertions.assertEquals("3_X", counter.startUniqueIdSection("X"));
        Assertions.assertEquals("3_X_1", counter.generateUniqueId());
        Assertions.assertEquals("3_X_2",counter.generateUniqueId());
        Assertions.assertEquals("3_X_3",counter.startUniqueIdSection());
        Assertions.assertEquals("3_X_3_1",counter.generateUniqueId());
        Assertions.assertEquals("3_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assertions.assertEquals("3_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        Assertions.assertEquals("3_Y", counter.startUniqueIdSection("Y"));
        Assertions.assertEquals("3_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        Assertions.assertEquals("4",counter.startUniqueIdSection());
        counter.endUniqueIdSection();
        Assertions.assertEquals("5", counter.generateUniqueId());
        Assertions.assertEquals("6", counter.generateUniqueId());
    }
    
    @Test
    public void testCounterExpansion3()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assertions.assertEquals("1",counter.startUniqueIdSection());
        Assertions.assertEquals("1_1", counter.generateUniqueId());
        Assertions.assertEquals("1_2",counter.startUniqueIdSection());
        Assertions.assertEquals("1_2_1",counter.startUniqueIdSection());
        Assertions.assertEquals("1_2_1_1", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assertions.assertEquals("1_2_2", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assertions.assertEquals("1_3", counter.generateUniqueId());
        
        Assertions.assertEquals("1_3_X", counter.startUniqueIdSection("X"));
        Assertions.assertEquals("1_3_X_1", counter.generateUniqueId());
        Assertions.assertEquals("1_3_X_2",counter.generateUniqueId());
        Assertions.assertEquals("1_3_X_3",counter.startUniqueIdSection());
        Assertions.assertEquals("1_3_X_3_1",counter.generateUniqueId());
        Assertions.assertEquals("1_3_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assertions.assertEquals("1_3_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        Assertions.assertEquals("1_3_Y", counter.startUniqueIdSection("Y"));
        Assertions.assertEquals("1_3_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        Assertions.assertEquals("1_4",counter.startUniqueIdSection());
        counter.endUniqueIdSection();
        Assertions.assertEquals("1_5", counter.generateUniqueId());
        Assertions.assertEquals("1_6", counter.generateUniqueId());
        counter.endUniqueIdSection();
    }    
}
