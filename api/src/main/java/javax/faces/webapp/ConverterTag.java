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

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This tag creates an instance of the specified Converter, and
 * associates it with the nearest parent UIComponent.
 * 
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFJspTag
 *   name="f:converter"
 *   bodyContent="empty"
 *   
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ConverterTag
        extends TagSupport
{
    private static final long serialVersionUID = -6168345066829108081L;
    private String _converterId;

    public ConverterTag()
    {
        super();
    }

    /**
     * The converter's registered ID.
     * 
     * @JSFJspAttribute
     *   required="true"
     */
    public void setConverterId(String converterId)
    {
        _converterId = converterId;
    }

    public int doStartTag()
            throws JspException
    {
        UIComponentTag componentTag = UIComponentTag.getParentUIComponentTag(pageContext);
        if (componentTag == null)
        {
            throw new JspException("no parent UIComponentTag found");
        }
        if (!componentTag.getCreated())
        {
            return Tag.SKIP_BODY;
        }

        Converter converter = createConverter();

        UIComponent component = componentTag.getComponentInstance();
        if (component == null)
        {
            throw new JspException("parent UIComponentTag has no UIComponent");
        }
        if (!(component instanceof ValueHolder))
        {
            throw new JspException("UIComponent is no ValueHolder");
        }
        ((ValueHolder)component).setConverter(converter);

        return Tag.SKIP_BODY;
    }

    public void release()
    {
        super.release();
        _converterId = null;
    }

    protected Converter createConverter()
            throws JspException
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        if (UIComponentTag.isValueReference(_converterId))
        {
            ValueBinding vb = facesContext.getApplication().createValueBinding(_converterId);
            return application.createConverter((String)vb.getValue(facesContext));
        }
        else
        {
            return application.createConverter(_converterId);
        }
    }

}
