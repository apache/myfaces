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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class _AttachedStateWrapperTest
{
    /*
     * Test method for 'jakarta.faces.component._AttachedStateWrapper._AttachedStateWrapper(Class, Object)'
     */
    @Test
    void attached_state_wrapper()
    {
        _AttachedStateWrapper subject = new _AttachedStateWrapper(null, null);
        assertNull(subject.getWrappedStateObject());
        assertNull(subject.getClazz());
    }

    /*
     * Test method for 'jakarta.faces.component._AttachedStateWrapper.getClazz()'
     */
    @Test
    void getClazz()
    {
        _AttachedStateWrapper subject = new _AttachedStateWrapper(String.class, "foo");
        assertEquals(String.class, subject.getClazz());
    }

    /*
     * Test method for 'jakarta.faces.component._AttachedStateWrapper.getWrappedStateObject()'
     */
    @Test
    void getWrappedStateObject()
    {
        _AttachedStateWrapper subject = new _AttachedStateWrapper(String.class, "foo");
        assertEquals(String.class, subject.getClazz());
    }

    @Test
    void serialize() throws Exception
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
        assertEquals(blorg.getWrappedStateObject(), subject.getWrappedStateObject());
        oos.close();
        ois.close();
    }
}
