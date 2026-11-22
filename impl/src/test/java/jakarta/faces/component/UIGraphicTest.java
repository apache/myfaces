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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.el.ValueExpression;

import org.easymock.classextension.EasyMock;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class UIGraphicTest
{

    @Test
    void urlValue()
    {
        UIGraphic graphic = new UIGraphic();
        graphic.setValue("xxx");
        assertEquals("xxx", graphic.getUrl());
        graphic.setUrl("xyz");
        assertEquals("xyz", graphic.getValue());
    }

    @Test
    void urlValueExpression()
    {
        UIGraphic graphic = new UIGraphic();
        ValueExpression expression = EasyMock.createMock(ValueExpression.class);
        graphic.setValueExpression("url", expression);
        assertSame(graphic.getValueExpression("value"), expression);

        expression = EasyMock.createMock(ValueExpression.class);
        graphic.setValueExpression("value", expression);
        assertSame(graphic.getValueExpression("url"), expression);
    }

}
