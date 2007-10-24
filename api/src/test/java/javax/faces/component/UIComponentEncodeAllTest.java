package javax.faces.component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.apache.myfaces.TestRunner;
import org.apache.myfaces.Assert;
import org.easymock.EasyMock;

/**
     * Tests for {@link UIComponent#encodeAll(javax.faces.context.FacesContext)}.
 */
public class UIComponentEncodeAllTest extends UIComponentTestBase
{
    private UIComponent _testimpl;

    @Override
    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        Collection<Method> mockedMethods = new ArrayList<Method>();
        Class<UIComponent> clazz = UIComponent.class;
        mockedMethods.add(clazz.getDeclaredMethod("isRendered", null));
        mockedMethods.add(clazz.getDeclaredMethod("encodeBegin", new Class[] { FacesContext.class }));
        mockedMethods.add(clazz.getDeclaredMethod("getRendersChildren", null));
        mockedMethods.add(clazz.getDeclaredMethod("encodeChildren", new Class[] { FacesContext.class }));
        mockedMethods.add(clazz.getDeclaredMethod("getChildren", null));
        mockedMethods.add(clazz.getDeclaredMethod("getChildCount", null));
        mockedMethods.add(clazz.getDeclaredMethod("encodeEnd", new Class[] { FacesContext.class }));

        _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
        _mocksControl.checkOrder(true);
    }

    @Test
    public void testEncodeAllNullContext() throws Exception
    {
        Assert.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.encodeAll(null);
            }
        });
    }

    @Test
    public void testEncodeAllNotRendered() throws Exception
    {
        EasyMock.expect(_testimpl.isRendered()).andReturn(false);
        _mocksControl.replay();
        _testimpl.encodeAll(_facesContext);
        _mocksControl.verify();
    }

    @Test
    public void testEncodeAllRenderesChildren() throws Exception
    {
        EasyMock.expect(_testimpl.isRendered()).andReturn(true);
        _testimpl.encodeBegin(EasyMock.same(_facesContext));
        EasyMock.expect(_testimpl.getRendersChildren()).andReturn(true);
        _testimpl.encodeChildren(EasyMock.same(_facesContext));
        _testimpl.encodeEnd(EasyMock.same(_facesContext));
        _mocksControl.replay();
        _testimpl.encodeAll(_facesContext);
        _mocksControl.verify();
    }

    @Test
    public void testEncodeAllNotRenderesChildren() throws Exception
    {
        EasyMock.expect(_testimpl.isRendered()).andReturn(true);
        _testimpl.encodeBegin(EasyMock.same(_facesContext));
        EasyMock.expect(_testimpl.getRendersChildren()).andReturn(false);

        List<UIComponent> childs = new ArrayList<UIComponent>();
        UIComponent testChild = _mocksControl.createMock(UIComponent.class);
        childs.add(testChild);
        EasyMock.expect(_testimpl.getChildCount()).andReturn(childs.size());        
        EasyMock.expect(_testimpl.getChildren()).andReturn(childs);
        testChild.encodeAll(EasyMock.same(_facesContext));

        _testimpl.encodeEnd(EasyMock.same(_facesContext));
        _mocksControl.replay();
        _testimpl.encodeAll(_facesContext);
        _mocksControl.verify();
    }
}
