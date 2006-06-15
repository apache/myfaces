package javax.faces.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

/**
 * @author Dennis Byrne
 * @since 1.2
 */

public abstract class UIComponentClassicTagBase extends UIComponentTagBase
        implements JspIdConsumer, BodyTag
{

    private UIComponent component ;
    
    private FacesContext ctx;
    
    protected PageContext pageContext ;
    
    private Tag parent ;
    
    private ResponseWriter responseWriter;
    
    private String id;
    
    private BodyContent bodyContent;
    
    private String jspId;
    
    private List<String> children ;
    
    private List<String> facets ;
    
    private boolean created ;
    
    private static final String VERBATIM_COMP_TYPE = "javax.faces.HtmlOutputText";
    
    public void doInitBody() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public int doAfterBody() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }
    
    protected String getFacesJspId()
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void setJspId(String id)
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected UIComponent findComponent(FacesContext context)
            throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public static UIComponentClassicTagBase getParentUIComponentClassicTagBase(
            PageContext context)
    {
        throw new UnsupportedOperationException("1.2");
    }

    public int doStartTag() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public int doEndTag() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void release()
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected int getDoAfterBodyValue() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected UIComponent createVerbatimComponentFromBodyContent()
    {
        
        // the 'single exit point' rule made this look like perl
        
        if( bodyContent == null)
            return null;
        
        String bodyString = bodyContent.getString();
        
        if( bodyString == null) // nothing
            return null;
        
        bodyContent.clearBody();
        
        String trimmedBody = bodyString.trim();
        
        if( "".equals(trimmedBody) ) // whitespace
            return null;
        
        if( trimmedBody.startsWith("<!--") 
                && trimmedBody.endsWith("-->") ) // comments
            return null;
        
        UIOutput output = createVerbatimComponent();
        output.setValue(bodyString);
        
        return output;
    }
    
    /**
     * Helper method for addVerbatimBeforeComponent and addVerbatimAfterComponent.
     */
    
    private void addVerbatimComponent(
            UIComponentClassicTagBase parentTag, 
            UIComponent verbatim,
            UIComponent component, int shift){
        
        if( component == null )
            throw new NullPointerException("UIComponent");
        
        UIComponent parent = component.getParent();
        
        if( parent != null ){
            
            List<UIComponent> brood = parent.getChildren();
            
            if( ! brood.contains(component) )
                throw new IllegalStateException("Child component can find parent, but parent does not claim child ");
            
            brood.add(brood.indexOf(component) + shift, verbatim);
            
        }else if( parent == null && ! ( component instanceof UIViewRoot ) )
            throw new NullPointerException("Could not find parent ");
        
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
    
    protected UIOutput createVerbatimComponent()
    {
        FacesContext _ctx = getFacesContext();
        
        if( _ctx == null )
            throw new NullPointerException("FacesContext");
        
        Application app = _ctx.getApplication();
        
        if( app == null )
            throw new NullPointerException("Application");
        
        UIViewRoot root = _ctx.getViewRoot();
        
        if( root == null )
            throw new NullPointerException("UIViewRoot");    
        
        UIComponent component = app.createComponent(VERBATIM_COMP_TYPE);
        component.setTransient(false);
        component.getAttributes().put("escape", Boolean.FALSE);
        component.setId(root.createUniqueId());
        
        return (UIOutput)component;
    }
    
    protected FacesContext getFacesContext()
    {
        return ctx != null ? ctx : FacesContext.getCurrentInstance();
    }

    protected String getFacetName()
    {
        if( parent == null || ! ( parent instanceof FacetTag ) )
            return null;
        else
            return ((FacetTag)parent).getName();
    }
    
    public boolean getCreated()
    {
        return created;
    }
    
    protected int getIndexOfNextChildTag()
    {
        return getChildren().size();
    }
    
    protected List<String> getCreatedComponents()
    {
        return children;
    }
    
    public JspWriter getPreviousOut()
    {
        if( bodyContent == null)
            throw new NullPointerException("BodyContent");
        
        return bodyContent.getEnclosingWriter();
    }
    
    private List<String> getChildren(){
        if( children == null)
            children = new ArrayList<String>();
        return children;
    }
    
    protected void addChild(UIComponent child)
    {
        getChildren().add(child.getId());
    }

    private List<String> getFacets(){
        if( facets == null)
            facets = new ArrayList<String>();
        return facets;
    }
    
    protected void addFacet(String name)
    {
        getFacets().add(name);
    }
    
    public void setBodyContent(BodyContent bodyContent)
    {
        this.bodyContent = bodyContent;
    }
    
    protected int getDoStartValue() throws JspException
    {
        return EVAL_BODY_BUFFERED;
    }

    protected int getDoEndValue() throws JspException
    {
        return EVAL_PAGE;
    }

    protected void encodeBegin() throws IOException
    {
        if( component == null )
            throw new NullPointerException("UIComponent component");
        
        component.encodeBegin(ctx);
    }

    protected void encodeChildren() throws IOException
    {
        if( component == null )
            throw new NullPointerException("UIComponent component");
        
        component.encodeChildren(ctx);
    }

    protected void encodeEnd() throws IOException
    {
        if( component == null )
            throw new NullPointerException("UIComponent component");
        
        component.encodeEnd(ctx);
    }

    public void setPageContext(PageContext pageContext)
    {
        this.pageContext = pageContext;
    }

    public Tag getParent()
    {
        return parent;
    }

    public void setParent(Tag parent)
    {
        this.parent = parent;
    }

    protected void setupResponseWriter()
    {
        // intentional no-op
    }

    public BodyContent getBodyContent()
    {
        return bodyContent;
    }

    public void setId(String id)
    {
        if( id != null && id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX) )
            throw new IllegalArgumentException("This is not a bug - @id may not begin w/ " + UIViewRoot.UNIQUE_ID_PREFIX);
        
        this.id = id;
    }

    protected String getId()
    {
        return id;
    }

    public String getJspId()
    {
        return jspId;
    }

    protected abstract void setProperties(UIComponent component);

    protected abstract UIComponent createComponent(FacesContext context,
            String newId) throws JspException;

    protected abstract boolean hasBinding();

    public UIComponent getComponentInstance()
    {
        return component;
    }

}
