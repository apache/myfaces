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
 * There are unicodes outside the ranges defined in the
 * <a href="https://www.w3.org/TR/REC-xml/#charsets">XML 1.0 specification</a> that break XML parsers
 * and therefore must be filtered out when writing partial responses. Otherwise this may lead to
 * Denial of Service attacks.
 * @see https://issues.apache.org/jira/browse/MYFACES-4266
 */
public class IllegalXmlCharacterFilterWriter extends FilterWriter
{
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private static final char BLANK_CHAR = ' ';
    
    public IllegalXmlCharacterFilterWriter(Writer out)
    {
        super(out);
    }

    @Override
    public void write(int c) throws IOException 
    {
        if (isInvalidChar((char) c))
        {
            super.write((int) BLANK_CHAR);
        }
        else
        {
            super.write(c);
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException 
    {
        super.write(encodeCharArray(cbuf, off, len), off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException 
    {
        super.write(encodeString(str, off, len), off, len);
    }

    private static String encodeString(String str, int off, int len)
    {
        boolean containsInvalidChar = false;
        char[] encodedCharArray = EMPTY_CHAR_ARRAY;
        
        int to = off + len;
        for (int i = off; i < to; i++)
        {
            if (isInvalidChar(str.charAt(i)))
            {
                if (!containsInvalidChar)
                {
                    containsInvalidChar = true;
                    encodedCharArray = str.toCharArray();
                }
                encodedCharArray[i] = BLANK_CHAR;
            }
        }

        if (containsInvalidChar)
        {
            return String.valueOf(encodedCharArray);
        }

        return str;
    }
    
    private static char[] encodeCharArray(char[] cbuf, int off, int len)
    {
        int to = off + len;
        for (int i = off; i < to; i++)
        {
            if (isInvalidChar(cbuf[i]))
            {
                cbuf[i] = BLANK_CHAR;
            }
        }
        return cbuf;
    }

    private static boolean isInvalidChar(char c)
    {
        if (Character.isSurrogate(c)) 
        {
            return true;
        }
        if (c == '\u0009' || c == '\n' || c == '\r') 
        {
            return false;
        }
        if (c > '\u0020' && c < '\uD7FF') 
        {
            return false;
        }
        if (c > '\uE000' && c < '\uFFFD') 
        {
            return false;
        }
        return true;
    }
}
