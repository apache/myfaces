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
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentELTag extends UIComponentClassicTagBase
        implements Tag
{

    private static final Log log = LogFactory.getLog(UIComponentELTag.class);

    private ValueExpression binding;

    private ValueExpression rendered;

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentELTag.html#createComponent(javax.faces.context.FacesContext,%20java.lang.String)
     */

    protected UIComponent createComponent(FacesContext context, String newId)
    {
        if (context == null)
            throw new NullPointerException("FacesContext context");

        Application app = context.getApplication();

        String componentType = getComponentType();

        UIComponent component;

        if (log.isDebugEnabled())
            log.debug("JSF SPEC 1.2 : If this "
                    + "UIComponentELTag has a non-null binding");

        if (binding != null)
        {
            if (log.isDebugEnabled())
                log.debug("... call Application.createComponent");
            component = app.createComponent(binding, context, componentType);

        }
        else
        {

            if (log.isDebugEnabled())
                log.debug("... called with only the component type");
            component = app.createComponent(componentType);

        }

        if (log.isDebugEnabled())
            log.debug("... initialize the components id and other properties");
        component.setId(newId);
        setProperties(component);

        return component;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentELTag.html#getELContext()
     */

    protected ELContext getELContext()
    {
        FacesContext ctx = getFacesContext();

        return ctx == null ? null : ctx.getELContext();
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentELTag.html#hasBinding()
     */

    protected boolean hasBinding()
    {
        return binding != null;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentELTag.html#release()
     */

    public void release()
    {
        super.release();
        this.binding = null;
        this.rendered = null;
    }

    /**
     *
     * @param binding The new value expression
     *
     * @throws JspException if an error occurs
     */
    public void setBinding(ValueExpression binding) throws JspException
    {
        this.binding = binding;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentELTag.html#setProperties(javax.faces.component.UIComponent)
     */

    protected void setProperties(UIComponent component)
    {

        if (!getCreated())
        {
            // throw an exception ?
            if (log.isWarnEnabled())
                log.warn("JSF 1.2 SPEC : This method must be called ONLY "
                        + "if the specified UIComponent was in fact created "
                        + "during the execution of this tag handler instance");
        }

        if (log.isDebugEnabled())
            log.debug("rendered set if a value for the "
                    + "rendered property is specified for "
                    + "this tag handler instance.");

        if (component == null)
            throw new NullPointerException(
                    "could not find component to set properties for");

        if (rendered != null && rendered.isLiteralText())
        {
            component.setRendered(Boolean.valueOf(rendered
                    .getExpressionString()));
        }
        else if (rendered != null && !rendered.isLiteralText())
        {
            component.setValueExpression("rendered", rendered);
        }

        if (log.isDebugEnabled())
            log.debug("rendererType set if the getRendererType()"
                    + " method returns a non-null value.");

        String renderType = getRendererType();

        if (renderType != null)
            component.setRendererType(renderType);
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentELTag.html#setRendered(javax.el.ValueExpression)
     */

    public void setRendered(ValueExpression rendered)
    {
        this.rendered = rendered;
    }

}
