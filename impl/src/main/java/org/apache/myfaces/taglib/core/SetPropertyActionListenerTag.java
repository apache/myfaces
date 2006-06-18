package org.apache.myfaces.taglib.core;

import javax.el.ValueExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionListener;
import javax.faces.webapp.UIComponentClassicTagBase;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.myfaces.event.SetPropertyActionListener;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public class SetPropertyActionListenerTag extends TagSupport
{
    
    private static final Log log = LogFactory.getLog(SetPropertyActionListenerTag.class);
    
    private ValueExpression target;
    
    private ValueExpression value;

    public int doStartTag() throws JspException 
    {
        
        if(log.isDebugEnabled())
            log.debug("JSF 1.2 Spec : Create a new instance of the ActionListener");

        ActionListener actionListener = new SetPropertyActionListener(target, value);
        
        UIComponentClassicTagBase tag = UIComponentClassicTagBase.getParentUIComponentClassicTagBase(pageContext);
        
        if(tag == null)
            throw new JspException("Could not find a " +
                    "parent UIComponentClassicTagBase ... is this " +
                    "tag in a child of a UIComponentClassicTagBase?");
        
        if(tag.getCreated())
        {
            
            UIComponent component = tag.getComponentInstance();
            
            if(component == null)
                throw new JspException(" Could not locate a UIComponent " +
                        "for a UIComponentClassicTagBase w/ a " +
                        "JSP id of " + tag.getJspId());
            
            if( ! ( component instanceof ActionSource ) )
                throw new JspException("Component w/ id of " + component.getId() 
                        + " is associated w/ a tag w/ JSP id of " + tag.getJspId() 
                        + ". This component is of type " + component.getClass() 
                        + ", which is not an " + ActionSource.class );
            
            if(log.isDebugEnabled())
                log.debug(" ... register it with the UIComponent " +
                        "instance associated with our most immediately " +
                        "surrounding UIComponentTagBase");
            
            ((ActionSource)component).addActionListener(actionListener);
            
        }
        
        return SKIP_BODY;
    }
    
    public ValueExpression getTarget()
    {
        return target;
    }

    public void setTarget(ValueExpression target)
    {
        this.target = target;
    }

    public ValueExpression getValue()
    {
        return value;
    }

    public void setValue(ValueExpression value)
    {
        this.value = value;
    }
    
    public void release() 
    {
        target = null;
        value = null;
    }
    
}
