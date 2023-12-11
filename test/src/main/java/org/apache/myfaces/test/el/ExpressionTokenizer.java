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
package org.apache.myfaces.test.el;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParsePosition;
import java.util.ArrayList;

/**
 * Expression Tokenizer
 */
class ExpressionTokenizer
{
    private ExpressionTokenizer()
    {
    }

    public static String[] tokenize(CharSequence expr)
    {
        final ArrayList<String> tokens = new ArrayList<String>();
        ParsePosition pos = new ParsePosition(0);
        int len = expr.length();
        boolean sep = true;
        while (pos.getIndex() < len)
        {
            int here = pos.getIndex();
            char c = expr.charAt(here);
            switch (c)
            {
            case ' ':
                next(pos);
                break;
            case ']':
                throw new IllegalStateException("Position %s: unexpected '%s'".formatted(here, c));
            case '[':
                tokens.add(parseIndex(expr, next(pos)));
                break;
            case '.':
                if (sep)
                {
                    throw new IllegalStateException(
                            "Position %s: expected property, index/key, or end of expression".formatted(here));
                }
                sep = true;
                next(pos);
                // fall through:
            default:
                if (!sep)
                {
                    throw new IllegalStateException(
                            "Position %s: expected property path separator, index/key, or end of expression".formatted(here));
                }
                tokens.add(parseProperty(expr, pos));
            }
            sep = false;
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    private static ParsePosition next(ParsePosition pos)
    {
        pos.setIndex(pos.getIndex() + 1);
        return pos;
    }

    private static String parseProperty(CharSequence expr, ParsePosition pos)
    {
        int len = expr.length();
        int start = pos.getIndex();
        loop: while (pos.getIndex() < len)
        {
            switch (expr.charAt(pos.getIndex()))
            {
            case '[':
            case ']':
            case '.':
                break loop;
            default:
            }
            next(pos);
        }
        if (pos.getIndex() > start)
        {
            return expr.subSequence(start, pos.getIndex()).toString();
        }
        throw new IllegalStateException("Position %s: expected property".formatted(start));
    }

    /**
     * Handles an index/key. If the text contained between [] is surrounded by a
     * pair of " or ', these will be stripped.
     * 
     * @param expr Expression string to tokenize
     * @param pos current position of parser, will be updated by the method.
     * @return token found on the position
     */
    private static String parseIndex(CharSequence expr, ParsePosition pos)
    {
        int len = expr.length();
        int start = pos.getIndex();
        if (start < len)
        {
            char first = expr.charAt(pos.getIndex());
            if (first == '"' || first == '\'')
            {
                String s = parseQuotedString(expr, pos);
                if (s != null && expr.charAt(pos.getIndex()) == ']')
                {
                    next(pos);
                    return s;
                }
            }
            // no quoted string; match ] greedily and trim
            while (pos.getIndex() < len)
            {
                int here = pos.getIndex();
                try
                {
                    if (expr.charAt(here) == ']')
                    {
                        return expr.subSequence(start, here).toString().trim();
                    }
                }
                finally
                {
                    next(pos);
                }
            }
        }
        throw new IllegalStateException("Position %s: unparsable index".formatted(start));
    }

    private static String parseQuotedString(CharSequence expr, ParsePosition pos)
    {
        int len = expr.length();
        int start = pos.getIndex();
        if (start < len)
        {
            char quote = expr.charAt(start);
            next(pos);
            StringWriter w = new StringWriter();
            while (pos.getIndex() < len)
            {
                int here = pos.getIndex();
                char c = expr.charAt(here);
                boolean esc = false;
                if (c == '\\' && here + 1 < len && expr.charAt(here + 1) == quote)
                {
                    esc = true;
                    here = next(pos).getIndex();
                }
                try
                {
                    // look for matching quote
                    if (c == quote && !esc)
                    {
                        return w.toString();
                    }
                    w.write(Character.toChars(Character.codePointAt(expr, here)));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    next(pos);
                }
            }
            // if reached, reset due to no ending quote found
            pos.setIndex(start);
        }
        return null;
    }

}
