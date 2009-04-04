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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.Renderer;

class UILeaf extends UIComponentBase
{

    private final static Map<String, UIComponent> facets = new HashMap<String, UIComponent>()
    {

        @Override
        public void putAll(Map<? extends String, ? extends UIComponent> map)
        {
            // do nothing
        }

        @Override
        public UIComponent put(String name, UIComponent value)
        {
            return null;
        }
    };

    private UIComponent parent;

    @Override
    @SuppressWarnings("deprecation")
    public ValueBinding getValueBinding(String binding)
    {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setValueBinding(String name, ValueBinding binding)
    {
        // do nothing
    }

    @Override
    public ValueExpression getValueExpression(String name)
    {
        return null;
    }

    @Override
    public void setValueExpression(String name, ValueExpression arg1)
    {
        // do nothing
    }

    public String getFamily()
    {
        return "facelets.LiteralText";
    }

    @Override
    public UIComponent getParent()
    {
        return this.parent;
    }

    @Override
    public void setParent(UIComponent parent)
    {
        this.parent = parent;
    }

    @Override
    public boolean isRendered()
    {
        return true;
    }

    @Override
    public void setRendered(boolean rendered)
    {
        // do nothing
    }

    @Override
    public String getRendererType()
    {
        return null;
    }

    @Override
    public void setRendererType(String rendererType)
    {
        // do nothing
    }

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public List<UIComponent> getChildren()
    {
        List<UIComponent> children = Collections.emptyList();
        return children;
    }

    @Override
    public int getChildCount()
    {
        return 0;
    }

    @Override
    public UIComponent findComponent(String id)
    {
        return null;
    }

    @Override
    public Map<String, UIComponent> getFacets()
    {
        return facets;
    }

    @Override
    public int getFacetCount()
    {
        return 0;
    }

    @Override
    public UIComponent getFacet(String name)
    {
        return null;
    }

    @Override
    public Iterator<UIComponent> getFacetsAndChildren()
    {
        List<UIComponent> childrenAndFacets = Collections.emptyList();
        
        return childrenAndFacets.iterator();
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        // do nothing
    }

    @Override
    public void decode(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public void encodeBegin(FacesContext faces) throws IOException
    {
        // do nothing
    }

    @Override
    public void encodeChildren(FacesContext faces) throws IOException
    {
        // do nothing
    }

    @Override
    public void encodeEnd(FacesContext faces) throws IOException
    {
        // do nothing
    }

    @Override
    public void encodeAll(FacesContext faces) throws IOException
    {
        this.encodeBegin(faces);
    }

    @Override
    protected void addFacesListener(FacesListener faces)
    {
        // do nothing
    }

    @Override
    protected FacesListener[] getFacesListeners(Class faces)
    {
        return null;
    }

    @Override
    protected void removeFacesListener(FacesListener faces)
    {
        // do nothing
    }

    @Override
    public void queueEvent(FacesEvent event)
    {
        // do nothing
    }

    @Override
    public void processRestoreState(FacesContext faces, Object state)
    {
        // do nothing
    }

    @Override
    public void processDecodes(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public void processValidators(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public void processUpdates(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public Object processSaveState(FacesContext faces)
    {
        return null;
    }

    @Override
    protected FacesContext getFacesContext()
    {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected Renderer getRenderer(FacesContext faces)
    {
        return null;
    }

    @Override
    public Object saveState(FacesContext faces)
    {
        return null;
    }

    @Override
    public void restoreState(FacesContext faces, Object state)
    {
        // do nothing
    }

    @Override
    public boolean isTransient()
    {
        return true;
    }

    @Override
    public void setTransient(boolean tranzient)
    {
        // do nothing
    }

}
