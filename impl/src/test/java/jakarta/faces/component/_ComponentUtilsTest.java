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

import org.apache.myfaces.core.api.shared.ComponentUtils;

import org.apache.myfaces.test.mock.MockRenderedValueExpression;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class _ComponentUtilsTest extends AbstractJsfTestCase {

    @Test
    public void testIsRendered() {
        UIComponent uiComponent = new UIOutput();
        boolean rendered = ComponentUtils.isRendered(facesContext, uiComponent);
        Assertions.assertTrue(rendered);

        uiComponent.setRendered(false);
        rendered = ComponentUtils.isRendered(facesContext, uiComponent);
        Assertions.assertFalse(rendered);

        UIOutput uiOutput = new UIOutput();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, uiOutput, false);
        rendered = ComponentUtils.isRendered(facesContext, uiComponent);
        Assertions.assertFalse(rendered);
        Assertions.assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "isRendered must not change current component");
    }

}
