/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.faces.webapp;

import javax.el.ELContext;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.tagext.JspTag;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentTagBase extends Object implements JspTag
{

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#addChild(javax.faces.component.UIComponent)
     * @param child
     */

    protected abstract void addChild(UIComponent child);

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#addFacet(java.lang.String)
     * @param name
     */

    protected abstract void addFacet(String name);

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#getComponentInstance()
     * @return
     */

    public abstract UIComponent getComponentInstance();

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#getComponentType()
     * @return
     */

    public abstract String getComponentType();

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#getCreated()
     * @return
     */

    public abstract boolean getCreated();

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#getELContext()
     * @return
     */

    protected ELContext getELContext()
    {

        FacesContext ctx = getFacesContext();

        if (ctx == null)
            throw new NullPointerException("FacesContext ctx");

        return getFacesContext().getELContext();
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#getFacesContext()
     * @return
     */

    protected abstract FacesContext getFacesContext();

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#getIndexOfNextChildTag()
     * @return
     */

    protected abstract int getIndexOfNextChildTag();

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#getRendererType()
     * @return
     */

    public abstract String getRendererType();

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentTagBase.html#setId(java.lang.String)
     * @param id
     */

    public abstract void setId(String id);
}
