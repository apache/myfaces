package javax.faces.component;

import javax.el.ValueExpression;

public class UIOutcomeTarget extends UIOutput
{
    public static final String COMPONENT_TYPE = "javax.faces.OutcomeTarget";
    public static final String COMPONENT_FAMILY = "javax.faces.UIOutcomeTarget";
    
    private static final boolean DEFAULT_INCLUDEVIEWPARAMS = false;
    
    private String _outcome;
    private boolean _includeViewParams;
    
    public UIOutcomeTarget()
    {
        super();
        setRendererType("javax.faces.Link");
    }
    
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public String getOutcome()
    {
        if (_outcome != null)
        {
            return _outcome;
        }
        
        ValueExpression expression = getValueExpression("Outcome");
        if (expression != null)
        {
            return (String) expression.getValue(getFacesContext().getELContext());
        }
        
        if(isInView())  //default to the view id
        {
            return getFacesContext().getViewRoot().getViewId();
        }
        
        return _outcome;
    }

    public void setOutcome(String outcome)
    {
        _outcome = outcome;
    }

    public boolean isIncludeViewParams()
    {        
        return getExpressionValue("includePageParams", _includeViewParams, DEFAULT_INCLUDEVIEWPARAMS);
    }

    public void setIncludeViewParams(boolean includeViewParams)
    {
        _includeViewParams = includeViewParams;
    }

    
}
