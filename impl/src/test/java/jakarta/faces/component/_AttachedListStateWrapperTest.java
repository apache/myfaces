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

import jakarta.faces.component._AttachedListStateWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Assert;

public class _AttachedListStateWrapperTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(_AttachedListStateWrapperTest.class);
    }

    public _AttachedListStateWrapperTest(String name)
    {
        super(name);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /*
     * Test method for 'jakarta.faces.component._AttachedListStateWrapper._AttachedListStateWrapper(List)'
     */
    public void test_AttachedListStateWrapper()
    {
        List<Object> foo = new ArrayList<Object>();
        _AttachedListStateWrapper subject = new _AttachedListStateWrapper(foo);
        Assert.assertNotNull(subject.getWrappedStateList());
        Assert.assertTrue(subject.getWrappedStateList() == foo);
    }

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
        Assert.assertEquals(blorg.getWrappedStateList(), subject.getWrappedStateList());
        oos.close();
        ois.close();
    }

}
