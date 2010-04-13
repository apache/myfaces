package javax.faces.component;


/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:19:19
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentFindComponentTest extends AbstractComponentTest
{
    public UIComponentFindComponentTest(String arg0)
    {
        super(arg0);
    }
    
    protected UIComponentBase _testImpl;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _testImpl = new UIOutput();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testWithNullExperession() throws Exception
    {
        try
        {
            _testImpl.findComponent(null);
            assertNull(_testImpl.findComponent(""));
            fail();
        }
        catch(NullPointerException e)
        {
            //Success
        }
        catch(Exception e)
        {
            fail();
        }
    }

    public void testWithEmptyExperession() throws Exception
    {
        assertNull(_testImpl.findComponent(""));
    }

    public void testRootExpression() throws Exception
    {
        String expression = ":parent";
        UIComponent root = new UIViewRoot();
        UIComponent parent = new UIPanel();
        
        root.setId("root");
        root.getChildren().add(parent);
        parent.setId("parent");
        parent.getChildren().add(_testImpl);        
        _testImpl.setId("testimpl");

        assertEquals(parent, _testImpl.findComponent(expression));
    }

    public void testRelativeExpression() throws Exception
    {
        String expression = "testimpl";
        
        UIComponent namingContainer = new UINamingContainer();
        UIComponent parent = new UIPanel();
        
        namingContainer.setId("namingContainer");
        namingContainer.getChildren().add(parent);
        parent.setId("parent");
        parent.getChildren().add(_testImpl);
        _testImpl.setId("testimpl");
        
        assertEquals(_testImpl, _testImpl.findComponent(expression));
    }

    public void testComplexRelativeExpression() throws Exception
    {
        String expression = "child1_1:testimpl";
        
        UIComponent namingContainer = new UINamingContainer();
        UIComponent child1_1 = new UINamingContainer();

        namingContainer.setId("namingContainer");
        namingContainer.getChildren().add(child1_1);
        child1_1.setId("child1_1");
        child1_1.getChildren().add(_testImpl);
        _testImpl.setId("testimpl");

        assertEquals(_testImpl, namingContainer.findComponent(expression));
    }

    public void testWithRelativeExpressionNamingContainer() throws Exception
    {
        String expression = "testimpl";
        
        UIComponent namingContainer = new UINamingContainer();
        UIComponent parent = new UIPanel();

        namingContainer.setId("namingContainer");
        namingContainer.getChildren().add(parent);
        parent.setId("parent");
        parent.getChildren().add(_testImpl);
        _testImpl.setId("testimpl");

        assertEquals(_testImpl, namingContainer.findComponent(expression));
    }

}
