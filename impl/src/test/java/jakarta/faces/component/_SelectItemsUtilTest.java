/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package jakarta.faces.component;

import jakarta.faces.component.UISelectOne;
import org.apache.myfaces.core.api.shared.SelectItemsIterator;
import org.apache.myfaces.core.api.shared.SelectItemsUtil;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class _SelectItemsUtilTest extends AbstractJsfTestCase
{

    private static final String NO_SELECTION_ITEM_VALUE = "1.0";
    private UISelectOne uiComponent;
    private Float value;
    private SelectItemsIterator iterator;
    private UISelectItem noSelectionOption;
    private UISelectItem selectItem1;
    private UISelectItem selectItem2;
    private UISelectItem selectItem3;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        uiComponent = new UISelectOne();
        
        value = Float.valueOf("1.2");

        noSelectionOption = new UISelectItem();
        noSelectionOption.setNoSelectionOption(true);
        noSelectionOption.setItemValue(NO_SELECTION_ITEM_VALUE);
        uiComponent.getChildren().add(noSelectionOption);
        
        selectItem1 = new UISelectItem();
        selectItem1.setItemValue("1.1");
        uiComponent.getChildren().add(selectItem1);
        selectItem2 = new UISelectItem();
        selectItem2.setItemValue("1.2");
        uiComponent.getChildren().add(selectItem2);
        selectItem3 = new UISelectItem();
        selectItem3.setItemValue("1.3");
        uiComponent.getChildren().add(selectItem3);
        
        iterator = new SelectItemsIterator(uiComponent, facesContext);
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        uiComponent = null;
        value = null;
        iterator = null;
        noSelectionOption = null;
        selectItem1 = null;
        selectItem2 = null;
        selectItem3 = null;
    }

    @Test
    public void testMatchValue()
    {
        
        boolean matchValue = SelectItemsUtil.matchValue(facesContext, uiComponent, value, iterator, null);
        
        Assert.assertTrue("Value Float 1.2 must match SelectItem.value \"1.2\" (type of String)", matchValue);
        
        Float valueNotInSelectItems = Float.valueOf("2.0");
        matchValue = SelectItemsUtil.matchValue(facesContext, uiComponent, valueNotInSelectItems, iterator, null);
        Assert.assertFalse(matchValue);
    }
    
    @Test
    public void testMatchValueWithEnums() throws Exception
    {
        noSelectionOption.setItemValue("NONE");
        selectItem1.setItemValue("ONE");
        selectItem2.setItemValue("TWO");
        selectItem3.setItemValue("THREE");
        iterator = new SelectItemsIterator(uiComponent, facesContext);
        
        Object enumValue = MockEnum.THREE;
        boolean matchValue = SelectItemsUtil.matchValue(facesContext, uiComponent, enumValue, iterator, null);
        
        Assert.assertTrue("Value Enum THREE must match SelectItem.value \"THREE\" (type of String)", matchValue);
        
        enumValue = MockEnum.FOUR;
        matchValue = SelectItemsUtil.matchValue(facesContext, uiComponent, enumValue, iterator, null);
        Assert.assertFalse(matchValue);
    }
    
    @Test 
    public void testMatchValueWithEnumsNoExtends() throws Exception
    {
        noSelectionOption.setItemValue("NONE");
        selectItem1.setItemValue("ONE");
        selectItem2.setItemValue("TWO");
        selectItem3.setItemValue("THREE");
        iterator = new SelectItemsIterator(uiComponent, facesContext);
        
        Object enumValue = MockEnum.TWO;
        boolean matchValue = SelectItemsUtil.matchValue(facesContext, uiComponent, enumValue, iterator, null);
        
        Assert.assertTrue("Value Enum TWO must match SelectItem.value \"TWO\" (type of String)", matchValue);
    }
    
    private static enum MockEnum {
        NONE,
        ONE {

            @Override
            public String toString()
            {
                return "ONE";
            } 
            
        },TWO,
        THREE {
 
            @Override
            public String toString()
            {
                return "THREE";
            } 
            
        }, FOUR
    }

    @Test
    public void testIsNoSelectionOption()
    {
        Float value = Float.parseFloat(NO_SELECTION_ITEM_VALUE);
        boolean noSelectionOption = SelectItemsUtil.isNoSelectionOption(facesContext, uiComponent, value, iterator, null);
        Assert.assertTrue(noSelectionOption);
        
        Float valueNotInSelectItems = Float.valueOf("2.0");
        noSelectionOption = SelectItemsUtil.isNoSelectionOption(facesContext, uiComponent, valueNotInSelectItems, iterator, null);
        Assert.assertFalse(noSelectionOption);
        
    }

}
