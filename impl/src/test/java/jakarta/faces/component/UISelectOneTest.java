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

import java.util.List;
import java.util.Locale;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UISelectOneTest extends AbstractFacesTestCase
{
    
    @Test
    public void testValidateRequiredNull()
    {

        facesContext.getViewRoot().setLocale(_TEST_LOCALE);

        UISelectOne selectOne = new UISelectOne();
        selectOne.setId("selectOne");
        selectOne.setRendererType(null);
        selectOne.setRequired(true);
        List<UIComponent> children = selectOne.getChildren();

        UISelectItem one = new UISelectItem();
        one.setItemValue(new Integer(1));
        children.add(one);

        UISelectItem two = new UISelectItem();
        two.setItemValue(new Integer(2));
        children.add(two);

        UISelectItem three = new UISelectItem();
        three.setItemValue(new Integer(3));
        children.add(three);

        selectOne.validateValue(facesContext, null);

        Assertions.assertFalse(selectOne.isValid());
        Assertions.assertEquals(1, facesContext.getMessageList().size());
    }
    
    @Test
    public void testValidateNotRequiredValid()
    {

        facesContext.getViewRoot().setLocale(_TEST_LOCALE);

        UISelectOne selectOne = new UISelectOne();
        selectOne.setId("selectOne");
        selectOne.setRendererType(null);
        selectOne.setRequired(false);
        List<UIComponent> children = selectOne.getChildren();

        UISelectItem one = new UISelectItem();
        one.setItemValue(new Integer(1));
        children.add(one);

        UISelectItem two = new UISelectItem();
        two.setItemValue(new Integer(2));
        children.add(two);

        UISelectItem three = new UISelectItem();
        three.setItemValue(new Integer(3));
        children.add(three);

        selectOne.validateValue(facesContext, 1);

        Assertions.assertTrue(selectOne.isValid());
        Assertions.assertEquals(0, facesContext.getMessageList().size());
    }
    
    @Test
    public void testValidateNotRequiredNotValid()
    {

        facesContext.getViewRoot().setLocale(_TEST_LOCALE);

        UISelectOne selectOne = new UISelectOne();
        selectOne.setId("selectOne");
        selectOne.setRendererType(null);
        selectOne.setRequired(false);
        List<UIComponent> children = selectOne.getChildren();

        UISelectItem one = new UISelectItem();
        one.setItemValue(new Integer(1));
        children.add(one);

        UISelectItem two = new UISelectItem();
        two.setItemValue(new Integer(2));
        children.add(two);

        UISelectItem three = new UISelectItem();
        three.setItemValue(new Integer(3));
        children.add(three);

        selectOne.validateValue(facesContext, 4);

        Assertions.assertFalse(selectOne.isValid());
        Assertions.assertEquals(1, facesContext.getMessageList().size());
    }
    
    @Test
    public void testValidateRequiredNotSelectOption()
    {

        facesContext.getViewRoot().setLocale(_TEST_LOCALE);

        UISelectOne selectOne = new UISelectOne();
        selectOne.setId("selectOne");
        selectOne.setRendererType(null);
        selectOne.setRequired(true);
        List<UIComponent> children = selectOne.getChildren();

        UISelectItem one = new UISelectItem();
        one.setItemValue(new Integer(1));
        children.add(one);

        UISelectItem two = new UISelectItem();
        two.setItemValue(new Integer(2));
        children.add(two);

        UISelectItem three = new UISelectItem();
        three.setItemValue(new Integer(3));
        three.setNoSelectionOption(true);
        children.add(three);

        selectOne.validateValue(facesContext, 3);

        Assertions.assertFalse(selectOne.isValid());
        Assertions.assertEquals(1, facesContext.getMessageList().size());
    }
    
    @Test
    public void testValidateNotRequiredNotSelectOption()
    {

        facesContext.getViewRoot().setLocale(_TEST_LOCALE);

        UISelectOne selectOne = new UISelectOne();
        selectOne.setId("selectOne");
        selectOne.setRendererType(null);
        selectOne.setRequired(false);
        List<UIComponent> children = selectOne.getChildren();

        UISelectItem one = new UISelectItem();
        one.setItemValue(new Integer(1));
        children.add(one);

        UISelectItem two = new UISelectItem();
        two.setItemValue(new Integer(2));
        children.add(two);

        UISelectItem three = new UISelectItem();
        three.setItemValue(new Integer(3));
        three.setNoSelectionOption(true);
        children.add(three);

        selectOne.validateValue(facesContext, 3);

        Assertions.assertTrue(selectOne.isValid());
        Assertions.assertEquals(0, facesContext.getMessageList().size());
    }
    
    /**
     * MYFACES-4783: a grouped radio next to a UISelectOne without a group must not throw a NPE.
     */
    @Test
    public void testGroupedRadioNextToUngroupedSelectOne()
    {
        facesContext.getViewRoot().setLocale(_TEST_LOCALE);

        UIForm form = new UIForm();
        form.setId("form");
        facesContext.getViewRoot().getChildren().add(form);

        UISelectOne menu = new UISelectOne();
        menu.setId("menu");
        menu.setRendererType(null);
        UISelectItem menuItem = new UISelectItem();
        menuItem.setItemValue("x");
        menu.getChildren().add(menuItem);
        form.getChildren().add(menu);

        UISelectOne radio = new UISelectOne();
        radio.setId("radio");
        radio.setRendererType(null);
        radio.setGroup("group");
        UISelectItem radioItem = new UISelectItem();
        radioItem.setItemValue("a");
        radio.getChildren().add(radioItem);
        form.getChildren().add(radio);

        Assertions.assertDoesNotThrow(() -> radio.processValidators(facesContext));

        radio.validateValue(facesContext, "a");
        Assertions.assertTrue(radio.isValid());
        Assertions.assertEquals(0, facesContext.getMessageList().size());
    }

    static private final Locale _TEST_LOCALE = new Locale("xx", "TEST");

}
