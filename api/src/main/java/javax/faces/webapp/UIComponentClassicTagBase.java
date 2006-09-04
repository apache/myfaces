package javax.faces.webapp;

import java.util.Stack;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.Tag;

/**
 * @see http://www.onjava.com/pub/a/onjava/2004/06/09/jsf.html
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentClassicTagBase extends UIComponentTagBase
        implements BodyTag, IterationTag, JspIdConsumer, JspTag, Tag
{
    
    //  do not change this w/out doing likewise in UIComponentTag
    private static final String COMPONENT_STACK_ATTR = "org.apache.myfaces.COMPONENT_STACK";
    
    private boolean created;
    
    private String jspId;
    
    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getCreated()
     */

    public boolean getCreated()
    {
        return created;
    }
    
    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getParentUIComponentClassicTagBase(javax.servlet.jsp.PageContext)
     * @param pageContext
     * @return
     */

    public static UIComponentClassicTagBase getParentUIComponentClassicTagBase(
            PageContext pageContext)
    {
        Stack stack = getStack(pageContext);

        int size = stack.size();

        return size > 1 ? (UIComponentClassicTagBase) stack.get(size - 1)
                : null;
    }
    
    private static final Stack getStack(PageContext pageContext)
    {
        Stack stack = (Stack) pageContext.getAttribute(COMPONENT_STACK_ATTR,
                PageContext.REQUEST_SCOPE);

        if (stack == null)
        {
            stack = new Stack();
            pageContext.setAttribute(COMPONENT_STACK_ATTR,
                    stack, PageContext.REQUEST_SCOPE);
        }

        return stack;
    }
    
    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getFacesJspId()
     * @return
     */

    public String getJspId()
    {
        return jspId;
    }
    
}
