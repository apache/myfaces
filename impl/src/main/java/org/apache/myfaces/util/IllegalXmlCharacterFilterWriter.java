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
import org.apache.myfaces.shared.util.ClassUtils;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

/**
 * There are unicodes outside the ranges defined in the
 * <a href="https://www.w3.org/TR/REC-xml/#charsets">XML 1.0 specification</a> that break XML parsers
 * and therefore must be filtered out when writing partial responses. Otherwise this may lead to
 * Denial of Service attacks.
 * @see https://issues.apache.org/jira/browse/MYFACES-4266
 */
public class IllegalXmlCharacterFilterWriter extends FilterWriter
{
    private static final char BLANK_CHAR = ' ';

    // Java 1.6 doesnt support Character.isSurrogate
    private static boolean java16;
    
    static 
    {
        try
        {
            java16 = ClassUtils.classForName("java.util.Objects") == null;
        }
        catch (Throwable e)
        {
            java16 = true;
        }

        if (java16)
        {
            System.err.println("Skip using " + IllegalXmlCharacterFilterWriter.class.getName()
                    + ", it's only available in Java 1.6+");
        }
    }

    public IllegalXmlCharacterFilterWriter(Writer out)
    {
        super(out);
    }

    @Override
    public void write(int c) throws IOException 
    {
        if (java16)
        {
            super.write(c);
            return;
        }

        if (isInvalidChar(c))
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
        if (java16)
        {
            super.write(cbuf, off, len);
            return;
        }

        super.write(encodeCharArray(cbuf, off, len), off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException 
    {
        if (java16)
        {
            super.write(str, off, len);
            return;
        }

        super.write(encodeString(str, off, len), off, len);
    }

    @IgnoreJRERequirement // Java 1.6 doesnt support Character.isSurrogate
    private static String encodeString(String str, int off, int len)
    {
        if (str == null)
        {
            return null;
        }
        
        int to = off + len;
        boolean surrogateResolved = false;
        char c;
        int codePoint;
        char cNext;
        
        char[] encoded = null;
        for (int i = off; i < to; i++)
        {
            if (surrogateResolved == true)
            {
                surrogateResolved = false;
                continue;
            }
            
            c = str.charAt(i);
            codePoint = c;

            if (Character.isHighSurrogate(c) && i + 1 < to)
            {
                cNext = str.charAt(i + 1);
                if (Character.isLowSurrogate(cNext))
                {
                    codePoint = Character.toCodePoint(c, cNext);
                    surrogateResolved = true;
                }
            }

            // try to resolve surrogate, this is required e.g. for emojis
            if ((!surrogateResolved && Character.isSurrogate(c)) || isInvalidChar(codePoint))
            {
                if (encoded == null)
                {
                    encoded = str.toCharArray();
                }
                encoded[i] = BLANK_CHAR;
                
                if (surrogateResolved)
                {
                    encoded[i + 1] = BLANK_CHAR;
                }
            }
        }

        if (encoded != null)
        {
            return String.valueOf(encoded);
        }

        return str;
    }

    @IgnoreJRERequirement // Java 1.6 doesnt support Character.isSurrogate
    private static char[] encodeCharArray(char[] cbuf, int off, int len)
    {
        if (cbuf == null)
        {
            return null;
        }

        int to = off + len;
        
        boolean surrogateResolved = false;
        char c;
        int codePoint;
        char cNext;

        for (int i = off; i < to; i++)
        {
            if (surrogateResolved == true)
            {
                surrogateResolved = false;
                continue;
            }
            
            c = cbuf[i];
            codePoint = c;

            // try to resolve surrogate, this is required e.g. for emojis
            if (Character.isHighSurrogate(c) && i + 1 < to)
            {
                cNext = cbuf[i + 1];
                if (Character.isLowSurrogate(cNext))
                {
                    codePoint = Character.toCodePoint(c, cNext);
                    surrogateResolved = true;
                }
            }
            
            if ((!surrogateResolved && Character.isSurrogate(c)) || isInvalidChar(codePoint))
            {
                cbuf[i] = BLANK_CHAR;
                
                if (surrogateResolved)
                {
                    cbuf[i + 1] = BLANK_CHAR;
                }
            }
        }
        return cbuf;
    }

    private static boolean isInvalidChar(int codePoint)
    {
        if (codePoint == 1113088)
        {
            return true;
        }

        if (codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD) 
        {
            return false;
        }
        if (codePoint >= 0x20 && codePoint <= 0xD7FF) 
        {
            return false;
        }
        if (codePoint >= 0xE000 && codePoint <= 0xFFFD) 
        {
            return false;
        }
        if (codePoint >= 0x10000 && codePoint <= 0x10FFFF) 
        {
            return false;
        }

        return true;
    }
}
