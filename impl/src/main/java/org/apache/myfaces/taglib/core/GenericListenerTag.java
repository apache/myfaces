/*
* Copyright 2004-2006 The Apache Software Foundation.
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

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentClassicTagBase;
import javax.faces.webapp.UIComponentELTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Andreas Berger (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.2
 */
public abstract class GenericListenerTag<_Holder, _Listener>
        extends TagSupport
{
    private ValueExpression _type = null;
    private ValueExpression _binding = null;
    private Class<_Holder> _holderClazz;

    protected GenericListenerTag(Class<_Holder> holderClazz)
    {
        super();
        _holderClazz = holderClazz;
    }

    public void setType(ValueExpression type)
    {
        _type = type;
    }

    public void setBinding(ValueExpression binding)
    {
        _binding = binding;
    }

    public void release()
    {
        super.release();
        _type = null;
        _binding = null;
    }

    protected abstract void addListener(_Holder holder, _Listener listener);

    public int doStartTag() throws JspException
    {
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

        if (_type == null)
        {
            throw new JspException("type attribute not set");
        }

        _Holder holder = null;
        UIComponent component = componentTag.getComponentInstance();
        try
        {
            holder = (_Holder) component;
        } catch (ClassCastException e)
        {
            throw new JspException(
                    "Component " + ((UIComponent) holder).getId() + " is not instance of " + _holderClazz.getName());
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        _Listener listener;
        // type and/or binding must be specified
        try
        {
            if (null != _binding)
            {
                try
                {
                    listener = (_Listener) _binding.getValue(facesContext.getELContext());
                    if (null != listener)
                    {
                        addListener(holder, listener);
                        // no need for further processing
                        return Tag.SKIP_BODY;
                    }
                }
                catch (ELException e)
                {
                    throw new JspException("Exception while evaluating the binding attribute of Component "
                            + component.getId(), e);
                }
            }
            if (null != _type)
            {
                String className;
                if (_type.isLiteralText())
                {
                    className = _type.getExpressionString();
                } else
                {
                    className = (String) _type.getValue(facesContext.getELContext());
                }
                listener = (_Listener) ClassUtils.newInstance(className);
                if (null != _binding)
                {
                    try
                    {
                        _binding.setValue(facesContext.getELContext(), listener);
                    } catch (ELException e)
                    {
                        throw new JspException("Exception while evaluating the binding attribute of Component "
                                + component.getId(), e);
                    }
                }
                addListener(holder, listener);
            }
        } catch (ClassCastException e)
        {
            throw new JspException(e);
        }

        return Tag.SKIP_BODY;
    }
}
