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

    protected abstract void addChild(UIComponent child);

    protected abstract void addFacet(String name);

    public abstract UIComponent getComponentInstance();

    public abstract String getComponentType();

    public abstract boolean getCreated();

    protected ELContext getELContext()
    {
        return getFacesContext().getELContext();
    }

    protected abstract FacesContext getFacesContext();

    protected abstract int getIndexOfNextChildTag();

    public abstract String getRendererType();

    public abstract void setId(String id);
}
