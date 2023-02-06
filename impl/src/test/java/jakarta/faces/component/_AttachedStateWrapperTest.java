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

package jakarta.faces.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class _AttachedStateWrapperTest
{
    /*
     * Test method for 'jakarta.faces.component._AttachedStateWrapper._AttachedStateWrapper(Class, Object)'
     */
    @Test
    public void test_AttachedStateWrapper()
    {
        _AttachedStateWrapper subject = new _AttachedStateWrapper(null, null);
        Assert.assertNull(subject.getWrappedStateObject());
        Assert.assertNull(subject.getClazz());
    }

    /*
     * Test method for 'jakarta.faces.component._AttachedStateWrapper.getClazz()'
     */
    @Test
    public void testGetClazz()
    {
        _AttachedStateWrapper subject = new _AttachedStateWrapper(String.class, "foo");
        Assert.assertEquals(subject.getClazz(), String.class);
    }

    /*
     * Test method for 'jakarta.faces.component._AttachedStateWrapper.getWrappedStateObject()'
     */
    @Test
    public void testGetWrappedStateObject()
    {
        _AttachedStateWrapper subject = new _AttachedStateWrapper(String.class, "foo");
        Assert.assertEquals(subject.getClazz(), String.class);
    }

    @Test
    public void testSerialize() throws Exception
    {
        String foo = "foo";
        _AttachedStateWrapper subject = new _AttachedStateWrapper(String.class, foo);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(subject);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        _AttachedStateWrapper blorg = (_AttachedStateWrapper) ois.readObject();
        Assert.assertEquals(blorg.getWrappedStateObject(), subject.getWrappedStateObject());
        oos.close();
        ois.close();
    }
}
