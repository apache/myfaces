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
package org.apache.myfaces.taglib.core;

import org.apache.myfaces.shared_impl.renderkit.JSFAttr;
import org.apache.myfaces.shared_impl.taglib.UIComponentBodyTagBase;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

/**
 * Outputs its body as verbatim text. No JSP tags within the verbatim
 * tag (including JSF tags) are evaluated; the content is treated
 * simply as literal text to be copied to the response.
 * &lt;p&gt;
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * 
 * @JSFJspTag
 *   name="f:verbatim"
 *   bodyContent="JSP" 
 *   
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class VerbatimTag
        extends UIComponentBodyTagBase
{
    //private static final Log log = LogFactory.getLog(VerbatimTag.class);

    public String getComponentType()
    {
        return "javax.faces.Output";
    }

    public String getRendererType()
    {
        return "javax.faces.Text";
    }

    // HtmlOutputText attributes
    private String _escape;

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);
        if (_escape != null)
        {
            setBooleanProperty(component, JSFAttr.ESCAPE_ATTR, _escape);
        }
        else
        {
            //Default escape value for component is true, but for this tag it is false,
            //so we must set it to false explicitly, if no attribute is given
            component.getAttributes().put(JSFAttr.ESCAPE_ATTR, Boolean.FALSE);
        }

        //No need to save component state
        component.setTransient(true);
    }

    /**
     * If true, generated markup is escaped.  Default:  false.
     * 
     * @JSFJspAttribute
     */
    public void setEscape(String escape)
    {
        _escape = escape;
    }
    
    public int doAfterBody() throws JspException
    {
        BodyContent bodyContent = getBodyContent();
        if (bodyContent != null)
        {
            UIOutput component = (UIOutput)getComponentInstance();
            component.setValue(bodyContent.getString());
        }
        return super.doAfterBody();
    }
}
