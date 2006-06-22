package org.apache.myfaces.event;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * The MyFaces implementation of the <code>SetPropertyActionListener</code>.
 * 
 * @author Dennis Byrne
 * @since 1.2
 */
public class SetPropertyActionListener implements ActionListener, StateHolder
{

    private ValueExpression target;
    
    private ValueExpression value;
    
    private boolean _transient ;
    
    public SetPropertyActionListener(){}
    
    public SetPropertyActionListener(ValueExpression target, ValueExpression value)
    {
        this.target = target;
        this.value = value;
    }
    
    public void processAction(ActionEvent actionEvent) throws AbortProcessingException
    {
        
        if( target == null )
            throw new AbortProcessingException("@target has not been set");

        if( value == null )
            throw new AbortProcessingException("@value has not been set");
        
        FacesContext ctx = FacesContext.getCurrentInstance();
        
        if( ctx == null )
            throw new AbortProcessingException("FacesContext ctx is null");
        
        ELContext ectx = ctx.getELContext();
        
        if( ectx == null )
            throw new AbortProcessingException("ELContext ectx is null");
        
        target.setValue(ectx, value.getValue(ectx));
        
    }

    public Object saveState(FacesContext context)
    {
        Object[] state = new Object[2];
        state[0] = target;
        state[1] = value;
        return state;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object[] values = new Object[2];
        target = (ValueExpression) values[0];
        value = (ValueExpression) values[1];
    }

    public boolean isTransient()
    {
        return _transient;
    }

    public void setTransient(boolean _transient)
    {
        this._transient = _transient;
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

}
