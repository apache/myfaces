/*
 * Copyright 2004 The Apache Software Foundation.
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

import org.apache.myfaces.shared.util.ClassUtils;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.ValueChangeListener;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ValueChangeListenerTag
        extends TagSupport
{
    //private static final Log log = LogFactory.getLog(ValueChangeListenerTag.class);

    private static final long serialVersionUID = 2155190261951046892L;
    private String _type = null;

    public ValueChangeListenerTag()
    {
    }

    public void setType(String type)
    {
        _type = type;
    }


    public int doStartTag() throws JspException
    {
        if (_type == null)
        {
            throw new JspException("type attribute not set");
        }

        //Find parent UIComponentTag
        UIComponentTag componentTag = UIComponentTag.getParentUIComponentTag(pageContext);
        if (componentTag == null)
        {
            throw new JspException("ValueChangeListenerTag has no UIComponentTag ancestor");
        }

        if (componentTag.getCreated())
        {
            //Component was just created, so we add the Listener
            UIComponent component = componentTag.getComponentInstance();
            if (component instanceof EditableValueHolder)
            {
                String className;
                if (UIComponentTag.isValueReference(_type))
                {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    ValueBinding vb = facesContext.getApplication().createValueBinding(_type);
                    className = (String)vb.getValue(facesContext);
                }
                else
                {
                    className = _type;
                }
                ValueChangeListener vcl = (ValueChangeListener)ClassUtils.newInstance(className);
                ((EditableValueHolder)component).addValueChangeListener(vcl);
            }
            else
            {
                throw new JspException("Component " + component.getId() + " is no EditableValueHolder");
            }
        }

        return Tag.SKIP_BODY;
    }


}
