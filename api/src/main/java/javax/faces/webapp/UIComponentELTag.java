package javax.faces.webapp;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentELTag extends UIComponentClassicTagBase
        implements Tag
{

    protected UIComponent createComponent(FacesContext context, String newId)
    {
        throw new UnsupportedOperationException("");
    }

    protected ELContext getELContext()
    {
        throw new UnsupportedOperationException("");
    }

    protected boolean hasBinding()
    {
        throw new UnsupportedOperationException("");
    }

    public void release()
    {
        throw new UnsupportedOperationException("");
    }

    public void setBinding(ValueExpression binding)
    {
        throw new UnsupportedOperationException("");
    }

    protected void setProperties(UIComponent component)
    {
        throw new UnsupportedOperationException("");
    }

    public void setRendered(ValueExpression rendered)
    {
        throw new UnsupportedOperationException("");
    }

}
