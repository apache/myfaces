package javax.faces.component;

import javax.faces.context.FacesContext;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.testng.annotations.BeforeMethod;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class UIComponentTestBase
{
    protected IMocksControl _mocksControl;
    protected FacesContext _facesContext;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws Exception
    {
        _mocksControl = EasyMock.createNiceControl();
        _facesContext = _mocksControl.createMock(FacesContext.class);
    }
}