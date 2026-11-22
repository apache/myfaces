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
package org.apache.myfaces.view.facelets.tag;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import jakarta.faces.view.facelets.TagAttribute;
import org.junit.jupiter.api.Test;

class TagAttributesImplTest
{
    @Test
    void namespaceMapping()
    {
        TagAttributeImpl test1 = new TagAttributeImpl(null, "", "test1", null, "");
        TagAttributeImpl test2 = new TagAttributeImpl(null, "", "test2", null, "");
        TagAttributeImpl testTest1 = new TagAttributeImpl(null, "test", "test1", null, "");
        
        TagAttributesImpl impl = new TagAttributesImpl(new TagAttribute[] { test1, test2, testTest1 });
        assertEquals(test1, impl.get("test1"));
        assertEquals(test2, impl.get("test2"));
        
        assertEquals(testTest1, impl.get("test", "test1"));

        
        assertEquals(3, Arrays.asList(impl.getAll()).size());
        assertTrue(Arrays.asList(impl.getAll()).contains(test1));
        assertTrue(Arrays.asList(impl.getAll()).contains(test2));
        assertTrue(Arrays.asList(impl.getAll()).contains(testTest1));
        
        assertEquals(2, Arrays.asList(impl.getAll("")).size());
        assertTrue(Arrays.asList(impl.getAll()).contains(test1));
        assertTrue(Arrays.asList(impl.getAll()).contains(test2));
    }

    @Test
    void notAvailable()
    {
        TagAttributesImpl impl = new TagAttributesImpl(new TagAttribute[] { });
        assertNull(impl.get("test1"));
        assertNull(impl.get("test", "test2"));
    }
}
