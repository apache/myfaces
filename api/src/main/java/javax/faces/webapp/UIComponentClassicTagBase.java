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
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @see http://www.onjava.com/pub/a/onjava/2004/06/09/jsf.html
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentClassicTagBase extends UIComponentTagBase
        implements BodyTag, IterationTag, JspIdConsumer, JspTag, Tag
{

    private static final Log log = LogFactory
            .getLog(UIComponentClassicTagBase.class);

    // do not change this w/out doing likewise in UIComponentTag
    private static final String COMPONENT_STACK_ATTR = "org.apache.myfaces.COMPONENT_STACK";

    private static final String VERBATIM_COMP_TYPE = "javax.faces.HtmlOutputText";

    private static final boolean DEFAULT_CREATED = false;
    
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

    private boolean created = DEFAULT_CREATED;
    
    private String facesJspId;

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#setJspId(java.lang.String)
     */

    public void setJspId(String id)
    {
        throw new UnsupportedOperationException("1.2");
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#doEndTag()
     */

    public int doEndTag() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#doStartTag()
     */

    public int doStartTag() throws JspException
    {
        getStack(pageContext).add(this); // push 

        throw new UnsupportedOperationException("1.2");
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#findComponent(javax.faces.context.FacesContext)
     * @param context
     * @return
     * @throws JspException
     */

    protected UIComponent findComponent(FacesContext context)
            throws JspException
    {

        UIComponent foundComponent = null;

        if (component != null)
        {
            if (log.isDebugEnabled())
                log.debug("JSF 1.2 SPEC: If we have previously " +
                                "located this component, return it.");

            foundComponent = component;

        }
        else
        {

            FacesContext ctx = getFacesContext();
            
            if (log.isDebugEnabled())
                log.debug("Locate the parent component by looking " +
                                "for a parent UIComponentTag instance");

            UIComponentClassicTagBase parentTag = getParentUIComponentClassicTagBase(pageContext);

            if (parentTag == null)
            {
                if (log.isDebugEnabled())
                    log.debug(" If there is no parent UIComponentTag " +
                                    "instance, this tag represents the root component, " +
                                    "so get it from the current Tree and return it.");

                foundComponent = getFacesContext().getViewRoot();
            }
            else
            {

                if (log.isDebugEnabled())
                    log.debug(" and ask it for its component");

                UIComponent componentOfParent = parentTag
                        .getComponentInstance();

                if (componentOfParent == null)
                    throw new NullPointerException(
                            "Could not locate the component of parent tag "
                                    + parentTag.getId());

                String facetName = getFacetName();

                if (facetName != null)
                {

                    if (log.isDebugEnabled())
                        log.debug("If this UIComponentTag instance has " +
                                        "the facetName attribute set, ask the parent " +
                                        "UIComponent for a facet with this name");

                    foundComponent = componentOfParent.getFacet(facetName);

                    if(foundComponent == null)
                    {
                        
                        if (log.isDebugEnabled())
                            log.debug("If not found, create one, call " +
                                            "setProperties() with the new component " +
                                            "as a parameter ");
                        
                        foundComponent = internalCreateComponent(this, ctx);

                        setProperties(foundComponent);
                        
                        componentOfParent.getFacets().put(facetName, foundComponent);
                        
                    }
                    
                }
                else
                {

                    

                }

                throw new UnsupportedOperationException("1.2");

            }

        }

        return foundComponent;

    }

    /**
     * http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#findComponent(javax.faces.context.FacesContext)
     * @return
     */

    private static UIComponent internalCreateComponent(UIComponentClassicTagBase tagBase, FacesContext ctx)
    {

        UIComponent component = null;
        
        if(log.isDebugEnabled())
            log.debug("JSF Spec 1.2 : When creating a component, the process is:" +
                    "Retrieve the component type by calling UIComponentTagBase.getComponentType()");
        
        String componentType = tagBase.getComponentType();
        
        if ( tagBase.hasBinding() )
        {
            
            throw new UnsupportedOperationException("1.2");
            
            //component = ctx.getApplication().createComponent(null , ctx, componentType);
            
        }
        else
        {
            
            component = ctx.getApplication().createComponent( componentType );
            
        }
        
        return component;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#addChild(javax.faces.component.UIComponent)
     */

    protected void addChild(UIComponent child)
    {
        getChildren().add(child.getId());
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#addFacet(java.lang.String)
     */

    protected void addFacet(String name)
    {
        getFacets().add(name);
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#addVerbatimAfterComponent(javax.faces.webapp.UIComponentClassicTagBase,%20javax.faces.component.UIComponent,%20javax.faces.component.UIComponent)
     * @param parentTag
     * @param verbatim
     * @param component
     */

    protected void addVerbatimAfterComponent(
            UIComponentClassicTagBase parentTag, UIComponent verbatim,
            UIComponent component)
    {
        addVerbatimComponent(parentTag, verbatim, component, 1);
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#addVerbatimBeforeComponent(javax.faces.webapp.UIComponentClassicTagBase,%20javax.faces.component.UIComponent,%20javax.faces.component.UIComponent)
     * @param parentTag
     * @param verbatim
     * @param component
     */

    protected void addVerbatimBeforeComponent(
            UIComponentClassicTagBase parentTag, UIComponent verbatim,
            UIComponent component)
    {
        addVerbatimComponent(parentTag, verbatim, component, 0);
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#createComponent(javax.faces.context.FacesContext,%20java.lang.String)
     * @param context
     * @param newId
     * @return
     * @throws JspException
     */

    protected abstract UIComponent createComponent(FacesContext context,
            String newId) throws JspException;

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#createVerbatimComponent()
     * @return
     */

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

        // TODO maybe it would be better to call addChild here?
        
        return (UIOutput) component;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#createVerbatimComponentFromBodyContent()
     * @return
     */

    protected UIComponent createVerbatimComponentFromBodyContent()
    {

        // the 'single exit point' rule made this look like perl

        if (bodyContent == null)
            return null;

        String bodyString = bodyContent.getString();

        if (log.isDebugEnabled())
            log.debug("JSF 1.2 SPEC : return null if there is no body content");

        if (bodyString == null)
            return null;

        bodyContent.clearBody();

        String trimmedBody = bodyString.trim();

        if (log.isDebugEnabled())
            log
                    .debug("JSF 1.2 SPEC: return null if ... the body content is whitespace");

        if ("".equals(trimmedBody))
            return null;

        if (log.isDebugEnabled())
            log
                    .debug("JSF 1.2 SPEC: return null if ... the body content is a comment");

        if (trimmedBody.startsWith("<!--") && trimmedBody.endsWith("-->"))
            return null;

        if (log.isDebugEnabled())
            log
                    .debug("JSF 1.2 SPEC : Create a transient UIOutput component from the body content ");

        UIOutput output = createVerbatimComponent();
        output.setValue(bodyString);

        return output;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#doAfterBody()
     */

    public int doAfterBody() throws JspException
    {
        if( log.isDebugEnabled() )
            log.debug("Perform any processing necessary " +
                    "to handle the content implications of CASE 4 " +
                    "(template text and/or non-component custom tag " +
                    "output occurring between the last child component " +
                    "tag and its enclosing parent component tag's end tag )");
        
        UIComponentClassicTagBase parent = getParentUIComponentClassicTagBase(pageContext);
        
        if( parent != null )
        {
            UIComponent componentOfParent = parent.getComponentInstance();
            
            if( componentOfParent != null && componentOfParent.getRendersChildren() )
            {
                UIComponent verbatimComponent = createVerbatimComponentFromBodyContent();
                
                if( verbatimComponent != null )
                {
                    
                    if(log.isDebugEnabled())
                        log.debug("adding child w/ id of " + verbatimComponent.getId());
                    
                    componentOfParent.getChildren().add(verbatimComponent);
                    addChild(verbatimComponent);
                }
                
            }
            
        }
        
        return getDoAfterBodyValue();
    }
    
    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#doInitBody()
     */

    public void doInitBody() throws JspException
    {
        // noop
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#encodeBegin()
     * @throws IOException
     */

    protected void encodeBegin() throws IOException
    {
        if (component == null)
            throw new NullPointerException("UIComponent component");

        component.encodeBegin(ctx);
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#encodeChildren()
     * @throws IOException
     */

    protected void encodeChildren() throws IOException
    {
        if (component == null)
            throw new NullPointerException("UIComponent component");

        component.encodeChildren(ctx);
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#encodeEnd()
     * @throws IOException
     */

    protected void encodeEnd() throws IOException
    {
        if (component == null)
            throw new NullPointerException("UIComponent component");

        component.encodeEnd(ctx);
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getBodyContent()
     * @return
     */

    public BodyContent getBodyContent()
    {
        return bodyContent;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getComponentInstance()
     */

    public UIComponent getComponentInstance()
    {
        return component;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getCreated()
     */

    public boolean getCreated()
    {
        return created;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getCreatedComponents()
     * @return
     */

    protected List<String> getCreatedComponents()
    {
        return children;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getDoEndValue()
     * @return
     * @throws JspException
     */

    protected int getDoEndValue() throws JspException
    {
        return EVAL_PAGE;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getDoAfterBodyValue()
     * @return
     * @throws JspException
     */

    protected int getDoAfterBodyValue() throws JspException
    {
        return SKIP_BODY;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getDoStartValue()
     * @return
     * @throws JspException
     */

    protected int getDoStartValue() throws JspException
    {
        return EVAL_BODY_BUFFERED;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getFacesContext()
     */

    protected FacesContext getFacesContext()
    {
        return ctx != null ? ctx : FacesContext.getCurrentInstance();
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getFacesJspId()
     * @return
     */
    
    protected String getFacesJspId()
    {
        if(facesJspId == null)
        {
            if( jspId != null )
                facesJspId = UIViewRoot.UNIQUE_ID_PREFIX + jspId;
            else
                facesJspId = ctx.getViewRoot().createUniqueId();
            
        }
        return facesJspId;
    }
    
    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getFacetName()
     * @return
     */

    protected String getFacetName()
    {
        if (parent == null || !(parent instanceof FacetTag))
            return null;
        else
            return ((FacetTag) parent).getName();
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getId()
     * @return
     */

    protected String getId()
    {
        return id;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getIndexOfNextChildTag()
     */

    protected int getIndexOfNextChildTag()
    {
        return getChildren().size();
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getFacesJspId()
     * @return
     */

    public String getJspId()
    {
        return jspId;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getParent()
     */

    public Tag getParent()
    {
        return parent;
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

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#getPreviousOut()
     * @return
     */

    public JspWriter getPreviousOut()
    {
        if (bodyContent == null)
            throw new NullPointerException("BodyContent");

        return bodyContent.getEnclosingWriter();
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#hasBinding()
     * @return
     */

    protected abstract boolean hasBinding();

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#release()
     */

    public void release()
    {
        component = null;
        ctx = null;
        pageContext = null;
        parent = null;
        responseWriter = null;
        id = null;
        bodyContent = null;
        jspId = null;
        children = null;
        facets = null;
        created = DEFAULT_CREATED;
        facesJspId = null;
    }
    
    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#setBodyContent(javax.servlet.jsp.tagext.BodyContent) 
     */

    public void setBodyContent(BodyContent bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#setId(java.lang.String)
     */

    public void setId(String id)
    {
        if (id != null && id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
            throw new IllegalArgumentException(
                    "This is not a bug - @id may not begin w/ "
                            + UIViewRoot.UNIQUE_ID_PREFIX);

        this.id = id;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#setPageContext(javax.servlet.jsp.PageContext)
     */

    public void setPageContext(PageContext pageContext)
    {
        this.pageContext = pageContext;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#setParent(javax.servlet.jsp.tagext.Tag)
     */

    public void setParent(Tag parent)
    {
        this.parent = parent;
    }

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#setProperties(javax.faces.component.UIComponent)
     * @param component
     */

    protected abstract void setProperties(UIComponent component);

    /**
     * @see http://java.sun.com/javaee/5/docs/api/javax/faces/webapp/UIComponentClassicTagBase.html#setupResponseWriter()
     */

    protected void setupResponseWriter()
    {
        // noop
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
