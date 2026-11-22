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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.TreeSet;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Test;

class SectionUniqueIdCounterTest extends AbstractFacesTestCase
{
    @Test
    void simpleCounter()
    {
        Set<String> idSet = new TreeSet<String>();
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.startUniqueIdSection()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.startUniqueIdSection()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        counter.endUniqueIdSection();
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        counter.endUniqueIdSection();
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        assertTrue(idSet.add(counter.generateUniqueId()));
        
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
    void cachedCounter()
    {
        String[] cache = SectionUniqueIdCounter.generateUniqueIdCache("_", 10);
        
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter("_", cache);
        SectionUniqueIdCounter counterOrig = new SectionUniqueIdCounter("_");
        
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        
        counterOrig.startUniqueIdSection();
        counter.startUniqueIdSection();
        
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());

        counterOrig.endUniqueIdSection();
        counter.endUniqueIdSection();
        
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
        assertEquals(counterOrig.generateUniqueId(), counter.generateUniqueId());
    }

    @Test
    void counterExpansion()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        assertEquals("1", counter.generateUniqueId());
        assertEquals("2", counter.generateUniqueId());
        assertEquals("2_X", counter.startUniqueIdSection("X"));
        assertEquals("2_X_1", counter.generateUniqueId());
        assertEquals("2_X_2",counter.generateUniqueId());
        assertEquals("2_X_3",counter.startUniqueIdSection());
        assertEquals("2_X_3_1",counter.generateUniqueId());
        assertEquals("2_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        assertEquals("2_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        assertEquals("2_Y", counter.startUniqueIdSection("Y"));
        assertEquals("2_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        assertEquals("3", counter.generateUniqueId());
        assertEquals("4", counter.generateUniqueId());
    }

    @Test
    void counterExpansion2()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        assertEquals("1", counter.generateUniqueId());
        assertEquals("2",counter.startUniqueIdSection());
        assertEquals("2_1",counter.startUniqueIdSection());
        assertEquals("2_1_1", counter.generateUniqueId());
        counter.endUniqueIdSection();
        assertEquals("2_2", counter.generateUniqueId());
        counter.endUniqueIdSection();
        assertEquals("3", counter.generateUniqueId());
        
        assertEquals("3_X", counter.startUniqueIdSection("X"));
        assertEquals("3_X_1", counter.generateUniqueId());
        assertEquals("3_X_2",counter.generateUniqueId());
        assertEquals("3_X_3",counter.startUniqueIdSection());
        assertEquals("3_X_3_1",counter.generateUniqueId());
        assertEquals("3_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        assertEquals("3_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        assertEquals("3_Y", counter.startUniqueIdSection("Y"));
        assertEquals("3_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        assertEquals("4",counter.startUniqueIdSection());
        counter.endUniqueIdSection();
        assertEquals("5", counter.generateUniqueId());
        assertEquals("6", counter.generateUniqueId());
    }

    @Test
    void counterExpansion3()
    {
        SectionUniqueIdCounter counter = new SectionUniqueIdCounter();
        
        assertEquals("1",counter.startUniqueIdSection());
        assertEquals("1_1", counter.generateUniqueId());
        assertEquals("1_2",counter.startUniqueIdSection());
        assertEquals("1_2_1",counter.startUniqueIdSection());
        assertEquals("1_2_1_1", counter.generateUniqueId());
        counter.endUniqueIdSection();
        assertEquals("1_2_2", counter.generateUniqueId());
        counter.endUniqueIdSection();
        assertEquals("1_3", counter.generateUniqueId());
        
        assertEquals("1_3_X", counter.startUniqueIdSection("X"));
        assertEquals("1_3_X_1", counter.generateUniqueId());
        assertEquals("1_3_X_2",counter.generateUniqueId());
        assertEquals("1_3_X_3",counter.startUniqueIdSection());
        assertEquals("1_3_X_3_1",counter.generateUniqueId());
        assertEquals("1_3_X_3_2",counter.generateUniqueId());
        counter.endUniqueIdSection();
        assertEquals("1_3_X_4",counter.generateUniqueId());
        counter.endUniqueIdSection("X");
        assertEquals("1_3_Y", counter.startUniqueIdSection("Y"));
        assertEquals("1_3_Y_1", counter.generateUniqueId());
        counter.endUniqueIdSection("Y");
        assertEquals("1_4",counter.startUniqueIdSection());
        counter.endUniqueIdSection();
        assertEquals("1_5", counter.generateUniqueId());
        assertEquals("1_6", counter.generateUniqueId());
        counter.endUniqueIdSection();
    }    
}
