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
package javax.faces.webapp;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This tag associates an attribute with the nearest parent
 * UIComponent. 
 * <p>
 * When the value is not an EL expression, this tag has the same effect
 * as calling component.getAttributes.put(name, value). When the attribute
 * name specified matches a standard property of the component, that
 * property is set. However it is also valid to assign attributes
 * to components using any arbitrary name; the component itself won't
 * make any use of these but other objects such as custom renderers,
 * validators or action listeners can later retrieve the attribute
 * from the component by name.
 * <p>
 * When the value is an EL expression, this tag has the same effect
 * as calling component.setValueBinding. A call to method
 * component.getAttributes().get(name) will then cause that
 * expression to be evaluated and the result of the expression is
 * returned, not the original EL expression string.
 * <p>
 * See the javadoc for UIComponent.getAttributes for more details.
 * <p>
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * 
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFJspTag 
 *   name="f:attribute"
 *   bodyContent="empty"
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class AttributeTag
        extends TagSupport
{
    private static final long serialVersionUID = 3147657100171678632L;
    private String _name;
    private String _value;

    /**
     * The name of the attribute.
     * 
     * @JSFJspAttribute
     *   required="true"
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * The attribute's value.
     * 
     * @JSFJspAttribute
     *   required="true"
     */
    public void setValue(String value)
    {
        _value = value;
    }

    public int doStartTag() throws JspException
    {
        UIComponentTag componentTag = UIComponentTag.getParentUIComponentTag(pageContext);
        if (componentTag == null)
        {
            throw new JspException("no parent UIComponentTag found");
        }
        UIComponent component = componentTag.getComponentInstance();
        if (component == null)
        {
            throw new JspException("parent UIComponentTag has no UIComponent");
        }
        String name = getName();
        if (component.getAttributes().get(name) == null)
        {
            if (UIComponentTag.isValueReference(_value))
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ValueBinding vb = facesContext.getApplication().createValueBinding(_value);
                component.setValueBinding(name, vb);
            }
            else
            {
            if(_value != null) component.getAttributes().put(name, _value);
            }
        }
        return Tag.SKIP_BODY;
    }

    public void release()
    {
        super.release();
        _name = null;
        _value = null;
    }


    private String getName()
    {
        if (UIComponentTag.isValueReference(_name))
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ValueBinding vb = facesContext.getApplication().createValueBinding(_name);
            return (String)vb.getValue(facesContext);
        }
        else
        {
            return _name;
        }
    }

}
