package javax.faces.component;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.el.ValueExpression;
import javax.faces.el.ValueBinding;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:22:43
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentBaseValueBindingTest extends AbstractUIComponentBaseTest
{
    private ValueBinding _valueBinding;

    @Override
    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        _valueBinding = _mocksControl.createMock(ValueBinding.class);
    }

    @Override
    protected Collection<Method> getMockedMethods() throws Exception
    {
        Collection<Method> mockedMethods = super.getMockedMethods();
        mockedMethods.add(UIComponent.class.getDeclaredMethod("getValueExpression", new Class[]{String.class}));
        mockedMethods.add(UIComponent.class.getDeclaredMethod("setValueExpression", new Class[]{String.class,
                ValueExpression.class}));
        return mockedMethods;
    }

    @Test
    public void testGetValueBindingWOValueExpression() throws Exception
    {
        expect(_testImpl.getValueExpression(EasyMock.eq("xxx"))).andReturn(null);
        _mocksControl.replay();
        assertNull(_testImpl.getValueBinding("xxx"));
    }

    @Test
    public void testSetValueBinding() throws Exception
    {
        _testImpl.setValueExpression(EasyMock.eq("xxx"), EasyMock.isA(_ValueBindingToValueExpression.class));
        expectLastCall().andAnswer(new IAnswer<Object>()
        {
            public Object answer() throws Throwable
            {
                _ValueBindingToValueExpression ve = (_ValueBindingToValueExpression) EasyMock.getCurrentArguments()[1];
                assertEquals(_valueBinding, ve.getValueBinding());
                return null;
            }
        });
        _mocksControl.replay();
        _testImpl.setValueBinding("xxx", _valueBinding);
    }

    @Test
    public void testSetValueBindingWNullValue() throws Exception
    {
        _testImpl.setValueExpression(EasyMock.eq("xxx"), (ValueExpression) EasyMock.isNull());
        _mocksControl.replay();
        _testImpl.setValueBinding("xxx", null);
    }

    @Test
    public void testGetValueBindingWithVBToVE() throws Exception
    {
        ValueExpression valueExpression = new _ValueBindingToValueExpression(_valueBinding);
        expect(_testImpl.getValueExpression(EasyMock.eq("xxx"))).andReturn(valueExpression);
        _mocksControl.replay();
        assertEquals(_valueBinding, _testImpl.getValueBinding("xxx"));
    }

    @Test
    public void testGetValueBindingFromVE() throws Exception
    {
        ValueExpression valueExpression = _mocksControl.createMock(ValueExpression.class);
        expect(_testImpl.getValueExpression(EasyMock.eq("xxx"))).andReturn(valueExpression);
        _mocksControl.replay();
        ValueBinding valueBinding = _testImpl.getValueBinding("xxx");
        assertNotNull(valueBinding);
        assertTrue(valueBinding instanceof _ValueExpressionToValueBinding);
        assertEquals(valueExpression, ((_ValueExpressionToValueBinding) valueBinding).getValueExpression());
    }
}
