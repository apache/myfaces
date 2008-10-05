package javax.faces.component;

import javax.faces.component.html.HtmlInputText;

import org.apache.shale.test.base.AbstractJsfTestCase;

public class UIComponentAttributesTest extends AbstractJsfTestCase
{

    public UIComponentAttributesTest(String arg0)
    {
        super(arg0);
    }

    private HtmlInputText input;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        input = new HtmlInputText();
        input.setId("testId");
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        input = null;
    }

    public void testSetNullAttributeOnValidProperty()
    {
        input.getAttributes().put("style", null);
    }

    public void testSetNullAttributeOnInvalidProperty()
    {
        try
        {
            input.getAttributes().put("someBogus", null);
            fail("Should have thrown NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
    }
}