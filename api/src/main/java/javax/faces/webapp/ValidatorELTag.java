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

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.validator.Validator;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class ValidatorELTag extends TagSupport
{

    private static final Log log = LogFactory.getLog(ValidatorELTag.class);

    public int doStartTag() throws JspException
    {

        if (log.isDebugEnabled())
            log.debug("JSF 1.2 SPEC : Create a new instance "
                    + "of the specified Validator");

        Validator validator = createValidator();

        if (validator == null)
            throw new JspException("Could not create a Validator");

        if (log.isDebugEnabled())
            log.debug(" ... and register it with the UIComponent "
                    + "instance associated with our most "
                    + "immediately surrounding UIComponentTagBase");

        UIComponentTagBase tag = UIComponentELTag
                .getParentUIComponentClassicTagBase(pageContext);

        if (tag == null)
            throw new JspException(
                    "Could not obtain reference to parent UIComponentClassicTagBase instance ");

        if (log.isDebugEnabled())
            log.debug(" ... if the UIComponent instance was created "
                    + "by this execution of the containing JSP page.");

        if (tag.getCreated())
        {

            UIComponent component = tag.getComponentInstance();

            if (component == null)
                throw new JspException(
                        "Could not obtain reference to UIComponent for parent UIComponentClassicTagBase instance ");

            if (!(component instanceof EditableValueHolder))
                throw new JspException(
                        "UIComponent is not a EditableValueHolder " + component);

            ((EditableValueHolder) component).addValidator(validator);
        }
        
        return SKIP_BODY;
    }
    
    protected abstract Validator createValidator() throws JspException;
    
}
