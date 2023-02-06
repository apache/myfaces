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
package org.apache.myfaces.view.facelets.tag.faces.html;

import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SelectOneItemGroupTest extends AbstractMyFacesCDIRequestTestCase
{
    @Test
    public void testSelectOneItemGroup() throws Exception
    {
        startViewRequest("/selectOneItemGroup.xhtml");
        processLifecycleExecuteAndRender();
        
        String content = getRenderedContent();
        Assertions.assertTrue(content.contains("Dog"));
        Assertions.assertTrue(content.contains("Cat"));
        Assertions.assertTrue(content.contains("Fish"));
        Assertions.assertTrue(content.contains("Kibble"));

        endRequest();
    }
}
