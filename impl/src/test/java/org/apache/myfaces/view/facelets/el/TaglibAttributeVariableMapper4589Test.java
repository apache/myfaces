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
package org.apache.myfaces.view.facelets.el;

import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;

import org.apache.myfaces.test.base.junit.AbstractFacesConfigurableMockTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit coverage for MYFACES-4589 at {@link TaglibAttributeVariableMapper} level: a taglib short name
 * bound to a composite pass-through ({@code #{cc.attrs.*}}) must not be returned from
 * {@link jakarta.el.VariableMapper#resolveVariable}, so {@code #{color.blue}} can resolve a same-named
 * managed bean via {@link jakarta.el.ELResolver} at render time.
 */
@Disabled
public class TaglibAttributeVariableMapper4589Test extends AbstractFacesConfigurableMockTestCase
{

    @Test
    public void testCcAttrsPassThroughBindingMustNotResolveOnVariableMapper4589()
    {
        ExpressionFactory ef = application.getExpressionFactory();
        ValueExpression ccAttrsColorVe = ef.createValueExpression(
                facesContext.getELContext(), "#{cc.attrs.color}", Object.class);

        DefaultVariableMapper delegate = new DefaultVariableMapper();
        TaglibAttributeVariableMapper mapper = new TaglibAttributeVariableMapper(delegate);
        mapper.setVariable("color", ccAttrsColorVe);

        Assertions.assertNull(mapper.resolveVariable("color"),
                "Expected null so AstIdentifier falls through to ELResolver for a CDI bean named 'color'");
    }

    @Test
    public void testNonCcAttrsTaglibBindingStillResolvesOnVariableMapper4585()
    {
        ExpressionFactory ef = application.getExpressionFactory();
        ValueExpression converterVe = ef.createValueExpression(
                facesContext.getELContext(), "#{requestScope.user4585Bean.converter}", Object.class);

        DefaultVariableMapper delegate = new DefaultVariableMapper();
        TaglibAttributeVariableMapper mapper = new TaglibAttributeVariableMapper(delegate);
        mapper.setVariable("converter", converterVe);

        ValueExpression resolved = mapper.resolveVariable("converter");
        Assertions.assertNotNull(resolved);
        Assertions.assertTrue(resolved.getExpressionString().contains("user4585Bean"),
                "MYFACES-4585: ordinary taglib bindings must remain on VariableMapper");
    }
}
