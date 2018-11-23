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
package org.apache.myfaces.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * There are unicodes outside the ranges defined in the <a href="https://www.w3.org/TR/REC-xml/#charsets">XML 1.0 specification</a> that break XML parsers
 * and therefore must be filtered out when writing partial responses. Otherwise this may lead to Denial of Service attacks.
 * @see https://issues.apache.org/jira/browse/MYFACES-4266
 */
public class IllegalXmlCharacterFilterWriter extends FilterWriter
{
    public IllegalXmlCharacterFilterWriter(Writer out)
    {
        super(out);
    }

    @Override
    public void write(int c) throws IOException 
    {
        super.write(xmlEncode((char) c));
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException 
    {
        super.write(xmlEncode(cbuf), off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException 
    {
        super.write(xmlEncode(str.toCharArray()), off, len);
    }

    private char[] xmlEncode(char[] ca)
    {
        for (int i = 0; i < ca.length; i++)
        {
            ca[i] = xmlEncode(ca[i]);
        }
        return ca;
    }

    private char xmlEncode(char c)
    {
        if (Character.isSurrogate(c)) 
        {
            return ' ';
        }
        if (c == '\u0009' || c == '\n' || c == '\r') 
        {
            return c;
        }
        if (c > '\u0020' && c < '\uD7FF') 
        {
            return c;
        }
        if (c > '\uE000' && c < '\uFFFD') 
        {
            return c;
        }
        return ' ';
    }
}
