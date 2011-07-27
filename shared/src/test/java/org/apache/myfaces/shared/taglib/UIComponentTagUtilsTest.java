/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.myfaces.shared.taglib;

import junit.framework.Test;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;

/**
 * TestCase for UIComponentTagUtils
 */
public class UIComponentTagUtilsTest extends AbstractJsfTestCase {

    public UIComponentTagUtilsTest(String name) {
        super(name);
    }

    //public static Test suite() {
    //    return null; // keep this method or maven won't run it
    //}

    /**
     * Simple custom component that counts how many times its
     * getValue method is called.
     */
    public static class LocalComponent extends HtmlInputText {
        public int accessCount = 0;
        public Object getValue() {
            ++accessCount;
            return super.getValue();
        }
    };

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that the setStringProperty util method works as expected.
     */
    public void testSetStringProperty() {
        UIComponent component = new HtmlInputText();
        String propName;
        String propValue;
        String oldValue;

        // first, set an attribute that doesn't have a corresponding
        // getter on the underlying component..
        propName = "someName";
        propValue = "someValue";
        oldValue = (String) component.getAttributes().get(propName);
        assertNull(oldValue);

        UIComponentTagUtils.setStringProperty(facesContext, component, propName, propValue);

        oldValue = (String) component.getAttributes().get(propName);
        assertEquals(oldValue, propValue);

        // Now set an attribute that does have a corresponding getter on the
        // underlying component.
        propName = "style";
        propValue = "someStyle";
        oldValue = (String) component.getAttributes().get(propName);
        assertNull(oldValue);

        org.apache.myfaces.shared.taglib.UIComponentTagUtils.setStringProperty(facesContext, component, propName, propValue);

        oldValue = (String) component.getAttributes().get(propName);
        assertEquals(oldValue, propValue);
    }
}
