/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.compiler;

import java.io.IOException;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.facelets.el.ELAdaptor;
import com.sun.facelets.el.ELText;

public class XMLInstruction implements Instruction
{

    private final static char[] STOP = new char[0];

    private final ELText text;

    public XMLInstruction(ELText text)
    {
        this.text = text;
    }

    public void write(FacesContext context) throws IOException
    {
        ResponseWriter rw = context.getResponseWriter();
        rw.writeText(STOP, 0, 0); // hack to get closing elements
        this.text.write(rw, ELAdaptor.getELContext(context));
    }

    public Instruction apply(ExpressionFactory factory, ELContext ctx)
    {
        return new XMLInstruction(text.apply(factory, ctx));
    }

    public boolean isLiteral()
    {
        return false;
    }
}
