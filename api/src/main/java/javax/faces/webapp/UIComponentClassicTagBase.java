package javax.faces.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentClassicTagBase extends UIComponentTagBase
        implements JspIdConsumer, BodyTag
{

    private static final Log log = LogFactory
            .getLog(UIComponentClassicTagBase.class);

    // do not change this w/out doing likewise in UIComponentTag
    private static final String COMPONENT_STACK_ATTR = "org.apache.myfaces.COMPONENT_STACK";

    private static final String VERBATIM_COMP_TYPE = "javax.faces.HtmlOutputText";

    private UIComponent component;

    private FacesContext ctx;

    protected PageContext pageContext;

    private Tag parent;

    private ResponseWriter responseWriter;

    private String id;

    private BodyContent bodyContent;

    private String jspId;

    private List<String> children;

    private List<String> facets;

    private boolean created;

    protected void addChild(UIComponent child)
    {
        getChildren().add(child.getId());
    }

    protected void addFacet(String name)
    {
        getFacets().add(name);
    }

    protected void addVerbatimAfterComponent(
            UIComponentClassicTagBase parentTag, UIComponent verbatim,
            UIComponent component)
    {
        addVerbatimComponent(parentTag, verbatim, component, 1);
    }

    protected void addVerbatimBeforeComponent(
            UIComponentClassicTagBase parentTag, UIComponent verbatim,
            UIComponent component)
    {
        addVerbatimComponent(parentTag, verbatim, component, 0);
    }

    protected abstract UIComponent createComponent(FacesContext context,
            String newId) throws JspException;

    protected UIOutput createVerbatimComponent()
    {
        FacesContext _ctx = getFacesContext();

        if (_ctx == null)
            throw new NullPointerException("FacesContext");

        Application app = _ctx.getApplication();

        if (app == null)
            throw new NullPointerException("Application");

        UIViewRoot root = _ctx.getViewRoot();

        if (root == null)
            throw new NullPointerException("UIViewRoot");

        String uniqueId = root.createUniqueId();

        if (log.isDebugEnabled())
            log
                    .debug("JSF 1.2 SPEC: Use the Application instance to create a new component "
                            + "with the following characteristics: "
                            + "componentType is javax.faces.HtmlOutputText, "
                            + "transient is true, "
                            + "escape is false, "
                            + "id is FacesContext.getViewRoot().createUniqueId() "
                            + uniqueId);

        UIComponent component = app.createComponent(VERBATIM_COMP_TYPE);
        component.setTransient(true);
        component.getAttributes().put("escape", Boolean.FALSE);
        component.setId(uniqueId);

        return (UIOutput) component;
    }

    protected UIComponent createVerbatimComponentFromBodyContent()
    {

        // the 'single exit point' rule made this look like perl

        if (bodyContent == null)
            return null;

        String bodyString = bodyContent.getString();

        if (log.isDebugEnabled())
            log.debug("JSF 1.2 SPEC : return null if there is no body content");

        if (bodyString == null) // nothing
            return null;

        bodyContent.clearBody();

        String trimmedBody = bodyString.trim();

        if (log.isDebugEnabled())
            log
                    .debug("JSF 1.2 SPEC: return null if ... the body content is whitespace");

        if ("".equals(trimmedBody)) // whitespace
            return null;

        if (log.isDebugEnabled())
            log
                    .debug("JSF 1.2 SPEC: return null if ... the body content is a comment");

        if (trimmedBody.startsWith("<!--") && trimmedBody.endsWith("-->")) // comments
            return null;

        if (log.isDebugEnabled())
            log
                    .debug("JSF 1.2 SPEC : Create a transient UIOutput component from the body content ");

        UIOutput output = createVerbatimComponent();
        output.setValue(bodyString);

        return output;
    }

    public int doAfterBody() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public int doEndTag() throws JspException
    {
        throw new UnsupportedOperationException("1.2");

        // popTag();
    }

    public void doInitBody() throws JspException
    {
        // intentional noop
    }

    public int doStartTag() throws JspException
    {
        getStack(pageContext).add(this); // push 

        throw new UnsupportedOperationException("1.2");
    }

    protected void encodeBegin() throws IOException
    {
        if (component == null)
            throw new NullPointerException("UIComponent component");

        component.encodeBegin(ctx);
    }

    protected void encodeChildren() throws IOException
    {
        if (component == null)
            throw new NullPointerException("UIComponent component");

        component.encodeChildren(ctx);
    }

    protected void encodeEnd() throws IOException
    {
        if (component == null)
            throw new NullPointerException("UIComponent component");

        component.encodeEnd(ctx);
    }

    protected UIComponent findComponent(FacesContext context)
            throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public BodyContent getBodyContent()
    {
        return bodyContent;
    }

    public UIComponent getComponentInstance()
    {
        return component;
    }

    public boolean getCreated()
    {
        return created;
    }

    protected List<String> getCreatedComponents()
    {
        return children;
    }

    protected int getDoEndValue() throws JspException
    {
        return EVAL_PAGE;
    }

    protected int getDoStartValue() throws JspException
    {
        return EVAL_BODY_BUFFERED;
    }

    protected int getDoAfterBodyValue() throws JspException
    {
        return SKIP_BODY;
    }

    protected FacesContext getFacesContext()
    {
        return ctx != null ? ctx : FacesContext.getCurrentInstance();
    }

    protected String getFacesJspId()
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected String getFacetName()
    {
        if (parent == null || !(parent instanceof FacetTag))
            return null;
        else
            return ((FacetTag) parent).getName();
    }

    protected String getId()
    {
        return id;
    }

    protected int getIndexOfNextChildTag()
    {
        return getChildren().size();
    }

    public String getJspId()
    {
        return jspId;
    }

    public Tag getParent()
    {
        return parent;
    }

    public static UIComponentClassicTagBase getParentUIComponentClassicTagBase(
            PageContext pageContext)
    {
        Stack stack = getStack(pageContext);

        int size = stack.size();

        return size > 1 ? (UIComponentClassicTagBase) stack.get(size - 1)
                : null;
    }

    public JspWriter getPreviousOut()
    {
        if (bodyContent == null)
            throw new NullPointerException("BodyContent");

        return bodyContent.getEnclosingWriter();
    }

    protected abstract boolean hasBinding();

    public void release()
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void setBodyContent(BodyContent bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    public void setJspId(String id)
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void setId(String id)
    {
        if (id != null && id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
            throw new IllegalArgumentException(
                    "This is not a bug - @id may not begin w/ "
                            + UIViewRoot.UNIQUE_ID_PREFIX);

        this.id = id;
    }

    public void setPageContext(PageContext pageContext)
    {
        this.pageContext = pageContext;
    }

    public void setParent(Tag parent)
    {
        this.parent = parent;
    }

    protected abstract void setProperties(UIComponent component);

    protected void setupResponseWriter()
    {
        // intentional no-op
    }

    /**
     * Returns a request scoped stack, creating one if necessary.
     * 
     * @see UIComponentClassicTagBase.getStack()
     */

    private static final Stack getStack(PageContext pageContext)
    {
        Stack stack = (Stack) pageContext.getAttribute(COMPONENT_STACK_ATTR,
                PageContext.REQUEST_SCOPE);

        if (stack == null)
        {
            stack = new Stack();
            pageContext.setAttribute(COMPONENT_STACK_ATTR,
                    PageContext.REQUEST_SCOPE);
        }

        return stack;
    }

    /**
     * @see UIComponentTag.popTag
     */

    private void popTag()
    {
        Stack stack = getStack(pageContext);

        int size = stack.size();

        if (size == 0)
        {
            if (log.isWarnEnabled())
                log
                        .warn("If you can read this in your log files, "
                                + "there may be a bug in MyFaces. popTag() "
                                + "should not be called when there are no "
                                + "Tags to pop from the stack.  Please report "
                                + "this to a MyFaces mailing list or the issue tracker ");
        }
        else
            stack.pop();

        if (size <= 1)
            pageContext.removeAttribute(COMPONENT_STACK_ATTR,
                    PageContext.REQUEST_SCOPE);
    }

    /**
     * Helper method for addVerbatimBeforeComponent and addVerbatimAfterComponent.
     */

    private void addVerbatimComponent(UIComponentClassicTagBase parentTag,
            UIComponent verbatim, UIComponent component, int shift)
    {

        if (component == null)
            throw new NullPointerException("UIComponent");

        UIComponent parent = component.getParent();

        if (parent != null)
        {

            List<UIComponent> brood = parent.getChildren();

            if (!brood.contains(component))
                throw new IllegalStateException(
                        "Child component can find parent, but parent does not claim child ");

            if (log.isDebugEnabled())
                log.debug("adding verbatim component to list of UIComponents");

            brood.add(brood.indexOf(component) + shift, verbatim);

        }
        else if (parent == null && !(component instanceof UIViewRoot))
            throw new NullPointerException("Could not find parent ");

    }

    /**
     * Null safe method for facets.
     */

    private List<String> getFacets()
    {
        if (facets == null)
            facets = new ArrayList<String>();
        return facets;
    }

    /**
     * Null safe method for children.
     */

    private List<String> getChildren()
    {
        if (children == null)
            children = new ArrayList<String>();
        return children;
    }

}