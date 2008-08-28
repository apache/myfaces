package javax.faces.component;
import static org.easymock.EasyMock.expect;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for {@link UIComponent#getValueExpression(String)}. and
 * {@link UIComponent#setValueExpression(String, javax.el.ValueExpression)}.
 */
public class UIComponentValueExpressionTest extends UIComponentTestBase
{
    private UIComponent _testimpl;
    private ValueExpression _expression;
    private ELContext _elContext;

    @Override
    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        Collection<Method> mockedMethods = new ArrayList<Method>();
        Class<UIComponent> clazz = UIComponent.class;
        mockedMethods.add(clazz.getDeclaredMethod("getAttributes", null));
        mockedMethods.add(clazz.getDeclaredMethod("getFacesContext", null));
        mockedMethods.add(clazz.getDeclaredMethod("getValueBinding", new Class[] { String.class }));

        _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
        _expression = _mocksControl.createMock(ValueExpression.class);
        _elContext = _mocksControl.createMock(ELContext.class);
        _mocksControl.checkOrder(true);
    }

    @Test(expectedExceptions = { NullPointerException.class })
    public void testValueExpressionArgumentNPE() throws Exception
    {
        _testimpl.setValueExpression(null, _expression);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testValueExpressionArgumentId() throws Exception
    {
        _testimpl.setValueExpression("id", _expression);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testValueExpressionArgumentsParent() throws Exception
    {
        _testimpl.setValueExpression("parent", _expression);
    }

    @Test
    public void testValueExpression() throws Exception
    {
        expect(_expression.isLiteralText()).andReturn(false);
        _mocksControl.replay();
        _testimpl.setValueExpression("xxx", _expression);
        _mocksControl.verify();
        assertEquals(_expression, _testimpl.getValueExpression("xxx"));
        _testimpl.setValueExpression("xxx", null);
        _mocksControl.verify();

        assertNull(_testimpl.getValueExpression("xxx"));
        assertNull(_testimpl.bindings);
    }

    @Test(expectedExceptions = { FacesException.class })
    public void testValueExpressionWithExceptionOnGetValue() throws Exception
    {
        expect(_expression.isLiteralText()).andReturn(true);
        expect(_testimpl.getFacesContext()).andReturn(_facesContext);
        expect(_facesContext.getELContext()).andReturn(_elContext);
        expect(_expression.getValue(EasyMock.eq(_elContext))).andThrow(new ELException());
        Map map = new HashMap();
        expect(_testimpl.getAttributes()).andReturn(map);
        _mocksControl.replay();
        _testimpl.setValueExpression("xxx", _expression);
    }

    @Test
    public void testValueExpressionWithLiteralText() throws Exception
    {
        expect(_expression.isLiteralText()).andReturn(true);
        expect(_testimpl.getFacesContext()).andReturn(_facesContext);
        expect(_facesContext.getELContext()).andReturn(_elContext);
        expect(_expression.getValue(EasyMock.eq(_elContext))).andReturn("abc");
        Map map = new HashMap();
        expect(_testimpl.getAttributes()).andReturn(map);
        _mocksControl.replay();
        _testimpl.setValueExpression("xxx", _expression);
        assertEquals("abc", map.get("xxx"));
        _mocksControl.verify();
        assertNull(_testimpl.getValueExpression("xxx"));
    }

    @Test
    public void testValueExpressionWithValueBindingFallback() throws Exception
    {
        ValueBinding valueBinding = _mocksControl.createMock(ValueBinding.class);
        expect(_testimpl.getValueBinding("xxx")).andReturn(valueBinding);
        _mocksControl.replay();
        ValueExpression valueExpression = _testimpl.getValueExpression("xxx");
        _mocksControl.verify();
        assertTrue(valueExpression instanceof _ValueBindingToValueExpression);
        _mocksControl.reset();
        expect(_elContext.getContext(EasyMock.eq(FacesContext.class))).andReturn(_facesContext);
        expect(valueBinding.getValue(EasyMock.eq(_facesContext))).andReturn("value");
        _mocksControl.replay();
        assertEquals("value", valueExpression.getValue(_elContext));
        _mocksControl.verify();
    }
}
