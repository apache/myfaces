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

import jakarta.el.ValueExpression;
import jakarta.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.myfaces.core.api.shared.SelectItemsIterator;
import org.apache.myfaces.core.api.shared.SelectItemsUtil;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.el.MockValueExpression;
import  org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class _SelectItemsUtilTest extends AbstractFacesTestCase
{

    private static final String NO_SELECTION_ITEM_VALUE = "1.0";
    private UISelectOne uiComponent;
    private Float value;
    private SelectItemsIterator iterator;
    private UISelectItem noSelectionOption;
    private UISelectItem selectItem1;
    private UISelectItem selectItem2;
    private UISelectItem selectItem3;

    @Override
    @BeforeEach
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

    @Override
    @AfterEach
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
        
        Assertions.assertTrue(matchValue, "Value Float 1.2 must match SelectItem.value \"1.2\" (type of String)");
        
        Float valueNotInSelectItems = Float.valueOf("2.0");
        matchValue = SelectItemsUtil.matchValue(facesContext, uiComponent, valueNotInSelectItems, iterator, null);
        Assertions.assertFalse(matchValue);
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
        
        Assertions.assertTrue(matchValue, "Value Enum THREE must match SelectItem.value \"THREE\" (type of String)");
        
        enumValue = MockEnum.FOUR;
        matchValue = SelectItemsUtil.matchValue(facesContext, uiComponent, enumValue, iterator, null);
        Assertions.assertFalse(matchValue);
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
        
        Assertions.assertTrue(matchValue, "Value Enum TWO must match SelectItem.value \"TWO\" (type of String)");
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
        Assertions.assertTrue(noSelectionOption);
        
        Float valueNotInSelectItems = Float.valueOf("2.0");
        noSelectionOption = SelectItemsUtil.isNoSelectionOption(facesContext, uiComponent, valueNotInSelectItems, iterator, null);
        Assertions.assertFalse(noSelectionOption);
        
    }

    @Test
    public void testSelectListAsValue()
    {
        List<SelectItem> values = new ArrayList<>();

        values.add(new SelectItem("#1", "D1"));
        values.add(new SelectItem("#2", "D2"));
        values.add(new SelectItem("#3", "D3"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(values);
        selectItems.getAttributes().put("var", "item");
        ValueExpression itemValue = new MockValueExpression("#{item.label}", Object.class);
        ValueExpression itemLabel = new MockValueExpression("#{item.value}", Object.class);
        ValueExpression itemDescription = new MockValueExpression("#{item.value}", Object.class);

        selectItems.setValueExpression("itemValue", itemValue);
        selectItems.setValueExpression("itemLabel", itemLabel);
        selectItems.setValueExpression("itemDescription", itemDescription);
        UISelectOne selectOne = new UISelectOne();
        selectOne.getChildren().add(selectItems);

        SelectItemsIterator iter = new SelectItemsIterator(selectOne, facesContext);
        List<String> options = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        while (iter.hasNext())
        {
            SelectItem next = iter.next();
            options.add((String) next.getValue());
            labels.add(next.getLabel());
            descriptions.add(next.getDescription());
        }
        Assertions.assertAll(
                () -> Assertions.assertEquals(values.stream().map(SelectItem::getLabel).collect(Collectors.toList()), options),
                () -> Assertions.assertEquals(values.stream().map(SelectItem::getValue).collect(Collectors.toList()), labels),
                () -> Assertions.assertEquals(values.stream().map(SelectItem::getValue).collect(Collectors.toList()), descriptions));
    }
}
