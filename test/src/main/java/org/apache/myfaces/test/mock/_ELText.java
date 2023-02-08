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
package org.apache.myfaces.test.mock;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.context.ResponseWriter;

/**
 * Handles parsing EL Strings in accordance with the EL-API Specification. The parser accepts either <code>${..}</code>
 * or <code>#{..}</code>.
 * 
 * @author Jacob Hookom
 * @version $Id: ELText.java,v 1.8 2008/07/13 19:01:42 rlubke Exp $
 */
class _ELText
{

    private static final class LiteralValueExpression extends ValueExpression
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private final String text;

        public LiteralValueExpression(String text)
        {
            this.text = text;
        }

        public boolean isLiteralText()
        {
            return false;
        }

        public int hashCode()
        {
            return 0;
        }

        public String getExpressionString()
        {
            return this.text;
        }

        public boolean equals(Object obj)
        {
            return false;
        }

        public void setValue(ELContext context, Object value)
        {
        }

        public boolean isReadOnly(ELContext context)
        {
            return false;
        }

        public Object getValue(ELContext context)
        {
            return null;
        }

        public Class<?> getType(ELContext context)
        {
            return null;
        }

        public Class<?> getExpectedType()
        {
            return null;
        }

    }

    private static final class ELTextComposite extends _ELText
    {
        private final _ELText[] txt;

        public ELTextComposite(_ELText[] txt)
        {
            super(null);
            this.txt = txt;
        }

        public void write(Writer out, ELContext ctx) throws ELException, IOException
        {
            for (int i = 0; i < this.txt.length; i++)
            {
                this.txt[i].write(out, ctx);
            }
        }

        public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException
        {
            for (int i = 0; i < this.txt.length; i++)
            {
                this.txt[i].writeText(out, ctx);
            }
        }

        public String toString(ELContext ctx)
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < this.txt.length; i++)
            {
                sb.append(this.txt[i].toString(ctx));
            }
            return sb.toString();
        }

        /*
         * public String toString(ELContext ctx) { StringBuffer sb = new StringBuffer(); for (int i = 0; i <
         * this.txt.length; i++) { sb.append(this.txt[i].toString(ctx)); } return sb.toString(); }
         */

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < this.txt.length; i++)
            {
                sb.append(this.txt[i].toString());
            }
            return sb.toString();
        }

        public boolean isLiteral()
        {
            return false;
        }

        public _ELText apply(ExpressionFactory factory, ELContext ctx)
        {
            int len = this.txt.length;
            _ELText[] nt = new _ELText[len];
            for (int i = 0; i < len; i++)
            {
                nt[i] = this.txt[i].apply(factory, ctx);
            }
            return new ELTextComposite(nt);
        }
    }

    private static final class ELTextVariable extends _ELText
    {
        private final ValueExpression ve;

        public ELTextVariable(ValueExpression ve)
        {
            super(ve.getExpressionString());
            this.ve = ve;
        }

        public boolean isLiteral()
        {
            return false;
        }

        public _ELText apply(ExpressionFactory factory, ELContext ctx)
        {
            return new ELTextVariable(factory.createValueExpression(ctx, this.ve.getExpressionString(), String.class));
        }

        public void write(Writer out, ELContext ctx) throws ELException, IOException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                out.write((String) v);
            }
        }

        public String toString(ELContext ctx) throws ELException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                return v.toString();
            }

            return null;
        }

        public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                out.writeText(v, null);
            }
        }
    }

    protected final String literal;

    public _ELText(String literal)
    {
        this.literal = literal;
    }

    /**
     * If it's literal text
     * 
     * @return true if the String is literal (doesn't contain <code>#{..}</code> or <code>${..}</code>)
     */
    public boolean isLiteral()
    {
        return true;
    }

    /**
     * Return an instance of <code>this</code> that is applicable given the ELContext and ExpressionFactory state.
     * 
     * @param factory
     *            the ExpressionFactory to use
     * @param ctx
     *            the ELContext to use
     * @return an ELText instance
     */
    public _ELText apply(ExpressionFactory factory, ELContext ctx)
    {
        return this;
    }

    /**
     * Allow this instance to write to the passed Writer, given the ELContext state
     * 
     * @param out
     *            Writer to write to
     * @param ctx
     *            current ELContext state
     * @throws ELException
     * @throws IOException
     */
    public void write(Writer out, ELContext ctx) throws ELException, IOException
    {
        out.write(this.literal);
    }

    public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException
    {
        out.writeText(this.literal, null);
    }

    /**
     * Evaluates the ELText to a String
     * 
     * @param ctx
     *            current ELContext state
     * @throws ELException
     * @return the evaluated String
     */
    public String toString(ELContext ctx) throws ELException
    {
        return this.literal;
    }

    public String toString()
    {
        return this.literal;
    }

    /**
     * Parses the passed string to determine if it's literal or not
     * 
     * @param in
     *            input String
     * @return true if the String is literal (doesn't contain <code>#{..}</code> or <code>${..}</code>)
     */
    public static boolean isLiteral(String in)
    {
        _ELText txt = parse(in);
        return txt == null || txt.isLiteral();
    }

    /**
     * Factory method for creating an unvalidated ELText instance. NOTE: All expressions in the passed String are
     * treated as {@link org.apache.myfaces.view.facelets.el.LiteralValueExpression LiteralValueExpressions}.
     * 
     * @param in
     *            String to parse
     * @return ELText instance that knows if the String was literal or not
     * @throws jakarta.el.ELException
     */
    public static _ELText parse(String in) throws ELException
    {
        return parse(null, null, in);
    }

    /**
     * Factory method for creating a validated ELText instance. When an Expression is hit, it will use the
     * ExpressionFactory to create a ValueExpression instance, resolving any functions at that time. <p/> Variables and
     * properties will not be evaluated.
     * 
     * @param fact
     *            ExpressionFactory to use
     * @param ctx
     *            ELContext to validate against
     * @param in
     *            String to parse
     * @return ELText that can be re-applied later
     * @throws jakarta.el.ELException
     */
    public static _ELText parse(ExpressionFactory fact, ELContext ctx, String in) throws ELException
    {
        char[] ca = in.toCharArray();
        int i = 0;
        char c = 0;
        int len = ca.length;
        int end = len - 1;
        boolean esc = false;
        int vlen = 0;

        StringBuffer buff = new StringBuffer(128);
        List<_ELText> text = new ArrayList<_ELText>();
        _ELText t = null;
        ValueExpression ve = null;

        while (i < len)
        {
            c = ca[i];
            if ('\\' == c)
            {
                esc = !esc;
                if (esc && i < end && (ca[i + 1] == '$' || ca[i + 1] == '#'))
                {
                    i++;
                    continue;
                }
            }
            else if (!esc && ('$' == c || '#' == c))
            {
                if (i < end)
                {
                    if ('{' == ca[i + 1])
                    {
                        if (buff.length() > 0)
                        {
                            text.add(new _ELText(buff.toString()));
                            buff.setLength(0);
                        }
                        vlen = findVarLength(ca, i);
                        if (ctx != null && fact != null)
                        {
                            ve = fact.createValueExpression(ctx, new String(ca, i, vlen), String.class);
                            t = new ELTextVariable(ve);
                        }
                        else
                        {
                            t = new ELTextVariable(new LiteralValueExpression(new String(ca, i, vlen)));
                        }
                        text.add(t);
                        i += vlen;
                        continue;
                    }
                }
            }
            esc = false;
            buff.append(c);
            i++;
        }

        if (buff.length() > 0)
        {
            text.add(new _ELText(buff.toString()));
            buff.setLength(0);
        }

        if (text.size() == 0)
        {
            return null;
        }
        else if (text.size() == 1)
        {
            return text.get(0);
        }
        else
        {
            _ELText[] ta = text.toArray(new _ELText[text.size()]);
            return new ELTextComposite(ta);
        }
    }

    private static int findVarLength(char[] ca, int s) throws ELException
    {
        int i = s;
        int len = ca.length;
        char c = 0;
        int str = 0;
        while (i < len)
        {
            c = ca[i];
            if ('\\' == c && i < len - 1)
            {
                i++;
            }
            else if ('\'' == c || '"' == c)
            {
                if (str == c)
                {
                    str = 0;
                }
                else
                {
                    str = c;
                }
            }
            else if (str == 0 && ('}' == c))
            {
                return i - s + 1;
            }
            i++;
        }
        throw new ELException("EL Expression Unbalanced: ... " + new String(ca, s, i - s));
    }

}
