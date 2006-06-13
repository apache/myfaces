package javax.faces.webapp;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
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

    protected int getDoStartValue() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected int getDoEndValue() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected void encodeBegin() throws IOException
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected void encodeChildren() throws IOException
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected void encodeEnd() throws IOException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void setPageContext(PageContext pageContext)
    {

        throw new UnsupportedOperationException("1.2");

    }

    public Tag getParent()
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void setParent(Tag parent)
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected void setupResponseWriter()
    {
        throw new UnsupportedOperationException("1.2");
    }

    private UIComponent createChild(FacesContext context, UIComponent parent,
            String componentId) throws JspException
    {
        throw new UnsupportedOperationException("1.2");

    }

    private UIComponent createFacet(FacesContext context, UIComponent parent,
            String name, String newId) throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    private UIComponent getChild(UIComponent component, String componentId)
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

    protected int getIndexOfNextChildTag()
    {

        throw new UnsupportedOperationException("1.2");
    }

    protected void addChild(UIComponent child)
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected void addFacet(String name)
    {
        throw new UnsupportedOperationException("1.2");
    }

    private void popUIComponentClassicTagBase()
    {
        throw new UnsupportedOperationException("1.2");
    }

    private void pushUIComponentClassicTagBase()
    {
        throw new UnsupportedOperationException("1.2");

    }

    private void removeOldChildren()
    {
        throw new UnsupportedOperationException("1.2");
    }

    private void removeOldFacets()
    {

        throw new UnsupportedOperationException("1.2");
    }

    protected UIComponent createVerbatimComponentFromBodyContent()
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected UIOutput createVerbatimComponent()
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected void addVerbatimBeforeComponent(
            UIComponentClassicTagBase parentTag, UIComponent verbatim,
            UIComponent component)
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected void addVerbatimAfterComponent(
            UIComponentClassicTagBase parentTag, UIComponent verbatim,
            UIComponent component)
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

    public void setBodyContent(BodyContent bodyContent)
    {
        throw new UnsupportedOperationException("1.2");
    }

    public JspWriter getPreviousOut()
    {
        throw new UnsupportedOperationException("1.2");
    }

    public BodyContent getBodyContent()
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void doInitBody() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public int doAfterBody() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void setId(String id)
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected String getId()
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected String getFacesJspId()
    {
        throw new UnsupportedOperationException("1.2");
    }

    private boolean isDuplicateId(String componentId)
    {
        throw new UnsupportedOperationException("1.2");
    }

    private String generateIncrementedId(String componentId)
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected List<String> getCreatedComponents()
    {
        throw new UnsupportedOperationException("1.2");
    }

    private String createId() throws JspException
    {
        throw new UnsupportedOperationException("1.2");
    }

    public void setJspId(String id)
    {
        throw new UnsupportedOperationException("1.2");
    }

    private void updatePreviousJspIdAndIteratorStatus(String id)
    {
        throw new UnsupportedOperationException("1.2");
    }

    private boolean isIncludedOrForwarded()
    {
        throw new UnsupportedOperationException("1.2");
    }

    public String getJspId()
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected abstract void setProperties(UIComponent component);

    protected abstract UIComponent createComponent(FacesContext context,
            String newId) throws JspException;

    protected abstract boolean hasBinding();

    public UIComponent getComponentInstance()
    {
        throw new UnsupportedOperationException("1.2");

    }

    public boolean getCreated()
    {
        throw new UnsupportedOperationException("1.2");
    }

    protected FacesContext getFacesContext()
    {
        throw new UnsupportedOperationException("1.2");

    }

    protected String getFacetName()
    {
        throw new UnsupportedOperationException("1.2");
    }

}
