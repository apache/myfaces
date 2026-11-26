/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.application.viewstate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Test;

/**
 *
 */
class SerializedViewCollectionTestCase extends AbstractFacesTestCase
{

    @Test
    void serializedViewCollection1()
    {
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new SerializedViewKeyIntInt(viewId.hashCode(), 1);
        SerializedViewKey key2 = new SerializedViewKeyIntInt(viewId.hashCode(), 2);
                
        AtomicInteger destroyed = new AtomicInteger();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, "2", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key2));
        assertNull(collection.get(key1));
        assertEquals(1, destroyed.get());
        
    }

    @Test
    void serializedViewCollection2()
    {
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION, "2");
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new SerializedViewKeyIntInt(viewId.hashCode(), 1);
        SerializedViewKey key2 = new SerializedViewKeyIntInt(viewId.hashCode(), 2);
        SerializedViewKey key3 = new SerializedViewKeyIntInt(viewId.hashCode(), 3);
        
        AtomicInteger destroyed = new AtomicInteger();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, "3", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key2, key1, "2", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        assertNull(collection.get(key1));
        assertEquals(1, destroyed.get());
        
    }

    @Test
    void serializedViewCollection3()
    {
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new SerializedViewKeyIntInt(viewId.hashCode(), 1);
        SerializedViewKey key2 = new SerializedViewKeyIntInt(viewId.hashCode(), 2);
        SerializedViewKey key3 = new SerializedViewKeyIntInt(viewId.hashCode(), 3);
        
        AtomicInteger destroyed = new AtomicInteger();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key2));
        assertNull(collection.get(key1));
        // Destroy should not happen, because there is still one view holding the viewScopeId.
        assertEquals(0, destroyed.get());
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, "2", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key3));
        assertNull(collection.get(key2));
        // Now it should be destroyed the view 1
        assertEquals(1, destroyed.get());
    }

    @Test
    void serializedViewCollection4()
    {
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION, "2");
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new SerializedViewKeyIntInt(viewId.hashCode(), 1);
        SerializedViewKey key2 = new SerializedViewKeyIntInt(viewId.hashCode(), 2);
        SerializedViewKey key3 = new SerializedViewKeyIntInt(viewId.hashCode(), 3);
        
        AtomicInteger destroyed = new AtomicInteger();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, "3", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key2, key1, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        assertNull(collection.get(key1));
        assertEquals(0, destroyed.get());
        
    }

    @Test
    void serializedViewCollection5()
    {
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION, "3");
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new SerializedViewKeyIntInt(viewId.hashCode(), 1);
        SerializedViewKey key2 = new SerializedViewKeyIntInt(viewId.hashCode(), 2);
        SerializedViewKey key3 = new SerializedViewKeyIntInt(viewId.hashCode(), 3);
        SerializedViewKey key4 = new SerializedViewKeyIntInt(viewId.hashCode(), 4);
        
        AtomicInteger destroyed = new AtomicInteger();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, "2", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, "3", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        
        // The are 3 slots, and when enters key4 the algorithm should not discard the most
        // recently used, so key1 and key3 should be preserved and key2 discarded.
        collection.put(facesContext, new Object[]{null,null,2}, key4, null, "4", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        assertNotNull(collection.get(key4));


        assertEquals(1, destroyed.get());
    }

    @Test
    void serializedViewCollection6()
    {
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION, "4");
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION, "2");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new SerializedViewKeyIntInt(viewId.hashCode(), 1);
        SerializedViewKey key2 = new SerializedViewKeyIntInt(viewId.hashCode(), 2);
        SerializedViewKey key3 = new SerializedViewKeyIntInt(viewId.hashCode(), 3);
        SerializedViewKey key4 = new SerializedViewKeyIntInt(viewId.hashCode(), 4);
        SerializedViewKey key5 = new SerializedViewKeyIntInt(viewId.hashCode(), 5);
        SerializedViewKey key6 = new SerializedViewKeyIntInt(viewId.hashCode(), 6);
        SerializedViewKey key7 = new SerializedViewKeyIntInt(viewId.hashCode(), 7);
        SerializedViewKey key8 = new SerializedViewKeyIntInt(viewId.hashCode(), 8);
        SerializedViewKey key9 = new SerializedViewKeyIntInt(viewId.hashCode(), 9);
        
        AtomicInteger destroyed = new AtomicInteger();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, "1", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, key1, "2", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, "3", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key4, key3, "4", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        assertNotNull(collection.get(key4));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, "2", (id) -> destroyed.incrementAndGet());
        assertNotNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        assertNotNull(collection.get(key4));
        
        // The collection is full, but under a new key should remove key1
        collection.put(facesContext, new Object[]{null,null,2}, key5, null, "5", (id) -> destroyed.incrementAndGet());
        assertNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNotNull(collection.get(key3));
        assertNotNull(collection.get(key4));
        assertNotNull(collection.get(key5));
        
        // The next oldest is key2, but it was refreshed, so the next one in age is key3
        collection.put(facesContext, new Object[]{null,null,2}, key6, null, "6", (id) -> destroyed.incrementAndGet());
        assertNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNull(collection.get(key3));
        assertNotNull(collection.get(key4));
        assertNotNull(collection.get(key5));
        assertNotNull(collection.get(key6));
        
        // There is a sequential view for key6, destroy the oldest one, which is key4
        collection.put(facesContext, new Object[]{null,null,2}, key7, key6, "7", (id) -> destroyed.incrementAndGet());
        assertNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNull(collection.get(key3));
        assertNull(collection.get(key4));
        assertNotNull(collection.get(key5));
        assertNotNull(collection.get(key6));
        assertNotNull(collection.get(key7));
        
        // Since org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION is 2, and we have
        // the sequence [key6, key7, key8] , the one to destroy is key6. 
        collection.put(facesContext, new Object[]{null,null,2}, key8, key7, "8", (id) -> destroyed.incrementAndGet());
        assertNull(collection.get(key1));
        assertNotNull(collection.get(key2));
        assertNull(collection.get(key3));
        assertNull(collection.get(key4));
        assertNotNull(collection.get(key5));
        assertNull(collection.get(key6));
        assertNotNull(collection.get(key7));
        assertNotNull(collection.get(key8));
        
        // This is a sequence [key2, key9], but the oldest one is key2, so in this case
        // key2 should be removed.
        collection.put(facesContext, new Object[]{null,null,2}, key9, key2, "9", (id) -> destroyed.incrementAndGet());
        assertNull(collection.get(key1));
        assertNull(collection.get(key2));
        assertNull(collection.get(key3));
        assertNull(collection.get(key4));
        assertNotNull(collection.get(key5));
        assertNull(collection.get(key6));
        assertNotNull(collection.get(key7));
        assertNotNull(collection.get(key8));
        assertNotNull(collection.get(key9));

        assertEquals(5, destroyed.get());
    }
}
