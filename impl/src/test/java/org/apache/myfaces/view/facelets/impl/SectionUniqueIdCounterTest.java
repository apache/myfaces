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

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;

public class SectionUniqueIdCounterTest extends AbstractJsfTestCase
{
    @Test
    public void testSimpleCounter()
    {
        Set<String> idSet = new TreeSet<String>();
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.startUniqueIdSection()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.startUniqueIdSection()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        counter.endUniqueIdSection();
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        counter.endUniqueIdSection();
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        Assert.assertTrue(idSet.add(counter.generateUniqueId()));
        
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
        
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        
        counterOrig.startUniqueIdSection();
        counter.startUniqueIdSection();
        
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());

        counterOrig.endUniqueIdSection();
        counter.endUniqueIdSection();
        
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        Assert.assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
    }
    
    @Test
    public void testCounterExpansion()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assert.assertEquals("1", counter.generateUniqueId());
        Assert.assertEquals("2", counter.generateUniqueId());
        Assert.assertEquals("2_X", counter.startUniqueIdSection("X"));
        Assert.assertEquals("2_X_1", counter.generateUniqueId());
        Assert.assertEquals("2_X_2",counter.generateUniqueId());
        Assert.assertEquals("2_X_3",counter.startUniqueIdSection());
        Assert.assertEquals("2_X_3_1",counter.generateUniqueId());
        Assert.assertEquals("2_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assert.assertEquals("2_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        Assert.assertEquals("2_Y", counter.startUniqueIdSection("Y"));
        Assert.assertEquals("2_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        Assert.assertEquals("3", counter.generateUniqueId());
        Assert.assertEquals("4", counter.generateUniqueId());
    }
    
    @Test
    public void testCounterExpansion2()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assert.assertEquals("1", counter.generateUniqueId());
        Assert.assertEquals("2",counter.startUniqueIdSection());
        Assert.assertEquals("2_1",counter.startUniqueIdSection());
        Assert.assertEquals("2_1_1", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assert.assertEquals("2_2", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assert.assertEquals("3", counter.generateUniqueId());
        
        Assert.assertEquals("3_X", counter.startUniqueIdSection("X"));
        Assert.assertEquals("3_X_1", counter.generateUniqueId());
        Assert.assertEquals("3_X_2",counter.generateUniqueId());
        Assert.assertEquals("3_X_3",counter.startUniqueIdSection());
        Assert.assertEquals("3_X_3_1",counter.generateUniqueId());
        Assert.assertEquals("3_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assert.assertEquals("3_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        Assert.assertEquals("3_Y", counter.startUniqueIdSection("Y"));
        Assert.assertEquals("3_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        Assert.assertEquals("4",counter.startUniqueIdSection());
        counter.endUniqueIdSection();
        Assert.assertEquals("5", counter.generateUniqueId());
        Assert.assertEquals("6", counter.generateUniqueId());
    }
    
    @Test
    public void testCounterExpansion3()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        Assert.assertEquals("1",counter.startUniqueIdSection());
        Assert.assertEquals("1_1", counter.generateUniqueId());
        Assert.assertEquals("1_2",counter.startUniqueIdSection());
        Assert.assertEquals("1_2_1",counter.startUniqueIdSection());
        Assert.assertEquals("1_2_1_1", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assert.assertEquals("1_2_2", counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assert.assertEquals("1_3", counter.generateUniqueId());
        
        Assert.assertEquals("1_3_X", counter.startUniqueIdSection("X"));
        Assert.assertEquals("1_3_X_1", counter.generateUniqueId());
        Assert.assertEquals("1_3_X_2",counter.generateUniqueId());
        Assert.assertEquals("1_3_X_3",counter.startUniqueIdSection());
        Assert.assertEquals("1_3_X_3_1",counter.generateUniqueId());
        Assert.assertEquals("1_3_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        Assert.assertEquals("1_3_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        Assert.assertEquals("1_3_Y", counter.startUniqueIdSection("Y"));
        Assert.assertEquals("1_3_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        Assert.assertEquals("1_4",counter.startUniqueIdSection());
        counter.endUniqueIdSection();
        Assert.assertEquals("1_5", counter.generateUniqueId());
        Assert.assertEquals("1_6", counter.generateUniqueId());
        counter.endUniqueIdSection();
    }    
}
