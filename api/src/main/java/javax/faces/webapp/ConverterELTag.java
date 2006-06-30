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

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.convert.Converter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;  
import org.apache.commons.logging.LogFactory;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class ConverterELTag extends TagSupport
{
    
    private static final Log log = LogFactory.getLog(ConverterELTag.class);
    
    public int doStartTag() throws JspException
    {

        if( log.isDebugEnabled() )
            log.debug("JSF 1.2 SPEC : Create a new instance of " +
                    "the specified Converter class");

        Converter converter = createConverter();

        if (converter == null)
            throw new JspException("Could not create Converter instance");

        UIComponentClassicTagBase tag = UIComponentClassicTagBase
                .getParentUIComponentClassicTagBase(pageContext);

        if (tag == null)
            throw new JspException(
                    "Could not obtain reference to parent UIComponentClassicTagBase instance ");

        if (tag.getCreated())
        {

            if( log.isDebugEnabled() )
                log.debug("JSF 1.2 SPEC : ... and register it with the " +
                        "UIComponent instance associated with our" +
                        " most immediately surrounding " +
                        "UIComponentClassicTagBase instance" +
                        "if the UIComponent " +
                        "instance was created by this execution " +
                        "of the containing JSP page."); 
            
            UIComponent component = tag.getComponentInstance();

            if (component == null)
                throw new JspException(
                        "Could not obtain reference to UIComponent for parent UIComponentClassicTagBase instance ");

            if (!(component instanceof ValueHolder))
                throw new JspException("UIComponent is not a ValueHolder "
                        + component);

            ((ValueHolder) component).setConverter(converter);
        }
        
        return SKIP_BODY;
    }
    
    protected abstract Converter createConverter() throws JspException;
}
