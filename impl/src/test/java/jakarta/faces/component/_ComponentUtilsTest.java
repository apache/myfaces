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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.myfaces.mock.MockRenderedValueExpression;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Test;

public class _ComponentUtilsTest extends AbstractJsfTestCase {

    @Test
    public void testIsRendered() {
        UIComponent uiComponent = new UIOutput();
        boolean rendered = _ComponentUtils.isRendered(facesContext, uiComponent);
        assertTrue(rendered);

        uiComponent.setRendered(false);
        rendered = _ComponentUtils.isRendered(facesContext, uiComponent);
        assertFalse(rendered);

        UIOutput uiOutput = new UIOutput();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, uiOutput, false);
        rendered = _ComponentUtils.isRendered(facesContext, uiComponent);
        assertFalse(rendered);
        assertEquals("isRendered must not change current component", parent,
                UIComponent.getCurrentComponent(facesContext));
    }

}
