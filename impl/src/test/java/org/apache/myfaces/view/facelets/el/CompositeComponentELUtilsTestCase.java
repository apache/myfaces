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
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Leonardo Uribe
 */
public class CompositeComponentELUtilsTestCase extends FaceletTestCase
{
    
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    @Test
    public void test_cc_attrs_method_expression1() throws Exception
    {
        Assertions.assertTrue(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{cc.attrs.someProp}"));
        Assertions.assertTrue(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{ cc.attrs.someProp}"));
        Assertions.assertFalse(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{cc.attrs.someProp.someKey}"));
        Assertions.assertFalse(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{xy:call(cc.attrs.someProp)}"));
        Assertions.assertFalse(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{xy:call( cc.attrs.someProp)}"));
        Assertions.assertFalse(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{xy:call(zz, cc.attrs.someProp)}"));        
        Assertions.assertFalse(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{xy:call(zz,cc.attrs.someProp)}"));
        Assertions.assertTrue(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{cc.attrs.method(someProp)}"));
        Assertions.assertTrue(
            CompositeComponentELUtils.isCompositeComponentAttrsMethodExpression(
            "#{cc.attrs.method( someProp, someAttr )}"));
    }
}
