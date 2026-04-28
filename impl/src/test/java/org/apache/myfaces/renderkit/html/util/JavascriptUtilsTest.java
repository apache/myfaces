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
package org.apache.myfaces.renderkit.html.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavascriptUtilsTest
{
    @Test
    public void testGetValidJavascriptNameTest_SimpleIdentifierUnchanged()
    {
        Assertions.assertEquals("fooBar123",
                JavascriptUtils.getValidJavascriptName("fooBar123", false));
        Assertions.assertEquals("fooBar123",
                JavascriptUtils.getValidJavascriptName("fooBar123", true, false));
    }

    @Test
    public void testGetValidJavascriptNameTest_ReservedWordAppendsUnderscoreWhenChecking()
    {
        Assertions.assertEquals("var_",
                JavascriptUtils.getValidJavascriptName("var", true));
        Assertions.assertEquals("var_",
                JavascriptUtils.getValidJavascriptName("var", true, true));
    }

    @Test
    public void testGetValidJavascriptNameTest_ReservedWordUnchangedWhenNotChecking()
    {
        Assertions.assertEquals("var",
                JavascriptUtils.getValidJavascriptName("var", false));
    }

    @Test
    public void testGetValidJavascriptNameTest_InvalidCharactersBecomeUnderscorePlusHex()
    {
        Assertions.assertEquals("a_2Db",
                JavascriptUtils.getValidJavascriptName("a-b", false));
        Assertions.assertEquals("x_2Ey",
                JavascriptUtils.getValidJavascriptName("x.y", false));
    }

    @Test
    public void testGetValidJavascriptNameTest_AllowNamespacesSplitsSegments()
    {
        Assertions.assertEquals("a.b",
                JavascriptUtils.getValidJavascriptName("a.b", true, false));
        Assertions.assertEquals("a_3Ab.c",
                JavascriptUtils.getValidJavascriptName("a:b.c", true, false));
    }

    @Test
    public void testGetValidJavascriptNameTest_AllowNamespacesFalseTreatsDotsAsInvalid()
    {
        Assertions.assertEquals("a_2Eb",
                JavascriptUtils.getValidJavascriptName("a.b", false, false));
    }

    @Test
    public void testGetValidJavascriptNameTest_EmptyString()
    {
        Assertions.assertEquals("",
                JavascriptUtils.getValidJavascriptName("", false));
    }
}
