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
package org.apache.myfaces.view.facelets.compiler;

import java.io.IOException;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.view.facelets.el.ELText;

public class XMLInstruction implements Instruction
{
    private final static char[] STOP = new char[0];

    private final ELText _text;

    public XMLInstruction(ELText text)
    {
        _text = text;
    }

    @Override
    public void write(FacesContext context) throws IOException
    {
        ResponseWriter rw = context.getResponseWriter();
        rw.writeText(STOP, 0, 0); // hack to get closing elements
        _text.write(rw, context.getELContext());
    }

    @Override
    public Instruction apply(ExpressionFactory factory, ELContext ctx)
    {
        return new XMLInstruction(_text.apply(factory, ctx));
    }

    @Override
    public boolean isLiteral()
    {
        return false;
    }
}
