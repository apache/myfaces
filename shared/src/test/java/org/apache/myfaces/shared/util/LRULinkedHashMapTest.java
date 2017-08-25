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
package org.apache.myfaces.shared.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests were copied from org.apache.commons.collections4.map.LRUMapTest
 *
 * @param <K>
 * @param <V>
 */
public class LRULinkedHashMapTest<K, V> {

   @Test
   public void testLRU() {
      final K[] keys = getSampleKeys();
      final V[] values = getSampleValues();
      Iterator<K> kit;
      Iterator<V> vit;

      final LRULinkedHashMap<K, V> map = new LRULinkedHashMap<>(2);
      assertEquals(0, map.size());

      map.put(keys[0], values[0]);
      assertEquals(1, map.size());

      map.put(keys[1], values[1]);
      assertEquals(2, map.size());
      kit = map.keySet().iterator();
      assertSame(keys[0], kit.next());
      assertSame(keys[1], kit.next());
      vit = map.values().iterator();
      assertSame(values[0], vit.next());
      assertSame(values[1], vit.next());

      map.put(keys[2], values[2]);
      assertEquals(2, map.size());
      kit = map.keySet().iterator();
      assertSame(keys[1], kit.next());
      assertSame(keys[2], kit.next());
      vit = map.values().iterator();
      assertSame(values[1], vit.next());
      assertSame(values[2], vit.next());

      map.put(keys[2], values[0]);
      assertEquals(2, map.size());
      kit = map.keySet().iterator();
      assertSame(keys[1], kit.next());
      assertSame(keys[2], kit.next());
      vit = map.values().iterator();
      assertSame(values[1], vit.next());
      assertSame(values[0], vit.next());

      map.put(keys[1], values[3]);
      assertEquals(2, map.size());
      kit = map.keySet().iterator();
      assertSame(keys[2], kit.next());
      assertSame(keys[1], kit.next());
      vit = map.values().iterator();
      assertSame(values[0], vit.next());
      assertSame(values[3], vit.next());
   }

   //-----------------------------------------------------------------------
   @Test
   public void testAccessOrder() {
      final K[] keys = getSampleKeys();
      final V[] values = getSampleValues();
      Iterator<K> kit;
      Iterator<V> vit;

      final LRULinkedHashMap<K, V> map = new LRULinkedHashMap<>(2);
      map.put(keys[0], values[0]);
      map.put(keys[1], values[1]);
      kit = map.keySet().iterator();
      assertSame(keys[0], kit.next());
      assertSame(keys[1], kit.next());
      vit = map.values().iterator();
      assertSame(values[0], vit.next());
      assertSame(values[1], vit.next());

      // no change to order
      map.put(keys[1], values[1]);
      kit = map.keySet().iterator();
      assertSame(keys[0], kit.next());
      assertSame(keys[1], kit.next());
      vit = map.values().iterator();
      assertSame(values[0], vit.next());
      assertSame(values[1], vit.next());

      // no change to order
      map.put(keys[1], values[2]);
      kit = map.keySet().iterator();
      assertSame(keys[0], kit.next());
      assertSame(keys[1], kit.next());
      vit = map.values().iterator();
      assertSame(values[0], vit.next());
      assertSame(values[2], vit.next());

      // change to order
      map.put(keys[0], values[3]);
      kit = map.keySet().iterator();
      assertSame(keys[1], kit.next());
      assertSame(keys[0], kit.next());
      vit = map.values().iterator();
      assertSame(values[2], vit.next());
      assertSame(values[3], vit.next());

      // change to order
      map.get(keys[1]);
      kit = map.keySet().iterator();
      assertSame(keys[0], kit.next());
      assertSame(keys[1], kit.next());
      vit = map.values().iterator();
      assertSame(values[3], vit.next());
      assertSame(values[2], vit.next());

      // change to order
      map.get(keys[0]);
      kit = map.keySet().iterator();
      assertSame(keys[1], kit.next());
      assertSame(keys[0], kit.next());
      vit = map.values().iterator();
      assertSame(values[2], vit.next());
      assertSame(values[3], vit.next());

      // no change to order
      map.get(keys[0]);
      kit = map.keySet().iterator();
      assertSame(keys[1], kit.next());
      assertSame(keys[0], kit.next());
      vit = map.values().iterator();
      assertSame(values[2], vit.next());
      assertSame(values[3], vit.next());
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testClone() {
      final LRULinkedHashMap<K, V> map = new LRULinkedHashMap<>(10);
      map.put((K) "1", (V) "1");
      final Map<K, V> cloned = (Map<K, V>) map.clone();
      assertEquals(map.size(), cloned.size());
      assertSame(map.get("1"), cloned.get("1"));
   }

   @Test
   public void testSynchronizedRemoveFromEntrySet() throws InterruptedException {

      final Map<Object, Thread> map = new LRULinkedHashMap<>(10000);

      final Map<Throwable, String> exceptions = new HashMap<>();
      final ThreadGroup tg = new ThreadGroup(LRULinkedHashMapTest.class.getSimpleName()) {
         @Override
         public void uncaughtException(final Thread t, final Throwable e) {
            exceptions.put(e, t.getName());
            super.uncaughtException(t, e);
         }
      };

      final int[] counter = new int[1];
      counter[0] = 0;
      final Thread[] threads = new Thread[50];
      for (int i = 0; i < threads.length; ++i) {
         threads[i] = new Thread(tg, "JUnit Thread " + i) {

            @Override
            public void run() {
               int i = 0;
               try {
                  synchronized (this) {
                     notifyAll();
                     wait();
                  }
                  final Thread thread = Thread.currentThread();
                  while (i < 1000  && !interrupted()) {
                     synchronized (map) {
                        map.put(thread.getName() + "[" + ++i + "]", thread);
                     }
                  }
                  synchronized (map) {
                     for (final Iterator<Map.Entry<Object, Thread>> iter = map.entrySet().iterator(); iter.hasNext();) {
                        final Map.Entry<Object, Thread> entry = iter.next();
                        if (entry.getValue() == this) {
                           iter.remove();
                        }
                     }
                  }
               } catch (final InterruptedException e) {
                  fail("Unexpected InterruptedException");
               }
               if (i > 0) {
                  synchronized (counter) {
                     counter[0]++;
                  }
               }
            }

         };
      }

      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.start();
            thread.wait();
         }
      }

      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.notifyAll();
         }
      }

      Thread.sleep(1000);

      for (final Thread thread : threads) {
         thread.interrupt();
      }
      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.join();
         }
      }

      assertEquals("Exceptions have been thrown: " + exceptions, 0, exceptions.size());
      assertTrue("Each thread should have put at least 1 element into the map, but only "
         + counter[0] + " did succeed", counter[0] >= threads.length);
   }

   @Test
   public void testSynchronizedRemoveFromKeySet() throws InterruptedException {

      final Map<Object, Thread> map = new LRULinkedHashMap<>(10000);

      final Map<Throwable, String> exceptions = new HashMap<>();
      final ThreadGroup tg = new ThreadGroup(LRULinkedHashMapTest.class.getSimpleName()) {
         @Override
         public void uncaughtException(final Thread t, final Throwable e) {
            exceptions.put(e, t.getName());
            super.uncaughtException(t, e);
         }
      };

      final int[] counter = new int[1];
      counter[0] = 0;
      final Thread[] threads = new Thread[50];
      for (int i = 0; i < threads.length; ++i) {
         threads[i] = new Thread(tg, "JUnit Thread " + i) {

            @Override
            public void run() {
               int i = 0;
               try {
                  synchronized (this) {
                     notifyAll();
                     wait();
                  }
                  final Thread thread = Thread.currentThread();
                  while (i < 1000  && !interrupted()) {
                     synchronized (map) {
                        map.put(thread.getName() + "[" + ++i + "]", thread);
                     }
                  }
                  synchronized (map) {
                     for (final Iterator<Object> iter = map.keySet().iterator(); iter.hasNext();) {
                        final String name = (String) iter.next();
                        if (name.substring(0, name.indexOf('[')).equals(getName())) {
                           iter.remove();
                        }
                     }
                  }
               } catch (final InterruptedException e) {
                  fail("Unexpected InterruptedException");
               }
               if (i > 0) {
                  synchronized (counter) {
                     counter[0]++;
                  }
               }
            }

         };
      }

      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.start();
            thread.wait();
         }
      }

      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.notifyAll();
         }
      }

      Thread.sleep(1000);

      for (final Thread thread : threads) {
         thread.interrupt();
      }
      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.join();
         }
      }

      assertEquals("Exceptions have been thrown: " + exceptions, 0, exceptions.size());
      assertTrue("Each thread should have put at least 1 element into the map, but only "
         + counter[0] + " did succeed", counter[0] >= threads.length);
   }

   @Test
   public void testSynchronizedRemoveFromValues() throws InterruptedException {

      final Map<Object, Thread> map = new LinkedHashMap<>(10000);

      final Map<Throwable, String> exceptions = new HashMap<>();
      final ThreadGroup tg = new ThreadGroup(LRULinkedHashMapTest.class.getSimpleName()) {
         @Override
         public void uncaughtException(final Thread t, final Throwable e) {
            exceptions.put(e, t.getName());
            super.uncaughtException(t, e);
         }
      };

      final int[] counter = new int[1];
      counter[0] = 0;
      final Thread[] threads = new Thread[50];
      for (int i = 0; i < threads.length; ++i) {
         threads[i] = new Thread(tg, "JUnit Thread " + i) {

            @Override
            public void run() {
               int i = 0;
               try {
                  synchronized (this) {
                     notifyAll();
                     wait();
                  }
                  final Thread thread = Thread.currentThread();
                  while (i < 1000  && !interrupted()) {
                     synchronized (map) {
                        map.put(thread.getName() + "[" + ++i + "]", thread);
                     }
                  }
                  synchronized (map) {
                     for (final Iterator<Thread> iter = map.values().iterator(); iter.hasNext();) {
                        if (iter.next() == this) {
                           iter.remove();
                        }
                     }
                  }
               } catch (final InterruptedException e) {
                  fail("Unexpected InterruptedException");
               }
               if (i > 0) {
                  synchronized (counter) {
                     counter[0]++;
                  }
               }
            }

         };
      }

      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.start();
            thread.wait();
         }
      }

      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.notifyAll();
         }
      }

      Thread.sleep(1000);

      for (final Thread thread : threads) {
         thread.interrupt();
      }
      for (final Thread thread : threads) {
         synchronized (thread) {
            thread.join();
         }
      }

      assertEquals("Exceptions have been thrown: " + exceptions, 0, exceptions.size());
      assertTrue("Each thread should have put at least 1 element into the map, but only "
         + counter[0] + " did succeed", counter[0] >= threads.length);
   }

   /**
    *  Returns the set of keys in the mappings used to test the map.  This
    *  method must return an array with the same length as {@link
    *  #getSampleValues()} and all array elements must be different.
    */
   @SuppressWarnings("unchecked")
   private K[] getSampleKeys() {
      final Object[] result = new Object[] {
         "blah", "foo", "bar", "baz", "tmp", "gosh", "golly", "gee",
         "hello", "goodbye", "we'll", "see", "you", "all", "again",
         "key",
         "key2"
      };
      return (K[]) result;
   }

   /**
    * Returns the set of values in the mappings used to test the map.  This
    * method must return an array with the same length as
    * {@link #getSampleKeys()}.
    * <code>true</code>.
    */
   @SuppressWarnings("unchecked")
   private V[] getSampleValues() {
      final Object[] result = new Object[] {
         "blahv", "foov", "barv", "bazv", "tmpv", "goshv", "gollyv", "geev",
         "hellov", "goodbyev", "we'llv", "seev", "youv", "allv", "againv",
         "value", "value2",
      };
      return (V[]) result;
   }
}
