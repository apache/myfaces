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
package org.apache.myfaces.view.facelets.el;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.view.Location;
import org.apache.myfaces.resource.ResourceELUtils;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;

/**
 * Handles parsing EL Strings in accordance with the EL-API Specification.
 * The parser accepts either <code>${..}</code>
 * or <code>#{..}</code>.
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public class ELText
{

    protected static final class LiteralValueExpression extends ValueExpression
    {
        private static final long serialVersionUID = 1L;

        private final String text;

        public LiteralValueExpression(String text)
        {
            this.text = text;
        }

        @Override
        public boolean isLiteralText()
        {
            return false;
        }

        @Override
        public int hashCode()
        {
            return 0;
        }

        @Override
        public String getExpressionString()
        {
            return this.text;
        }

        @Override
        public boolean equals(Object obj)
        {
            return false;
        }

        @Override
        public void setValue(ELContext context, Object value)
        {
        }

        @Override
        public boolean isReadOnly(ELContext context)
        {
            return false;
        }

        @Override
        public Object getValue(ELContext context)
        {
            return null;
        }

        @Override
        public Class<?> getType(ELContext context)
        {
            return null;
        }

        @Override
        public Class<?> getExpectedType()
        {
            return null;
        }
    }

    protected static final class ELTextComposite extends ELText
    {
        private final ELText[] txt;

        public ELTextComposite(ELText[] txt)
        {
            super(null);
            this.txt = txt;
        }

        @Override
        public void write(Writer out, ELContext ctx) throws ELException, IOException
        {
            for (int i = 0; i < this.txt.length; i++)
            {
                this.txt[i].write(out, ctx);
            }
        }

        @Override
        public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException
        {
            for (int i = 0; i < this.txt.length; i++)
            {
                this.txt[i].writeText(out, ctx);
            }
        }

        @Override
        public String toString(ELContext ctx)
        {
            StringBuilder sb = new StringBuilder();
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
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.txt.length; i++)
            {
                sb.append(this.txt[i].toString());
            }
            return sb.toString();
        }

        @Override
        public boolean isLiteral()
        {
            return false;
        }

        @Override
        public ELText apply(ExpressionFactory factory, ELContext ctx)
        {
            int len = this.txt.length;
            ELText[] nt = new ELText[len];
            for (int i = 0; i < len; i++)
            {
                nt[i] = this.txt[i].apply(factory, ctx);
            }
            return new ELTextComposite(nt);
        }

        public ELText[] getElements()
        {
            return this.txt;
        }
    }

    protected static final class ELTextVariable extends ELText
    {
        private final ValueExpression ve;

        public ELTextVariable(ValueExpression ve)
        {
            super(ve.getExpressionString());
            this.ve = ve;
        }

        @Override
        public boolean isLiteral()
        {
            return false;
        }

        @Override
        public ELText apply(ExpressionFactory factory, ELContext ctx)
        {
            return new ELTextVariable(factory.createValueExpression(ctx, this.ve.getExpressionString(), String.class));
        }

        @Override
        public void write(Writer out, ELContext ctx) throws ELException, IOException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                out.write((String) v);
            }
        }

        @Override
        public String toString(ELContext ctx) throws ELException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                return v.toString();
            }

            return null;
        }

        @Override
        public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                out.writeText(v, null);
            }
        }
    }
    
    protected static final class ELCacheableTextVariable extends ELText
    {
        //Just like TagAttributeImpl
        private final static int EL_CC = 2;
        private final static int EL_RESOURCE = 8;
        
        private final ValueExpression ve;
        private final int capabilities;
        private volatile ELTextVariable cached;
        
        public ELCacheableTextVariable(ValueExpression ve)
        {
            super(ve.getExpressionString());
            this.ve = ve;
            boolean compositeComponentExpression
                    = CompositeComponentELUtils.isCompositeComponentExpression(ve.getExpressionString());
            boolean resourceExpression = ResourceELUtils.isResourceExpression(ve.getExpressionString());
            this.capabilities = (compositeComponentExpression ? EL_CC : 0) | ( resourceExpression ? EL_RESOURCE : 0);
        }

        @Override
        public boolean isLiteral()
        {
            return false;
        }

        @Override
        public ELText apply(ExpressionFactory factory, ELContext ctx)
        {
            AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
            
            if (actx.isAllowCacheELExpressions() && cached != null)
            {
                // In TagAttributeImpl.getValueExpression(), it is necessary to do an
                // special logic to detect the cases where #{cc} is included into the
                // EL expression and set the proper ccLevel. In this case, it is usual
                // the parent composite component is always on top, but it is possible to
                // write a nesting case with <composite:insertChildren>, and
                // pass a flat EL expression over itself. So, it is necessary to update
                // the ccLevel to make possible to find the right parent where this 
                // expression belongs to.
                if ((this.capabilities & EL_CC) != 0)
                {
                    UIComponent cc = actx.getFaceletCompositionContext().getCompositeComponentFromStack();
                    if (cc != null)
                    {
                        Location location = (Location) cc.getAttributes().get(CompositeComponentELUtils.LOCATION_KEY);
                        if (location != null)
                        {
                            return new ELTextVariable(((LocationValueExpression) cached.ve).apply(
                                    actx.getFaceletCompositionContext().getCompositeComponentLevel(), location));
                        }
                    }
                    return new ELTextVariable(((LocationValueExpression) cached.ve).apply(
                            actx.getFaceletCompositionContext().getCompositeComponentLevel()));
                }
                return cached;
            }
            
            actx.beforeConstructELExpression();
            try
            {
                ValueExpression valueExpression
                        = factory.createValueExpression(ctx, this.ve.getExpressionString(), String.class);
              
                if (this.ve instanceof ContextAwareTagValueExpression)
                {
                    valueExpression = new ContextAwareTagValueExpression(
                            ((ContextAwareTagValueExpression) this.ve).getLocation(),
                            "expression",
                            valueExpression);
                }

                if ((this.capabilities & EL_CC) != 0)
                {
                    UIComponent cc = actx.getFaceletCompositionContext().getCompositeComponentFromStack();
                    if (cc != null)
                    {
                        Location location = (Location) cc.getAttributes().get(CompositeComponentELUtils.LOCATION_KEY);
                        if (location != null)
                        {
                            valueExpression = new LocationValueExpression(location, valueExpression,
                                    actx.getFaceletCompositionContext().getCompositeComponentLevel());
                        }
                    }
                }
                else if ((this.capabilities & EL_RESOURCE) != 0)
                {
                    UIComponent cc = actx.getFaceletCompositionContext().getCompositeComponentFromStack();
                    if (cc != null)
                    {
                        Location location = (Location) cc.getAttributes().get(CompositeComponentELUtils.LOCATION_KEY);
                        if (location != null)
                        {
                            valueExpression = new ResourceLocationValueExpression(location, valueExpression);
                        }
                    }
                }
                
                ELTextVariable eltv = new ELTextVariable(valueExpression);
                
                if (actx.isAllowCacheELExpressions() && !actx.isAnyFaceletsVariableResolved())
                {
                     cached = eltv;
                }
                return eltv;
            }
            finally
            {
                actx.afterConstructELExpression();
            }
        }

        @Override
        public void write(Writer out, ELContext ctx) throws ELException, IOException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                out.write((String) v);
            }
        }

        @Override
        public String toString(ELContext ctx) throws ELException
        {
            Object v = this.ve.getValue(ctx);
            if (v != null)
            {
                return v.toString();
            }

            return null;
        }

        @Override
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

    public ELText(String literal)
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
    public ELText apply(ExpressionFactory factory, ELContext ctx)
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

    @Override
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
        return isLiteral(null, null, in);
    }

    public static ELText parse(String in) throws ELException
    {
        return parse(null, null, in, null);
    }

    /**
     * Factory method for creating an unvalidated ELText instance. NOTE: All expressions in the passed String are
     * treated as {@link org.apache.myfaces.view.facelets.el.LocationValueExpression}.
     * 
     * @param in
     *            String to parse
     * @param location
     *            The location
     * @return ELText instance that knows if the String was literal or not
     * @throws jakarta.el.ELException
     */
    public static ELText parse(String in, Location location) throws ELException
    {
        return parse(null, null, in, location);
    }
    
    public static ELText parseAllowEmptyString(String in, Location location) throws ELException
    {
        if (in != null && in.length() == 0)
        {
            return new ELText(in);
        }
        else
        {
            return parse(null, null, in, location);
        }
    }

    public static String parseAsString(ExpressionFactory fact, ELContext ctx, String in) throws ELException
    {
        if (isLiteral(fact, ctx, in))
        {
            return in;
        }

        return parse(fact, ctx, in, null).toString(ctx);
    }

    public static ELText parse(ExpressionFactory fact, ELContext ctx, String in) throws ELException
    {
        return parse(fact, ctx, in, null);
    }

    /**
     * Factory method for creating a validated ELText instance. When an Expression is hit, it will use the
     * ExpressionFactory to create a ValueExpression instance, resolving any functions at that time. <p> Variables and
     * properties will not be evaluated.</p>
     * 
     * @param fact
     *            ExpressionFactory to use
     * @param ctx
     *            ELContext to validate against
     * @param in
     *            String to parse
     * @param location
     *            The location
     * @return ELText that can be re-applied later
     * @throws jakarta.el.ELException
     */
    public static ELText parse(ExpressionFactory fact, ELContext ctx, String in, Location location) throws ELException
    {
        char[] ca = in.toCharArray();
        int i = 0;
        char c = 0;
        int len = ca.length;
        int end = len - 1;
        boolean esc = false;
        int vlen = 0;

        StringBuilder buff = null;
        List<ELText> text = null;
        ELText t = null;
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
                        if (buff != null && buff.length() > 0)
                        {
                            if (text == null)
                            {
                                text = new ArrayList<>();
                            }
                            text.add(new ELText(buff.toString()));
                            buff.setLength(0);
                        }
                        vlen = findVarLength(ca, i);
                        if (ctx != null && fact != null)
                        {
                            ve = fact.createValueExpression(ctx, new String(ca, i, vlen), String.class);
                            if (location != null)
                            {
                                ve = new ContextAwareTagValueExpression(location, "expression", ve);
                            }
                            t = new ELCacheableTextVariable(ve);
                        }
                        else
                        {
                            ve = new LiteralValueExpression(new String(ca, i, vlen));
                            if (location != null)
                            {
                                ve = new ContextAwareTagValueExpression(location, "expression", ve);
                            }
                            t = new ELCacheableTextVariable(ve);
                        }
                        if (text == null)
                        {
                            text = new ArrayList<>();
                        }
                        text.add(t);
                        i += vlen;
                        continue;
                    }
                }
            }

            esc = false;
            if (buff == null)
            {
                buff = new StringBuilder(128);
            }
            buff.append(c);
            i++;
        }

        if (buff != null && buff.length() > 0)
        {
            if (text == null)
            {
                text = new ArrayList<>();
            }
            text.add(new ELText(buff.toString()));
            buff.setLength(0);
        }

        if (text == null || text.isEmpty())
        {
            return null;
        }
        else if (text.size() == 1)
        {
            return text.get(0);
        }
        else
        {
            ELText[] ta = text.toArray(new ELText[text.size()]);
            return new ELTextComposite(ta);
        }
    }

    public static ELText[] parseAsArray(String in, Location location) throws ELException
    {
        return parseAsArray(null, null, in, location);
    }
    
    public static ELText[] parseAsArray(ExpressionFactory fact, ELContext ctx, String in, Location location)
            throws ELException
    {
        char[] ca = in.toCharArray();
        int i = 0;
        char c = 0;
        int len = ca.length;
        int end = len - 1;
        boolean esc = false;
        int vlen = 0;

        StringBuilder buff = null;
        List<ELText> text = null;
        ELText t = null;
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
                        if (buff != null && buff.length() > 0)
                        {
                            if (text == null)
                            {
                                text = new ArrayList<>();
                            }
                            text.add(new ELText(buff.toString()));
                            buff.setLength(0);
                        }
                        vlen = findVarLength(ca, i);
                        if (ctx != null && fact != null)
                        {
                            ve = fact.createValueExpression(ctx, new String(ca, i, vlen), String.class);
                            if (location != null)
                            {
                                ve = new ContextAwareTagValueExpression(location, "expression", ve);
                            }
                            t = new ELCacheableTextVariable(ve);
                        }
                        else
                        {
                            ve = new LiteralValueExpression(new String(ca, i, vlen));
                            if (location != null)
                            {
                                ve = new ContextAwareTagValueExpression(location, "expression", ve);
                            }
                            t = new ELCacheableTextVariable(ve);
                        }
                        if (text == null)
                        {
                            text = new ArrayList<>();
                        }
                        text.add(t);
                        i += vlen;
                        continue;
                    }
                }
            }
            
            esc = false;
            if (buff == null)
            {
                buff = new StringBuilder(128);
            }
            buff.append(c);
            i++;
        }

        if (buff != null && buff.length() > 0)
        {
            if (text == null)
            {
                text = new ArrayList<>();
            }
            text.add(new ELText(buff.toString()));
            buff.setLength(0);
        }

        if (text == null || text.isEmpty())
        {
            return null;
        }
        else if (text.size() == 1)
        {
            return new ELText[]{text.get(0)};
        }
        else
        {
            ELText[] ta = text.toArray(new ELText[text.size()]);
            return ta;
        }
    }
    
    public static boolean isLiteral(ExpressionFactory fact, ELContext ctx, String in) throws ELException
    {
        char[] ca = in.toCharArray();
        int i = 0;
        char c = 0;
        int len = ca.length;
        int end = len - 1;
        boolean esc = false;
        //int vlen = 0;

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
                        //vlen = findVarLength(ca, i);
                        //In this point we have at least 1 EL expression, so it is not literal
                        return false;
                    }
                }
            }
            esc = false;
            i++;
        }
        return true;
    }

    private static int findVarLength(char[] ca, int s) throws ELException
    {
        int i = s;
        int len = ca.length;
        char c = 0;
        int str = 0;
        int nest = 0;
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
            else if ('{' == c && str == 0)
            {
                ++nest;
            }
            else if ('}' == c && str == 0 && nest > 1)
            {
                --nest;
            }
            else if (str == 0 && ('}' == c && nest == 1))
            {
                return i - s + 1;
            }
            i++;
        }
        throw new ELException("EL Expression unbalanced: ... " + new String(ca, s, i - s));
    }

}
