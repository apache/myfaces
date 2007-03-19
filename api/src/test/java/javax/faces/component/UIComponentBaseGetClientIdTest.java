package javax.faces.component;

import javax.faces.FacesException;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:42:55
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentBaseGetClientIdTest extends AbstractUIComponentBaseTest
{
    @Test(expectedExceptions = {NullPointerException.class})
    public void testNullFacesContext() throws Exception
    {
        _testImpl.getClientId(null);
    }

    @Test
    public void testWithoutParentAndNoRenderer() throws Exception
    {
        String expectedClientId = "testId";
        _testImpl.setId(expectedClientId);
        expect(_testImpl.getParent()).andReturn(null);
        expect(_testImpl.getRenderer(EasyMock.same(_facesContext))).andReturn(null);
        _mocksControl.replay();
        assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
        _mocksControl.verify();
        assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
    }

    @Test
    public void testWithRenderer() throws Exception
    {
        String id = "testId";
        String expectedClientId = "convertedClientId";
        _testImpl.setId(id);
        expect(_testImpl.getParent()).andReturn(null);
        expect(_testImpl.getRenderer(EasyMock.same(_facesContext))).andReturn(_renderer);
        expect(_renderer.convertClientId(EasyMock.same(_facesContext), EasyMock.eq(id))).andReturn(expectedClientId);
        _mocksControl.replay();
        assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
        _mocksControl.verify();
        assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
    }

    @Test
    public void testWithParentNamingContainer() throws Exception
    {
        String id = "testId";
        String containerClientId = "containerClientId";
        String expectedClientId = containerClientId + NamingContainer.SEPARATOR_CHAR + id;
        UIComponent parent = _mocksControl.createMock(UIComponent.class);
        UIComponent namingContainer = _mocksControl.createMock(TestNamingContainerComponent.class);
        _testImpl.setId(id);
        expect(_testImpl.getParent()).andReturn(parent);
        expect(parent.getParent()).andReturn(namingContainer);
        expect(namingContainer.getContainerClientId(EasyMock.same(_facesContext))).andReturn(containerClientId);

        expect(_testImpl.getRenderer(EasyMock.same(_facesContext))).andReturn(_renderer);
        expect(_renderer.convertClientId(EasyMock.same(_facesContext), EasyMock.eq(expectedClientId))).andReturn(expectedClientId);
        _mocksControl.replay();
        assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
        _mocksControl.verify();
        assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
    }

    @Test
    public void testWithParentNamingContainerChanging() throws Exception
    {
        String id = "testId";
        String containerClientId = "containerClientId";
        UIComponent parent = _mocksControl.createMock(UIComponent.class);
        UIComponent namingContainer = _mocksControl.createMock(TestNamingContainerComponent.class);
        for (int i = 0; i < 10; i++)
        {
            _testImpl.setId(id);
            String expectedClientId = containerClientId + i + NamingContainer.SEPARATOR_CHAR + id;
            expect(_testImpl.getParent()).andReturn(parent);
            expect(parent.getParent()).andReturn(namingContainer);
            expect(namingContainer.getContainerClientId(EasyMock.same(_facesContext))).andReturn(containerClientId + i);

            expect(_testImpl.getRenderer(EasyMock.same(_facesContext))).andReturn(_renderer);
            expect(_renderer.convertClientId(EasyMock.same(_facesContext), EasyMock.eq(expectedClientId)))
                    .andReturn(expectedClientId);
            _mocksControl.replay();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
            _mocksControl.verify();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
            _mocksControl.reset();
        }
    }

    @Test
    public void testWithoutId() throws Exception
    {
        UIViewRoot viewRoot = _mocksControl.createMock(UIViewRoot.class);
        expect(_facesContext.getViewRoot()).andReturn(viewRoot);
        String expectedId = "uniqueId";
        expect(viewRoot.createUniqueId()).andReturn(expectedId);
        expect(_testImpl.getParent()).andReturn(null).anyTimes();
        expect(_testImpl.getRenderer(EasyMock.same(_facesContext))).andReturn(null);
        _mocksControl.replay();
        assertEquals(expectedId, _testImpl.getClientId(_facesContext));
        assertEquals(expectedId, _testImpl.getId());
        _mocksControl.verify();
        assertEquals(expectedId, _testImpl.getClientId(_facesContext));
    }

    @Test(expectedExceptions = {FacesException.class})
    public void testWithoutIdAndNoUIViewRoot() throws Exception
    {
        expect(_testImpl.getParent()).andReturn(null).anyTimes();
        expect(_facesContext.getViewRoot()).andReturn(null);
        _mocksControl.replay();
        _testImpl.getClientId(_facesContext);
    }

    public abstract static class TestNamingContainerComponent extends UIComponent implements NamingContainer
    {
    }

}
