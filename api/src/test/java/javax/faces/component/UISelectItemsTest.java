package javax.faces.component;

import java.util.ArrayList;
import java.util.List;

import javax.el.ValueExpression;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.el.MockValueExpression;

public class UISelectItemsTest extends AbstractJsfTestCase
{

    public UISelectItemsTest(String name)
    {
        super(name);
    }
    
    public void testStringListAsValue() 
    {
        List<String> value = new ArrayList<String>();
        value.add("#1");
        value.add("#2");
        value.add("#3");
        
        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(value);
        selectItems.setVar("item");
        ValueExpression itemValue = new MockValueExpression("#{item}", Object.class);
        selectItems.setValueExpression("itemValue" , itemValue);
        
        UISelectOne selectOne = new UISelectOne();
        selectOne.getChildren().add(selectItems);
        
        _SelectItemsIterator iter = new _SelectItemsIterator(selectOne);
        List<String> options = new ArrayList<String>();
        while(iter.hasNext())
        {
            options.add((String) iter.next().getValue());
        }
        
        assertEquals(value, options);
    }
}
