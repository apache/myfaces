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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class _AttachedListStateWrapperTest
{
    /*
     * Test method for 'jakarta.faces.component._AttachedListStateWrapper._AttachedListStateWrapper(List)'
     */
    @Test
    public void test_AttachedListStateWrapper()
    {
        List<Object> foo = new ArrayList<Object>();
        _AttachedListStateWrapper subject = new _AttachedListStateWrapper(foo);
        Assertions.assertNotNull(subject.getWrappedStateList());
        Assertions.assertTrue(subject.getWrappedStateList() == foo);
    }

    @Test
    public void testSerialize() throws Exception
    {
        String foo = "foo";
        List<Object> list = new ArrayList<Object>();
        list.add(foo);
        _AttachedListStateWrapper subject = new _AttachedListStateWrapper(list);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(subject);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        _AttachedListStateWrapper blorg = (_AttachedListStateWrapper) ois.readObject();
        Assertions.assertEquals(blorg.getWrappedStateList(), subject.getWrappedStateList());
        oos.close();
        ois.close();
    }

}
