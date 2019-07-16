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
package org.apache.myfaces.shared.resource;

import org.apache.myfaces.resource.ResourceLoaderUtils;
import org.junit.Assert;
import org.junit.Test;

public class ResourceLoaderUtilsTest {
    
    @Test
    public void testFormatDateHeader()
    {
        Assert.assertEquals("Tue, 16 Jul 2019 08:10:19 GMT",
                ResourceLoaderUtils.formatDateHeader(1563264619000L));
        
        Assert.assertEquals("Tue, 16 Jul 2019 08:29:38 GMT",
                ResourceLoaderUtils.formatDateHeader(1563265778653L));
    }
    
    @Test
    public void testParseDateHeader()
    {
        Assert.assertEquals(1563264619000L,
                ResourceLoaderUtils.parseDateHeader("Tue, 16 Jul 2019 08:10:19 GMT"),
                2000);
        
        Assert.assertEquals(1563265778653L,
                ResourceLoaderUtils.parseDateHeader("Tue, 16 Jul 2019 08:29:38 GMT"),
                2000);
    }
}
