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

        if (log.isInfoEnabled())
            log.info("JSF 1.2 SPEC : Create a new instance "
                    + "of the specified Validator");

        Validator validator = createValidator();

        if (validator == null)
            throw new JspException("Could not create a Validator");

        if (log.isInfoEnabled())
            log.info(" ... and register it with the UIComponent "
                    + "instance associated with our most "
                    + "immediately surrounding UIComponentTagBase");

        UIComponentTagBase tag = UIComponentELTag
                .getParentUIComponentClassicTagBase(pageContext);

        if (tag == null)
            throw new JspException(
                    "Could not obtain reference to parent UIComponentClassicTagBase instance ");

        if (log.isInfoEnabled())
            log.info(" ... if the UIComponent instance was created "
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
