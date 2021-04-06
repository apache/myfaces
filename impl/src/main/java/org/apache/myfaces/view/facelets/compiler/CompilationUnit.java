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
import java.util.ArrayList;
import java.util.List;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
class CompilationUnit
{

    protected final static FaceletHandler LEAF = new FaceletHandler()
    {
        @Override
        public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
                ELException
        {
        }

        @Override
        public String toString()
        {
            return "FaceletHandler Tail";
        }
    };

    private List<CompilationUnit> children;

    public CompilationUnit()
    {
    }

    public void addChild(CompilationUnit unit)
    {
        if (this.children == null)
        {
            this.children = new ArrayList<>();
        }
        this.children.add(unit);
    }

    public FaceletHandler createFaceletHandler()
    {
        return this.getNextFaceletHandler();
    }

    protected final FaceletHandler getNextFaceletHandler()
    {
        if (this.children == null || this.children.isEmpty())
        {
            return LEAF;
        }
        if (this.children.size() == 1)
        {
            CompilationUnit u = (CompilationUnit) this.children.get(0);
            return u.createFaceletHandler();
        }
        FaceletHandler[] fh = new FaceletHandler[this.children.size()];
        for (int i = 0; i < fh.length; i++)
        {
            fh[i] = ((CompilationUnit) this.children.get(i)).createFaceletHandler();
        }
        return new jakarta.faces.view.facelets.CompositeFaceletHandler(fh);
    }

}
