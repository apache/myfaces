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

import java.util.Map;
import javax.faces.context.FacesContext;
import org.apache.myfaces.spi.ViewScopeProvider;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Test;
import org.testng.Assert;

/**
 *
 */
public class SerializedViewCollectionTestCase extends AbstractJsfTestCase
{
    
    @Test
    public void testSerializedViewCollection1()
    {
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new IntIntSerializedViewKey(viewId.hashCode(), 1);
        SerializedViewKey key2 = new IntIntSerializedViewKey(viewId.hashCode(), 2);
        
        TestViewScopeProvider provider = new TestViewScopeProvider();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, provider, "1");
        Assert.assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, provider, "2");
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNull(collection.get(key1));
        Assert.assertEquals(provider.getDestroyCount(), 1);
        
    }
    
    @Test
    public void testSerializedViewCollection2()
    {
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM, "2");
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new IntIntSerializedViewKey(viewId.hashCode(), 1);
        SerializedViewKey key2 = new IntIntSerializedViewKey(viewId.hashCode(), 2);
        SerializedViewKey key3 = new IntIntSerializedViewKey(viewId.hashCode(), 3);
        
        TestViewScopeProvider provider = new TestViewScopeProvider();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, provider, "1");
        Assert.assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, provider, "3");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key2, key1, provider, "2");
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        Assert.assertNull(collection.get(key1));
        Assert.assertEquals(provider.getDestroyCount(), 1);
        
    }    
    
    @Test
    public void testSerializedViewCollection3()
    {
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new IntIntSerializedViewKey(viewId.hashCode(), 1);
        SerializedViewKey key2 = new IntIntSerializedViewKey(viewId.hashCode(), 2);
        SerializedViewKey key3 = new IntIntSerializedViewKey(viewId.hashCode(), 3);
        
        TestViewScopeProvider provider = new TestViewScopeProvider();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, provider, "1");
        Assert.assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, provider, "1");
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNull(collection.get(key1));
        // Destroy should not happen, because there is still one view holding the viewScopeId.
        Assert.assertEquals(provider.getDestroyCount(), 0);
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, provider, "2");
        Assert.assertNotNull(collection.get(key3));
        Assert.assertNull(collection.get(key2));
        // Now it should be destroyed the view 1
        Assert.assertEquals(provider.getDestroyCount(), 1);
    }
    
    @Test
    public void testSerializedViewCollection4()
    {
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM, "2");
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new IntIntSerializedViewKey(viewId.hashCode(), 1);
        SerializedViewKey key2 = new IntIntSerializedViewKey(viewId.hashCode(), 2);
        SerializedViewKey key3 = new IntIntSerializedViewKey(viewId.hashCode(), 3);
        
        TestViewScopeProvider provider = new TestViewScopeProvider();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, provider, "1");
        Assert.assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, provider, "3");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key2, key1, provider, "1");
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        Assert.assertNull(collection.get(key1));
        Assert.assertEquals(provider.getDestroyCount(), 0);
        
    }   
    
    @Test
    public void testSerializedViewCollection5()
    {
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM, "3");
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM, "1");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new IntIntSerializedViewKey(viewId.hashCode(), 1);
        SerializedViewKey key2 = new IntIntSerializedViewKey(viewId.hashCode(), 2);
        SerializedViewKey key3 = new IntIntSerializedViewKey(viewId.hashCode(), 3);
        SerializedViewKey key4 = new IntIntSerializedViewKey(viewId.hashCode(), 4);
        
        TestViewScopeProvider provider = new TestViewScopeProvider();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, provider, "1");
        Assert.assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, provider, "2");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, provider, "3");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, provider, "1");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        
        // The are 3 slots, and when enters key4 the algorithm should not discard the most
        // recently used, so key1 and key3 should be preserved and key2 discarded.
        collection.put(facesContext, new Object[]{null,null,2}, key4, null, provider, "4");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        Assert.assertNotNull(collection.get(key4));
        

        Assert.assertEquals(provider.getDestroyCount(), 1);
    }
    
    @Test
    public void testSerializedViewCollection6()
    {
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_VIEWS_IN_SESSION_PARAM, "4");
        servletContext.addInitParameter(ServerSideStateCacheImpl.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION_PARAM, "2");
        
        SerializedViewCollection collection = new SerializedViewCollection();
        String viewId = "/test.xhtml";
        SerializedViewKey key1 = new IntIntSerializedViewKey(viewId.hashCode(), 1);
        SerializedViewKey key2 = new IntIntSerializedViewKey(viewId.hashCode(), 2);
        SerializedViewKey key3 = new IntIntSerializedViewKey(viewId.hashCode(), 3);
        SerializedViewKey key4 = new IntIntSerializedViewKey(viewId.hashCode(), 4);
        SerializedViewKey key5 = new IntIntSerializedViewKey(viewId.hashCode(), 5);
        SerializedViewKey key6 = new IntIntSerializedViewKey(viewId.hashCode(), 6);
        SerializedViewKey key7 = new IntIntSerializedViewKey(viewId.hashCode(), 7);
        SerializedViewKey key8 = new IntIntSerializedViewKey(viewId.hashCode(), 8);
        SerializedViewKey key9 = new IntIntSerializedViewKey(viewId.hashCode(), 9);
        
        TestViewScopeProvider provider = new TestViewScopeProvider();
        
        collection.put(facesContext, new Object[]{null,null,2}, key1, null, provider, "1");
        Assert.assertNotNull(collection.get(key1));
        collection.put(facesContext, new Object[]{null,null,2}, key2, key1, provider, "2");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        collection.put(facesContext, new Object[]{null,null,2}, key3, null, provider, "3");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        collection.put(facesContext, new Object[]{null,null,2}, key4, key3, provider, "4");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        Assert.assertNotNull(collection.get(key4));
        collection.put(facesContext, new Object[]{null,null,2}, key2, null, provider, "2");
        Assert.assertNotNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        Assert.assertNotNull(collection.get(key4));
        
        // The collection is full, but under a new key should remove key1
        collection.put(facesContext, new Object[]{null,null,2}, key5, null, provider, "5");
        Assert.assertNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNotNull(collection.get(key3));
        Assert.assertNotNull(collection.get(key4));
        Assert.assertNotNull(collection.get(key5));
        
        // The next oldest is key2, but it was refreshed, so the next one in age is key3
        collection.put(facesContext, new Object[]{null,null,2}, key6, null, provider, "6");
        Assert.assertNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNull(collection.get(key3));
        Assert.assertNotNull(collection.get(key4));
        Assert.assertNotNull(collection.get(key5));
        Assert.assertNotNull(collection.get(key6));
        
        // There is a sequential view for key6, destroy the oldest one, which is key4
        collection.put(facesContext, new Object[]{null,null,2}, key7, key6, provider, "7");
        Assert.assertNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNull(collection.get(key3));
        Assert.assertNull(collection.get(key4));
        Assert.assertNotNull(collection.get(key5));
        Assert.assertNotNull(collection.get(key6));
        Assert.assertNotNull(collection.get(key7));
        
        // Since org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION is 2, and we have
        // the sequence [key6, key7, key8] , the one to destroy is key6. 
        collection.put(facesContext, new Object[]{null,null,2}, key8, key7, provider, "8");
        Assert.assertNull(collection.get(key1));
        Assert.assertNotNull(collection.get(key2));
        Assert.assertNull(collection.get(key3));
        Assert.assertNull(collection.get(key4));
        Assert.assertNotNull(collection.get(key5));
        Assert.assertNull(collection.get(key6));
        Assert.assertNotNull(collection.get(key7));
        Assert.assertNotNull(collection.get(key8));
        
        // This is a sequence [key2, key9], but the oldest one is key2, so in this case
        // key2 should be removed.
        collection.put(facesContext, new Object[]{null,null,2}, key9, key2, provider, "9");
        Assert.assertNull(collection.get(key1));
        Assert.assertNull(collection.get(key2));
        Assert.assertNull(collection.get(key3));
        Assert.assertNull(collection.get(key4));
        Assert.assertNotNull(collection.get(key5));
        Assert.assertNull(collection.get(key6));
        Assert.assertNotNull(collection.get(key7));
        Assert.assertNotNull(collection.get(key8));
        Assert.assertNotNull(collection.get(key9));
        
        Assert.assertEquals(provider.getDestroyCount(), 5);
    }
    
    private static class TestViewScopeProvider extends ViewScopeProvider
    {
        private int destroyCount = 0;

        @Override
        public void onSessionDestroyed()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String generateViewScopeId(FacesContext facesContext)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, Object> createViewScopeMap(FacesContext facesContext, String viewScopeId)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, Object> restoreViewScopeMap(FacesContext facesContext, String viewScopeId)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void destroyViewScopeMap(FacesContext facesContext, String viewScopeId)
        {
            destroyCount++;
        }

        /**
         * @return the destroyCount
         */
        public int getDestroyCount()
        {
            return destroyCount;
        }

        /**
         * @param destroyCount the destroyCount to set
         */
        public void setDestroyCount(int destroyCount)
        {
            this.destroyCount = destroyCount;
        }
        
    }
}
