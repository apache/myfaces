package javax.faces.component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;

import javax.faces.context.FacesContext;
import javax.faces.FacesException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.apache.myfaces.TestRunner;
import org.easymock.EasyMock;

/**
     * Tests for
 * {@link UIComponent#invokeOnComponent(javax.faces.context.FacesContext, String, ContextCallback)}.
 */
public class UIComponentInvokeOnComponentTest extends UIComponentTestBase
{
    private UIComponent _testimpl;
    private ContextCallback _contextCallback;

    @Override
    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        Collection<Method> mockedMethods = new ArrayList<Method>();
        Class<UIComponent> clazz = UIComponent.class;
        mockedMethods.add(clazz.getDeclaredMethod("getClientId", new Class[] { FacesContext.class }));
        mockedMethods.add(clazz.getDeclaredMethod("getFacetsAndChildren", null));

        _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
        _contextCallback = _mocksControl.createMock(ContextCallback.class);
        _mocksControl.checkOrder(true);
    }

    @Test
    public void testInvokeOnComponentWithSameClientId() throws Exception
    {
        EasyMock.expect(_testimpl.getClientId(EasyMock.same(_facesContext))).andReturn("xxxId");
        _contextCallback.invokeContextCallback(EasyMock.same(_facesContext), EasyMock.same(_testimpl));
        _mocksControl.replay();
        Assert.assertTrue(_testimpl.invokeOnComponent(_facesContext, "xxxId", _contextCallback));
        _mocksControl.verify();
    }

    @Test
    public void testInvokeOnComponentWithException() throws Exception
    {
        EasyMock.expect(_testimpl.getClientId(EasyMock.same(_facesContext))).andReturn("xxxId");
        _contextCallback.invokeContextCallback(EasyMock.same(_facesContext), EasyMock.same(_testimpl));
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        _mocksControl.replay();
        org.apache.myfaces.Assert.assertException(FacesException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                Assert.assertTrue(_testimpl.invokeOnComponent(_facesContext, "xxxId", _contextCallback));
            }
        });
    }

    @Test
    public void testInvokeOnComponentAndNotFindComponentWithClientId() throws Exception
    {
        EasyMock.expect(_testimpl.getClientId(EasyMock.same(_facesContext))).andReturn("xxxId");
        EasyMock.expect(_testimpl.getFacetsAndChildren()).andReturn(Collections.EMPTY_LIST.iterator());
        _mocksControl.replay();
        Assert.assertFalse(_testimpl.invokeOnComponent(_facesContext, "xxId", _contextCallback));
        _mocksControl.verify();
    }

    @Test
    public void testInvokeOnComponentOnChild() throws Exception
    {
        EasyMock.expect(_testimpl.getClientId(EasyMock.same(_facesContext))).andReturn("xxxId");
        String childId = "childId";
        UIComponent child = _mocksControl.createMock(UIComponent.class);
        EasyMock.expect(_testimpl.getFacetsAndChildren()).andReturn(Collections.singletonList(child).iterator());
        EasyMock.expect(child.invokeOnComponent(EasyMock.same(_facesContext), EasyMock.eq(childId), EasyMock.same(_contextCallback))).andReturn(true);
        _mocksControl.replay();
        Assert.assertTrue(_testimpl.invokeOnComponent(_facesContext, "childId", _contextCallback));
        _mocksControl.verify();
    }

    @Test
    public void testInvokeOnComponentExceptions() throws Exception
    {
        org.apache.myfaces.Assert.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.invokeOnComponent(null, "xxx", _contextCallback);
            }
        });
        org.apache.myfaces.Assert.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.invokeOnComponent(_facesContext, null, _contextCallback);
            }
        });
        org.apache.myfaces.Assert.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.invokeOnComponent(_facesContext, "xxx", null);
            }
        });
    }
}
