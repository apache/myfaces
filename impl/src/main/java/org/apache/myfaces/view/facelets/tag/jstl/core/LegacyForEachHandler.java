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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;
import org.apache.myfaces.util.lang.ArrayIterator;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.PageContext;
import org.apache.myfaces.view.facelets.tag.ComponentContainerHandler;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;

/**
 * The basic iteration tag, accepting many different
 * collection types and supporting subsetting and other
 * functionality
 * 
 * NOTE: This implementation is provided for compatibility reasons and
 * it is considered faulty. It is enabled using
 * org.apache.myfaces.STRICT_JSF_2_FACELETS_COMPATIBILITY web config param.
 * Don't use it if EL expression caching is enabled.
 * 
 * @author Jacob Hookom
 * @author Andrew Robinson
 * @version $Id$
 */
//@JSFFaceletTag(name="c:forEach")
public final class LegacyForEachHandler extends TagHandler implements ComponentContainerHandler
{
    /**
     * If items specified:
     * Iteration begins at the item located at the
     * specified index. First item of the collection has
     * index 0.
     * If items not specified:
     * Iteration begins with index set at the value
     * specified.
     */
    private final TagAttribute begin;

    /**
     * If items specified:
     * Iteration ends at the item located at the
     * specified index (inclusive).
     * If items not specified:
     * Iteration ends when index reaches the value
     * specified.
     */
    private final TagAttribute end;

    /**
     * Collection of items to iterate over.
     */
    private final TagAttribute items;

    /**
     * Iteration will only process every step items of
     * the collection, starting with the first one.
     */
    private final TagAttribute step;

    private final TagAttribute tranzient;

    /**
     * Name of the exported scoped variable for the
     * current item of the iteration. This scoped
     * variable has nested visibility. Its type depends
     * on the object of the underlying collection.
     */
    private final TagAttribute var;

    /**
     * Name of the exported scoped variable for the
     * status of the iteration. 
     */
    private final TagAttribute varStatus;

    public LegacyForEachHandler(TagConfig config)
    {
        super(config);
        this.items = this.getAttribute("items");
        this.var = this.getAttribute("var");
        this.begin = this.getAttribute("begin");
        this.end = this.getAttribute("end");
        this.step = this.getAttribute("step");
        this.varStatus = this.getAttribute("varStatus");
        this.tranzient = this.getAttribute("transient");

        if (this.items == null && this.begin != null && this.end == null)
        {
            throw new TagAttributeException(this.tag, this.begin,
                                            "If the 'items' attribute is not specified, but the 'begin' attribute is, "
                                            + "then the 'end' attribute is required");
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {

        int s = this.getBegin(ctx);
        int e = this.getEnd(ctx);
        int m = this.getStep(ctx);
        Integer sO = this.begin != null ? Integer.valueOf(s) : null;
        Integer eO = this.end != null ? Integer.valueOf(e) : null;
        Integer mO = this.step != null ? Integer.valueOf(m) : null;

        boolean t = this.getTransient(ctx);
        Object src = null;
        ValueExpression srcVE = null;
        if (this.items != null)
        {
            srcVE = this.items.getValueExpression(ctx, Object.class);
            src = srcVE.getValue(ctx);
        }
        else
        {
            byte[] b = new byte[e + 1];
            for (int i = 0; i < b.length; i++)
            {
                b[i] = (byte) i;
            }
            src = b;
        }
        FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
        if (src != null)
        {
            try
            {
                fcc.startComponentUniqueIdSection();
                AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
                PageContext pctx = actx.getPageContext();
                Iterator<?> itr = this.toIterator(src);
                if (itr != null)
                {
                    int i = 0;

                    // move to start
                    while (i < s && itr.hasNext())
                    {
                        itr.next();
                        i++;
                    }

                    String v = this.getVarName(ctx);
                    String vs = this.getVarStatusName(ctx);
                    ValueExpression ve = null;
                    ValueExpression vO = this.capture(v, pctx);
                    ValueExpression vsO = this.capture(vs, pctx);
                    int mi = 0;
                    Object value = null;
                    try
                    {
                        boolean first = true;
                        while (i <= e && itr.hasNext())
                        {
                            value = itr.next();

                            // set the var
                            if (v != null)
                            {
                                if (t || srcVE == null)
                                {
                                    if (value == null)
                                    {
                                        pctx.getAttributes().put(v, null);
                                    }
                                    else
                                    {
                                        pctx.getAttributes().put(v, 
                                                ctx.getExpressionFactory().createValueExpression(
                                                    value, Object.class));
                                    }
                                }
                                else
                                {
                                    ve = this.getVarExpr(srcVE, src, value, i);
                                    pctx.getAttributes().put(v, ve);
                                }
                            }

                            // set the varStatus
                            if (vs != null)
                            {
                                IterationStatus itrS = new IterationStatus(first, !itr.hasNext(), i, sO, eO, mO, value);
                                if (t || srcVE == null)
                                {
                                    if (srcVE == null)
                                    {
                                        pctx.getAttributes().put(vs, null);
                                    }
                                    else
                                    {
                                        pctx.getAttributes().put(vs, 
                                                ctx.getExpressionFactory().createValueExpression(
                                                    itrS, Object.class));
                                    }
                                }
                                else
                                {
                                    ve = new IterationStatusExpression(itrS);
                                    pctx.getAttributes().put(vs, ve);
                                }
                            }

                            // execute body
                            this.nextHandler.apply(ctx, parent);

                            // increment steps
                            mi = 1;
                            while (mi < m && itr.hasNext())
                            {
                                itr.next();
                                mi++;
                                i++;
                            }
                            i++;

                            first = false;
                        }
                    }
                    finally
                    {
                        //Remove them from PageContext
                        if (v != null)
                        {
                            pctx.getAttributes().put(v, vO);
                        }
                        else
                        {
                            pctx.getAttributes().remove(v);
                        }
                        if (vs != null)
                        {
                            pctx.getAttributes().put(vs, vsO);
                        }
                        else
                        {
                            pctx.getAttributes().remove(vs);
                        }
                    }
                }
            }
            finally
            {
                fcc.endComponentUniqueIdSection();
            }
        }

        if (fcc.isUsingPSSOnThisView() && fcc.isRefreshTransientBuildOnPSS() && !fcc.isRefreshingTransientBuild())
        {
            //Mark the parent component to be saved and restored fully.
            ComponentSupport.markComponentToRestoreFully(ctx.getFacesContext(), parent);
        }
        if (fcc.isDynamicComponentSection())
        {
            ComponentSupport.markComponentToRefreshDynamically(ctx.getFacesContext(), parent);
        }
    }

    private ValueExpression capture(String name, PageContext pctx)
    {
        if (name != null)
        {
            return pctx.getAttributes().put(name, null);
        }
        return null;
    }

    private int getBegin(FaceletContext ctx)
    {
        if (this.begin != null)
        {
            return this.begin.getInt(ctx);
        }
        return 0;
    }

    private int getEnd(FaceletContext ctx)
    {
        if (this.end != null)
        {
            return this.end.getInt(ctx);
        }
        return Integer.MAX_VALUE - 1; // hotspot bug in the JVM
    }

    private int getStep(FaceletContext ctx)
    {
        if (this.step != null)
        {
            return this.step.getInt(ctx);
        }
        return 1;
    }

    private boolean getTransient(FaceletContext ctx)
    {
        if (this.tranzient != null)
        {
            return this.tranzient.getBoolean(ctx);
        }
        return false;
    }

    private ValueExpression getVarExpr(ValueExpression ve, Object src, Object value, int i)
    {
        if (src instanceof List || src.getClass().isArray())
        {
            return new IndexedValueExpression(ve, i);
        }
        else if (src instanceof Map && value instanceof Map.Entry entry)
        {
            return new MappedValueExpression(ve, entry);
        }
        else if (src instanceof Collection)
        {
            return new IteratedValueExpression(ve, value);
        }
        throw new IllegalStateException("Cannot create VE for: " + src);
    }

    private String getVarName(FaceletContext ctx)
    {
        if (this.var != null)
        {
            return this.var.getValue(ctx);
        }
        return null;
    }

    private String getVarStatusName(FaceletContext ctx)
    {
        if (this.varStatus != null)
        {
            return this.varStatus.getValue(ctx);
        }
        return null;
    }

    private Iterator<?> toIterator(Object src)
    {
        if (src == null)
        {
            return null;
        }
        else if (src instanceof Collection collection)
        {
            return collection.iterator();
        }
        else if (src instanceof Map map)
        {
            return map.entrySet().iterator();
        }
        else if (src.getClass().isArray())
        {
            return new ArrayIterator(src);
        }
        else
        {
            throw new TagAttributeException(this.tag, this.items,
                    "Must evaluate to a Collection, Map, Array, or null.");
        }
    }

}
