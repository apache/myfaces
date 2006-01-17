/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.myfaces.AbstractTestCase;
import org.apache.myfaces.mock.MockApplication;
import org.apache.myfaces.mock.MockFacesContext;
import javax.faces.component.UIViewRoot;

public class UISelectManyTest extends AbstractTestCase {

  public UISelectManyTest(String name) {
    super(name);
  }

  
  public void testValidateRequiredNull() {
    UIViewRoot view = new UIViewRoot();
    view.setLocale(_TEST_LOCALE);

    MockApplication application = new MockApplication();
    MockFacesContext context = new MockFacesContext();
    context.setViewRoot(view);
    context.setApplication(application);

    UISelectMany selectMany = new UISelectMany();
    selectMany.setId("selectMany");
    selectMany.setRendererType(null);
    selectMany.setRequired(true);
    List children = selectMany.getChildren();

    UISelectItem one = new UISelectItem();
    one.setItemValue(new Integer(1));
    children.add(one);

    UISelectItem two = new UISelectItem();
    two.setItemValue(new Integer(2));
    children.add(two);

    UISelectItem three = new UISelectItem();
    three.setItemValue(new Integer(3));
    children.add(three);

    selectMany.validateValue(context, null);

    assertFalse(selectMany.isValid());
  }

  public void testValidateRequiredEmptyList() {
    UIViewRoot view = new UIViewRoot();
    view.setLocale(_TEST_LOCALE);

    MockApplication application = new MockApplication();
    MockFacesContext context = new MockFacesContext();
    context.setViewRoot(view);
    context.setApplication(application);

    UISelectMany selectMany = new UISelectMany();
    selectMany.setId("selectMany");
    selectMany.setRendererType(null);
    selectMany.setRequired(true);
    List children = selectMany.getChildren();

    UISelectItem one = new UISelectItem();
    one.setItemValue(new Integer(1));
    children.add(one);

    UISelectItem two = new UISelectItem();
    two.setItemValue(new Integer(2));
    children.add(two);

    UISelectItem three = new UISelectItem();
    three.setItemValue(new Integer(3));
    children.add(three);

    selectMany.validateValue(context, Collections.EMPTY_LIST);

    assertFalse(selectMany.isValid());
  }

  public void testValidateIntArray() {
    UIViewRoot view = new UIViewRoot();
    view.setLocale(_TEST_LOCALE);

    MockApplication application = new MockApplication();

    MockFacesContext context = new MockFacesContext();
    context.setViewRoot(view);
    context.setApplication(application);

    UISelectMany selectMany = new UISelectMany();
    selectMany.setId("selectMany");
    selectMany.setRendererType(null);
    List children = selectMany.getChildren();

    UISelectItem one = new UISelectItem();
    one.setItemValue(new Integer(1));
    children.add(one);

    UISelectItem two = new UISelectItem();
    two.setItemValue(new Integer(2));
    children.add(two);

    UISelectItem three = new UISelectItem();
    three.setItemValue(new Integer(3));
    children.add(three);

    selectMany.validateValue(context, new int[] { 2, 3 });

    assertTrue(selectMany.isValid());
  }

  public void testValidateStringArray() {
    UIViewRoot view = new UIViewRoot();
    view.setLocale(_TEST_LOCALE);

    MockApplication application = new MockApplication();

    MockFacesContext context = new MockFacesContext();
    context.setViewRoot(view);
    context.setApplication(application);

    UISelectMany selectMany = new UISelectMany();
    selectMany.setId("selectMany");
    selectMany.setRendererType(null);
    List children = selectMany.getChildren();

    UISelectItem one = new UISelectItem();
    one.setItemValue("1");
    children.add(one);

    UISelectItem two = new UISelectItem();
    two.setItemValue("2");
    children.add(two);

    UISelectItem three = new UISelectItem();
    three.setItemValue("3");
    children.add(three);

    selectMany.validateValue(context, new String[] { "2", "3" });

    assertTrue(selectMany.isValid());
  }

  public void testValidateStringList() {
    UIViewRoot view = new UIViewRoot();
    view.setLocale(_TEST_LOCALE);

    MockApplication application = new MockApplication();

    MockFacesContext context = new MockFacesContext();
    context.setViewRoot(view);
    context.setApplication(application);

    UISelectMany selectMany = new UISelectMany();
    selectMany.setId("selectMany");
    selectMany.setRendererType(null);
    List children = selectMany.getChildren();

    UISelectItem one = new UISelectItem();
    one.setItemValue("1");
    children.add(one);

    UISelectItem two = new UISelectItem();
    two.setItemValue("2");
    children.add(two);

    UISelectItem three = new UISelectItem();
    three.setItemValue("3");
    children.add(three);

    selectMany.validateValue(context, Arrays.asList(new String[] { "2", "3" }));

    assertTrue(selectMany.isValid());
  }

  static private final Locale _TEST_LOCALE = new Locale("xx", "TEST");
}
