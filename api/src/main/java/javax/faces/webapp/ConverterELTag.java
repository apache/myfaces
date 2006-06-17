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

        if( log.isInfoEnabled() )
            log.info("JSF 1.2 SPEC : Create a new instance of " +
                    "the specified Converter class");

        Converter converter = createConverter();

        if (converter == null)
            throw new JspException("Could not create Converter instance");

        if( log.isInfoEnabled() )
            log.info("JSF 1.2 SPEC : ... and register it with the " +
                    "UIComponent instance associated with our" +
                    " most immediately surrounding " +
                    "UIComponentClassicTagBase instance"); 

        UIComponentClassicTagBase tag = UIComponentClassicTagBase
                .getParentUIComponentClassicTagBase(pageContext);

        if (tag == null)
            throw new JspException(
                    "Could not obtain reference to parent UIComponentClassicTagBase instance ");

        if( log.isInfoEnabled() )
            log.info("JSF 1.2 SPEC : ... if the UIComponent " +
                    "instance was created by this execution " +
                    "of the containing JSP page.");

        if (tag.getCreated())
        {

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
