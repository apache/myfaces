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
package javax.faces.component;

import javax.faces.event.AbortProcessingException;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   type = "javax.faces.Component"
 *   family = "javax.faces.Component"
 *   desc = "abstract base component"
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class UIComponent
        implements StateHolder
{
    public UIComponent()
    {
    }

    public abstract java.util.Map getAttributes();

    public abstract javax.faces.el.ValueBinding getValueBinding(java.lang.String name);

    public abstract void setValueBinding(java.lang.String name,
                                         javax.faces.el.ValueBinding binding);

    public abstract java.lang.String getClientId(javax.faces.context.FacesContext context);

    public abstract java.lang.String getFamily();

    public abstract java.lang.String getId();

    public abstract void setId(java.lang.String id);

    public abstract javax.faces.component.UIComponent getParent();

    /**
     * For JSF-framework internal use only.   Don't call this method to
     * add components to the component tree.
     * Use <code>parent.getChildren().add(child)</code> instead.
     */
    public abstract void setParent(javax.faces.component.UIComponent parent);

    public abstract boolean isRendered();

    public abstract void setRendered(boolean rendered);

    public abstract java.lang.String getRendererType();

    public abstract void setRendererType(java.lang.String rendererType);

    public abstract boolean getRendersChildren();

    public abstract java.util.List getChildren();

    public abstract int getChildCount();

    public abstract javax.faces.component.UIComponent findComponent(java.lang.String expr);

    public abstract java.util.Map getFacets();

    public abstract javax.faces.component.UIComponent getFacet(java.lang.String name);

    public abstract java.util.Iterator getFacetsAndChildren();

    public abstract void broadcast(javax.faces.event.FacesEvent event)
            throws AbortProcessingException;

    public abstract void decode(javax.faces.context.FacesContext context);

    public abstract void encodeBegin(javax.faces.context.FacesContext context)
            throws java.io.IOException;

    public abstract void encodeChildren(javax.faces.context.FacesContext context)
            throws java.io.IOException;

    public abstract void encodeEnd(javax.faces.context.FacesContext context)
            throws java.io.IOException;

    protected abstract void addFacesListener(javax.faces.event.FacesListener listener);

    protected abstract javax.faces.event.FacesListener[] getFacesListeners(java.lang.Class clazz);

    protected abstract void removeFacesListener(javax.faces.event.FacesListener listener);

    public abstract void queueEvent(javax.faces.event.FacesEvent event);

    public abstract void processRestoreState(javax.faces.context.FacesContext context,
                                             java.lang.Object state);

    public abstract void processDecodes(javax.faces.context.FacesContext context);

    public abstract void processValidators(javax.faces.context.FacesContext context);

    public abstract void processUpdates(javax.faces.context.FacesContext context);

    public abstract java.lang.Object processSaveState(javax.faces.context.FacesContext context);

    protected abstract javax.faces.context.FacesContext getFacesContext();

    protected abstract javax.faces.render.Renderer getRenderer(javax.faces.context.FacesContext context);
}
