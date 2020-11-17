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

import javax.el.ExpressionFactory;
import org.junit.Assert;
import org.junit.Test;

public class ELTextTest {
    
    @Test
    public void parseLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText elText = ELText.parse(expressionFactory, null, "blub", null);
        Assert.assertEquals("blub", elText.toString());
        
        elText = ELText.parse(expressionFactory, null, "", null);
        Assert.assertEquals(null, elText);
    }

    @Test
    public void parseValueExpressionAndLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText elText = ELText.parse(expressionFactory, null, "#{myblub.blub} blub", null);
        Assert.assertTrue(elText instanceof ELText.ELTextComposite);
        Assert.assertEquals(2, ((ELText.ELTextComposite) elText).getElements().length);
        Assert.assertEquals(" blub", ((ELText.ELTextComposite) elText).getElements()[1].toString());
        
        elText = ELText.parse(expressionFactory, null, "#{myblub.blub} blub #{myblub.blub}", null);
        Assert.assertTrue(elText instanceof ELText.ELTextComposite);
        Assert.assertEquals(3, ((ELText.ELTextComposite) elText).getElements().length);
        Assert.assertEquals(" blub ", ((ELText.ELTextComposite) elText).getElements()[1].toString());
    }

    @Test
    public void parseAsArrayLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText[] elText = ELText.parseAsArray(expressionFactory, null, "blub", null);
        Assert.assertEquals("blub", elText[0].toString());
        
        elText = ELText.parseAsArray(expressionFactory, null, "", null);
        Assert.assertTrue(elText == null);
    }

    @Test
    public void parseAsArrayValueExpressionAndLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText[] elText = ELText.parseAsArray(expressionFactory, null, "#{myblub.blub} blub", null);
        Assert.assertEquals(2, elText.length);
        Assert.assertEquals(" blub", elText[1].toString());
        
        elText = ELText.parseAsArray(expressionFactory, null, "#{myblub.blub} blub #{myblub.blub}", null);
        Assert.assertEquals(3, elText.length);
        Assert.assertEquals(" blub ", elText[1].toString());
    }
    
}
