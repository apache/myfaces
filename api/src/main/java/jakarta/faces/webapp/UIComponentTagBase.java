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

package jakarta.faces.webapp;

import java.util.logging.Logger;

import javax.el.ELContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import javax.servlet.jsp.tagext.JspTag;

/**
 * @since 1.2
 */

public abstract class UIComponentTagBase extends Object implements JspTag
{
    protected static final Logger log = Logger.getLogger("javax.faces.webapp");
    
    protected abstract void addChild(UIComponent child);

    protected abstract void addFacet(String name);

    public abstract UIComponent getComponentInstance();

    /**
     * Specify the "component type name" used together with the component's
     * family and the Application object to create a UIComponent instance for
     * this tag. This method is called by other methods in this class, and is
     * intended to be overridden in subclasses to specify the actual component
     * type to be created.
     *
     * @return a registered component type name, never null.
     */

    public abstract String getComponentType();

    /**
     * @return
     */

    public abstract boolean getCreated();

    /**
     * @return
     */

    protected ELContext getELContext()
    {

        FacesContext ctx = getFacesContext();

        if (ctx == null)
        {
            throw new NullPointerException("FacesContext ctx");
        }

        return getFacesContext().getELContext();
    }

    /**
     * @return
     */

    protected abstract FacesContext getFacesContext();

    /**
     * @return
     */

    protected abstract int getIndexOfNextChildTag();

    /**
     * Specify the "renderer type name" used together with the current
     * renderKit to get a Renderer instance for the corresponding UIComponent.
     * <p>
     * A JSP tag can return null here to use the default renderer type string.
     * If non-null is returned, then the UIComponent's setRendererType method
     * will be called passing this value, and this will later affect the
     * type of renderer object returned by UIComponent.getRenderer().
     * 
     * @return
     */
    public abstract String getRendererType();

    /**
     * @param id
     */

    public abstract void setId(String id);
}
