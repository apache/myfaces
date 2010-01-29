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
package org.apache.myfaces.view.facelets.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UniqueIdVendor;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.el.ELText;
import org.apache.myfaces.view.facelets.tag.composite.InsertChildrenHandler;
import org.apache.myfaces.view.facelets.tag.composite.InsertFacetHandler;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.util.FastWriter;

/**
 * @author Adam Winer
 * @version $Id: UIInstructionHandler.java,v 1.6 2008/07/13 19:01:33 rlubke Exp $
 */
final class UIInstructionHandler extends AbstractUIHandler
{

    private final String alias;

    private final String id;

    private final ELText txt;

    private final Instruction[] instructions;

    private final int length;

    private final boolean literal;

    public UIInstructionHandler(String alias, String id, Instruction[] instructions, ELText txt)
    {
        this.alias = alias;
        this.id = id;
        this.instructions = instructions;
        this.txt = txt;
        this.length = txt.toString().length();

        boolean literal = true;
        int size = instructions.length;

        for (int i = 0; i < size; i++)
        {
            Instruction ins = this.instructions[i];
            if (!ins.isLiteral())
            {
                literal = false;
                break;
            }
        }

        this.literal = literal;
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        if (parent != null)
        {
            // our id
            String id = ctx.generateUniqueId(this.id);

            // grab our component
            UIComponent c = ComponentSupport.findChildByTagId(parent, id);
            boolean componentFound = false;
            if (c != null)
            {
                componentFound = true;
                // mark all children for cleaning
                ComponentSupport.markForDeletion(c);
            }
            else
            {
                Instruction[] applied;
                if (this.literal)
                {
                    applied = this.instructions;
                }
                else
                {
                    int size = this.instructions.length;
                    applied = new Instruction[size];
                    // Create a new list with all of the necessary applied
                    // instructions
                    Instruction ins;
                    for (int i = 0; i < size; i++)
                    {
                        ins = this.instructions[i];
                        applied[i] = ins.apply(ctx.getExpressionFactory(), ctx);
                    }
                }

                c = new UIInstructions(txt, applied);
                // mark it owned by a facelet instance
                //c.setId(ComponentSupport.getViewRoot(ctx, parent).createUniqueId());
                AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
                UniqueIdVendor uniqueIdVendor = actx.getUniqueIdVendorFromStack();
                if (uniqueIdVendor == null)
                {
                    uniqueIdVendor = ComponentSupport.getViewRoot(ctx, parent);
                }
                if (uniqueIdVendor != null)
                {
                    // UIViewRoot implements UniqueIdVendor, so there is no need to cast to UIViewRoot
                    // and call createUniqueId(). Also, note that UIViewRoot.createUniqueId() javadoc
                    // says we could send as seed the facelet generated id.
                    String uid = uniqueIdVendor.createUniqueId(ctx.getFacesContext(), id);
                    c.setId(uid);
                }                
                c.getAttributes().put(ComponentSupport.MARK_CREATED, id);
            }
            // finish cleaning up orphaned children
            if (componentFound)
            {
                ComponentSupport.finalizeForDeletion(c);
                parent.getChildren().remove(c);
            }
            if ( ((AbstractFaceletContext)ctx).isRefreshingTransientBuild() 
                    && UIComponent.isCompositeComponent(parent))
            {
                // Save the child structure behind this component, so it can be
                // used later by InsertChildrenHandler and InsertFacetHandler
                // to update components correctly.
                String facetName = this.getFacetName(ctx, parent);
                if (facetName != null)
                {
                    if (parent.getAttributes().containsKey(InsertFacetHandler.INSERT_FACET_TARGET_ID+facetName))
                    {
                        List<String> ordering = (List<String>) parent.getAttributes().get(
                                InsertFacetHandler.INSERT_FACET_ORDERING+facetName);
                        if (ordering == null)
                        {
                            ordering = new ArrayList<String>();
                            parent.getAttributes().put(InsertFacetHandler.INSERT_FACET_ORDERING+facetName, ordering);
                        }
                        ordering.remove(id);
                        ordering.add(id);
                    }
                }
                else
                {
                    if (parent.getAttributes().containsKey(InsertChildrenHandler.INSERT_CHILDREN_TARGET_ID))
                    {
                        List<String> ordering = (List<String>) parent.getAttributes().get(
                                InsertChildrenHandler.INSERT_CHILDREN_ORDERING);
                        if (ordering == null)
                        {
                            ordering = new ArrayList<String>();
                            parent.getAttributes().put(InsertChildrenHandler.INSERT_CHILDREN_ORDERING, ordering);
                        }
                        ordering.remove(id);
                        ordering.add(id);
                    }
                }
            }
            this.addComponent(ctx, parent, c);
        }
    }

    public String toString()
    {
        return this.txt.toString();
    }

    public String getText()
    {
        return this.txt.toString();
    }

    public String getText(FaceletContext ctx)
    {
        Writer writer = new FastWriter(this.length);
        try
        {
            this.txt.apply(ctx.getExpressionFactory(), ctx).write(writer, ctx);
        }
        catch (IOException e)
        {
            throw new ELException(this.alias + ": " + e.getMessage(), e.getCause());
        }
        return writer.toString();
    }

}
