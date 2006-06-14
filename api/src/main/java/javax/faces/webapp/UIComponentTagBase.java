package javax.faces.webapp;

import javax.el.ELContext;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.tagext.JspTag;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentTagBase extends Object implements JspTag
{

    protected abstract void addChild(UIComponent child);

    protected abstract void addFacet(String name);

    public abstract UIComponent getComponentInstance();

    public abstract String getComponentType();

    public abstract boolean getCreated();

    protected ELContext getELContext()
    {
        return getFacesContext().getELContext();
    }

    protected abstract FacesContext getFacesContext();

    protected abstract int getIndexOfNextChildTag();

    public abstract String getRendererType();

    public abstract void setId(String id);
}
