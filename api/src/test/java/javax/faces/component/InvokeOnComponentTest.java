package javax.faces.component;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jmock.Mock;

public class InvokeOnComponentTest extends AbstractComponentTest
{

  public InvokeOnComponentTest(String arg0)
  {
    super(arg0);
  }
  
  public void setUp()
  {
      super.setUp();
  }

  public void tearDown()
  {
      super.tearDown();
  }
  
  public void testInvokeOnComp() throws Exception
  {
    UIForm form = new UIForm();
    UIInput i1 = new UIInput();
    i1.setId("_id1");
    UIInput i2 = new UIInput();
    i2.setId("_id2");
    UIInput i3 = new UIInput();
    i3.setId("_id3");
    UIInput i4 = new UIInput();
    i4.setId("_id4");
    form.getChildren().add(i1);
    form.getChildren().add(i4);
    form.getChildren().add(i2);
    form.getChildren().add(i3);
    this.facesContext.getViewRoot().getChildren().add(form);
    Mock mock = mock(ContextCallback.class);
    ContextCallback cc = (ContextCallback) mock.proxy();
   
    mock.expects(once()).method("invokeContextCallback").with(eq(facesContext), eq(i2));
    
    this.facesContext.getViewRoot().invokeOnComponent(facesContext, i2.getClientId(facesContext), cc);
    
    mock.verify();
  }
  
  public static Test suite()
  {
    return new TestSuite(InvokeOnComponentTest.class);
  }
  
}