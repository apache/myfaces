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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ELTextTest {
    
    @Test
    public void parseLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText elText = ELText.parse(expressionFactory, null, "blub", null);
        Assertions.assertEquals("blub", elText.toString());
        
        elText = ELText.parse(expressionFactory, null, "", null);
        Assertions.assertEquals(null, elText);
    }

    @Test
    public void parseValueExpressionAndLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText elText = ELText.parse(expressionFactory, null, "#{myblub.blub} blub", null);
        Assertions.assertTrue(elText instanceof ELText.ELTextComposite);
        Assertions.assertEquals(2, ((ELText.ELTextComposite) elText).getElements().length);
        Assertions.assertEquals(" blub", ((ELText.ELTextComposite) elText).getElements()[1].toString());
        
        elText = ELText.parse(expressionFactory, null, "#{myblub.blub} blub #{myblub.blub}", null);
        Assertions.assertTrue(elText instanceof ELText.ELTextComposite);
        Assertions.assertEquals(3, ((ELText.ELTextComposite) elText).getElements().length);
        Assertions.assertEquals(" blub ", ((ELText.ELTextComposite) elText).getElements()[1].toString());
    }

    @Test
    public void parseAsArrayLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText[] elText = ELText.parseAsArray(expressionFactory, null, "blub", null);
        Assertions.assertEquals("blub", elText[0].toString());
        
        elText = ELText.parseAsArray(expressionFactory, null, "", null);
        Assertions.assertTrue(elText == null);
    }

    @Test
    public void parseAsArrayValueExpressionAndLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText[] elText = ELText.parseAsArray(expressionFactory, null, "#{myblub.blub} blub", null);
        Assertions.assertEquals(2, elText.length);
        Assertions.assertEquals(" blub", elText[1].toString());
        
        elText = ELText.parseAsArray(expressionFactory, null, "#{myblub.blub} blub #{myblub.blub}", null);
        Assertions.assertEquals(3, elText.length);
        Assertions.assertEquals(" blub ", elText[1].toString());
    }
    
}
