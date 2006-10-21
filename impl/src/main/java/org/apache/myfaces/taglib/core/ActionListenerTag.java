/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.myfaces.taglib.core;

import org.apache.myfaces.shared_impl.util.ClassUtils;

import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionListener;
import javax.faces.webapp.UIComponentClassicTagBase;
import javax.faces.webapp.UIComponentELTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import javax.el.ValueExpression;
import javax.el.ELException;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ActionListenerTag
        extends TagSupport
{
    //private static final Log log = LogFactory.getLog(ActionListenerTag.class);
    private static final long serialVersionUID = -2021978765020549175L;
    private ValueExpression _type = null;
    private ValueExpression _binding = null;

    public ActionListenerTag()
    {
    }

    public void setType(ValueExpression type)
    {
        _type = type;
    }


    public void setBinding(ValueExpression binding)
    {
        _binding = binding;
    }

    public int doStartTag() throws JspException
    {
        //Find parent UIComponentTag
        UIComponentClassicTagBase componentTag = UIComponentELTag.getParentUIComponentClassicTagBase(pageContext);
        if (componentTag == null)
        {
            throw new JspException("no parent UIComponentTag found");
        }

        if (_type == null)
        {
            throw new JspException("type attribute not set");
        }

        if (!componentTag.getCreated())
        {
            return Tag.SKIP_BODY;
        }

        UIComponent component = componentTag.getComponentInstance();

        // Must be nested inside a UIComponent custom action.
        if (component instanceof ActionSource)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ActionListener actionListener;

            // type and/or binding must be specified
            if (null != _binding)
            {
                try
                {
                    actionListener = (ActionListener) _binding.getValue(facesContext.getELContext());
                    if (null != actionListener)
                    {
                        ((ActionSource) component).addActionListener(actionListener);
                        // no need for further processing
                        return Tag.SKIP_BODY;
                    }
                }
                catch (ELException elException)
                {
                    throw new JspException("Exception while evaluating the binding attribute of Component "
                            + component.getId(), elException);
                }
            }
            if (null != _type)
            {
                String className;
                if (_type.isLiteralText())
                {
                    className = (String) _type.getValue(facesContext.getELContext());
                    actionListener = (ActionListener) ClassUtils.newInstance(className);
                    if (null != _binding)
                    {
                        _binding.setValue(facesContext.getELContext(), actionListener);
                    }
                    ((ActionSource) component).addActionListener(actionListener);
                }
            }
        }
        else
        {
            throw new JspException("Component " + component.getId() + " is no ActionSource");
        }

        return Tag.SKIP_BODY;
    }
}
