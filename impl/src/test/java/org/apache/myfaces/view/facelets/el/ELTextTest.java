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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.el.ExpressionFactory;
import org.junit.jupiter.api.Test;

class ELTextTest {

    @Test
    void parseLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText elText = ELText.parse(expressionFactory, null, "blub", null);
        assertEquals("blub", elText.toString());
        
        elText = ELText.parse(expressionFactory, null, "", null);
        assertNull(elText);
    }

    @Test
    void parseValueExpressionAndLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText elText = ELText.parse(expressionFactory, null, "#{myblub.blub} blub", null);
        assertInstanceOf(ELText.ELTextComposite.class, elText);
        assertEquals(2, ((ELText.ELTextComposite) elText).getElements().length);
        assertEquals(" blub", ((ELText.ELTextComposite) elText).getElements()[1].toString());
        
        elText = ELText.parse(expressionFactory, null, "#{myblub.blub} blub #{myblub.blub}", null);
        assertInstanceOf(ELText.ELTextComposite.class, elText);
        assertEquals(3, ((ELText.ELTextComposite) elText).getElements().length);
        assertEquals(" blub ", ((ELText.ELTextComposite) elText).getElements()[1].toString());
    }

    @Test
    void parseAsArrayLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText[] elText = ELText.parseAsArray(expressionFactory, null, "blub", null);
        assertEquals("blub", elText[0].toString());
        
        elText = ELText.parseAsArray(expressionFactory, null, "", null);
        assertNull(elText);
    }

    @Test
    void parseAsArrayValueExpressionAndLiteral()
    {
        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();
        
        ELText[] elText = ELText.parseAsArray(expressionFactory, null, "#{myblub.blub} blub", null);
        assertEquals(2, elText.length);
        assertEquals(" blub", elText[1].toString());
        
        elText = ELText.parseAsArray(expressionFactory, null, "#{myblub.blub} blub #{myblub.blub}", null);
        assertEquals(3, elText.length);
        assertEquals(" blub ", elText[1].toString());
    }
    
}
