package javax.faces.webapp;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentELTag extends UIComponentClassicTagBase
        implements Tag
{

    private static final Log log = LogFactory.getLog(UIComponentELTag.class);
    
    private ValueExpression binding;
    
    private ValueExpression rendered;

    protected void setProperties(UIComponent component)
    {
        
        if( getCreated() )
        {
            // throw an exception ?
            if(log.isWarnEnabled())
                log.warn("JSF 1.2 SPEC : This method must be called ONLY " +
                        "if the specified UIComponent was in fact created " +
                        "during the execution of this tag handler instance");
        }
        
        if( log.isInfoEnabled() )
            log.info("rendered set if a value for the " +
                    "rendered property is specified for " +
                    "this tag handler instance.");
        
        if( component == null )
            throw new NullPointerException("could not find component to set properties for");
        
        if( rendered != null && rendered.isLiteralText() )
        {
            component.setRendered(Boolean.valueOf(rendered.getExpressionString()));
        }
        else if ( rendered != null && ! rendered.isLiteralText() )
        {
            component.setValueExpression("rendered", rendered);
        }
        
        if( log.isInfoEnabled() )
            log.info("rendererType set if the getRendererType()" +
                    " method returns a non-null value.");
        
        String renderType = getRendererType();
        
        if ( renderType != null)
            component.setRendererType(renderType);
    }

    protected UIComponent createComponent(FacesContext context, String newId)
    {
        if( context == null )
            throw new NullPointerException("FacesContext context");
        
        Application app = context.getApplication();
        
        String componentType = getComponentType();
        
        UIComponent component;
        
        if(log.isInfoEnabled())
            log.info("JSF SPEC 1.2 : If this " +
                    "UIComponentELTag has a non-null binding");
        
        if( binding != null )
        {
            if(log.isInfoEnabled())
                log.info("... call Application.createComponent");
            component = app.createComponent(binding, context, componentType);
            
        }else{
            
            if(log.isInfoEnabled())
                log.info("... called with only the component type");
            component = app.createComponent(componentType);
            
        }
        
        if(log.isInfoEnabled())
            log.info("... initialize the components id and other properties");
        component.setId(newId);
        setProperties(component);
        
        return component;
    }
    
    public void setBinding(ValueExpression binding) throws JspException 
    {
       // waiting to hear back on RI team about whether or not to 
       // uncomment this
        
       // if (!isValueReference(binding))
       //     throw new JspException(binding.getExpressionString()
       //            + " is not a value reference");

        this.binding = binding;
    }
    
    protected ELContext getELContext()
    {
        FacesContext ctx = getFacesContext();
        
        return ctx == null ? null : ctx.getELContext();
    }
    
    public void release()
    {
        super.release();
        this.binding = null;
        this.rendered = null;
    }
    
    public void setRendered(ValueExpression rendered)
    {
        this.rendered = rendered;
    }
    
    protected boolean hasBinding()
    {
        return binding != null;
    }

}
