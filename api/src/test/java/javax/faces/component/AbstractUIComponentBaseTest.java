package javax.faces.component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.ArrayList;

import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

import org.easymock.classextension.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.testng.annotations.BeforeMethod;

/**
 * Created by IntelliJ IDEA.
* User: mathias
* Date: 18.03.2007
* Time: 01:22:02
* To change this template use File | Settings | File Templates.
*/
public abstract class AbstractUIComponentBaseTest
{
    protected UIComponentBase _testImpl;
    protected IMocksControl _mocksControl;
    protected FacesContext _facesContext;
    protected Renderer _renderer;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _facesContext = _mocksControl.createMock(FacesContext.class);
        _testImpl = _mocksControl.createMock(UIComponentBase.class, getMockedMethodsArray());
        _renderer = _mocksControl.createMock(Renderer.class);
    }

    protected final Method[] getMockedMethodsArray() throws Exception
    {
        Collection<Method> mockedMethods = getMockedMethods();
        return mockedMethods.toArray(new Method[mockedMethods.size()]);
    }

    protected Collection<Method> getMockedMethods() throws Exception
    {
        Collection<Method> methods = new ArrayList<Method>();
        methods.add(UIComponentBase.class.getDeclaredMethod("getRenderer", new Class[]{FacesContext.class}));
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getParent", null));
        methods.add(UIComponentBase.class
                .getDeclaredMethod("getPathToComponent", new Class[]{UIComponent.class}));

        return methods;
    }
}
