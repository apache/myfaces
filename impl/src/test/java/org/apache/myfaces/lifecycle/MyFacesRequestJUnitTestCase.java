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
package org.apache.myfaces.lifecycle;

import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class test 
 */
public class MyFacesRequestJUnitTestCase extends AbstractMyFacesRequestTestCase
{
    @Test
    public void testRedirect1() throws Exception
    {
        startViewRequest("/redirect1.xhtml");
        processLifecycleExecute();
        renderResponse();
        client.submit("mainForm:submit");
        processLifecycleExecuteAndRender();
        client.processRedirect();
        processLifecycleExecuteAndRender();
        String redirectedContent = getRenderedContent();
        Assert.assertTrue(redirectedContent.contains("Redirected Page"));
    }
}
