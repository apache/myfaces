/*
 * Copyright 2011 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.renderkit.html.util;

import java.io.StringWriter;
import org.junit.jupiter.api.Assertions;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Test;

public class UnicodeEncoderTest extends AbstractJsfTestCase
{

    @Test
    public void testUnicodeEncoder1() throws Exception
    {
        StringWriter sw = new StringWriter(40);
        UnicodeEncoder.encode(sw, ""+(char)0xE1);
        Assertions.assertEquals(UnicodeEncoder.encode(""+(char)0xE1), sw.toString());
    }
    
    @Test
    public void testUnicodeEncoder2() throws Exception
    {
        StringWriter sw = new StringWriter(40);
        UnicodeEncoder.encode(sw, "h"+(char)0xE1);
        Assertions.assertEquals(UnicodeEncoder.encode("h"+(char)0xE1), sw.toString());
    }
    
    @Test
    public void testUnicodeEncoder3() throws Exception
    {
        StringWriter sw = new StringWriter(40);
        UnicodeEncoder.encode(sw, ""+(char)0xE1+"a");
        Assertions.assertEquals(UnicodeEncoder.encode(""+(char)0xE1)+"a", sw.toString());
    }
    
    @Test
    public void testUnicodeEncoder4() throws Exception
    {
        StringWriter sw = new StringWriter(40);
        UnicodeEncoder.encode(sw, "hello h"+(char)0xE1+"aaa <p></p>");
        Assertions.assertEquals("hello h&#225;aaa <p></p>", sw.toString());
    }

}
