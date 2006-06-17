package javax.faces.component.html;

import javax.faces.component.UIColumn;
import javax.faces.context.FacesContext;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public class HtmlColumn extends UIColumn
{

    public static final String COMPONENT_TYPE = "javax.faces.HtmlColumn";
    
    private String headerClass;
    
    private String footerClass;

    public String getFooterClass()
    {
        return footerClass;
    }

    public void setFooterClass(String footerClass)
    {
        this.footerClass = footerClass;
    }

    public String getHeaderClass()
    {
        return headerClass;
    }

    public void setHeaderClass(String headerClass)
    {
        this.headerClass = headerClass;
    }
    
    public Object saveState(FacesContext ctx)
    {
        Object values[] = new Object[3];
        values[0] = super.saveState(ctx);
        values[1] = headerClass;
        values[2] = footerClass;
        return ((Object) (values));
    }
    
    public void restoreState(FacesContext ctx, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(ctx, values[0]);
        headerClass = (String)values[1];
        footerClass = (String)values[2];
    }
    
}
